package ir.markazandroid.advertiser.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.police.aidl.AuthenticationDetails;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * Coded by Ali on 11/12/2017.
 */

public class NetworkClient {
    private OkHttpClient client;
    private Set<Cookie> cookie;
    private Context mContext;

    public NetworkClient(Context context) {
        this.mContext=context;
        cookie = new HashSet<>();
        //TODO No cookie saved
        //populateCookies();
        client = new OkHttpClient()
                .newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                // for(int i=0;i<cookies.size();i++) Log.e("SAVED ",cookies.get(i).name()+"="+cookies.get(i).value());
                for (int i = 0; i < cookies.size(); i++) {
                    Cookie cook = cookies.get(i);
                    //TODO No cookie saved
                   // mContext.getSharedPreferences(AdvertiserApplication.SHARED_PREFRENCES, Context.MODE_PRIVATE)
                    //        .edit().putString("COOKIE_" + cook.name(), cook.value()).apply();
                }
                //TODO No cookie saved
                //cookie.addAll(cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                //for(int i=0;i<cookie.size();i++) System.err.println("SENT "+cookie.get(i).name()+"="+cookie.get(i).value());
                AuthenticationDetails authenticationDetails = getAuthenticationDetails();
                if (authenticationDetails!=null)
                    cookie.add(new Cookie.Builder().name("JSESSIONID")
                            .value(authenticationDetails.getSessionId())
                            .domain(NetStatics.DOMAIN.substring(7).replace(":8080", ""))
                            .build());
                return new ArrayList<>(cookie);
            }
        })/*.addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        for (String h:chain.request().headers().names()){
                            Log.e("Head","name:"+h+"  value="+chain.request().header(h));
                        }
                        return chain.proceed(chain.request());
                    }
                }).addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        for (String h:chain.request().headers().names()){
                            Log.e("Head","name:"+h+"  value="+chain.request().header(h));
                        }
                        return chain.proceed(chain.request());
                    }
                })*/.retryOnConnectionFailure(true).build();
    }
    private void populateCookies() {
        Map map = mContext.getSharedPreferences(AdvertiserApplication.SHARED_PREFRENCES, Context.MODE_PRIVATE).getAll();
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            if (((String) entry.getKey()).startsWith("COOKIE_")) {
                Cookie cookie = new Cookie.Builder().name(((String) entry.getKey()).substring(7))
                        .value((String) entry.getValue())
                        .domain(NetStatics.DOMAIN.substring(7).replace(":8080", ""))
                        .build();
                this.cookie.add(cookie);
            }
        }
    }
    public void deleteCookies(){
        SharedPreferences sharedPreferences =mContext.getSharedPreferences(AdvertiserApplication.SHARED_PREFRENCES, Context.MODE_PRIVATE);
        Map map = sharedPreferences.getAll();
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            if (((String) entry.getKey()).startsWith("COOKIE_")) {
                sharedPreferences.edit().remove(entry.getKey().toString()).apply();
            }
        }
        cookie.clear();
    }

    public OkHttpClient getClient() {
        return client;
    }


    public Set<Cookie> getCookie() {
        return cookie;
    }

    private AuthenticationDetails getAuthenticationDetails(){
        return ((AdvertiserApplication)mContext.getApplicationContext()).getPoliceBridge().getAuthenticationDetails();
    }
}
