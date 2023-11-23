package com.dulmi.sinhalabookreader.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

import com.dulmi.sinhalabookreader.API.ApiServices;
import com.dulmi.sinhalabookreader.API.RetroServer;
import com.dulmi.sinhalabookreader.Content.AudioContents;
import com.dulmi.sinhalabookreader.Models.Audio;
import com.dulmi.sinhalabookreader.ResponseModels.CaptionResponse;
import com.dulmi.sinhalabookreader.ResponseModels.OcrResponse;
import com.dulmi.sinhalabookreader.databinding.ActivityPreviewImageBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreviewImageActivity extends AppCompatActivity {

    private ActivityPreviewImageBinding binding;
    private String capturedImage;
    Bitmap compressedBitmap;
    private String where;
    private TextToSpeech textToSpeech;
    private AudioContents audioContents;

    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private SensorManager sensorManager;
    private Sensor proximitySensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreviewImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        capturedImage = getIntent().getStringExtra("capturedImage");
        where = getIntent().getStringExtra("function");
        binding.previewImage.setImageBitmap(compressImage(new File(capturedImage)));

        audioContents = new AudioContents();

        // Create a background thread to load and compress the image
        Thread imageThread = new Thread(() -> {

            compressedBitmap = compressImage(new File(capturedImage));

            // Update the UI with the compressed image using a handler
            Handler uiHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    binding.previewImage.setImageBitmap(compressedBitmap);
                }
            };
            uiHandler.sendEmptyMessage(0);
        });

        imageThread.start();

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                if (i != TextToSpeech.ERROR) {

                    textToSpeech.setLanguage(Locale.ENGLISH);

                }

            }
        });

        binding.checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkButtonProcess();

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

                    if (spokenText.equals("කියවන්න")) {

                        checkButtonProcess();

                    } else if (spokenText.equals("ප්\u200Dරධාන මෙනුව")) {

                        Intent intent = new Intent(PreviewImageActivity.this, HomeActivity.class);
                        startActivity(intent);

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

    private void checkButtonProcess() {

        if (where.equals("ObjectDetection")) {

            Intent intent = new Intent(this, ObjectDetectionActivity.class);
            intent.putExtra("capturedImage", capturedImage);
            startActivity(intent);

        } else if (where.equals("Captioning")) {

            getImageCaption();

        } else {

            getOCR(String.valueOf(getRandomValue(setArray())), String.valueOf(getRandomValue(setArray())));

        }

    }

    private int [] setArray() {

        int[] numbers = new int[100]; // Create an integer array with a size of 100.

        for (int i = 0; i < 100; i++) {
            numbers[i] = i + 1; // Fill the array with values from 1 to 100.
        }

        return numbers;

    }

    // Method to get a random value from the array
    public int getRandomValue(int[] arr) {

        Random rand = new Random();
        int randomIndex = rand.nextInt(arr.length); // Generate a random index within the array length.
        return arr[randomIndex]; // Return the value at the random index.

    }

    private void getOCR(String userID, String bookID) {

        System.out.println(userID);
        System.out.println(bookID);

        binding.progressBar.setVisibility(View.VISIBLE);
        final File file = reducedImageSize(new File(capturedImage));
        System.out.println("Image File Name : "+file.getName());
        Toast.makeText(this, "Image File Name : "+file.getName(), Toast.LENGTH_SHORT).show();

        if (file.exists()) {

            RequestBody requestFile = RequestBody.create(MultipartBody.FORM, file);

            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part bodyFile = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            // add another part within the multipart request
            RequestBody bodyUserID = RequestBody.create(MultipartBody.FORM, userID);
            RequestBody bodyBookID = RequestBody.create(MultipartBody.FORM, bookID);

            ApiServices apiServices = RetroServer.getRetrofitInstance().create(ApiServices.class);
            Call<OcrResponse>call = apiServices.getOCR(bodyFile, bodyUserID, bodyBookID);

            call.enqueue(new Callback<OcrResponse>() {
                @Override
                public void onResponse(@NonNull Call<OcrResponse> call, @NonNull Response<OcrResponse> response) {

                    if (response.isSuccessful()) {

                        if (response.body() != null) {

                            Intent intent = new Intent(getApplicationContext(), PlayAudioActivity.class);
                            intent.putExtra("encodedAudio", response.body().getEncodedAudio());
                            startActivity(intent);
                            finish();

                        } else {

                            Toast.makeText(PreviewImageActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();

                        }

                    } else {

                        System.out.println("Path File : "+file.getAbsolutePath());
                        Toast.makeText(PreviewImageActivity.this, "Error - "+response.code(), Toast.LENGTH_SHORT).show();

                    }

                    binding.progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onFailure(@NonNull Call<OcrResponse> call, @NonNull Throwable t) {

                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(PreviewImageActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        } else {

            Toast.makeText(PreviewImageActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();

        }


    }

    private void getImageCaption() {

        binding.progressBar.setVisibility(View.VISIBLE);
        final File file = reducedImageSize(new File(capturedImage));

        RequestBody request = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", file.getName(), request);
        ApiServices webService = RetroServer.getRetrofitInstance().create(ApiServices.class);
        Call<CaptionResponse>call = webService.getCaptioning(filePart);

        call.enqueue(new Callback<CaptionResponse>() {
            @Override
            public void onResponse(@NonNull Call<CaptionResponse> call, @NonNull Response<CaptionResponse> response) {

                if (response.isSuccessful()) {

                    if (response.body() != null) {

                        System.out.println(response.body().getCaption());
                        pronounceCaption(response.body().getCaption());

                    } else {

                        Toast.makeText(PreviewImageActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();

                    }

                } else {

                    Toast.makeText(PreviewImageActivity.this, "Error - "+response.code(), Toast.LENGTH_SHORT).show();

                }

                binding.progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NonNull Call<CaptionResponse> call, @NonNull Throwable t) {

                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(PreviewImageActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void pronounceCaption(String caption) {

        for (Audio data : audioContents.captionAudios()) {

            // Check if the caption matches the audio content.
            if (caption.equals(data.getAudioContent())) {

                try {

                    // Create a MediaPlayer to play the audio using the resource identifier from data.
                    MediaPlayer mp = MediaPlayer.create(this, data.getAudioFileName());
                    // Start playing the audio.
                    mp.start();

                } catch (Exception e) {

                    // Handle any exceptions that occur during audio playback and display a toast message with the error.
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    private File reducedImageSize(File imageFile) {

        // Calculate the desired width and height for the compressed image
        int desiredWidth = 800; // Set your desired width
        int desiredHeight = 600; // Set your desired height

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        int sampleSize = 1;
        if (imageWidth > desiredWidth || imageHeight > desiredHeight) {
            final int halfWidth = imageWidth / 2;
            final int halfHeight = imageHeight / 2;

            while ((halfWidth / sampleSize) >= desiredWidth
                    && (halfHeight / sampleSize) >= desiredHeight) {
                sampleSize *= 2;
            }
        }

        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;

        // Decode the image with the specified sample size
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        // Create a new file for the compressed image
        File compressedImageFile = new File(getExternalFilesDir(null), System.currentTimeMillis()+"_image.jpg");

        try {
            FileOutputStream out = new FileOutputStream(compressedImageFile);

            // Compress the image and save it to the file
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

            // Clean up resources
            out.flush();
            out.close();
            bitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Now, you can use 'compressedImageFile' to access the compressed image.
        return compressedImageFile;

    }

    private Bitmap compressImage(File file) {

        // Load the image from the file
        Bitmap originalBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        // Compress the image by reducing its quality
        int quality = 50; // Adjust the quality value as needed (0-100)
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);

        // Create a new Bitmap from the compressed data
        byte[] compressedByteArray = byteArrayOutputStream.toByteArray();

        return BitmapFactory.decodeByteArray(compressedByteArray, 0, compressedByteArray.length);

    }

}