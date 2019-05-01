package ir.markazandroid.advertiser.adapter;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.util.List;

import ir.markazandroid.advertiser.R;
import ir.markazandroid.advertiser.downloader.RecordDownloader;
import ir.markazandroid.advertiser.object.GoldEntity;

/**
 * Coded by Ali on 3/5/2019.
 */
public class GoldChooserAdapter extends RecyclerView.Adapter<GoldChooserAdapter.ViewHolder> {

    public interface GoldClickListener {
        boolean onGoldClick(GoldEntity goldEntity);
    }

    private List<GoldEntity> golds;
    private Context context;
    private GoldClickListener listener;
    private GoldEntity selectedGold;

    public GoldChooserAdapter(Context context, GoldClickListener listener) {
        this.context = context;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.gold_item_entry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GoldEntity goldEntity = golds.get(position);
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/advertiser/gold");
        File goldFile = new File(myDir, RecordDownloader.extractFilename(goldEntity.getPhotoUrl()));
        RequestCreator creator;

        if (goldFile.exists()) {
            creator = Picasso.get().load(goldFile);
        } else {
            creator = Picasso.get().load(goldEntity.getPhotoUrl());
        }
        creator.fit()
                .noFade()
                .into(holder.image);

        if (goldEntity.equals(selectedGold))
            holder.transition.startTransition(0);
        else
            holder.transition.resetTransition();

        holder.itemView.setOnClickListener(v -> {
            if (!goldEntity.equals(selectedGold)) {
                int lastIndex = golds.indexOf(selectedGold);
                selectedGold = goldEntity;
                holder.transition.startTransition(500);
                notifyItemChanged(lastIndex);
                listener.onGoldClick(goldEntity);
            }
        });
    }


    @Override
    public int getItemCount() {
        return golds == null ? 0 : golds.size();
    }

    public void setGolds(List<GoldEntity> golds) {
        this.golds = golds;
        if (!golds.isEmpty())
            selectedGold = golds.get(0);
        notifyDataSetChanged();
    }

    public void refresh() {
        if (golds != null && !golds.isEmpty())
            selectedGold = golds.get(0);
        notifyDataSetChanged();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TransitionDrawable transition;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            transition = (TransitionDrawable) itemView.getBackground();
        }
    }
}
