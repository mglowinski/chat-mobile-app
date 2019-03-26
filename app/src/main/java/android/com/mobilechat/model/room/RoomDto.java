package android.com.mobilechat.model.room;

import android.com.mobilechat.model.user.UserInfoDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class RoomDto implements Serializable {
    private static final long serialVersionUID = -4220537434293212171L;

    private Long id;
    private String name;
    private String description;
    private UserInfoDto creator;
    private boolean archived;
    private int membersCount;

    public RoomDto(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
