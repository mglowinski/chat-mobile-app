package android.com.mobilechat.user;

import android.com.mobilechat.R;
import android.com.mobilechat.model.token.TokenDecoded;
import android.com.mobilechat.model.user.UserAdditionalDataDto;
import android.com.mobilechat.service.ServiceGenerator;
import android.com.mobilechat.service.requests.UserProfileService;
import android.com.mobilechat.utils.MessageConstants;
import android.com.mobilechat.utils.NotificationManager;
import android.com.mobilechat.utils.TokenStore;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import lombok.NoArgsConstructor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
public class EditUserProfileFragment extends Fragment {

    public static final String TAG = EditUserProfileFragment.class.getName();

    private TokenDecoded tokenDecoded;

    private EditText editTextEditProfilePhone;
    private EditText editTextEditProfileInterests;
    private EditText editTextEditProfileAboutMe;

    private UserAdditionalDataDto userAdditionalDataDto;

    public static EditUserProfileFragment newInstance() {
        return new EditUserProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenDecoded = TokenStore.decodeToken(TokenStore.getToken(getContext()));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_edit_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configureToolbar(view);
        configureTextsView(view);
        configureEditTexts(view);
        configureEditUserProfileSaveButton(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserAdditionalDataRequest();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void configureToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar_fragment_edit_profile);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        }
    }

    private void configureEditTexts(View view) {
        editTextEditProfilePhone = view.findViewById(R.id.edit_text_edit_profile_phone);
        editTextEditProfileInterests = view.findViewById(R.id.edit_text_edit_profile_interests);
        editTextEditProfileAboutMe = view.findViewById(R.id.edit_text_edit_profile_about_me);
    }

    private void configureTextsView(View view) {
        TextView textViewName = view.findViewById(R.id.text_view_edit_profile_name);
        TextView textViewEmail = view.findViewById(R.id.text_view_edit_profile_email);
        TextView textViewIndexNumber = view.findViewById(R.id.text_view_edit_profile_index_number);

        textViewName.setText(tokenDecoded.getName() + " " + tokenDecoded.getSurname());
        textViewEmail.setText(tokenDecoded.getSub());
        textViewIndexNumber.setText(tokenDecoded.getIndexNumber());
    }

    private void configureEditUserProfileSaveButton(View view) {
        Button editProfileSaveButton = view.findViewById(R.id.btn_save_edit_profile);
        editProfileSaveButton.setOnClickListener(v -> changeUserAdditionalData());
    }

    private void changeUserAdditionalData() {
        String profilePhone = editTextEditProfilePhone.getText().toString();
        String profileInterests = editTextEditProfileInterests.getText().toString();
        String profileAboutMe = editTextEditProfileAboutMe.getText().toString();

        UserAdditionalDataDto userAdditionalDataDto = createUserAdditionalDataBasedOnEditForm(profilePhone,
                profileInterests,
                profileAboutMe);

        editUserProfileRequest(userAdditionalDataDto);
    }

    private UserAdditionalDataDto createUserAdditionalDataBasedOnEditForm(String userNewPhone,
                                                                          String userNewInterests,
                                                                          String userNewAboutMe) {
        String userPhone;
        Set<String> userInterests;
        String userSurname;

        if (userNewPhone.isEmpty()) {
            userPhone = userAdditionalDataDto.getPhoneNumber();
        } else {
            userPhone = userNewPhone;
        }

        if (userNewInterests.isEmpty()) {
            userInterests = userAdditionalDataDto.getInterests();
        } else {
            userInterests = new HashSet<>(Collections.singletonList(userNewInterests));
        }

        if (userNewAboutMe.isEmpty()) {
            userSurname = userAdditionalDataDto.getAboutMe();
        } else {
            userSurname = userNewAboutMe;
        }

        return new UserAdditionalDataDto(userPhone, userInterests, userSurname);
    }

    private void editUserProfileRequest(UserAdditionalDataDto userAdditionalDataDto) {
        UserProfileService userProfileService = ServiceGenerator.createService
                (UserProfileService.class, TokenStore.getToken(getContext()));

        userProfileService.editUserAdditionalData(userAdditionalDataDto)
                .enqueue(new Callback<UserAdditionalDataDto>() {
                    @Override
                    public void onResponse(@NonNull Call<UserAdditionalDataDto> call,
                                           @NonNull Response<UserAdditionalDataDto> response) {
                        if (response.isSuccessful()) {
                            showEditProfileMessageToast(MessageConstants.USER_PROFILE_UPDATED_SUCCESSFUL_MESSAGE);
                            backToPreviousFragment();
                        } else {
                            showEditProfileMessageToast(MessageConstants.USER_PROFILE_UPDATED_FAILURE_MESSAGE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserAdditionalDataDto> call,
                                          @NonNull Throwable t) {
                        Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
                    }
                });
    }

    private void getUserAdditionalDataRequest() {
        UserProfileService userProfileService = ServiceGenerator.createService(
                UserProfileService.class, TokenStore.getToken(getContext()));

        userProfileService.getUserAdditionalData().enqueue(new Callback<UserAdditionalDataDto>() {
            @Override
            public void onResponse(@NonNull Call<UserAdditionalDataDto> call,
                                   @NonNull Response<UserAdditionalDataDto> response) {
                if (response.isSuccessful()) {
                    userAdditionalDataDto = response.body();
                } else {
                    Log.e(TAG, "GET_ADD_DATA_ERROR");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserAdditionalDataDto> call, @NonNull Throwable t) {
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    private void showEditProfileMessageToast(String message) {
        NotificationManager.showToast(getContext(), message);
    }

    private void backToPreviousFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

}
