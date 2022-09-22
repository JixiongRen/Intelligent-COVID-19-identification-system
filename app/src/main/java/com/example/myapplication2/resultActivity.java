package com.example.myapplication2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


public class resultActivity extends AppCompatActivity {

    private static float neg_rate_local;
    private static float pos_rate_local;
    private static String str_neg_rate_online;
    private static String str_pos_rate_online;
    private static  float neg_rate_online;
    private static  float pos_rate_online;
    private static float mixed_neg_rate;
    private static float mixed_pos_rate;

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
    private SpectrogramView mytv3_1;
    private TextView mytv3_2;
    private TextView mytv3_3;
    private TextView NetResult;
    private Interpreter tflite = null;
    private int[] ddims = {1, 3, 224, 224};//-------------------
    private List<String> resultLabel = new ArrayList<>();
    private String PATHOF;
    public static double PI1 = 3.1415926536;//
    public static int FRM_LEN = 64000;//窗长/帧长
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
    String result0="上传：失败";
    String result1="上传：成功";

//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mytv3_1.write(sampleRate, data, mstring);
//    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        File dir=new File(getExternalFilesDir(null),"sounds");
        if(!dir.exists()){
            dir.mkdirs();
        }
        mytv3_3=(TextView) findViewById(R.id.tv3_3);
        PATHOF=dir.getPath();
        mytv3_2 = findViewById(R.id.tv3_2);
        NetResult = findViewById(R.id.netResult);
        Bundle bundle = getIntent().getExtras();
        //String string = Objects.requireNonNull(bundle).getString("0");
        //String string=Environment.getExternalStorageDirectory().getAbsolutePath() +
        //  "/Music/" + "Audio1" + ".wav";
        mstring=bundle.getString("3");

//        mstring = string;//WAV文件地址


        //读声音文件------------------------------------------------------------------------------------------------------------
        try {
            File f = new File(mstring);
            //wavFile=new RandomAccessFile(f,"r");
            //wavFile = new RandomAccessFile(new File("C:\\Users\\LiChao\\Desktop\\test.wav"), "r");
            wavFile=new RandomAccessFile(new File("/storage/emulated/0/Android/data/com.example.myapplication2/files/sounds/test.wav"),"r");
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
        }catch (FileNotFoundException e) {
            Log.e("WAVExplorer","Couldn't find file ");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }




//        WaveFileReader waveFileReader=new WaveFileReader(mstring);
//        System.out.println("=============================================");
//        System.out.println("是否读取成功："+waveFileReader.isSuccess());
//        System.out.println("采用的编码长度为："+waveFileReader.getBitPerSample());
//        System.out.println("采样率为："+waveFileReader.getSampleRate());
//        System.out.println("声道数为："+waveFileReader.getNumChannels());
//        System.out.println("数据长度为："+waveFileReader.getDataLen());
//        double[][] res=waveFileReader.getData();


//        String[]  strs=mstring.split("\\/|\\.");
//        string2 = Environment.getExternalStorageDirectory() + "/DCIM/" + strs[strs.length-2] + ".jpg";
//        dirFile = new File(string2);
//
//        //如果不存在，那就画出频谱图
//        if(!dirFile.exists()) mytv3_1.write(sampleRate, data, mstring);
//----------------------------------------------------------------------------------------------------------------------


