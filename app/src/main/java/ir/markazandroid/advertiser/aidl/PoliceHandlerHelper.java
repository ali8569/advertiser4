package ir.markazandroid.advertiser.aidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalManager;
import ir.markazandroid.police.aidl.AuthenticationDetails;
import ir.markazandroid.police.aidl.IPolice;

public class PoliceHandlerHelper implements PoliceBridge {

    private IPolice police;
    private Context context;
    private SignalManager signalManager;

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            Log.e("PoliceHandler","Connected to service");
            signalManager.sendMainSignal(new Signal(Signal.SIGNAL_CONNECTED_TO_POLICE));
            police = IPolice.Stub.asInterface(service);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e("PoliceHandler","disconnected from service");
            signalManager.sendMainSignal(new Signal(Signal.SIGNAL_DISCONNECTED_FROM_POLICE));
            police = null;
            connectToService();
        }
    };

    public PoliceHandlerHelper(Context context,SignalManager signalManager){
        this.context=context;
        this.signalManager=signalManager;
        connectToService();
    }



    private void connectToService(){
        Intent intent = new Intent();
        intent.setClassName("ir.markazandroid.police","ir.markazandroid.police.bridge.BridgeService");
        context.startService(intent);
        intent=new Intent();
        intent.setClassName("ir.markazandroid.police","ir.markazandroid.police.bridge.BridgeService");
        context.bindService(intent,mConnection,0);
    }


    @Override
    public AuthenticationDetails getAuthenticationDetails() {
        if (police==null) return null;
        try {
            return police.getAuthenticationDetails();
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }
}
