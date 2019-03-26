package android.com.mobilechat.service.requests;

import android.com.mobilechat.model.user.UserInfoDto;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

import static android.com.mobilechat.utils.ConnectionProperties.API_GATEWAY_URL;

public interface UserService {

    @GET(API_GATEWAY_URL + "/users/")
    Call<List<UserInfoDto>> getAllUsers();
}
