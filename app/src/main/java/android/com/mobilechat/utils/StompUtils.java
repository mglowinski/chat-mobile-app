package android.com.mobilechat.utils;

import android.annotation.SuppressLint;
import android.util.Log;
import ua.naiksoftware.stomp.client.StompClient;

public class StompUtils {

    @SuppressLint("CheckResult")
    public static void lifecycle(StompClient stompClient) {
        stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.e("SOCKET_CONNECTION_STATE", "Connect: stomp connection opened!");
                    break;
                case ERROR:
                    Log.e("SOCKET_CONNECTION_STATE", "Connect: Error occured!", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.e("SOCKET_CONNECTION_STATE", "Connect: stomp connection closed!");
                    break;
            }
        });
    }

}
