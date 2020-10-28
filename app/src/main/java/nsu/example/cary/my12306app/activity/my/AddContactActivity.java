package nsu.example.cary.my12306app.activity.my;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nsu.example.cary.my12306app.R;
import nsu.example.cary.my12306app.bean.Account;
import nsu.example.cary.my12306app.bean.Passenger;
import nsu.example.cary.my12306app.utils.Constant;
import nsu.example.cary.my12306app.utils.DialogUtils;

public class AddContactActivity extends AppCompatActivity {
    private ListView listView;
    private Button btnAdd;
    private List<Map<String,Object>> data;
    private SimpleAdapter adapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        //绑定控件
        listView = findViewById(R.id.lvAddContact);
        btnAdd = findViewById(R.id.btnAddContact);

        data = new ArrayList<>();

        Map<String,Object> map1 = new HashMap<>();
        map1.put("key1","姓名");
        map1.put("key2","");
        map1.put("key3",R.drawable.forward_25);
        data.add(map1);

        Map<String,Object> map2 = new HashMap<>();
        map2.put("key1","乘客类型");
        map2.put("key2","");
        map2.put("key3",R.drawable.forward_25);
        data.add(map2);

        Map<String,Object> map3 = new HashMap<>();
        map3.put("key1","证件类型");
        map3.put("key2","");
        map3.put("key3",R.drawable.forward_25);
        data.add(map3);

        Map<String,Object> map4 = new HashMap<>();
        map4.put("key1","证件号码");
        map4.put("key2","");
        map4.put("key3",R.drawable.forward_25);
        data.add(map4);

        Map<String,Object> map5 = new HashMap<>();
        map5.put("key1","电话");
        map5.put("key2","");
        map5.put("key3",R.drawable.forward_25);
        data.add(map5);


        adapter = new SimpleAdapter(this,
                data,
                R.layout.item_my_contact_edit_layout,
                new String[]{"key1","key2","key3"},
                new int[]{R.id.tv_MyContact_edit_key,R.id.tv_MyContact_edit_value,R.id.img_MyContact_edit_flag});
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                switch (position){
                    case 0:
                        final EditText edtName = new EditText(AddContactActivity.this);
                        edtName.setText((String) data.get(position).get("key2"));
                        new AlertDialog.Builder(AddContactActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setTitle("输入姓名")
                                .setView(edtName)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String newName = edtName.getText().toString();
                                        if (TextUtils.isEmpty(newName)){
                                            DialogUtils.setClosable(dialog,false);
                                            edtName.setError("请输入姓名");
                                            edtName.requestFocus();
                                        }else {
                                            dialog.dismiss();
                                            DialogUtils.setClosable(dialog,true);
                                            data.get(position).put("key2",newName);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        DialogUtils.setClosable(dialog,true);
                                    }
                                })
                                .create().show();
                        break;

                    case 1:
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddContactActivity.this);
                        final EditText edtType = new EditText(AddContactActivity.this);
                        edtType.setText((String) data.get(position).get("key2"));
                        final String[] str ={"成人","学生","儿童","军人","其他"};
                        builder.setTitle("请选择乘客类型");
                        builder.setIcon(android.R.drawable.ic_dialog_info);
                        //设置单选形式
                        builder.setSingleChoiceItems(str,0,null);
                        builder.setSingleChoiceItems(str, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String res = str[which];
                                Toast.makeText(AddContactActivity.this,res,Toast.LENGTH_SHORT).show();
                                data.get(position).put("key2",res);
                                dialog.dismiss();
                                adapter.notifyDataSetChanged();
                            }
                        });
                        //设置确定按钮
//                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                DialogUtils.setClosable(dialog,true);
//                            }
//                        });
//                        //设置取消按钮
//                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                DialogUtils.setClosable(dialog,true);
//                            }
//                        });
                        builder.create().show();
                        break;

                    case 2:
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(AddContactActivity.this);
                        final String[] str1 ={"身份证"};
                        builder1.setTitle("请选择乘客类型");
                        builder1.setIcon(android.R.drawable.ic_dialog_info);
                        //设置单选形式
                        builder1.setSingleChoiceItems(str1,0,null);
                        builder1.setSingleChoiceItems(str1, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String res = str1[which];
                                data.get(position).put("key2",res);
                                dialog.dismiss();
                                adapter.notifyDataSetChanged();
                            }
                        });
                        //设置确定按钮
//                        builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                DialogUtils.setClosable(dialog,true);
//                            }
//                        });
//                        //设置取消按钮
//                        builder1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                DialogUtils.setClosable(dialog,true);
//                            }
//                        });
                        builder1.create().show();
                        break;

                    case 3:
                        final EditText edtIdNum = new EditText(AddContactActivity.this);
                        edtIdNum.setText((String) data.get(position).get("key2"));
                        new AlertDialog.Builder(AddContactActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setTitle("输入身份证号码")
                                .setView(edtIdNum)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String newName = edtIdNum.getText().toString();
                                        if (TextUtils.isEmpty(newName)){
                                            DialogUtils.setClosable(dialog,false);
                                            edtIdNum.setError("请输入姓名");
                                            edtIdNum.requestFocus();
                                        }else {
                                            dialog.dismiss();
                                            DialogUtils.setClosable(dialog,true);
                                            data.get(position).put("key2",newName);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        DialogUtils.setClosable(dialog,true);
                                    }
                                })
                                .create().show();
                        break;

                    case 4:
                        final EditText edtTel = new EditText(AddContactActivity.this);
                        edtTel.setText((String) data.get(position).get("key2"));
                        new AlertDialog.Builder(AddContactActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setTitle("输入电话号码")
                                .setView(edtTel)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String newName = edtTel.getText().toString();
                                        if (TextUtils.isEmpty(newName)){
                                            DialogUtils.setClosable(dialog,false);
                                            edtTel.setError("请输入电话号码");
                                            edtTel.requestFocus();
                                        }else {
                                            dialog.dismiss();
                                            DialogUtils.setClosable(dialog,true);
                                            data.get(position).put("key2",newName);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        DialogUtils.setClosable(dialog,true);
                                    }
                                })
                                .create().show();
                        break;
                }
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //使用异步任务实现
                new AddContactTask().execute("new");
            }
        });
    }

    class AddContactTask extends AsyncTask<String, Integer, Object>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //弹出正在加载的对话框
            progressDialog = ProgressDialog.show(AddContactActivity.this,
                    null,
                    "正在加载...",
                    false,
                    true);
        }

        @Override
        protected Object doInBackground(String... strings) {
            String result = "";
            try {
                URL url = new URL(Constant.HOST+"/otn/Passenger");
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
                String params = "姓名=" +data.get(0).get("key2")+
                        "&证件类型=" +data.get(2).get("key2")+
                        "&证件号码=" +data.get(3).get("key2")+
                        "&乘客类型=" +data.get(1).get("key2")+
                        "&电话=" +data.get(4).get("key2")+
                        "&action=" + strings[0];
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
                    String result1 = sb.toString().substring(1,2);
                    System.out.println("服务器返回的结果："+result1);


                    inputStream.close();
                    return result1;
                }else {
                    return "添加用户失败！";
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

            //判断doInBackground()方法return的result是否为字符串
            if ("1".equals(result)) {
                finish();
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(AddContactActivity.this,"错误，请重试！",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
