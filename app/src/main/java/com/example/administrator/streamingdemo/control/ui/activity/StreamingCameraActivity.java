package com.example.administrator.streamingdemo.control.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.io.IOException;
import java.nio.ShortBuffer;

import com.example.administrator.streamingdemo.R;
import com.example.administrator.streamingdemo.control.BaseApplication;
import com.example.administrator.streamingdemo.model.BasicInfo;
import com.example.administrator.streamingdemo.model.StreamSettingInfo;
import com.example.administrator.streamingdemo.model.api.POJO.BaseResponse;
import com.example.administrator.streamingdemo.model.api.event.BaseEvent;
import com.example.administrator.streamingdemo.model.utils.EventType;
import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.googlecode.javacv.cpp.opencv_core.*;

/**
 * Created by linhtruong on 4/27/2016.
 * Using this: https://github.com/vanevery/JavaCV_0.3_stream_test
 * Streaming camera to rtmp server:
 * - catch yuv frame bytes in onPreviewFrame and push to ffmpeg recorder
 * - ffmpeg will encode to flv, packet rtmp and send
 */
public class StreamingCameraActivity extends Activity implements OnClickListener {

    private final static String TAG = "StreamingCameraActivity";

    private PowerManager.WakeLock mWakeLock;
    //    private String ffmpeg_link = "rtmp://a.rtmp.youtube.com/live2/svfy-fddj-r44p-dzfc";
    private String ffmpeg_link = "rtmp://192.168.86.224/live/linh3"; // wowza local server
    //    private String ffmpeg_link = "rtmp://live:live@128.122.151.108:1935/live/test.flv";
//    private String ffmpeg_link = "/mnt/sdcard/new_stream.flv";
//        private String ffmpeg_link = "rtmp://192.168.168.42/live/linh1";
    private volatile FFmpegFrameRecorder recorder;
    boolean recording = false;
    long startTime = 0;

    private int sampleAudioRateInHz = 44100;
    private int imageWidth = 640;
    private int imageHeight = 480;
    private int frameRate = 60;

    private Thread audioThread;
    volatile boolean runAudioThread = true;
    private AudioRecord audioRecord;
    private AudioRecordRunnable audioRecordRunnable;

    private CameraView cameraView;
    private IplImage yuvIplimage = null;

