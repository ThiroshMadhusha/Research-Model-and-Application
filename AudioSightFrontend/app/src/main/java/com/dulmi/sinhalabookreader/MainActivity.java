package com.dulmi.sinhalabookreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import com.dulmi.sinhalabookreader.Activities.HomeActivity;
import com.dulmi.sinhalabookreader.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check and request microphone permission if not granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);

        } else {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finishAffinity();

                }
            }, 3000);

        }
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
                    String spokenText = results.get(0);
                    // Handle the recognized text as needed
                    System.out.println(spokenText);
                    String s = "හෙලෝ කොහොමද";

                    if (spokenText.equals(s)) {

                        System.out.println("Working");

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
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finishAffinity();

                    }
                }, 3000);

            } else {
                // Permissions not granted, show a toast or other UI message
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }

    }

}