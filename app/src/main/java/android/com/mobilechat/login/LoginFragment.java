package android.com.mobilechat.login;

import android.app.ProgressDialog;
import android.com.mobilechat.R;
import android.com.mobilechat.model.token.Token;
import android.com.mobilechat.model.user.UserCredentialsDto;
import android.com.mobilechat.room.RoomsFragment;
import android.com.mobilechat.service.ServiceGenerator;
import android.com.mobilechat.service.requests.LoginService;
import android.com.mobilechat.utils.*;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import lombok.NoArgsConstructor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@NoArgsConstructor
public class LoginFragment extends Fragment {

    public static final String TAG = LoginFragment.class.getName();

    private ProgressDialog progressDialog;
    private CheckBox rememberMeCheckBox;
    private EditText emailEditText;
    private EditText passwordEditText;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configureEditTexts(view);
        configureProgressDialog();
        configureLogInButton(view);
        configureCheckBox(view);
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
    public void onResume() {
        super.onResume();

        if (rememberMeCheckBox.isChecked()) {
            completeCredentialsRememberedValues();
        } else {
            clearEditTextFields();
        }
    }

    private void clearEditTextFields() {
        emailEditText.getText().clear();
        passwordEditText.getText().clear();
    }

    private void configureProgressDialog() {
        progressDialog = new ProgressDialog(getContext(), R.style.AppTheme_Dark_Dialog);
    }

    private void configureCheckBox(View view) {
        rememberMeCheckBox = view.findViewById(R.id.check_box_remember_me);
        rememberMeCheckBox.setChecked(RememberMeStore.isCheckBoxSelected(getContext()));
    }

    private void completeCredentialsRememberedValues() {
        emailEditText.setText(RememberMeStore.getRememberedEmail(getContext()));
        passwordEditText.setText(RememberMeStore.getRememberedPassword(getContext()));
    }

    private void configureEditTexts(View view) {
        emailEditText = view.findViewById(R.id.edit_text_log_in_email);
        passwordEditText = view.findViewById(R.id.edit_text_log_in_password);
    }

    private void configureLogInButton(View view) {
        Button logInButton = view.findViewById(R.id.btn_log_in);
        logInButton.setOnClickListener(v -> logIn(emailEditText, passwordEditText));
    }

    private void logIn(EditText emailEditText, EditText passwordEditText) {
        if (Validator.loginValidate(emailEditText, passwordEditText)) {
            UserCredentialsDto userCredentialsDto = createUser(emailEditText, passwordEditText);
            saveCheckBoxSelection();
            saveCredentials(userCredentialsDto);
            authenticateUser(userCredentialsDto);
        }
    }

    private void saveCheckBoxSelection() {
        RememberMeStore.saveCheckBoxSelection(getContext(), rememberMeCheckBox.isChecked());
    }

    private void saveCredentials(UserCredentialsDto userCredentialsDto) {
        if (rememberMeCheckBox.isChecked()) {
            RememberMeStore.saveRememberedEmail(getContext(), userCredentialsDto.getEmail());
            RememberMeStore.saveRememberedPassword(getContext(), userCredentialsDto.getPassword());
        }
    }

    private UserCredentialsDto createUser(EditText emailEditText, EditText passwordEditText) {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        return new UserCredentialsDto(email, password);
    }

    private void authenticateUser(UserCredentialsDto userCredentialsDto) {
        startProgressDialog();

        LoginService loginService = ServiceGenerator.createService(LoginService.class);
        loginService.logIn(userCredentialsDto).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(@NonNull Call<Token> call, @NonNull Response<Token> response) {
                if (response.isSuccessful()) {
                    saveToken(response.body());
                    loadRoomsFragment();
                    stopProgressDialog();
                } else {
                    loginFailureInformation();
                    stopProgressDialog();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Token> call, @NonNull Throwable t) {
                stopProgressDialog();
                Log.e(MessageConstants.REST_ERROR_MESSAGE, t.getMessage());
            }
        });
    }

    private void saveToken(Token token) {
        if (token != null) {
            TokenStore.saveToken(getContext(), token.getToken());
        }
    }

    private void loginFailureInformation() {
        NotificationManager.showToast(getContext(), MessageConstants.LOG_IN_FAILED_ERROR_MESSAGE);
    }

    private void startProgressDialog() {
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
    }

    private void stopProgressDialog() {
        progressDialog.dismiss();
    }

    private void loadRoomsFragment() {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, RoomsFragment.newInstance(), RoomsFragment.TAG)
                    .addToBackStack(null)
                    .commit();
        }
    }

}
