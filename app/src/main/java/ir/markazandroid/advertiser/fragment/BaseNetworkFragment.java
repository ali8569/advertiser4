package ir.markazandroid.advertiser.fragment;

import android.support.v4.app.Fragment;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.network.NetworkManager;
import ir.markazandroid.advertiser.network.NetworkMangerImp;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalManager;
import ir.markazandroid.advertiser.util.PreferencesManager;

/**
 * base fragment abstract class for extending by fragments that need to use
 * {@link NetworkManager} to perform network operations
 * and {@link SignalManager} to send signals
 * <p>
 * Coded by Ali on 06/11/2017.
 */

public abstract class BaseNetworkFragment extends Fragment {
    private NetworkManager networkManager;

    protected NetworkManager getNetworkManager() {
        if (networkManager == null) {
            networkManager = new NetworkMangerImp.NetworkManagerBuilder()
                    .from(getActivity())
                    .tag(toString())
                    .build();
        }
        return networkManager;
    }

    protected SignalManager getSignalManager() {
        return ((AdvertiserApplication) getActivity().getApplication()).getSignalManager();
    }

    protected PreferencesManager getPreferencesManager() {
        return ((AdvertiserApplication) getActivity().getApplication()).getPreferencesManager();
    }


    /**
     * send view destroyed signal to cancel request callbacks in order to
     * to avoid {@link NullPointerException}
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getSignalManager().sendSignal(new Signal(toString(), Signal.SIGNAL_VIEW_DESTROYED));
    }
}
