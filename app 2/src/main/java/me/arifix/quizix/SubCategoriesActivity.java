package me.arifix.quizix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import me.arifix.quizix.Network.QueryCallback;
import me.arifix.quizix.Utils.Common;
import me.arifix.quizix.Utils.Config;
import com.crashlytics.android.Crashlytics;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.wang.avi.AVLoadingIndicatorView;

import me.arifix.quizix.Adapter.SubCategoryAdapter;
import me.arifix.quizix.Model.Category;
import me.arifix.quizix.Network.ApiService;
import me.arifix.quizix.Network.NetworkCall;
import me.arifix.quizix.Utils.SharedPref;
import me.arifix.quizix.Utils.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SubCategoriesActivity extends AppCompatActivity {
    private static final String TAG = "SubCategoriesActivity";
    private static final boolean SHOW_BANNER_AD = true;
    private static final boolean SHOW_INTERSTITIAL_AD = true;

    private boolean isPurchased;

    private RecyclerView.LayoutManager layoutManager;
    private SubCategoryAdapter adapter;
    private SharedPref sharedPref;
    private ShareDialog shareDialog;
    private InterstitialAd interstitialAds;

    @BindView(R.id.listSubCategory)
    RecyclerView listSubCategory;
    @BindView(R.id.loader)
    AVLoadingIndicatorView loader;
    @BindView(R.id.adsBanner)
    AdView adsBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Fabric Crashlytics SDK
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_sub_category);

        // Set ActionBar Title & Add back icon on ActionBar
        String category = getIntent().getStringExtra(Config.CATEGORY);
        if (category != null) {
            setTitle(category);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Center ActionBar Title
        Utils.centerActionbarTitle(this);

        // Initialize Logger Library
        Logger.addLogAdapter(new AndroidLogAdapter());

        // Initialize Butterknife Library
        ButterKnife.bind(this);

        // Initialize Calligraphy Library with default Font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // Initialize Shared Preference
        sharedPref = SharedPref.getPreferences(this);

        // Initialize Facebook Share Dialog
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
            if (SHOW_BANNER_AD) {
                showBannerAd();
            }
            if (SHOW_INTERSTITIAL_AD) {
                AdRequest adRequest = new AdRequest.Builder().build();
                interstitialAds = new InterstitialAd(this);
                interstitialAds.setAdUnitId(getString(R.string.ads_interstitial_id));
                interstitialAds.loadAd(adRequest);
            }
        }


        // Show Loader while getting Data through API Call
        loader.show();

        // API Call to get Categories information from Server
        String categoryId = getIntent().getStringExtra(Config.CATEGORY_ID);
        if (category != null && categoryId != null) {
            getDataFromServer(category, categoryId);
        } else {
            finish();
        }
    }

    // Get Categories Data from Server - API Call
    private void getDataFromServer(final String categoryName, String categoryId) {
        // Show all Subcategories if Purchased
        String type;
        if (isPurchased) {
            type = Config.API_SUFFIX_ALL;
        } else {
            type = Config.API_SUFFIX_FREE;
        }

        // Get Data of Category
        if (categoryId != null) {
            ApiService apiService = new NetworkCall();
            apiService.getSubCategories(Integer.valueOf(categoryId), type, new QueryCallback<List<Category>>() {
                @Override
                public void onSuccess(List<Category> data) {
                    String categories = new Gson().toJson(data);
                    sharedPref.setStringData(categoryName, categories);
                    layoutManager = new LinearLayoutManager(getApplicationContext());
                    listSubCategory.setLayoutManager(layoutManager);
                    listSubCategory.setHasFixedSize(true);
                    adapter = new SubCategoryAdapter(getApplicationContext(), data, isPurchased);
                    listSubCategory.setAdapter(adapter);

                    // Hide Loader
                    if (loader.isShown()) {
                        loader.hide();
                    }
                }

                @Override
                public void onError(Throwable th) {
                    String subCategortData = sharedPref.getStringData(categoryName, "");
                    if (!subCategortData.isEmpty()) {
                        Type categoriesListType = new TypeToken<ArrayList<Category>>() {
                        }.getType();
                        List<Category> subcategories = new Gson().fromJson(subCategortData, categoriesListType);

                        layoutManager = new LinearLayoutManager(getApplicationContext());
                        listSubCategory.setLayoutManager(layoutManager);
                        listSubCategory.setHasFixedSize(true);
                        adapter = new SubCategoryAdapter(getApplicationContext(), subcategories, isPurchased);
                        listSubCategory.setAdapter(adapter);
                    } else {
                        Toast.makeText(SubCategoriesActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
                    }

                    // Hide Loader
                    if (loader.isShown()) {
                        loader.hide();
                    }
                }
            });
        }
    }

    // Show Banner Ads
    private void showBannerAd() {
        if (!getString(R.string.ads_app_id).isEmpty()) {
            MobileAds.initialize(this, getString(R.string.ads_app_id));
            AdRequest adRequest = new AdRequest.Builder().build();
            adsBanner.loadAd(adRequest);
            adsBanner.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    adsBanner.setVisibility(View.VISIBLE);
                }
            });
        }
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
        // Pause Banner Ads
        if (adsBanner != null) {
            adsBanner.pause();
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume Banner Ads
        if (adsBanner != null) {
            adsBanner.resume();
        }
    }

    @Override
    public void onDestroy() {
        // Destroy Banner Ads
        if (adsBanner != null) {
            adsBanner.destroy();
        }

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
            Intent setting = new Intent(SubCategoriesActivity.this, SettingActivity.class);
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
