//package com.example.login_demo;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import com.example.login_demo.dao.UserDao;
//import com.example.login_demo.entity.User;
//
//import java.lang.ref.WeakReference;
//
//public class RegisterActivity extends AppCompatActivity {
//    private MyHandler mHandler = new MyHandler(this);
//    EditText name = null;
//    EditText username = null;
//    EditText password = null;
//    EditText phone = null;
//    EditText age = null;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.register);
//
//        name = findViewById(R.id.name);
//        username = findViewById(R.id.username);
//        password = findViewById(R.id.password);
//        phone = findViewById(R.id.phone);
//        age = findViewById(R.id.age);
//    }
//
//
//    public void register(View view){
//
//
//
//        String cname = name.getText().toString();
//        String cusername = username.getText().toString();
//        String cpassword = password.getText().toString();
//
//        System.out.println(phone.getText().toString());
//
//        String cphone = phone.getText().toString();
//        int cgae = Integer.parseInt(age.getText().toString());
//
//        if(cname.length() < 2 || cusername.length() < 2 || cpassword.length() < 2 ){
//            Toast.makeText(getApplicationContext(),"输入信息不符合要求请重新输入",Toast.LENGTH_LONG).show();
//            return;
//
//        }
//
//
//        User user = new User();
//
//        user.setName(cname);
//        user.setUsername(cusername);
//        user.setPassword(cpassword);
//        user.setAge(cgae);
//        user.setPhone(cphone);
//
//        new Thread(){
//            @Override
//            public void run() {
//
//                int msg = 0;
//
//                UserDao userDao = new UserDao();
//
//                User uu = userDao.findUser(user.getName());
//
//                if(uu != null){
//                    msg = 1;
//                }
//
//                boolean flag = userDao.register(user);
//                if(flag){
//                    msg = 2;
//                }
//
//                mHandler.sendEmptyMessage(msg);
//
//            }
//        }.start();
//
//
//    }
////    final Handler hand = new Handler()
////    {
////        @Override
////        public void handleMessage(Message msg) {
////            if(msg.what == 0)
////            {
////                Toast.makeText(getApplicationContext(),"注册失败",Toast.LENGTH_LONG).show();
////
////            }
////            if(msg.what == 1)
////            {
////                Toast.makeText(getApplicationContext(),"该账号已经存在，请换一个账号",Toast.LENGTH_LONG).show();
////
////            }
////            if(msg.what == 2)
////            {
////                //startActivity(new Intent(getApplication(),MainActivity.class));
////
////                Intent intent = new Intent();
////                //将想要传递的数据用putExtra封装在intent中
////                intent.putExtra("a","註冊");
////                setResult(RESULT_CANCELED,intent);
////                finish();
////            }
////
////        }
////    };
//private static class MyHandler extends Handler {
//    private final WeakReference<RegisterActivity> mActivity;
//
//    MyHandler(RegisterActivity registerActivity) {
//        mActivity = new WeakReference<>(registerActivity);
//    }
//
//    @Override
//    public void handleMessage(Message msg) {
//        RegisterActivity registerActivity = mActivity.get();
//        if (registerActivity == null) {
//            return;
//        }
//
//        if (msg.what == 0) {
//            Toast.makeText(registerActivity, "注册失败", Toast.LENGTH_LONG).show();
//        } else if (msg.what == 1) {
//            Toast.makeText(registerActivity, "该账号已经存在，请换一个账号", Toast.LENGTH_LONG).show();
//        } else if (msg.what == 2) {
//            Intent intent = new Intent();
//            intent.putExtra("a", "注册");
//            registerActivity.setResult(RESULT_CANCELED, intent);
//            registerActivity.finish();
//        }
//    }
//}
//
//}
//


package com.example.myapplication2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication2.dao.UserDao;
import com.example.myapplication2.entity.User;

public class RegisterActivity extends AppCompatActivity {
    EditText name = null;
    EditText username = null;
    EditText password = null;
    EditText phone = null;
    EditText age = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        phone = findViewById(R.id.phone);
        age = findViewById(R.id.age);
    }


    public void register(View view){


        // 获取用户信息
        String cname = name.getText().toString();
        String cusername = username.getText().toString();
        String cpassword = password.getText().toString();

        System.out.println(phone.getText().toString());

        String cphone = phone.getText().toString();
        int cgae = Integer.parseInt(age.getText().toString());


        // 定义用户名、姓名、密码规则，可根据我们的需求修改
        if(cname.length() < 2 || cusername.length() < 2 || cpassword.length() < 2 ){
            Toast.makeText(getApplicationContext(),"输入信息不符合要求请重新输入",Toast.LENGTH_LONG).show();
            return;

        }

        // 创建一个user
        User user = new User();
        // 将用户输入的信息赋值给user
        user.setName(cname);
        user.setUsername(cusername);
        user.setPassword(cpassword);
        user.setAge(cgae);
        user.setPhone(cphone);

        // 判断用户是否存在
        new Thread(){
            @Override
            public void run() {

                int msg = 0;

                UserDao userDao = new UserDao();

                User uu = userDao.findUser(user.getName());

                if(uu != null){
                    msg = 1;
                }

                boolean flag = userDao.register(user);
                if(flag){
                    msg = 2;
                }
                hand.sendEmptyMessage(msg);

            }
        }.start();


    }

    final Handler hand = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0)
            {
                Toast.makeText(getApplicationContext(),"注册失败",Toast.LENGTH_LONG).show();

            }
            if(msg.what == 1)
            {
                Toast.makeText(getApplicationContext(),"该账号已经存在，请换一个账号",Toast.LENGTH_LONG).show();

            }
            if(msg.what == 2)
            {
                //startActivity(new Intent(getApplication(),MainActivity.class));

                Intent intent = new Intent();
                //将想要传递的数据用putExtra封装在intent中
                intent.putExtra("a","注册");
                setResult(RESULT_CANCELED,intent);
                finish();
            }

        }
    };
}
