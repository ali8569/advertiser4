package ir.markazandroid.advertiser.object;

import ir.markazandroid.advertiser.network.JSONParser.annotations.JSON;

/**
 * Coded by Ali on 10/21/2018.
 */
public class Content extends Record.Image {

    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_VIDEO = "video";

    private String type;

    @JSON
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Content)) return false;
        if (!super.equals(o)) return false;

        Content content = (Content) o;

        return type != null ? type.equals(content.type) : content.type == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
