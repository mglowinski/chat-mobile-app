package android.com.mobilechat.model.user;

import lombok.Data;

@Data
public class UserDto {

    private String email;
    private String name;
    private String surname;
    private String indexNumber;
    private boolean enabled = true;
}
