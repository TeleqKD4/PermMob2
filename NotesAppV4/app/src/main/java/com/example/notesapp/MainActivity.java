package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private NotesDatabaseHelper dbHelper;
    private EditText etSearch;
    private TextView tvEmpty;
    private View rootLayout;

    public static final int REQUEST_CODE_ADD = 1;
    public static final int REQUEST_CODE_EDIT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new NotesDatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        etSearch = findViewById(R.id.etSearch);
        tvEmpty = findViewById(R.id.tvEmpty);
        rootLayout = findViewById(R.id.rootLayout);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> openEditor(null));

        setupRecyclerView();
        setupSearch();
        loadNotes();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterNotes(String query) {
        List<Note> filtered = query.isEmpty()
                ? dbHelper.getAllNotes()
                : dbHelper.searchNotes(query);
        adapter.updateList(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadNotes() {
        List<Note> notes = dbHelper.getAllNotes();
        if (adapter == null) {
            adapter = new NoteAdapter(notes, new NoteAdapter.OnNoteClickListener() {
                @Override
                public void onNoteClick(Note note) { openEditor(note); }

                @Override
                public void onNoteDelete(Note note, int position) {
                    confirmDelete(note, position);
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(notes);
        }
        tvEmpty.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
        findViewById(R.id.tvEmptyText).setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmDelete(Note note, int position) {
        new AlertDialog.Builder(this)
            .setTitle("🗑️ Hapus Catatan?")
            .setMessage("\"" + note.getTitle() + "\" akan dihapus permanen.")
            .setPositiveButton("Hapus", (d, w) -> {
                dbHelper.deleteNote(note.getId());
                adapter.removeItem(position);
                if (adapter.getItemCount() == 0) tvEmpty.setVisibility(View.VISIBLE);

                // Undo snackbar
                Snackbar.make(rootLayout, "Catatan dihapus", Snackbar.LENGTH_LONG)
                    .setAction("Undo", v -> {
                        dbHelper.insertNote(note);
                        loadNotes();
                    })
                    .show();
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void openEditor(Note note) {
        Intent intent = new Intent(this, EditorActivity.class);
        if (note != null) intent.putExtra("note_id", note.getId());
        startActivityForResult(intent, note == null ? REQUEST_CODE_ADD : REQUEST_CODE_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) loadNotes();
    }
}
