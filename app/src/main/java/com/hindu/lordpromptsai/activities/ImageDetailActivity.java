package com.hindu.lordpromptsai.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.ui.PlayerView;

import com.hindu.lordpromptsai.adapter.ImageItem;
import com.hindu.lordpromptsai.R;
import com.hindu.lordpromptsai.audio.BackgroundVideoPlayer;
import com.hindu.lordpromptsai.audio.LordMusicManager;
import com.hindu.lordpromptsai.dao.FavoriteDao;
import com.hindu.lordpromptsai.entity.FavoriteEntity;
import com.hindu.lordpromptsai.repository.AppDatabase;
import com.hindu.lordpromptsai.tutorial.TutorialHelper;
import com.hindu.lordpromptsai.util.AppExecutors;
import com.hindu.lordpromptsai.util.ClickTracker;
import com.hindu.lordpromptsai.util.InterstitialAdManager;
import com.hindu.lordpromptsai.util.UtilityHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

public class ImageDetailActivity extends BaseActivity {

    ImageItem currentImageItem;
    private CardView imageCard;
    ImageView detailImage;
    ImageButton favButton;
    TextView descriptionText, detailTitle;
    private TextView imageCounter;
    private ArrayList<ImageItem> imageList;
    private int currentIndex;
    private boolean uiVisible = true;
    private MaterialButton btnPromptAction;
    private boolean isPromptGenerated = false;
    private String fullPromptText; // your description text
    private FrameLayout promptContainer;
    private Handler typingHandler;
    private boolean isPromptGenerating = false;
    private View imagePulseOverlay;
    private boolean favoriteChanged = false;
    private boolean isFirstLoad = true;

    private boolean isSwipeFromRight = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        setupAd(findViewById(android.R.id.content));

        imageCard = findViewById(R.id.imageCard);
        detailImage = findViewById(R.id.detailImage);
        favButton = findViewById(R.id.favButton);
        imageCounter = findViewById(R.id.imageCounter);

        detailTitle = findViewById(R.id.detailTitle);
        View btnHelpDetails = findViewById(R.id.btnHelpDetails);
        ImageButton btnYoutube = findViewById(R.id.btnYoutube);
        imageList = getIntent().getParcelableArrayListExtra("imageList");
        currentIndex = getIntent().getIntExtra("position", 0);
        promptContainer = findViewById(R.id.promptContainer);
        descriptionText = findViewById(R.id.descriptionText);
        btnPromptAction = findViewById(R.id.btnPromptAction);
        typingHandler = new Handler(Looper.getMainLooper());
        imagePulseOverlay = findViewById(R.id.imagePulseOverlay);
        btnPromptAction.setText(R.string.generate_prompt);
        btnPromptAction.setEnabled(true);


        btnPromptAction.setOnClickListener(v -> {

            if (!isPromptGenerated) {
                startPromptGeneration();
            } else {
                copyPromptToClipboard();
                showOpenPromptCenterDialog();
                performHapticFeedback(v);
            }
        });

        PlayerView textureView = findViewById(R.id.backgroundVideoView);

        BackgroundVideoPlayer backgroundVideoPlayer = new BackgroundVideoPlayer(this, R.raw.bgfull);

        backgroundVideoPlayer.attach(textureView);


