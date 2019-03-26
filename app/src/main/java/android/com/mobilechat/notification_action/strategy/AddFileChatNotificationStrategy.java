package android.com.mobilechat.notification_action.strategy;

import android.com.mobilechat.adapter.MessagesAdapter;
import android.com.mobilechat.model.message.MessageDto;
import android.com.mobilechat.model.message.MessageType;
import android.com.mobilechat.model.notification.body.AddFileBody;
import android.com.mobilechat.notification_action.NotificationMethod;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import lombok.AllArgsConstructor;

import static android.com.mobilechat.chat.ChatFragment.SPLIT_FILE_REGEX;

@AllArgsConstructor
public class AddFileChatNotificationStrategy implements NotificationMethod {

    private RecyclerView recyclerViewRoomMessages;

    @Override
    public void reactOnNotification(Object object) {
        MessageDto messageDto = convertAddFileBodyToMessageDto((AddFileBody) object);
        messageDto.setType(MessageType.FILE);

        MessagesAdapter messagesAdapter = (MessagesAdapter) recyclerViewRoomMessages.getAdapter();
        messagesAdapter.getMessages().add(messageDto);

        new Handler(Looper.getMainLooper()).post(() -> {
            messagesAdapter.notifyItemInserted(messagesAdapter.getMessages().size());
            scrollToDown(messagesAdapter.getMessages().size());
        });
    }

    private MessageDto convertAddFileBodyToMessageDto(AddFileBody addFileBody) {
        return MessageDto.builder()
                .author(addFileBody.getAuthor())
                .roomId(addFileBody.getRoomId())
                .content(addFileBody.getStoredFilename() + SPLIT_FILE_REGEX + addFileBody.getOriginalFilename())
                .timestamp(addFileBody.getTimestamp())
                .build();
    }

    private void scrollToDown(int position) {
        recyclerViewRoomMessages.scrollToPosition(position - 1);
    }

}
