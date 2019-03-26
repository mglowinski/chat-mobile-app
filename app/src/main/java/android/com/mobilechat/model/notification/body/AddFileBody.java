package android.com.mobilechat.model.notification.body;

import android.com.mobilechat.model.user.UserInfoDto;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class AddFileBody {

    private UserInfoDto author;
    private Long roomId;
    private String storedFilename;
    private String originalFilename;
    private Date timestamp;
}
