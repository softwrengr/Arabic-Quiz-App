package me.arifix.quizix.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;

import me.arifix.quizix.R;

public class Common {

    // Block App if Internet isn't Available
    public static void blockAppIfNoIntenet(Activity activity) {
        if (!Utils.isNetworkAvailable(activity)) {
            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.app_name))
                    .setMessage(activity.getString(R.string.internet_notice))
                    .setCancelable(false)
                    .show();
        }
    }

    // Display App Share Dialog
    public static void showAppShareDialog(final Activity activity, final ShareDialog shareDialog) {
        new MaterialStyledDialog.Builder(activity)
                .setTitle(activity.getString(R.string.app_name))
                .setDescription(activity.getString(R.string.share_heading))
                .setIcon(R.drawable.icon_app)
                .setScrollable(true, 5)
                .setPositiveText(activity.getString(R.string.share_on_facebook))
                .setNegativeText(activity.getString(R.string.share_on_other))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String quote = activity.getString(R.string.app_name);
                        String packageName = "https://play.google.com/store/apps/details?id=" + activity.getApplicationContext().getPackageName();

                        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                .setQuote(quote)
                                .setContentUrl(Uri.parse(packageName))
                                .build();

                        if (ShareDialog.canShow(ShareLinkContent.class)) {
                            shareDialog.show(linkContent);
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String shareBody = String.format(activity.getString(R.string.share_text), activity.getString(R.string.app_name), activity.getApplicationContext().getPackageName());
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, activity.getString(R.string.app_name));
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        activity.startActivity(Intent.createChooser(sharingIntent, activity.getString(R.string.share_on_other)));
                    }
                })
                .show();
    }
}
