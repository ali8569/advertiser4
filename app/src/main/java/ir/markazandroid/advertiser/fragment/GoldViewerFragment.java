package ir.markazandroid.advertiser.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Size;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.R;
import ir.markazandroid.advertiser.adapter.GoldChooserAdapter;
import ir.markazandroid.advertiser.downloader.GoldDownloader;
import ir.markazandroid.advertiser.downloader.RecordDownloader;
import ir.markazandroid.advertiser.network.JSONParser.Parser;
import ir.markazandroid.advertiser.network.OnResultLoaded;
import ir.markazandroid.advertiser.object.ErrorObject;
import ir.markazandroid.advertiser.object.GoldEntity;
import ir.markazandroid.advertiser.object.GoldListContainer;
import ir.markazandroid.advertiser.object.ScreenShot;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalReceiver;
import ir.markazandroid.advertiser.util.Utils;
import ir.markazandroid.advertiser.view.gold.CameraOverlayView;
import ir.markazandroid.advertiser.view.gold.GoldShowAdapter;
import ir.markazandroid.advertiser.view.gold.ScannerAnimationView;
import ir.markazandroid.advertiser.view.gold.camera.GoldCameraActivity;


public class GoldViewerFragment extends BaseNetworkFragment implements SignalReceiver {

    private static final int PERMISION_REQUEST_CODE = 5;
    private static final String TITLE_EXTRA = "ir.markazandroid.advertiser.fragment.GoldViewerFragment.TITLE";

    private CameraView cameraView;
    private ImageView photoIV, goLeftIC, goRightIC, takePhotoIC, refreshIC;
    private FirebaseVisionFaceDetector detector;
    private CameraOverlayView overlayView;
    private Intent cameraIntent;
    private Timer refresherTimer;
    private GoldShowAdapter goldShowAdapter;
    private GoldChooserAdapter goldChooserAdapter;
    private GoldListContainer goldListContainer;
    private RecyclerView goldChooserList;
    private View goldShowerLayout;
    private ScannerAnimationView scannerAnimationView;
    private TextView goldDownloaderStats;
    private Timer ssPostTimer;
    private Button menuButton;
    private BoomMenuButton menuBmb;
    private TextView goldTitle;
    private ConcurrentHashMap<String, ScreenShot> onSendScreenShots;

    public String title;


