package ir.markazandroid.advertiser.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import ir.markazandroid.advertiser.R;
import ir.markazandroid.advertiser.object.Record;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageFragment extends SelectiveFragment {

    private static final String IMAGE = "image";
    private Record.Image image;


    public ImageFragment() {
        Bundle bundle = new Bundle();
        setArguments(bundle);
        // Required empty public constructor
    }

    public ImageFragment image(Record.Image image) {
        getArguments().putSerializable(IMAGE, image);
        return this;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        image = (Record.Image) getArguments().getSerializable(IMAGE);
    }


    private void showImage() {
        //Picasso.get().setIndicatorsEnabled(true);
        RequestCreator requestCreator = Picasso.get()
                .load(image.getFile());
        DisplayMetrics metrics = new DisplayMetrics();
        if (getActivity() == null) {
            int a = 1;
        }
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        switch (image.getScaleType()) {
            case "fitXY":
                requestCreator.fit();
                break;
            case "centerInside":
                requestCreator.resize(imageView.getWidth(), imageView.getHeight())
                        .centerInside();
                break;
            case "centerCrop":
                requestCreator.resize(imageView.getWidth(), imageView.getHeight())
                        .centerCrop();
                break;
            case "noScale":
                break;
        }
        requestCreator.into(imageView);
    }

    private ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        imageView = view.findViewById(R.id.image);
        return view;
    }

    @Override
    protected void start() {
        try {
            imageView.post(this::showImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void refresh() {
        start();
    }

    @Override
    protected void stop() {

    }
}
