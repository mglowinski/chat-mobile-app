package android.com.mobilechat.model.room;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class AddManyRoomMembersDto implements Serializable {
    private static final long serialVersionUID = 7734482034886222075L;

    private List<String> membersEmails;
}
