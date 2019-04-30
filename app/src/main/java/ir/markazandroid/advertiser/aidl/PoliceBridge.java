package ir.markazandroid.advertiser.aidl;

import ir.markazandroid.police.aidl.AuthenticationDetails;

/**
 * Coded by Ali on 4/27/2019.
 */
public interface PoliceBridge {

    /**
     *
     * @return may be null
     */
    AuthenticationDetails getAuthenticationDetails();
}
