package nl.rmokveld.wearcast.phone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.media.MediaRouteSelector;

import nl.rmokveld.wearcast.Debug;

public class WearCastNotificationManager {

    private static WearCastNotificationManager sInstance;
    private static MediaRouteSelector sMediaRouteSelector;
    private static Extension sExtension;

    private final Context mContext;
    private final NotificationManagerCompat mNotificationManager;

    public static WearCastNotificationManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (WearCastNotificationManager.class) {
                if (sInstance == null) {
                    sInstance = new WearCastNotificationManager(context);
                }
            }
        }
        return sInstance;
    }


    private WearCastNotificationManager(Context context) {
        mContext = context.getApplicationContext();
        mNotificationManager = NotificationManagerCompat.from(context);
    }

    public static void init(MediaRouteSelector mediaRouteSelector, Extension extension) {
        sMediaRouteSelector = mediaRouteSelector;
        sExtension = extension;
    }

    static MediaRouteSelector getMediaRouteSelector() {
        return sMediaRouteSelector;
    }

    static Extension getExtension() {
        return sExtension;
    }

    public void notify(int id, Notification notification) {
        notify(null, id, notification);
    }

    public void notify(String tag, int id, Notification notification) {
        // force notification to local only because app is using other value
        notification.flags |= NotificationCompat.FLAG_LOCAL_ONLY;
        notification.deleteIntent = createDeletePendingIntent(mContext, WearCastIntentService.createDeleteNotificationIntent(mContext, id));
        mNotificationManager.notify(tag, id, notification);
        Debug.logd("Starting WearCastIntentService to put notification on DataApi");
        WearCastIntentService.sendNotification(mContext, id, notification);
    }

    public void cancel(int id) {
        cancel(null, id);
    }

    public void cancel(String tag, int id) {
        mNotificationManager.cancel(tag, id);
        mContext.startService(WearCastIntentService.createDeleteNotificationIntent(mContext, id));
    }

    @SuppressWarnings("unused")
    public void cancelAll() {
        mNotificationManager.cancelAll();
        mContext.startService(WearCastIntentService.createDeleteNotificationIntent(mContext));
    }

    private PendingIntent createDeletePendingIntent(Context context, Intent intent) {
        PendingIntent pendingIntent;
        int requestCode = 0;
        do {
            pendingIntent = PendingIntent.getService(context, ++requestCode, intent, PendingIntent.FLAG_NO_CREATE);
        } while (pendingIntent != null);
        return PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public interface Extension {
        AbstractStartCastHelper newStartCastHelper(Context context);
    }
}
