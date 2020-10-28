package nsu.example.cary.my12306app.activity.order;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
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
import nsu.example.cary.my12306app.bean.Order;
import nsu.example.cary.my12306app.utils.Constant;
import nsu.example.cary.my12306app.utils.NetCheckUtil;

public class TicketOrderNoPayActivity extends AppCompatActivity {
    private TextView tvOrderNoPayId;
    private ListView lvOrderNoPayList;
    private TextView tvOrderNoPayCancel;
    private TextView tvOrderNoPaySubmit;
    private Order order;
    private List<Map<String,Object>> data;
    private SimpleAdapter simpleAdapter;
    private ProgressDialog progressDialog;

    private Handler handlerCancel = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (progressDialog != null){
                progressDialog.dismiss();
            }
            switch (msg.what){
                case 1:
                    String result = msg.obj.toString();
                    if ("1".equals(result)){
                        Toast.makeText(TicketOrderNoPayActivity.this,"取消订单成功！",Toast.LENGTH_SHORT).show();
                        TicketOrderNoPayActivity.this.finish();
                        simpleAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };
    private Handler handlerPay = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (progressDialog != null){
                progressDialog.dismiss();
            }
            switch (msg.what){
                case 1:
                    String result = msg.obj.toString();
                    if ("1".equals(result)){
                        Toast.makeText(TicketOrderNoPayActivity.this,"确认支付成功！",Toast.LENGTH_SHORT).show();
                        TicketOrderNoPayActivity.this.finish();
                        simpleAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_order_no_pay);

        tvOrderNoPayId = findViewById(R.id.tvOrderNoPayId);
        lvOrderNoPayList = findViewById(R.id.lvOrderNoPayList);
        tvOrderNoPayCancel = findViewById(R.id.tvOrderNoPayCancel);
        tvOrderNoPaySubmit = findViewById(R.id.tvOrderNoPaySubmit);

        order = (Order) getIntent().getSerializableExtra("order");
        tvOrderNoPayId.setText(order.getId());

        data = new ArrayList<Map<String,Object>>();
        for (int i=0 ; i<order.getPassengerList().size() ; i++){
            Map<String,Object> map = new HashMap<>();
            map.put("ticket4Name",order.getPassengerList().get(i).getName());
            map.put("ticket4No",order.getTrain().getTrainNo());
            map.put("ticket4Date",order.getTrain().getStartTrainDate());
            map.put("ticket4SeatName",order.getPassengerList().get(i).getSeat().getSeatNo());
            data.add(map);
        }
        simpleAdapter = new SimpleAdapter(TicketOrderNoPayActivity.this,
                data,
                R.layout.item_order_no_pay_cance1,
                new String[]{"ticket4Name","ticket4No","ticket4Date","ticket4SeatName"},
                new int[]{R.id.NoPayName,R.id.NoPayTrainNo,R.id.NoPayDate,R.id.NoPaySeatNO});
        lvOrderNoPayList.setAdapter(simpleAdapter);

        //点击取消订单
        tvOrderNoPayCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NetCheckUtil.checkNet(TicketOrderNoPayActivity.this)){
                    Toast.makeText(TicketOrderNoPayActivity.this,"当前网络不可用",Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog = ProgressDialog.show(TicketOrderNoPayActivity.this,
                        null,
                        "正在加载中....",
                        false,true);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        Message msg = handlerCancel.obtainMessage();
                        try {
                            URL url = new URL(Constant.HOST+"/otn/Cancel");
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
                            String params = "orderId="+ order.getId();
                            System.out.println("发往服务器的内容(未支付):"+params);
                            printWriter.write(params);
                            printWriter.flush();
                            printWriter.close();
                            int resultCode = httpURLConnection.getResponseCode();
                            if (resultCode == HttpURLConnection.HTTP_OK){
                                InputStream in = httpURLConnection.getInputStream();
                                StringBuffer sb = new StringBuffer();
                                String readLine = new String();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
                                while ((readLine = reader.readLine()) != null){
                                    sb.append(readLine).append("\n");
                                }
                                String result = sb.toString();
                                System.out.println("服务器返回的结果(未支付):"+result);

                                //解析JSON
                                Gson gson = new GsonBuilder().create();
                                String orders= gson.fromJson(result,String.class);
                                msg.what = 1;
                                msg.obj = orders;
                            }else {
                                msg.what = 2;
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            msg.what = 2;
                        } catch (IOException e) {
                            e.printStackTrace();
                            msg.what = 2;
                        }
                        handlerCancel.sendMessage(msg);
                    }
                }.start();
            }
        });

        //确认支付
        tvOrderNoPaySubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NetCheckUtil.checkNet(TicketOrderNoPayActivity.this)){
                    Toast.makeText(TicketOrderNoPayActivity.this,"当前网络不可用",Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog = ProgressDialog.show(TicketOrderNoPayActivity.this,
                        null,
                        "正在加载中....",
                        false,true);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        Message msg = handlerPay.obtainMessage();
                        try {
                            URL url = new URL(Constant.HOST+"/otn/Pay");
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
                            String params = "orderId="+ order.getId();
                            System.out.println("发往服务器的内容(Pay)：" + params);
                            printWriter.write(params);
                            printWriter.flush();
                            printWriter.close();
                            int resultCode = httpURLConnection.getResponseCode();
                            if (resultCode == HttpURLConnection.HTTP_OK){
                                InputStream in = httpURLConnection.getInputStream();
                                StringBuffer sb = new StringBuffer();
                                String readLine = new String();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
                                while ((readLine = reader.readLine()) != null){
                                    sb.append(readLine).append("\n");
                                }
                                String result = sb.toString();
                                System.out.println("服务器返回的结果(Pay)：" + result);


                                //解析JSON
                                Gson gson = new GsonBuilder().create();
                                String orders1= gson.fromJson(result,String.class);
                                msg.what = 1;
                                msg.obj = orders1;
                            }else {
                                msg.what = 2;
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            msg.what = 2;
                        } catch (IOException e) {
                            e.printStackTrace();
                            msg.what = 2;
                        }

                        handlerPay.sendMessage(msg);
                    }
                }.start();
            }
        });
    }
}
