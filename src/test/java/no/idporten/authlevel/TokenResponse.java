package no.idporten.authlevel;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class TokenResponse {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("id_token")
    private String idToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("expires_in")
    private long expiresIn;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("scope")
    private String scope;

    public String getAccessToken() {
        return accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

}
