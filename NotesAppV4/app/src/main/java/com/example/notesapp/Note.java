package com.example.notesapp;

public class Note {
    private long id;
    private String title;
    private String content;
    private int colorIndex; // 0-5 for pastel card colors
    private long createdAt;
    private long updatedAt;

    public Note() {}

    public Note(String title, String content, int colorIndex) {
        this.title = title;
        this.content = content;
        this.colorIndex = colorIndex;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getColorIndex() { return colorIndex; }
    public void setColorIndex(int colorIndex) { this.colorIndex = colorIndex; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
