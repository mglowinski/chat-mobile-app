package android.com.mobilechat.notification_action.strategy;

import android.com.mobilechat.adapter.RoomsAdapter;
import android.com.mobilechat.model.notification.body.MemberRemovedBody;
import android.com.mobilechat.model.room.RoomDto;
import android.com.mobilechat.notification_action.NotificationMethod;
import android.com.mobilechat.utils.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.AllArgsConstructor;

import java.util.List;

import static android.com.mobilechat.room.RoomsFragment.SUBSCRIBE_TOPIC_PREFIX;

@AllArgsConstructor
public class MemberRemovedRoomNotificationStrategy implements NotificationMethod {

    private Context context;
    private RecyclerView recyclerViewRooms;
    private String email;

    @Override
    public void reactOnNotification(Object object) {
        RoomsAdapter roomsAdapter = (RoomsAdapter) recyclerViewRooms.getAdapter();
        List<RoomDto> roomDtos = roomsAdapter.getRoomDtos();
        MemberRemovedBody memberRemovedBody = (MemberRemovedBody) object;

        if (isUserLoggedRemoved(memberRemovedBody)) {
            int position = findRoomPosition(roomDtos, memberRemovedBody);

            String roomName = roomsAdapter.getRoomDtos().get(position).getName();
            roomsAdapter.getRoomDtos().remove(position);

            new Handler(Looper.getMainLooper()).post(() -> {
                roomsAdapter.notifyItemRemoved(position);

                NotificationManager.showToast(context, "You have been removed from room " +
                        roomName);

                unsubscribeFromTopicRoom(memberRemovedBody);
            });
        }
    }

    private void unsubscribeFromTopicRoom(MemberRemovedBody memberRemovedBody) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(SUBSCRIBE_TOPIC_PREFIX +
                memberRemovedBody.getRoomId());
    }

    private boolean isUserLoggedRemoved(MemberRemovedBody memberRemovedBody) {
        return email.equals(memberRemovedBody.getMemberEmail());
    }

    private int findRoomPosition(List<RoomDto> roomDtos,
                                 MemberRemovedBody memberRemovedBody) {
        int position = 0;

        for (RoomDto roomDto : roomDtos) {
            if (roomDto.getId().equals(memberRemovedBody.getRoomId())) {
                return position;
            }
            position++;
        }

        return position;
    }

}
