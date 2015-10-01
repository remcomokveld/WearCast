package nl.rmokveld.wearcast.ccl;

import android.content.Context;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;

import nl.rmokveld.wearcast.State;
import nl.rmokveld.wearcast.phone.AbstractStartCastHelper;

public class CastCompanionStartCastHelper extends AbstractStartCastHelper {
    private final VideoCastManager mCastManager;

    public CastCompanionStartCastHelper(Context context) {
        super(context);
        mCastManager = VideoCastManager.getInstance();
        mCastManager.addVideoCastConsumer(new VideoCastConsumerImpl() {
            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
                super.onApplicationConnected(appMetadata, sessionId, wasLaunched);
                receiverAppConnected();
            }

            @Override
            public void onRemoteMediaPlayerStatusUpdated() {
                super.onRemoteMediaPlayerStatusUpdated();
                switch (mCastManager.getMediaStatus().getPlayerState()) {
                    case MediaStatus.PLAYER_STATE_BUFFERING:
                        setState(State.LOADING_MEDIA);
                        break;
                    case MediaStatus.PLAYER_STATE_PLAYING:
                        setState(State.PLAYING);
                        break;
                }
            }
        });
    }

    @Override
    protected void startReceiverApp(MediaRouter.RouteInfo route, CastDevice castDevice) {
        if (mCastManager.isConnected()) {
            receiverAppConnected();
        } else {
            // if the receiver app does not start yet it can be started using CCL by resetting the
            // selected device
            mCastManager.onDeviceSelected(castDevice);
        }
    }

    @Override
    public void onReceiverAppConnected(MediaInfo mediaInfo) {
        try {
            mCastManager.loadMedia(mediaInfo, true, 0);
        } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
            e.printStackTrace();
        }
    }
}
