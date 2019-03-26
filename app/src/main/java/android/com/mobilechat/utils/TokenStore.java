package android.com.mobilechat.utils;

import android.com.mobilechat.model.token.TokenDecoded;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;

public class TokenStore {

    private static final String TOKEN_KEY = "token";

    public static String getToken(Context context) {
        String value = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null) {
            value = preferences.getString(TOKEN_KEY, null);
        }
        return value;
    }

    public static void saveToken(Context context, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences != null && !TextUtils.isEmpty(TOKEN_KEY)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(TOKEN_KEY, value);
            editor.apply();
        }
    }

    public static TokenDecoded decodeToken(String tokenEncoded) {
        TokenDecoded tokenDecoded = null;
        try {
            String[] secondTokenPart = tokenEncoded.split("\\.");
            String tokenDecodedInJsonString = getJsonStringFromEncodeValue(secondTokenPart[1]);
            tokenDecoded = parseJsonToken(tokenDecodedInJsonString);
        } catch (UnsupportedEncodingException e) {
            Log.e("Error", e.getMessage());
        }
        return tokenDecoded;
    }

    private static String getJsonStringFromEncodeValue(String strEncoded)
            throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }

    private static TokenDecoded parseJsonToken(String token) {
        Gson gson = new Gson();
        return gson.fromJson(token, TokenDecoded.class);
    }

}
