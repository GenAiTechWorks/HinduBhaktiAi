package com.hindu.lordpromptsai.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.hindu.lordpromptsai.adapter.ImageAdapter;
import com.hindu.lordpromptsai.adapter.ImageItem;
import com.hindu.lordpromptsai.util.JsonUtils;
import com.hindu.lordpromptsai.R;
import com.hindu.lordpromptsai.util.TabData;
import com.hindu.lordpromptsai.audio.BackgroundVideoPlayer;
import com.hindu.lordpromptsai.audio.LordMusicManager;
import com.hindu.lordpromptsai.dao.FavoriteDao;
import com.hindu.lordpromptsai.entity.FavoriteEntity;
import com.hindu.lordpromptsai.repository.AppDatabase;
import com.hindu.lordpromptsai.tutorial.TutorialHelper;

import com.hindu.lordpromptsai.util.AppExecutors;
import com.hindu.lordpromptsai.util.InterstitialAdManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.MaterialToolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends BaseActivity {

    DrawerLayout drawerLayout;
    MaterialToolbar toolbar;
    TabLayout tabLayout;
    RecyclerView recyclerView;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    private List<TabData> tabDataList;
    private ImageAdapter adapter;
    private int currentTabIndex = 0;
    private View emptyFavoritesView;
    private GestureDetector tabSwipeDetector;
    private BackgroundVideoPlayer backgroundVideoPlayer;
    private boolean wasOnFavoritesTab = false;

    private static final String KEY_SELECTED_TAB = "selected_tab_index";
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_LAST_TAB_INDEX = "last_selected_tab";

    private AppUpdateManager appUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check version upgrade
        checkForAppUpdate();
        // Initialize AdMob
        MobileAds.initialize(this, initializationStatus -> {});
        InterstitialAdManager.load(this);
        tabDataList = JsonUtils.loadTabsFromJson(this);

        if (tabDataList.isEmpty()) {
            Toast.makeText(this, "No data loaded", Toast.LENGTH_LONG).show();
            return;
        }

        setContentView(R.layout.activity_main);

        setupAd(findViewById(android.R.id.content));
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.recyclerView);
        navigationView = findViewById(R.id.navigationView);
        emptyFavoritesView = findViewById(R.id.emptyFavoritesView);
        MaterialButton btnBrowseImages = emptyFavoritesView.findViewById(R.id.btnBrowseImages);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // 2️⃣ VERY IMPORTANT (forces ActionBar to exist)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        PlayerView textureView = findViewById(R.id.backgroundVideo);

        backgroundVideoPlayer = new BackgroundVideoPlayer(this, R.raw.starbg);
        backgroundVideoPlayer.attach(textureView);
        backgroundVideoPlayer.play(); // 🔥 start immediately

        btnBrowseImages.setOnClickListener(v -> {
            TabLayout.Tab firstTab = tabLayout.getTabAt(0);
            if (firstTab != null) {
                firstTab.select();
            }
        });

        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        // 4️⃣ THIS LINE IS CRITICAL (MOST PEOPLE MISS THIS)
        toggle.setDrawerIndicatorEnabled(true);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        toolbar.setNavigationIcon(R.drawable.ic_menu_large);

        toolbar.setNavigationOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );
        toolbar.setNavigationContentDescription(
                getString(R.string.cd_open_navigation)
        );

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.appBarMainActivity),
                (view, insets) -> {
                    Insets statusBars =
                            insets.getInsets(WindowInsetsCompat.Type.statusBars());

                    view.setPadding(
                            view.getPaddingLeft(),
                            statusBars.top,
                            view.getPaddingRight(),
                            view.getPaddingBottom()
                    );
                    return insets;
                }
        );


        int originalBottomPadding = emptyFavoritesView.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(emptyFavoritesView, (v, insets) -> {
            Insets navBars =
                    insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    originalBottomPadding
                            + navBars.bottom
                            + getResources().getDimensionPixelSize(R.dimen.spacing_xlarge)
            );
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.adContainer),
                (view, insets) -> {

                    Insets navBars =
                            insets.getInsets(WindowInsetsCompat.Type.navigationBars());

                    view.setPadding(
                            view.getPaddingLeft(),
                            view.getPaddingTop(),
                            view.getPaddingRight(),
                            navBars.bottom
                    );
                    return insets;
                }
        );

        // Modern back handling
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });

        for (TabData ignored : tabDataList) {
            tabLayout.addTab(tabLayout.newTab());
        }

        for (int i = 0; i < tabDataList.size(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                View tabView = LayoutInflater.from(this)
                        .inflate(R.layout.tab_item, null);

                TextView tabText = tabView.findViewById(R.id.tabText);
                tabText.setText(tabDataList.get(i).title());

                tab.setCustomView(tabView);
            }
        }

        tabLayout.post(() -> {
            ViewGroup tabStrip = (ViewGroup) tabLayout.getChildAt(0);

            for (int i = 0; i < tabStrip.getChildCount(); i++) {

                View tabView = tabStrip.getChildAt(i);

                // 🔥 REMOVE TABLELAYOUT'S INTERNAL BACKGROUND
                tabView.setBackground(null);

                // 🔥 REMOVE INTERNAL PADDING
                tabView.setPadding(0, 0, 5, 0);

                // 🔥 ADD SPACE BETWEEN PILLS
                ViewGroup.MarginLayoutParams params =
                        (ViewGroup.MarginLayoutParams) tabView.getLayoutParams();
                params.setMargins(8, 0, 8, 0);
                tabView.setLayoutParams(params);
            }
        });

        ViewGroup tabStrip = (ViewGroup) tabLayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            View tabView = tabStrip.getChildAt(i);
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) tabView.getLayoutParams();
            params.setMargins(8, 0, 8, 0); // spacing between pills
            tabView.setLayoutParams(params);
        }
        recyclerView = findViewById(R.id.recyclerView);

        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(
                        2,
                        StaggeredGridLayoutManager.VERTICAL
                );

