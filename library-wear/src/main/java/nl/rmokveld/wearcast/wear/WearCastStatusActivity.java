package nl.rmokveld.wearcast.wear;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import nl.rmokveld.wearcast.State;
import nl.rmokveld.wearcast.shared.C;
import nl.rmokveld.wearcast.shared.WearMessageHelper;
import nl.rmokveld.wearcast.wear.model.Notification;
import nl.rmokveld.wearcast.wear.model.Route;

public class WearCastStatusActivity extends Activity implements GoogleApiClient.ConnectionCallbacks {

    private final MessageApi.MessageListener mMessageListener = new MessageApi.MessageListener() {
        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            String path = messageEvent.getPath();
            if (C.TIMEOUT_PATH.equals(path)) {
                mStatusTextView.setText(R.string.wear_cast_error);
            } else if (C.STATE_PATH.equals(path)) {
                if (mStatusTextView != null) {
                    State state = State.valueOf(new String(messageEvent.getData()));
                    if (state == State.PLAYING) {
                        mProgressBar.setVisibility(View.GONE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!isFinishing()) finish();
                            }
                        }, 500);
                    }
                    mStatusTextView.setText(getString(state.getStatusText(), mRoute.getName()));
                }
            }
        }
    };

    public static Intent createStartCastIntent(Context context, Notification notification, Route route) {
        return new Intent(context, WearCastStatusActivity.class)
                .putExtra("notification", notification)
                .putExtra("route", route);
    }

    private TextView mStatusTextView;
    private ProgressBar mProgressBar;

    GoogleApiClient mClient;

    private Notification mNotification;
    private Route mRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wearcast_status_activity);
        mStatusTextView = (TextView) findViewById(R.id.status);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        mNotification = getIntent().getParcelableExtra("notification");
        mRoute = getIntent().getParcelableExtra("route");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mClient.isConnected()) Wearable.MessageApi.removeListener(mClient, mMessageListener);
        mClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mClient, mMessageListener);
        Wearable.NodeApi.getLocalNode(mClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
                WearMessageHelper.startCast(getApplicationContext(), getLocalNodeResult.getNode().getId(), mRoute.getNode(), mRoute.getId(), mNotification.getMediaInfoJson());
            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
