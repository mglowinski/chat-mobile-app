package android.com.mobilechat.notification_action.strategy;

import android.com.mobilechat.adapter.MessagesAdapter;
import android.com.mobilechat.model.message.MessageDto;
import android.com.mobilechat.model.notification.body.MessageDeletedBody;
import android.com.mobilechat.notification_action.NotificationMethod;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class MessageDeletedNotificationStrategy implements NotificationMethod {

    private RecyclerView recyclerViewRoomMessages;

    @Override
    public void reactOnNotification(Object object) {
        MessagesAdapter messagesAdapter = (MessagesAdapter) recyclerViewRoomMessages.getAdapter();
        List<MessageDto> messages = messagesAdapter.getMessages();
        MessageDeletedBody messageDeletedBody = (MessageDeletedBody) object;

        int position = findMessagePosition(messages, messageDeletedBody);
        messagesAdapter.getMessages().remove(position);

        new Handler(Looper.getMainLooper()).post(() -> messagesAdapter.notifyItemRemoved(position));
    }

    private int findMessagePosition(List<MessageDto> messages,
                                    MessageDeletedBody messageDeletedBody) {
        int position = 0;

        for (MessageDto messageDto : messages) {
            if (messageDto.getMessageId().equals(messageDeletedBody.getMessageId())) {
                return position;
            }
            position++;
        }

        return position;
    }

}
