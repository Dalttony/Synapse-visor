package com.example.reconocimiento.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reconocimiento.R;
import com.example.reconocimiento.data.local.AttendanceEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private List<AttendanceEntity> attendanceList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public void setAttendanceList(List<AttendanceEntity> attendanceList) {
        this.attendanceList = attendanceList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceEntity attendance = attendanceList.get(position);
        holder.bind(attendance);
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    class AttendanceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvWorkerName;
        private final TextView tvEntryDate;
        private final TextView tvExitDate;
        private final View statusIndicator;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkerName = itemView.findViewById(R.id.tvWorkerName);
            tvEntryDate = itemView.findViewById(R.id.tvEntryDate);
            tvExitDate = itemView.findViewById(R.id.tvExitDate);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }

        public void bind(AttendanceEntity attendance) {
            tvWorkerName.setText(attendance.getWorkerName());

            if (attendance.getEntryDate() != null) {
                tvEntryDate.setText(dateFormat.format(new Date(attendance.getEntryDate())));
            } else {
                tvEntryDate.setText("--");
            }

            if (attendance.getExitDate() != null) {
                tvExitDate.setText(dateFormat.format(new Date(attendance.getExitDate())));
                statusIndicator.setBackgroundResource(R.drawable.circle_green);
            } else {
                tvExitDate.setText("En curso");
                statusIndicator.setBackgroundResource(R.drawable.circle_red);
            }
        }
    }
}