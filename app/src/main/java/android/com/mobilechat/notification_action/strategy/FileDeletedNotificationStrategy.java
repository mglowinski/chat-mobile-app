package android.com.mobilechat.notification_action.strategy;

import android.com.mobilechat.adapter.MessagesAdapter;
import android.com.mobilechat.model.message.MessageDto;
import android.com.mobilechat.model.notification.body.FileDeletedBody;
import android.com.mobilechat.notification_action.NotificationMethod;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import lombok.AllArgsConstructor;

import java.util.List;

import static android.com.mobilechat.chat.ChatFragment.SPLIT_FILE_REGEX;

@AllArgsConstructor
public class FileDeletedNotificationStrategy implements NotificationMethod {

    private RecyclerView recyclerViewRoomMessages;

    @Override
    public void reactOnNotification(Object object) {
        MessagesAdapter messagesAdapter = (MessagesAdapter) recyclerViewRoomMessages.getAdapter();
        List<MessageDto> messages = messagesAdapter.getMessages();
        FileDeletedBody fileDeletedBody = (FileDeletedBody) object;

        int position = findMessagePosition(messages, fileDeletedBody);
        messagesAdapter.getMessages().remove(position);

        new Handler(Looper.getMainLooper()).post(() -> messagesAdapter.notifyItemRemoved(position));
    }

    private int findMessagePosition(List<MessageDto> messages,
                                    FileDeletedBody fileDeletedBody) {
        int position = 0;

        for (MessageDto messageDto : messages) {
            String uniqueFileName = getUniqueFileName(messageDto.getContent());
            if (uniqueFileName.equals(fileDeletedBody.getStoredFilename())) {
                return position;
            }
            position++;
        }

        return position;
    }

    private String getUniqueFileName(String content) {
        String[] parts = content.split(SPLIT_FILE_REGEX);
        return parts[0];
    }

}
