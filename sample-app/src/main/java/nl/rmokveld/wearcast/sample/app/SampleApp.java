package nl.rmokveld.wearcast.sample.app;

import android.app.Application;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;

import nl.rmokveld.wearcast.Debug;
import nl.rmokveld.wearcast.ccl.CastCompanionExtension;
import nl.rmokveld.wearcast.phone.WearCastNotificationManager;

public class SampleApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VideoCastManager.initialize(this, "77FF5269", null, null);
        WearCastNotificationManager.init(VideoCastManager.getInstance().getMediaRouteSelector(), new CastCompanionExtension());
        Debug.setLoggingEnabled(true);
    }
}
