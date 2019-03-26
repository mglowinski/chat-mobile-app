package android.com.mobilechat.notification_action.strategy;

import android.com.mobilechat.model.notification.body.BecameRoomMemberBody;
import android.com.mobilechat.notification_action.NotificationMethod;
import android.com.mobilechat.utils.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.AllArgsConstructor;

import static android.com.mobilechat.room.RoomsFragment.SUBSCRIBE_TOPIC_PREFIX;

@AllArgsConstructor
public class BecameRoomMemberChatNotificationStrategy implements NotificationMethod {

    private Context context;

    @Override
    public void reactOnNotification(Object object) {
        BecameRoomMemberBody becameRoomMemberBody =
                (BecameRoomMemberBody) object;

        new Handler(Looper.getMainLooper()).post(() -> {
            notifyAboutAddedToRoom(becameRoomMemberBody);
            subscribeToTopicRoom(becameRoomMemberBody);
        });
    }

    private void subscribeToTopicRoom(BecameRoomMemberBody becameRoomMemberBody) {
        FirebaseMessaging.getInstance().subscribeToTopic(SUBSCRIBE_TOPIC_PREFIX +
                becameRoomMemberBody.getRoomId());
    }

    private void notifyAboutAddedToRoom(BecameRoomMemberBody becameRoomMemberBody) {
        NotificationManager.showToast(context, "You have been added to room " +
                becameRoomMemberBody.getRoomName());
    }

}
