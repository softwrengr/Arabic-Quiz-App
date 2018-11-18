package me.arifix.quizix.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.arifix.quizix.Adapter.ScoreAdapter;
import me.arifix.quizix.Database.AppDatabase;
import me.arifix.quizix.Model.Score;
import me.arifix.quizix.SplashActivity;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import me.arifix.quizix.Database.DatabaseCall;
import me.arifix.quizix.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ScoresFragment extends Fragment {
    private static final String TAG = "ScoresFragment";
    private Unbinder unbinder;

    @BindView(R.id.listScore)
    RecyclerView listScore;


    public ScoresFragment() {
        // Required empty public constructor
    }

    // To get Instance of that Fragment
    public static ScoresFragment getInstance() {
        return new ScoresFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scores, container, false);

        // Initialize Logger Library
        Logger.addLogAdapter(new AndroidLogAdapter());

        // Initialize Butterknife Library
        ButterKnife.bind(this, view);
        unbinder = ButterKnife.bind(this, view);

        // Get all Score from Database & Display
        List<Score> datas = DatabaseCall.getAllScore(AppDatabase.getAppDatabase(SplashActivity.context));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(SplashActivity.context);
        listScore.setLayoutManager(layoutManager);
        listScore.setHasFixedSize(true);
        ScoreAdapter adapter = new ScoreAdapter(SplashActivity.context, datas, getActivity());
        listScore.setAdapter(adapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