    public static GoldViewerFragment getInstance(String title) {
        GoldViewerFragment fragment = new GoldViewerFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_EXTRA, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(TITLE_EXTRA, null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gold_viewer_fragment, container, false);
        //cameraView=findViewById(R.id.camera_view);
        overlayView = v.findViewById(R.id.overlay_view);
        photoIV = v.findViewById(R.id.photo);
        goLeftIC = v.findViewById(R.id.go_left);
        goRightIC = v.findViewById(R.id.go_right);
        takePhotoIC = v.findViewById(R.id.take_photo);
        goldShowerLayout = v.findViewById(R.id.goldShowerLayout);
        refreshIC = v.findViewById(R.id.refresh);
        scannerAnimationView = v.findViewById(R.id.scanner_animation);
        goldDownloaderStats = v.findViewById(R.id.goldDownlaoderStat);
        goldChooserList = v.findViewById(R.id.goldChooser);
        menuBmb = v.findViewById(R.id.menu);
        menuButton = v.findViewById(R.id.menuButton);
        goldTitle = v.findViewById(R.id.goldTitle);
        return v;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cameraIntent = new Intent(getActivity(), GoldCameraActivity.class);
        cameraIntent.putExtra(GoldCameraActivity.IMAGE_PATH, getActivity().getExternalCacheDir().getPath() + "/pickImageResult.jpeg");
        //cameraIntent = CropImage.getCameraIntent(getActivity(),null);
        //cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,FileProvider.getUriForFile(getActivity(),
        //        getActivity().getApplicationContext().getPackageName() + ".util.GenericFileProvider",
        //      new File(getActivity().getExternalCacheDir().getPath(),"pickImageResult.jpeg")));

        goldShowAdapter = new GoldShowAdapter(getView().findViewById(R.id.name));
        goldChooserAdapter = new GoldChooserAdapter(getActivity(), this::onGoldClick);
        goldChooserList.setAdapter(goldChooserAdapter);
        goldChooserList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        overlayView.setAdapter(goldShowAdapter);
        overlayView.setProgressBar(getView().findViewById(R.id.processbar));
        checkNextBackButtons();

        // if (!((AdvertiserApplication)getActivity().getApplication()).isInternetConnected()){

        // }

        if (getPreferencesManager().getGoldList() != null) {
            try {
                loadGold(getParser().get(GoldListContainer.class, new JSONObject(getPreferencesManager().getGoldList())), true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        refresherTimer = new Timer();
        refresherTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getNetworkManager().getGold(goldListContainer == null ? 0 : goldListContainer.getLastUpdate(), new OnResultLoaded<GoldListContainer>() {
                    @Override
                    public void loaded(GoldListContainer result) {
                        getActivity().runOnUiThread(() -> {
                            loadGold(result, false);
                        });
                    }

                    @Override
                    public void failed(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }, 0, 20_000);
        takePhotoIC.setOnClickListener(v -> {
            //checkAndRequestCameraPermission();
            takePhoto();
        });

        goLeftIC.setOnClickListener(v -> {
            overlayView.goBack();
            checkNextBackButtons();
        });

        goRightIC.setOnClickListener(v -> {
            overlayView.goNext();
            checkNextBackButtons();
        });

        refreshIC.setOnClickListener(v -> refresh());

        ssPostTimer = new Timer();
        ssPostTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(() -> {
                    String mPath = Environment.getExternalStorageDirectory().toString() + "/advertiser/screenShots/";
                    File screenDirectories = new File(mPath);
                    if (!screenDirectories.exists()) screenDirectories.mkdirs();

                    Collection<File> screenShots = FileUtils.listFiles(screenDirectories, null, false);

                    for (File file : screenShots) {
                        sendScreenShot(file);
                    }
                });
            }
        }, 30_000);

        menuBmb.addBuilder(getDefaultBuilder(menuBmb, "گردنبند", R.drawable.necklace));
        menuBmb.addBuilder(getDefaultBuilder(menuBmb, "گوشواره", R.drawable.earing));
        menuBmb.addBuilder(getDefaultBuilder(menuBmb, "دستبند", R.drawable.bracelet));
        menuBmb.addBuilder(getDefaultBuilder(menuBmb, "حلقه", R.drawable.ring));

        menuButton.setOnClickListener(v ->
                menuBmb.boom()
        );

        if (title == null)
            title = ((AdvertiserApplication) getActivity().getApplication()).getGoldTitle();


        setTitle();

        onSendScreenShots = new ConcurrentHashMap<>();

        getSignalManager().addReceiver(this);
    }


    private void setTitle() {
        if (title != null && !title.isEmpty()) {
            goldTitle.setText(title);
            goldTitle.setVisibility(View.VISIBLE);
            TextView name = goldShowerLayout.findViewById(R.id.name);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) name.getLayoutParams();
            params.topMargin += Utils.dpToPx(getActivity(), 29);
        }
    }

    private TextOutsideCircleButton.Builder getDefaultBuilder(BoomMenuButton menu, String text, int iconId) {
        int width = (int) (Utils.dpToPx(getActivity(), 50) * 1.4);
        int padd = (int) (Utils.dpToPx(getActivity(), 50) * 0.3);
        return new TextOutsideCircleButton.Builder()
                .normalImageRes(iconId)
                .normalText(text)
                .unableText(text)
                .imageRect(new Rect(padd, padd, width + padd, width + padd))
                .textSize(18)
                .typeface(Typeface.createFromAsset(menu.getContext().getAssets(), "font/B Yekan.ttf"))
                .ellipsize(TextUtils.TruncateAt.END)
                .normalColor(Color.WHITE)
                .pieceColor(Color.WHITE)
                .highlightedColor(Color.WHITE)
                .buttonRadius(Utils.dpToPx(getActivity(), 50))
                .unableImageRes(iconId);
    }


