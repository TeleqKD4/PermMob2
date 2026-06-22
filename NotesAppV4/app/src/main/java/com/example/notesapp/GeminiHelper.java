package com.example.notesapp;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiHelper {

    // GANTI dengan API key kamu dari https://aistudio.google.com/
    private static final String API_KEY = "MASUKKAN_API_KEY_KAMU_DISINI";
    private static final String API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    private final OkHttpClient client = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface OnResultListener {
        void onSuccess(String result);
        void onError(String error);
    }

    /**
     * Rapikan teks hasil dikte menggunakan Gemini AI
     * - Perbaiki ejaan & tanda baca
     * - Hilangkan kata filler (eh, hmm, eee, dll)
     * - Buat kalimat lebih terstruktur
     */
    public void rapikanTeks(String rawText, OnResultListener listener) {
        String prompt = "Kamu adalah asisten pencatatan. Rapikan teks berikut yang merupakan hasil dikte suara:\n\n"
            + "\"" + rawText + "\"\n\n"
            + "Instruksi:\n"
            + "1. Perbaiki ejaan dan tanda baca\n"
            + "2. Hilangkan kata filler seperti 'eh', 'hmm', 'eee', 'anu', 'jadi gitu'\n"
            + "3. Buat kalimat lebih terstruktur dan mudah dibaca\n"
            + "4. Pertahankan makna aslinya, jangan tambah informasi baru\n"
            + "5. Gunakan Bahasa Indonesia yang baik\n"
            + "6. Langsung tulis hasilnya saja tanpa penjelasan tambahan";

        callGemini(prompt, listener);
    }

    private void callGemini(String prompt, OnResultListener listener) {
        try {
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);

            JSONArray parts = new JSONArray();
            parts.put(textPart);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject body = new JSONObject();
            body.put("contents", contents);

            RequestBody requestBody = RequestBody.create(
                body.toString(),
                MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mainHandler.post(() -> listener.onError("Gagal konek: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String result = json
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");
                        mainHandler.post(() -> listener.onSuccess(result.trim()));
                    } catch (Exception e) {
                        mainHandler.post(() -> listener.onError("Gagal parse: " + responseBody));
                    }
                }
            });
        } catch (Exception e) {
            listener.onError("Error: " + e.getMessage());
        }
    }
}
