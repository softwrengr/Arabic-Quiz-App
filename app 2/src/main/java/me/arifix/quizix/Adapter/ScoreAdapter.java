package me.arifix.quizix.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import me.arifix.quizix.Utils.Config;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;

import me.arifix.quizix.Model.Score;
import me.arifix.quizix.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Arif Khan on 1/4/2018.
 */

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {
    private Context context;
    private List<Score> score;
    private ShareDialog shareDialog;

    public ScoreAdapter(Context context, List<Score> score, Activity activity) {
        FacebookSdk.sdkInitialize(context);
        this.context = context;
        this.score = score;
        shareDialog = new ShareDialog(activity);
    }

    @Override
    public ScoreAdapter.ScoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        // Conditional Layout assign for Small & Regular Screen
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        if(screenHeight <= 800 & screenWidth <= 480){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_score_small, parent, false);
        }
        else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_score, parent, false);
        }
        return new ScoreAdapter.ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ScoreAdapter.ScoreViewHolder holder, final int position) {
        // Set Category Image
        if (score.get(position).getThumbnail() == null) {
            holder.ivCategoryImage.setImageDrawable(context.getResources().getDrawable(R.drawable.placeholder));
        } else {
            Picasso.with(context)
                    .load(Config.BASE_URL + "uploads/category/" + score.get(position).getThumbnail())
                    .fit()
                    .error(R.drawable.placeholder)
                    .centerCrop()
                    .into(holder.ivCategoryImage);
        }

        // Set Other Data
        holder.tvCategoryTitle.setText(score.get(position).getCategoryName());
        holder.tvCategoryScore.setText(String.valueOf(String.format(holder.layoutListScore.getContext().getString(R.string.best_score), score.get(position).getScore())));

        // OnClick Share Button open Share Intent
        holder.btnShareScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shareBody = view.getContext().getString(R.string.app_name) + ", " + score.get(position).getCategoryName() + "- " + String.format(view.getContext().getString(R.string.best_score), score.get(position).getScore());
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, view.getContext().getString(R.string.app_name));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                view.getContext().startActivity(Intent.createChooser(sharingIntent, view.getContext().getString(R.string.share_score)));
            }
        });

        // OnClick Share on Facebook Button open Facebook Share Dialog
        holder.btnShareScoreFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quote = view.getContext().getString(R.string.app_name) + ", " + score.get(position).getCategoryName() + "- " + String.format(view.getContext().getString(R.string.best_score), score.get(position).getScore());
                String packageName = "https://play.google.com/store/apps/details?id=" + context.getPackageName();

                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setQuote(quote)
                        .setContentUrl(Uri.parse(packageName))
                        .build();

                if(ShareDialog.canShow(ShareLinkContent.class)){
                    shareDialog.show(linkContent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return score.size();
    }

    public class ScoreViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.layoutListScore)
        ConstraintLayout layoutListScore;
        @BindView(R.id.ivCategoryImage)
        ImageView ivCategoryImage;
        @BindView(R.id.tvCategoryTitle)
        TextView tvCategoryTitle;
        @BindView(R.id.tvCategoryScore)
        TextView tvCategoryScore;
        @BindView(R.id.btnShareScore)
        Button btnShareScore;
        @BindView(R.id.btnShareScoreFB)
        Button btnShareScoreFB;

        public ScoreViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
