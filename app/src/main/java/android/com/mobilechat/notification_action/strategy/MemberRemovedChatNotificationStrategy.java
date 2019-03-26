package android.com.mobilechat.notification_action.strategy;

import android.com.mobilechat.model.notification.body.MemberRemovedBody;
import android.com.mobilechat.notification_action.NotificationMethod;
import android.com.mobilechat.utils.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.AllArgsConstructor;

import static android.com.mobilechat.room.RoomsFragment.SUBSCRIBE_TOPIC_PREFIX;

@AllArgsConstructor
public class MemberRemovedChatNotificationStrategy implements NotificationMethod {

    private Context context;
    private FragmentManager fragmentManager;
    private String email;
    private long roomId;

    @Override
    public void reactOnNotification(Object object) {
        MemberRemovedBody memberRemovedBody = (MemberRemovedBody) object;

        if (isUserLoggedBeingRemoved(memberRemovedBody)
                && isUserLocatedInRoomFromWhichIsRemoved(memberRemovedBody)) {
            new Handler(Looper.getMainLooper()).post(() -> {
                NotificationManager.showToast(context, "You have been removed from room");

                backToRoomFragment();
                unsubscribeFromTopicRoom(memberRemovedBody);
            });
        } else {
            new Handler(Looper.getMainLooper()).post(() -> {
                NotificationManager.showToast(context, "You have been removed from room");

                unsubscribeFromTopicRoom(memberRemovedBody);
            });
        }
    }

    private void unsubscribeFromTopicRoom(MemberRemovedBody memberRemovedBody) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(SUBSCRIBE_TOPIC_PREFIX +
                memberRemovedBody.getRoomId());
    }

    private boolean isUserLoggedBeingRemoved(MemberRemovedBody memberRemovedBody) {
        return email.equals(memberRemovedBody.getMemberEmail());
    }

    private boolean isUserLocatedInRoomFromWhichIsRemoved(MemberRemovedBody memberRemovedBody) {
        return roomId == memberRemovedBody.getRoomId();
    }

    private void backToRoomFragment() {
        fragmentManager.popBackStack();
    }

}
