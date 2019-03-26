package android.com.mobilechat;

import android.com.mobilechat.login.LoginFragment;
import android.com.mobilechat.room.RoomsFragment;
import android.com.mobilechat.utils.MessageConstants;
import android.com.mobilechat.utils.NotificationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextThemeWrapper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, LoginFragment.newInstance(), LoginFragment.TAG)
                .commit();
    }

    @Override
    public void onBackPressed() {
        RoomsFragment fragment =
                (RoomsFragment) getSupportFragmentManager().findFragmentByTag(RoomsFragment.TAG);
        if (fragment != null && fragment.isVisible()) {
            createLogoutDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void createLogoutDialog() {
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_Dark_Dialog))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Application")
                .setMessage("Are you sure you want to close this application?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    getSupportFragmentManager().popBackStack();
                    NotificationManager.showToast(this, MessageConstants.LOGOUT_SUCCESSFUL_MESSAGE);
                })
                .setNegativeButton("No", null)
                .show();
    }

}

