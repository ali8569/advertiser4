package ir.markazandroid.advertiser.notification;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import ir.markazandroid.advertiser.network.NetworkManager;
import ir.markazandroid.advertiser.network.NetworkMangerImp;
import ir.markazandroid.advertiser.network.OnResultLoaded;
import ir.markazandroid.advertiser.object.ErrorObject;


/**
 * Coded by Ali on 24/12/2017.
 */

public class TokenService extends FirebaseInstanceIdService {
    private static final String TAG = "Firebase";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "Refreshed token: " + refreshedToken);

       /* NetworkManager networkManager= new NetworkMangerImp.NetworkManagerBuilder()
                .from(this)
                .tag(toString())
                .build();
        */
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        // sendRegistrationToServer(refreshedToken);
    }
}
