package android.com.mobilechat.web_socket;

import android.annotation.SuppressLint;
import android.com.mobilechat.utils.ConnectionProperties;
import android.com.mobilechat.utils.StompUtils;
import lombok.Data;
import okhttp3.WebSocket;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompHeader;
import ua.naiksoftware.stomp.client.StompClient;

import java.util.List;

@Data
public class WebSocketClient {

    private Callback callback;
    private StompClient stompClient;

    public WebSocketClient(Callback callback) {
        this.callback = callback;
    }

    public void initConnection(List<StompHeader> headers) {
        stompClient = Stomp.over(WebSocket.class, ConnectionProperties.WEB_SOCKET_SERVICE_URL);
        stompClient.connect(headers);
        StompUtils.lifecycle(stompClient);
    }

    public void closeConnection() {
        stompClient.disconnect();
    }

    @SuppressLint("CheckResult")
    public void subscribeOnMessage(String subscribeTopicPath) {
        stompClient.topic(subscribeTopicPath)
                .subscribe(message -> callback.onMessageReceived(message.getPayload()));
    }

    public interface Callback {
        void onMessageReceived(String jsonMessageForm);
    }

}


