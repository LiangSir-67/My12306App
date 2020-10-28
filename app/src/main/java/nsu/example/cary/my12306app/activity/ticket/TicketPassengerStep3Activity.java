package nsu.example.cary.my12306app.activity.ticket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.message.BasicNameValuePair;

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
import nsu.example.cary.my12306app.adapter.Ticket3AddPassengerAdapter;
import nsu.example.cary.my12306app.adapter.TicketPassengerStep3Adapter;
import nsu.example.cary.my12306app.bean.Order;
import nsu.example.cary.my12306app.bean.Passenger;
import nsu.example.cary.my12306app.bean.Train;
import nsu.example.cary.my12306app.utils.Constant;
import nsu.example.cary.my12306app.utils.NetCheckUtil;

public class TicketPassengerStep3Activity extends AppCompatActivity {
    private Button addPassenger;
    private ListView listView;
    private List<Map<String, Object>> data, resultData;
    public static List<Map<String, Object>> toStep4Data;
    private Button btnTicketPassengerStep3Submit;
    private TextView tvTicketPassengerStep3Ticket, tvTicketPassengerStep3Price;
    private TextView tvTicketPassengerStep3From, tvTicketPassengerStep3To;
    private TextView tvTicketPassengerStep3FromTime, tvTicketPassengerStep3ToTime;
    private TextView tvTicketPassengerStep3Num, tvTicketPassengerStep3Sumtime;
    public static TextView tvTicketPassengerStep3SumPrice;
    private TicketPassengerStep3Adapter adapter;
    private String seatPrice;
    public Train trains;
    public String[] seatName;
    public Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_passenger_step3);

        final Intent intent = getIntent();
        seatName = new String[]{intent.getStringExtra("seatName")};
        Log.d("seatName",seatName[0]);
        String seatNum = intent.getStringExtra("seatNum");
        seatPrice = intent.getStringExtra("seatPrice");
         trains = (Train) intent.getSerializableExtra("trains");
//        Log.d("Step3Data:",trains.toString());


        listView = findViewById(R.id.TicketPassengerStep3List);
        btnTicketPassengerStep3Submit = findViewById(R.id.btnTicketPassengerStep3Submit);
        tvTicketPassengerStep3Ticket = findViewById(R.id.tvTicketPassengerStep3Ticket);
        tvTicketPassengerStep3Price = findViewById(R.id.tvTicketPassengerStep3Price);
        tvTicketPassengerStep3From = findViewById(R.id.tvTicketPassengerStep3From);
        tvTicketPassengerStep3To = findViewById(R.id.tvTicketPassengerStep3To);
        tvTicketPassengerStep3FromTime = findViewById(R.id.tvTicketPassengerStep3FromTime);
        tvTicketPassengerStep3ToTime = findViewById(R.id.tvTicketPassengerStep3ToTime);
        tvTicketPassengerStep3Num = findViewById(R.id.tvTicketPassengerStep3Num);
        tvTicketPassengerStep3Sumtime = findViewById(R.id.tvTicketPassengerStep3Sumtime);
        tvTicketPassengerStep3SumPrice = findViewById(R.id.tvTicketPassengerStep3SumPrice);

        tvTicketPassengerStep3Ticket.setText(seatName[0] + "(" + seatNum + ")");
        tvTicketPassengerStep3Price.setText("¥" + seatPrice);
        tvTicketPassengerStep3From.setText(trains.getFromStationName());
        tvTicketPassengerStep3To.setText(trains.getToStationName());
        tvTicketPassengerStep3FromTime.setText(trains.getStartTime());
        tvTicketPassengerStep3ToTime.setText(trains.getArriveTime());
        tvTicketPassengerStep3Num.setText(trains.getTrainNo());

        tvTicketPassengerStep3Sumtime.setText(trains.getStartTrainDate());
        data = new ArrayList<>();
//        resultData = Ticket3AddPassengerAdapter.getResult();
//        Map<String,Object> map = new HashMap<>();
//        map.put("name","冬不拉");
//        map.put("idCard","11010119910511947X");
//        map.put("tel","13812345678");
//        map.put("seatPrice",seatPrice);
//        data.add(map);

        adapter = new TicketPassengerStep3Adapter(this, data);
        listView.setAdapter(adapter);


        double price = adapter.getPrice();
        tvTicketPassengerStep3SumPrice.setText("订单总额：¥" + price + "元");


        addPassenger = findViewById(R.id.btnTicketPassengerStep3Add);
        addPassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
                intent1.setClass(TicketPassengerStep3Activity.this, AddPassengerActivity.class);
                startActivityForResult(intent1, 222);
            }
        });

        btnTicketPassengerStep3Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                toStep4Data = new ArrayList();