        AdView adView = findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
       // adView.loadAd(adRequest);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) {
                adView.loadAd(adRequest);
            }
        }, 600);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // ✅ Show ad container
                findViewById(R.id.adContainer).setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                // ✅ Hide ad container when no internet / no fill
                findViewById(R.id.adContainer).setVisibility(View.INVISIBLE);
            }
        });

        View appBar = findViewById(R.id.appBarImageActivity);

        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBars.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        View adContainer = findViewById(R.id.adContainer);

        ViewCompat.setOnApplyWindowInsetsListener(adContainer, (v, insets) -> {
            Insets navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    navBars.bottom
            );
            return insets;
        });



        // ✅ GET TAB TITLE
        String tabTitle = getIntent().getStringExtra("tabTitle");

        if (tabTitle != null && !tabTitle.isEmpty()) {
            detailTitle.setText(tabTitle);
        }

        btnYoutube.setOnClickListener(v -> {
            performHapticFeedback(v);
            openUrl("https://youtu.be/_nwlYoMxSP4");
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // important
        }

        // Back arrow click
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        if (imageList == null || imageList.isEmpty()) {
            finish();
            return;
        }
        // Initial load
        loadImage();

        // 👉 TAP IMAGE = TOGGLE UI
        imageCard.setOnClickListener(v -> toggleUI());

        // 👉 SWIPE LEFT / RIGHT
        GestureDetector detector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2,
                                           float velocityX, float velocityY) {


                        // 🚫 BLOCK swipe while prompt is generating
                        if (isPromptGenerating) {
                            return true; // consume gesture, do nothing
                        }

                        float diffX = e2.getX() - e1.getX();

                        // 👉 Swipe RIGHT → NEXT
                        if (diffX > 120) {
                            goPrevious();
                            return true;
                        }

                        // 👈 Swipe LEFT → PREVIOUS
                        if (diffX < -120) {
                            goNext();
                            return true;
                        }

                        return false;
                    }
                });

        imageCard.setOnTouchListener((v, event) -> {

            if (isPromptGenerating) {
                return true;
            }

            return detector.onTouchEvent(event);
        });

        // -------- FAVOURITE TOGGLE --------
        favButton.setOnClickListener(v -> {

            ImageItem item = imageList.get(currentIndex);
            boolean currentlyFavorite = item.isFavorite();

            // optimistic UI
            favButton.setImageResource(
                    currentlyFavorite
                            ? R.drawable.ic_favorite_border
                            : R.drawable.ic_favorite
            );

            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

            AppExecutors.IO.execute(() -> {

                FavoriteDao dao =
                        AppDatabase.get(getApplicationContext()).favoriteDao();

                if (currentlyFavorite) {
                    dao.deleteByImageName(item.getImageName());
                } else {
                    String tabName = getFormattedTitle(item.getImageName());
                    dao.insert(new FavoriteEntity(
                            item.getImageName(),
                            tabName,
                            UtilityHelper.getTabOrder(tabName),
                            UtilityHelper.getImageIndex(item.getImageName()),
                            String.valueOf(item.getImageResId()),
                            item.getDescription(),
                            true
                    ));
                }

                // update model AFTER DB success
                item.setFavorite(!currentlyFavorite);

                // 🔥 MARK CHANGE
                favoriteChanged = true;


                runOnUiThread(() -> {
                    Intent result = new Intent();
                    result.putExtra("FAVORITE_CHANGED", true);
                    result.putParcelableArrayListExtra("imageList", imageList);
                    setResult(Activity.RESULT_OK, result);
                });
            });

            Snackbar.make(
                    imageCard,
                    currentlyFavorite ? "Removed from favourites" : "Added to favourites",
                    Snackbar.LENGTH_SHORT
            ).show();

            favButton.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.fav_pop)
            );
        });

