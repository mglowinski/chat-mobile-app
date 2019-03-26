package android.com.mobilechat.model.notification.body;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageEditedBody {

    private final Long messageId;
    private final Long roomId;
    private final String messageNewContent;
}
