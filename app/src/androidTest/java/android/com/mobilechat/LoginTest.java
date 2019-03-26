package android.com.mobilechat;

import android.com.mobilechat.login.LoginFragment;
import android.com.mobilechat.room.RoomsFragment;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class LoginTest {

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule
            = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        LoginFragment fragment = new LoginFragment();
        activityTestRule.getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Test
    public void checkTextDisplayedInDynamicallyCreatedFragment() {
        onView(withId(R.id.edit_text_log_in_email)).check(matches(withHint("Email")));
        onView(withId(R.id.edit_text_log_in_password)).check(matches(withHint("Password")));
    }

    @Test
    public void performLogin() {
        onView(withId(R.id.edit_text_log_in_email)).perform(clearText(), typeText("student@mail" +
                ".mail"));
        onView(withId(R.id.edit_text_log_in_password))
                .perform(closeSoftKeyboard()).perform(clearText(), typeText("password"));
        onView(withId(R.id.btn_log_in)).perform(closeSoftKeyboard()).perform(click());

        activityTestRule.getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, RoomsFragment.newInstance())
                .commit();

        onView(allOf(withId(R.id.recycler_rooms))).check(matches(isDisplayed()));
    }

}
