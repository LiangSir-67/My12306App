package nsu.example.cary.my12306app.activity.my;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

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
import nsu.example.cary.my12306app.bean.Passenger;
import nsu.example.cary.my12306app.utils.Constant;
import nsu.example.cary.my12306app.utils.NetCheckUtil;

public class MyContactActivity extends AppCompatActivity {

    private ListView lvMyContact;
    private List<Map<String, Object>> data;
    private SimpleAdapter adapter;
    private ProgressDialog progressDialog;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            //每次都清空原数据
            data.clear();
            switch (msg.what) {
                case 1:
                    Passenger[] passengers = (Passenger[]) msg.obj;
                    for (Passenger passenger : passengers) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("name", passenger.getName() + "(" + passenger.getType() + ")");
                        map.put("idCard", passenger.getIdType() + ":" + passenger.getId());
                        map.put("tel", "电话:" + passenger.getTel());
                        data.add(map);
                    }
                    adapter.notifyDataSetChanged();
                    break;
                case 2:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_contact);

        //返回按钮
        setActionBar();

        lvMyContact = findViewById(R.id.lvMyContactList);
        data = new ArrayList<>();
//        Map<String,Object> map1 = new HashMap<String,Object>();
//        map1.put("name","张三(成人)");
//        map1.put("idCard","身份证:632124198004056547");
//        map1.put("tel","电话:13198604231");
//        data.add(map1);

//        Map<String,Object> map2 = new HashMap<String,Object>();
//        map2.put("name","李四(学生)");
//        map2.put("idCard","神仙证:452013188004063211");
//        map2.put("tel","电话:15500567894");
//        data.add(map2);
//
//        Map<String,Object> map3 = new HashMap<String,Object>();
//        map3.put("name","王五(军人)");
//        map3.put("idCard","学生证:632421199006085789");
//        map3.put("tel","电话:18797468975");
//        data.add(map3);

        adapter = new SimpleAdapter(this,
                data,
                R.layout.item_my_contact_list_layout,
                new String[]{"name", "idCard", "tel", "image"},
                new int[]{R.id.tvContactName, R.id.tvContactIdCard, R.id.tvContactTel});

        lvMyContact.setAdapter(adapter);

        lvMyContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setClass(MyContactActivity.this, MyContactEditActivity.class);
                intent.putExtra("row", (Serializable) data.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!NetCheckUtil.checkNet(MyContactActivity.this)) {
            Toast.makeText(MyContactActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            return; //后续代码不执行
        }
        //弹出正在加载的对话框
        progressDialog = ProgressDialog.show(MyContactActivity.this,
                null,
                "正在加载...",
                false,
                true);
        new Thread(){
            @Override
            public void run() {
                super.run();

                Message msg = handler.obtainMessage();
                try {
                    URL url = new URL(Constant.HOST+"/otn/PassengerList");
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
                        String result = sb.toString();
                        System.out.println("服务器返回的结果："+result);
                        //解析json数据
                        Gson gson = new GsonBuilder().create();

                        Passenger[] passengers = gson.fromJson(result,Passenger[].class);

                        inputStream.close();
                        msg.what = 1;
                        msg.obj = passengers;
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
                handler.sendMessage(msg);
            }
        }.start();
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
                case R.id.menu_mycontact_adduser:
                    Toast.makeText(MyContactActivity.this,"添加用户",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MyContactActivity.this,AddContactActivity.class);
                    startActivity(intent);
                    break;
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
