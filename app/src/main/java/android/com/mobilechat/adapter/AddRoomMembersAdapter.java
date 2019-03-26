package android.com.mobilechat.adapter;

import android.com.mobilechat.R;
import android.com.mobilechat.model.user.UserInfoDto;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class AddRoomMembersAdapter extends RecyclerView.Adapter<AddRoomMembersAdapter.ViewHolder> {

    @Getter
    private List<UserInfoDto> userInfoDtos;

    private ClickListener clickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_add_room_member, parent, false);
        return new ViewHolder(itemView);
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserInfoDto userInfoDto = userInfoDtos.get(position);
        holder.textViewUserInfoDtoEmail.setText(userInfoDto.getEmail());
    }

    @Override
    public int getItemCount() {
        return userInfoDtos.size();
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewUserInfoDtoEmail;

        ViewHolder(View itemView) {
            super(itemView);
            textViewUserInfoDtoEmail = itemView.findViewById(R.id.text_view_add_room_member_email);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

}
