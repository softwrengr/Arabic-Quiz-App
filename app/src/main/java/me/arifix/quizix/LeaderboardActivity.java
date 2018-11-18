package me.arifix.quizix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import me.arifix.quizix.Utils.Common;
import me.arifix.quizix.Utils.Config;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.crashlytics.android.Crashlytics;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import me.arifix.quizix.Fragment.LeadersFragment;
import me.arifix.quizix.Fragment.ScoresFragment;
import me.arifix.quizix.Utils.SharedPref;
import me.arifix.quizix.Utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LeaderboardActivity extends AppCompatActivity {
    private static final String TAG = "LeaderboardActivity";
    private static final boolean SHOW_INTERSTITIAL_AD = true;

    private boolean isPurchased;

    private SharedPref sharedPref;
    private ShareDialog shareDialog;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private InterstitialAd interstitialAds;

    @BindView(R.id.bnScoreMenu)
    AHBottomNavigation bnScoreMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Fabric Crashlytics SDK
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_leaderboard);

        // Set ActionBar Title & Add back icon on ActionBar
        setTitle(getString(R.string.leaderboard));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Center ActionBar Title
        Utils.centerActionbarTitle(this);

        // Initialize Logger Library for Logging
        Logger.addLogAdapter(new AndroidLogAdapter());

        // Initialize Butter-knife Library for field and method binding for Android views
        ButterKnife.bind(this);

        // Calligraphy Library to add custom Font support, Define default font here
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // Initialize Shared Preference
        sharedPref = SharedPref.getPreferences(this);

        // Initialize Facebook SDK
        shareDialog = new ShareDialog(this);

        // Check if Purchased(Premium)
        isPurchased = sharedPref.getBoolData(Config.IS_PURCHASED, false);

        // Block Application if no Internet Connection & Internet-only Mode Activated
        if (Config.INTERNET_ONLY) {
            Common.blockAppIfNoIntenet(this);
        }

        // Initialize AdMob Ads
        if (Config.SHOW_ADS && !isPurchased) {
            MobileAds.initialize(getApplicationContext(), getString(R.string.ads_app_id));
            if (SHOW_INTERSTITIAL_AD) {
                AdRequest adRequest = new AdRequest.Builder().build();
                interstitialAds = new InterstitialAd(this);
                interstitialAds.setAdUnitId(getString(R.string.ads_interstitial_id));
                interstitialAds.loadAd(adRequest);
            }
        }

        // Bottom Navigation & Fargment reltaed things
        Fragment leaders = new LeadersFragment();
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.replace(R.id.frameLeaderboard, leaders, Config.TAG_LEADERS_FRAGMENT);
        ft.addToBackStack(Config.TAG_LEADERS_FRAGMENT);
        ft.commit();


        // Create items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.leaderboard, R.drawable.icon_trophy, R.color.colorTeal);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.scores, R.drawable.icon_scorecard, R.color.colorBrown);

        // Add items
        bnScoreMenu.addItem(item1);
        bnScoreMenu.addItem(item2);

        // Set background color
        bnScoreMenu.setDefaultBackgroundColor(getResources().getColor(R.color.colorBrown));

        // Disable the translation inside the CoordinatorLayout
        bnScoreMenu.setBehaviorTranslationEnabled(false);

        // Force to tint the drawable (useful for font with icon for example)
        bnScoreMenu.setForceTint(true);

        // Display color under navigation bar (API 21+)
        bnScoreMenu.setTranslucentNavigationEnabled(true);

        // Manage titles
        bnScoreMenu.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        // Use colored navigation with circle reveal effect
        bnScoreMenu.setColored(true);

        // Set current item programmatically
        bnScoreMenu.setCurrentItem(0);

        // Set listeners
        bnScoreMenu.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                Fragment leaders = new LeadersFragment();
                Fragment scores = new ScoresFragment();
                fm = getSupportFragmentManager();
                ft = fm.beginTransaction();

                // OnChange Tab Change Fragment
                switch (position) {
                    case 0:
                        ft.replace(R.id.frameLeaderboard, leaders, Config.TAG_LEADERS_FRAGMENT);
                        ft.addToBackStack(Config.TAG_LEADERS_FRAGMENT);
                        break;
                    case 1:
                        ft.replace(R.id.frameLeaderboard, scores, Config.TAG_SCORES_FRAGMENT);
                        ft.addToBackStack(Config.TAG_SCORES_FRAGMENT);
                        break;
                }

                ft.commit();
                return true;
            }
        });
        bnScoreMenu.setOnNavigationPositionListener(new AHBottomNavigation.OnNavigationPositionListener() {
            @Override
            public void onPositionChange(int y) {
                Logger.d(TAG + ": setOnNavigationPositionListener");
            }
        });
    }

    // Show Interstitial Ads
    private void showInterstitialAds() {
        if (Config.SHOW_ADS && !isPurchased) {
            if (interstitialAds.isLoaded()) {
                interstitialAds.show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // OnClick Setting icon open SettingActivity
        if (id == R.id.settings) {
            Intent setting = new Intent(LeaderboardActivity.this, SettingActivity.class);
            startActivity(setting);
        }

        // OnClick Share icon open Share Dialog
        if (id == R.id.share) {
            Common.showAppShareDialog(this, shareDialog);
        }

        return super.onOptionsItemSelected(item);
    }

    // When back icon pressed from ActionBar
    @Override
    public void onBackPressed() {
        if (Config.SHOW_ADS && SHOW_INTERSTITIAL_AD) {
            showInterstitialAds();
        }

        super.onBackPressed();
        this.finish();
    }

    // Add back icon press Support
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Calligraphy Library- Inject into Context
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
