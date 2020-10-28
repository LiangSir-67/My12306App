package nsu.example.cary.my12306app.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import nsu.example.cary.my12306app.R;
import nsu.example.cary.my12306app.utils.Constant;
import nsu.example.cary.my12306app.utils.Md5Utils;
import nsu.example.cary.my12306app.utils.NetCheckUtil;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private TextView tvLostPassword;
    private EditText edtUsername;
    private EditText edtPassword;
    private CheckBox ckLogin;
    private ProgressDialog progressDialog;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (progressDialog != null){
                progressDialog.dismiss();
            }
            switch (msg.what){
                case 1:
                    String jsessionid = (String) msg.obj;
                    int result = msg.arg1;
                    if (0 == result){
                        //用户名和密码错误
                        edtUsername.selectAll();
                        edtUsername.setError("用户名或密码错误");
                        edtUsername.requestFocus();
                    }else if (1 == result){
                        SharedPreferences pref = getSharedPreferences("user",
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit(); // 编辑器
                        editor.putString("Cookie",jsessionid);
                        // 记录用户名或密码
                        if (ckLogin.isChecked()) {
                            editor.putString("username", edtUsername.getText()
                                    .toString());
                            editor.putString("password", edtPassword.getText()
                                    .toString());
                            editor.commit(); // 一定要提交

                        } else {
                            // 清空以前的登录信息
                            editor.remove("username");
                            editor.remove("password");
                            editor.commit();
                        }
                        // 显式意图
                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        // 关闭LoginActivity
                    }
                    break;

                case 2:
                    // 显式意图
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvLostPassword = findViewById(R.id.tvLostPassword);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        ckLogin = findViewById(R.id.cb_autologin);

        // 忘记密码链接?
        tvLostPassword
                .setText(Html.fromHtml("<a href=\"http://www.12306.cn/lostPassword\">忘记密码？</a>"));
        tvLostPassword.setMovementMethod(LinkMovementMethod.getInstance());

        // 获取组件对象
        btnLogin = findViewById(R.id.ckLogin);
        // 绑定监听器
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.d("My12306", "Login Button Click");

                if (TextUtils.isEmpty(edtUsername.getText().toString())) {
                    edtUsername.setError("请输入用户名");
                    edtUsername.requestFocus();
                } else if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    edtPassword.setError("请输入密码");
                    edtPassword.requestFocus();
                } else {
                    if (!NetCheckUtil.checkNet(LoginActivity.this)){
                        Toast.makeText(LoginActivity.this,"网络异常！",Toast.LENGTH_SHORT).show();
                        return;//后续代码不执行
                    }
                    //弹出对话框
                    progressDialog = ProgressDialog.show(LoginActivity.this,
                            null,
                            "正在加载中...",
                            false,true);

                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            Message msg = handler.obtainMessage();
                            try {
                                URL url = new URL(Constant.HOST+"/Login");
                                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                                httpURLConnection.setConnectTimeout(Constant.REQUEST_TIMEOUT);
                                httpURLConnection.setReadTimeout(Constant.SO_TIMEOUT);
                                //发送Post请求
                                httpURLConnection.setDoOutput(true);
                                httpURLConnection.setDoInput(true);
                                //设置不使用缓存
                                httpURLConnection.setUseCaches(false);
                                //设置请求属性
                                httpURLConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                                httpURLConnection.setRequestProperty("Connection","Keep-Alive");
                                httpURLConnection.setRequestProperty("Charset","UTF-8");
                                //获取输出流
                                PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
                                //请求参数
                                String params = "username="+edtUsername.getText().toString()+"&password="+ Md5Utils.MD5(edtPassword.getText().toString());
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
                                    System.out.println("服务器返回的结果："+sb.toString());

                                    //xml解析
                                    XmlPullParser parser = Xml.newPullParser();
                                    parser.setInput(new StringReader(sb.toString()));
                                    int type = parser.getEventType();
                                    String result = null;
                                    while (type != XmlPullParser.END_DOCUMENT){
                                        switch (type){
                                            case XmlPullParser.START_TAG:
                                                if ("result".equals(parser.getName())){
                                                    result = parser.nextText();
                                                    Log.d("result:",result);
                                                }
                                                break;
                                        }
                                        type = parser.next();
                                    }
                                    //记录 SESSIONID
                                    String value = "";
                                    String responseCookie = httpURLConnection.getHeaderField("Set-Cookie");
                                    Log.d("My12306",responseCookie);
                                    if (responseCookie != null){
                                        String sessionString = responseCookie.substring(0,responseCookie.indexOf(";"));
                                        value = sessionString.split("=")[1];
                                    }
                                    Log.d("My12306","JSESSIONID:"+value);
                                    inputStream.close();
                                    msg.what = 1;
                                    msg.arg1 = Integer.parseInt(result);
                                    msg.obj = "JSESSIONID="+value;
                                }else {
                                    msg.what = 2;
                                }
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                                msg.what = 2;
                            } catch (IOException e) {
                                e.printStackTrace();
                                msg.what = 2;
                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                                msg.what = 2;
                            }
                            handler.sendMessage(msg);
                        }
                    }.start();
                }

            }
        });

    }
}
