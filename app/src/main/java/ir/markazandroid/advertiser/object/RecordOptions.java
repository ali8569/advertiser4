package ir.markazandroid.advertiser.object;


import ir.markazandroid.advertiser.network.JSONParser.annotations.JSON;

/**
 * Created by Ali on 7/16/2018.
 */
public class RecordOptions {

    private boolean logoVisible = true;
    private boolean clockVisible = false;
    private boolean subtitleVisible = true;
    private boolean dateVisible = false;

    private int logoSize = 52;
    private int timeTextSize = 21;
    private String calendarType = "persian";

    @JSON
    public boolean getLogoVisible() {
        return logoVisible;
    }

    public void setLogoVisible(boolean logoVisible) {
        this.logoVisible = logoVisible;
    }

    @JSON
    public boolean getClockVisible() {
        return clockVisible;
    }

    public void setClockVisible(boolean clockVisible) {
        this.clockVisible = clockVisible;
    }

    @JSON
    public boolean getSubtitleVisible() {
        return subtitleVisible;
    }

    public void setSubtitleVisible(boolean subtitleVisible) {
        this.subtitleVisible = subtitleVisible;
    }

    @JSON
    public boolean getDateVisible() {
        return dateVisible;
    }

    public void setDateVisible(boolean dateVisible) {
        this.dateVisible = dateVisible;
    }

    @JSON
    public int getLogoSize() {
        return logoSize;
    }

    public void setLogoSize(int logoSize) {
        this.logoSize = logoSize;
    }

    @JSON
    public int getTimeTextSize() {
        return timeTextSize;
    }

    public void setTimeTextSize(int timeTextSize) {
        this.timeTextSize = timeTextSize;
    }

    @JSON
    public String getCalendarType() {
        return calendarType;
    }

    public void setCalendarType(String calendarType) {
        this.calendarType = calendarType;
    }
}
