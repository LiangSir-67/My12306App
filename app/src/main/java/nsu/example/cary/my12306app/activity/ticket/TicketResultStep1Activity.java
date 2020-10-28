package nsu.example.cary.my12306app.activity.ticket;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nsu.example.cary.my12306app.R;
import nsu.example.cary.my12306app.activity.my.MyAccountActivity;
import nsu.example.cary.my12306app.bean.Account;
import nsu.example.cary.my12306app.bean.Seat;
import nsu.example.cary.my12306app.bean.Train;
import nsu.example.cary.my12306app.utils.Constant;

public class TicketResultStep1Activity extends AppCompatActivity {

    private ListView lvTicketResultStep1;
    private TextView tvTicketResultStep1Before;
    private TextView tvTicketResultStep1After;
    private TextView tvTicketResultStep1DateTitle;
    private TextView tvTicketResultStep1StationTitle;
    private ProgressDialog progressDialog;
    private Train[] trains;
    private List<Map<String,Object>> data;
    private SimpleAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result_step1);

        lvTicketResultStep1 = findViewById(R.id.lvTicketResultStep1);
        tvTicketResultStep1Before = findViewById(R.id.tvTicketResultStep1Before);
        tvTicketResultStep1After = findViewById(R.id.tvTicketResultStep1After);
        tvTicketResultStep1DateTitle = findViewById(R.id.tvTicketResultStep1DateTitle);
        tvTicketResultStep1StationTitle = findViewById(R.id.tvTicketResultStep1StationTitle);

        tvTicketResultStep1StationTitle.setText(getIntent().getStringExtra("stationFrom")+"-"+getIntent().getStringExtra("stationTo"));
        tvTicketResultStep1DateTitle.setText(getIntent().getStringExtra("startTrainDate"));

        tvTicketResultStep1Before.setOnClickListener(new HandleTicketResultStep1());
        tvTicketResultStep1After.setOnClickListener(new HandleTicketResultStep1());

        data = new ArrayList<>();

//        Map<String,Object> row1 = new HashMap<>();
//        row1.put("trainNo","G108");
//        row1.put("flag1",R.drawable.flg_shi);
//        row1.put("flag2",R.drawable.flg_zhong);
//        row1.put("timeFrom","07:00");
//        row1.put("timeTo","13:00(0天)");
//        row1.put("seat1","高级软卧:100");
//        row1.put("seat2","硬座:80");
//        row1.put("seat3","一等座:60");
//        row1.put("seat4","二等座:20");
//        data.add(row1);


        adapter = new SimpleAdapter(this,
                data,
                R.layout.item_ticket_result_step1,
                new String[]{"trainNo","flag1","flag2","timeFrom","timeTo","seat1","seat2","seat3","seat4"},
                new int[]{R.id.tvTicketResultStep1TrainNo,
                R.id.imgTicketResultStep1Flg1,
                R.id.imgTicketResultStep1Flg2,
                R.id.tvTicketResultStep1TimeFrom,
                R.id.tvTicketResultStep1TimeTo,
                R.id.tvTicketResultStep1Seat1,
                R.id.tvTicketResultStep1Seat2,
                R.id.tvTicketResultStep1Seat3,
                R.id.tvTicketResultStep1Seat4});

        lvTicketResultStep1.setAdapter(adapter);

        lvTicketResultStep1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                String fromStationName = trains[position].getFromStationName();
                String toStationName = trains[position].getToStationName();
                String trainNo = trains[position].getTrainNo();
                String startTrainDate = tvTicketResultStep1DateTitle.getText().toString().split(" ")[0];
