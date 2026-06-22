package com.example.notesapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;
import java.util.Locale;

public class SpeechHelper {

    public interface SpeechListener {
        void onStartListening();
        void onPartialResult(String partial);  // teks sementara saat bicara
        void onResult(String text);             // teks final setelah selesai
        void onError(String error);
        void onEndListening();
    }

    private SpeechRecognizer recognizer;
    private final Context context;
    private SpeechListener listener;
    private boolean isListening = false;

    public SpeechHelper(Context context) {
        this.context = context;
    }

    public boolean isAvailable() {
        return SpeechRecognizer.isRecognitionAvailable(context);
    }

    public boolean isListening() {
        return isListening;
    }

    public void startListening(SpeechListener listener) {
        this.listener = listener;

        if (recognizer != null) {
            recognizer.destroy();
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(context);
        recognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                isListening = true;
                if (listener != null) listener.onStartListening();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> results = partialResults
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (results != null && !results.isEmpty()) {
                    if (listener != null) listener.onPartialResult(results.get(0));
                }
            }

            @Override
            public void onResults(Bundle results) {
                isListening = false;
                ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    if (listener != null) listener.onResult(matches.get(0));
                }
                if (listener != null) listener.onEndListening();
            }

            @Override
            public void onError(int error) {
                isListening = false;
                String msg;
                switch (error) {
                    case SpeechRecognizer.ERROR_NO_MATCH: msg = "Suara tidak dikenali, coba lagi"; break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: msg = "Tidak ada suara terdeteksi"; break;
                    case SpeechRecognizer.ERROR_NETWORK: msg = "Perlu koneksi internet"; break;
                    case SpeechRecognizer.ERROR_AUDIO: msg = "Error mikrofon"; break;
                    default: msg = "Error dikte (" + error + ")";
                }
                if (listener != null) listener.onError(msg);
                if (listener != null) listener.onEndListening();
            }

            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID"); // Bahasa Indonesia
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "id-ID");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        recognizer.startListening(intent);
    }

    public void stopListening() {
        if (recognizer != null) {
            recognizer.stopListening();
            isListening = false;
        }
    }

    public void destroy() {
        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }
    }
}
