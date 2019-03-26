package android.com.mobilechat.notification_action.strategy;

import android.com.mobilechat.adapter.MessagesAdapter;
import android.com.mobilechat.model.message.MessageDto;
import android.com.mobilechat.model.message.MessageType;
import android.com.mobilechat.notification_action.NotificationMethod;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AddMessageChatNotificationStrategy implements NotificationMethod {

    private RecyclerView recyclerViewRoomMessages;

    @Override
    public void reactOnNotification(Object object) {
        MessageDto messageDto = (MessageDto) object;
        messageDto.setType(MessageType.TEXT);

        MessagesAdapter messagesAdapter = (MessagesAdapter) recyclerViewRoomMessages.getAdapter();
        messagesAdapter.getMessages().add(messageDto);

        new Handler(Looper.getMainLooper()).post(() -> {
            messagesAdapter.notifyItemInserted(messagesAdapter.getMessages().size());
            scrollToDown(messagesAdapter.getMessages().size());
        });
    }

    private void scrollToDown(int position) {
        recyclerViewRoomMessages.scrollToPosition(position - 1);
    }

}
