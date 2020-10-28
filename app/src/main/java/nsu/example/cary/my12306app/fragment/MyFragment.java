package nsu.example.cary.my12306app.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import nsu.example.cary.my12306app.R;
import nsu.example.cary.my12306app.activity.SplashActivity;
import nsu.example.cary.my12306app.activity.my.MyAccountActivity;
import nsu.example.cary.my12306app.activity.my.MyContactActivity;
import nsu.example.cary.my12306app.activity.my.MyPasswordActivity;
import nsu.example.cary.my12306app.utils.Constant;
import nsu.example.cary.my12306app.utils.DialogUtils;

import static android.content.Context.MODE_PRIVATE;


public class MyFragment extends Fragment {

    Button btnLogout = null;
    ListView lvMyList = null;
    private ProgressDialog progressDialog;
    String userPass = new String();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnLogout = getActivity().findViewById(R.id.btnLogout);
        lvMyList = getActivity().findViewById(R.id.lvMyList);
        // 每行布局(系统自带)
        // android.R.layout.simple_list_item_1

        // 数据
        String[] data = {"我的联系人", "我的账户", "我的密码"};

        // 适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, data);

        // 绑定
        lvMyList.setAdapter(adapter);

        lvMyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                switch (position) {
                    case 0:
                        intent.setClass(getActivity(), MyContactActivity.class);
                        startActivity(intent);
                        break;

                    case 1:
                        intent.setClass(getActivity(), MyAccountActivity.class);
                        startActivity(intent);
                        break;

                    case 2:
//                        intent.setClass(getActivity(), MyPasswordActivity.class);
//                        startActivity(intent);
                        final EditText edtPass = new EditText(getActivity());
                        //设置为密码框
                        edtPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        new AlertDialog.Builder(getActivity())
                                .setIcon(android.R.drawable.ic_dialog_info)

                                .setTitle("请输入原密码")
                                .setView(edtPass)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        userPass = edtPass.getText().toString();
                                        if (TextUtils.isEmpty(userPass)){
                                            DialogUtils.setClosable(dialog,false);
                                            edtPass.setError("原密码错误，请重新输入！");
                                            edtPass.requestFocus();
                                        }else {
                                            new MyPasswordTask().execute("query");
                                            DialogUtils.setClosable(dialog,true);
                                        }
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        DialogUtils.setClosable(dialog,true);
                                    }
                                })
                                .create().show();
                        break;
                }
            }
        });

        //退出登录
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoginOutTask().execute();
            }
        });

    }


    private class LoginOutTask extends AsyncTask<String, Integer, Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //弹出正在加载的对话框
            progressDialog = ProgressDialog.show(getContext(),
                    null,
                    "正在加载...",
                    false,
                    true);
        }

        @Override
        protected Object doInBackground(String... strings) {
            String result = "";
            try {
                URL url = new URL(Constant.HOST+"/otn/Logout");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(Constant.REQUEST_TIMEOUT);
                httpURLConnection.setReadTimeout(Constant.SO_TIMEOUT);
                //发送Post请求
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                //设置不使用缓存
                httpURLConnection.setUseCaches(false);

                SharedPreferences sp = getActivity().getSharedPreferences("user",MODE_PRIVATE);
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
                    String result1 = sb.toString().substring(1,2);
                    System.out.println("服务器返回的结果："+result1);

                    inputStream.close();
                    return result1;
                }else {
                    return "失败！";
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

            if ("1".equals(result)){
                Intent intent = new Intent(getActivity(), SplashActivity.class);
                startActivity(intent);
            }else {
                Toast.makeText(getActivity(),"退出登录失败!",Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class MyPasswordTask extends AsyncTask<String, Integer, Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //弹出正在加载的对话框
            progressDialog = ProgressDialog.show(getContext(),
                    null,
                    "正在加载...",
                    false,
                    true);
        }

        @Override
        protected Object doInBackground(String... strings) {
            String result = "";
            try {
                URL url = new URL(Constant.HOST+"/otn/AccountPassword");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(Constant.REQUEST_TIMEOUT);
                httpURLConnection.setReadTimeout(Constant.SO_TIMEOUT);
                //发送Post请求
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                //设置不使用缓存
                httpURLConnection.setUseCaches(false);

                SharedPreferences sp = getActivity().getSharedPreferences("user",MODE_PRIVATE);
                String value = sp.getString("Cookie","");

                //设置请求属性
                httpURLConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                httpURLConnection.setRequestProperty("Connection","Keep-Alive");
                httpURLConnection.setRequestProperty("Charset","UTF-8");
                httpURLConnection.setRequestProperty("Cookie",value);

                //获取输出流
                PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
                //请求参数
                String params = "oldPassword="+userPass+"&action="+strings[0];
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
                    return "失败！";
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

            if ("1".equals(result)){
                Intent intent = new Intent(getActivity(), MyPasswordActivity.class);
                startActivity(intent);
            }else {
                Toast.makeText(getActivity(),"密码错误，请重试",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
