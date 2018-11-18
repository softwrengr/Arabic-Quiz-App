package me.arifix.quizix;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.WindowManager;

import me.arifix.quizix.Utils.Common;
import me.arifix.quizix.Utils.Config;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import me.arifix.quizix.Utils.SharedPref;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    public static Context context;
    private Locale mylocale;

    private SharedPref sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Initialize Shared Preference
        sharedPref = SharedPref.getPreferences(this);

        setLanguage();

//        if (sharedPref.getBoolData("language",false)){
//            setLanguage();
//        }


        // Initialize Fabric Crashlytics SDK
        Fabric.with(this, new Crashlytics());

        // Add/Remove Flag from Layout Params to make Transparent StatusBar
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        // We will set layout after setting Layouts Params
        // Conditional Layout assign for Small & Regular Screen
        int screenHeight = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        int screenWidth = getApplicationContext().getResources().getDisplayMetrics().widthPixels;

        if (screenHeight <= 800 & screenWidth <= 480) {
            setContentView(R.layout.activity_splash_small);
        } else {
            setContentView(R.layout.activity_splash);
        }









        if (!Config.SPLASH_SCREEN && sharedPref.getBoolData("splashScreen", false)) {
            Intent home = new Intent(SplashActivity.this, HomeActivity.class);
            startActivity(home);
        }
        sharedPref.setBoolData(Config.SPLASH_SCREEN_VISITED, true);


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

        // Subscribe to Firebase Topic
        FirebaseMessaging.getInstance().subscribeToTopic(Config.FIREBASE_TOPIC);

        // Get Application Context
        context = getApplicationContext();

        // Block Application if no Internet Connection & Internet-only Mode Activated
        if (Config.INTERNET_ONLY) {
            Common.blockAppIfNoIntenet(this);
        }

        // Print Key Hash of Application
        printKeyHash();
    }

    private void setLanguage() {
        String languageToLoad  = "ar"; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

    }

    // Get Key Hash of Application
    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Logger.d(TAG + ": Debug KeyHash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // OnClick HowTo open IntroActivity
    @OnClick(R.id.btnHowTo)
    void openHelpActivity() {
        Intent howTo = new Intent(SplashActivity.this, IntroActivity.class);
        startActivity(howTo);
    }

    // OnClick Lets Start open HomeActivity
    @OnClick(R.id.btnLetsStart)
    void openHomeActivity() {
        Intent letsStart = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(letsStart);
    }

    // Calligraphy Library- Inject into Context
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
