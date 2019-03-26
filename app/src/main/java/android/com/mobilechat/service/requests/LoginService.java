package android.com.mobilechat.service.requests;

import android.com.mobilechat.model.token.Token;
import android.com.mobilechat.model.user.UserCredentialsDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import static android.com.mobilechat.utils.ConnectionProperties.API_GATEWAY_URL;

public interface LoginService {

    @POST(API_GATEWAY_URL + "/login")
    Call<Token> logIn(@Body UserCredentialsDto userCredentialsDto);
}
