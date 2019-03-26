package android.com.mobilechat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class RoomNotificationStore {

    private static final String ROOMS_MARKED_KEY = "rooms_marked";

    public static Set<String> getRoomsSet(Context context) {
        Set<String> rooms = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            rooms = preferences.getStringSet(ROOMS_MARKED_KEY, new HashSet<>());
        }
        return rooms;
    }

    public static void saveRoomsSet(Context context, Set<String> roomsSet) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putStringSet(ROOMS_MARKED_KEY, roomsSet);
            editor.apply();
        }
    }

}
