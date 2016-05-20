package com.example.administrator.streamingdemo.control.core.encoder;

import android.annotation.TargetApi;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.example.administrator.streamingdemo.model.utils.lib.sea.SrsEncoder;
import com.example.administrator.streamingdemo.model.utils.lib.sea.SrsFlvMuxer;
import com.example.administrator.streamingdemo.model.utils.lib.sea.rtmp.RtmpPublisher;
import com.example.administrator.streamingdemo.model.utils.lib.sea.rtmp.io.RtmpConnection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Record screen with MediaProjection intent
 * Using MediaCodec as encoder and return h264 bitstream
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ScreenRecorder extends Thread {
    private static final String TAG = "ScreenRecorder";

    private int mWidth;
    private int mHeight;
    private int mBitRate;
    private int mDpi;
    private String mDstPath;

    private MediaProjection mMediaProjection;
    private MediaCodec mEncoder;
    private Surface mSurface;
    private VirtualDisplay mVirtualDisplay;
    private MediaCodec.BufferInfo mBufferInfo;

    private SrsFlvMuxer muxer;

    // encoder config
    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 60; // 60 fps
    private static final int IFRAME_INTERVAL = 10; // 10 seconds between I-frames
    private static final int TIMEOUT_US = 10000;

    private H264ToFLV h264ToFLV;

    private int mVideoTrackIndex = -1;
    private int atrack;
    private AtomicBoolean mQuit = new AtomicBoolean(false);
    private SrsEncoder mSrsEncoder;

    RtmpConnection rtmpConnection;

    public ScreenRecorder(int width, int height, int bitrate, int dpi, MediaProjection mp, String dstPath) {
        super(TAG);
        mWidth = width;
        mHeight = height;
        mBitRate = bitrate;
        mDpi = dpi;
        mMediaProjection = mp;
        mDstPath = dstPath;
        mBufferInfo = new MediaCodec.BufferInfo();

        mSrsEncoder = new SrsEncoder(new RtmpPublisher.EventHandler() {
            @Override
            public void onRtmpConnecting(String msg) {
            }

            @Override
            public void onRtmpConnected(String msg) {
            }

            @Override
            public void onRtmpVideoStreaming(String msg) {
            }

            @Override
            public void onRtmpAudioStreaming(String msg) {
            }

            @Override
            public void onRtmpStopped(String msg) {
            }

            @Override
            public void onRtmpDisconnected(String msg) {
            }

            @Override
            public void onRtmpOutputFps(final double fps) {
                Log.i(TAG, String.format("Output Fps: %f", fps));
            }
        });

//        SrsEncoder.vbitrate = getsh.getInt("vbitrate", SrsEncoder.vbitrate);
        SrsEncoder.rtmpUrl = "rtmp://192.168.86.224/live/linh3";

        rtmpConnection = new RtmpConnection(new RtmpPublisher.EventHandler() {
            @Override
            public void onRtmpConnecting(String msg) {
            }

            @Override
            public void onRtmpConnected(String msg) {
            }

            @Override
            public void onRtmpVideoStreaming(String msg) {
            }

            @Override
            public void onRtmpAudioStreaming(String msg) {
            }

            @Override
            public void onRtmpStopped(String msg) {
            }

            @Override
            public void onRtmpDisconnected(String msg) {
            }

            @Override
            public void onRtmpOutputFps(final double fps) {
                Log.i(TAG, String.format("Output Fps: %f", fps));
            }
        });

        h264ToFLV = new H264ToFLV(rtmpConnection);
    }

    @Override
    public synchronized void start() {
        super.start();
        mSrsEncoder.start();
     /*   aworker = new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
                startAudio();
            }
        });
        aloop = true;
        aworker.start();*/
    }

    private AudioRecord mic = null;
    private boolean aloop = false;
    private Thread aworker = null;

    private void onGetPcmFrame(byte[] pcmBuffer, int size) {
        mSrsEncoder.onGetPcmFrame(pcmBuffer, size);
    }

    private void startAudio() {
        if (mic != null) {
            return;
        }

        int bufferSize = 2 * AudioRecord.getMinBufferSize(SrsEncoder.ASAMPLERATE, SrsEncoder.ACHANNEL, SrsEncoder.AFORMAT);
        mic = new AudioRecord(MediaRecorder.AudioSource.MIC, SrsEncoder.ASAMPLERATE, SrsEncoder.ACHANNEL, SrsEncoder.AFORMAT, bufferSize);
        mic.startRecording();

        byte pcmBuffer[] = new byte[4096];
        while (aloop && !Thread.interrupted()) {
            int size = mic.read(pcmBuffer, 0, pcmBuffer.length);
            if (size <= 0) {
                Log.e(TAG, "***** audio ignored, no data to read.");
                break;
            }
            onGetPcmFrame(pcmBuffer, size);
        }
    }

    /**
     * stop task
     */
    public final void quit() {
        mQuit.set(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void run() {
        try {
            try {
                prepareEncoder();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG + "-display",
                    mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    mSurface, null, null);
            Log.d(TAG, "created virtual display: " + mVirtualDisplay);

            recordVirtualDisplay();
        } finally {
            Log.d(TAG, "release res");
            release();
        }
    }

    private void recordVirtualDisplay() {
        String path = "file.txt";
        String pathSpsPPs = "spsPps.txt";
        FileOutputStream output = null;
        FileOutputStream spsPpsOutput = null;
        try {
            output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + path);
            spsPpsOutput = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + pathSpsPPs);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ByteBuffer[] outBuffers = mEncoder.getOutputBuffers();

        try {
            rtmpConnection.connect("rtmp://125.212.194.16:1935/live/linh");
            rtmpConnection.publish("live");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!mQuit.get()) {
            int index = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_US);
            Log.i(TAG, "dequeue output buffer index=" + index);

            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                //
            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.d(TAG, "retrieving buffers time out!");
                try {
                    // wait 10ms
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.d(TAG, "interrupted");
                }
            } else if (index >= 0) { //valid data
                encodeToVideoTrack(index, output, spsPpsOutput);
//
//                ByteBuffer bb = outBuffers[index];
//                Log.d("LINH", "bb =  " + bb.array().length);
//                mSrsEncoder.onEncodedAacFrame(bb, mBufferInfo);
                // release buffer then
//                mEncoder.releaseOutputBuffer(index, false);
                mEncoder.releaseOutputBuffer(index, false);
            }
        }
        h264ToFLV.release();
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void encodeToVideoTrack(int index, FileOutputStream output, FileOutputStream spsPpsOutput) {
        ByteBuffer encodedData = mEncoder.getOutputBuffer(index);

        if (mBufferInfo.size != 0) {
            byte[] outData = new byte[mBufferInfo.size];
            encodedData.get(outData);

            if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                Log.d(TAG, "get SPS PPS data");
                try {
                    spsPpsOutput.write(outData, 0, outData.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "get all data");
//            mSrsEncoder.onEncodedAacFrame(encodedData, mBufferInfo);
//            h264ToFLV.readInput(outData);

            try {
                muxer.writeSampleData(vtrack, encodedData, mBufferInfo);
            } catch (Exception e) {
                Log.e(TAG, "muxer write video sample failed.");
                e.printStackTrace();
            }
/*

            try {
                muxer.writeSampleData(vtrack, encodedData, mBufferInfo);
            } catch (Exception e) {
                Log.e(TAG, "muxer write video sample failed.");
                e.printStackTrace();
            }*/

            /*try {
                output.write(outData, 0, outData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void prepareEncoder() throws IOException {

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        Log.d(TAG, "created video format: " + format);
        mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        mSurface = mEncoder.createInputSurface();
        Log.d(TAG, "created input surface: " + mSurface);

        muxer = new SrsFlvMuxer(new RtmpPublisher.EventHandler() {
            @Override
            public void onRtmpConnecting(String msg) {
            }

            @Override
            public void onRtmpConnected(String msg) {
            }

            @Override
            public void onRtmpVideoStreaming(String msg) {
            }

            @Override
            public void onRtmpAudioStreaming(String msg) {
            }

            @Override
            public void onRtmpStopped(String msg) {
            }

            @Override
            public void onRtmpDisconnected(String msg) {
            }

            @Override
            public void onRtmpOutputFps(final double fps) {
                Log.i(TAG, String.format("Output Fps: %f", fps));
            }
        });

        MediaFormat vformat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mWidth, mHeight);
        vtrack = muxer.addTrack(vformat);

        mEncoder.start();
        muxer.start("rtmp://192.168.86.224/live/linh3");
    }

    int vtrack;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void release() {
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
        if (muxer != null) {
            muxer.stop();
//            muxer.rele
        }
    }
}
