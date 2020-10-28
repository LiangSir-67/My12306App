package nsu.example.cary.my12306app.activity.ticket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nsu.example.cary.my12306app.R;
import nsu.example.cary.my12306app.adapter.TicketPassengerStep3Adapter;
import nsu.example.cary.my12306app.bean.Order;
import nsu.example.cary.my12306app.bean.Train;
import nsu.example.cary.my12306app.utils.Constant;
import nsu.example.cary.my12306app.utils.NetCheckUtil;

public class TicketOrderSubmitStep4Activity extends AppCompatActivity {
    private TextView tvTicketOrderStep4Pay, tvTicketOrderticketId;
    private ListView lvTicketOrderlist;
    private List<Map<String, Object>> data;
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_order_submit_step4);

        data = new ArrayList<>();

        //接收order
        Intent intent = getIntent();
        final Order order = (Order) intent.getSerializableExtra("order");

        tvTicketOrderStep4Pay = findViewById(R.id.tvTicketOrderStep4Pay);
        lvTicketOrderlist = findViewById(R.id.lvTicketOrderlist);
        tvTicketOrderticketId = findViewById(R.id.tvTicketOrderticketId);

        tvTicketOrderticketId.setText(order.getId());


        if (data == null)
            data = new ArrayList<>();
        data.clear();
        for (int i = 0; i < order.getPassengerList().size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("tvItemName", order.getPassengerList().get(i).getName());
            map.put("tvItemTrainNo", order.getTrain().getTrainNo());
            map.put("tvItemDate", order.getTrain().getStartTrainDate());
            map.put("tvItemSeatNo", order.getPassengerList().get(i).getSeat().getSeatNo());
            data.add(map);
        }

        Log.d("Step4Data==",data.toString());

        adapter = new SimpleAdapter(
                TicketOrderSubmitStep4Activity.this,
                data,
                R.layout.item_order_manage_list,
                new String[]{"tvItemName", "tvItemTrainNo", "tvItemDate", "tvItemSeatNo"},
                new int[]{R.id.tvItemName, R.id.tvItemTrainNo, R.id.tvItemDate, R.id.tvItemSeatNo}
        );
        lvTicketOrderlist.setAdapter(adapter);


        tvTicketOrderStep4Pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TicketOrderSubmitStep4Task().execute();
                Intent intent = new Intent();
                intent.setClass(TicketOrderSubmitStep4Activity.this, TicketPaySuccessStep5Activity.class);
                intent.putExtra("order", order);
                startActivity(intent);
            }
        });
    }

    class TicketOrderSubmitStep4Task extends AsyncTask<String, String, Object> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!NetCheckUtil.checkNet(TicketOrderSubmitStep4Activity.this)) {
                Toast.makeText(TicketOrderSubmitStep4Activity.this, "当前网络不可用", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        @Override
        protected Object doInBackground(String... strings) {
            String result = "";
            try {
                URL url = new URL(Constant.HOST + "/otn/Pay");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(Constant.REQUEST_TIMEOUT);
                httpURLConnection.setReadTimeout(Constant.SO_TIMEOUT);
                //发送Post请求
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                //设置不使用缓存
                httpURLConnection.setUseCaches(false);

                SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
                String value = sp.getString("Cookie", "");

                //设置请求属性
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                httpURLConnection.setRequestProperty("Charset", "UTF-8");
                httpURLConnection.setRequestProperty("Cookie", value);

                //获取输出流
                PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
                //请求参数
                String params = "orderId=" + tvTicketOrderticketId.getText().toString();
                System.out.println("发往服务器的内容(支付)：" + params);
                printWriter.write(params);
                printWriter.flush();
                printWriter.close();

                //获取响应状态
                int resultCode = httpURLConnection.getResponseCode();
                if (resultCode == HttpURLConnection.HTTP_OK) {
                    //打印结果
                    InputStream inputStream = httpURLConnection.getInputStream();
                    StringBuffer sb = new StringBuffer();
                    String readLine = new String();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream, "UTF-8"));
                    while ((readLine = reader.readLine()) != null) {
                        sb.append(readLine).append("\n");
                    }
                    String result2 = sb.toString();
                    System.out.println("服务器返回的结果（支付）：" + result2);

                    //解析
                    Gson gson = new GsonBuilder().create();
                    String orderPay = gson.fromJson(result2, String.class);
                    return orderPay;
                } else {
                    return "result error!";
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if ("1".equals(result)) {
                Toast.makeText(TicketOrderSubmitStep4Activity.this, "支付成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(TicketOrderSubmitStep4Activity.this, "支付失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
