package ir.markazandroid.advertiser.object;


import ir.markazandroid.advertiser.Message;
import ir.markazandroid.advertiser.network.JSONParser.annotations.JSON;

/**
 * Created by Ali on 2/17/2019.
 */
public class WebSocketConfiguration extends Message {


    private int pingTime=5_000;
    private int noPongTimeout=15_000;
    private int noMessageTimeout=60_000;



    @Override
    public void setType(String type) {
       // super.setType(type);
    }

    @JSON
    public int getPingTime() {
        return pingTime;
    }

    public void setPingTime(int pingTime) {
        this.pingTime = pingTime;
    }

    @JSON
    public int getNoPongTimeout() {
        return noPongTimeout;
    }

    public void setNoPongTimeout(int noPongTimeout) {
        this.noPongTimeout = noPongTimeout;
    }

    @JSON
    public int getNoMessageTimeout() {
        return noMessageTimeout;
    }

    public void setNoMessageTimeout(int noMessageTimeout) {
        this.noMessageTimeout = noMessageTimeout;
    }
}
