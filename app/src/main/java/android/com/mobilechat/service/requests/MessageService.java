package android.com.mobilechat.service.requests;

import android.com.mobilechat.model.message.MessageDto;
import android.com.mobilechat.model.message.MessageFormDto;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

import static android.com.mobilechat.utils.ConnectionProperties.API_GATEWAY_URL;

public interface MessageService {

    @GET(API_GATEWAY_URL + "/rooms/{roomId}/history/{messagesLimit}")
    Call<List<MessageDto>> getRoomMessages(@Path("roomId") long roomId,
                                           @Path("messagesLimit") long messagesLimit);

    @POST(API_GATEWAY_URL + "/rooms/{roomId}/messages/")
    Call<Void> createMessage(@Path("roomId") long roomId, @Body MessageFormDto messageFormDto);

    @PUT(API_GATEWAY_URL + "/messages/{messageId}")
    Call<Void> modifyMessage(@Path("messageId") long messageId, @Body MessageFormDto
            messageFormDto);

    @DELETE(API_GATEWAY_URL + "/messages/{messageId}")
    Call<Void> deleteMessage(@Path("messageId") long messageId);

}
