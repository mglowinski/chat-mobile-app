package android.com.mobilechat.adapter;

import android.com.mobilechat.R;
import android.com.mobilechat.model.user.UserInfoDto;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class RoomMembersAdapter extends RecyclerView.Adapter<RoomMembersAdapter.ViewHolder> {

    @Getter
    private List<UserInfoDto> userInfoDtos;

    private ClickListener clickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_room_member, parent, false);
        return new ViewHolder(itemView);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        UserInfoDto userInfoDto = userInfoDtos.get(position);
        holder.textViewRoomMemberEmail.setText("Email: " + userInfoDto.getEmail());
        holder.textViewRoomMemberNameSurname.setText("Name: " + userInfoDto.getName() + " " +
                userInfoDto.getSurname());
    }

    @Override
    public int getItemCount() {
        return userInfoDtos.size();
    }

    public interface ClickListener {
        void onImageClick(int position, View v);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRoomMemberEmail;
        TextView textViewRoomMemberNameSurname;
        ImageView imageViewRemoveUser;

        ViewHolder(View itemView) {
            super(itemView);
            textViewRoomMemberEmail = itemView.findViewById(R.id.text_view_room_member_email);
            textViewRoomMemberNameSurname = itemView.findViewById(R.id.text_view_room_member_name_surname);
            imageViewRemoveUser = itemView.findViewById(R.id.image_remove_user_from_room);

            imageViewRemoveUser.setOnClickListener(v ->
                    clickListener.onImageClick(getAdapterPosition(), v));
        }
    }

}
