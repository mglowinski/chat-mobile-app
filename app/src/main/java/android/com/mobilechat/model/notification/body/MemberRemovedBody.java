package android.com.mobilechat.model.notification.body;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberRemovedBody {

    private final Long roomId;
    private final String memberEmail;
}
