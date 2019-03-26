package android.com.mobilechat.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class EditUserDto {

    private String email;
    private String name;
    private String surname;
    private String indexNumber;
}
