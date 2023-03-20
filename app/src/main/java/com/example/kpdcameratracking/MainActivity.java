package com.example.kpdcameratracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageView;

import java.nio.ByteBuffer;





public class MainActivity extends AppCompatActivity {

    CaptureRequest.Builder previewRequestBuilder;
    HandlerThread handlerThread = new HandlerThread("camera");
    Handler cameraHandler;
    ImageReader imageReader;
    public static CameraDevice mCameraDevice;
    private SurfaceHolder surfaceHolder;
    Vibrator vibrator;
    SurfaceView surfaceView;
    Button button;

    ImageView imageView = findViewById(R.id.imageView);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            mCameraDevice = cameraDevice;

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

        }
    };
}
