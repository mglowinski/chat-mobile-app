package android.com.mobilechat.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class UserCredentialsDto implements Serializable {
    private static final long serialVersionUID = 4501805500978983861L;

    private String email;
    private String password;
}
