package android.com.mobilechat.adapter;

import android.com.mobilechat.R;
import android.com.mobilechat.model.room.RoomDto;
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
public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.ViewHolder> {

    @Getter
    private List<RoomDto> roomDtos;

    private ClickListener clickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_room, parent, false);
        return new ViewHolder(itemView);
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomDto roomDto = roomDtos.get(position);
        holder.textViewRoomName.setText("Name: " + setRoomName(roomDto));
        holder.textViewRoomDescription.setText("Description: " + roomDto.getDescription());
        holder.textViewRoomMembersCount.setText("Members count: " + roomDto.getMembersCount());
    }

    private String setRoomName(RoomDto roomDto) {
        if (roomDto.isArchived()) {
            return roomDto.getName() + " - ARCHIVED";
        } else {
            return roomDto.getName();
        }
    }
    
    @Override
    public int getItemCount() {
        return roomDtos.size();
    }

    public interface ClickListener {
        void onItemClick(int position, View v);

        void onImageClick(int position, View v);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewRoomName;
        TextView textViewRoomDescription;
        TextView textViewRoomMembersCount;
        ImageView imageViewAddUsersToRoom;

        ViewHolder(View itemView) {
            super(itemView);
            textViewRoomName = itemView.findViewById(R.id.text_view_room_name);
            textViewRoomDescription = itemView.findViewById(R.id.text_view_room_description);
            textViewRoomMembersCount = itemView.findViewById(R.id.text_view_members_count);
            imageViewAddUsersToRoom = itemView.findViewById(R.id.image_add_users_to_room);

            imageViewAddUsersToRoom.setOnClickListener(v ->
                    clickListener.onImageClick(getAdapterPosition(), v));
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

}
