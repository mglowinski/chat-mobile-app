package android.com.mobilechat.model.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDataFormDto implements Serializable {
    private static final long serialVersionUID = -5519096934637581238L;

    private String name;
    private String description;
}
