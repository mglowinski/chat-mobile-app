package android.com.mobilechat.model.notification;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Notification<T> {

    private final NotificationType type;
    private final T body;
}
