package com.example.notesapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    // Pastel family-friendly colors
    public static final int[] CARD_COLORS = {
        0xFFFFF9C4, // Kuning lembut
        0xFFFFCCBC, // Peach
        0xFFC8E6C9, // Hijau mint
        0xFFBBDEFB, // Biru langit
        0xFFE1BEE7, // Ungu lavender
        0xFFFFCDD2, // Merah muda
    };

    private List<Note> noteList;
    private OnNoteClickListener listener;
    private int lastPosition = -1;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteDelete(Note note, int position);
    }

    public NoteAdapter(List<Note> noteList, OnNoteClickListener listener) {
        this.noteList = noteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);

        holder.tvTitle.setText(note.getTitle());
        String content = note.getContent() != null ? note.getContent() : "";
        holder.tvContent.setText(content.isEmpty() ? "Ketuk untuk lihat catatan" : content);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(note.getUpdatedAt())));

        // Set card color
        int colorIndex = note.getColorIndex() % CARD_COLORS.length;
        holder.cardView.setCardBackgroundColor(CARD_COLORS[colorIndex]);

        // Entry animation
        if (position > lastPosition) {
            android.view.animation.Animation anim = AnimationUtils.loadAnimation(
                holder.itemView.getContext(), android.R.anim.fade_in);
            anim.setDuration(300);
            holder.itemView.startAnimation(anim);
            lastPosition = position;
        }

        holder.itemView.setOnClickListener(v -> listener.onNoteClick(note));

        holder.btnDelete.setOnClickListener(v -> {
            listener.onNoteDelete(note, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return noteList.size(); }

    public void removeItem(int position) {
        noteList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, noteList.size());
    }

    public void updateList(List<Note> newList) {
        noteList = newList;
        lastPosition = -1;
        notifyDataSetChanged();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle, tvContent, tvDate;
        View btnDelete;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
