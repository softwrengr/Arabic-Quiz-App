package me.arifix.quizix.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.arifix.quizix.SplashActivity;
import me.arifix.quizix.R;

public class Slide2Fragment extends Fragment {

    public Slide2Fragment() {
        // Required empty public constructor
    }

    // To get Instance of that Fragment
    public static Slide2Fragment getInstance() {
        return new Slide2Fragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Conditional Layout assign for Small & Regular Screen
        int screenHeight = SplashActivity.context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = SplashActivity.context.getResources().getDisplayMetrics().widthPixels;
        if (screenHeight <= 800 & screenWidth <= 480) {
            return inflater.inflate(R.layout.fragment_slide2_small, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_slide2, container, false);
        }
    }

}
