package com.dulmi.sinhalabookreader.Activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Toast;

import com.dulmi.sinhalabookreader.R;
import com.dulmi.sinhalabookreader.databinding.ActivityCameraBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private ActivityCameraBinding binding;
    private int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private Camera camera;
    private ImageCapture imageCapture;
    private String permission;
    ProcessCameraProvider provider;
    private String function;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;

    private final ActivityResultLauncher<String>activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {

            // Callback when permission request is handled.
            validatePermission(result);

        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // If not granted, request camera permission
            permission = "CAMERA";
            activityResultLauncher.launch(Manifest.permission.CAMERA);

        } else {

            // If granted, start the camera
            startCamera(cameraFacing);

        }

        function = getIntent().getStringExtra("function");

        // Disable flash button if not available
        if (camera != null) {

            if (!camera.getCameraInfo().hasFlashUnit()) {

                binding.flasherButton.setEnabled(false);

            }

        }

        // Handle flip camera button click
        binding.flipCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                flipCameraButtonProcess();

            }
        });

        // Handle capture image button click
        binding.captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                captureImageButtonProcess();

            }
        });

        // Handle flash button click
        binding.flasherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setFlashIcon(camera);

            }
        });

        configSensor();

    }

    private void configSensor() {

        // calling sensor service.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager == null) {

            Toast.makeText(this, "No sensorManager found in device.", Toast.LENGTH_SHORT).show();

        } else {

            // from sensor service we are
            // calling proximity sensor
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        }

        // handling the case if the proximity
        // sensor is not present in users device.
        if (proximitySensor == null) {

            Toast.makeText(this, "No proximity sensor found in device.", Toast.LENGTH_SHORT).show();

        } else {
            // registering our sensor with sensor manager.
            sensorManager.registerListener(proximitySensorEventListener,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    // calling the sensor event class to detect
    // the change in data when sensor starts working.
    SensorEventListener proximitySensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // method to check accuracy changed in sensor.
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // check if the sensor type is proximity sensor.
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] == 0) {
                    // here we are setting our status to our textview..
                    // if sensor event return 0 then object is closed
                    // to sensor else object is away from sensor.
                    speechProcess();

                } else {

                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(proximitySensorEventListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(proximitySensorEventListener);

    }

    private void speechProcess() {

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                isListening = true;
                //binding.speakingText.setTextColor(Color.RED);
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int i) {
                isListening = false;
                //binding.speakingText.setTextColor(Color.BLACK);
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (results != null && !results.isEmpty()) {

                    // Handle the recognized text as needed
                    String spokenText = results.get(0);
                    System.out.println(spokenText);

                    if (spokenText.equals("flasher එක on කරන්න")) {

                        setFlashIcon(camera);

                    } else if (spokenText.equals("කැමරාව හරවන්න")) {

                        flipCameraButtonProcess();

                    } else if (spokenText.equals("පොතක් කියවමු")) {

                        Intent intent = new Intent(CameraActivity.this, CameraActivity.class);
                        intent.putExtra("function", "OCR");
                        startActivity(intent);

                    } else if (spokenText.equals("පින්තූරයක් ගනිමු")) {

                        captureImageButtonProcess();

                    } else if (spokenText.equals("ප්\u200Dරධාන මෙනුව")) {

                        startActivity(new Intent(CameraActivity.this, HomeActivity.class));
                        finishAffinity();

                    }

                }

                isListening = false;
                //binding.speakingText.setTextColor(Color.BLACK);
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });

        // Start listening for speech when the app is launched
        startListening();

    }

    private void startListening() {
        if (!isListening) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            // intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US"); // Set English as one of the recognized languages
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "si-LK"); // Set Sinhala as one of the recognized languages // Set the language to Sinhala (Sri Lanka)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            //intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500); // Adjust as needed
            //intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500); // Adjust as needed
            speechRecognizer.startListening(intent);
        }
    }

    private void setFlashIcon(Camera camera) {

        // Toggle flashlight if available
        if (camera.getCameraInfo().hasFlashUnit()) {

            // Check if the torch (flashlight) is currently off (TorchState.VALUE_OFF is 0)
            if (camera.getCameraInfo().getTorchState().getValue() == 0) {

                // If torch is off, enable it by setting it to true
                camera.getCameraControl().enableTorch(true);

                // Update the flashlight button's image resource to represent it as "off"
                binding.flasherButton.setImageResource(R.drawable.flasher_off);

            } else {

                // If torch is already on, disable it by setting it to false
                camera.getCameraControl().enableTorch(false);

                // Update the flashlight button's image resource to represent it as "on"
                binding.flasherButton.setImageResource(R.drawable.flasher_on);

            }

        } else {

            // Show a message if flashlight is not available
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(CameraActivity.this, "Flash is not Available!", Toast.LENGTH_SHORT).show();

                }
            });

        }

    }

    private void captureImageButtonProcess() {

        // Check for write external storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // If not granted, request write external storage permission
            permission = "WRITE_EXTERNAL_STORAGE";
            activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        } else {

            // If granted, take a picture
            takePicture(imageCapture);

        }

    }

    private void validatePermission(Boolean result) {

        if (permission.equals("CAMERA")) {

            if (result) {

                // Start the camera if camera permission is granted
                startCamera(cameraFacing);

            } else {

                // Show a message if camera permission is not granted
                Toast.makeText(this, "Camera Permission not Granted!", Toast.LENGTH_SHORT).show();

            }

        } else if (permission.equals("WRITE_EXTERNAL_STORAGE")) {

            if (result) {

                // Take a picture if write external storage permission is granted
                takePicture(imageCapture);

            } else {

                // Show a message if write external storage permission is not granted
                Toast.makeText(this, "WRITE_EXTERNAL_STORAGE Permission not Granted!", Toast.LENGTH_SHORT).show();

            }

        }

    }

    // Capture an image and save it
    private void takePicture(ImageCapture imageCapture) {

        // Create a File object to represent the destination path for saving the captured image
        final File file = new File(getExternalFilesDir(null), System.currentTimeMillis()+".jpg");

        // Create OutputFileOptions to specify the file where the captured image will be saved
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

        // Use the ImageCapture instance to capture an image and save it to the specified file
        // This operation is performed on a separate thread from a cached thread pool
        imageCapture.takePicture(outputFileOptions, Executors.newCachedThreadPool(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                if (file.exists()) {

                    // Display a message when the image is saved
                    System.out.println("File Name : "+file.getName());
                    System.out.println("Path : "+file.getAbsolutePath());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(CameraActivity.this, "Image Saved at "+file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

                        }
                    });

                    // Close the camera after capturing the image
                    //provider.unbindAll();

                    if (function.equals("ObjectDetection")) {

                        Intent intent = new Intent(CameraActivity.this, PreviewImageActivity.class);
                        intent.putExtra("capturedImage", file.getAbsolutePath());
                        intent.putExtra("function", function);
                        startActivity(intent);
                        finish();

                    } else if (function.equals("Captioning")) {

                        Intent intent = new Intent(CameraActivity.this, PreviewImageActivity.class);
                        intent.putExtra("capturedImage", file.getAbsolutePath());
                        intent.putExtra("function", function);
                        startActivity(intent);
                        finish();

                    } else if (function.equals("OCR")) {

                        Intent intent = new Intent(CameraActivity.this, PreviewImageActivity.class);
                        intent.putExtra("capturedImage", file.getAbsolutePath());
                        intent.putExtra("function", function);
                        startActivity(intent);
                        finish();

                    }

                }

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {

                // Handle error when capturing an image
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(CameraActivity.this, "Error - "+exception.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

                startCamera(cameraFacing);

            }
        });


    }

    // Start the camera with specified camera facing
    private void startCamera(int cameraFacing) {

        // Calculate the aspect ratio based on the width and height of the camera preview view
        int aspectRatio = aspectRatio(binding.cameraPreviewView.getWidth(), binding.cameraPreviewView.getHeight());

        // Get a ListenableFuture of ProcessCameraProvider using the application context
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(getApplicationContext());

        listenableFuture.addListener(new Runnable() {
            @Override
            public void run() {

                // Configure the camera
                cameraConfig(aspectRatio, listenableFuture, cameraFacing);

            }
        }, ContextCompat.getMainExecutor(this));


    }

    // Configure the camera
    private void cameraConfig(int aspectRatio, ListenableFuture<ProcessCameraProvider> listenableFuture, int cameraFacing) {

        try {

            // Get an instance of ProcessCameraProvider from the ListenableFuture
            provider = (ProcessCameraProvider) listenableFuture.get();

            // Create a Preview use case with a builder and set the target aspect ratio for the camera preview
            Preview preview = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();

            // Create an ImageCapture use case with a builder
            // Set capture mode to minimize latency and the target rotation based on device display rotation
            imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();

            // Create a CameraSelector to specify the camera lens facing direction (front or back)
            CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(cameraFacing).build();

            // Unbind any existing use cases from the camera provider to prepare for rebinding
            provider.unbindAll();

            // Bind the CameraSelector, Preview, and ImageCapture use cases to the camera provider
            // This step links the camera use cases to the camera lifecycle of the current activity
            camera = provider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            // Set the surface provider for the preview, indicating where the camera preview should be displayed
            preview.setSurfaceProvider(binding.cameraPreviewView.getSurfaceProvider());

        } catch (Exception e) {

            // Handle camera configuration errors
            Toast.makeText(CameraActivity.this, "Error - " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();

        }

    }

    // Calculate and return the aspect ratio
    private int aspectRatio(int width, int height) {

        // Calculate the preview ratio by dividing the maximum dimension (width or height)
        // by the minimum dimension (width or height) of the camera preview view
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);

        // Check if the calculated preview ratio is closer to 4:3 or 16:9 aspect ratio
        if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {

            // If closer to 4:3, return the constant representing 4:3 aspect ratio
            return AspectRatio.RATIO_4_3;

        } else {

            // If closer to 16:9, return the constant representing 16:9 aspect ratio
            return AspectRatio.RATIO_16_9;

        }

    }

    // Toggle between front and back camera
    private void flipCameraButtonProcess() {

        // Check if the current camera facing is set to the back camera
        if (cameraFacing == CameraSelector.LENS_FACING_BACK) {

            // If it is, switch to the front camera
            cameraFacing = CameraSelector.LENS_FACING_FRONT;

        } else {

            // If it is not (i.e., currently using the front camera), switch to the back camera
            cameraFacing = CameraSelector.LENS_FACING_BACK;

        }

        // Restart the camera with the new camera facing
        startCamera(cameraFacing);

    }

}