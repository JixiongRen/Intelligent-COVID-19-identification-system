package com.example.myapplication2;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PCMCovWavUtil {
    private  int audioRate;
    //录音的声道，单声道
    private  int audioChannel;
    //量化的深度
    private  int audioFormat;
    //缓存的大小
    private  int bufferSize;

    //PCM文件
    private File pcmFile;
    //WAV文件
    private File wavFile;

    private String basePath ;
    //wav文件目录
    private String outFileName;
    //pcm文件目录
    private String inFileName;

    // samHz,shengdao,audioFormat,bufferSize
    public PCMCovWavUtil(int audioRate,int audioChannel,int audioFormat,int bufferSize,String inFileName,String outFileName,String basePath){

        this.audioRate = audioRate;
        this.audioChannel = audioChannel;
        this.audioFormat = audioFormat;
        this.bufferSize = bufferSize;
        this.inFileName=inFileName;
        this.outFileName=outFileName;
        this.basePath=basePath;
        File baseFile = new File(basePath);
        if(!baseFile.exists())
            baseFile.mkdirs();
        pcmFile = new File(inFileName);
        wavFile = new File(outFileName);
        try{
            if (!pcmFile.exists())
                pcmFile.createNewFile();

            if (!wavFile.exists())
                wavFile.createNewFile();
        }catch(IOException e){}
    }

    //转换函数
    public void convertWaveFile() {
        File  twavFile = new File(outFileName);
        if(twavFile.exists()){
            twavFile.delete();
        }
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = audioRate;
        int channels = 1;
        long byteRate = 16 * audioRate * channels / 8;
        if (audioFormat == AudioFormat.ENCODING_PCM_16BIT){
            byteRate = 16 * audioRate * channels / 8;
        }else if (audioFormat == AudioFormat.ENCODING_PCM_8BIT){
            byteRate = 8 * audioRate * channels / 8;
        }
        byte[] data = new byte[bufferSize];
        try {
            in = new FileInputStream(inFileName);
            out = new FileOutputStream(outFileName);
            totalAudioLen = in.getChannel().size();
            //由于不包括前面的8个字节RIFF和WAV
            totalDataLen = totalAudioLen + 36;
            addWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
            System.out.println("==========转换完毕================");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //添加Wav头部信息
    private void addWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        // RIFF 头表示
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        //数据大小，数据大小，真正大小是添加了8bit
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        //wave格式
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //fmt Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        int tongdaowei;
        if ( audioChannel ==  AudioFormat.CHANNEL_IN_MONO){
            tongdaowei =1;
        }else{
            tongdaowei =2;
        }
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        if (audioFormat == AudioFormat.ENCODING_PCM_16BIT){
            header[32] = (byte) (tongdaowei * 16 / 8);
        }else if (audioFormat == AudioFormat.ENCODING_PCM_8BIT){
            header[32] = (byte) (tongdaowei * 8 / 8);
        }
        // header[32] = (byte) (1 * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        if (audioFormat == AudioFormat.ENCODING_PCM_16BIT){
            header[34] = 16;
        }else if (audioFormat == AudioFormat.ENCODING_PCM_8BIT){
            header[34] = 8;
        }
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }
}
