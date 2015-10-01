package nl.rmokveld.wearcast.ccl;

import android.content.Context;

import nl.rmokveld.wearcast.phone.AbstractStartCastHelper;
import nl.rmokveld.wearcast.phone.WearCastNotificationManager;

@SuppressWarnings("unused")
public class CastCompanionExtension implements WearCastNotificationManager.Extension {

    @Override
    public AbstractStartCastHelper newStartCastHelper(Context context) {
        return new CastCompanionStartCastHelper(context);
    }
}
