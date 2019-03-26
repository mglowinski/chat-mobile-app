package android.com.mobilechat.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto implements Serializable {
    private static final long serialVersionUID = -8258896336442234629L;

    private String email;
    private String name;
    private String surname;
    private boolean selected;
}
