package nsu.example.cary.my12306app.activity.order;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
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
import nsu.example.cary.my12306app.activity.ticket.TicketOrderSubmitStep4Activity;
import nsu.example.cary.my12306app.bean.Order;
import nsu.example.cary.my12306app.bean.Passenger;
import nsu.example.cary.my12306app.utils.Constant;
import nsu.example.cary.my12306app.utils.NetCheckUtil;
import nsu.example.cary.my12306app.utils.ZxingUtils;

public class TicketOrderPayActivity extends AppCompatActivity {

    private TextView tvTicketOrderPayId, tvTicketOrderPayCheck;
    private ListView lvTicketOrderPayList;
    private Order order;
    private List<Map<String, Object>> data;
    private SimpleAdapter adapter;
    private String payName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_order_pay);

        tvTicketOrderPayId = findViewById(R.id.tvTicketOrderPayId);
        tvTicketOrderPayCheck = findViewById(R.id.tvTicketOrderPayCheck);
        lvTicketOrderPayList = findViewById(R.id.lvTicketOrderPayList);

        order = (Order) getIntent().getSerializableExtra("order");
        Log.d("TicketOrderPay-tui",order.toString());
        tvTicketOrderPayId.setText(order.getId());

        data = new ArrayList<>();
        Log.d("TicketOrderPay:", order.toString());

        for (int i = 0; i < order.getPassengerList().size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("PayName", order.getPassengerList().get(i).getName());
            map.put("PayTrainNo", order.getTrain().getTrainNo());
            map.put("PayDate", order.getTrain().getStartTrainDate());
            map.put("PaySeatName", order.getPassengerList().get(i).getSeat().getSeatNo());
            data.add(map);
        }

        adapter = new SimpleAdapter(
                TicketOrderPayActivity.this,
                data,
                R.layout.item_order_no_pay_cance1,
                new String[]{"PayName","PayTrainNo","PayDate","PaySeatName"},
                new int[]{R.id.NoPayName,R.id.NoPayTrainNo,R.id.NoPayDate,R.id.NoPaySeatNO});
        lvTicketOrderPayList.setAdapter(adapter);

        lvTicketOrderPayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(TicketOrderPayActivity.this,data.get(position).get("PayDate").toString(),Toast.LENGTH_SHORT).show();
                for (int i = 0; i < data.size(); i++) {
                    payName = data.get(i).get("PayName").toString();
                }


                AlertDialog.Builder builder = new AlertDialog.Builder(TicketOrderPayActivity.this);
                final String str[] = {"退票","改签"};
                builder.setTitle("请选择操作");
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setItems(str, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String itemData = str[which];
//                        Toast.makeText(TicketOrderPayActivity.this,itemData,Toast.LENGTH_SHORT).show();
                        if ("退票".equals(itemData)){
                            new TicketOrderPayTask().execute();
                        }else if("改签".equals(itemData)){

                        }
                    }
                });
                builder.create().show();
            }
        });

        //查看二维码
        tvTicketOrderPayCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = order.getId()+","+order.getTrain().getTrainNo()+","+order.getPassengerList();
                AlertDialog.Builder builder = new AlertDialog.Builder(TicketOrderPayActivity.this);
                //创建一个view,并且将布局加入到view中
                View view = LayoutInflater.from(TicketOrderPayActivity.this).inflate(R.layout.acitivity_dialog_qr_check,null,false);
                //将view添加到builder中
                builder.setView(view);
                //创建dialog
                final Dialog dialog = builder.create();
                //初始化
                TextView tvQR = view.findViewById(R.id.tvQR);
                TextView tvQR1 = view.findViewById(R.id.tvQR1);
                ImageView ivQR = view.findViewById(R.id.ivQR);
                Button btnQR = view.findViewById(R.id.btnQR);

                ZxingUtils.createQRImage(data,ivQR,800,800);
                //button的点击事件
                btnQR.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

    }

    class TicketOrderPayTask extends AsyncTask<String,String,Object> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!NetCheckUtil.checkNet(TicketOrderPayActivity.this)){
                Toast.makeText(TicketOrderPayActivity.this,"当前网络不可用",Toast.LENGTH_SHORT).show();
                return;
            }
        }

        @Override
        protected Object doInBackground(String... strings) {
            String result = "";
            try {
                URL url = new URL(Constant.HOST + "/otn/Refund");
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
                String params = "orderId="+order.getId();

                int j = 0;
                String id = "";
                String idType = "";
                for (int i = 0; i < order.getPassengerList().size(); i++) {
                    String name = order.getPassengerList().get(i).getName();
                    if (payName.equals(name)){
                        j = i;
                        id = order.getPassengerList().get(j).getId();
                        idType = order.getPassengerList().get(j).getIdType();
                        Log.d("TicketOrderPayParams",id+"==="+idType);
                    }
                }
                params += "&id="+id + "&idType="+idType;

                System.out.println("发往服务器的内容(退票)：" + params);
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
                    System.out.println("服务器返回的结果（退票）：" + result2);

                    //解析
                    Gson gson = new GsonBuilder().create();
                    String orderPay = gson.fromJson(result2,String.class);
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
            if ("1".equals(result)){
                finish();
                Toast.makeText(TicketOrderPayActivity.this,"退票成功！",Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }else {

            }
        }
    }
}
