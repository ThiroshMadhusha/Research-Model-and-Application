package com.dulmi.sinhalabookreader.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.dulmi.sinhalabookreader.R;
import com.dulmi.sinhalabookreader.databinding.ActivityHomeBinding;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private SensorManager sensorManager;
    private Sensor proximitySensor;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check and request microphone permission if not granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);

        }

        mediaPlayer = MediaPlayer.create(this, R.raw.start_msg);
        mediaPlayer.start();

        binding.objectDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }

                Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
                intent.putExtra("function", "ObjectDetection");
                startActivity(intent);

            }
        });

        binding.captioning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }

                Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
                intent.putExtra("function", "Captioning");
                startActivity(intent);

            }
        });

        binding.bookReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }

                Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
                intent.putExtra("function", "OCR");
                startActivity(intent);

            }
        });

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

                    if (spokenText.equals("භාණ්ඩ හඳුනාගනිමු")) {

                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }

                        Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
                        intent.putExtra("function", "ObjectDetection");
                        startActivity(intent);

                    } else if (spokenText.equals("පින්තූර බලමු")) {

                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }

                        Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
                        intent.putExtra("function", "Captioning");
                        startActivity(intent);

                    } else if (spokenText.equals("පොතක් කියවමු")) {

                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }

                        Intent intent = new Intent(HomeActivity.this, CameraActivity.class);
                        intent.putExtra("function", "OCR");
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            // If request is cancelled, the grantResults arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, proceed with file deletion
                Toast.makeText(this, "All Permissions are Granted!", Toast.LENGTH_SHORT).show();

            } else {
                // Permissions not granted, show a toast or other UI message
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }

    }

}