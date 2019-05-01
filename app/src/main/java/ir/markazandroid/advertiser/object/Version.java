package ir.markazandroid.advertiser.object;

import java.io.Serializable;

import ir.markazandroid.advertiser.network.JSONParser.annotations.JSON;

/**
 * Coded by Ali on 5/24/2018.
 */
public class Version implements Serializable {
    private int version;
    private String url;

    @JSON
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @JSON
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
