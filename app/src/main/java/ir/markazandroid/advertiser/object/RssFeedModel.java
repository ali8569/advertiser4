package ir.markazandroid.advertiser.object;

import java.io.Serializable;

/**
 * Created by obaro on 27/11/2016.
 */

public class RssFeedModel implements Serializable {

    private String title;
    private String link;
    private String description;

    public RssFeedModel(String title, String link, String description) {
        this.title = title;
        this.link = link;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return description;
    }
}