        //跳转
        //      Button mbtn3_1 = findViewById(R.id.bt3_1);
        Button mbtn3_2 = findViewById(R.id.bt3_2);
        Button mbtn3_3= findViewById(R.id.bt3_3);
//        mbtn3_1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 删除
//                assert mstring != null;
////                String[]  strs=mstring.split("\\/|\\.");
////                string2 = Environment.getExternalStorageDirectory() + "/DCIM/" + strs[strs.length-2] + ".jpg";
////                File file = new File(mstring);
////                File file2 = new File(string2);
////                boolean deleted = file.delete();
////                boolean deleted2= file2.delete();
//                Intent intent = new Intent(resultActivity.this, MainActivity.class);
//                startActivity(intent);
//
//            }
//        });

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
            @Override
            public void onClick(View view) {
                SendThread sendThread = new SendThread();
                sendThread.start();
                //可能是还没下载完
                File f=new File(PATHOF+"/train_result.txt");

                do {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (f.length()==0);


                try {
                    FileInputStream fileIn=new FileInputStream(PATHOF+"/train_result.txt");
                    System.out.println(fileIn.toString());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                File file=new File(PATHOF+"/train_result.txt");
                FileInputStream in= null;
                try {
                    in = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                String result ="";
                for(int i=0;i< file.length();i++){
                    try {
                        result=result+(char)in.read();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                String a=result.substring(0,8);
                String b=result.substring(9,16);
                String c=result.substring(17,25);
                String d=result.substring(26,35);
                String[] aa={a,b,c,d};
                int p=0;//服务器中阳性个数
                int n=0;//服务器中阴性个数
                for(int i=0;i<aa.length;i++){
                    if(aa[i].equalsIgnoreCase("positive")) {
                        p = p + 1;
                    }
                    if(aa[i].equalsIgnoreCase("negative")) {
                        n = n + 1;
                    }

                }

                //mytv3_3.setText(a+"\n"+b+"\n"+c+"\n"+d);

            }
        });

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
        TimerHandler.postDelayed(myTimerRun,1000);
    }
    //25600
    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("现在开始写onstart");
//        mytv3_1.write(sampleRate, data, mstring);
//        System.out.println("现在完事了");
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

    //预测
    private void predict() {

//        Bitmap bmp = PhotoUtil.getScaleBitmap(image_path);
//        ByteBuffer inputData = PhotoUtil.getScaledMatrix(bmp, ddims);
        try {
            dwSoundlen=firstChannelArray.length;
            AudPreEmphasize();
            AunEnframe();
            Hamming();
            AudHamming();
            AudSte();
            AudEstimate();
            AudioFile=new float[1][FRM_LEN];
            for(int i=0;i<maxData.length;i++)
            {
                AudioFile[0][i]=(float) 1.0;
            }
            for(int i=0;i<maxData.length;i++)
            {
                AudioFile[0][i]=(float) maxData[i];
            }

            float[][] labelProbArray = new float[1][2];
            long start = System.currentTimeMillis();

            float[][][] newLabel=new float[1][1][2];
            newLabel[0]=labelProbArray;
            tflite.run(AudioFile, newLabel);//找到了
            /*
             * 这里就是模型的入口
             *
             * */
            long end = System.currentTimeMillis();
            long time = end - start;
            float[] results = new float[labelProbArray[0].length];
            System.arraycopy(labelProbArray[0], 0, results, 0, labelProbArray[0].length);
            int r = get_max_result(results);
//            String show_text = "The result is: " + r + "\nname: " + resultLabel.get(r) + "\nprobability: " + results[r] + "\ntime: " + time + "ms";
//            float pos_rate_local = 0;
//            float neg_rate_local = 0;
            if (resultLabel.equals("positive")) {
                pos_rate_local = results[r];
                neg_rate_local = 1-results[r];
            } else {
                neg_rate_local = results[r];
                pos_rate_local = 1-results[r];
            }
            String OnlineResultFilePath = "/storage/emulated/0/Android/data/com.example.myapplication2/files/2022-09-06-12-18-57online_model_aver_rate_result.txt";
            File OnlineResultFile = new File(OnlineResultFilePath);
            String encoding = "utf-8";
            try (InputStreamReader read = new InputStreamReader(new FileInputStream(OnlineResultFile),encoding);
                 BufferedReader bufferedReader = new BufferedReader(read)) {
                if (OnlineResultFile.isFile() && OnlineResultFile.exists()) {
                    str_neg_rate_online = bufferedReader.readLine();
                    str_pos_rate_online = bufferedReader.readLine();
                }
            }catch (Exception e) {
                System.out.println("读取文件内容出错");
            }

            neg_rate_online = Float.parseFloat(str_neg_rate_online);
            pos_rate_online = Float.parseFloat(str_pos_rate_online);

            mixed_neg_rate = (float) (0.5*neg_rate_local + 0.5*pos_rate_online);
            mixed_pos_rate = (float) (0.5*pos_rate_local + 0.5*pos_rate_online);

            String mixedResult;
            String onlineResult;
            String localResult;
            float local_rate;
            float online_rate;
            float identy_rate;

            if (mixed_neg_rate > mixed_pos_rate) {
                mixedResult = "negative";
                identy_rate = mixed_neg_rate;
            } else {
                mixedResult = "positive";
                identy_rate = mixed_pos_rate;
            }

            if (neg_rate_online > pos_rate_online) {
                onlineResult = "negative";
                online_rate = neg_rate_online;
            } else {
                onlineResult = "positive";
                online_rate = pos_rate_online;
            }

            if (neg_rate_local > pos_rate_local) {
                localResult = "negative";
                local_rate = neg_rate_local;
            } else {
                localResult = "positive";
                local_rate = pos_rate_local;
            }

            String show_distinct_text = "服务器端ResNet识别结果为：" + onlineResult + "\n识别准确率：" + online_rate + "\n\n本地LeafGhostNet识别结果为：" + localResult + "\n识别准确率为：" + local_rate;
            String show_text = "融合后的识别结果为： "  + mixedResult + "\n识别准确率： " + identy_rate ;
            System.out.println("=====" + show_distinct_text);
            mytv3_3.setText(show_text);
            mytv3_2.setText(show_distinct_text);
//            String dirPath_time = dir.getPath();
//            File LocalResultFile = new File(dirPath_time+"/" + "LocalResult.txt");
//            if (LocalResultFile.exists()){
//                LocalResultFile.delete();
//            }
//            try {
//                LocalResultFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            try {
//                RandomAccessFile tsfoif = new RandomAccessFile(LocalResultFile,"rwd");
//                tsfoif.seek(LocalResultFile.length());
//                tsfoif.write(timeStamp.getBytes());
//                tsfoif.close();
//            } catch(IOException e) {
//                e.printStackTrace();
//            }
//            mytv3_2.setText(show_text);
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    public void AunEnframe() {//分帧
        FrmNum = (fpData.length-FRM_LEN+FRM_SFT) / FRM_SFT;  // 帧数=（数据长度-帧长+窗移）/窗移
        audFrame = new AudFrame[FrmNum];  // 音频帧列表，新建函数列表
        for(int i=0;i<FrmNum;i++){
            audFrame[i] = new AudFrame();  // 列表，放入audframe参数
        }
        int x = 0;//
        for (int i = 0; i < FrmNum; i++) {
            //     for (int i = 0; (i < FrmNum)&&(x<fpData.length); i++) {
            audFrame[i].fltFrame = new double[FRM_LEN];

            // audFrame[i].fltFrame[j]代表某一帧某一值
//            for (int j = 0; (j < FRM_LEN)&&((x+j)<fpData.length); j++) {
            for (int j = 0; j < FRM_LEN; j++) {
                audFrame[i].fltFrame[j] = fpData[x + j];
            }
            x+=FRM_SFT;
            System.out.println("分帧执行完成");
//            System.out.println("x="+x);
        }
    }


    public void Hamming() {
        fltHamm = new double[FRM_LEN];
        for (int i = 0; i < FRM_LEN; i++) {
            fltHamm[i] = (0.54 - 0.46*Math.cos((2*i*PI1) / (FRM_LEN-1)));  // 加窗
        }
    }


    public void AudHamming() {
        for (int i = 0; i < FrmNum; i++) {  // 遍历帧数FrmNum
            for (int j = 0; j < FRM_LEN; j++) {  // 遍历窗长
                audFrame[i].fltFrame[j] *= fltHamm[j];  // 每一帧用上窗函数
//                System.out.println("i="+i);
//                System.out.println("j="+j);
//                System.out.println(audFrame[i].fltFrame[j]);
            }
        }
        System.out.println("加窗执行完成");
    }



    public void AudSte() {//计算每帧能量
        for (int i = 0; i < FrmNum; i++) {  // 遍历帧数
            double fltShortEnergy = 0.0;
            for (int j = 0; j < FRM_LEN; j++) {
                fltShortEnergy += Math.pow(audFrame[i].fltFrame[j],2);
            }
            audFrame[i].fltSte = fltShortEnergy;  // 每一帧总能值
        }
        System.out.println("计算每帧能量执行完成");
    }



    public void AudEstimate(){
        maxSte = 0.0;  // 最大能量值
        maxData=new double[FRM_LEN];
        for(int i = 0; i < FrmNum; i++)  {
            if(maxSte<audFrame[i].fltSte) {
                maxSte = audFrame[i].fltSte;
                System.arraycopy(audFrame[i].fltFrame, 0, maxData, 0, FRM_LEN);
            }
        }
        System.out.println("能量估计执行完成");
    }


    //上传文件的类----------------------------------------------------------------------------------------
    public class SendThread extends Thread {
        Boolean netok=true;
        @Override
        public void run() {
            int readlen;
            String scrWav=mstring;
            Socket socket= null;
            try {
                socket = new Socket();
                socket.setSoTimeout(3000);
                socket.connect(new InetSocketAddress("202.182.112.88",2000),100000);
            } catch (IOException e) {
                e.printStackTrace();
                netok=false;
                resultActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        NetResult.setText(result0);
                    }
                });
            }


            System.out.print("客户端IP："+socket.getLocalAddress());
            System.out.println("  客户端端口："+socket.getLocalPort());
            System.out.print("服务器IP："+socket.getInetAddress());
            System.out.println("  服务器端口："+socket.getPort());

            try {
                FileInputStream fileInputStream=new FileInputStream(scrWav);

                OutputStream outputStream=socket.getOutputStream();
                byte[] bytes=new byte[1024];
                while ((readlen=fileInputStream.read(bytes))!=-1){
                    outputStream.write(bytes,0,readlen);
                }
                System.out.println("WAV文件 OK");
                socket.shutdownOutput();


                //这里总是会出现连接失败
                InputStream in=socket.getInputStream();
                FileOutputStream out=new FileOutputStream(PATHOF+"/train_result.txt");
                //可能是时间太短了，考虑适当休眠
                sleep(500);
                byte[] bytes1=new byte[2048];
                int read;
                while((read= in.read(bytes1))!=-1){
                    out.write(bytes1,0,read);
                }



                System.out.println("TXT文件 OK");
                socket.close();
                resultActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        NetResult.setText(result1);
                    }
                });
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
//            if (netok) {
//                resultActivity.this.runOnUiThread(new Runnable() {
//                    public void run() {
//                        NetResult.setText(result1);
//                    }
//                });
//            }

        }
    }
    public float get_neg_rate_local() {
        return resultActivity.neg_rate_local;
    }
    public float get_pos_rate_local() {
        return resultActivity.pos_rate_local;
    }

    // 结果融合
}
