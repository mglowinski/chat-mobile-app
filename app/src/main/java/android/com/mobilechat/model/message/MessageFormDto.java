package android.com.mobilechat.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class MessageFormDto implements Serializable {
    private static final long serialVersionUID = -4525878310615585225L;

    private String content;
}
