package ir.markazandroid.advertiser.network;


/**
 * Coded by Ali on 05/11/2017.
 */

public class NetStatics {


    static final String DOMAIN = "http://harajgram.ir";
    //static final String DOMAIN = "http://192.168.1.36:8080";
    //static final String DOMAIN = "http://192.168.44.:8080";
    static final String SUFFIX = DOMAIN + "/advertisergold/api";
    //static final String SUFFIX = DOMAIN + "/api";


    static final String REGISTRATION = SUFFIX + "/registration";
    static final String REGISTRATION_REGISTER = REGISTRATION + "/register";
    static final String REGISTRATION_LOGIN = REGISTRATION + "/login";

    static final String RECORD = SUFFIX + "/record";

    static final String PHONE = SUFFIX + "/phone";
    static final String PHONE_FIRSTLOGIN = PHONE + "/firstLogin";
    static final String PHONE_POSTSCREENSHOT = PHONE + "/postScreenShot";
    public static final String VERSION = PHONE + "/getUpdate";

    static final String GOLD = SUFFIX + "/gold";
    static final String GOLD_List = GOLD + "/list";
}
