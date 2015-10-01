package nl.rmokveld.wearcast.phone;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.WorkerThread;
import android.support.v7.media.MediaRouter;
import android.text.TextUtils;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import nl.rmokveld.wearcast.Debug;
import nl.rmokveld.wearcast.State;
import nl.rmokveld.wearcast.shared.C;
import nl.rmokveld.wearcast.shared.WearMessageHelper;

public abstract class AbstractStartCastHelper {

    private final MediaRouter mMediaRouter;
    private final MediaRouter.Callback mCallback;
    private final Handler mHandler;
    private final Context mContext;

    private State mState;
    private String mRequestedDeviceId;
    private MediaInfo mMediaInfo;
    private Runnable mTimeoutRunnable;
    private int mTimeout = 10000;
    private String mRequestedBy;
    private WearCastDiscoveryHelper mCastDiscoveryHelper;

    public AbstractStartCastHelper(Context context) {
        mContext = context;
        mMediaRouter = MediaRouter.getInstance(context.getApplicationContext());
        mCallback = new MediaRouter.Callback() {
            @Override
            public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
                checkRoute(route);
            }
        };
        mHandler = new Handler();
    }

    public void setTimeout(int timeout) {
        mTimeout = timeout;
    }

    @WorkerThread
    public void startCastFromWear(String deviceId, String mediaInfoJson, String requestedBy, WearCastDiscoveryHelper castDiscoveryHelper) {
        mCastDiscoveryHelper = castDiscoveryHelper;
        Debug.logd("AbstractStartCastHelper.startCastFromWear() called with: " + "deviceId = [" + deviceId + "], mediaInfoJson = [" + mediaInfoJson + "], requestedBy = [" + requestedBy + "]");
        mRequestedDeviceId = deviceId;
        mRequestedBy = requestedBy;
        setState(State.SEARCHING);
        try {
            Constructor<MediaInfo> declaredConstructor = MediaInfo.class.getDeclaredConstructor(JSONObject.class);
            declaredConstructor.setAccessible(true);
            mMediaInfo = declaredConstructor.newInstance(new JSONObject(mediaInfoJson));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | JSONException | NoSuchMethodException e) {
            Debug.loge("Failed to construct MediaInfo from json", e);
        }
        for (MediaRouter.RouteInfo routeInfo : mMediaRouter.getRoutes()) {
            if (checkRoute(routeInfo)) return;
        }

        // Requested route was not found. Start active discovery
        mCastDiscoveryHelper.startDiscovery(mCallback);
        mTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                Debug.logw("Timeout reached in state: " + mState);
                WearMessageHelper.sendMessage(mContext, mRequestedBy, C.TIMEOUT_PATH, mState.toString().getBytes());
                mCastDiscoveryHelper.stopDiscovery(mCallback);
            }
        };
        mHandler.postDelayed(mTimeoutRunnable, mTimeout);
    }

    protected void setState(State state) {
        if (mState == state) return;
        mState = state;
        WearMessageHelper.sendMessage(mContext, mRequestedBy, C.STATE_PATH, mState.toString().getBytes());
    }

    private boolean checkRoute(MediaRouter.RouteInfo routeInfo) {
        CastDevice castDevice = CastDevice.getFromBundle(routeInfo.getExtras());
        if (castDevice == null) return false;
        if (TextUtils.equals(castDevice.getDeviceId(), mRequestedDeviceId)) {
            onRequestedRouteFound(routeInfo, castDevice);
            return true;
        }
        return false;
    }

    private void onRequestedRouteFound(MediaRouter.RouteInfo route, CastDevice castDevice) {
        Debug.logd("Requested route found");
        mHandler.removeCallbacks(mTimeoutRunnable);
        setState(State.RECEIVER_STARTING);
        if (route.isSelected()) {
            Debug.logd("Starting receiver app");
            startReceiverApp(route, castDevice);
        } else {
            selectRoute(route, castDevice);
        }

    }

    protected void selectRoute(MediaRouter.RouteInfo route, CastDevice castDevice) {
        Debug.logd("Selecting route: " + route.getName());
        route.select();
        Debug.logd("Starting receiver app");
        setState(State.RECEIVER_STARTING);
        startReceiverApp(route, castDevice);
    }

    protected abstract void startReceiverApp(MediaRouter.RouteInfo route, CastDevice castDevice);

    public final void receiverAppConnected() {
        Debug.logd("ReceiverApp connected");
        setState(State.LOADING_MEDIA);
        onReceiverAppConnected(mMediaInfo);
    }

    public abstract void onReceiverAppConnected(MediaInfo mediaInfo);

    public void release() {
        mHandler.removeCallbacks(mTimeoutRunnable);
        if (mCastDiscoveryHelper != null) mCastDiscoveryHelper.stopDiscovery(mCallback);
    }

}
