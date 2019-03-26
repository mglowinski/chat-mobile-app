package android.com.mobilechat.adapter;

import android.com.mobilechat.R;
import android.com.mobilechat.model.message.MessageDto;
import android.com.mobilechat.model.token.TokenDecoded;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.com.mobilechat.chat.ChatFragment.SPLIT_FILE_REGEX;

@AllArgsConstructor
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private static final int TEXT_MSG_TYPE_SENT = 1;
    private static final int TEXT_MSG_TYPE_RECEIVED = 2;
    private static final int FILE_MSG_TYPE_SENT = 3;
    private static final int FILE_MSG_TYPE_RECEIVED = 4;

    @Getter
    private List<MessageDto> messages;
    private TokenDecoded tokenDecoded;
    private ClickListener clickListener;

    @Override
    public int getItemViewType(int position) {
        MessageDto messageDto = messages.get(position);

        switch (messageDto.getType()) {
            case FILE:
                if (isMyMessage(messageDto)) {
                    return FILE_MSG_TYPE_SENT;
                } else {
                    return FILE_MSG_TYPE_RECEIVED;
                }

            case TEXT:
                if (isMyMessage(messageDto)) {
                    return TEXT_MSG_TYPE_SENT;
                } else {
                    return TEXT_MSG_TYPE_RECEIVED;
                }
        }

        return 0;
    }

    private boolean isMyMessage(MessageDto messageDto) {
        return messageDto.getAuthor().getEmail().equals(tokenDecoded.getSub());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        ViewHolder viewHolder;
        switch (viewType) {
            case TEXT_MSG_TYPE_SENT:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_sent, viewGroup, false);
                viewHolder = new ViewHolder(view);

                viewHolder.itemView.setOnLongClickListener(v -> {
                    clickListener.onTextItemLongClick(viewHolder.getAdapterPosition(), view);
                    return true;
                });

                return viewHolder;

            case TEXT_MSG_TYPE_RECEIVED:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_received, viewGroup, false);

                viewHolder = new ViewHolder(view);

                return viewHolder;

            case FILE_MSG_TYPE_SENT:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_sent, viewGroup, false);
                viewHolder = new ViewHolder(view);

                viewHolder.itemView.setOnLongClickListener(v -> {
                    clickListener.onFileSentItemLongClick(viewHolder.getAdapterPosition(), view);
                    return true;
                });

                viewHolder.itemView.setOnClickListener(v ->
                        clickListener.onFileItemClick(viewHolder.getAdapterPosition(), view));

                return viewHolder;

            case FILE_MSG_TYPE_RECEIVED:
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_received, viewGroup, false);
                viewHolder = new ViewHolder(view);

                viewHolder.itemView.setOnLongClickListener(v -> {
                    clickListener.onFileReceivedItemLongClick(viewHolder.getAdapterPosition(), view);
                    return true;
                });

                viewHolder.itemView.setOnClickListener(v ->
                        clickListener.onFileItemClick(viewHolder.getAdapterPosition(), view));

                return viewHolder;

            default:
                throw new RuntimeException("The type has to be 1, 2, 3 or 4");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        MessageDto messageDto = messages.get(position);

        viewHolder.textViewLblFrom.setText(messageDto.getAuthor().getName());
        viewHolder.textViewLblDate.setText(formatDate(messageDto.getTimestamp()));
        viewHolder.textViewMsg.setText(setMessageContent(messageDto));

        if (messageDto.isEdited()) {
            viewHolder.textViewEdited.setVisibility(View.VISIBLE);
        }
    }

    private String setMessageContent(MessageDto messageDto) {
        switch (messageDto.getType()) {
            case TEXT:
                return messageDto.getContent();

            case FILE:
                return "FILE: " + getOriginalFileName(messageDto.getContent());
        }

        return null;
    }

    private String getOriginalFileName(String content) {
        String[] parts = content.split(SPLIT_FILE_REGEX);
        return parts[1];
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private String formatDate(Date date) {
        DateFormat df = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        return df.format(date);
    }

    public interface ClickListener {
        void onFileItemClick(int position, View v);

        void onTextItemLongClick(int position, View v);

        void onFileSentItemLongClick(int position, View v);

        void onFileReceivedItemLongClick(int position, View v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLblFrom;
        TextView textViewLblDate;
        TextView textViewMsg;
        TextView textViewEdited;

        ViewHolder(View itemView) {
            super(itemView);
            textViewLblFrom = itemView.findViewById(R.id.labelMsgFrom);
            textViewLblDate = itemView.findViewById(R.id.labelMsgDate);
            textViewMsg = itemView.findViewById(R.id.textViewMsg);
            textViewEdited = itemView.findViewById(R.id.labelMsgEdited);
        }
    }

}
