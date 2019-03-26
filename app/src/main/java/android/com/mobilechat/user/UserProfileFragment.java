package android.com.mobilechat.user;

import android.com.mobilechat.R;
import android.com.mobilechat.model.password_change.PasswordChangeFormDto;
import android.com.mobilechat.model.password_change.ValidationMessageDto;
import android.com.mobilechat.model.token.TokenDecoded;
import android.com.mobilechat.model.user.UserAdditionalDataDto;
import android.com.mobilechat.service.ServiceGenerator;
import android.com.mobilechat.service.requests.UserProfileService;
import android.com.mobilechat.utils.MessageConstants;
import android.com.mobilechat.utils.NotificationManager;
import android.com.mobilechat.utils.TokenStore;
import android.com.mobilechat.utils.Validator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import lombok.NoArgsConstructor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@NoArgsConstructor
public class UserProfileFragment extends Fragment {

    public static final String TAG = UserProfileFragment.class.getName();

    private AlertDialog alertDialog;
    private ProgressBar progressBar;
    private TextView textViewWrongPasswordErrorMessage;

    private TextView textViewPhone;
    private TextView textViewInterests;
    private TextView textViewAboutMe;

    public static UserProfileFragment newInstance() {
        return new UserProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configureToolbar(view);
        configureEditProfileButton(view);
        configureChangePasswordButton(view);
        configureTextsView(view);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_profile_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                createLogoutDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createLogoutDialog() {
        new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme_Dark_Dialog))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Application")
                .setMessage("Are you sure you want to close this application?")
                .setPositiveButton("Yes", (dialog, which) -> goToLogin())
                .setNegativeButton("No", null)
                .show();
    }

    private void goToLogin() {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
            fragmentManager.popBackStack();
            NotificationManager.showToast(getContext(), MessageConstants.LOGOUT_SUCCESSFUL_MESSAGE);
        }
    }

    private void configureToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar_fragment_profile);
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        }
    }

    private void configureTextsView(View view) {
        TokenDecoded tokenDecoded = TokenStore.decodeToken(TokenStore.getToken(getContext()));

        TextView textViewName = view.findViewById(R.id.text_view_edit_profile_name);
        TextView textViewEmail = view.findViewById(R.id.text_view_profile_email);
        TextView textViewIndexNumber = view.findViewById(R.id.text_view_profile_index_number);

        textViewName.setText(tokenDecoded.getName() + " " + tokenDecoded.getSurname());
        textViewEmail.setText(tokenDecoded.getSub());
        textViewIndexNumber.setText(tokenDecoded.getIndexNumber());

        textViewPhone = view.findViewById(R.id.text_view_profile_phone);
        textViewInterests = view.findViewById(R.id.text_view_profile_interests);
        textViewAboutMe = view.findViewById(R.id.text_view_profile_about_me);
        textViewAboutMe.setMovementMethod(new ScrollingMovementMethod());
    }

    private void configureEditProfileButton(View view) {
        Button logInButton = view.findViewById(R.id.btn_edit_profile);
        logInButton.setOnClickListener(v -> loadEditUserProfileFragment());
    }

    private void loadEditUserProfileFragment() {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            EditUserProfileFragment editUserProfileFragment = EditUserProfileFragment.newInstance();

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, editUserProfileFragment, EditUserProfileFragment.TAG)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void configureChangePasswordButton(View view) {
        Button changePasswordButton = view.findViewById(R.id.btn_change_password);
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void showChangePasswordDialog() {
        View view = configureChangePasswordDialogView();

        if (view != null) {
            AlertDialog.Builder builder = createDialogBuilder(view);
            configureDialogLayoutComponents(view);
            setDialogBuilderButtons(builder);
            showDialog(builder);
            configureSubmitChangePasswordButton(view);
        }
    }

    private View configureChangePasswordDialogView() {
        if (getActivity() != null) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            return inflater.inflate(R.layout.dialog_change_password, null);
        }
        return null;
    }

    private AlertDialog.Builder createDialogBuilder(View view) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(
                        new ContextThemeWrapper(getActivity(), R.style.AppTheme_Dark_Dialog));
        builder.setView(view);
        builder.setTitle("Change Password");
        return builder;
    }

    private void configureDialogLayoutComponents(View view) {
        textViewWrongPasswordErrorMessage = view.findViewById(R.id.tv_message_wrong_password);
        progressBar = view.findViewById(R.id.progress);
    }

    private void setDialogBuilderButtons(AlertDialog.Builder builder) {
        builder.setPositiveButton("Change Password", null);
        builder.setNegativeButton("Cancel", null);
    }

    private void configureSubmitChangePasswordButton(View view) {
        EditText editTextOldPassword = view.findViewById(R.id.dialog_old_password);
        EditText editTextNewPassword = view.findViewById(R.id.dialog_new_password);

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String oldPassword = editTextOldPassword.getText().toString();
            String newPassword = editTextNewPassword.getText().toString();

            if (Validator.changePasswordValidate(editTextOldPassword, editTextNewPassword)) {
                progressBar.setVisibility(View.VISIBLE);
                changePasswordRequest(oldPassword, newPassword);
            }
        });
    }

    private void changePasswordRequest(String oldPassword, String newPassword) {
        PasswordChangeFormDto passwordChangeFormDto =
                new PasswordChangeFormDto(oldPassword, newPassword);

        UserProfileService userProfileService = ServiceGenerator.createService(
                UserProfileService.class, TokenStore.getToken(getContext()));

        userProfileService.changePassword(passwordChangeFormDto).enqueue(new Callback<ValidationMessageDto>() {
            @Override
            public void onResponse(@NonNull Call<ValidationMessageDto> call,
                                   @NonNull Response<ValidationMessageDto> response) {
                stopProgressBar();
                if (response.isSuccessful()) {
                    dismissDialog();
                    NotificationManager.showToast(getContext(), MessageConstants.PASSWORD_SUCCESSFUL_CHANGED_MESSAGE);
                } else {
                    textViewWrongPasswordErrorMessage.setVisibility(View.VISIBLE);
                    textViewWrongPasswordErrorMessage.setText(MessageConstants.WRONG_CURRENT_PASSWORD_ERROR_MESSAGE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ValidationMessageDto> call, @NonNull Throwable t) {
                stopProgressBar();
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
                    setTextViewWithData(response.body());
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

    private void setTextViewWithData(UserAdditionalDataDto userAdditionalDataDto) {
        textViewPhone.setText(userAdditionalDataDto.getPhoneNumber());
        textViewInterests.setText(userAdditionalDataDto.getInterests().toString());
        textViewAboutMe.setText(userAdditionalDataDto.getAboutMe());
    }

    private void stopProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void showDialog(AlertDialog.Builder builder) {
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void dismissDialog() {
        alertDialog.dismiss();
    }

}
