package android.com.mobilechat.room;

import android.com.mobilechat.R;
import android.com.mobilechat.adapter.RoomsAdapter;
import android.com.mobilechat.chat.ChatFragment;
import android.com.mobilechat.model.notification.Notification;
import android.com.mobilechat.model.room.RoomDto;
import android.com.mobilechat.notification_action.NotificationMethod;
import android.com.mobilechat.notification_action.NotificationMethodContext;
import android.com.mobilechat.notification_action.strategy.*;
import android.com.mobilechat.service.ServiceGenerator;
import android.com.mobilechat.service.requests.RoomService;
import android.com.mobilechat.user.UserProfileFragment;
import android.com.mobilechat.utils.*;
import android.com.mobilechat.web_socket.WebSocketClient;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.NoArgsConstructor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.naiksoftware.stomp.StompHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
public class RoomsFragment extends Fragment implements RoomsAdapter.ClickListener, WebSocketClient.Callback {

    public static final String TAG = RoomsFragment.class.getName();
    public static final String SUBSCRIBE_TOPIC_PREFIX = "room-";

    private WebSocketClient webSocketClient;
    private List<RoomDto> roomDtos = new ArrayList<>();
    private AlertDialog dialog;
    private TextView textViewErrorMessage;
    private RecyclerView recyclerViewRooms;
    private NotificationMethodContext notificationMethodContext = new NotificationMethodContext();

    public static RoomsFragment newInstance() {
        return new RoomsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configureToolbar(view);
        configureRoomsRecyclerView(view);
        configureAddRoomFloatingActionButton(view);
    }

    @Override
    public void onResume() {
        super.onResume();

        createWebSocketClient();
        initConnection();
        subscribeOnTopic();
        getRoomsRequest();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStop() {
        super.onStop();
        closeConnection();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_rooms_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                loadUserProfileFragment();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void configureToolbar(View view) {
        Toolbar mToolbar = view.findViewById(R.id.toolbar_fragment_rooms);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        }
    }

    private void loadUserProfileFragment() {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            UserProfileFragment userProfileFragment = UserProfileFragment.newInstance();

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, userProfileFragment, UserProfileFragment.TAG)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void configureRoomsRecyclerView(View view) {
        recyclerViewRooms = view.findViewById(R.id.recycler_rooms);
    }

    private void configureAddRoomFloatingActionButton(View view) {
        FloatingActionButton fab = view.findViewById(R.id.fab_add_room);
        fab.setOnClickListener(v -> showCreateRoomDialog());
    }

    private void showCreateRoomDialog() {
        if (getActivity() != null) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_room, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(dialogView);
            dialog = builder.create();
            dialog.show();
            configureCreateNewRoomButton(dialogView);
        }
    }

    private void configureCreateNewRoomButton(View view) {
        EditText newRoomNameEditText = view.findViewById(R.id.edit_text_new_room_name);
        EditText newRoomDescriptionEditText =
                view.findViewById(R.id.edit_text_new_room_description);
        Button createNewRoomButton = view.findViewById(R.id.btn_create_new_room);
        textViewErrorMessage = view.findViewById(R.id.text_view_error_msg);

        createNewRoomButton.setOnClickListener(v -> {
            String newRoomName = newRoomNameEditText.getText().toString();
            String newRoomDescription = newRoomDescriptionEditText.getText().toString();
            if (newRoomName.isEmpty()) {
                setErrorMessage(MessageConstants.ROOM_NAME_CANNOT_BE_BLANK_ERROR_MESSAGE);
            } else {
                createRoomRequest(newRoomName, newRoomDescription);
            }
        });
    }

    private void setErrorMessage(String errorMessage) {
        textViewErrorMessage.setVisibility(View.VISIBLE);
        textViewErrorMessage.setText(errorMessage);
    }

    @Override
    public void onItemClick(int position, View v) {
        loadChatFragment(position);
        unMarkRoom(position);
    }

    private void loadChatFragment(int position) {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            ChatFragment chatFragment = ChatFragment.newInstance();
            chatFragment.setArguments(loadRoomToBundle(position));

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, chatFragment, ChatFragment.TAG)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private Bundle loadRoomToBundle(int position) {
        Bundle bundle = new Bundle();
        RoomDto roomDto = roomDtos.get(position);
        bundle.putSerializable("roomDto", roomDto);
        return bundle;
    }

    private void unMarkRoom(int position) {
        long roomId = roomDtos.get(position).getId();
        Set<String> markRoomsSet = RoomNotificationStore.getRoomsSet(getContext());

        if (isRoomAlreadyMarked(roomId, markRoomsSet)) {
            markRoomsSet.remove(String.valueOf(roomId));
            RoomNotificationStore.saveRoomsSet(getContext(), markRoomsSet);
        }
    }

    private boolean isRoomAlreadyMarked(long roomId, Set<String> markRoomsSet) {
        return markRoomsSet.contains(String.valueOf(roomId));
    }

    @Override
    public void onImageClick(int position, View v) {
        loadAddMembersFragment(position);
    }

