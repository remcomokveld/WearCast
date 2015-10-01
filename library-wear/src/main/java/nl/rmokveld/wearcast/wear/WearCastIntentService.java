package nl.rmokveld.wearcast.wear;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import nl.rmokveld.wearcast.Debug;
import nl.rmokveld.wearcast.shared.C;
import nl.rmokveld.wearcast.shared.WearMessageHelper;
import nl.rmokveld.wearcast.wear.model.Notification;
import nl.rmokveld.wearcast.wear.model.Route;

public class WearCastIntentService extends IntentService {

    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_UPDATE_NOTIFICATION = "show_notifications";
    private static AtomicInteger sRequestCodeGenerator = new AtomicInteger();
    private GoogleApiClient mClient;

    public static Intent createDeleteIntent(Context context, Notification notification) {
        return new Intent(context, WearCastIntentService.class)
                .setAction(ACTION_DELETE)
                .putExtra("notification", notification);
    }

    public static void updateNotifications(Context context) {
        context.startService(new Intent(context, WearCastIntentService.class)
                .setAction(ACTION_UPDATE_NOTIFICATION));
    }

    public WearCastIntentService() {
        super("WearCastIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mClient = new GoogleApiClient.Builder(this).addApiIfAvailable(Wearable.API).build();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ACTION_DELETE.equals(intent.getAction())) {
            Notification notification = intent.getParcelableExtra("notification");
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(notification.getId());
            WearMessageHelper.sendMessage(getApplicationContext(), notification.getOriginNode(), C.DELETE_NOTIFICATION_PATH+"/"+notification.getId(), null);
        } else if (ACTION_UPDATE_NOTIFICATION.equals(intent.getAction())) {
            showNotifications();
        }
    }

    private void showNotifications() {
        GoogleApiClient client = new GoogleApiClient.Builder(this).addApiIfAvailable(Wearable.API).build();
        ConnectionResult connectionResult = client.blockingConnect();
        ArrayList<Notification> notifications = new ArrayList<>();
        ArrayList<Route> routes = new ArrayList<>();
        if (connectionResult.isSuccess()) {
            DataItemBuffer dataItemBuffer = Wearable.DataApi.getDataItems(client).await();
            for (DataItem dataItem : dataItemBuffer) {
                String path = dataItem.getUri().getPath();
                if (path.startsWith(C.DEVICE_PATH)) {
                    routes.add(new Route(DataMapItem.fromDataItem(dataItem)));
                } else if (path.startsWith(C.NOTIFICATION_PATH)) {
                    notifications.add(new Notification(DataMapItem.fromDataItem(dataItem)));
                }
            }
            dataItemBuffer.release();

            for (Notification notification : notifications) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                        .setContentTitle(notification.getContentTitle())
                        .setContentText(notification.getContentText())
                        .setSmallIcon(R.drawable.ic_notification)
                        .setDeleteIntent(PendingIntent.getService(this, sRequestCodeGenerator.getAndIncrement(),
                                WearCastIntentService.createDeleteIntent(this, notification),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        .extend(new NotificationCompat.WearableExtender()
                                .setDisplayIntent(PendingIntent.getActivity(this, sRequestCodeGenerator.getAndIncrement(),
                                        WearCastNotificationActivity.launchIntent(this, notification),
                                        PendingIntent.FLAG_UPDATE_CURRENT)));
                Debug.logd("Add %d routes to notification");
                for (Route route : routes) {
                    builder.addAction(
                            R.drawable.ic_cast, getString(R.string.wear_cast_cast_to, route.getName()),
                            PendingIntent.getActivity(this, sRequestCodeGenerator.getAndIncrement(),
                                    WearCastStatusActivity.createStartCastIntent(this, notification, route),
                                    PendingIntent.FLAG_UPDATE_CURRENT));
                }
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(notification.getId(), builder.build());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mClient.isConnected() || mClient.isConnecting()) mClient.disconnect();
    }
}
