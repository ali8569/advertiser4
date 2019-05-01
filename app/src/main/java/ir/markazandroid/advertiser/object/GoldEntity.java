package ir.markazandroid.advertiser.object;

import java.io.Serializable;

import ir.markazandroid.advertiser.network.JSONParser.annotations.JSON;
import ir.markazandroid.advertiser.network.formdata.Form;


/**
 * Created by Ali on 12/2/2018.
 */
public class GoldEntity implements Serializable {
    private int userId;
    private long goldId;

    @Form
    private int weight;
    @Form
    private String name;
    @Form
    private String details = "";
    private long createTime;

    private String photoUrl;

    @Form(type = Form.FILE)
    private String photoFile;


    @JSON
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @JSON
    public long getGoldId() {
        return goldId;
    }

    public void setGoldId(long goldId) {
        this.goldId = goldId;
    }

    @JSON
    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @JSON
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JSON
    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }


    @JSON
    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @JSON
    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoFile() {
        return photoFile;
    }

    public void setPhotoFile(String photoFile) {
        this.photoFile = photoFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoldEntity)) return false;

        GoldEntity that = (GoldEntity) o;

        return goldId == that.goldId;
    }

    @Override
    public int hashCode() {
        return (int) (goldId ^ (goldId >>> 32));
    }
}
