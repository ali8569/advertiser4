package ir.markazandroid.police.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Coded by Ali on 4/27/2019.
 */
public class AuthenticationDetails implements Parcelable {
    private String sessionId;
    private long lastUpdate;

    protected AuthenticationDetails(Parcel in) {
        sessionId = in.readString();
        lastUpdate = in.readLong();
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sessionId);
        dest.writeLong(lastUpdate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AuthenticationDetails> CREATOR = new Creator<AuthenticationDetails>() {
        @Override
        public AuthenticationDetails createFromParcel(Parcel in) {
            return new AuthenticationDetails(in);
        }

        @Override
        public AuthenticationDetails[] newArray(int size) {
            return new AuthenticationDetails[size];
        }
    };

    public String getSessionId() {
        return sessionId;
    }
}
