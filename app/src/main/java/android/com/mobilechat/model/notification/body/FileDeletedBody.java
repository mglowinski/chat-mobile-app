package android.com.mobilechat.model.notification.body;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileDeletedBody {

    private final Long roomId;
    private final String storedFilename;
    private final String originalFilename;
}
