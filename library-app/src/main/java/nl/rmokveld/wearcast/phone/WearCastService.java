package nl.rmokveld.wearcast.phone;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import nl.rmokveld.wearcast.shared.C;

public class WearCastService extends Service {


    private static final String ACTION_DISCOVERY = "discovery";
    private static final String ACTION_START_CAST = "start_cast";

    public static void startDiscovery(Context context) {
        context.startService(new Intent(context, WearCastService.class).setAction(ACTION_DISCOVERY).putExtra("start", true));
    }

    public static void stopDiscovery(Context context) {
        context.startService(new Intent(context, WearCastService.class).setAction(ACTION_DISCOVERY).putExtra("start", false));
    }

    public static void startCast(Context context, String requestNode, String deviceId, String mediaJson) {
        context.startService(new Intent(context, WearCastService.class)
                .setAction(ACTION_START_CAST)
                .putExtra(C.REQUEST_NODE_ID, requestNode)
                .putExtra(C.ARG_MEDIA_INFO, mediaJson)
                .putExtra(C.DEVICE_ID, deviceId));
    }

    private WearCastDiscoveryHelper mCastDiscoveryHelper;
    private AbstractStartCastHelper mStartCastHelper;
    private Runnable mTimeout = new Runnable() {
        @Override
        public void run() {
            stopForeground(true);
            mCastDiscoveryHelper.stopDiscovery();
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        mCastDiscoveryHelper = new WearCastDiscoveryHelper(this);
        if (WearCastNotificationManager.getExtension() != null)
            mStartCastHelper = WearCastNotificationManager.getExtension().newStartCastHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ACTION_DISCOVERY.equals(intent.getAction())) {
                if (intent.getBooleanExtra("start", false)) {
                    mCastDiscoveryHelper.startDiscovery();
                }
                else {
                    mCastDiscoveryHelper.stopDiscovery();
                }
            } else if (ACTION_START_CAST.equals(intent.getAction())) {
                mStartCastHelper.startCastFromWear(
                        intent.getStringExtra(C.DEVICE_ID),
                        intent.getStringExtra(C.ARG_MEDIA_INFO),
                        intent.getStringExtra(C.REQUEST_NODE_ID), null);
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCastDiscoveryHelper.release();
        mStartCastHelper.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
