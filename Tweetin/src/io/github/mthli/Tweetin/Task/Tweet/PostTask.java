package io.github.mthli.Tweetin.Task.Tweet;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import io.github.mthli.Tweetin.Activity.PostActivity;
import io.github.mthli.Tweetin.Flag.FlagUnit;
import io.github.mthli.Tweetin.Notification.NotificationUnit;
import io.github.mthli.Tweetin.R;
import io.github.mthli.Tweetin.Twitter.TwitterUnit;
import twitter4j.GeoLocation;
import twitter4j.StatusUpdate;

import java.io.File;

public class PostTask extends AsyncTask<Void, Void, Boolean> {
    private PostActivity postActivity;

    private long inReplyToStatusId;
    private String inReplyToScreenName;
    private String picturePath;
    private String text;
    private boolean checkIn;

    private StatusUpdate statusUpdate;

    public PostTask(PostActivity postActivity) {
        this.postActivity = postActivity;

        this.inReplyToStatusId = -1;
        this.inReplyToScreenName = null;
        this.picturePath = null;
        this.text = "";
        this.checkIn = false;

        this.statusUpdate = null;
    }

    private GeoLocation getGeoLocation() {
        LocationManager locationManager = (LocationManager) postActivity.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000,
                0,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {}

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {}

                    @Override
                    public void onProviderEnabled(String s) {}

                    @Override
                    public void onProviderDisabled(String s) {}
                }
        );

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            return new GeoLocation(location.getLatitude(), location.getLongitude());
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        inReplyToStatusId = postActivity.getInReplyToStatusId();
        inReplyToScreenName = postActivity.getInReplyToScreenName();
        picturePath = postActivity.getPicturePath();
        text = postActivity.getText();
        checkIn = postActivity.isCheckIn();

        statusUpdate = new StatusUpdate(text);

        if (inReplyToStatusId != -1 && inReplyToScreenName != null && text.contains(inReplyToScreenName)) {
            statusUpdate.setInReplyToStatusId(inReplyToStatusId);
        }

        if (checkIn && getGeoLocation() != null) {
            statusUpdate.setLocation(getGeoLocation());
        }

        if (picturePath != null) {
            statusUpdate.setMedia(new File(picturePath));
        }

        NotificationUnit.show(postActivity, R.drawable.ic_notification_send, R.string.notification_post_ing, text);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            TwitterUnit.getTwitterFromSharedPreferences(postActivity).updateStatus(statusUpdate);

            NotificationUnit.show(postActivity, R.drawable.ic_notification_send, R.string.notification_post_successful, text);
            NotificationUnit.cancel(postActivity);
        } catch (Exception e) {
            NotificationUnit.show(postActivity, R.drawable.ic_notification_send, R.string.notification_post_failed, text, getPendingIntent());
            return false;
        }

        if (isCancelled()) {
            return false;
        }
        return true;
    }

    @Override
    protected void onCancelled() {}

    @Override
    protected void onPostExecute(Boolean result) {}

    private PendingIntent getPendingIntent() {
        Intent intent = postActivity.getIntent();
        intent.putExtra(postActivity.getString(R.string.post_intent_post_flag), FlagUnit.POST_RESEND);
        intent.putExtra(postActivity.getString(R.string.post_intent_text), text);
        intent.putExtra(postActivity.getString(R.string.post_intent_check_in), checkIn);
        intent.putExtra(postActivity.getString(R.string.post_intent_picture_path), picturePath);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(postActivity);
        taskStackBuilder.addParentStack(PostActivity.class);
        taskStackBuilder.addNextIntent(intent);

        return taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);
    }
}
