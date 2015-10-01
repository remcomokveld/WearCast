package nl.rmokveld.wearcast.wear;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Locale;

import nl.rmokveld.wearcast.Debug;
import nl.rmokveld.wearcast.shared.C;

public class WearCastListenerService extends WearableListenerService {
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Debug.logd(String.format(Locale.getDefault(), "WearCastListenerService.onDataChanged() called with %d events", dataEvents.getCount()));
        boolean dataChanged = false;
        for (DataEvent dataEvent : dataEvents) {
            dataChanged |= dataEvent.getDataItem().getUri().getPath().startsWith(C.WEAR_CAST_BASE_PATH);
        }
        if (dataChanged)
            WearCastIntentService.updateNotifications(this);
        dataEvents.release();
    }
}
