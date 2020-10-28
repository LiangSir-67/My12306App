package nsu.example.cary.my12306app.activity.ticket;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nsu.example.cary.my12306app.R;
import nsu.example.cary.my12306app.adapter.Ticket3AddPassengerAdapter;
import nsu.example.cary.my12306app.bean.Passenger;
import nsu.example.cary.my12306app.utils.Constant;

public class AddPassengerActivity extends AppCompatActivity {
    private ListView listView;
    private List<Map<String, Object>> data;
    private static List<Map<String, Object>> passengerData;
    private ProgressDialog progressDialog;
    private Ticket3AddPassengerAdapter adapter;
    private CheckBox cbPassenger;
    private Button btnAddPassenger;
    private TextView tvPassengerName, tvPassengerIdCard, tvPassengerTel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_passenger);

        listView = findViewById(R.id.lvAddPassengerList);
        cbPassenger = findViewById(R.id.cbTicket3Add);
        btnAddPassenger = findViewById(R.id.btnAddPassenger);
        tvPassengerName = findViewById(R.id.tvPassengerName);
        tvPassengerIdCard = findViewById(R.id.tvPassengerIdCard);
        tvPassengerTel = findViewById(R.id.tvPassengerTel);
        setActionBar();

        data  = new ArrayList<>();
        new AddPassengerTask().execute();
        adapter = new Ticket3AddPassengerAdapter(this,data);
        listView.setAdapter(adapter);

        btnAddPassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passengerData == null) passengerData = new ArrayList<>();
                passengerData = adapter.getResult();
               Log.d("addPassenger",adapter.getResult()+"");
               List<Map<String, Object>> passenger = adapter.getResult();
                Log.d("addPassenger",passenger.toString());



               Intent intent = new Intent();
               intent.putExtra("addPassenger", (Serializable) passenger);
               setResult(111,intent);
               finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (passengerData == null) passengerData = new ArrayList<>();
        passengerData.clear();

    }

    public static List<Map<String, Object>> getPassengerData() {
        return passengerData;
    }

    class AddPassengerTask extends AsyncTask<String, Integer, Object>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //弹出正在加载的对话框
            progressDialog = ProgressDialog.show(AddPassengerActivity.this,
                    null,
                    "正在加载...",
                    false,
                    true);
        }

        @Override
        protected Object doInBackground(String... strings) {
            String result = "";
            try {
                URL url = new URL(Constant.HOST+"/otn/TicketPassengerList");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(Constant.REQUEST_TIMEOUT);
                httpURLConnection.setReadTimeout(Constant.SO_TIMEOUT);
                //发送Post请求
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                //设置不使用缓存
                httpURLConnection.setUseCaches(false);

                SharedPreferences sp = getSharedPreferences("user",MODE_PRIVATE);
                String value = sp.getString("Cookie","");

                //设置请求属性
                httpURLConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                httpURLConnection.setRequestProperty("Connection","Keep-Alive");
                httpURLConnection.setRequestProperty("Charset","UTF-8");
                httpURLConnection.setRequestProperty("Cookie",value);

                //获取输出流
                PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
                //请求参数
                String params = "";
                System.out.println("发往服务器的内容："+params);
                printWriter.write(params);
                printWriter.flush();
                printWriter.close();

                //获取响应状态
                int resultCode = httpURLConnection.getResponseCode();
                if (resultCode == HttpURLConnection.HTTP_OK){
                    //打印结果
                    InputStream inputStream = httpURLConnection.getInputStream();
                    StringBuffer sb = new StringBuffer();
                    String readLine = new String();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream,"UTF-8"));
                    while ((readLine = reader.readLine()) != null){
                        sb.append(readLine).append("\n");
                    }
                    String result1 = sb.toString();
                    System.out.println("服务器返回的结果(TicketPassengerList)："+result1);
                    //解析json数据
                    Gson gson = new GsonBuilder().create();

                    Passenger[] passengers = gson.fromJson(result1,Passenger[].class);

                    inputStream.close();
                    return passengers;
                }else {

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
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            data.clear();

            Passenger[] passengers = (Passenger[]) result;
            //如果result是Account的一个对象
            if (result instanceof Passenger[]) {
                for (Passenger passenger : passengers){
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", passenger.getName());
                    map.put("idCard", "身份证:"+passenger.getId());
                    map.put("tel", "电话:"+passenger.getTel());
                    data.add(map);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    //设置返回按钮
    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        //显示返回按钮
        actionBar.setDisplayHomeAsUpEnabled(true);
        //使用自己的返回图标
        actionBar.setHomeAsUpIndicator(R.drawable.back);
    }

    //左上角返回按钮监听
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //加载添加用户菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_adduser, menu);
        return true;
    }
}
