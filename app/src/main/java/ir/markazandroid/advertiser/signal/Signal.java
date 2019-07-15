package ir.markazandroid.advertiser.signal;

import java.io.Serializable;

/**
 * Coded by Ali on 29/07/2017.
 */

public class Signal implements Serializable {

    public static final int SIGNAL_LOGIN = 0x00000001;
    public static final int SIGNAL_RESPONSE = 0x00000002;
    public static final int SIGNAL_LOGOUT = 0x00000004;
    public static final int SIGNAL_VIEW_DESTROYED =0x00000008;
    public static final int SIGNAL_ACTIVITY_DESTROYED =0x00000010 | SIGNAL_VIEW_DESTROYED;
    public static final int SIGNAL_REFRESH_RECORDS =0x00000020;
    public static final int SIGNAL_SCREEN_BLOCK =0x00000040;
    public static final int SIGNAL_SCREEN_UNBLOCK =0x00000080;
    public static final int DOWNLOADER_FINISHED =0x00000100;
    public static final int DOWNLOADER_NO_NETWORK =0x00000200;
    public static final int DOWNLOADER_NETWORK =0x00000400;
    public static final int RECORD_AVALABLE =0x00000800;

    public static final int START_MAIN_ACTIVITY =0x00001000;
    public static final int OPEN_SOCKET_HEADER_RECEIVED =0x00002000;

    public static final int SIGNAL_DEVICE_AUTHENTICATED =0x00004000;
    public static final int SIGNAL_CONNECTED_TO_POLICE =0x00008000;
    public static final int SIGNAL_DISCONNECTED_FROM_POLICE =0x00010000;
    public static final int SIGNAL_DISABLE_KEEP_ALIVE = 0x00020000;
    public static final int SIGNAL_ENABLE_KEEP_ALIVE = 0x00040000;

    public static final int SIGNAL_LAUNCHING_3PARTY_APP = 0x00080000;

    private String msg;
    private int type;

    private Serializable extras;

    public Signal(String msg, int type, Serializable extras) {
        this.msg = msg;
        this.type = type;
        this.extras = extras;
    }

    public Signal(String msg, int type) {
        this.msg = msg;
        this.type = type;
    }

    public Signal(int type) {
        this.type = type;
    }

    public Signal() {
    }

    public Serializable getExtras() {
        return extras;
    }

    public void setExtras(Serializable extras) {
        this.extras = extras;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