// -------- MODERN BACK HANDLING --------
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        returnResultAndFinish();
                    }
                }
        );

        btnHelpDetails.setOnClickListener(v -> {
            performHapticFeedback(v);
            showHowToUseDialog();
        });

    }

    private void autoScrollDescription() {
        ScrollView scrollView = findViewById(R.id.promptScrollView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }
    private void startPromptGeneration() {

        // 🔒 prevent double start
        if (isPromptGenerating) return;

        isPromptGenerating = true;
        isPromptGenerated = false;

        btnPromptAction.setEnabled(false);
        btnPromptAction.setText(R.string.prompt_generating);
        btnPromptAction.setIconResource(R.drawable.outline_arrow_upload_ready_24);
        btnPromptAction.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        btnPromptAction.setIconTintResource(android.R.color.white);

        promptContainer.setVisibility(View.VISIBLE);
        promptContainer.setAlpha(0f);
        promptContainer.animate().alpha(1f).setDuration(200).start();
        descriptionText.setText("");

        fullPromptText = currentImageItem.getDescription();
        StringBuilder builder = new StringBuilder();

        final int delay = 5;

        typingHandler.removeCallbacksAndMessages(null); // 🔥 SAFETY

        for (int i = 0; i < fullPromptText.length(); i++) {
            final int index = i;

            typingHandler.postDelayed(() -> {

                if (!isPromptGenerating) return;

                builder.append(fullPromptText.charAt(index));
                descriptionText.setText(getString(
                        R.string.typing_cursor,
                        builder.toString()
                ));

                autoScrollDescription();

                if (index == fullPromptText.length() - 1) {
                    onPromptGenerated();
                }

            }, (long) delay * i);
        }
    }
    private void onPromptGenerated() {

        isPromptGenerating = false;
        isPromptGenerated = true;

        // 🔥 SAVE STATE INTO IMAGE ITEM
        currentImageItem.setPromptGenerated(true);
        currentImageItem.setGeneratedPrompt(fullPromptText);

        descriptionText.setText(fullPromptText);

        btnPromptAction.setText(R.string.copy_prompt);
        btnPromptAction.setIconResource(R.drawable.outline_content_copy_24);
        btnPromptAction.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        btnPromptAction.setBackgroundResource(R.drawable.bg_copy_prompt);
        btnPromptAction.setIconTintResource(android.R.color.white);

        btnPromptAction.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.pulse)
        );

        btnPromptAction.setEnabled(true);

        if (ClickTracker.shouldShowAd(this)) {
            InterstitialAdManager.show(this);
        }
    }

    private void resetPromptState() {

        isPromptGenerating = false;
        isPromptGenerated = false;

        if (typingHandler != null) {
            typingHandler.removeCallbacksAndMessages(null);
        }

        promptContainer.setVisibility(View.GONE);
        descriptionText.setText("");

        btnPromptAction.setText(R.string.generate_prompt);
        btnPromptAction.setIcon(null);
        btnPromptAction.setBackgroundResource(R.drawable.bg_copy_prompt_pink);
        btnPromptAction.setIconResource(R.drawable.outline_play_circle_24);
        btnPromptAction.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        btnPromptAction.setIconTintResource(android.R.color.white);
        btnPromptAction.setEnabled(true);
    }

    private void performHapticFeedback(View view) {
        view.performHapticFeedback(
                android.view.HapticFeedbackConstants.VIRTUAL_KEY
        );
    }

    private void copyPromptToClipboard() {
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newPlainText(
                "Image Prompt",
                descriptionText.getText().toString()
        );
        clipboard.setPrimaryClip(clip);
    }
    private void showOpenPromptCenterDialog() {

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = getLayoutInflater()
                .inflate(R.layout.open_ai_copied_prompt, null);
        dialog.setContentView(view);

        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setGravity(Gravity.CENTER);
        }

        view.findViewById(R.id.btnGemini).setOnClickListener(v ->{
                    dialog.dismiss();
                    openUrl("https://gemini.google.com");
                });


        view.findViewById(R.id.btnChatGPT).setOnClickListener(v ->
                {
                    dialog.dismiss();
                    openUrl("https://chat.openai.com");
                }
               );

        view.findViewById(R.id.btnDalle).setOnClickListener(v -> {
                    dialog.dismiss();
            openUrl("https://copilot.microsoft.com");
                }
               );

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                dialog.dismiss());

        dialog.show();
    }
    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void showHowToUseDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_how_to_use_prompt, null);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.setView(view);
        dialog.setCancelable(false);

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

   private void goNext() {
       isSwipeFromRight = true;

       boolean wrapped = false;

       currentIndex++;

       if (currentIndex >= imageList.size()) {
           currentIndex = 0; // 🔁 wrap to first
           wrapped = true;
       }
       if (wrapped) {
           detailImage.startAnimation(
                   AnimationUtils.loadAnimation(this, R.anim.fade_in)
           );
       }

       loadImage();
   }

    private void goPrevious() {

        isSwipeFromRight = false;
        currentIndex--;

        if (currentIndex < 0) {
            currentIndex = imageList.size() - 1; // 🔁 wrap to last
        }

        loadImage();
    }

    private void toggleUI() {
        uiVisible = !uiVisible;
        int anim = uiVisible ? R.anim.fade_in : R.anim.fade_out;
        imageCounter.startAnimation(AnimationUtils.loadAnimation(this, anim));
    }

    // -------- LOAD IMAGE + TEXT --------
    private void loadImage() {

        if (typingHandler != null) {
            typingHandler.removeCallbacksAndMessages(null);
        }

        currentImageItem = imageList.get(currentIndex);
        playMusic(currentImageItem);

        // Image
        detailImage.clearAnimation();
        detailImage.setImageResource(currentImageItem.getImageResId());

        if (!isFirstLoad) {
            int anim = isSwipeFromRight
                    ? R.anim.slide_from_right
                    : R.anim.slide_from_left;
            detailImage.startAnimation(
                    AnimationUtils.loadAnimation(this, anim)
            );
        } else {
            // first load → no animation
            isFirstLoad = false;
        }

        // Title counter
        imageCounter.setText(
                getString(
                        R.string.image_counter,
                        currentIndex + 1,
                        imageList.size()
                )
        );

        favButton.setImageResource(
                currentImageItem.isFavorite()
                        ? R.drawable.ic_favorite
                        : R.drawable.ic_favorite_border
        );

        // 🔥 RESTORE PROMPT STATE
        if (currentImageItem.isPromptGenerated()) {

            isPromptGenerated = true;
            isPromptGenerating = false;

            promptContainer.setVisibility(View.VISIBLE);
            descriptionText.setText(currentImageItem.getGeneratedPrompt());

            btnPromptAction.setText(R.string.copy_prompt);
            btnPromptAction.setIconResource(R.drawable.outline_content_copy_24);
            btnPromptAction.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            btnPromptAction.setBackgroundResource(R.drawable.bg_copy_prompt);
            btnPromptAction.setIconTintResource(android.R.color.white);
            btnPromptAction.setEnabled(true);

        } else {
            resetPromptState(); // only when never generated
        }

        if (currentIndex == 0) {
            showImageInteractionHint();
        }
    }

    private void playMusic(ImageItem currentImageItem) {
        // in case of favorites
        if(currentImageItem.getImageName().contains("_")) {
            String lordName = getFormattedTitle(currentImageItem.getImageName());
            detailTitle.setText(lordName);
            // 🎵 PLAY LORD MUSIC
            if (!LordMusicManager.getInstance().isMuted()) {
                LordMusicManager.getInstance().playForTab(ImageDetailActivity.this, lordName);
            }
        }else{
            String tabTitle = getIntent().getStringExtra("tabTitle");

            if (tabTitle != null && !tabTitle.isEmpty()) {
                if (!LordMusicManager.getInstance().isMuted()) {
                    LordMusicManager.getInstance().playForTab(ImageDetailActivity.this, tabTitle);
                }
            }
        }
    }

    // -------- RETURN DATA TO MAIN ACTIVITY --------
    private void returnResultAndFinish() {
        Intent data = new Intent();
        data.putExtra("FAVORITE_CHANGED", favoriteChanged);
        data.putParcelableArrayListExtra("imageList", imageList);
        setResult(Activity.RESULT_OK, data);
        detailImage.clearAnimation();
        finish();

        // Exit animation
        overridePendingTransition(0, R.anim.slide_out_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_activity, menu);
        MenuItem musicItem = menu.findItem(R.id.action_music);
        updateMusicIcon(musicItem);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem musicItem = menu.findItem(R.id.action_music);
        updateMusicIcon(musicItem);
        return super.onPrepareOptionsMenu(menu);
    }

    private void shareApp() {
        String appPackageName = getPackageName();

        String shareMessage =
                "✨ Discover Divine AI Prompts ✨\n\n" +
                        "Generate beautiful AI prompts inspired by Lords & devotion.\n\n" +
                        "👉 Download now:\n" +
                        "https://play.google.com/store/apps/details?id=" + appPackageName;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

        startActivity(Intent.createChooser(
                shareIntent,
                "Share app with friends & family"
        ));
    }

        private void showImageInteractionHint() {
        imagePulseOverlay.setVisibility(View.VISIBLE);
        imagePulseOverlay.setAlpha(1f);

        imagePulseOverlay.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.pulse_scale)
        );

        imagePulseOverlay.postDelayed(() -> imagePulseOverlay.animate()
                .alpha(0f)
                .setDuration(10)
                .withEndAction(() ->
                        imagePulseOverlay.setVisibility(View.GONE)
                )
                .start(), 100);
    }

    private String getFormattedTitle(String imageName) {
        if (imageName == null || imageName.isEmpty()) return "";

        // Split by underscore → "ganesha_1" → ["ganesha", "1"]
        String base = imageName.split("_")[0];

        // Capitalize first letter → "ganesha" → "Ganesha"
        return base.substring(0, 1).toUpperCase() + base.substring(1);
    }

    @Override
    protected void onPause() {

        // ✅ DISMISS tutorial safely
        TutorialHelper.clear();

        // 🔥 Stop all animations immediately
        detailImage.clearAnimation();
        imageCard.clearAnimation();

        // 🔥 Cancel prompt typing safely
        if (typingHandler != null) {
            typingHandler.removeCallbacksAndMessages(null);
        }

        isPromptGenerating = false;

        super.onPause();
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (detailImage != null) {
            detailImage.setImageDrawable(null); // 🔥 RELEASE bitmap
        }
    }

    @Override
    protected void onDestroy() {
       // TutorialHelper.clear(); // 🔥 ADD THIS
        super.onDestroy();
        if (detailImage != null) {
            detailImage.setImageDrawable(null);
        }

        if (typingHandler != null) {
            typingHandler.removeCallbacksAndMessages(null);
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_go_favorites) {

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("OPEN_FAVORITES", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();

            return true;
        }
        else if (item.getItemId() == R.id.action_music) {

            LordMusicManager manager = LordMusicManager.getInstance();
            manager.toggleMute();
            currentImageItem = imageList.get(currentIndex);
            playMusic(currentImageItem);

            invalidateOptionsMenu();
            return true;
        }

        if (item.getItemId() == R.id.action_share) {
            shareApp();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        invalidateOptionsMenu();

        if (detailImage.getDrawable() == null && imageList != null) {
            loadImage();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if (isFinishing() || isDestroyed()) return;

            TutorialHelper.showIfFirstTime(
                    this,
                    TutorialHelper.KEY_IMAGE,
                    new View[]{
                            findViewById(R.id.imagePulseOverlay),
                            findViewById(R.id.btnPromptAction),
                            findViewById(R.id.btnHelpDetails),
                            findViewById(R.id.btnYoutube),
                    },
                    new String[]{
                            "To move Next–Previous",
                            "Generate & Copy Prompt Instantly",
                            "Help - How to Generate Images",
                            "Watch Tutorial on YouTube",
                            "You’re All Set 🎉"
                    },
                    new String[]{
                            "Swipe Left / Right to view more beautiful images",
                            "Generate & Copy the prompt and paste it into any AI image generator.",
                            "Guideline of simple steps to create stunning AI images.",
                            "Learn how to create divine AI prompts step by step",
                            "Start creating, saving, and sharing AI prompts with ease."
                    }
            );

        }, 300);
    }
}
