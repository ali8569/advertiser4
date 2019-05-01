package ir.markazandroid.advertiser.object;


import java.io.Serializable;

import ir.markazandroid.advertiser.network.formdata.Form;

/**
 * Created by Ali on 3/25/2019.
 */
public class ScreenShot implements Serializable {

    @Form(type = Form.FILE)
    private String file;
    private String url;
    @Form
    private long timestamp;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
