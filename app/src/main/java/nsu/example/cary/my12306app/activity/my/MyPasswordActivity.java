package nsu.example.cary.my12306app.activity.my;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import nsu.example.cary.my12306app.utils.Constant;


public class MyPasswordActivity extends AppCompatActivity {
    private EditText edtPass1,edtPass2;
    private Button btnPassUpdate;
    private ProgressDialog progressDialog;
    String passWord = new String();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_password);

        edtPass1 = findViewById(R.id.edt_mypassword1);
        edtPass2 = findViewById(R.id.edt_mypassword2);
        btnPassUpdate = findViewById(R.id.btnMyPassword);

        btnPassUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword1 = edtPass1.getText().toString();
                String newPassword2 = edtPass2.getText().toString();
                if (TextUtils.isEmpty(newPassword1)  || TextUtils.isEmpty(newPassword2)){
                    edtPass1.setError("密码不能为空，请填写！");
                }else if (newPassword1.equals(newPassword2)){
                    passWord = newPassword1;
                    new MyPasswordTask().execute("update");
                } else{
                    edtPass1.setError("两次密码输入不一致！");
                    edtPass1.setText("");
                    edtPass2.setText("");
                }
            }
        });
    }

    private class MyPasswordTask extends AsyncTask<String, Integer, Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //弹出正在加载的对话框
            progressDialog = ProgressDialog.show(MyPasswordActivity.this,
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
                String params = "oldPassword="+passWord+"&action="+strings[0];
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
                finish();
                Toast.makeText(MyPasswordActivity.this,"重置密码成功！",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(MyPasswordActivity.this,"重置密码失败！",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
