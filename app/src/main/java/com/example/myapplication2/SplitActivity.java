package com.example.myapplication2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.util.Objects;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.media.AudioManager.STREAM_MUSIC;

import com.example.myapplication2.Wave.SamplePlayer;
import com.example.myapplication2.Wave.WaveCanvas;
import com.example.myapplication2.Wave.WaveSurfaceView;
import com.example.myapplication2.Wave.WaveformView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SplitActivity extends AppCompatActivity {
    private static final int FREQUENCY = 16000;// ????????????????????????44100???????????????????????????????????????????????????22050???16000???11025
    private static final int  CHANNELCONGIFIGURATION = AudioFormat.CHANNEL_IN_MONO;// ???????????????
    private static final int AUDIOENCODING = AudioFormat.ENCODING_PCM_16BIT;// ?????????????????????????????????16???
    public final static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;// ???????????????
    private static final int RequestPermissionCode = 1;
    private static int bufferSize;
    boolean isRecording;
    private final int UPDATE_WAV = 100;
    boolean mLoadingKeepGoing;
    float mDensity;
    int read;
    private float audioFloats[];
    public String PATHOF;
    File file;

    File mFile;
    Thread mLoadSoundFileThread;
    SamplePlayer mPlayer;
    SoundFile mSoundFile;
    WaveCanvas waveCanvas = new WaveCanvas();
    WaveformView waveView;
    WaveSurfaceView waveSfv;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_split);
        waveSfv = findViewById(R.id.wavesfv);
        waveView = findViewById(R.id.waveview);

        //??????
        final Bundle bundle = getIntent().getExtras();
        final String string = Objects.requireNonNull(bundle).getString("0");

        final String string1 = bundle.getString("1");//????????????
        final String pcmstring=bundle.getString("2");//pcm?????????????????????

        File dir=new File(getExternalFilesDir(null),"sounds");
        if(!dir.exists()){
            dir.mkdirs();
        }
        PATHOF=dir.getPath();
        //?????????
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final Chronometer chronometer = findViewById(R.id.tv2_2);
        chronometer.setBase(SystemClock.elapsedRealtime());//??????????????????
        chronometer.setFormat("%s");//????????????
        chronometer.start();//????????????

        //????????????
        if (checkPermission()) {
            Thread thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    file=new File(PATHOF+"/test.pcm");
                    if (file.exists()) {
                        file.delete();
                    }
                    try {
                        file.createNewFile();
                    }catch (IOException e){
                        throw new IllegalStateException("????????????"+file.toString());
                    }
                    try {
                        OutputStream os=new FileOutputStream(file);
                        BufferedOutputStream bos=new BufferedOutputStream(os);
                        DataOutputStream dos=new DataOutputStream(bos);
                        bufferSize=AudioRecord.getMinBufferSize(FREQUENCY,CHANNELCONGIFIGURATION,AUDIOENCODING);
                        AudioRecord audioRecord=new AudioRecord(AUDIO_SOURCE,FREQUENCY,CHANNELCONGIFIGURATION,AUDIOENCODING,bufferSize);
                        short[] buffer=new short[bufferSize];
                        audioRecord.startRecording();
                        isRecording=true;
                        while (isRecording){
                            int bufferReadResult=audioRecord.read(buffer,0,bufferSize);
                            for (int i=0;i<bufferReadResult;i++){
                                dos.writeShort(Short.reverseBytes(buffer[i]));//???????????????16?????????????????????????????????????????????????????????????????????

                            }
                        }
                        audioRecord.stop();
                        dos.close();
                    }catch (Throwable t){
                        System.out.println("????????????");
                    }
                }
            });
            thread.start();
//==========================================================================================
            int recBufSize = AudioRecord.getMinBufferSize(FREQUENCY,
                    CHANNELCONGIFIGURATION, AUDIOENCODING);// ????????????
            // ??????????????????????????????????????? 16000HZ???????????? ???????????? ??????????????????
            AudioRecord maudioRecord = new AudioRecord(AUDIO_SOURCE,
                    FREQUENCY, CHANNELCONGIFIGURATION, AUDIO_SOURCE, recBufSize);
            int aa =  waveSfv.getHeight() / 2;
            waveCanvas.baseLine = waveSfv.getHeight() / 2;
            waveCanvas.Start(maudioRecord, recBufSize, waveSfv, string1, string, new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    return true;
                }
            });
//===========================================================================================

        } else {
            requestPermission();
            System.out.println("???????????????");
    }




//        Button mbtn2_1 = findViewById(R.id.bt2_1);
//        mbtn2_1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//
//            }
//        });




        // ????????????????????????
