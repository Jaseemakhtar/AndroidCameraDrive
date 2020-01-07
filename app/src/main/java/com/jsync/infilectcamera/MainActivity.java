package com.jsync.infilectcamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.jsync.infilectcamera.imageGallery.GalleryActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextureView cameraView;
    private Button btnCapture;
    private ImageView btnGallery;
    private ImageView btnSwitchCamera;
    private CameraDevice cameraDevice;
    private String[] cameraIds;
    private String cameraId;
    private CameraManager cameraManager;
    private CameraCharacteristics cameraCharacteristics;
    private Size imageDimens;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureReqBuilder;
    private Handler backgroundHandler;
    private HandlerThread handlerThread;
    private final int REQUEST_CAMERA_PERMISSION = 945;
    private final int REQUEST_STORAGE_PERMISSION = 345;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    public static final File infilectDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/InfilectPics") ;

    //
    // Surface Texture Listener
    //
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                Log.i("CamLog", "Error opening camera");
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };


    //
    // Camera state Callback
    //
    private CameraDevice.StateCallback cameraStateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            try {
                startPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }


    };


    //
    // Camera Capture Callback
    //
    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);

        cameraView = findViewById(R.id.textureView);
        btnCapture = findViewById(R.id.btnCapture);
        btnGallery = findViewById(R.id.btnGallery);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);

        cameraView.setSurfaceTextureListener(surfaceTextureListener);
        btnCapture.setOnClickListener(MainActivity.this);
        btnGallery.setOnClickListener(MainActivity.this);
        btnSwitchCamera.setOnClickListener(MainActivity.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCapture:
                saveImage();
                break;

            case R.id.btnGallery:
                openImageGallery();
                break;

            case R.id.btnSwitchCamera:
                Toast.makeText(MainActivity.this, "Will add this feature soon!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void openImageGallery() {
        Intent gallery = new Intent(MainActivity.this, GalleryActivity.class);
        startActivity(gallery);
    }

    private void openCamera() throws CameraAccessException {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraIds = cameraManager.getCameraIdList();
        cameraId = cameraIds[0];
        cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        imageDimens = map.getOutputSizes(SurfaceTexture.class)[0];

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            return;
        }


        cameraManager.openCamera(cameraId, cameraStateCallBack, null);

    }

    private void startPreview() throws CameraAccessException {
        SurfaceTexture surfaceTexture = cameraView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(imageDimens.getWidth(), imageDimens.getHeight());
        Surface surface = new Surface(surfaceTexture);

        captureReqBuilder = cameraDevice.createCaptureRequest(cameraDevice.TEMPLATE_PREVIEW);
        captureReqBuilder.set(CaptureRequest.JPEG_QUALITY, (byte) 60);
        captureReqBuilder.addTarget(surface);
        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                if(cameraDevice == null)
                    return;

                cameraCaptureSession = session;
                try {
                    updatePreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Toast.makeText(MainActivity.this, "Configuration Changed", Toast.LENGTH_SHORT).show();
            }
        }, null);
    }

    private void updatePreview() throws CameraAccessException {
        if(cameraDevice == null){
            return;
        }

        captureReqBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        captureReqBuilder.set(CaptureRequest.JPEG_QUALITY, (byte) 60);
        cameraCaptureSession.setRepeatingRequest(captureReqBuilder.build(), null, backgroundHandler);
    }

    private void closeCamera(){
        if(cameraDevice != null){
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void saveImage(){
        if(null == cameraDevice)
            return;

        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(cameraView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            String timeStamp = new SimpleDateFormat("MM-dd-yyyy_HH_mm_ss").format(new Date());
            String imageFileName = timeStamp + ".jpg";


            if(!infilectDirectory.exists()){
                infilectDirectory.mkdirs();
            }
            final File file = new File(infilectDirectory, imageFileName);

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                        //output.flush();
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, backgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(MainActivity.this, "Saved: " + file, Toast.LENGTH_SHORT).show();
                    //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                    MediaScannerConnection.scanFile(MainActivity.this, new String[]{file.getAbsolutePath()}, null, null);
                    try {
                        startPreview();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void startBackgroundThread() {
        handlerThread = new HandlerThread("Camera Background");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
    }
    protected void stopBackgroundThread() {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();

        if(cameraView.isAvailable()){
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }else{
            cameraView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CAMERA_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                Toast.makeText(MainActivity.this, "Camera Permission is required to use this app", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
