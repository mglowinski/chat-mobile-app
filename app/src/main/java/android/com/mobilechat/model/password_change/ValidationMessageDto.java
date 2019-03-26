package android.com.mobilechat.model.password_change;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationMessageDto {

    private String messageContent;
    private String fieldName;
}
