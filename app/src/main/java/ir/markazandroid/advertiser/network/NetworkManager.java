package ir.markazandroid.advertiser.network;

import java.util.ArrayList;
import java.util.List;

import ir.markazandroid.advertiser.object.Record;
import ir.markazandroid.advertiser.object.RssFeedModel;


/**
 * Coded by Ali on 03/11/2017.
 */

public interface NetworkManager {

    void getRecords(OnResultLoaded.ActionListener<ArrayList<Record>> actionListener);


    void loadRssFeed(String url, OnResultLoaded<List<RssFeedModel>> listener);

}
