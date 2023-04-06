package com.example.myapplication2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class resultActivity extends AppCompatActivity {
    ArrayList<Float> Negative = new ArrayList<Float>();
    ArrayList<Float> Positive = new ArrayList<Float>();
    private static String resultonline;
    private static String resultlocal;
    private static float rate_local;
    private static float rate_online;
    private static float neg_rate_online = 0;
    private static float pos_rate_online;
    private static float neg_rate_local;
    private static float pos_rate_local;
    private static String str_neg_rate_online;
    private static String str_pos_rate_online;
    private static float mixed_neg_rate;
    private static float mixed_pos_rate;
    private int stateOfMessage;
    //列表跳过来
    private float[][] AudioFile;
    private String mstring;
    private String AudioString;
    private String string2;
    private File dirFile;
    private RandomAccessFile wavFile; //file to open
    private long fileLength; //length of whole file in bytes
    private int dataLength; //number of bytes in data section
    private short numChannels; //number of channels in the file
    private int sampleRate; //sampling frequency Fs used to encode the file
    private int bitsPerSample; //number of bits used to hold each sample 用于保存每个样本的位数
    private int numSamples; //number of samples in the file 文件中的样本数
    private double[] firstChannelArray; //array of samples from first (left) channel
    private double[] secondChannelArray; //array of samples from second (right) channel, if it exists
    private boolean isMono; // true if there is only one channel, i.e. signal is mono, not stereo
    private int duration; //duration of WAV file in seconds

    private String userID = LoginActivity.namestring;

    private File WavFile;
    private SpectrogramView mytv3_1;
    private TextView mytv3_2;
    private static TextView mytv3_3;
    private static TextView test_view;
    private TextView NetResult;
    private ImageView result_img;
    private Interpreter tflite = null;
    private int[] ddims = {1, 3, 224, 224};//-------------------
    private List<String> resultLabel = new ArrayList<>();
    private String PATHOF;
    private Button Query;
    public static double PI1 = 3.1415926536;//
    public static int FRM_LEN = 64000; //窗长/帧长
    public int FrmNum;//帧数
    public int dwSoundlen;//数据长度
    public static int FRM_SFT = 8000;// 窗移
    public double[] fpData;
    public double[] fltHamm;//
    public AudFrame[] audFrame;//
    //    public ArrayList<Integer> WavStart = new ArrayList<>();//音频开始
//    public ArrayList<Integer> WavEnd = new ArrayList<>();//音频结束
    public double maxSte;//最大能量
    public double[] maxData;//
    public int maxDataNum;
    private final OkHttpClient client = new OkHttpClient();
    String result0 = "上传：失败";
    private String timeStamp;
    String result1 = "上传：成功";

    public void showTextRes() {
        String show_text;
        mixed_neg_rate = (float) (neg_rate_local * 0.5 + neg_rate_online * 0.5);
        mixed_pos_rate = (float) (pos_rate_local * 0.5 + pos_rate_online * 0.5);
        if (mixed_pos_rate >= mixed_neg_rate) {
//            show_text = "融合后的识别结果为：Positive" + "\n识别准确率： " + mixed_pos_rate;
            show_text = "融合后的识别结果  阳性";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    result_img.setImageResource(R.drawable.bad_result);
                }
            });
        } else{
            //            show_text = "融合后的识别结果为：Negative" + "\n识别准确率： " + mixed_neg_rate;

            show_text = "融合后的识别结果  阴性";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    result_img.setImageResource(R.drawable.good_result);
                }
            });
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                test_view.setText(show_text);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        File dir = new File(getExternalFilesDir(null), "sounds");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        mytv3_3 = (TextView) findViewById(R.id.tv3_3);
        result_img=(ImageView)findViewById(R.id.result_img);
        PATHOF = dir.getPath();
        mytv3_2 = findViewById(R.id.tv3_2);
        //NetResult = findViewById(R.id.netResult);
        Bundle bundle = getIntent().getExtras();
        mstring = bundle.getString("3");
        timeStamp = bundle.getString("10");
        test_view = (TextView) findViewById(R.id.test_view);

        //读声音文件------------------------------------------------------------------------------------------------------------
        try {
            wavFile = new RandomAccessFile(new File("/storage/emulated/0/Android/data/com.example.myapplication2/files/sounds/" + timeStamp + "test.wav"), "r");
            fileLength = wavFile.length();
            wavFile.seek(22);  //文件偏移22个位
            numChannels = wavFile.readShort();    //读取16位字节
            numChannels = Short.reverseBytes(numChannels); //must reverse bits since little-endian
            //必须从little endian开始反转位
            sampleRate = wavFile.readInt(); //at offset 24 so no seeking necessary偏移量为24，无需寻找
            sampleRate = Integer.reverseBytes(sampleRate); //little-endian
            wavFile.seek(34);    //文件偏移34位
            bitsPerSample = wavFile.readByte();
            if (bitsPerSample > 32) {
                Log.e("WAVExplorer", "Sample size of " + bitsPerSample + " bits not supported. Please use a file with a sample size of 32 bits or lower.");
                throw new IOException();
            }
            wavFile.seek(40);
            dataLength = wavFile.readInt();
            dataLength = Integer.reverseBytes(dataLength); //little-endian
            numSamples = (8 * dataLength) / (numChannels * bitsPerSample);
            firstChannelArray = new double[numSamples];
            if (numChannels == 1) { //mono, only one channel of samples
                isMono = true;
                for (int i = 0; i < numSamples; i++) {
                    //wavFile already at offset 44, no need to seek
                    if (bitsPerSample == 8) firstChannelArray[i] = wavFile.readByte();
                    if (bitsPerSample == 16)
                        firstChannelArray[i] = Short.reverseBytes(wavFile.readShort());
                    if (bitsPerSample == 32)
                        firstChannelArray[i] = Integer.reverseBytes(wavFile.readInt());
                }
            } else {
                if (numChannels == 2) { //stereo, two channels of samples
                    isMono = false;
                    secondChannelArray = new double[numSamples];
                    for (int i = 0; i < numSamples; i++) {
                        //wavFile already at offset 44, no need to seek
                        if (bitsPerSample == 8) {
                            firstChannelArray[i] = wavFile.readByte();
                            secondChannelArray[i] = wavFile.readByte();
                        }
                        if (bitsPerSample == 16) {
                            firstChannelArray[i] = Short.reverseBytes(wavFile.readShort());
                            secondChannelArray[i] = Short.reverseBytes(wavFile.readShort());
                        }
                        if (bitsPerSample == 32) {
                            firstChannelArray[i] = Integer.reverseBytes(wavFile.readInt());
                            secondChannelArray[i] = Integer.reverseBytes(wavFile.readInt());
                        }
                    }
                } else throw new IOException();
            }
            duration = numSamples * sampleRate;
            wavFile.close();
        } catch (FileNotFoundException e) {
            Log.e("WAVExplorer", "Couldn't find file ");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//----------------------------------------------------------------------------------------------------------------------
        //跳转
        //      Button mbtn3_1 = findViewById(R.id.bt3_1);
        Button mbtn3_2 = findViewById(R.id.bt3_2);
        Button mbtn3_3 = findViewById(R.id.bt3_3);
        Query = (Button) findViewById(R.id.Query_Button);
        mbtn3_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 跳转到主界面
                Intent intent = new Intent(resultActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        System.out.println("oncreat,开创完了");
//         访问网络不能在主程序中进行，需要开新的线程
//上传文件到服务器------------------------------------------------------------------------------------
        mbtn3_3.setOnClickListener(new View.OnClickListener() {
//            public JSONObject readJSONtext(String JSONtext) {
//                String jsonString = "";
//                jsonString = JSONtext;
//                return JSONObject.parseObject(jsonString);
//            }

            @Override
            public void onClick(View view) {
                ProgressDialog progressDialog = new ProgressDialog(resultActivity.this);
                progressDialog.setMessage("Sending...");
                progressDialog.setCancelable(true);
                progressDialog.show();
                new Thread() {
                    @Override
                    public void run() {
                        //目标服务器IP 202.182.112.88
                        File f = new File("/storage/emulated/0/Android/data/com.example.myapplication2/files/sounds/" + timeStamp + "test.wav");
                        //创建RequestBody
                        //RequestBody fileBody=RequestBody.create(MediaType.parse("image/jpeg"),ImageFile);
                        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), f);
                        //构建MultipartBody
                        MultipartBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("file", f.getName(), fileBody)
                                .build();
                        //构建请求
                        Request request = new Request.Builder()
                                .url("http://202.182.112.88:10002/upload_files/" + userID)
                                .post(requestBody)
                                .build();
                        try (Response response = client.newCall(request).execute()) {
                            if (!response.isSuccessful())
                                throw new IOException("Unexpected code: " + response);

                            if (response.isSuccessful()) {
                                Log.d("State", "Succeed");
                                stateOfMessage = 1;
                                progressDialog.dismiss();
                            } else {
                                Log.d("State", "Fail");
                                stateOfMessage = 0;
                                progressDialog.dismiss();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //成功或失败后的UI操作
                                    if (stateOfMessage == 1) {
                                        Toast.makeText(resultActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(resultActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                //查询返回结果
                Query.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //查询函数
                        OkHttpClient okHttpClient = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url("http://202.182.112.88:10002/result/" + userID)
                                .build();
                        //解析JSON数据
                        //String jsontext="{\"Result\":\"Positive\"}";需要的JSON数据格式
                        ProgressDialog progressDialog1 = new ProgressDialog(resultActivity.this);
                        progressDialog1.setMessage("Query...");
                        progressDialog1.setCancelable(true);
                        progressDialog1.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Response response = okHttpClient.newCall(request).execute();
                                    String jsontext = response.body().string();
                                    //String jsontext = "\"Res\":[{\"Restext\":\"negative\",\"Res_N\":0.999,\"Res_P\":0.001},\"Res\":[{\"Restext\":\"egative\",\"Res_N\":0.998,\"Res_P\":0.002}]";
                                    System.out.println(jsontext);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            test_view.setText("Result:" + jsontext);
                                            progressDialog1.dismiss();
                                        }
                                    });
                                    String regex = "([0-9]+[.][0-9]+)";
                                    Pattern pattern = Pattern.compile(regex);
                                    Matcher matcher = pattern.matcher(jsontext);
                                    int i = 0;
                                    while (matcher.find()) {
                                        if (i % 2 == 0) {
                                            Negative.add(Float.valueOf(matcher.group()));
                                        } else {
                                            Positive.add(Float.valueOf(matcher.group()));
                                        }
                                        i++;
                                    }
                                    for (int j = 0; j < Negative.size(); j++) {
                                        neg_rate_online += (Negative.get(j)) / (float) (Negative.size());
                                    }
                                    for (int k = 0; k < Positive.size(); k++) {
                                        pos_rate_online += (Positive.get(k)) / (float) (Positive.size());
                                    }
                                    showTextRes();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                String resonline;
                                if (pos_rate_online < neg_rate_online) {
                                    resonline = "negative";
                                    rate_online = neg_rate_online;
                                } else {
                                    resonline = "positive";

                                    rate_online = pos_rate_online;
                                }
//                                String Str_online = "云端识别结果：" + resonline + "\n识别准确率：" + rate_online;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String Str_online = "";
                                        if (resonline.equals("negative")){
                                            Str_online = "云端识别结果  阴性";
                                        } else {
                                            Str_online = "云端识别结果  阳性";
                                        }

                                        mytv3_3.setText(Str_online);
                                    }
                                });
                            }
                        }).start();
                    }
                });
            }
        });
    }

    //25600
    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("现在开始写onstart");
    }

    //加载文件
    private MappedByteBuffer loadModelFile(String model) throws IOException {
        AssetFileDescriptor fileDescriptor = getApplicationContext().getAssets().openFd(model + ".tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("现在开了onresume");
        final Handler TimerHandler = new Handler();
        Runnable myTimerRun = new Runnable() {
            @Override
            public void run() {
                try {
                    tflite = new Interpreter(loadModelFile("chirpModel"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                request_permission();
                readCacheLabelFromLocalFile();
                predict();
            }
        };
        TimerHandler.postDelayed(myTimerRun, 1000);
    }

    //获取最大值
    private int get_max_result(float[] result) {

        float probability = result[0];
        int r = 0;
        for (int i = 0; i < result.length; i++) {
            if (probability < result[i]) {
                probability = result[i];
                r = i;
            }
        }
        return r;
    }

    //读标签值
    private void readCacheLabelFromLocalFile() {

        try {
            AssetManager assetManager = getApplicationContext().getAssets();
            BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open("cacheLabel.txt")));
            String readLine = null;
            while ((readLine = reader.readLine()) != null) {
                resultLabel.add(readLine);
            }
            reader.close();
        } catch (Exception e) {
            Log.e("labelCache", "error" + e);
        }
    }

    private void request_permission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 1);
        }

    }

    public void AudPreEmphasize() {  // 音频预加重
        fpData = new double[firstChannelArray.length];  // 声明相同的长度
        fpData[0] = firstChannelArray[0];    // 第一个数不变
        for (int i = 1; i < firstChannelArray.length; i++) {    // 之后每个数，后一个减前一个乘系数，就是预加重
            fpData[i] = (firstChannelArray[i]) - (firstChannelArray[i - 1]) * 0.9375;
        }
    }

    //预测
    private void predict() {
        try {
            dwSoundlen = firstChannelArray.length;
            AudPreEmphasize();
            AunEnframe();
            Hamming();
            AudHamming();
            AudSte();
            AudEstimate();
            AudioFile = new float[1][FRM_LEN];
            for (int i = 0; i < maxData.length; i++) {
                AudioFile[0][i] = (float) 1.0;
            }
            for (int i = 0; i < maxData.length; i++) {
                AudioFile[0][i] = (float) maxData[i];
            }

            float[][] labelProbArray = new float[1][2];
            long start = System.currentTimeMillis();

            float[][][] newLabel = new float[1][1][2];
            newLabel[0] = labelProbArray;
            tflite.run(AudioFile, newLabel);//模型入口

            /*
             * 这里就是模型的入口
             *
             * */
            long end = System.currentTimeMillis();
            long time = end - start;
            float[] results = new float[labelProbArray[0].length];
            System.arraycopy(labelProbArray[0], 0, results, 0, labelProbArray[0].length);
            int r = get_max_result(results);
            System.out.println("====================================================================!!!@@@@" + results[r]);
            System.out.println(results[r]);
            System.out.println("====================================================================!!!@@@@");
            if (resultLabel.equals("positive")) {
                resultlocal = "positive";
                pos_rate_local = results[r];
                neg_rate_local = 1 - results[r];
                rate_local = results[r];
            } else {
                resultlocal = "negative";
                neg_rate_local = results[r];
                pos_rate_local = 1 - results[r];
                rate_local = results[r];
            }
            System.out.println("negLocal is " + neg_rate_local);
//            mytv3_2.setText("识别结果：" + resultlocal + "\n识别准确率" + rate_local);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Objects.equals(resultlocal, "negative")){
                        mytv3_2.setText("本地识别结果  阴性");
                    } else {
                        mytv3_2.setText("本地识别结果  阳性");
                    }
                }
            });

            String show_distinct_text = "本地识别结果为" + resultlocal + "\n识别准确率为：" + rate_local;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void AunEnframe() {//分帧
        FrmNum = (fpData.length - FRM_LEN + FRM_SFT) / FRM_SFT;  // 帧数=（数据长度-帧长+窗移）/窗移
        audFrame = new AudFrame[FrmNum];  // 音频帧列表，新建函数列表
        for (int i = 0; i < FrmNum; i++) {
            audFrame[i] = new AudFrame();  // 列表，放入audframe参数
        }
        int x = 0;//
        for (int i = 0; i < FrmNum; i++) {
            audFrame[i].fltFrame = new double[FRM_LEN];
            // audFrame[i].fltFrame[j]代表某一帧某一值
            for (int j = 0; j < FRM_LEN; j++) {
                audFrame[i].fltFrame[j] = fpData[x + j];
            }
            x += FRM_SFT;
            System.out.println("分帧执行完成");
        }
    }


    public void AudHamming() {
        for (int i = 0; i < FrmNum; i++) {  // 遍历帧数FrmNum
            for (int j = 0; j < FRM_LEN; j++) {  // 遍历窗长
                audFrame[i].fltFrame[j] *= fltHamm[j];  // 每一帧用上窗函数
            }
        }
        System.out.println("加窗执行完成");
    }

    public void Hamming() {
        fltHamm = new double[FRM_LEN];
        for (int i = 0; i < FRM_LEN; i++) {
            fltHamm[i] = (0.54 - 0.46 * Math.cos((2 * i * PI1) / (FRM_LEN - 1)));  // 加窗
        }
    }

    public void AudSte() {//计算每帧能量
        for (int i = 0; i < FrmNum; i++) {  // 遍历帧数
            double fltShortEnergy = 0.0;
            for (int j = 0; j < FRM_LEN; j++) {
                fltShortEnergy += Math.pow(audFrame[i].fltFrame[j], 2);
            }
            audFrame[i].fltSte = fltShortEnergy;  // 每一帧总能值
        }
        System.out.println("计算每帧能量执行完成");
    }


    //上传文件的类----------------------------------------------------------------------------------------

    public float get_neg_rate_local() {
        return resultActivity.neg_rate_local;
    }

    public float get_pos_rate_local() {
        return resultActivity.pos_rate_local;
    }

    public void AudEstimate() {
        maxSte = 0.0;  // 最大能量值
        maxData = new double[FRM_LEN];
        for (int i = 0; i < FrmNum; i++) {
            if (maxSte < audFrame[i].fltSte) {
                maxSte = audFrame[i].fltSte;
                System.arraycopy(audFrame[i].fltFrame, 0, maxData, 0, FRM_LEN);
            }
        }
        System.out.println("能量估计执行完成");
    }
    // 结果融合
}
