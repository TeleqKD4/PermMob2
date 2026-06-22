package com.example.notesapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class EditorActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_AUDIO = 101;

    private EditText etTitle, etContent;
    private NotesDatabaseHelper dbHelper;
    private Note currentNote;
    private boolean isEditing = false;
    private int selectedColorIndex = 0;
    private View[] colorDots;

    // Dikte & AI
    private SpeechHelper speechHelper;
    private GeminiHelper geminiHelper;
    private TextView btnDictate;
    private View aiLoadingView;
    private boolean isDictating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        dbHelper = new NotesDatabaseHelper(this);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnDictate = findViewById(R.id.btnDictate);
        aiLoadingView = findViewById(R.id.aiLoadingView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        speechHelper = new SpeechHelper(this);
        geminiHelper = new GeminiHelper();

        setupColorPicker();
        setupDictateButton();

        long noteId = getIntent().getLongExtra("note_id", -1);
        if (noteId != -1) {
            isEditing = true;
            currentNote = dbHelper.getNoteById(noteId);
            etTitle.setText(currentNote.getTitle());
            etContent.setText(currentNote.getContent());
            selectedColorIndex = currentNote.getColorIndex();
            setTitle("Edit Catatan");
            updateEditorBackground();
            highlightSelectedDot();
        } else {
            setTitle("Catatan Baru");
            updateEditorBackground();
        }
    }

    private void setupDictateButton() {
        btnDictate.setOnClickListener(v -> {
            if (isDictating) {
                stopDictation();
            } else {
                startDictation();
            }
        });
    }

    private void startDictation() {
        // Cek permission mikrofon
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_AUDIO);
            return;
        }

        if (!speechHelper.isAvailable()) {
            Toast.makeText(this, "Speech recognition tidak tersedia di HP ini", Toast.LENGTH_SHORT).show();
            return;
        }

        isDictating = true;
        btnDictate.setText("⏹️ Stop");
        btnDictate.setBackgroundResource(R.drawable.bg_btn_stop);

        speechHelper.startListening(new SpeechHelper.SpeechListener() {
            @Override
            public void onStartListening() {
                btnDictate.setText("🎙️ Sedang mendengar...");
                Toast.makeText(EditorActivity.this, "🎙️ Mulai bicara!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPartialResult(String partial) {
                // Tampilkan preview teks sementara
                etContent.setHint("🎙️ " + partial + "...");
            }

            @Override
            public void onResult(String text) {
                etContent.setHint("Tulis catatanmu di sini... ✍️");
                processWithAI(text);
            }

            @Override
            public void onError(String error) {
                isDictating = false;
                resetDictateButton();
                Toast.makeText(EditorActivity.this, error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEndListening() {
                isDictating = false;
                resetDictateButton();
            }
        });
    }

    private void stopDictation() {
        speechHelper.stopListening();
        isDictating = false;
        resetDictateButton();
    }

    private void processWithAI(String rawText) {
        // Tampilkan loading
        aiLoadingView.setVisibility(View.VISIBLE);
        btnDictate.setEnabled(false);
        btnDictate.setText("✨ AI merapikan...");

        geminiHelper.rapikanTeks(rawText, new GeminiHelper.OnResultListener() {
            @Override
            public void onSuccess(String result) {
                aiLoadingView.setVisibility(View.GONE);
                btnDictate.setEnabled(true);
                resetDictateButton();

                // Tambahkan ke konten yang sudah ada
                String existing = etContent.getText().toString();
                if (existing.isEmpty()) {
                    etContent.setText(result);
                } else {
                    etContent.setText(existing + "\n\n" + result);
                }
                etContent.setSelection(etContent.getText().length());
                Toast.makeText(EditorActivity.this, "✅ Teks berhasil dirapikan AI!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                aiLoadingView.setVisibility(View.GONE);
                btnDictate.setEnabled(true);
                resetDictateButton();

                // Kalau AI gagal, tetap pakai teks mentah
                String existing = etContent.getText().toString();
                if (existing.isEmpty()) {
                    etContent.setText(rawText);
                } else {
                    etContent.setText(existing + "\n\n" + rawText);
                }
                Toast.makeText(EditorActivity.this,
                    "AI tidak tersedia, teks mentah ditambahkan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetDictateButton() {
        btnDictate.setText("🎙️ Dikte");
        btnDictate.setBackgroundResource(R.drawable.bg_btn_dictate);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDictation();
            } else {
                Toast.makeText(this, "Izin mikrofon diperlukan untuk fitur dikte", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupColorPicker() {
        colorDots = new View[]{
            findViewById(R.id.dot0), findViewById(R.id.dot1),
            findViewById(R.id.dot2), findViewById(R.id.dot3),
            findViewById(R.id.dot4), findViewById(R.id.dot5)
        };
        for (int i = 0; i < colorDots.length; i++) {
            final int idx = i;
            colorDots[i].setOnClickListener(v -> {
                selectedColorIndex = idx;
                updateEditorBackground();
                highlightSelectedDot();
            });
        }
        highlightSelectedDot();
    }

    private void updateEditorBackground() {
        int color = NoteAdapter.CARD_COLORS[selectedColorIndex % NoteAdapter.CARD_COLORS.length];
        findViewById(R.id.editorRoot).setBackgroundColor(color);
    }

    private void highlightSelectedDot() {
        for (int i = 0; i < colorDots.length; i++) {
            colorDots[i].setAlpha(i == selectedColorIndex ? 1.0f : 0.4f);
            colorDots[i].setScaleX(i == selectedColorIndex ? 1.3f : 1.0f);
            colorDots[i].setScaleY(i == selectedColorIndex ? 1.3f : 1.0f);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        if (item.getItemId() == R.id.action_save) { saveNote(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        if (title.isEmpty()) {
            etTitle.setError("Judul tidak boleh kosong");
            etTitle.requestFocus();
            return;
        }
        if (isEditing) {
            currentNote.setTitle(title);
            currentNote.setContent(content);
            currentNote.setColorIndex(selectedColorIndex);
            dbHelper.updateNote(currentNote);
            Toast.makeText(this, "✅ Catatan diperbarui!", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.insertNote(new Note(title, content, selectedColorIndex));
            Toast.makeText(this, "✅ Catatan disimpan!", Toast.LENGTH_SHORT).show();
        }
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechHelper.destroy();
    }
}