// 🔥 THIS LINE FIXES 90% ISSUES
        layoutManager.setGapStrategy(
                StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        );

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null); // prevents jump animation
        recyclerView.setItemViewCacheSize(4);

        // 1️⃣ Register launcher FIRST
        ActivityResultLauncher<Intent> imageDetailLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode() != Activity.RESULT_OK
                                    || result.getData() == null) return;

                            boolean favChanged =
                                    result.getData().getBooleanExtra(
                                            "FAVORITE_CHANGED", false
                                    );

                            TabData currentTab = tabDataList.get(currentTabIndex);
                            if ("Favorites".equals(currentTab.title())) {
                                if (!LordMusicManager.getInstance().isMuted()) {
                                    String tabName = tabDataList.get(currentTabIndex).title();
                                    LordMusicManager.getInstance().playForTab(MainActivity.this, tabName);
                                }
                            }
                            if (!favChanged) return;

                            // 🔥 ALWAYS sync icons
                            syncAllTabsWithDB();

                            // 🔥 IF currently on Favorites → reload DB
                            if ("Favorites".equals(currentTab.title())) {
                                loadFavoritesFromDB();
                            }
                        }
                );

        // 2️⃣ THEN create adapter
        adapter = new ImageAdapter(
                this,
                tabDataList.get(0).images(),
                imageDetailLauncher,
                tabDataList.get(0).title()
        );

        recyclerView.setAdapter(adapter);
        // 🔥 If Favorites tab exists, refresh it
      //adapter.onFavoriteChanged = this::refreshFavoritesTab;

        adapter.onFavoriteChanged = () -> {
            syncAllTabsWithDB();        // 🔥 NEW
            if (tabLayout.getSelectedTabPosition()
                    == tabDataList.indexOf(getFavoritesTab())) {
                loadFavoritesFromDB();
            }
        };

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabIndex = tab.getPosition();

                // ✅ SAVE SELECTED TAB
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putInt(KEY_LAST_TAB_INDEX, currentTabIndex)
                        .apply();

                TabData selectedTab = tabDataList.get(currentTabIndex);
                wasOnFavoritesTab = "Favorites".equals(selectedTab.title());
                adapter.setFavoritesTab(wasOnFavoritesTab);
                tab.view.performHapticFeedback(
                        android.view.HapticFeedbackConstants.VIRTUAL_KEY
                );

                if (wasOnFavoritesTab) {
                    loadFavoritesFromDB(); // 🔥 ONLY SOURCE
                } else {
                    emptyFavoritesView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setFavoritesTab(false);
                    recyclerView.getRecycledViewPool().clear();
                    adapter.updateImages(selectedTab.images());
                    adapter.setTabTitle(selectedTab.title());
                    // ✅ ALWAYS START FROM TOP
                    recyclerView.scrollToPosition(0);
                }

                syncAllTabsWithDB();

                // 🎵 PLAY LORD MUSIC
                if (!LordMusicManager.getInstance().isMuted()) {
                    String tabName = tabDataList.get(currentTabIndex).title();
                    LordMusicManager.getInstance().playForTab(MainActivity.this, tabName);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        int restoredTabIndex;

        if (savedInstanceState != null) {
            // 🔁 Rotation / temporary recreate
            restoredTabIndex = savedInstanceState.getInt(KEY_SELECTED_TAB, 0);
        } else {
            // 🔁 App restart / fresh launch
            restoredTabIndex = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .getInt(KEY_SELECTED_TAB, 0);
        }

        // ✅ Select tab safely
        if (restoredTabIndex < tabLayout.getTabCount()) {
            TabLayout.Tab tab = tabLayout.getTabAt(restoredTabIndex);
            if (tab != null) tab.select();
        }

        // ---------------- 🔥 TAB SWIPE DETECTOR ----------------
        tabSwipeDetector =
                new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

                    private final int SWIPE_THRESHOLD =
                            (int) (getResources().getDisplayMetrics().density * 100);
                    private static final int SWIPE_VELOCITY_THRESHOLD = 120;

                    @Override
                    public boolean onFling(
                            MotionEvent e1,
                            @NonNull MotionEvent e2,
                            float velocityX,
                            float velocityY
                    ) {
                        if (e1 == null) return false;

                        float diffX = e2.getX() - e1.getX();
                        float diffY = e2.getY() - e1.getY();

                        // ✅ ONLY horizontal swipe
                        if (Math.abs(diffX) > Math.abs(diffY)
                                && Math.abs(diffX) > SWIPE_THRESHOLD
                                && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                            if (diffX > 0) {
                                moveToPreviousTab();
                            } else {
                                moveToNextTab();
                            }
                            return true;
                        }
                        return false;
                    }
                });

