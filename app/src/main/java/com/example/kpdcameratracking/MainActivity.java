package com.example.kpdcameratracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.nio.ByteBuffer;
import java.util.Arrays;
import android.Manifest;



public class MainActivity extends AppCompatActivity {
    private static final int VIBRATER_TIMES = 100;
    CaptureRequest.Builder previewRequestBuilder;
    HandlerThread handlerThread = new HandlerThread("camera");
    Handler mCameraHandler;
    ImageReader mImageReader;
    public static CameraDevice mCameraDevice;
    private SurfaceHolder mHoderCamera;
    Vibrator vibrator;
    SurfaceView surfaceView;
    Button button;

    ImageView imageView = findViewById(R.id.imageView);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);
        surfaceView = findViewById(R.id.surfaceView);
        button = findViewById(R.id.open);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    open_camera2();
                }catch (CameraAccessException e){
                    e.printStackTrace();
                }
            }
        });

        mHoderCamera = surfaceView.getHolder();
        mHoderCamera.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }

    private final ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image = imageReader.acquireNextImage();
            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
            imageView.setImageBitmap(bitmap);
            image.close();
        }
    };

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            try {
                mCameraDevice = cameraDevice;
                previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mImageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 2);
                mImageReader.setOnImageAvailableListener(onImageAvailableListener, mCameraHandler);

                previewRequestBuilder.addTarget(surfaceView.getHolder().getSurface());
                previewRequestBuilder.addTarget(mImageReader.getSurface());
                cameraDevice.createCaptureSession(Arrays.asList(surfaceView.getHolder().getSurface(), mImageReader.getSurface()), mStateCallBack_session, mCameraHandler);
            }catch (CameraAccessException e){
                e.printStackTrace();
            }
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            if (cameraDevice != null){
                cameraDevice.close();
            }
        }
    };

    private CameraCaptureSession.StateCallback mStateCallBack_session = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            try {
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);

                CaptureRequest request = previewRequestBuilder.build();
                cameraCaptureSession.setRepeatingRequest(request, null, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

        }
    };
    private void initLooper(){
        handlerThread = new HandlerThread("CAMERA2");
        handlerThread.start();
        mCameraHandler = new Handler(handlerThread.getLooper());
    }

    public void open_camera2() throws CameraAccessException{
        vibrator.vibrate(VIBRATER_TIMES);
        surfaceView.setVisibility(View.VISIBLE);
        if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            System.out.println("ok");
        }else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},1);
        }
        Log.d("opencv","start open");
        CameraManager cameraManager = (CameraManager)this.getSystemService(this.CAMERA_SERVICE);
        CameraCharacteristics characteristics = null;
        characteristics = cameraManager.getCameraCharacteristics("0");
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        //mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

    }
}
