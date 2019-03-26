package android.com.mobilechat.service.requests;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import static android.com.mobilechat.utils.ConnectionProperties.API_GATEWAY_URL;

public interface FileService {

    @Multipart
    @POST(API_GATEWAY_URL + "/rooms/{roomId}/files/")
    Call<Void> uploadFile(@Path("roomId") long roomId,
                          @Part MultipartBody.Part file);

    @GET(API_GATEWAY_URL + "/files/{fileName}")
    Call<ResponseBody> getFile(@Path("fileName") String fileName);

    @DELETE(API_GATEWAY_URL + "/files/{fileName}")
    Call<Void> deleteFile(@Path("fileName") String fileName);

}
