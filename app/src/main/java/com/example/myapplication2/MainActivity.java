package com.example.myapplication2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//hello world
public class MainActivity extends AppCompatActivity {
    public static StringBuilder currentPosition = new StringBuilder();
    public static String[] KeyLocationInfo = new String[3];
    private static final String TAG = "MainActivity";
    private String AudioSavePathInDevice = null;  //初始化，并准备录音保存路径
    private String AudioSavePath = null;
    private String pcmFile = null;//初始化，并准备大的保存路径
    private ListView listView;
    public String PATHOF;
    private MyViewPager mLoopPager;
    private LooperPagerAdapter mLooperPagerAdapter;
    private Handler mHandler;
    private boolean mIsTouch = false;
    private LinearLayout mPointContainer;

    private static List<Integer> sPics = new ArrayList<>();
    public LocationClient mLocationClient;
    private TextView positionText;


    static {
        sPics.add(R.mipmap.pic1);
        sPics.add(R.mipmap.pic2);
        sPics.add(R.mipmap.pic3);
    }


    //刷新
    //Environment.getExternalStorageDirectory().getAbsolutePath()能
    @Override
    protected void onStart() {
        super.onStart();
        File dir = new File(getExternalFilesDir(null), "sounds");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        listInitial(listaNagran());
        AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Music/" + "Audio" + CreateRandomAudioFileName() + ".wav";
        pcmFile = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Music/" + "Audio" + CreateRandomAudioFileName() + ".pcm";
    }//onStart()用于在活动从不可见变为可见时，加载可见资源
    //当我们需要在活动的可见生命周期中使用系统资源时，应该在OnStart中注册

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        initView();

