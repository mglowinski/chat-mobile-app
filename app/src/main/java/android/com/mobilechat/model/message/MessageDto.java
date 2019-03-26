package android.com.mobilechat.model.message;

import android.com.mobilechat.model.user.UserInfoDto;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
public class MessageDto implements Serializable {
    private static final long serialVersionUID = 1970783545021122425L;

    private final Long messageId;
    private final UserInfoDto author;
    private final Long roomId;
    private final Date timestamp;
    private String content;
    private MessageType type;
    private boolean edited;
}
