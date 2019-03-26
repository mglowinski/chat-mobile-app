package android.com.mobilechat.service.requests;

import android.com.mobilechat.model.room.AddManyRoomMembersDto;
import android.com.mobilechat.model.room.ManyMemberAdditionResultDto;
import android.com.mobilechat.model.room.RoomDataFormDto;
import android.com.mobilechat.model.room.RoomDto;
import android.com.mobilechat.model.user.UserInfoDto;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

import static android.com.mobilechat.utils.ConnectionProperties.API_GATEWAY_URL;

public interface RoomService {

    @POST(API_GATEWAY_URL + "/rooms/")
    Call<RoomDto> createRoom(@Body RoomDto roomDto);

    @GET(API_GATEWAY_URL + "/profile/rooms/")
    Call<List<RoomDto>> getRooms();

    @PUT(API_GATEWAY_URL + "/rooms/{roomId}")
    Call<RoomDto> modifyRoom(@Path("roomId") long roomId, @Body RoomDataFormDto roomDataFormDto);

    @GET(API_GATEWAY_URL + "/rooms/{roomId}/members/")
    Call<List<UserInfoDto>> getRoomMembers(@Path("roomId") long roomId);

    @POST(API_GATEWAY_URL + "/rooms/{roomId}/members/many")
    Call<ManyMemberAdditionResultDto> addMembersToRoom(@Path("roomId") long roomId,
                                                       @Body AddManyRoomMembersDto addManyRoomMembersDto);

    @DELETE(API_GATEWAY_URL + "/rooms/{roomId}/members/{memberEmail}")
    Call<Void> removeMemberFromRoom(@Path("roomId") long roomId,
                                    @Path("memberEmail") String memberEmail);

    @GET(API_GATEWAY_URL + "/rooms/{roomId}")
    Call<RoomDto> getRoomById(@Path("roomId") long roomId);

    @PUT(API_GATEWAY_URL + "/rooms/{roomId}/archiving")
    Call<Void> archiveRoom(@Path("roomId") long roomId);

}