    private Button recordButton;
    private FrameLayout mainLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_streaming_camera);

        ffmpeg_link = getStreamLink();
        Log.d(TAG, "ffmpeg = " + ffmpeg_link);

        initLayout();
        initRecorder();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
            mWakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        recording = false;
    }

    private String getStreamLink() {
        String url = "";
        BasicInfo info = BasicInfo.getInstance();
        url = info.getStreamServer() + "/" + info.getStreamKey();
        return url;
    }


    private void initLayout() {

        mainLayout = (FrameLayout) this.findViewById(R.id.record_layout);
//        recordButton = (Button) findViewById(R.id.recorder_control);

        DisplayMetrics dp = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dp);
        imageWidth = dp.widthPixels;
        imageHeight = dp.heightPixels;
        Log.d(TAG, "w = " + imageWidth + " - h = " + imageHeight);

        cameraView = new CameraView(this);
        LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(imageWidth, imageHeight);
        mainLayout.addView(cameraView, layoutParam);

        recordButton = new Button(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        recordButton.setLayoutParams(lp);
        mainLayout.addView(recordButton);

        recordButton.setText("Start");
        recordButton.setOnClickListener(this);

        Log.v(TAG, "added cameraView to mainLayout");
    }

    private void initRecorder() {
        Log.w(TAG, "initRecorder");

        if (yuvIplimage == null) {
            // Recreated after frame size is set in surface change method
            yuvIplimage = IplImage.create(imageWidth, imageHeight, IPL_DEPTH_8U, 2);
            //yuvIplimage = IplImage.create(imageWidth, imageHeight, IPL_DEPTH_32S, 2);

            Log.v(TAG, "IplImage.create");
        }

        recorder = new FFmpegFrameRecorder(ffmpeg_link, imageWidth, imageHeight, 1);
        Log.v(TAG, "FFmpegFrameRecorder: " + ffmpeg_link + " imageWidth: " + imageWidth + " imageHeight " + imageHeight);

        recorder.setFormat("flv");
        Log.v(TAG, "recorder.setFormat(\"flv\")");

        recorder.setSampleRate(sampleAudioRateInHz);
        Log.v(TAG, "recorder.setSampleRate(sampleAudioRateInHz)");

        // re-set in the surface changed method as well
        recorder.setFrameRate(frameRate);
        Log.v(TAG, "recorder.setFrameRate(frameRate)");


        // Create audio recording thread
        audioRecordRunnable = new AudioRecordRunnable();
        audioThread = new Thread(audioRecordRunnable);

    }

    // Start the capture
    public void startRecording() {
        try {
            if (recorder == null) {
                initRecorder();
            }
            recorder.start();
            recording = true;
            startTime = System.currentTimeMillis();
            audioThread.start();

        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());

        }
    }

    public void stopRecording() {
        // This should stop the audio thread from running
        runAudioThread = false;

        if (recorder != null && recording) {
            recording = false;
            Log.v(TAG, "Finishing recording, calling stop and release on recorder");
            try {
                recorder.stop();
                recorder.release();
            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }
            recorder = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Quit when back button is pushed
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (recording) {
                stopRecording();
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        if (!recording) {
            startRecording();
            Log.d(TAG, "Start Button Pushed");
            recordButton.setText("Stop");
            StreamSettingInfo info = BasicInfo.getInstance().getStreamInfo();
            BaseApplication.getInstance().getService().startStream(BasicInfo.getInstance().getApitoken(), info.getTitle(), info.getDescription()
                    , info.getIsArchiving(), info.getIsMakeArhieve(), info.getIsLiveChat(), info.getRestriction());
            Log.d(TAG, info.getTitle() + " - " + info.getDescription() + " - " + info.getIsArchiving() +
                    " - " + info.getIsMakeArhieve() + " - " + info.getIsLiveChat() + " - " + info.getRestriction());
        } else {
            stopRecording();
            BaseApplication.getInstance().getService().stopStream(BasicInfo.getInstance().getApitoken());
            Log.w(TAG, "Stop Button Pushed");
            recordButton.setText("Start");
        }
    }

    @Subscribe
    public void onEvent(BaseEvent event) {
        int id = event.getEventType();
        switch (id) {
            case EventType.START_STREAMING_STARTED:
                Log.d(TAG, "START_STREAMING_STARTED");
                break;

            case EventType.START_STREAMING_FINISHED:
                Log.d(TAG, "START_STREAMING_FINISHED ");

                break;
        }
    }

    //---------------------------------------------
    // audio thread, gets and encodes audio data
    //---------------------------------------------
    class AudioRecordRunnable implements Runnable {

        @Override
        public void run() {
            // Set the thread priority
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            // Audio
            int bufferSize;
            short[] audioData;
            int bufferReadResult;

            bufferSize = AudioRecord.getMinBufferSize(sampleAudioRateInHz,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleAudioRateInHz,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            audioData = new short[bufferSize];

            Log.d(TAG, "audioRecord.startRecording()");
            audioRecord.startRecording();

            // Audio Capture/Encoding Loop
            while (runAudioThread) {
                // Read from audioRecord
                bufferReadResult = audioRecord.read(audioData, 0, audioData.length);
                if (bufferReadResult > 0) {
                    //Log.v(TAG,"audioRecord bufferReadResult: " + bufferReadResult);

                    // Changes in this variable may not be picked up despite it being "volatile"
                    if (recording) {
                        try {
                            // Write to FFmpegFrameRecorder
                            recorder.record(ShortBuffer.wrap(audioData, 0, bufferReadResult));
                        } catch (FFmpegFrameRecorder.Exception e) {
                            Log.v(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
            Log.v(TAG, "AudioThread Finished");

            /* Capture/Encoding finished, release recorder */
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
                Log.v(TAG, "audioRecord released");
            }
        }
    }


    class CameraView extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {

        private boolean previewRunning = false;

        private SurfaceHolder holder;
        private Camera camera;

        private byte[] previewBuffer;

        long videoTimestamp = 0;

        Bitmap bitmap;
        Canvas canvas;

        public CameraView(Context _context) {
            super(_context);

            holder = this.getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            camera = Camera.open();
//            openCamera(imageWidth, imageHeight);

            try {
                camera.setPreviewDisplay(holder);
                camera.setPreviewCallback(this);
                Camera.Parameters currentParams = camera.getParameters();
                Log.v(TAG, "Preview Framerate: " + currentParams.getPreviewFrameRate());
                Log.v(TAG, "Preview imageWidth: " + currentParams.getPreviewSize().width + " imageHeight: " + currentParams.getPreviewSize().height);

                // Use these values
                imageWidth = currentParams.getPreviewSize().width;
                imageHeight = currentParams.getPreviewSize().height;
//                frameRate = currentParams.getPreviewFrameRate();

//                bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ALPHA_8);


            /*    Log.v(TAG,"Creating previewBuffer size: " + imageWidth * imageHeight * ImageFormat.getBitsPerPixel(currentParams.getPreviewFormat())/8);
	        	previewBuffer = new byte[imageWidth * imageHeight * ImageFormat.getBitsPerPixel(currentParams.getPreviewFormat())/8];
				camera.addCallbackBuffer(previewBuffer);
	            camera.setPreviewCallbackWithBuffer(this);*/

                camera.startPreview();
                previewRunning = true;
            } catch (IOException e) {
                Log.v(TAG, e.getMessage());
                e.printStackTrace();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.v(TAG, "Surface Changed: width " + width + " height: " + height);

            // We would do this if we want to reset the camera parameters
            /*
            if (!recording) {
    			if (previewRunning){
    				camera.stopPreview();
    			}

    			try {
    				//Camera.Parameters cameraParameters = camera.getParameters();
    				//p.setPreviewSize(imageWidth, imageHeight);
    			    //p.setPreviewFrameRate(frameRate);
    				//camera.setParameters(cameraParameters);

    				camera.setPreviewDisplay(holder);
    				camera.startPreview();
    				previewRunning = true;
    			}
    			catch (IOException e) {
    				Log.e(TAG,e.getMessage());
    				e.printStackTrace();
    			}
    		}
            */

            // Get the current parameters
            Camera.Parameters currentParams = camera.getParameters();
            Log.v(TAG, "Preview Framerate: " + currentParams.getPreviewFrameRate());
            Log.v(TAG, "Preview imageWidth: " + currentParams.getPreviewSize().width + " imageHeight: " + currentParams.getPreviewSize().height);

            // Use these values
            imageWidth = currentParams.getPreviewSize().width;
            imageHeight = currentParams.getPreviewSize().height;
//            frameRate = currentParams.getPreviewFrameRate();
            // Create the yuvIplimage if needed
            yuvIplimage = IplImage.create(imageWidth, imageHeight, IPL_DEPTH_8U, 2);
            //yuvIplimage = IplImage.create(imageWidth, imageHeight, IPL_DEPTH_32S, 2);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            try {
                camera.setPreviewCallback(null);

                previewRunning = false;
                camera.release();

            } catch (RuntimeException e) {
                Log.v(TAG, e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

            if (yuvIplimage != null && recording) {
                videoTimestamp = 500 * (System.currentTimeMillis() - startTime);

                Log.d(TAG, "lenght? + " + data.length);
                // Put the camera preview frame right into the yuvIplimage object
                yuvIplimage.getByteBuffer().put(data);

                // FAQ about IplImage:
                // - For custom raw processing of data, getByteBuffer() returns an NIO direct
                //   buffer wrapped around the memory pointed by imageData, and under Android we can
                //   also use that Buffer with Bitmap.copyPixelsFromBuffer() and copyPixelsToBuffer().
                // - To get a BufferedImage from an IplImage, we may call getBufferedImage().
                // - The createFrom() factory method can construct an IplImage from a BufferedImage.
                // - There are also a few copy*() methods for BufferedImage<->IplImage data transfers.

                // Let's try it..
                // This works but only on transparency
                // Need to find the right Bitmap and IplImage matching types

            	/*
                bitmap.copyPixelsFromBuffer(yuvIplimage.getByteBuffer());
            	//bitmap.setPixel(10,10,Color.MAGENTA);

            	canvas = new Canvas(bitmap);
            	Paint paint = new Paint();
            	paint.setColor(Color.GREEN);
            	float leftx = 20;
            	float topy = 20;
            	float rightx = 50;
            	float bottomy = 100;
            	RectF rectangle = new RectF(leftx,topy,rightx,bottomy);
            	canvas.drawRect(rectangle, paint);

            	bitmap.copyPixelsToBuffer(yuvIplimage.getByteBuffer());
                */
                //Log.v(TAG,"Writing Frame");

                try {
                    Log.d(TAG, "time = " + videoTimestamp + " w = " + yuvIplimage.width());

                    // Get the correct time
                    recorder.setTimestamp(videoTimestamp);

                    // Record the image into FFmpegFrameRecorder
                    recorder.record(yuvIplimage);

                } catch (FFmpegFrameRecorder.Exception e) {
                    Log.v(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

}


