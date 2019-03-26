package android.com.mobilechat.service.requests;

import android.com.mobilechat.model.password_change.PasswordChangeFormDto;
import android.com.mobilechat.model.password_change.ValidationMessageDto;
import android.com.mobilechat.model.user.EditUserDto;
import android.com.mobilechat.model.user.UserAdditionalDataDto;
import android.com.mobilechat.model.user.UserDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

import static android.com.mobilechat.utils.ConnectionProperties.API_GATEWAY_URL;

public interface UserProfileService {

    @PUT(API_GATEWAY_URL + "/profile/password")
    Call<ValidationMessageDto> changePassword(@Body PasswordChangeFormDto passwordChangeFormDto);

    @PUT(API_GATEWAY_URL + "/users/user")
    Call<UserDto> editUserProfile(@Body EditUserDto editUserDto);

    @GET(API_GATEWAY_URL + "/users/user/additionalData")
    Call<UserAdditionalDataDto> getUserAdditionalData();

    @PUT(API_GATEWAY_URL + "/users/user/additionalData")
    Call<UserAdditionalDataDto> editUserAdditionalData(@Body UserAdditionalDataDto userAdditionalDataDto);

}
