package android.com.mobilechat.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
@AllArgsConstructor
public class UserAdditionalDataDto implements Serializable {
    private static final long serialVersionUID = 321251334247672585L;

    private String phoneNumber;
    private Set<String> interests;
    private String aboutMe;
}
