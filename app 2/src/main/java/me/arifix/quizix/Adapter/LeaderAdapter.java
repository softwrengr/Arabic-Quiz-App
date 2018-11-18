package me.arifix.quizix.Adapter;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import me.arifix.quizix.Model.Leader;
import me.arifix.quizix.Utils.CircleTransform;
import com.squareup.picasso.Picasso;

import me.arifix.quizix.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Arif Khan on 5/24/2018.
 */

public class LeaderAdapter extends RecyclerView.Adapter<LeaderAdapter.LeaderViewHolder> {
    private List<Leader> leader;
    private Context context;

    public LeaderAdapter(Context context, List<Leader> leader) {
        this.context = context;
        this.leader = leader;
    }

    @Override
    public LeaderAdapter.LeaderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_leader, parent, false);
        return new LeaderAdapter.LeaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final LeaderAdapter.LeaderViewHolder holder, final int position) {
        // Set User Image
        if (leader.get(position).getPhoto().isEmpty()) {
            holder.ivLeaderImage.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_user));
        } else {
            Picasso.with(context)
                    .load(leader.get(position).getPhoto())
                    .error(R.drawable.icon_user)
                    .transform(new CircleTransform())
                    .fit()
                    .centerCrop()
                    .into(holder.ivLeaderImage);
        }

        // Set Other Data
        holder.tvLeaderSL.setText(String.format("#%d", position + 1));
        holder.tvLeaderName.setText(leader.get(position).getName());
        holder.tvLeaderScore.setText(leader.get(position).getScore());
    }

    @Override
    public int getItemCount() {
        return leader.size();
    }

    public class LeaderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.layoutListLeader)
        ConstraintLayout layoutListLeader;
        @BindView(R.id.ivLeaderImage)
        ImageView ivLeaderImage;
        @BindView(R.id.tvLeaderSL)
        TextView tvLeaderSL;
        @BindView(R.id.tvLeaderName)
        TextView tvLeaderName;
        @BindView(R.id.tvLeaderScore)
        TextView tvLeaderScore;

        public LeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
