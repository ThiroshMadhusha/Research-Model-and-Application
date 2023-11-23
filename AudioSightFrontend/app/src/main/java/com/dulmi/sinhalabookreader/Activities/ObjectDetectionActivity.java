package com.dulmi.sinhalabookreader.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Toast;

import com.dulmi.sinhalabookreader.API.ApiServices;
import com.dulmi.sinhalabookreader.API.RetroServer;
import com.dulmi.sinhalabookreader.Adapters.ObjectDetectionAdapter;
import com.dulmi.sinhalabookreader.Content.AudioContents;
import com.dulmi.sinhalabookreader.Interfaces.DetectionClickEvent;
import com.dulmi.sinhalabookreader.Models.Audio;
import com.dulmi.sinhalabookreader.ResponseModels.ObjectDetectionResponse;
import com.dulmi.sinhalabookreader.databinding.ActivityObjectDetectionBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ObjectDetectionActivity extends AppCompatActivity {

    private ActivityObjectDetectionBinding binding;
    private String capturedImage;
    private File imageFile;
    private List<String>objectList;
    private ObjectDetectionAdapter adapter;

    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private AudioContents audioContents;
    private MediaPlayer mediaPlayer;
    private int currentAudioIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityObjectDetectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        capturedImage = getIntent().getStringExtra("capturedImage");
        imageFile = new File(capturedImage);

        audioContents = new AudioContents();

        if (imageFile.exists()) {

            uploadImage();

        } else {

            // Handle the case where the file doesn't exist

        }

        setDataToRecyclerView();
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

    private List<Integer>selectedAudios = new ArrayList<>();

    private void playAudios() {

        for (int i = 0; i < audioContents.objectAudios().size(); i++) {

            if (!objectList.isEmpty()) {

                for (String object : objectList) {

                    if (audioContents.objectAudios().get(i).getAudioContent().equals(object)) {

                        selectedAudios.add(audioContents.objectAudios().get(i).getAudioFileName());

                    }

                }

            }

        }

        mediaPlayer = MediaPlayer.create(this, selectedAudios.get(currentAudioIndex));
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNextAudio();
            }
        });

        // Start playing the first audio
        mediaPlayer.start();

    }

    private void playNextAudio() {
        currentAudioIndex++;
        if (currentAudioIndex < selectedAudios.size()) {
            // Release the current MediaPlayer instance
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            // Create a new MediaPlayer instance for the next audio
            mediaPlayer = MediaPlayer.create(this, selectedAudios.get(currentAudioIndex));
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playNextAudio();
                }
            });

            // Start playing the next audio
            mediaPlayer.start();
        } else {
            // All audio files have been played
            // You can handle this as needed (e.g., loop or stop)
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Release the MediaPlayer instance when the activity is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    private void setDataToRecyclerView() {

        objectList = new ArrayList<>();
        adapter = new ObjectDetectionAdapter(this, objectList, new DetectionClickEvent() {
            @Override
            public void onObjectClick(int position) {

                pronounceObject(objectList.get(position));

            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setAdapter(adapter);

    }

    private void pronounceObject(String object) {

        for (Audio data : audioContents.objectAudios()) {

            if (object.equals(data.getAudioContent())) {

                try {

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

    private void uploadImage() {

        binding.progressBar.setVisibility(View.VISIBLE);
        
        final File file = new File(capturedImage);

        RequestBody request = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", file.getName(), request);
        ApiServices webService = RetroServer.getRetrofitInstance().create(ApiServices.class);
        Call<ObjectDetectionResponse> uploadFile = webService.uploadImage(filePart);
        
        uploadFile.enqueue(new Callback<ObjectDetectionResponse>() {
            @Override
            public void onResponse(@NonNull Call<ObjectDetectionResponse> call, @NonNull Response<ObjectDetectionResponse> response) {

                if (response.isSuccessful()) {
                    
                    if (response.body() != null) {
                        
                        if (!response.body().getObjects_detected().isEmpty()) {

                            objectList.addAll(response.body().getObjects_detected());
                            adapter.notifyDataSetChanged();
                            //playAudios();
                            
                        } else {

                            Toast.makeText(ObjectDetectionActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                            
                        }
                        
                    } else {

                        Toast.makeText(ObjectDetectionActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                        
                    }
                    
                } else {

                    Toast.makeText(ObjectDetectionActivity.this, "Error - "+response.code(), Toast.LENGTH_SHORT).show();
                    
                }

                binding.progressBar.setVisibility(View.GONE);
                
            }

            @Override
            public void onFailure(@NonNull Call<ObjectDetectionResponse> call, @NonNull Throwable t) {

                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ObjectDetectionActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void saveAndConvertImage() {

        File directory = getExternalFilesDir(null);
        String fileName = System.currentTimeMillis()+"_compressed"+".jpg";
        imageFile = new File(directory, fileName);

        try {

            FileOutputStream fos = new FileOutputStream(imageFile);
            compressImage(new File(capturedImage)).compress(Bitmap.CompressFormat.JPEG, 50, fos); // 100 is the highest quality (0-100)
            fos.flush();
            fos.close();

        } catch (Exception e) {

            e.printStackTrace();
            System.out.println(e.getMessage());
            Toast.makeText(this, "Error - "+e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        System.out.println("New Image File : "+imageFile.getName());
        System.out.println("New Image File Path: "+imageFile.getAbsolutePath());

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
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    speechProcess();

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

                        playAudios();

                    } else if (spokenText.equals("ප්\u200Dරධාන මෙනුව")) {

                        startActivity(new Intent(ObjectDetectionActivity.this, HomeActivity.class));
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

}