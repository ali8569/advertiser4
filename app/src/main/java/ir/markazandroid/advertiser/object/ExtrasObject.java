package ir.markazandroid.advertiser.object;


import java.io.Serializable;
import java.util.ArrayList;

import ir.markazandroid.advertiser.network.JSONParser.annotations.JSON;

/**
 * Created by Ali on 8/28/2018.
 */
public class ExtrasObject implements Serializable {

    //Doctor
    private String resumePhotoUrl;
    private String rssFeedUrl;
    private int rssFeedTextSize = 21;
    private String tvUrl;

    //4 parts
    private String weatherUrl;
    private String currencyUrl;

    //2 Parts
    private String webViewUrl;

    // special
    private ArrayList<Content> contents1;
    private ArrayList<Content> contents2;
    private ArrayList<Content> contents3;

    //Gold
    private String goldTitle;

    @JSON
    public String getWebViewUrl() {
        return webViewUrl;
    }

    public void setWebViewUrl(String webViewUrl) {
        this.webViewUrl = webViewUrl;
    }


    @JSON
    public String getResumePhotoUrl() {
        return resumePhotoUrl;
    }

    public void setResumePhotoUrl(String resumePhotoUrl) {
        this.resumePhotoUrl = resumePhotoUrl;
    }

    @JSON
    public String getRssFeedUrl() {
        return rssFeedUrl;
    }

    public void setRssFeedUrl(String rssFeedUrl) {
        this.rssFeedUrl = rssFeedUrl;
    }

    @JSON
    public String getTvUrl() {
        return tvUrl;
    }

    public void setTvUrl(String tvUrl) {
        this.tvUrl = tvUrl;
    }

    @JSON
    public String getWeatherUrl() {
        return weatherUrl;
    }

    public void setWeatherUrl(String weatherUrl) {
        this.weatherUrl = weatherUrl;
    }

    @JSON
    public String getCurrencyUrl() {
        return currencyUrl;
    }

    public void setCurrencyUrl(String currencyUrl) {
        this.currencyUrl = currencyUrl;
    }

    @JSON
    public int getRssFeedTextSize() {
        return rssFeedTextSize;
    }

    public void setRssFeedTextSize(int rssFeedTextSize) {
        this.rssFeedTextSize = rssFeedTextSize;
    }

    @JSON(classType = JSON.CLASS_TYPE_ARRAY, clazz = Content.class)
    public ArrayList<Content> getContents1() {
        return contents1;
    }

    public void setContents1(ArrayList<Content> contents1) {
        this.contents1 = contents1;
    }

    @JSON(classType = JSON.CLASS_TYPE_ARRAY, clazz = Content.class)
    public ArrayList<Content> getContents2() {
        return contents2;
    }

    public void setContents2(ArrayList<Content> contents2) {
        this.contents2 = contents2;
    }

    @JSON(classType = JSON.CLASS_TYPE_ARRAY, clazz = Content.class)
    public ArrayList<Content> getContents3() {
        return contents3;
    }

    public void setContents3(ArrayList<Content> contents3) {
        this.contents3 = contents3;
    }

    @JSON
    public String getGoldTitle() {
        return goldTitle;
    }

    public void setGoldTitle(String goldTitle) {
        this.goldTitle = goldTitle;
    }
}
