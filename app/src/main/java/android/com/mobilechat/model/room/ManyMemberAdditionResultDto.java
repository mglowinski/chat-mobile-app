package android.com.mobilechat.model.room;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ManyMemberAdditionResultDto implements Serializable {
    private static final long serialVersionUID = -1028560780282256246L;

    private List<String> added = new ArrayList<>();
    private List<String> notAdded = new ArrayList<>();
}
