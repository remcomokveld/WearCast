package nl.rmokveld.wearcast.phone;

import android.app.Service;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.support.v7.media.MediaRouter;

import java.util.List;

import nl.rmokveld.wearcast.Debug;
import nl.rmokveld.wearcast.shared.C;

class WearCastDiscoveryHelper {

    private final WifiManager.WifiLock mWifiLock;
    private final Context mContext;
    private final MediaRouter.Callback mCallback;
    private final MediaRouter mMediaRouter;
    private final List<MediaRouter.RouteInfo> mRoutes;
    private final Handler mHandler;
    private final Runnable mStopTask = new Runnable() {
        @Override
        public void run() {
            stopDiscovery(WearCastDiscoveryHelper.this.mCallback);
        }
    };
    private Service mService;

    public WearCastDiscoveryHelper(Service context) {
        mService = context;
        mContext = context.getApplicationContext();
        mMediaRouter = MediaRouter.getInstance(context.getApplicationContext());
        mRoutes = mMediaRouter.getRoutes();
        mHandler = new Handler();
        mCallback = new MediaRouter.Callback() {
            @Override
            public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
                super.onRouteAdded(router, route);
                Debug.logd("Route added: " + route.getName());
                WearCastIntentService.startUpdateRoutes(mContext, mRoutes);
            }

            @Override
            public void onRouteChanged(MediaRouter router, MediaRouter.RouteInfo route) {
                super.onRouteChanged(router, route);
                Debug.logd("Route changed: " + route.getName());
                WearCastIntentService.startUpdateRoutes(mContext, mRoutes);
            }

            @Override
            public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
                super.onRouteRemoved(router, route);
                Debug.logd("Route removed: " + route.getName());
                WearCastIntentService.startUpdateRoutes(mContext, mRoutes);
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            mWifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, C.TAG);
        } else {
            mWifiLock = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, C.TAG);
        }
    }

    public void startDiscovery(MediaRouter.Callback callback) {
        mService.startForeground(500, new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_cast_dark)
                .setContentTitle("Discovering")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build());
        mHandler.removeCallbacks(mStopTask);
        WearCastIntentService.startUpdateRoutes(mContext, mRoutes);
        if (!mWifiLock.isHeld()) mWifiLock.acquire();
        Debug.logd("Starting active discovery");
        mMediaRouter.addCallback(WearCastNotificationManager.getMediaRouteSelector(), callback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY | MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
        mHandler.postDelayed(mStopTask, 10000);
    }

    public void stopDiscovery(MediaRouter.Callback callback) {
        mService.stopForeground(true);
        mHandler.removeCallbacks(mStopTask);
        if (mWifiLock.isHeld()) mWifiLock.release();
        Debug.logd("stopping active discovery");
        mMediaRouter.removeCallback(callback);
    }

    public void release() {
        mService = null;
    }

    public void startDiscovery() {
        startDiscovery(mCallback);
    }

    public void stopDiscovery() {
        stopDiscovery(mCallback);
    }
}
