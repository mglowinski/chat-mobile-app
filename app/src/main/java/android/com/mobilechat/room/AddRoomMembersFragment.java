package android.com.mobilechat.room;

import android.app.ProgressDialog;
import android.com.mobilechat.R;
import android.com.mobilechat.adapter.AddRoomMembersAdapter;
import android.com.mobilechat.model.room.AddManyRoomMembersDto;
import android.com.mobilechat.model.room.ManyMemberAdditionResultDto;
import android.com.mobilechat.model.room.RoomDto;
import android.com.mobilechat.model.user.UserInfoDto;
import android.com.mobilechat.service.ServiceGenerator;
import android.com.mobilechat.service.requests.RoomService;
import android.com.mobilechat.service.requests.UserService;
import android.com.mobilechat.utils.MessageConstants;
import android.com.mobilechat.utils.NotificationManager;
import android.com.mobilechat.utils.TokenStore;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import lombok.NoArgsConstructor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class AddRoomMembersFragment extends Fragment implements AddRoomMembersAdapter.ClickListener {

    public static final String TAG = AddRoomMembersFragment.class.getName();

    private ProgressDialog progressDialog;
    private RecyclerView recyclerViewUserInfoDto;
    private RoomDto roomDto;
    private TextView textViewChooseUsersHeader;
    private Button addMembersButton;

    public static AddRoomMembersFragment newInstance() {
        return new AddRoomMembersFragment();
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
        return inflater.inflate(R.layout.fragment_add_room_members, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configureToolbar(view);
        configureRoomsRecyclerView(view);
        configureAddMembersButton(view);
        configureChooseUsersHeaderTextView(view);
    }

    private void configureChooseUsersHeaderTextView(View view) {
        textViewChooseUsersHeader = view.findViewById(R.id.text_view_choose_users_header);
    }

    @Override
    public void onResume() {
        super.onResume();
        getRoomMembersRequest();
    }

    private void configureToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar_fragment_add_room_members);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        }
    }

    private void configureRoomsRecyclerView(View view) {
        recyclerViewUserInfoDto = view.findViewById(R.id.recycler_add_room_members);
    }

    private void configureAddMembersButton(View view) {
        addMembersButton = view.findViewById(R.id.btn_save_add_members);
        addMembersButton.setOnClickListener(v -> addRoomMembersRequest());
    }

    private void addRoomMembersRequest() {
        AddManyRoomMembersDto addManyRoomMembersDto = addSelectedMembers();

        RoomService roomService = ServiceGenerator.createService(
                RoomService.class, TokenStore.getToken(getContext()));

        roomService.addMembersToRoom(roomDto.getId(), addManyRoomMembersDto)
                .enqueue(new Callback<ManyMemberAdditionResultDto>() {
                    @Override
                    public void onResponse(@NonNull Call<ManyMemberAdditionResultDto> call,
                                           @NonNull Response<ManyMemberAdditionResultDto> response) {
                        if (response.isSuccessful()) {
                            NotificationManager.showToast(getContext(),
                                    "Members added: " + response.body().getAdded().size());

                            backToPreviousFragment();
                        } else {
                            Log.e("FAIL", "FAIL");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ManyMemberAdditionResultDto> call,
                                          @NonNull Throwable t) {
                        Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
                    }
                });
    }

    private AddManyRoomMembersDto addSelectedMembers() {
        List<String> emails = new ArrayList<>();
        AddRoomMembersAdapter addRoomMembersAdapter = (AddRoomMembersAdapter)
                recyclerViewUserInfoDto.getAdapter();

        for (UserInfoDto addRoomMemberDto : addRoomMembersAdapter.getUserInfoDtos()) {
            if (addRoomMemberDto.isSelected()) {
                emails.add(addRoomMemberDto.getEmail());
            }
        }

        return new AddManyRoomMembersDto(emails);
    }

    private void backToPreviousFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
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
    public void onItemClick(int position, View v) {
        AddRoomMembersAdapter addRoomMembersAdapter = (AddRoomMembersAdapter)
                recyclerViewUserInfoDto.getAdapter();
        UserInfoDto userInfoDto = addRoomMembersAdapter.getUserInfoDtos().get(position);
        userInfoDto.setSelected(!userInfoDto.isSelected());

        CardView cardView = v.findViewById(R.id.card_view_item_add_room_member);
        cardView.setCardBackgroundColor(userInfoDto.isSelected() ? Color.CYAN : Color.WHITE);
    }

    private void getRoomMembersRequest() {
        startProgressDialog();

        RoomService roomService = ServiceGenerator.createService(
                RoomService.class, TokenStore.getToken(getContext()));

        roomService.getRoomMembers(roomDto.getId()).enqueue(new Callback<List<UserInfoDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserInfoDto>> call,
                                   @NonNull Response<List<UserInfoDto>> response) {
                getAllUsersRequest(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<UserInfoDto>> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    private void getAllUsersRequest(List<UserInfoDto> actualRoomMembers) {
        UserService userService = ServiceGenerator.createService(
                UserService.class, TokenStore.getToken(getContext()));

        userService.getAllUsers().enqueue(new Callback<List<UserInfoDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserInfoDto>> call,
                                   @NonNull Response<List<UserInfoDto>> response) {
                if (response.isSuccessful()) {
                    List<UserInfoDto> userInfoDtosFiltered = usersWithoutPresent(
                            response.body(), actualRoomMembers);

                    setAdapterWithUsers(userInfoDtosFiltered);

                    changeChooseUsersHeaderIfNoUsers(userInfoDtosFiltered);
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

    private List<UserInfoDto> usersWithoutPresent(List<UserInfoDto> allUsers,
                                                  List<UserInfoDto> actualRoomMembers) {
        List<UserInfoDto> userInfoDtosFiltered = new ArrayList<>();

        for (UserInfoDto userInfoDto : allUsers) {
            boolean exist = false;
            for (UserInfoDto actualMembers : actualRoomMembers) {
                if (userInfoDto.getEmail().equals(actualMembers.getEmail())) {
                    exist = true;
                }
            }
            if (!exist) {
                userInfoDtosFiltered.add(userInfoDto);
            }
        }

        return userInfoDtosFiltered;
    }

    private void setAdapterWithUsers(List<UserInfoDto> userInfoDtos) {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        AddRoomMembersAdapter roomsAdapter = new AddRoomMembersAdapter(userInfoDtos, this);
        recyclerViewUserInfoDto.setLayoutManager(layoutManager);
        recyclerViewUserInfoDto.setAdapter(roomsAdapter);
    }

    private void changeChooseUsersHeaderIfNoUsers(List<UserInfoDto> userInfoDtos) {
        if (userInfoDtos.isEmpty()) {
            configureInformationAboutNoMembers();
        }
    }

    private void configureInformationAboutNoMembers() {
        textViewChooseUsersHeader.setText("No members to add");
        addMembersButton.setEnabled(false);
        addMembersButton.setAlpha(.3f);
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

}
