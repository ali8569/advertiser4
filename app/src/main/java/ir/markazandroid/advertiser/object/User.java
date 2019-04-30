package ir.markazandroid.advertiser.object;

import java.io.Serializable;

import ir.markazandroid.advertiser.network.formdata.Form;

/**
 * Coded by Ali on 01/02/2018.
 */

public class User implements Serializable {
    private int userId;
    @Form
    private String username;
    @Form
    private String password;
    @Form(name = "fcmToken")
    private String token;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
