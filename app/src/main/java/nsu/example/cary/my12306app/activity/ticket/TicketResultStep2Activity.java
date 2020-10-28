package nsu.example.cary.my12306app.activity.ticket;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nsu.example.cary.my12306app.R;
import nsu.example.cary.my12306app.bean.Order;
import nsu.example.cary.my12306app.bean.Seat;
import nsu.example.cary.my12306app.bean.Train;
import nsu.example.cary.my12306app.utils.Constant;
import nsu.example.cary.my12306app.utils.NetCheckUtil;

public class TicketResultStep2Activity extends AppCompatActivity {

    private ListView lvTicketDetailsStep2;
    private List<Map<String, Object>> data;
    private TicketDetailsStep2Adapter adapter;
    private TextView tvTicketDetailsStep2SeatName,tvTicketDetailsStep2SeatNum;
    private TextView tvTicketDetailsStep2DateTitle,tvTicketDetailsStep2StationTitle;
    private TextView tvTicketDetailsStep2Before,tvTicketDetailsStep2After;
    String fromStationName;
    String toStationName;
    String trainNo;
    String startTrainDate;
    String startDate;
    private Train trains;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result_step2);

        lvTicketDetailsStep2 = findViewById(R.id.lvTicketDetailsStep2);
        tvTicketDetailsStep2SeatName = findViewById(R.id.tvTicketDetailsStep2SeatName);
        tvTicketDetailsStep2SeatNum = findViewById(R.id.tvTicketDetailsStep2SeatNum);
        tvTicketDetailsStep2DateTitle = findViewById(R.id.tvTicketDetailsStep2DateTitle);
        tvTicketDetailsStep2StationTitle = findViewById(R.id.tvTicketDetailsStep2StationTitle);
        tvTicketDetailsStep2Before = findViewById(R.id.tvTicketDetailsStep2Before);
        tvTicketDetailsStep2After = findViewById(R.id.tvTicketDetailsStep2After);

        //为前一天和后一天添加监听
        tvTicketDetailsStep2Before.setOnClickListener(new HandleTicketResultStep2());
        tvTicketDetailsStep2After.setOnClickListener(new HandleTicketResultStep2());

        //接收Step1传递的train数据
        final Intent intent = getIntent();
        fromStationName = intent.getStringExtra("fromStationName");
        toStationName = intent.getStringExtra("toStationName");
        trainNo = intent.getStringExtra("trainNo");
        startTrainDate = intent.getStringExtra("startTrainDate");
        startDate = intent.getStringExtra("startDate");
        data = new ArrayList<>();
        adapter = new TicketDetailsStep2Adapter(TicketResultStep2Activity.this, data);
        lvTicketDetailsStep2.setAdapter(adapter);
        new TicketResultStep2Task().execute();





    }

    class TicketResultStep2Task extends AsyncTask<String,String,Object>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!NetCheckUtil.checkNet(TicketResultStep2Activity.this)){
                Toast.makeText(TicketResultStep2Activity.this,"当前网络不可用",Toast.LENGTH_SHORT).show();
                return;
            }
        }

        @Override
        protected Object doInBackground(String... strings) {
            String result = "";
            try {
                URL url = new URL(Constant.HOST + "/otn/Train");
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
                String params = "fromStationName=" +fromStationName+
                        "&toStationName=" +toStationName+
                        "&startTrainDate=" +startTrainDate+
                        "&trainNo="+trainNo;
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
                    System.out.println("服务器返回的结果（查询车票train）：" + result2);
                    //解析json数据
                    Gson gson = new GsonBuilder().create();
                    trains = gson.fromJson(result2,Train.class);
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

            //每次清空原有的数据
//            data.clear();

            //如果result是Account的一个对象
            if (result instanceof Train) {
                Train train = (Train) result;
//                Toast.makeText(TicketResultStep2Activity.this,train.toString(),Toast.LENGTH_SHORT).show();
                Log.d("testData:",train.toString());
                Map<String, Seat> seats = train.getSeats();
                Log.d("testData:",seats.toString());
                tvTicketDetailsStep2SeatName.setText(train.getTrainNo());
                tvTicketDetailsStep2SeatNum.setText(train.getStartTime()+"-"+train.getArriveTime()+","+"历时"+train.getDurationTime());
                tvTicketDetailsStep2DateTitle.setText(startDate);
                tvTicketDetailsStep2StationTitle.setText(fromStationName+"-"+toStationName);

                Map<String,Object> map;
                for (String key : seats.keySet()){
                    map = new HashMap<>();
                    Log.d("testData:",seats.get(key).getSeatName());
                    map.put("seatName",seats.get(key).getSeatName());
                    map.put("seatNum",seats.get(key).getSeatNum()+"张");
                    map.put("seatPrice",seats.get(key).getSeatPrice());
                    data.add(map);
                }
                Log.d("data",data.get(2).toString());

                adapter.notifyDataSetChanged();
            } else if(result instanceof String) {

            }
        }
    }

    class TicketDetailsStep2Adapter extends BaseAdapter{
        private Context context;
        private List<Map<String,Object>> data;
        private LayoutInflater inflater;

        public TicketDetailsStep2Adapter(Context context, List<Map<String, Object>> data) {
            this.context = context;
            this.data = data;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null){
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_ticket_details_step2,null);
                holder.tvTicketDetailsStep2SeatName = convertView.findViewById(R.id.tvTicketDetailsStep2SeatName);
                holder.tvTicketDetailsStep2SeatNum = convertView.findViewById(R.id.tvTicketDetailsStep2SeatNum);
                holder.tvTicketDetailsStep2SeatPrice = convertView.findViewById(R.id.tvTicketDetailsStep2SeatPrice);
                holder.btnTicketDetailsStep2Order = convertView.findViewById(R.id.btnTicketDetailsStep2Order);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvTicketDetailsStep2SeatName.setText((String) data.get(position).get("seatName"));
            holder.tvTicketDetailsStep2SeatNum.setText((String) data.get(position).get("seatNum"));
            holder.tvTicketDetailsStep2SeatPrice.setText((String)data.get(position).get("seatPrice"));

            Log.d("Step2Data:",trains.toString());

            final String seatName = holder.tvTicketDetailsStep2SeatName.getText().toString();
            final String seatNum = holder.tvTicketDetailsStep2SeatNum.getText().toString();
            final String seatPrice = holder.tvTicketDetailsStep2SeatPrice.getText().toString();

            holder.btnTicketDetailsStep2Order.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("seatName",seatName);
                    intent.putExtra("seatNum",seatNum);
                    intent.putExtra("seatPrice",seatPrice);
                    intent.putExtra("trains",trains);
                    intent.setClass(context, TicketPassengerStep3Activity.class);
                    context.startActivity(intent);
                }
            });

            return convertView;
        }


        class ViewHolder{
            TextView tvTicketDetailsStep2SeatName;
            TextView tvTicketDetailsStep2SeatNum;
            TextView tvTicketDetailsStep2SeatPrice;
            Button btnTicketDetailsStep2Order;
        }
    }


    private class HandleTicketResultStep2 implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Calendar calendar = Calendar.getInstance();
            //获取选中日期
            String oldDateFrom = tvTicketDetailsStep2DateTitle.getText().toString();
            Log.d("Step2DateFrom",oldDateFrom);
            int oldYear = Integer.parseInt(oldDateFrom.split("-")[0]);
            int oldMonth = Integer.parseInt(oldDateFrom.split("-")[1])-1;
            int oldDayOfMonth = Integer.parseInt(oldDateFrom.split("-")[2].split(" ")[0]);
            Log.d("Step2DateFrom",oldYear+","+oldMonth+","+oldDayOfMonth);
            calendar.set(oldYear,oldMonth,oldDayOfMonth);
            switch (v.getId()){
                case R.id.tvTicketDetailsStep2Before:
                    //前一天
                    calendar.add(Calendar.DAY_OF_MONTH,-1);
                    //联网更新车次信息
                    new TicketResultStep2Task2().execute();
                    break;

                case R.id.tvTicketDetailsStep2After:
                    //后一天
                    calendar.add(Calendar.DAY_OF_MONTH,1);
                    //联网更新车次信息
                    new TicketResultStep2Task2().execute();
                    break;
            }
            String weekDay = DateUtils.formatDateTime(TicketResultStep2Activity.this,calendar.getTimeInMillis(),DateUtils.FORMAT_SHOW_WEEKDAY);
            tvTicketDetailsStep2DateTitle.setText(calendar.get(Calendar.YEAR)+"-"
                    +(calendar.get(Calendar.MONTH)+1)+"-"
                    +calendar.get(Calendar.DAY_OF_MONTH)+" "
                    +weekDay);
            Log.d("Step2DateFromLast",
                    calendar.get(Calendar.YEAR)+"-"
                            +(calendar.get(Calendar.MONTH)+1)+"-"
                            +calendar.get(Calendar.DAY_OF_MONTH)+" "
                            +weekDay);
        }
    }

    class TicketResultStep2Task2 extends AsyncTask<String,String,Object>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!NetCheckUtil.checkNet(TicketResultStep2Activity.this)){
                Toast.makeText(TicketResultStep2Activity.this,"当前网络不可用",Toast.LENGTH_SHORT).show();
                return;
            }
        }

        @Override
        protected Object doInBackground(String... strings) {
            String result = "";
            try {
                URL url = new URL(Constant.HOST + "/otn/Train");
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
                String date = tvTicketDetailsStep2DateTitle.getText().toString();
                Log.d("Step2DateFrom2",date.split(" ")[0]);
                String params = "fromStationName=" +fromStationName+
                        "&toStationName=" +toStationName+
                        "&startTrainDate=" +date.split(" ")[0]+
                        "&trainNo="+trainNo;
                /*
                * fromStationName=北京&toStationName=成都&startTrainDate=2020-9-4&trainNo=T297
                * fromStationName=北京&toStationName=成都&startTrainDate=2020-9-4 星期五&trainNo=T297
                * */
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
                    System.out.println("服务器返回的结果（查询车票train）：" + result2);
                    //解析json数据
                    Gson gson = new GsonBuilder().create();
                    trains = gson.fromJson(result2,Train.class);
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

            //每次清空原有的数据
            data.clear();

            //如果result是Train的一个对象
            if (result instanceof Train) {
                Train train = (Train) result;
//                Toast.makeText(TicketResultStep2Activity.this,train.toString(),Toast.LENGTH_SHORT).show();
                    Log.d("testData:",train.toString());
                    Map<String, Seat> seats = train.getSeats();
                    Log.d("testData:",seats.toString());
                    tvTicketDetailsStep2SeatName.setText(train.getTrainNo());
                    tvTicketDetailsStep2SeatNum.setText(train.getStartTime()+"-"+train.getArriveTime()+","+"历时"+train.getDurationTime());
                    tvTicketDetailsStep2DateTitle.setText(startDate);
                    tvTicketDetailsStep2StationTitle.setText(fromStationName+"-"+toStationName);

                    Map<String,Object> map;
                    for (String key : seats.keySet()){
                        map = new HashMap<>();
                        Log.d("testData:",seats.get(key).getSeatName());
                        map.put("seatName",seats.get(key).getSeatName());
                        map.put("seatNum",seats.get(key).getSeatNum()+"张");
                        map.put("seatPrice",seats.get(key).getSeatPrice());
                        data.add(map);
                    }
//                Log.d("data",data.get(2).toString());

                    adapter.notifyDataSetChanged();
            }else {
                if (result == null){
                    Toast.makeText(TicketResultStep2Activity.this,"没有对应的车次！",Toast.LENGTH_SHORT).show();
                }else{

                }
            }
        }
    }
}
