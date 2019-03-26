package android.com.mobilechat.utils;

import android.widget.EditText;

public class Validator {

    public static boolean loginValidate(EditText emailEditText, EditText passwordEditText) {
        boolean emailValid = isEmailValid(emailEditText);
        boolean passwordValid = isPasswordValid(passwordEditText);

        return emailValid && passwordValid;
    }

    private static boolean isEmailValid(EditText emailEditText) {
        String email = emailEditText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(MessageConstants.LOG_IN_EMAIL_ERROR_MESSAGE);
            return false;
        } else {
            emailEditText.setError(null);
        }

        return true;
    }

    private static boolean isPasswordValid(EditText passwordEditText) {
        String password = passwordEditText.getText().toString();

        if (password.isEmpty()) {
            passwordEditText.setError(MessageConstants.LOG_IN_PASSWORD_ERROR_MESSAGE);
            return false;
        } else {
            passwordEditText.setError(null);
        }

        return true;
    }

    public static boolean changePasswordValidate(EditText editTextCurrentPassword,
                                                 EditText editTextNewPassword) {
        boolean currentPasswordValid = isCurrentPasswordValid(editTextCurrentPassword);
        boolean newPasswordValid = isNewPasswordValid(editTextNewPassword);

        return currentPasswordValid && newPasswordValid;
    }

    private static boolean isCurrentPasswordValid(EditText editTextCurrentPassword) {
        String currentPassword = editTextCurrentPassword.getText().toString();

        if (currentPassword.isEmpty() || currentPassword.length() < 3
                || currentPassword.length() > 61) {
            editTextCurrentPassword.setError(MessageConstants.WRONG_CURRENT_PASSWORD_FORMAT_ERROR_MESSAGE);
            return false;
        } else {
            editTextCurrentPassword.setError(null);
        }

        return true;
    }

    private static boolean isNewPasswordValid(EditText editTextNewPassword) {
        String newPassword = editTextNewPassword.getText().toString();

        if (newPassword.isEmpty() || newPassword.length() < 3
                || newPassword.length() > 61) {
            editTextNewPassword.setError(MessageConstants.WRONG_NEW_PASSWORD_FORMAT_ERROR_MESSAGE);
            return false;
        } else {
            editTextNewPassword.setError(null);
        }

        return true;
    }

    public static boolean modifyRoomValidate(EditText editTextRoomName,
                                             EditText editTextRoomDescription) {
        boolean roomNameValid = isRoomNameValid(editTextRoomName);
        boolean roomDescriptionValid = isRoomDescriptionValid(editTextRoomDescription);

        return roomNameValid && roomDescriptionValid;
    }

    private static boolean isRoomNameValid(EditText editTextRoomName) {
        String roomName = editTextRoomName.getText().toString();

        if (roomName.isEmpty() || roomName.length() < 3
                || roomName.length() > 61) {
            editTextRoomName.setError(MessageConstants.WRONG_ROOM_NAME_ERROR_MESSAGE);
            return false;
        } else {
            editTextRoomName.setError(null);
        }

        return true;
    }

    private static boolean isRoomDescriptionValid(EditText editTextRoomDescription) {
        String newPassword = editTextRoomDescription.getText().toString();

        if (newPassword.isEmpty() || newPassword.length() < 3
                || newPassword.length() > 61) {
            editTextRoomDescription.setError(MessageConstants.WRONG_ROOM_DESCRIPTION_ERROR_MESSAGE);
            return false;
        } else {
            editTextRoomDescription.setError(null);
        }

        return true;
    }

}
