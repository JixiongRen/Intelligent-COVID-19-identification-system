package com.example.myapplication2;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication2.dao.UserDao;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    EditText EditTextname;
    String token; //声明 token 字符串变量
    OkHttpClient mOkHttpClient=new OkHttpClient();
    token token1=new token(); // 创建一个名为token1的 token类变量

    private ProgressDialog main_login; // 创建一个名为main_login的进度条变量
    SharedPreferences sharedPreferences; // 创建一个名为sharedPreferences的本地存储变量
    public static String namestring;
    @Override
    protected void onCreate(Bundle savedInstanceState) { // 重写onCreate方法
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // 初始界面
        main_login=new ProgressDialog(LoginActivity.this); // 创建一个名为main_login的进度条
        main_login.setMessage("login...");
        main_login.setCancelable(true);
        // MODE_PRIVATE参数表示该对象只能被创建它的应用程序访问和修改，其他应用程序无法访问。
        // 因此，该代码行创建了一个名为sharedPreferences的私有SharedPreferences对象，该对象用于存储应用程序需要持久保存的数据。
        sharedPreferences=getSharedPreferences("root",MODE_PRIVATE);
        String token_local=sharedPreferences.getString("token","");
        if(!token_local.equals("")){
            // 定义Intent：如果本地存储的token不为空，则从MainActivity跳转到SecondActivity
            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
            // 开始执行
            startActivity(intent);
        }
    }

    // 定义唤起注册界面的方法
    public void reg(View view){

        startActivity(new Intent(getApplicationContext(),RegisterActivity.class));

    }


    // 登录方法，点击登录按钮时调用
    public void login(View view){
        main_login.show(); //调用 login方法时，显示进度条
        EditTextname = (EditText)findViewById(R.id.name);
        EditText EditTextpassword = (EditText)findViewById(R.id.password);

        new Thread(){
            @Override
            public void run() {

                UserDao userDao = new UserDao();//创建UserDao对象
                // 接收login方法的返回值，判断是否登录成功，该方法返回值为true或false
                boolean aa = userDao.login(EditTextname.getText().toString(),EditTextpassword.getText().toString());
                token=verification(EditTextname.getText().toString(),EditTextpassword.getText().toString()); // 调用verification方法，获取token
                System.out.println(token);
                main_login.dismiss(); // 登录成功后，隐藏进度条
                //接下来把token保存到本地
                sharedPreferences=getSharedPreferences("root",MODE_PRIVATE);//MODE_PRIVATE
                SharedPreferences.Editor editor = sharedPreferences.edit(); // 进入编辑模式
                editor.putString("token", token);
                editor.putString("name", EditTextname.getText().toString());
                editor.commit(); // 提交修改


                int msg = 0; // 初始化返回值
                if(aa){
                    msg = 1; // aa==1，登陆成功，msg==1；
                }

                hand1.sendEmptyMessage(msg);


            }
        }.start();


    }


    private String verification(String username, String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestBody requestBody=new FormBody.Builder() // 创建一个请求体
                        .add("username",username) // 添加用户名
                        .add("password",password) // 添加密码
                        .build(); // 创建请求体
                Request request=new Request.Builder()
                        .url("http://202.182.112.88:10011/getToken")
                        .post(requestBody)
                        .build();
                try {
                    Response response=mOkHttpClient.newCall(request).execute();
                    if(!response.isSuccessful()) throw new IOException("Unexpected code: "+response);
                    token=response.body().string();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    Thread.sleep(1000);//休眠1秒
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Gson gson = new Gson();
                token1 = gson.fromJson(token, token.class); // 将token字符串转换为token类
                token=token1.getToken(); // 获取token类中的token字符串
            }
        }).start();
        System.out.println("11User name is " + sharedPreferences.getString("name", ""));
        namestring = sharedPreferences.getString("name", "");
        return token;
    }


    /**
     * 用于接收子线程的消息,并处理,更新UI,子线程中不能直接更新UI,必须通过Handler
     */
    final Handler hand1 = new Handler()
    {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {

            if(msg.what == 1)
            {
                main_login.dismiss();
                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                Toast.makeText(getApplicationContext(),"登录成功 "+EditTextname.getText().toString(),Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
            else
            {
                main_login.dismiss();
                Toast.makeText(getApplicationContext(),"登录失败",Toast.LENGTH_LONG).show();
            }
        }
    };
}