    private boolean onGoldClick(GoldEntity goldEntity) {
        int selectedIndex = overlayView.getPosition();
        int toIndex = goldListContainer.getGolds().indexOf(goldEntity);
        if (selectedIndex == toIndex) return false;
        overlayView.goToPosition(toIndex);
        checkNextBackButtons();
        return true;
    }

    private void loadGold(GoldListContainer result, boolean fromMem) {
        goldListContainer = result;
        if (fromMem) {
            saveAndShowGoldList();
        }
        if (result.getGolds() != null && !result.getGolds().isEmpty()) {
            goldDownloaderStats.setVisibility(View.VISIBLE);
            getGoldDownloader().setGolds(result.getGolds());
            getGoldDownloader().setProcessMonitor(new RecordDownloader.ProcessMonitor() {
                @Override
                public void onProcess(String status) {
                    getActivity().runOnUiThread(() -> {
                        goldDownloaderStats.setText(status);
                    });
                }

                @Override
                public void onFinish() {
                    getActivity().runOnUiThread(() -> {
                        goldDownloaderStats.setVisibility(View.GONE);
                        saveAndShowGoldList();
                    });
                }
            });
            getGoldDownloader().init();
        }
    }

    private void saveAndShowGoldList() {
        getPreferencesManager().saveGoldList(getParser().get(goldListContainer).toString());
        if (goldListContainer != null && goldListContainer.getGolds() != null) {
            goldShowAdapter.setGolds(goldListContainer.getGolds());
            goldChooserAdapter.setGolds(goldListContainer.getGolds());
            checkNextBackButtons();
            overlayView.refresh();
        }
    }

    private void refresh() {
        takeScreenshot();
        Utils.fadeFade(goldShowerLayout, 500, false);
        overlayView.refresh();
        goldChooserAdapter.refresh();
        photoIV.setImageResource(0);
    }

    private void checkNextBackButtons() {
        //goLeftIC.setVisibility(overlayView.getBackAvailable()?View.VISIBLE:View.INVISIBLE);
        //goRightIC.setVisibility(overlayView.getNextAvailable()?View.VISIBLE:View.INVISIBLE);
    }

