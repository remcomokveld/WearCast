package nl.rmokveld.wearcast.sample.app;

import android.app.Notification;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;

import java.util.concurrent.atomic.AtomicInteger;

import nl.rmokveld.wearcast.phone.WearCastExtender;
import nl.rmokveld.wearcast.phone.WearCastNotificationManager;

public class MainActivity extends AppCompatActivity {

    private AtomicInteger mInteger = new AtomicInteger(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button showNotificationButton = (Button) findViewById(R.id.show_notification);
        showNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaInfo mediaInfo = new MediaInfo.Builder("http://www.quirksmode.org/html5/videos/big_buck_bunny.mp4")
                        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                        .setContentType("video/mp4")
                        .setMetadata(new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)).build();
                Notification build = new NotificationCompat.Builder(getApplicationContext())
                        .setContentTitle("TestNotification")
                        .setContentText("Test")
                        .setSmallIcon(R.drawable.ic_stat_action_notification)
                        .extend(new android.support.v4.app.NotificationCompat.WearableExtender())
                        .extend(new WearCastExtender(mediaInfo)).build();
                WearCastNotificationManager.getInstance(getApplicationContext()).notify(mInteger.getAndIncrement(), build);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cast_player_menu, menu);
        VideoCastManager.getInstance().addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }
}
