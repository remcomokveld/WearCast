package nl.rmokveld.wearcast.wear;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.widget.TextView;

import nl.rmokveld.wearcast.shared.WearMessageHelper;
import nl.rmokveld.wearcast.wear.model.Notification;

public class WearCastNotificationActivity extends Activity {

    public static Intent launchIntent(Context context, Notification notification) {
        return new Intent(context, WearCastNotificationActivity.class)
                .putExtra("notification", notification);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wearcast_notification_activity);

        Notification notification = getIntent().getParcelableExtra("notification");
        TextView contentTitle = (TextView) findViewById(R.id.content_title);
        contentTitle.setText(notification.getContentTitle());
        TextView contentText = (TextView) findViewById(R.id.content_text);
        contentText.setText(notification.getContentText());
    }

    @Override
    protected void onResume() {
        super.onResume();
        WearMessageHelper.requestDiscovery(this, true);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        WearMessageHelper.requestDiscovery(this, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        WearMessageHelper.requestDiscovery(this, false);
    }
}
