package com.boxshell.lesson.argenerator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.awt.font.TextAttribute;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static Button mStartBtn;
    private static Button mStopBtn;
    private static Button mPlayBtn;
    private static  int  mSampleRate;

    private static Boolean bRecording;

    private final static String TAG = "ARGenerator";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartBtn = (Button) findViewById(R.id.startButton);
        mStartBtn.setOnClickListener(this);
        mStopBtn = (Button) findViewById(R.id.stopButton);
        mStopBtn.setOnClickListener(this);
        mPlayBtn = (Button) findViewById(R.id.playButton);
        mPlayBtn.setOnClickListener(this);

        mStopBtn.setEnabled(false);
        mPlayBtn.setEnabled(false);

        init_audio();

        bRecording = false;
    }

    private void init_audio() {


    }

    private void clickStartRecord() {
        Thread recordThread = new Thread(new Runnable() {

            @Override
            public void run() {
                bRecording = true;
                startRecord();
            }
        });

        recordThread.start();
        mStartBtn.setEnabled(false);
        mStopBtn.setEnabled(true);
    }

    private void clickStopRecord() {
        bRecording = false;
        mStartBtn.setEnabled(true);
        mStopBtn.setEnabled(false);
        mPlayBtn.setEnabled(true);
    }

    private void clickPlayRecord(){

        Thread recordThread = new Thread(new Runnable() {

            @Override
            public void run() {

                PlayRecord();
            }
        });
        recordThread.start();

        mPlayBtn.setEnabled(false);
        mStartBtn.setEnabled(false);
    }

    private void PlayRecord() {
        // if there is no recording file, then return without playing

        L("Into playRecord");



        File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");
        int shortSizeInBytes = Short.SIZE/Byte.SIZE;

        int bufferSizeInBytes = (int)(file.length()/shortSizeInBytes);
        short[] audioData = new short[bufferSizeInBytes];

        try{
            L("Read data begin");

            InputStream inputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

            int i = 0;
            while(dataInputStream.available() > 0){
                audioData[i] = dataInputStream.readShort();
                i++;
            }

            dataInputStream.close();

            L("Read data finished");

            int sampleFreq = mSampleRate;

            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleFreq,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSizeInBytes,
                    AudioTrack.MODE_STREAM);

            audioTrack.play();

            audioTrack.write(audioData, 0, bufferSizeInBytes);



        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {

            runOnUiThread(new Runnable() {
                public void run() {
                    //something here
                    mPlayBtn.setEnabled(true);
                    mStartBtn.setEnabled(true);
                }
            });

        }

    }

    private void startRecord() {
        File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");
        mSampleRate = 44100;

        try{
            file.createNewFile();

            OutputStream outputStream = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

            int minBufferSize = AudioRecord.getMinBufferSize(mSampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            short[] audioData = new short[minBufferSize];

            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    mSampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize);
            audioRecord.startRecording();

            while(bRecording){
                int numberOfShort = audioRecord.read(audioData, 0, minBufferSize);
                for(int i = 0; i < numberOfShort; i++){
                    dataOutputStream.writeShort(audioData[i]);
                }
            }

            audioRecord.stop();
            audioRecord.release();

            dataOutputStream.close();



        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private void L(String str) {
        Log.d(TAG, str);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == mStartBtn.getId()) {
            L("clicked start");
            clickStartRecord();
        } else if (v.getId() == mStopBtn.getId()) {
            L("clicked stop");
            clickStopRecord();
        } else if (v.getId() == mPlayBtn.getId()) {
            L("clicked play");
            clickPlayRecord();
        }
    }
}