//        Button mbtn2_1 = findViewById(R.id.bt2_1);
//        mbtn2_1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // ??????
//                if (mPlayer == null)
//                    return;
//                if (mPlayer.isPlaying()) {
//                    mPlayer.pause();
//                    updateTime.removeMessages(UPDATE_WAV);
//                }
//                //??????????????????fft???
//                int mPlayStartMsec = waveView.pixelsToMillisecs(0);
//                mPlayEndMsec = waveView.pixelsToMillisecsTotal();
//                mPlayer.setOnCompletionListener(new SamplePlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion() {
//                        waveView.setPlayback(-1);
//                        updateDisplay();
//                        updateTime.removeMessages(UPDATE_WAV);
//                        Toast.makeText(getApplicationContext(),"????????????",Toast.LENGTH_LONG).show();
//                    }
//                });
//                mPlayer.seekTo(mPlayStartMsec);
//                mPlayer.start();
//                Message msg = new Message();
//                msg.what = UPDATE_WAV;
//                updateTime.sendMessage(msg);
//            }
//        });//????????????????????????????????????????????????


        // ????????????????????????
        Button mbtn2_2 = findViewById(R.id.bt2_2);
        mbtn2_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @SuppressLint("ResourceType") Animation animation = AnimationUtils.loadAnimation(SplitActivity.this,R.drawable.pusheffect);
                mbtn2_2.setAnimation(animation);
                chronometer.stop();//????????????
                isRecording=false;
                mbtn2_2.setEnabled(false);
                String pattern = "yyyy-MM-dd-HH-mm-ss";
                Long timestamp = System.currentTimeMillis();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String timeStamp = simpleDateFormat.format(new Date(timestamp));
                System.out.println("This is time : " + timeStamp);
                String wavFileName = "/" + timeStamp + "test.wav";
                // ??????????????????txt???????????????????????????
                File dir_time=new File(getExternalFilesDir(null),"TimeStamp");
                if(!dir_time.exists()){
                    dir_time.mkdirs();
                }
                String dirPath_time = dir.getPath();
                File TimeStampFile = new File(dirPath_time+"/" + "TimeStamps.txt");
                if (TimeStampFile.exists()){
                    TimeStampFile.delete();
                }
                try {
                    TimeStampFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    RandomAccessFile tsfoif = new RandomAccessFile(TimeStampFile,"rwd");
                    tsfoif.seek(TimeStampFile.length());
                    tsfoif.write(timeStamp.getBytes());
                    tsfoif.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }




                PCMCovWavUtil x=new PCMCovWavUtil(FREQUENCY,CHANNELCONGIFIGURATION,AUDIOENCODING,bufferSize,PATHOF+"/test.pcm",PATHOF+wavFileName,PATHOF);
                x.convertWaveFile();//?????????????????????
                try {
                    waveCanvas.Stop();
                } catch (IllegalStateException e) {
                    System.out.println("????????????");
                }
                waveCanvas = null;
                Toast.makeText(SplitActivity.this, "Recording stopped", Toast.LENGTH_SHORT).show();
                // ??????wav??????????????????
                try {
                    Thread.sleep(300);//??????????????????????????????????????? ??????????????????
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                assert string != null;
                mFile = new File(string);    //test.wav??????
                mLoadingKeepGoing = true;
                // Load the sound file in a background thread  ????????????????????????????????????
                mLoadSoundFileThread = new Thread() {
                    public void run() {
                        try {
                            mSoundFile = SoundFile.create(mFile.getAbsolutePath(),null);
                            if (mSoundFile == null) {
                                return;
                            }
                            mPlayer = new SamplePlayer(mSoundFile);
                        } catch (final Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        if (mLoadingKeepGoing) {
                            Runnable runnable = new Runnable() {
                                public void run() {
                                    //waveview??????????????????
                                    waveView.setSoundFile(mSoundFile);
                                    DisplayMetrics metrics = new DisplayMetrics();
                                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                    mDensity = metrics.density;
                                    waveView.recomputeHeights(mDensity);

                                    waveSfv.setVisibility(View.INVISIBLE);
                                    waveView.setVisibility(View.VISIBLE);
                                }
                            };
                            SplitActivity.this.runOnUiThread(runnable);
                        }
                    }
                };
                mLoadSoundFileThread.start();




                bundle.putString("3",PATHOF+"/test.wav");//1111111111111111111111
            }
        });

        // ????????????????????????
        Button mbtn2_3 = findViewById(R.id.bt2_3);
        mbtn2_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ?????????????????????
                Intent intent = new Intent(SplitActivity.this, resultActivity.class);
                chronometer.stop();//????????????
//                if (waveCanvas != null){
//                    waveCanvas.Stop();
//                    waveCanvas = null;
//                }



//                int readlen;
//                    String scrWav=PATHOF+"/test.wav";
//                Socket socket= null;
//                try {
//                    socket = new Socket();
//                    socket.setSoTimeout(3000);
//                    socket.connect(new InetSocketAddress("202.182.112.88",2000),3000);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }finally {
//                    System.out.println("???????????????");
//                }
//
//                System.out.print("?????????IP???"+socket.getLocalAddress());
//                System.out.println("  ??????????????????"+socket.getLocalPort());
//                System.out.print("?????????IP???"+socket.getInetAddress());
//                System.out.println("  ??????????????????"+socket.getPort());
//
//                try {
//                    FileInputStream fileInputStream=new FileInputStream(scrWav);
//                    OutputStream outputStream=socket.getOutputStream();
//                    byte[] bytes=new byte[1024];
//                    while ((readlen=fileInputStream.read(bytes))!=-1){
//                        outputStream.write(bytes,0,readlen);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("???????????????????????????");
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });
    }




    //????????????
    private void requestPermission() {
        ActivityCompat.requestPermissions(SplitActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO,READ_EXTERNAL_STORAGE}, RequestPermissionCode);
    }

    //????????????
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestPermissionCode) {
            if (grantResults.length > 0) {
                boolean StoragePermission = grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED;
                boolean RecordPermission = grantResults[1] ==
                        PackageManager.PERMISSION_GRANTED;
                boolean StoragePermission1=grantResults[2]==
                        PackageManager.PERMISSION_GRANTED;

                if (StoragePermission && RecordPermission) {
                    Toast.makeText(SplitActivity.this, "Permission Granted",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SplitActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //????????????
    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result2=ContextCompat.checkSelfPermission(getApplicationContext(),READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2==PackageManager.PERMISSION_GRANTED;
    }

   //??????????????????????????????????????????


}


