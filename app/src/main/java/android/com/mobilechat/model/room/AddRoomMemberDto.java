package android.com.mobilechat.model.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddRoomMemberDto implements Serializable {
    private static final long serialVersionUID = 2603534915939113750L;

    private String email;
    private boolean selected;
}
