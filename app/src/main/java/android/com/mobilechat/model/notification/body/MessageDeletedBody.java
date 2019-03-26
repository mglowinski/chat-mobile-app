package android.com.mobilechat.model.notification.body;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageDeletedBody {

    private final Long messageId;
    private final Long roomId;
}
