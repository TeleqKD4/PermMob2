package com.example.notesapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class NotesDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NOTES = "notes";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";
    public static final String COL_COLOR = "color_index";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";

    public NotesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NOTES + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_TITLE + " TEXT NOT NULL, " +
            COL_CONTENT + " TEXT, " +
            COL_COLOR + " INTEGER DEFAULT 0, " +
            COL_CREATED_AT + " INTEGER, " +
            COL_UPDATED_AT + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    public long insertNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_TITLE, note.getTitle());
        v.put(COL_CONTENT, note.getContent());
        v.put(COL_COLOR, note.getColorIndex());
        v.put(COL_CREATED_AT, System.currentTimeMillis());
        v.put(COL_UPDATED_AT, System.currentTimeMillis());
        long id = db.insert(TABLE_NOTES, null, v);
        db.close();
        return id;
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NOTES, null, null, null, null, null, COL_UPDATED_AT + " DESC");
        if (c.moveToFirst()) {
            do { notes.add(cursorToNote(c)); } while (c.moveToNext());
        }
        c.close(); db.close();
        return notes;
    }

    public Note getNoteById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NOTES, null, COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        Note note = null;
        if (c.moveToFirst()) note = cursorToNote(c);
        c.close(); db.close();
        return note;
    }

    public int updateNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_TITLE, note.getTitle());
        v.put(COL_CONTENT, note.getContent());
        v.put(COL_COLOR, note.getColorIndex());
        v.put(COL_UPDATED_AT, System.currentTimeMillis());
        int rows = db.update(TABLE_NOTES, v, COL_ID + "=?", new String[]{String.valueOf(note.getId())});
        db.close();
        return rows;
    }

    public void deleteNote(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NOTES, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Note> searchNotes(String query) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String s = "%" + query + "%";
        Cursor c = db.query(TABLE_NOTES, null,
            COL_TITLE + " LIKE ? OR " + COL_CONTENT + " LIKE ?",
            new String[]{s, s}, null, null, COL_UPDATED_AT + " DESC");
        if (c.moveToFirst()) {
            do { notes.add(cursorToNote(c)); } while (c.moveToNext());
        }
        c.close(); db.close();
        return notes;
    }

    private Note cursorToNote(Cursor c) {
        Note note = new Note();
        note.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        note.setTitle(c.getString(c.getColumnIndexOrThrow(COL_TITLE)));
        note.setContent(c.getString(c.getColumnIndexOrThrow(COL_CONTENT)));
        note.setColorIndex(c.getInt(c.getColumnIndexOrThrow(COL_COLOR)));
        note.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(COL_CREATED_AT)));
        note.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(COL_UPDATED_AT)));
        return note;
    }
}
