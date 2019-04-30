package ir.markazandroid.advertiser.network;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.BuildConfig;
import ir.markazandroid.advertiser.hardware.PortReader;
import ir.markazandroid.advertiser.network.JSONParser.Parser;
import ir.markazandroid.advertiser.object.ErrorObject;
import ir.markazandroid.advertiser.object.Record;
import ir.markazandroid.advertiser.object.RssFeedModel;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.util.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Coded by Ali on 03/11/2017.
 */

public class NetworkMangerImp implements NetworkManager {


    private Context context;
    private Parser parser;
    private OkHttpClient client;
    private String tag;

    private NetworkMangerImp(Context context,String tag) {
        this.tag=tag;
        this.context=context;
        parser=((AdvertiserApplication)context.getApplicationContext()).getParser();
        client=((AdvertiserApplication)context.getApplicationContext()).getNetworkClient().getClient();
    }

    @Override
    public void getRecords(final OnResultLoaded.ActionListener<ArrayList<Record>> actionListener) {
        String stats =PortReader.lastData+"__v"
                + BuildConfig.VERSION_CODE
                //+"__"
                //+ getSensorMeter().getResults()+"__"
                //+ getLocation()
                ;
        PortReader.save(stats);
        Request request = new Request.Builder()
                .url(NetStatics.RECORD+"?stats="
                        +stats)
                .get()
                .build();
        client.newCall(request).enqueue(new CBack(context, tag) {

            @Override
            public boolean isSuccessfull(int code) {
                return true;
            }

            @Override
            public boolean isSuccessfull(int code, JSONObject response) {
                actionListener.onError(parser.get(ErrorObject.class,response));
                return false;
            }

            @Override
            public boolean isSuccessfull(int code, JSONArray response) {
                ((AdvertiserApplication)context.getApplicationContext()).getSignalManager().sendMainSignal(new Signal("record", Signal.RECORD_AVALABLE));
                actionListener.onSuccess(parser.get(Record.class,response));
                return true;
            }

            @Override
            public void fail(IOException e) {
                actionListener.failed(e);
            }

        });
    }

    @Override
    public void loadRssFeed(String url, final OnResultLoaded<List<RssFeedModel>> listener) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new CBack(context,tag,true) {
            @Override
            public void result(InputStream responseBody) {
                try {
                    listener.loaded(Utils.parseFeed(responseBody));
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void fail(IOException e) {
                listener.failed(e);
            }
        });
    }



    public static class NetworkManagerBuilder {
        private String tag;
        private Context context;

        public NetworkManagerBuilder() {
        }

        public NetworkManagerBuilder from(Context context) {
            this.context = context;
            return this;
        }

        public NetworkManagerBuilder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public NetworkManager build() {
            return new NetworkMangerImp(context, tag);
        }
    }

}