//                intent.putExtra("train",trains[position]);
                intent.putExtra("fromStationName",fromStationName);
                intent.putExtra("toStationName",toStationName);
                intent.putExtra("trainNo",trainNo);
                intent.putExtra("startTrainDate",startTrainDate);
                intent.putExtra("startDate",tvTicketResultStep1DateTitle.getText().toString());
                intent.setClass(TicketResultStep1Activity.this,TicketResultStep2Activity.class);
                startActivity(intent);
            }
        });

        //异步任务
        new TicketStep1Task().execute();
    }


    private class HandleTicketResultStep1 implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Calendar calendar = Calendar.getInstance();
            //获取选中日期
            String oldDateFrom = tvTicketResultStep1DateTitle.getText().toString();
            int oldYear = Integer.parseInt(oldDateFrom.split("-")[0]);
            int oldMonth = Integer.parseInt(oldDateFrom.split("-")[1])-1;
            int oldDayOfMonth = Integer.parseInt(oldDateFrom.split("-")[2].split(" ")[0]);
            calendar.set(oldYear,oldMonth,oldDayOfMonth);
            switch (v.getId()){
                case R.id.tvTicketResultStep1Before:
                    //前一天
                    calendar.add(Calendar.DAY_OF_MONTH,-1);
                    //联网更新车次信息
                    new TicketStep1Task().execute();
                    break;

                case R.id.tvTicketResultStep1After:
                    //后一天
                    calendar.add(Calendar.DAY_OF_MONTH,1);
                    //联网更新车次信息
                    new TicketStep1Task().execute();
                    break;
            }
            String weekDay = DateUtils.formatDateTime(TicketResultStep1Activity.this,calendar.getTimeInMillis(),DateUtils.FORMAT_SHOW_WEEKDAY);
            tvTicketResultStep1DateTitle.setText(calendar.get(Calendar.YEAR)+"-"
                        +(calendar.get(Calendar.MONTH)+1)+"-"
                        +calendar.get(Calendar.DAY_OF_MONTH)+" "
                        +weekDay);
        }
    }

    private class TicketStep1Task extends AsyncTask<String,String,Object> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //弹出正在加载的对话框
            progressDialog = ProgressDialog.show(TicketResultStep1Activity.this,
                    null,
                    "正在加载...",
                    false,
                    true);
        }

        @Override
        protected Object doInBackground(String... strings) {
            String result = "";
            try {
                URL url = new URL(Constant.HOST + "/otn/TrainList");
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
                String params = "fromStationName=" +getIntent().getStringExtra("stationFrom")+
                        "&toStationName=" +getIntent().getStringExtra("stationTo")+
                        "&startTrainDate=" +tvTicketResultStep1DateTitle.getText().toString().split(" ")[0];
                System.out.println("发往服务器的内容：" + params);
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
                    System.out.println("服务器返回的结果（查询车票）：" + result2);
                    //解析json数据
                    Gson gson = new GsonBuilder().create();
                    Train[] trains = gson.fromJson(result2,Train[].class);

                    inputStream.close();
                    return trains;
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
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            //每次清空原有的数据
            data.clear();

            //如果result是Account的一个对象
            if (result instanceof Train[]) {
                trains = (Train[]) result;
                if (trains.length == 0){
                    //没有对应的车次
                    Toast.makeText(TicketResultStep1Activity.this,"没有对应的车次",Toast.LENGTH_SHORT).show();
                }else {
                    for (Train train : trains){
                        Map<String,Object> row1 = new HashMap<>();
                        row1.put("trainNo",train.getTrainNo());
                        if (train.getStartStationName().equals(train.getFromStationName())){
                            row1.put("flag1",R.drawable.flg_shi);
                        }else{
                            row1.put("flag1",R.drawable.flg_guo);
                        }

                        if (train.getEndStationName().equals(train.getToStationName())){
                            row1.put("flag2",R.drawable.flg_zhong);
                        }else{
                            row1.put("flag2",R.drawable.flg_guo);
                        }
//                        row1.put("flag1",R.drawable.flg_shi);
//                        row1.put("flag2",R.drawable.flg_zhong);
                        row1.put("timeFrom",train.getStartTime());
//                        row1.put("timeTo","13:00(0天)");
                        row1.put("timeTo",train.getArriveTime()+"("+train.getDayDifference()+")");
//                        row1.put("seat1","高级软卧:100");
//                        row1.put("seat2","硬座:80");
//                        row1.put("seat3","一等座:60");
//                        row1.put("seat4","二等座:20");
                        String[] seatKey = {"seat1","seat2","seat3","seat4"};
                        //取出车次
                        Map<String, Seat> seats = train.getSeats();
                        int i = 0;
                        for (String key : seats.keySet()){
                            Seat seat = seats.get(key);
                            row1.put(seatKey[i++],seat.getSeatName()+":"+seat.getSeatNum());
                        }
                        data.add(row1);
                    }
                }
                adapter.notifyDataSetChanged();

            } else if (result instanceof String) {

            }
        }
    }
}
