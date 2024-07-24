package org.unipi.mpsp2343.smartalert;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;

import androidx.core.app.NotificationCompat;

import org.unipi.mpsp2343.smartalert.dto.SavedAlert;

import java.util.List;

//This class provides some infrastructure to assist with showing notifications to the user.
public class NotificationHelper {
    private final String CHANNEL_ID = "SmartAlert"; //Notification channel ID
    private final long[] vibrationPattern = { 0, 126, 912, 800, 918, 830, 918 }; //Notification vibration pattern
    private final Uri alertSoundUri; //URI of the notification sound file
    private NotificationManager mManager; //Manager for the notification service
    private final Context context; //Context of the service that uses the helper

    public NotificationHelper(Context context) {
        this.context = context;
        alertSoundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + R.raw.alert_sound);

        List<NotificationChannel> channelList = getManager().getNotificationChannels();
        for (int i = 0; channelList != null && i < channelList.size(); i++) {
            getManager().deleteNotificationChannel(channelList.get(i).getId());
        }
        createChannel();
    }

    //Creates the notification channel
    private void createChannel() {
        String channelName = "SmartAlertDisasterAlerts";

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        //Set channel settings
        channel.setDescription("Channel for disaster alerts!");
        channel.enableVibration(true);
        channel.setVibrationPattern(vibrationPattern);
        channel.setSound(alertSoundUri, attributes);
        getManager().createNotificationChannel(channel);
    }

    //Retrieve notification manager if it has been instantiated or make a new one
    public NotificationManager getManager() {
        if (mManager == null)
            mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return mManager;
    }

    //Creates an alert notification
    public NotificationCompat.Builder getNotification(SavedAlert alertData) {
        //A summary to show as content in a collapsed notification
        String summary = context.getResources().getStringArray(R.array.event_alert_summaries)[alertData.getEventType()];
        //location of the event and timestamp of when the notification was received
        String locAndTime = context.getString(R.string.alert_loc_and_time, String.valueOf(alertData.getLocation().getLat()), String.valueOf(alertData.getLocation().getLon()), alertData.getTimestamp());
        //Content to show in the expanded notification
        String content = summary + locAndTime +  context.getResources().getStringArray(R.array.event_alert_guidelines)[alertData.getEventType()];
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getResources().getStringArray(R.array.event_alert_headers)[alertData.getEventType()])
                .setContentText(summary)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(fromHtml(content))); //Set style as big text in order be able to show long text
    }

    //Creates a notification to notify the user that the alert service has started running
    public NotificationCompat.Builder getInitNotification() {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Alert Service")
                .setContentText("The alert service is running.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }
}
