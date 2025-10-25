package com.example.reconocimiento.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reconocimiento.R;
import com.example.reconocimiento.data.local.OutboxEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OutBoxAdapter extends RecyclerView.Adapter<OutBoxAdapter.OutBoxViewHolder> {

    private List<OutboxEntity> outBoxList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private OnOutBoxActionListener listener;

    public interface OnOutBoxActionListener {
        void onRetryRecord(OutboxEntity record);
        void onDeleteRecord(OutboxEntity record);
    }

    public void setOutBoxList(List<OutboxEntity> outBoxList) {
        this.outBoxList = outBoxList;
        notifyDataSetChanged();
    }

    public void setOnOutBoxActionListener(OnOutBoxActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OutBoxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outbox, parent, false);
        return new OutBoxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutBoxViewHolder holder, int position) {
        OutboxEntity outBox = outBoxList.get(position);
        holder.bind(outBox);
    }

    @Override
    public int getItemCount() {
        return outBoxList.size();
    }

    class OutBoxViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRecordId;
        private final TextView tvStatus;
        private final TextView tvPayload;
        private final TextView tvCreatedAt;
        private final TextView tvAttempts;
        private final TextView tvError;
        private final LinearLayout actionButtons;
        private final ImageButton btnRetry;
        private final ImageButton btnDelete;

        public OutBoxViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecordId = itemView.findViewById(R.id.tvRecordId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPayload = itemView.findViewById(R.id.tvPayload);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvAttempts = itemView.findViewById(R.id.tvAttempts);
            tvError = itemView.findViewById(R.id.tvError);
            actionButtons = itemView.findViewById(R.id.actionButtons);
            btnRetry = itemView.findViewById(R.id.btnRetry);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(OutboxEntity outBox) {
            // Set ID (truncated for display)
            String shortId = outBox.getId().length() > 8 ?
                outBox.getId().substring(0, 8) + "..." : outBox.getId();
            tvRecordId.setText("ID: " + shortId);

            // Set status with appropriate background
            tvStatus.setText(outBox.getStatus());
            setStatusBackground(outBox.getStatus());

            // Set payload preview (truncated)
            String payload = outBox.getPayloadJson();
            if (payload.length() > 100) {
                payload = payload.substring(0, 100) + "...";
            }
            tvPayload.setText(payload);

            // Set created date
            tvCreatedAt.setText("Creado: " + dateFormat.format(new Date(outBox.getCreatedAt())));

            // Set attempts
            tvAttempts.setText("Intentos: " + outBox.getAttempts());

            // Handle error display and action buttons
            if ("FAILED".equals(outBox.getStatus())) {
                if (outBox.getLastError() != null && !outBox.getLastError().isEmpty()) {
                    tvError.setText("Error: " + outBox.getLastError());
                    tvError.setVisibility(View.VISIBLE);
                } else {
                    tvError.setVisibility(View.GONE);
                }
                actionButtons.setVisibility(View.VISIBLE);

                btnRetry.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRetryRecord(outBox);
                    }
                });

                btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteRecord(outBox);
                    }
                });
            } else {
                tvError.setVisibility(View.GONE);
                actionButtons.setVisibility(View.GONE);
            }
        }

        private void setStatusBackground(String status) {
            switch (status) {
                case "PENDING":
                    tvStatus.setBackgroundResource(R.drawable.status_pending);
                    break;
                case "SENDING":
                    tvStatus.setBackgroundResource(R.drawable.status_sending);
                    break;
                case "SENT":
                    tvStatus.setBackgroundResource(R.drawable.status_sent);
                    break;
                case "FAILED":
                    tvStatus.setBackgroundResource(R.drawable.status_failed);
                    break;
                default:
                    tvStatus.setBackgroundResource(R.drawable.status_pending);
                    break;
            }
        }
    }
}