// ---------------- 🔥 ATTACH TO RECYCLERVIEW ----------------
        recyclerView.addOnItemTouchListener(
                new RecyclerView.OnItemTouchListener() {

                    @Override
                    public boolean onInterceptTouchEvent(
                            @NonNull RecyclerView rv,
                            @NonNull MotionEvent e
                    ) {
                        // Let GestureDetector inspect touch events
                        if (e.getPointerCount() == 1) {
                            tabSwipeDetector.onTouchEvent(e);
                        }
                        return false; // ❗ do NOT intercept → allow clicks & scroll
                    }

                    @Override
                    public void onTouchEvent(
                            @NonNull RecyclerView rv,
                            @NonNull MotionEvent e
                    ) {
                        // no-op
                    }

                    @Override
                    public void onRequestDisallowInterceptTouchEvent(
                            boolean disallowIntercept
                    ) {
                        // no-op
                    }
                }
        );


        // ---------------- Navigation Drawer ----------------
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_help_us) {
                startActivity(new Intent(this, HowToUseActivity.class));
            }else if (item.getItemId() == R.id.nav_about_app) {
                startActivity(new Intent(this, AboutActivity.class));
            }else if (id == R.id.nav_rate) {
                startActivity(new Intent(this, RateUsActivity.class));
            }else if (id == R.id.nav_privacy) {
                startActivity(new Intent(this, PrivacyPolicyActivity.class));
            }else if (item.getItemId() == R.id.navCredits) {
                startActivity(new Intent(this, CreditsActivity.class));
            }/*else if (item.getItemId() == R.id.nav_other_apps) {
                startActivity(new Intent(this, OurOtherAppsActivity.class));
            }*/

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });


        AdView adView = findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

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


        recyclerView.post(() -> {

            RecyclerView.ViewHolder vh =
                    recyclerView.findViewHolderForAdapterPosition(3);

            // 🔥 Get FIRST TAB (Krishna)
            TabLayout.Tab krishnaTab = tabLayout.getTabAt(0);
            View krishnaTabView = null;

            if (krishnaTab != null) {
                krishnaTabView = krishnaTab.view;
            }

            if (vh != null && krishnaTabView != null) {

                TutorialHelper.showIfFirstTime(
                        this,
                        TutorialHelper.KEY_MAIN,
                        new View[]{
                                krishnaTabView,          // ✅ Krishna tab only
                                findViewById(R.id.cardPulseOverlay),
                                findViewById(R.id.favButton),
                                findViewById(R.id.action_mute)
                        },
                        new String[]{
                                "Lords Selection Tabs",
                                "Tap any image for full screen view and generate AI prompts",
                                "Favourites Collection",
                                "Divine Bhakti Music"
                        },
                        new String[]{
                                "Select each individual Tab to view beautiful lords images.",
                                " ",
                                "Select to add to your favourites collection, You can view your favourites in the last tab",
                                "Listen to devotional bhakti music and stay blessed."
                        }
                );
            }
        });

        LordMusicManager.getInstance().resumeIfNeeded();
        handleIntent(getIntent());

    }
    private TabData getFavoritesTab() {
        for (TabData tab : tabDataList) {
            if ("Favorites".equals(tab.title())) {
                return tab;
            }
        }
        return null;
    }

    private void moveToNextTab() {
        int next = currentTabIndex + 1;
        if (next < tabLayout.getTabCount()) {
            TabLayout.Tab tab = tabLayout.getTabAt(next);
            if (tab != null) tab.select();
        }
    }

    private void moveToPreviousTab() {
        int prev = currentTabIndex - 1;
        if (prev >= 0) {
            TabLayout.Tab tab = tabLayout.getTabAt(prev);
            if (tab != null) tab.select();
        }
    }

    @Override
    protected void onPause() {

        if (backgroundVideoPlayer != null) {
            backgroundVideoPlayer.pause(); // 🔥 pause only
        }

        AdView adView = findViewById(R.id.adView);
        if (adView != null) {
            adView.pause();
        }
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (appUpdateManager == null) return;

        if (backgroundVideoPlayer != null) {
            backgroundVideoPlayer.play(); // 🔥 resume
        }

        appUpdateManager.getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {

                    if (appUpdateInfo.updateAvailability()
                            == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {

                        appUpdateManager.startUpdateFlow(
                                appUpdateInfo,
                                this,
                                AppUpdateOptions.newBuilder(
                                        AppUpdateType.IMMEDIATE
                                ).build()
                        );
                    }
                });

        AdView adView = findViewById(R.id.adView);
        if (adView != null) {
            adView.resume();
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }

        invalidateOptionsMenu();
    }

    @Override
    protected void onDestroy() {
        TutorialHelper.clear();

        if (backgroundVideoPlayer != null) {
            backgroundVideoPlayer.release();
            backgroundVideoPlayer = null;
        }

        AdView adView = findViewById(R.id.adView);
        if (adView != null) {
            adView.destroy();
        }

        super.onDestroy();

        // ✅ Stop music ONLY when app is really finishing
        if (!isChangingConfigurations() && isFinishing()) {
            LordMusicManager.getInstance().stop();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actions, menu);

        MenuItem musicItem = menu.findItem(R.id.action_mute);
        updateMusicIcon(musicItem);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (item.getItemId() == R.id.action_mute) {

            LordMusicManager manager = LordMusicManager.getInstance();
            manager.toggleMute();

            if (!manager.isMuted()) {
                int pos = tabLayout.getSelectedTabPosition();
                if (pos >= 0) {
                    manager.playForTab(this, tabDataList.get(pos).title());
                }
            }

            invalidateOptionsMenu();
            return true;
        }
        else if (id == R.id.action_favorites) {
            openFavoritesTab();
            return true;
        } else if (item.getItemId() == R.id.action_share) {
            shareApp();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem muteItem = menu.findItem(R.id.action_mute);

        if (muteItem != null) {
            muteItem.setIcon(
                    LordMusicManager.getInstance().isMuted()
                            ? R.drawable.outline_music_off
                            : R.drawable.outline_music_on
            );
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void openFavoritesTab() {
        for (int i = 0; i < tabDataList.size(); i++) {
            if ("Favorites".equalsIgnoreCase(tabDataList.get(i).title())) {

                TabLayout.Tab favTab = tabLayout.getTabAt(i);
                if (favTab != null) {
                    favTab.select();   // ✅ instantly switch
                }

                return;
            }
        }

        // Optional safety (should not happen)
        Toast.makeText(this, "Favorites tab not found", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, tabLayout.getSelectedTabPosition());
    }


    private void handleIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("OPEN_FAVORITES", false)) {

            // 1️⃣ Always reload favorites from DB
            loadFavoritesFromDB();

            // 2️⃣ Switch to Favorites tab AFTER data load
            TabData favoritesTab = getFavoritesTab();
            if (favoritesTab != null) {
                int index = tabDataList.indexOf(favoritesTab);
                if (index >= 0 && index < tabLayout.getTabCount()) {
                    Objects.requireNonNull(tabLayout.getTabAt(index)).select();
                }
                if (!LordMusicManager.getInstance().isMuted()) {
                    String tabName = tabDataList.get(currentTabIndex).title();
                    LordMusicManager.getInstance().playForTab(MainActivity.this, tabName);
                }
            }
        }
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // 🔥 REQUIRED
        handleIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (backgroundVideoPlayer != null) {
            backgroundVideoPlayer.pause();
        }
    }


    private void loadFavoritesFromDB() {

        AppExecutors.IO.execute(() -> {

            FavoriteDao dao = AppDatabase.get(this).favoriteDao();
            List<FavoriteEntity> dbFavorites = dao.getAllFavoritesSorted();

            List<ImageItem> favorites = new ArrayList<>();

            for (FavoriteEntity f : dbFavorites) {
                favorites.add(new ImageItem(
                        Integer.parseInt(f.getImageResId()),
                        f.getImageName(),
                        f.getDescription(),
                        true
                ));
            }

            runOnUiThread(() -> {

                if (adapter.isTabFavorite()
                        || adapter.getItemCount() != favorites.size()) {
                    adapter.setFavoritesTab(true);
                    adapter.updateImages(favorites);
                    recyclerView.scrollToPosition(0);
                }

                if (favorites.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyFavoritesView.setVisibility(View.VISIBLE);
                } else {
                    emptyFavoritesView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void checkForAppUpdate() {

        appUpdateManager = AppUpdateManagerFactory.create(this);

        appUpdateManager.getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {

                    if (appUpdateInfo.updateAvailability()
                            == UpdateAvailability.UPDATE_AVAILABLE
                            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                        appUpdateManager.startUpdateFlow(
                                appUpdateInfo,
                                this, // ✅ Activity
                                AppUpdateOptions.newBuilder(
                                        AppUpdateType.IMMEDIATE
                                ).build()
                        );
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void syncAllTabsWithDB() {

        AppExecutors.IO.execute(() -> {

            FavoriteDao dao = AppDatabase.get(this).favoriteDao();
            Set<String> favSet =
                    new HashSet<>(dao.getAllFavoriteNames());

            for (TabData tab : tabDataList) {
                if ("Favorites".equals(tab.title())) continue;

                for (ImageItem item : tab.images()) {
                    item.setFavorite(
                            favSet.contains(item.getImageName())
                    );
                }
            }

            runOnUiThread(() -> {
                if (adapter.isTabFavorite()) {
                    adapter.notifyItemRangeChanged(
                            0,
                            adapter.getItemCount(),
                            ImageAdapter.PAYLOAD_FAVORITE
                    );
                }
            });
        });
    }
}