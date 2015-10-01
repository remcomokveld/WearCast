package nl.rmokveld.wearcast.phone;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import nl.rmokveld.wearcast.shared.C;

public class WearCastListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (C.DISCOVERY_PATH.equals(messageEvent.getPath())) {
            if (messageEvent.getData()[0] == 1)
                WearCastService.startDiscovery(this);
            else
                WearCastService.stopDiscovery(this);
        } else if (messageEvent.getPath().startsWith(C.START_CAST_PATH)) {
            DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());
            String requestNodeId = dataMap.getString(C.REQUEST_NODE_ID);
            String deviceId = dataMap.getString(C.DEVICE_ID);
            String mediaInfoJson = dataMap.getString(C.ARG_MEDIA_INFO);
            WearCastService.startCast(this, requestNodeId, deviceId, mediaInfoJson);
        } else if (messageEvent.getPath().startsWith(C.DELETE_NOTIFICATION_PATH)) {
            int notificationId = Integer.parseInt(messageEvent.getPath().substring(messageEvent.getPath().lastIndexOf('/') + 1));
            WearCastNotificationManager.getInstance(this).cancel(notificationId);
        }
    }
}
