package ir.markazandroid.advertiser.network;

import java.util.ArrayList;
import java.util.List;

import ir.markazandroid.advertiser.object.GoldListContainer;
import ir.markazandroid.advertiser.object.Phone;
import ir.markazandroid.advertiser.object.Record;
import ir.markazandroid.advertiser.object.RssFeedModel;
import ir.markazandroid.advertiser.object.ScreenShot;
import ir.markazandroid.advertiser.object.User;


/**
 * Coded by Ali on 03/11/2017.
 */

public interface NetworkManager {

    void getRecords(OnResultLoaded.ActionListener<ArrayList<Record>> actionListener);

    void register(User user, OnResultLoaded.ActionListener<Phone> actionListener);

    void login(String uuid, OnResultLoaded.ActionListener<Phone> actionListener);

    void sendName(String name, OnResultLoaded.ActionListener<Phone> actionListener);

    void loadRssFeed(String url, OnResultLoaded<List<RssFeedModel>> listener);

    void getGold(long lastUpdate, OnResultLoaded<GoldListContainer> listener);

    void postScreenShot(ScreenShot screenShot, OnResultLoaded.ActionListener actionListener);
}
