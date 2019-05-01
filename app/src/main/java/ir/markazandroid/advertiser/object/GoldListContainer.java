package ir.markazandroid.advertiser.object;


import java.io.Serializable;
import java.util.List;

import ir.markazandroid.advertiser.network.JSONParser.annotations.JSON;

/**
 * Created by Ali on 12/2/2018.
 */
public class GoldListContainer implements Serializable {

    private List<GoldEntity> golds;
    private int userId;
    private long lastUpdate;

    @JSON(classType = JSON.CLASS_TYPE_ARRAY, clazz = GoldEntity.class)
    public List<GoldEntity> getGolds() {
        return golds;
    }

    public void setGolds(List<GoldEntity> golds) {
        this.golds = golds;
    }

    @JSON
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @JSON
    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoldListContainer)) return false;
        GoldListContainer that = (GoldListContainer) o;
        return userId == that.userId &&
                lastUpdate == that.lastUpdate;
    }
}
