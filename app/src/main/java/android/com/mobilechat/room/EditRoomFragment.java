package android.com.mobilechat.room;

import android.app.ProgressDialog;
import android.com.mobilechat.R;
import android.com.mobilechat.adapter.RoomMembersAdapter;
import android.com.mobilechat.model.room.RoomDataFormDto;
import android.com.mobilechat.model.room.RoomDto;
import android.com.mobilechat.model.token.TokenDecoded;
import android.com.mobilechat.model.user.UserInfoDto;
import android.com.mobilechat.service.ServiceGenerator;
import android.com.mobilechat.service.requests.RoomService;
import android.com.mobilechat.utils.MessageConstants;
import android.com.mobilechat.utils.NotificationManager;
import android.com.mobilechat.utils.TokenStore;
import android.com.mobilechat.utils.Validator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import lombok.NoArgsConstructor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

@NoArgsConstructor
public class EditRoomFragment extends Fragment implements RoomMembersAdapter.ClickListener {

    public static final String TAG = EditRoomFragment.class.getName();

    private ProgressDialog progressDialog;
    private RoomDto roomDto;
    private EditText editTextModifyRoomName;
    private EditText editTextModifyRoomDescription;
    private RecyclerView recyclerViewRoomMembers;

    public static EditRoomFragment newInstance() {
        return new EditRoomFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            roomDto = (RoomDto) bundle.getSerializable("roomDto");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_edit_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configureToolbar(view);
        configureEditTexts(view);
        configureEditRoomSaveButton(view);
        configureRoomsRecyclerView(view);
        configureArchiveRoomButton(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        getRoomMembersRequest();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void configureArchiveRoomButton(View view) {
        Button buttonArchiveRoom = view.findViewById(R.id.btn_archive_room);

        TokenDecoded tokenDecoded = TokenStore.decodeToken(TokenStore.getToken(getContext()));
        if (tokenDecoded.getRole().equals("STUDENT") || roomDto.getId() == 1) {
            buttonArchiveRoom.setEnabled(false);
            buttonArchiveRoom.setAlpha(.3f);
        } else {
            buttonArchiveRoom.setOnClickListener(v -> archiveRoomRequest());
        }
    }

    private void archiveRoomRequest() {
        RoomService roomService = ServiceGenerator.createService(RoomService.class,
                TokenStore.getToken(getContext()));

        roomService.archiveRoom(roomDto.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    NotificationManager.showToast(getContext(), "Room has been archived");
                } else {
                    NotificationManager.showToast(getContext(), "Room is already archived");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    private void configureRoomsRecyclerView(View view) {
        recyclerViewRoomMembers = view.findViewById(R.id.recycler_members);
    }

    private void configureToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar_fragment_edit_room);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        }
    }

    private void configureEditTexts(View view) {
        editTextModifyRoomName = view.findViewById(R.id.edit_text_modify_room_name);
        editTextModifyRoomDescription =
                view.findViewById(R.id.edit_text_modify_room_description);

        editTextModifyRoomName.setHint(roomDto.getName());
        editTextModifyRoomDescription.setHint(roomDto.getDescription());
    }

    private void configureEditRoomSaveButton(View view) {
        Button buttonSaveEditRoom = view.findViewById(R.id.btn_save_edit_room);

        TokenDecoded tokenDecoded = TokenStore.decodeToken(TokenStore.getToken(getContext()));
        if (tokenDecoded.getRole().equals("STUDENT") || roomDto.getId() == 1) {
            buttonSaveEditRoom.setEnabled(false);
            buttonSaveEditRoom.setAlpha(.3f);

            editTextModifyRoomName.setEnabled(false);
            editTextModifyRoomDescription.setEnabled(false);
        } else {
            buttonSaveEditRoom.setOnClickListener(v -> editRoom());
        }
    }

    private void editRoom() {
        if (Validator.modifyRoomValidate(editTextModifyRoomName, editTextModifyRoomDescription)) {
            RoomDataFormDto roomDataFormDto = createRoomDataFormDtoBasedOnEditForm(
                    editTextModifyRoomName.getText().toString(),
                    editTextModifyRoomDescription.getText().toString());

            editRoomRequest(roomDataFormDto);
        }
    }

    private void editRoomRequest(RoomDataFormDto roomDataFormDto) {
        RoomService roomService = ServiceGenerator.createService(
                RoomService.class, TokenStore.getToken(getContext()));

        roomService.modifyRoom(roomDto.getId(), roomDataFormDto).enqueue(new Callback<RoomDto>() {
            @Override
            public void onResponse(@NonNull Call<RoomDto> call,
                                   @NonNull Response<RoomDto> response) {
                if (response.isSuccessful()) {
                    NotificationManager.showToast(getContext(), "Room updated");
                } else {
                    Log.e("EDIT ROOM", "FAILED");
                }
            }

            @Override
            public void onFailure(@NonNull Call<RoomDto> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    private RoomDataFormDto createRoomDataFormDtoBasedOnEditForm(String roomNewName,
                                                                 String roomNewDescription) {
        RoomDataFormDto roomDataFormDto = new RoomDataFormDto();

        if (!roomNewName.isEmpty()) {
            roomDataFormDto.setName(roomNewName);
        } else {
            roomDataFormDto.setName(roomDto.getName());
        }

        if (!roomNewDescription.isEmpty()) {
            roomDataFormDto.setDescription(roomNewDescription);
        } else {
            roomDataFormDto.setDescription(roomDto.getDescription());
        }

        return roomDataFormDto;
    }

    private void getRoomMembersRequest() {
        startProgressDialog();

        RoomService roomService = ServiceGenerator.createService(
                RoomService.class, TokenStore.getToken(getContext()));

        roomService.getRoomMembers(roomDto.getId()).enqueue(new Callback<List<UserInfoDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserInfoDto>> call,
                                   @NonNull Response<List<UserInfoDto>> response) {
                if (response.isSuccessful()) {
                    setAdapterWithRoomMembers(response.body());
                }
                stopProgressDialog();
            }

            @Override
            public void onFailure(@NonNull Call<List<UserInfoDto>> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
                stopProgressDialog();
            }
        });
    }

    private void setAdapterWithRoomMembers(List<UserInfoDto> userInfoDtos) {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RoomMembersAdapter roomsAdapter = new RoomMembersAdapter(userInfoDtos, this);
        recyclerViewRoomMembers.setLayoutManager(layoutManager);
        recyclerViewRoomMembers.setAdapter(roomsAdapter);
    }

    private void startProgressDialog() {
        progressDialog = ProgressDialog.show(getContext(), null, null, false, true);
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progressDialog.setContentView(R.layout.progress_bar);
        }
    }

    private void stopProgressDialog() {
        progressDialog.dismiss();
    }

    @Override
    public void onImageClick(int position, View v) {
        RoomMembersAdapter roomMembersAdapter = (RoomMembersAdapter) recyclerViewRoomMembers
                .getAdapter();

        String emailChosenUser = roomMembersAdapter.getUserInfoDtos().get(position).getEmail();
        String loggedUserEmail = TokenStore.decodeToken(TokenStore.getToken(getContext())).getSub();
        String loggedUserRole = TokenStore.decodeToken(TokenStore.getToken(getContext())).getRole();

        if (loggedUserRole.equals("ADMIN") || loggedUserRole.equals("MODERATOR")
                || loggedUserEmail.equals(roomDto.getCreator().getEmail())) {
            createLogoutDialog(emailChosenUser);
        } else {
            NotificationManager.showToast(getContext(), "Don't have permission");
        }
    }

    private void createLogoutDialog(String emailChosenUser) {
        new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AppTheme_Dark_Dialog))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Removing Member")
                .setMessage("Are you sure you want to remove member from room?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    removeMemberFromRoomRequest(emailChosenUser);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void removeMemberFromRoomRequest(String emailChosenUser) {
        RoomService roomService = ServiceGenerator.createService(
                RoomService.class, TokenStore.getToken(getContext()));

        roomService.removeMemberFromRoom(roomDto.getId(), emailChosenUser)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call,
                                           @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            RoomMembersAdapter roomMembersAdapter =
                                    (RoomMembersAdapter) recyclerViewRoomMembers.getAdapter();
                            List<UserInfoDto> userInfoDtos = roomMembersAdapter.getUserInfoDtos();

                            int position = findRoomMemberPosition(userInfoDtos, emailChosenUser);
                            roomMembersAdapter.getUserInfoDtos().remove(position);

                            new Handler(Looper.getMainLooper()).post(() ->
                                    roomMembersAdapter.notifyItemRemoved(position));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
                    }
                });
    }

    private int findRoomMemberPosition(List<UserInfoDto> userInfoDtos,
                                       String emailChosenUser) {
        int position = 0;

        for (UserInfoDto userInfoDto : userInfoDtos) {
            if (userInfoDto.getEmail().equals(emailChosenUser)) {
                return position;
            }
            position++;
        }

        return position;
    }

}
