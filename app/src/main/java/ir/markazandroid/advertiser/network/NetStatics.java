package ir.markazandroid.advertiser.network;


/**
 * Coded by Ali on 05/11/2017.
 */

public class NetStatics {


    // chnage local address to public
    static final String DOMAIN = "http://harajgram.ir";
    //static final String DOMAIN = "http://192.168.1.36:8080";
    //static final String DOMAIN = "http://192.168.1.104:8080";
    static final String SUFFIX = DOMAIN + "/advertiserv4/api";
    //static final String SUFFIX = DOMAIN + "/api";


    static final String REGISTRATION=SUFFIX+"/registration";
    static final String REGISTRATION_REGISTER=REGISTRATION+"/register";
    static final String REGISTRATION_LOGIN=REGISTRATION+"/login";

    static final String RECORD=SUFFIX+"/record";

    static final String PHONE=SUFFIX+"/phone";
    static final String PHONE_FIRSTLOGIN=PHONE+"/firstLogin";
    public static final String VERSION=PHONE+"/getUpdate";

}
