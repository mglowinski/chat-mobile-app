package android.com.mobilechat.notification_action.strategy;

import android.com.mobilechat.R;
import android.com.mobilechat.adapter.RoomsAdapter;
import android.com.mobilechat.model.message.MessageDto;
import android.com.mobilechat.model.room.RoomDto;
import android.com.mobilechat.notification_action.NotificationMethod;
import android.com.mobilechat.utils.NotificationManager;
import android.com.mobilechat.utils.RoomNotificationStore;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class AddMessageRoomNotificationStrategy implements NotificationMethod {

    private Context context;
    private RecyclerView recyclerViewRooms;

    @Override
    public void reactOnNotification(Object object) {
        MessageDto messageDto = (MessageDto) object;
        int roomPosition = getRoomPosition(messageDto.getRoomId());

        markRoom(roomPosition);
        setRoomToStore(messageDto.getRoomId());

        playSoundNotification();
    }

    private void playSoundNotification() {
        NotificationManager.playBeep(context);
    }

    private int getRoomPosition(long roomId) {
        RoomsAdapter roomsAdapter = (RoomsAdapter) recyclerViewRooms.getAdapter();
        int position = 0;

        for (RoomDto roomDto : roomsAdapter.getRoomDtos()) {
            if (roomDto.getId() == roomId) {
                return position;
            }
            position++;
        }

        return position;
    }

    private void markRoom(int position) {
        recyclerViewRooms.post(() -> {
            View view = recyclerViewRooms.getLayoutManager().findViewByPosition(position);
            CardView cardView = view.findViewById(R.id.card_view_item_room);
            cardView.setCardBackgroundColor(Color.CYAN);
        });
    }

    private void setRoomToStore(long roomId) {
        Set<String> markRoomsSet = RoomNotificationStore.getRoomsSet(context);

        if (!markRoomsSet.contains(String.valueOf(roomId))) {
            markRoomsSet.add(String.valueOf(roomId));
            RoomNotificationStore.saveRoomsSet(context, markRoomsSet);
        }
    }

}
