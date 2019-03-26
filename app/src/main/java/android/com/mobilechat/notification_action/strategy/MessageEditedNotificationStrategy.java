package android.com.mobilechat.notification_action.strategy;

import android.com.mobilechat.adapter.MessagesAdapter;
import android.com.mobilechat.model.message.MessageDto;
import android.com.mobilechat.model.notification.body.MessageEditedBody;
import android.com.mobilechat.notification_action.NotificationMethod;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class MessageEditedNotificationStrategy implements NotificationMethod {

    private RecyclerView recyclerViewRoomMessages;

    @Override
    public void reactOnNotification(Object object) {
        MessagesAdapter messagesAdapter = (MessagesAdapter) recyclerViewRoomMessages.getAdapter();
        List<MessageDto> messages = messagesAdapter.getMessages();
        MessageEditedBody messageEditedBody = (MessageEditedBody) object;

        int position = findMessagePosition(messages, messageEditedBody);
        messagesAdapter.getMessages().get(position).setContent(messageEditedBody.getMessageNewContent());

        new Handler(Looper.getMainLooper()).post(() -> {
            messagesAdapter.getMessages().get(position).setEdited(true);

            messagesAdapter.notifyItemChanged(position);
        });
    }

    private int findMessagePosition(List<MessageDto> messages,
                                    MessageEditedBody messageEditedBody) {
        int position = 0;

        for (MessageDto messageDto : messages) {
            if (messageDto.getMessageId().equals(messageEditedBody.getMessageId())) {
                return position;
            }
            position++;
        }

        return position;
    }

}
