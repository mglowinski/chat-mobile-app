package android.com.mobilechat.model.notification.body;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BecameRoomMemberBody {

    private final Long roomId;
    private final String roomName;
    private final String roomDescription;
    private final int roomMembersCount;
}
