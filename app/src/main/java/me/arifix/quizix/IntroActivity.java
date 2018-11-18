package me.arifix.quizix;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.WindowManager;

import com.github.paolorotolo.appintro.AppIntro;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import me.arifix.quizix.Fragment.Slide1Fragment;
import me.arifix.quizix.Fragment.Slide2Fragment;
import me.arifix.quizix.Fragment.Slide3Fragment;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class IntroActivity extends AppIntro {
    private static final String TAG = "IntroActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add/Remove Flag from Layout Param to make Transparent StatusBar
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        // Initialize Logger Library
        Logger.addLogAdapter(new AndroidLogAdapter());

        // Initialize Calligraphy Library with default Font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // Create Fragment Instance from Slides Fragments
        Fragment slide1 = Slide1Fragment.getInstance();
        Fragment slide2 = Slide2Fragment.getInstance();
        Fragment slide3 = Slide3Fragment.getInstance();

        // Add Fragment as Slide
        addSlide(slide1);
        addSlide(slide2);
        addSlide(slide3);

        // Set Separator Color
        setSeparatorColor(Color.parseColor("#00ff0000"));

        // Show/Hide Skip Button
        showSkipButton(true);

        // Set/Unset Progress Button
        setProgressButtonEnabled(true);
    }

    // Open HomeActivity
    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        Intent home = new Intent(IntroActivity.this, HomeActivity.class);
        startActivity(home);
    }

    // Open SplashActivity
    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent splash = new Intent(IntroActivity.this, SplashActivity.class);
        startActivity(splash);
    }

    // OnSlide Changed Replace Slide Fragment
    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }

    // Calligraphy Library- Inject into Context
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
