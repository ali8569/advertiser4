package ir.markazandroid.advertiser.object;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import ir.markazandroid.advertiser.network.JSONParser.annotations.JSON;

/**
 * Coded by Ali on 01/02/2018.
 */

public class Record implements Serializable {

    private int userId;
    private int recordId;
    private String name;
    private ArrayList<String> videoArray = new ArrayList<>();
    private ArrayList<Image> photosArrayObject = new ArrayList<>();
    private ArrayList<SubTitle> subTitleArrayObject = new ArrayList<>();
    private ArrayList<String> soundArray = new ArrayList<>();
    private ArrayList<File> soundFiles = new ArrayList<>();
    private ArrayList<File> videoFiles = new ArrayList<>();
    private long createTime;
    private long latestEditTime;
    private String icon = "http://aaaaa.com";
    private int fontSize = 21;
    private RecordOptions options;
    private ExtrasObject extras;
    private String layoutType;

    @JSON
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @JSON
    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    @JSON
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JSON(classType = JSON.CLASS_TYPE_ARRAY)
    public ArrayList<String> getVideoArray() {
        return videoArray;
    }

    public void setVideoArray(ArrayList<String> videoArray) {
        this.videoArray = videoArray;
    }

    @JSON(classType = JSON.CLASS_TYPE_ARRAY, clazz = Image.class)
    public ArrayList<Image> getPhotosArrayObject() {
        return photosArrayObject;
    }

    public void setPhotosArrayObject(ArrayList<Image> photosArrayObject) {
        this.photosArrayObject = photosArrayObject;
    }

    @JSON(classType = JSON.CLASS_TYPE_ARRAY, clazz = SubTitle.class)
    public ArrayList<SubTitle> getSubTitleArrayObject() {
        return subTitleArrayObject;
    }

    public void setSubTitleArrayObject(ArrayList<SubTitle> subTitleArrayObject) {
        this.subTitleArrayObject = subTitleArrayObject;
    }

    @JSON(classType = JSON.CLASS_TYPE_ARRAY)
    public ArrayList<String> getSoundArray() {
        return soundArray;
    }

    public void setSoundArray(ArrayList<String> soundArray) {
        this.soundArray = soundArray;
    }

    @JSON
    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @JSON
    public long getLatestEditTime() {
        return latestEditTime;
    }

    public void setLatestEditTime(long latestEditTime) {
        this.latestEditTime = latestEditTime;
    }

    @JSON
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public ArrayList<File> getVideoFiles() {
        return videoFiles;
    }

    public void setVideoFiles(ArrayList<File> videoFiles) {
        this.videoFiles = videoFiles;
    }

    public ArrayList<File> getSoundFiles() {
        return soundFiles;
    }

    public void setSoundFiles(ArrayList<File> soundFiles) {
        this.soundFiles = soundFiles;
    }

    @JSON(classType = JSON.CLASS_TYPE_OBJECT, clazz = RecordOptions.class)
    public RecordOptions getOptions() {
        return options;
    }

    public void setOptions(RecordOptions options) {
        this.options = options;
    }

    @JSON(classType = JSON.CLASS_TYPE_OBJECT, clazz = ExtrasObject.class)
    public ExtrasObject getExtras() {
        return extras;
    }

    public void setExtras(ExtrasObject extras) {
        this.extras = extras;
    }

    @JSON
    public String getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(String layoutType) {
        this.layoutType = layoutType;
    }

    public static class Image implements Serializable {
        private String imageUrl;
        private long duration;
        private String scaleType = "fitXY";
        private File file;

        public Image() {
        }

        public Image(String imageUrl, long duration) {
            this.imageUrl = imageUrl;
            this.duration = duration;
        }

        @JSON
        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        @JSON
        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        @JSON
        public String getScaleType() {
            return scaleType;
        }

        public void setScaleType(String scaleType) {
            this.scaleType = scaleType;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Image)) return false;

            Image image = (Image) o;

            if (duration != image.duration) return false;
            if (imageUrl != null ? !imageUrl.equals(image.imageUrl) : image.imageUrl != null)
                return false;
            return scaleType != null ? scaleType.equals(image.scaleType) : image.scaleType == null;
        }

        @Override
        public int hashCode() {
            int result = imageUrl != null ? imageUrl.hashCode() : 0;
            result = 31 * result + (int) (duration ^ (duration >>> 32));
            result = 31 * result + (scaleType != null ? scaleType.hashCode() : 0);
            return result;
        }
    }

    public static class SubTitle implements Serializable {
        private String subTitle;
        private long duration;
        private int fontSize = 21;

        public SubTitle() {
        }

        public SubTitle(String subTitle, long duration) {
            this.subTitle = subTitle;
            this.duration = duration;
        }

        @JSON
        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        @JSON
        public String getSubTitle() {
            return subTitle;
        }

        public void setSubTitle(String subTitle) {
            this.subTitle = subTitle;
        }

        @JSON
        public int getFontSize() {
            return fontSize;
        }

        public void setFontSize(int fontSize) {
            this.fontSize = fontSize;
        }

    }
}
