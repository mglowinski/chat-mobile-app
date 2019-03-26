package android.com.mobilechat.notification_action.strategy;

import android.com.mobilechat.notification_action.NotificationMethod;
import android.com.mobilechat.utils.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RoomArchivedChatNotificationStrategy implements NotificationMethod {

    private Context context;

    @Override
    public void reactOnNotification(Object object) {
        new Handler(Looper.getMainLooper()).post(this::notifyAboutRoomArchived);
    }

    private void notifyAboutRoomArchived() {
        NotificationManager.showToast(context, "Room archived");
    }

}
