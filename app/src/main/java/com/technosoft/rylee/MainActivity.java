package com.technosoft.rylee;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.technosoft.rylee.model.InputRequest;
import com.technosoft.rylee.model.InputResponse;
import com.technosoft.rylee.network.ApiClient;
import com.technosoft.rylee.network.ApiInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {

    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    final SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkPermission();
    }

    private void initViews() {
        txtSpeechInput = findViewById(R.id.txtSpeechInput);
        btnSpeak = findViewById(R.id.btnSpeak);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = textToSpeech.setLanguage(Locale.getDefault());
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {

                        }

                        @Override
                        public void onDone(String utteranceId) {

//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtSpeechInput.setText("");
                                        }
                                    });

//                                }
//                            }, 3000);
                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startTextToSpeechIntent() {
        final Intent mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                txtSpeechInput.setText("");
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

            }

            @Override
            public void onResults(Bundle bundle) {
                //getting all the matches
                ArrayList<String> matches = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //displaying the first match
                if (matches != null) {

                    String input = matches.get(0);

                    txtSpeechInput.setText(input);
                    callAPI(input);
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                //getting all the matches
                ArrayList<String> matches = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //displaying the first match
                if (matches != null) {
                    String input = matches.get(0);
                    txtSpeechInput.setText(input);
                }
            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        btnSpeak.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mSpeechRecognizer.stopListening();
                        if (txtSpeechInput.getText().toString().equals(getString(R.string.listening))) {
                            txtSpeechInput.setText("");
                        }
                        break;

                    case MotionEvent.ACTION_DOWN:
                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                        txtSpeechInput.setText(getString(R.string.listening));
                        break;
                }
                return false;
            }
        });
    }

    private void callAPI(String text) {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<InputResponse> call = apiService.postInput(new InputRequest(text));
        call.enqueue(new Callback<InputResponse>() {
            @Override
            public void onResponse(Call<InputResponse> call, retrofit2.Response<InputResponse> response) {
                if (response.body() != null) {
                    String result = response.body().response;
                    Log.i("TAG", "server response: " + result);

                    HashMap<String, String> myHashAlarm = new HashMap<String, String>();
                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");

                    int speechStatus = textToSpeech.speak(result, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
                    txtSpeechInput.setText(String.valueOf(txtSpeechInput.getText() + " \n\n" + result));
                    if (speechStatus == TextToSpeech.ERROR) {
                        Log.e("TTS", "Error in converting Text to Speech!");
                    }
                }
            }

            @Override
            public void onFailure(Call<InputResponse> call, Throwable t) {
                // Log error here since request failed
                Log.e("TAG", t.toString());
            }
        });
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQ_CODE_SPEECH_INPUT);
            } else {
                startTextToSpeechIntent();
            }
        } else {
            startTextToSpeechIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startTextToSpeechIntent();
                } else {
                    this.finish();
                }
                return;
            }
        }
    }

//    /**
//     * Showing google speech input dialog
//     */
//    private void promptSpeechInput() {
//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
//        try {
//            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
//        } catch (ActivityNotFoundException a) {
//            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    /**
//     * Receiving speech input
//     */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        switch (requestCode) {
//            case REQ_CODE_SPEECH_INPUT: {
//                if (resultCode == RESULT_OK && null != data) {
//
//                    ArrayList<String> result = data
//                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                    txtSpeechInput.setText(result.get(0));
//                }
//                break;
//            }
//
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
