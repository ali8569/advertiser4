package ir.markazandroid.advertiser.object;

import java.io.Serializable;

import ir.markazandroid.advertiser.network.JSONParser.annotations.JSON;

/**
 * Coded by Ali on 06/02/2018.
 */

public class Phone implements Serializable {

    public static final int STATUS_NO_LOGIN=-2;
    public static final int STATUS_NOT_ASSIGNED=-3;
    public static final int STATUS_ACTIVE=2;
    public static final int STATUS_DISABLED=-1;

    private int phoneId;
    private String uuid;
    private String name;
    private String password;
    private int status;


    @JSON
    public int getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(int phoneId) {
        this.phoneId = phoneId;
    }

    @JSON
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @JSON
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JSON
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JSON
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
