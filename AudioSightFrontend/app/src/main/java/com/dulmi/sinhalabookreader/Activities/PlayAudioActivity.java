package com.dulmi.sinhalabookreader.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Base64;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.dulmi.sinhalabookreader.R;
import com.dulmi.sinhalabookreader.databinding.ActivityPlayAudioBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class PlayAudioActivity extends AppCompatActivity {

    private ActivityPlayAudioBinding binding;
    private String encodedAudio;
    private MediaPlayer mediaPlayer;

    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private File audioFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayAudioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        encodedAudio = getIntent().getStringExtra("encodedAudio");
        byte[] audioData = Base64.decode(encodedAudio, Base64.DEFAULT);

        mediaPlayer = new MediaPlayer();

        String audioPath = "Audio_" + System.currentTimeMillis() + ".mp3";

        audioFile = new File(getExternalFilesDir(null), audioPath);

        try {

            FileOutputStream fos = new FileOutputStream(audioFile);
            fos.write(audioData);
            fos.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        if (audioFile.exists()) {

            System.out.println("exists");
            System.out.println("File Name : "+audioFile.getName());
            System.out.println("File Path : "+audioFile.getPath());

            binding.fileNameText.setText(audioFile.getName());
            binding.filePathText.setText(audioFile.getPath());

            binding.playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    playAudio(audioFile);

                }
            });

            configSensor();

        }

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

                    if (spokenText.equals("කියවීම අරඹන්න")) {

                        playAudio(audioFile);

                    } else if (spokenText.equals("නතර කරන්න")) {

                        if (mediaPlayer.isPlaying()) {

                            mediaPlayer.stop();

                        }

                    } else if (spokenText.equals("ප්\u200Dරධාන මෙනුව")) {

                        Intent intent = new Intent(PlayAudioActivity.this, HomeActivity.class);
                        startActivity(intent);

                    } else if (spokenText.equals("අලුත් පිටුවක්")) {

                        Intent intent = new Intent(PlayAudioActivity.this, CameraActivity.class);
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

    private void playAudio(File audioFile) {

        if (mediaPlayer.isPlaying()) {

            binding.playButton.setImageResource(R.drawable.stop_button);
            mediaPlayer.stop();

        } else {

            playButtonProcess(audioFile);

        }

    }

    private void playButtonProcess(File audioFile) {

        try {

            binding.playButton.setImageResource(R.drawable.play_button);
            mediaPlayer.setDataSource(audioFile.getAbsolutePath()); //Write your location here
            mediaPlayer.prepare();
            binding.seekBar.setProgress(0);
            binding.seekBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.start();

        } catch(Exception e) {

            e.printStackTrace();

        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (mediaPlayer != null) {

                    binding.seekBar.setProgress(mediaPlayer.getCurrentPosition());

                }


                new Handler().postDelayed(this, 100);

            }
        });

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mediaPlayer != null && b) {

                    mediaPlayer.seekTo(i);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                binding.seekBar.setProgress(0);
                System.out.println("setOnCompletionListener");
                binding.playButton.setImageResource(R.drawable.play_button);
                // Update UI or perform any other actions after music ends

                MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.end_of_page);
                mPlayer.start();
            }
        });

    }

}