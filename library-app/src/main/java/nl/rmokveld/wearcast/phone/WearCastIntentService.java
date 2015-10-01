package nl.rmokveld.wearcast.phone;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.WorkerThread;
import android.support.v4.app.NotificationCompat;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.api.Batch;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.Locale;

import nl.rmokveld.wearcast.Debug;
import nl.rmokveld.wearcast.shared.C;

public class WearCastIntentService extends IntentService {

    private static final String ACTION_DELETE_NOTIFICATION = "delete_notification";
    private static final String ACTION_UPDATE_ROUTES = "update_routes";
    private static final String ACTION_SEND_NOTIFICATION = "send_notification";

    public static final String EXTRA_NOTIFICATION_ID = "notification_id";

    public static Intent createDeleteNotificationIntent(Context context) {
        return new Intent(context, WearCastIntentService.class)
                .setAction(ACTION_DELETE_NOTIFICATION);
    }

    public static Intent createDeleteNotificationIntent(Context context, int notificationId) {
        return new Intent(context, WearCastIntentService.class)
                .setAction(ACTION_DELETE_NOTIFICATION)
                .putExtra(EXTRA_NOTIFICATION_ID, notificationId);
    }

    public static void startUpdateRoutes(Context context, List<MediaRouter.RouteInfo> routes) {
        Bundle availableRoutes = new Bundle();
        for (MediaRouter.RouteInfo route : routes) {
            Debug.logd(String.format(Locale.getDefault(), "Checking if route '%s' is Chromecast", route.getName()));
            CastDevice castDevice = CastDevice.getFromBundle(route.getExtras());
            if (castDevice == null || castDevice.getDeviceId().startsWith("__cast_nearby"))
                continue;
            availableRoutes.putString(castDevice.getDeviceId(), castDevice.getFriendlyName());
        }
        context.startService(new Intent(context, WearCastIntentService.class)
                .setAction(ACTION_UPDATE_ROUTES)
                .putExtra("routes", availableRoutes));
    }

    public static void sendNotification(Context context, int id, Notification notification) {
        context.startService(new Intent(context, WearCastIntentService.class)
                .setAction(ACTION_SEND_NOTIFICATION)
                .putExtra("id", id)
                .putExtra("notification", notification));
    }

    public WearCastIntentService() {
        super("WearCastIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Debug.logd("WearCastIntentService started with action: " + intent.getAction());
        GoogleApiClient client = new GoogleApiClient.Builder(getApplicationContext()).addApiIfAvailable(Wearable.API).build();
        if (client.blockingConnect().isSuccess()) {
            if (ACTION_DELETE_NOTIFICATION.equals(intent.getAction())) {
                if (intent.hasExtra(EXTRA_NOTIFICATION_ID))
                    deleteNotification(client, intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0));
                else
                    deleteAllNotifications(client);
            } else if (ACTION_UPDATE_ROUTES.equals(intent.getAction())) {
                updateRoutes(client, intent.getBundleExtra("routes"));
            } else if (ACTION_SEND_NOTIFICATION.equals(intent.getAction())) {
                sendNotification(client, intent.getIntExtra("id", 0), (Notification) intent.getParcelableExtra("notification"));
            }
            client.disconnect();
        }
    }

    private void sendNotification(GoogleApiClient client, int id, Notification notification) {
        Bundle extras = NotificationCompat.getExtras(notification);
        Bundle wearCastExtras = extras.getBundle(C.ARG_WEARCAST_EXTENSIONS);
        if (wearCastExtras != null) {
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(C.NOTIFICATION_PATH + "/" + id);
            putDataMapRequest.getDataMap().putInt(C.NOTIFICATION_ID, id);
            putDataMapRequest.getDataMap().putString(C.ARG_CONTENT_TITLE, wearCastExtras.getString(C.ARG_CONTENT_TITLE));
            putDataMapRequest.getDataMap().putString(C.ARG_CONTENT_TEXT, wearCastExtras.getString(C.ARG_CONTENT_TEXT));
            putDataMapRequest.getDataMap().putString(C.ARG_MEDIA_INFO, wearCastExtras.getString(C.ARG_MEDIA_INFO));
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(client, putDataMapRequest.asPutDataRequest()).await();
            if (result.getStatus().isSuccess()) {
                Debug.logd("Notification added to DataApi: " + result.getDataItem().getUri());
            } else {
                Debug.logw("Failed to put notification on DataApi. Code:" + result.getStatus().getStatusCode() + ": " + result.getStatus().getStatusMessage());
            }
        } else {
            Debug.logw("wearCastExtras == null");
        }
    }

    private void updateRoutes(GoogleApiClient mClient, Bundle routes) {
        DataItemBuffer dataItemBuffer = Wearable.DataApi.getDataItems(mClient, Uri.parse("wear:" + C.DEVICE_PATH), DataApi.FILTER_PREFIX).await();
        Batch.Builder builder = new Batch.Builder(mClient);
        for (DataItem dataItem : dataItemBuffer) {
            if (!routes.containsKey(dataItem.getUri().getLastPathSegment())) {
                Debug.logd(String.format(Locale.getDefault(), "Deleting route %s from DataApi because route is no longer available", dataItem.getUri().getLastPathSegment()));
                builder.add(Wearable.DataApi.deleteDataItems(mClient, dataItem.getUri()));
            }
        }
        dataItemBuffer.release();

        for (String deviceId : routes.keySet()) {
            Debug.logd(String.format(Locale.getDefault(), "Adding route %s: %s to DataApi", deviceId, routes.getString(deviceId)));
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(C.DEVICE_PATH + "/" + deviceId);
            putDataMapRequest.getDataMap().putString(C.DEVICE_ID, deviceId);
            putDataMapRequest.getDataMap().putString(C.DEVICE_NAME, routes.getString(deviceId));
            builder.add(Wearable.DataApi.putDataItem(mClient, putDataMapRequest.asPutDataRequest()));
        }
        builder.build().await();
    }

    @WorkerThread
    private void deleteAllNotifications(GoogleApiClient client) {
        Wearable.DataApi.deleteDataItems(client, Uri.parse("wear://" + getLocalNodeId(client) + C.NOTIFICATION_PATH), DataApi.FILTER_PREFIX).await();
    }

    @WorkerThread
    private void deleteNotification(GoogleApiClient client, int notificationId) {
        Wearable.DataApi.deleteDataItems(client, Uri.parse("wear://" + getLocalNodeId(client) + C.NOTIFICATION_PATH + "/" + notificationId), DataApi.FILTER_PREFIX).await();
    }

    @WorkerThread
    private String getLocalNodeId(GoogleApiClient client) {
        return Wearable.NodeApi.getLocalNode(client).await().getNode().getId();
    }
}
