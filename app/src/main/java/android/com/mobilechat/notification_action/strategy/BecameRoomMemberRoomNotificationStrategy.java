package android.com.mobilechat.notification_action.strategy;

import android.com.mobilechat.adapter.RoomsAdapter;
import android.com.mobilechat.model.notification.body.BecameRoomMemberBody;
import android.com.mobilechat.model.room.RoomDto;
import android.com.mobilechat.notification_action.NotificationMethod;
import android.com.mobilechat.service.ServiceGenerator;
import android.com.mobilechat.service.requests.RoomService;
import android.com.mobilechat.utils.MessageConstants;
import android.com.mobilechat.utils.NotificationManager;
import android.com.mobilechat.utils.TokenStore;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.AllArgsConstructor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.com.mobilechat.room.RoomsFragment.SUBSCRIBE_TOPIC_PREFIX;

@AllArgsConstructor
public class BecameRoomMemberRoomNotificationStrategy implements NotificationMethod {

    private Context context;
    private RecyclerView recyclerViewRooms;

    @Override
    public void reactOnNotification(Object object) {
        BecameRoomMemberBody becameRoomMemberBody =
                (BecameRoomMemberBody) object;

        getRoomRequest(becameRoomMemberBody.getRoomId());

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

    private void getRoomRequest(long roomId) {
        RoomService roomService = ServiceGenerator.createService(RoomService.class,
                TokenStore.getToken(context));

        roomService.getRoomById(roomId).enqueue(new Callback<RoomDto>() {
            @Override
            public void onResponse(@NonNull Call<RoomDto> call,
                                   @NonNull Response<RoomDto> response) {
                if (response.isSuccessful()) {
                    updateRoomsAdapter(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RoomDto> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    private void updateRoomsAdapter(RoomDto roomDto) {
        RoomsAdapter roomsAdapter = (RoomsAdapter) recyclerViewRooms.getAdapter();
        roomsAdapter.getRoomDtos().add(roomDto);
        new Handler(Looper.getMainLooper()).post(() -> {
            roomsAdapter.notifyItemInserted(roomsAdapter.getRoomDtos().size());
            scrollToDown(roomsAdapter.getRoomDtos().size());
        });
    }

    private void scrollToDown(int position) {
        recyclerViewRooms.scrollToPosition(position - 1);
    }

}