        positionText = (TextView) findViewById(R.id.position_text_view);
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.
                    size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
                requestLocation();
        }

        initView();


        mHandler = new Handler();
        AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/" +
                "Audio" + "0" + ".wav";
        AudioSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/";

        try {
            if (listaNagran() != null) {
                listInitial(listaNagran());
            }
            System.out.println("正常内容已经执行0");
        } catch (NullPointerException e) {
            System.out.println("空指针错误");
        }

        AudioSavePathInDevice =
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/" +
                        "Audio" + CreateRandomAudioFileName() + ".wav";

        //按钮跳转
        Button mbtn1_1 = findViewById(R.id.bt1_1);
        mbtn1_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 跳转到信号切割
                Intent intent = new Intent(MainActivity.this, SplitActivity.class);//跳转到SplitActivity
                Bundle bundle = new Bundle();//
                bundle.putString("0", AudioSavePathInDevice);//传递MainActivity产生的路径到SplitActivity
                bundle.putString("1", AudioSavePath);
                bundle.putString("2", pcmFile);
                intent.putExtras(bundle);
                startActivity(intent);//开始跳转
            }
        });
        Button btn1 = findViewById(R.id.chaxun);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, epidemicActivity.class);
                startActivity(intent);
            }
        });
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        try {
            LocationClient.setAgreePrivacy(true);
            mLocationClient = new LocationClient(getApplicationContext());
        } catch (Exception e) {
            System.out.println("=============================================================");
            System.out.println(e.getMessage());
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        }
        try{
            LocationClient.setAgreePrivacy(true);
            mLocationClient.registerLocationListener(new MyLocationListener());
            LocationClientOption option = new LocationClientOption();
            option.setScanSpan(0);
            option.setIsNeedAddress(true);
            mLocationClient.setLocOption(option);
        } catch(Exception e) {
            Log.d(TAG, "initLocation: 实例化错误");
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }

    //录音列表
    @SuppressLint("Assert")
    ArrayList<String> listaNagran() {
        ArrayList<String> lista = new ArrayList<>();
        File f = new File(AudioSavePath);
        File[] files = f.listFiles();  //返回某个目录下所有文件和目录的绝对路径，返回的是File数组
//        assert files != null;
        try {
            for (File inFile : files) {
                assert true;
//              System.out.println(inFile.getName());
                lista.add(inFile.getName());  //返回音频列表
            }
            System.out.println("尝试catch 1");
        } catch (NullPointerException e) {
            System.out.println("baocuo");
        }
        return lista;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        //界面绑定到窗口时
        mHandler.post(mLooperTask);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
        mHandler.removeCallbacks(mLooperTask);
    }

    private Runnable mLooperTask = new Runnable() {
        @Override
        public void run() {
            if (!mIsTouch) {
                //切换ViewPager到下一个图
                int currentItem = mLoopPager.getCurrentItem();
                mLoopPager.setCurrentItem(++currentItem, true);
            }
            mHandler.postDelayed(this, 10000);
        }
    };

    private <LooperPagerAdapter> void initView() {
        //找到控件
        mLoopPager = (MyViewPager) this.findViewById(R.id.looper_pager);
        //设置适配器
        mLooperPagerAdapter = new com.example.myapplication2.LooperPagerAdapter();
        mLooperPagerAdapter.setData(sPics);
        mLoopPager.setAdapter(mLooperPagerAdapter);
        //mLoopPager.setOnPageChangeListener(this);
        // mLoopPager.setOnViewPagerTouchListener(this);
        mPointContainer = (LinearLayout) this.findViewById(R.id.ponits_container);
        //根据图片数量添加点的个数
        insertPoint();
        mLoopPager.setCurrentItem(mLooperPagerAdapter.getDataRealSize() * 100, false);
    }

    private void insertPoint() {
        for (int i = 0; i < sPics.size(); i++) {
            View point = new View(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(40, 40);
            layoutParams.leftMargin = 20;
            point.setBackground(getResources().getDrawable(R.drawable.shape_point_normal));
            point.setLayoutParams(layoutParams);
            mPointContainer.addView(point);

        }
    }

    //将列表初始化
    public void listInitial(ArrayList<String> files) {
        listView = findViewById(R.id.list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, files);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String itemValue = (String) listView.getItemAtPosition(position);
                AudioSavePathInDevice =
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/" + itemValue;
//                listaPlikow.setText(itemValue);
                // 跳转到信号结果
                Intent intent = new Intent(MainActivity.this, resultActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("0", AudioSavePathInDevice);
//                bundle.putString("1", String.valueOf(AudioTime));
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });
    }

    public void requestPermission(View view) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, 200);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (permissions[0].equals(Manifest.permission.RECORD_AUDIO) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[1].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    if (permissions[2].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                        if (permissions[3].equals(Manifest.permission.INTERNET) && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "已授权", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            }
        }
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            try {
                                Toast.makeText(this, "必须同意所有权限才能使用本程序",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            } catch (Exception e){
                                e.printStackTrace();
                            }

                            return;
                        }
                    }
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            runOnUiThread(() -> {
                currentPosition.append("纬度：").append(location.getLatitude()).
                        append("\n");
                currentPosition.append("经线：").append(location.getLongitude()).
                        append("\n");
                currentPosition.append("国家：").append(location.getCountry()).
                        append("\n");
                currentPosition.append("省（直辖市）：").append(location.getProvince()).
                        append("\n");
                KeyLocationInfo[0] = location.getProvince();
                currentPosition.append("地级市（直辖市区）：").append(location.getCity()).
                        append("\n");
                KeyLocationInfo[1] = location.getCity();
                currentPosition.append("区（县级市）：").append(location.getDistrict()).
                        append("\n");
                KeyLocationInfo[2] = location.getDistrict();
                currentPosition.append("街道：").append(location.getStreet()).
                        append("\n");
                currentPosition.append("定位方式：");
                if (location.getLocType() == BDLocation.TypeGpsLocation) {
                    currentPosition.append("GPS");
                } else if (location.getLocType() ==
                        BDLocation.TypeNetWorkLocation) {
                    currentPosition.append("网络");
                }
                System.out.println("位置是： " + currentPosition);
                positionText.setText(currentPosition);
                System.out.println("*********************");
                System.out.println(Arrays.toString(KeyLocationInfo));
                System.out.println("*********************");
            });
        }

        //     @Override
        public void onConnectHotSpotMessage(String s, int i) {
        }
    }

    //产生序列数
    public String CreateRandomAudioFileName() {
        ArrayList<String> nazwy = listaNagran();
        int numer = nazwy.size();
        String nazwa = "Audio" + numer + ".3gp";
        int i = 0;
        while (i < nazwy.size()) {
            if (nazwy.get(i).equals(nazwa)) {
                numer++;
                nazwa = "Audio" + numer + ".3gp";
                i = 0;
            }
            i += 1;
        }
        return Integer.toString(numer);
    }

//    //以强制指定只使用GPS进行定位修改位置结束
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mLocationClient.stop();
//    }


}




