package android.com.mobilechat.notification_action.strategy;

import android.com.mobilechat.adapter.RoomsAdapter;
import android.com.mobilechat.model.notification.body.RoomArchivedBody;
import android.com.mobilechat.model.room.RoomDto;
import android.com.mobilechat.notification_action.NotificationMethod;
import android.com.mobilechat.utils.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class RoomArchivedRoomNotificationStrategy implements NotificationMethod {

    private Context context;
    private RecyclerView recyclerViewRooms;

    @Override
    public void reactOnNotification(Object object) {
        RoomsAdapter roomsAdapter = (RoomsAdapter) recyclerViewRooms.getAdapter();
        List<RoomDto> roomDtos = roomsAdapter.getRoomDtos();
        RoomArchivedBody roomArchivedBody = (RoomArchivedBody) object;

        int position = findRoomPosition(roomDtos, roomArchivedBody);

        String actualRoomName = roomsAdapter.getRoomDtos().get(position).getName();
        roomsAdapter.getRoomDtos().get(position).setName(actualRoomName + " - ARCHIVED");

        new Handler(Looper.getMainLooper()).post(() -> {
            roomsAdapter.notifyItemChanged(position);
            notifyAboutAddedToRoom(actualRoomName);
        });
    }

    private int findRoomPosition(List<RoomDto> roomDtos,
                                 RoomArchivedBody roomArchivedBody) {
        int position = 0;

        for (RoomDto roomDto : roomDtos) {
            if (roomDto.getId() == roomArchivedBody.getRoomid()) {
                return position;
            }
            position++;
        }

        return position;
    }

    private void notifyAboutAddedToRoom(String roomName) {
        NotificationManager.showToast(context, "Room " + roomName + " has been archived");
    }

}