    private void checkAndRequestCameraPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                    PERMISION_REQUEST_CODE);
        } else {
            startFaceProcessor();
        }
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 110);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISION_REQUEST_CODE) {
            if (Manifest.permission.CAMERA.equals(permissions[0]) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startFaceProcessor();
            }
        } else if (requestCode == 110) {
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[0]) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void openCamera() {
        getSignalManager().sendMainSignal(new Signal(Signal.CAMERA_OPENED));
        startActivityForResult(cameraIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            getSignalManager().sendMainSignal(new Signal(Signal.CAMERA_CLOSED));
            if (resultCode == Activity.RESULT_OK) {
                Uri uri;
                uri = Uri.fromFile(new File(data.getStringExtra(GoldCameraActivity.IMAGE_PATH)));
                //if (data == null || data.getData()==null) uri = CropImage.getCaptureImageOutputUri(getActivity());
                //else uri = data.getData();
                Picasso.get()
                        .load(uri)
                        .fit()
                        .rotate(90)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .noFade()
                        .into(photoIV);
                //cropImage(uri);
                detectFaceFromImage(uri);
            }
        }
    }


    private void detectFaceFromImage(Uri uri) {
        Utils.fade(scannerAnimationView, goldShowerLayout, 500, false);

        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                //.setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                //.setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                //.setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .build();
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);

        Bitmap bitmap;

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            Matrix matrix = new Matrix();

            matrix.postRotate(90);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true);
            //bitmap.recycle();
            bitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            scaledBitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        CameraOverlayView.Size size = new CameraOverlayView.Size(bitmap.getWidth(), bitmap.getHeight());

        Bitmap finalBitmap = bitmap;
        detector.detectInImage(firebaseVisionImage).addOnSuccessListener(faceList -> {
            if (faceList.size() > 0) {
                Log.e("detected", "detected");
                // We just need the first face
                FirebaseVisionFace face = faceList.get(0);


                // Draw the bitmaps on the detected faces
                // Todo Frame size null
                // Log.e("size1",frame.getSize()+"");
                overlayView.setNewFace(face, size);
            } else {
                overlayView.setNewFace(null, size);
                Log.e("detected", "not detected");
            }

            finalBitmap.recycle();

        }).addOnFailureListener(Throwable::printStackTrace)
                .addOnCompleteListener(task -> {
                    finalBitmap.recycle();
                    Log.e("detected", "complete");
                    Utils.fade(goldShowerLayout, scannerAnimationView, 500, true);
                });
    }

    private void startFaceProcessor() {
        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                //.setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                //.setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .build();
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);

        cameraView.addFrameProcessor(frame -> {

            Size frameSize = frame.getSize();

            if (frame.getSize() == null)
                return;

            int rotation = frame.getRotation() / 90;

            FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                    .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                    .setWidth(frame.getSize().getWidth())
                    .setHeight(frame.getSize().getHeight())
                    .setRotation(rotation)
                    .build();
            // Create vision image object, and it will be consumed by FirebaseVisionFaceDetector
            // for face detection
            FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromByteArray(frame.getData(), metadata);

            // Perform face detection
            detector.detectInImage(firebaseVisionImage).addOnSuccessListener(faceList -> {
                if (faceList.size() > 0) {
                    Log.e("detected", "detected");
                    // We just need the first face
                    FirebaseVisionFace face = faceList.get(0);


                    // Draw the bitmaps on the detected faces
                    // Todo Frame size null
                    // Log.e("size1",frame.getSize()+"");
                    //  overlayView.setNewFace(face,frameSize);
                }
               /* else
                    overlayView.setNewFace(null,frameSize);*/
            }).addOnFailureListener(Throwable::printStackTrace);
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if (cameraView != null)
            cameraView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraView != null)
            cameraView.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraView != null)
            cameraView.destroy();
        if (refresherTimer != null)
            refresherTimer.cancel();

        if (goldShowAdapter != null)
            goldShowAdapter.dispose();

        if (ssPostTimer != null)
            ssPostTimer.cancel();

        getSignalManager().removeReceiver(this);

    }

    private GoldDownloader getGoldDownloader() {
        return ((AdvertiserApplication) getActivity().getApplication()).getGoldDownloader();
    }

    private Parser getParser() {
        try {
            return ((AdvertiserApplication) getActivity().getApplication()).getParser();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void takeScreenshot() {

        try {

            long now = System.currentTimeMillis();
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/advertiser/screenShots/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getActivity().getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            sendScreenShot(imageFile);

        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private synchronized void sendScreenShot(File imageFile) {
        if (!imageFile.exists()) return;
        if (onSendScreenShots.containsKey(imageFile.getPath())) return;

        ScreenShot screenShot = new ScreenShot();
        screenShot.setFile(imageFile.getPath());
        screenShot.setTimestamp(System.currentTimeMillis());

        onSendScreenShots.put(screenShot.getFile(), screenShot);
        getNetworkManager().postScreenShot(screenShot, new OnResultLoaded.ActionListener() {
            @Override
            public void onSuccess(Object successResult) {
                getActivity().runOnUiThread(() -> {
                    FileUtils.deleteQuietly(imageFile);
                    onSendScreenShots.remove(screenShot.getFile());
                });
            }

            @Override
            public void onError(ErrorObject error) {
                Log.e("ScreenShot", "send screenShot Error");
                getActivity().runOnUiThread(() -> onSendScreenShots.remove(screenShot.getFile()));
            }

            @Override
            public void failed(Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> onSendScreenShots.remove(screenShot.getFile()));
            }
        });
    }

    @Override
    public boolean onSignal(Signal signal) {
        if (signal.getType() == Signal.GOLD_TITLE_LOADED) {
            title = (String) signal.getExtras();
            setTitle();
            return true;
        }
        return false;
    }
}
