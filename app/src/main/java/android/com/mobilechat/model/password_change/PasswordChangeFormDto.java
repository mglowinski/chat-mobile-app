package android.com.mobilechat.model.password_change;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class PasswordChangeFormDto implements Serializable {
    private static final long serialVersionUID = 924950916368455337L;

    private String currentPassword;
    private String newPassword;
}
