package me.arifix.quizix.Fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.arifix.quizix.Model.Leader;
import me.arifix.quizix.SplashActivity;
import me.arifix.quizix.Utils.Config;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.wang.avi.AVLoadingIndicatorView;

import me.arifix.quizix.Adapter.LeaderAdapter;
import me.arifix.quizix.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class LeadersFragment extends Fragment {
    private static final String TAG = "LeadersFragment";

    private List<Leader> leaders;
    private Unbinder unbinder;

    @BindView(R.id.listLeader)
    RecyclerView listLeader;
    @BindView(R.id.loader)
    AVLoadingIndicatorView loader;
    @BindView(R.id.adsBanner)
    AdView adsBanner;

    public LeadersFragment() {
        // Required empty public constructor
    }

    // To get Instance of that Fragment
    public static LeadersFragment getInstance() {
        return new LeadersFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaders, container, false);

        // Initialize Logger Library
        Logger.addLogAdapter(new AndroidLogAdapter());

        // Initialize Butterknife Library
        ButterKnife.bind(this, view);
        unbinder = ButterKnife.bind(this, view);

        // Initialize Firebase Firestore
        FirebaseFirestore fsdb = FirebaseFirestore.getInstance();

        // Get Score Data from Firebase Firescore Realtime Database
        leaders = new ArrayList<>();
        CollectionReference leaderRef = fsdb.collection(Config.FIRESTORE_COLLECTION_NAME);
        leaderRef.orderBy(Config.FIRESTORE_SCORE_COLUMN, Query.Direction.DESCENDING).limit(Config.SHOW_LEADERS_COUNT).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Leader leader = document.toObject(Leader.class);
                                leader.setEmail(document.getId());
                                leaders.add(leader);
                            }
                            showLeaderboard();
                        } else {
                            Logger.d(TAG + ": Error getting documents: " + task.getException());
                        }
                    }
                });

        // Inflate the layout for this fragment
        return view;
    }

    // Show Leaderboard
    private void showLeaderboard() {
        if (listLeader != null) {
            // Sort Scores Data
            Collections.sort(leaders, new Comparator<Leader>() {
                public int compare(Leader obj1, Leader obj2) {
                    return Integer.valueOf(obj2.score).compareTo(Integer.valueOf(obj1.score));
                }
            });

            // Display Leaders Data
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(SplashActivity.context);
            listLeader.setLayoutManager(layoutManager);
            listLeader.setHasFixedSize(true);
            LeaderAdapter adapter = new LeaderAdapter(SplashActivity.context, leaders);
            listLeader.setAdapter(adapter);
            if (loader.isShown()) {
                loader.hide();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
