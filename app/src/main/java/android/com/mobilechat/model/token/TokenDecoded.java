package android.com.mobilechat.model.token;

import lombok.Data;

@Data
public class TokenDecoded {

    private String sub;
    private String name;
    private String surname;
    private String indexNumber;
    private String role;
    private boolean enabled;
}