    @Override
    public void onMessageReceived(String jsonMessageForm) {
        Log.e("MESSAGE_RECEIVED", jsonMessageForm);
        Notification notification = parseReceivedJsonMessage(jsonMessageForm);

        NotificationMethod notificationMethod = null;

        if (notification != null) {
            switch (notification.getType()) {
                case ADD_MESSAGE:
                    notificationMethod = new AddMessageRoomNotificationStrategy(getContext(),
                            recyclerViewRooms);
                    break;

                case ADD_FILE:
                    notificationMethod = new AddFileRoomNotificationStrategy(getContext(),
                            recyclerViewRooms);
                    break;

                case BECAME_ROOM_MEMBER:
                    notificationMethod = new BecameRoomMemberRoomNotificationStrategy(getContext(),
                            recyclerViewRooms);
                    break;

                case MEMBER_REMOVED:
                    notificationMethod = new MemberRemovedRoomNotificationStrategy(getContext(),
                            recyclerViewRooms, TokenStore.decodeToken(TokenStore.getToken
                            (getContext())).getSub());
                    break;

                case ROOM_ARCHIVED:
                    notificationMethod = new RoomArchivedRoomNotificationStrategy
                            (getContext(), recyclerViewRooms);
                    break;
            }

            if (notificationMethod != null) {
                notificationMethodContext.setNotificationMethodStrategy(notificationMethod);
                notificationMethodContext.createReactOnNotification(notification.getBody());
            }
        }
    }

    private Notification parseReceivedJsonMessage(String jsonMessageForm) {
        return JsonConverter.parseReceivedJsonToNotificationObject(jsonMessageForm);
    }

    private int getRoomPosition(long roomId) {
        int position = 0;

        for (RoomDto roomDto : roomDtos) {
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

    private void loadAddMembersFragment(int position) {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            AddRoomMembersFragment addRoomMembersFragment = AddRoomMembersFragment.newInstance();
            addRoomMembersFragment.setArguments(loadRoomToBundle(position));

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, addRoomMembersFragment, AddRoomMembersFragment.TAG)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void createRoomRequest(String roomName, String roomDescription) {
        RoomDto roomDto = new RoomDto(roomName, roomDescription);

        RoomService roomService = ServiceGenerator.createService(
                RoomService.class, TokenStore.getToken(getContext()));

        roomService.createRoom(roomDto).enqueue(new Callback<RoomDto>() {
            @Override
            public void onResponse(@NonNull Call<RoomDto> call,
                                   @NonNull Response<RoomDto> response) {
                if (response.isSuccessful()) {
                    onResume();
                    dialog.dismiss();
                } else {
                    setErrorMessage(MessageConstants.ROOM_NAME_ALREADY_EXISTS_ERROR_MESSAGE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RoomDto> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    private void getRoomsRequest() {
        RoomService roomService = ServiceGenerator.createService(
                RoomService.class, TokenStore.getToken(getContext()));

        roomService.getRooms().enqueue(new Callback<List<RoomDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<RoomDto>> call,
                                   @NonNull Response<List<RoomDto>> response) {
                if (response.isSuccessful()) {
                    roomDtos.clear();
                    roomDtos.addAll(response.body());
                    setAdapterWithRooms();
                    findRoomsToMarkAsNewMessageCame();
                    subscribeOnFirebaseRoomTopics(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RoomDto>> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    private void findRoomsToMarkAsNewMessageCame() {
        Set<String> rooms = RoomNotificationStore.getRoomsSet(getContext());

        for (RoomDto roomDto : roomDtos) {
            if (rooms.contains(String.valueOf(roomDto.getId()))) {
                int roomPosition = getRoomPosition(roomDto.getId());
                markRoom(roomPosition);
            }
        }
    }

    private void setAdapterWithRooms() {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RoomsAdapter roomsAdapter = new RoomsAdapter(roomDtos, this);
        recyclerViewRooms.setLayoutManager(layoutManager);
        recyclerViewRooms.setAdapter(roomsAdapter);
    }

    private void createWebSocketClient() {
        webSocketClient = new WebSocketClient(this);
    }

    private void initConnection() {
        webSocketClient.initConnection(createInitConnectionRequestHeader());
    }

    private void closeConnection() {
        webSocketClient.closeConnection();
    }

    private List<StompHeader> createInitConnectionRequestHeader() {
        List<StompHeader> headers = new ArrayList<>();

        StompHeader token = new StompHeader("Authorization", "Bearer " + TokenStore.getToken(getContext()));
        StompHeader contentType = new StompHeader("Content-Type", ConnectionProperties.CONTENT_TYPE);
        StompHeader acceptVersion = new StompHeader("accept-version", ConnectionProperties.ACCEPT_VERSION);
        StompHeader heartBeat = new StompHeader("heart-beat", ConnectionProperties.HEART_BEAT);

        headers.add(token);
        headers.add(contentType);
        headers.add(acceptVersion);
        headers.add(heartBeat);

        return headers;
    }

    private void subscribeOnTopic() {
        webSocketClient.subscribeOnMessage(ConnectionProperties.SUBSCRIBE_TOPIC_PATH);
    }

    private void subscribeOnFirebaseRoomTopics(List<RoomDto> roomDtos) {
        for (RoomDto roomDto : roomDtos) {
            String roomId = String.valueOf(roomDto.getId());
            FirebaseMessaging.getInstance().subscribeToTopic(SUBSCRIBE_TOPIC_PREFIX + roomId);
        }
    }

}
