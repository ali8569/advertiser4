package ir.markazandroid.advertiser.view.gold;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ir.markazandroid.advertiser.downloader.RecordDownloader;
import ir.markazandroid.advertiser.object.GoldEntity;

/**
 * Coded by Ali on 12/12/2018.
 */
public class GoldShowAdapter {

    private List<GoldEntity> golds;
    private ArrayList<Boolean> isLoadedList;
    private TextView name;


    public GoldShowAdapter(TextView name) {
        isLoadedList = new ArrayList<>();
        this.name = name;
    }


    public void getItemBitmap(GoldTarget target) {
        GoldEntity gold = golds.get(target.position);
        name.setText(String.format("%s\n%d گرم", gold.getName(), gold.getWeight(), gold.getDetails()));
        Picasso.get().cancelTag(this);

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/advertiser/gold");
        File goldFile = new File(myDir, RecordDownloader.extractFilename(gold.getPhotoUrl()));
        RequestCreator requestCreator = Picasso.get()
                .load(goldFile)
                .tag(this);

        boolean fromMemory = false;
        try {
            fromMemory = isLoadedList.get(target.position);
        } catch (Exception ignored) {
        }

        if (!fromMemory)
            requestCreator = requestCreator.memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE);

        requestCreator.into(target);
        /*if (goldFile.exists()){
            try {

            }catch (Exception e){
                e.printStackTrace();
            }
        }*/
        Log.e("url", golds.get(target.position).getPhotoUrl());
    }

    public int getCount() {
        return golds == null ? 0 : golds.size();
    }

    public List<GoldEntity> getGolds() {
        return golds;
    }

    public void setGolds(List<GoldEntity> golds) {
        Picasso.get().cancelTag(this);
        this.golds = golds;
        isLoadedList.clear();
        for (int i = 0; i < golds.size(); i++) {
            isLoadedList.add(false);
        }
    }

    public abstract class GoldTarget implements Target {

        private int position;

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            try {
                isLoadedList.set(position, true);
            } catch (Exception ignored) {
            }
        }
    }

    public void dispose() {
        Picasso.get().cancelTag(this);
    }

    GoldTarget goldTarget;

    public GoldTarget getGoldTarget(int position, Target target) {
        goldTarget = new GoldTarget() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                super.onBitmapLoaded(bitmap, from);
                target.onBitmapLoaded(bitmap, from);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                target.onBitmapFailed(e, errorDrawable);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                target.onPrepareLoad(placeHolderDrawable);
            }
        };
        goldTarget.position = position;
        return goldTarget;
    }
}