//                for (int i = 0; i < data.size(); i++) {
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("name", data.get(i).get("name"));
//                    Log.d("Order",data.toString());
//                    map.put("trainNo", trains.getTrainNo());
//                    map.put("date", trains.getStartTrainDate());
//                    map.put("seatNo", trains.getSeats().get("seatNo"));
//                    toStep4Data.add(map);
//                }

                new TicketPassengerStep3Task().execute();
//                Intent intent1 = new Intent();
//                intent1.setClass(TicketPassengerStep3Activity.this, TicketOrderSubmitStep4Activity.class);
//                intent1.putExtra("order",order);
//                startActivity(intent1);
            }
        });
    }

    public static List getToStep4Data() {
        return toStep4Data;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 222 && resultCode == 111) {
            this.data.addAll(AddPassengerActivity.getPassengerData());
            for (int i = 0; i < this.data.size(); i++) {
                this.data.get(i).put("seatPrice", seatPrice);
            }
            adapter = new TicketPassengerStep3Adapter(TicketPassengerStep3Activity.this, this.data);
            listView.setAdapter(adapter);
            double price = adapter.getPrice();
            tvTicketPassengerStep3SumPrice.setText("订单总额：¥" + price + "元");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    class TicketPassengerStep3Task extends AsyncTask<String, String, Object> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!NetCheckUtil.checkNet(TicketPassengerStep3Activity.this)) {
                Toast.makeText(TicketPassengerStep3Activity.this, "当前网络不可用", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        @Override
        protected Object doInBackground(String... strings) {
                String resultObject = "";
                try {
                    URL url = new URL(Constant.HOST+"/otn/Order");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setConnectTimeout(Constant.REQUEST_TIMEOUT);
                    httpURLConnection.setReadTimeout(Constant.SO_TIMEOUT);//读取超时 单位毫秒
                    //发送POST方法必须设置容下两行
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    //不使用缓存
                    httpURLConnection.setUseCaches(false);
                    SharedPreferences sp = getSharedPreferences("user",MODE_PRIVATE);
                    String value = sp.getString("Cookie","");
                    //设置请求属性
                    httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    httpURLConnection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
                    httpURLConnection.setRequestProperty("Charset", "UTF-8");
                    httpURLConnection.setRequestProperty("Cookie",value);

                    PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
                    //请求参数
                String params = "trainNo=" + trains.getTrainNo() +
                        "&startTrainDate=" + trains.getStartTrainDate() +
                        "&seatName=" + seatName[0];

                    for (int i = 0; i < data.size(); i++) {
                        String idCard = (String) data.get(i).get("idCard");
                        String idType = idCard.split(":")[0];
                        String id = idCard.split(":")[1];
                        params += "&id=" + id + "&idType=" + idType;
                    }

                Log.d("step3params",params+"");
                    printWriter.write(String.valueOf(params));
                    printWriter.flush();
                    printWriter.close();

                    int resultCode = httpURLConnection.getResponseCode();
                    Log.d("step3params","resultCode=="+resultCode);
                    if (resultCode == HttpURLConnection.HTTP_OK){
                        InputStream in = httpURLConnection.getInputStream();
                        StringBuffer sb = new StringBuffer();
                        String readLine = new String();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(in,"UTF-8"));
                        while ((readLine = reader.readLine())!= null){
                            sb.append(readLine).append("\n");
                        }
                        String result = sb.toString();
                        Log.d("step3params",result);
                        System.out.println("服务器返回结果（提交）"+result);
                        //解析Json
                        Gson gson = new GsonBuilder().create();
                        Order orders = gson.fromJson(result,Order.class);
                        in.close();
                        return orders;
                    }else {
                        resultObject = "2";
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    resultObject = "2";
                } catch (IOException e) {
                    e.printStackTrace();
                    resultObject = "2";
                }
                return resultObject;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (result instanceof Order) {
                order = (Order) result;
                Log.d("Order4",order.toString());
                Intent intent = new Intent();
                intent.setClass(TicketPassengerStep3Activity.this, TicketOrderSubmitStep4Activity.class);
                intent.putExtra("order",order);
                startActivity(intent);
            }
        }
    }
}
