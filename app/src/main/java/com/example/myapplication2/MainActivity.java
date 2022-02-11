package com.example.myapplication2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.myapplication2.utils.PathUtils;
import com.pedro.rtmp.utils.ConnectCheckerRtmp;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ConnectCheckerRtmp, SurfaceHolder.Callback {

    private RtmpCamera1 rtmpCamera1;

    private String currentDateAndTime = "";
    private File folder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   // 전체로 가져오는
        setContentView(R.layout.activity_main);

        folder = PathUtils.getRecordPath();

        SurfaceView surfaceView = findViewById(R.id.cameraView);
        rtmpCamera1 = new RtmpCamera1(surfaceView, this);
        rtmpCamera1.setReTries(10);
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!rtmpCamera1.isStreaming()) {
            // rtmpCamera1.isRecording() -> 아래 조건문에 이게 들어갔음
            if(rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
                rtmpCamera1.startStream("");
            }
            else {
                Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(rtmpCamera1.isStreaming()) {
            rtmpCamera1.stopStream();
        }
    }

    /** ConnectCheckerRtmp를 온전히 상속 받기 위한 메소드 (0) **/
    @Override
    public void onConnectionStartedRtmp(String rtmpUrl) {
    }

    /** ConnectCheckerRtmp를 온전히 상속 받기 위한 메소드 (1) **/
    @Override
    public void onNewBitrateRtmp(long bitrate) {
    }

    /** ConnectCheckerRtmp를 온전히 상속 받기 위한 메소드 (2) **/
    @Override
    public void onConnectionSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** ConnectCheckerRtmp를 온전히 상속 받기 위한 메소드 (3) **/
    @Override
    public void onConnectionFailedRtmp(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rtmpCamera1.reTry(5000, reason)) {
                    Toast.makeText(MainActivity.this, "Retry", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT).show();
                    rtmpCamera1.stopStream();
                    //button.setText(R.string.start_button);      // 버튼의 텍스트 변경하는건데
                }
            }
        });
    }

    /** ConnectCheckerRtmp를 온전히 상속 받기 위한 메소드 (4) **/
    @Override
    public void onDisconnectRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** ConnectCheckerRtmp를 온전히 상속 받기 위한 메소드 (5) **/
    @Override
    public void onAuthErrorRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** ConnectCheckerRtmp를 온전히 상속 받기 위한 메소드 (6) **/
    @Override
    public void onAuthSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** SurfaceHolder.Callback을 온전히 상속받기 위한 메소드 (1) **/
    /** SurfaceHolder를 사용하기 위함 **/
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    }

    /** SurfaceHolder.Callback을 온전히 상속받기 위한 메소드 (2) **/
    /** 인자 : format, width, height **/
    /** SurfaceView 상태가 바뀔 때마다 해당 메소드가 수행됨 **/
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        rtmpCamera1.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        /** (실행시키는 기계의 SDK의 버전을 가져옴  >=  JELLY_BEAN_MR2 보다 크면) && 지금 isRecording중이면 **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1.isRecording()) {
            /** stopRecord **/
            rtmpCamera1.stopRecord();

            /** 날짜 기록을 위한 **/
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            currentDateAndTime = sdf.format(new Date());

            /** 영상 파일을 기록 **/
            PathUtils.updateGallery(this, folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");

            //bRecord.setText(R.string.start_record); // 이건 버튼 문구 변경시키는거

            /** 밑에 안내 문구 출력 **/
            Toast.makeText(this, "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(), Toast.LENGTH_SHORT).show();

            /** 날짜 초기화 **/
            currentDateAndTime = "";
        }
        /** 만약 지금 스트리밍 중이라면 ? **/
        if (rtmpCamera1.isStreaming()) {
            /** stopStream **/
            rtmpCamera1.stopStream();
            //button.setText(getResources().getString(R.string.start_button));  // 버튼 문구 변경시키는..?
        }
        rtmpCamera1.stopPreview();      // 프리뷰 종료
    }
}