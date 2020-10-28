package nsu.example.cary.my12306app.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
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
import nsu.example.cary.my12306app.activity.order.TicketOrderNoPayActivity;
import nsu.example.cary.my12306app.activity.order.TicketOrderPayActivity;
import nsu.example.cary.my12306app.activity.ticket.TicketOrderSubmitStep4Activity;
import nsu.example.cary.my12306app.adapter.OrderAdapter;
import nsu.example.cary.my12306app.bean.Order;
import nsu.example.cary.my12306app.utils.Constant;
import nsu.example.cary.my12306app.utils.NetCheckUtil;

import static android.content.Context.MODE_PRIVATE;


public class OrderFragment extends Fragment {
    private TextView tvOrderNoPay,tvOrderAll;
    private ListView lvOrder;
    private List<Map<String,Object>> data;
    private OrderAdapter orderAdapter;
    private Order[] orders;
    private int status = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tvOrderNoPay = getActivity().findViewById(R.id.tvOrderNoPay);
        tvOrderAll = getActivity().findViewById(R.id.tvOrderAll);
        lvOrder = getActivity().findViewById(R.id.lvOrder);

        tvOrderNoPay.setOnClickListener(new OrderHandler());
        tvOrderAll.setOnClickListener(new OrderHandler());

        data = new ArrayList<>();
        orderAdapter = new OrderAdapter(getActivity(),data);
        lvOrder.setAdapter(orderAdapter);
        lvOrder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                if (data.get(position).get("orderStatus").toString().equals("未支付")){
                    intent.setClass(getActivity(), TicketOrderNoPayActivity.class);
                    intent.putExtra("order",orders[position]);
                    startActivity(intent);
                }else if (data.get(position).get("orderStatus").toString().equals("已支付")){
                    intent.setClass(getActivity(), TicketOrderPayActivity.class);
                    intent.putExtra("order",orders[position]);
                    startActivity(intent);
                }
            }
        });

    }


    class OrderTask extends AsyncTask<String,String,Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!NetCheckUtil.checkNet(getActivity())){
                Toast.makeText(getActivity(),"当前网络不可用",Toast.LENGTH_SHORT).show();
                return;
            }
        }

        @Override
        protected Object doInBackground(String... strings) {
            String resultObject = "";
            try {
                URL url = new URL(Constant.HOST+"/otn/OrderList");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setConnectTimeout(Constant.REQUEST_TIMEOUT);
                httpURLConnection.setReadTimeout(Constant.SO_TIMEOUT);//读取超时 单位毫秒
                //发送POST方法必须设置容下两行
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                //不使用缓存
                httpURLConnection.setUseCaches(false);
                SharedPreferences sp = getContext().getSharedPreferences("user",MODE_PRIVATE);
                String value = sp.getString("Cookie","");
                //设置请求属性
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpURLConnection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
                httpURLConnection.setRequestProperty("Charset", "UTF-8");
                httpURLConnection.setRequestProperty("Cookie",value);

                PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
                //发送请求参数
                //String params = "Cookie:"+value;
                String params = "status="+ status ;
                System.out.println("发往服务器的内容(OrderList)：" + params);
                printWriter.write(params);
                printWriter.flush();
                printWriter.close();

                int resultCode = httpURLConnection.getResponseCode();
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
                    System.out.println("服务器返回的结果(OrderList)：" + result);
                    //解析Json
                    Gson gson = new GsonBuilder().create();
                    Order[] orders = gson.fromJson(result,Order[].class);
                    Log.d("orders", result);
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
            if (result instanceof Order[]){
                orders = (Order[]) result;
                for (Order order:orders){
                    Map<String,Object> map = new HashMap<>();
                    map.put("orderId","订单编号:"+order.getId());
                    switch (order.getStatus()){
                        case 0:
                            map.put("orderStatus","未支付");
                            break;
                        case 1:
                            map.put("orderStatus","已支付");
                            break;
                        case 2:
                            map.put("orderStatus","已取消");
                            break;
                    }
                    map.put("orderTrainNo",order.getTrain().getTrainNo());
                    map.put("orderDateFrom",order.getTrain().getStartTrainDate());
                    map.put("orderStationFrom",order.getTrain().getFromStationName()+"-"+order.getTrain().getToStationName()+
                            " "+ order.getPassengerList().size() + "人");
                    map.put("orderPrice","￥"+order.getOrderPrice());
                    map.put("orderFlag",R.drawable.forward_25);
                    data.add(map);
                }
                orderAdapter.notifyDataSetChanged();
            }else if (result instanceof String){

            }
        }
    }

    private class OrderHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            data.clear();
            switch (view.getId()){
                case R.id.tvOrderNoPay:
                    tvOrderNoPay.setBackgroundResource(R.drawable.order_background);
                    tvOrderAll.setBackgroundResource(0);
                    status = 0;
                    new OrderTask().execute();
                    orderAdapter.notifyDataSetChanged();
                    break;
                case R.id.tvOrderAll:
                    tvOrderAll.setBackgroundResource(R.drawable.order_background);
                    tvOrderNoPay.setBackgroundResource(0);
                    status = 1;
                    new OrderTask().execute();
                    orderAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        data.clear();
        new OrderTask().execute();
        orderAdapter.notifyDataSetChanged();
    }
}
