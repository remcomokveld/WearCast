package nl.rmokveld.wearcast.shared;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.api.Batch;
import com.google.android.gms.common.api.BatchResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;


public class WearMessageHelper implements GoogleApiClient.ConnectionCallbacks {

    private final boolean mClientWasConnected;

    public static void requestDiscovery(Context context, boolean start) {
        sendMessage(context, C.DISCOVERY_PATH, new byte[]{(byte) (start?1:0)});
    }

    public static void startCast(Context context, String requestNodeId, String node, String id, String mediaInfoJson) {
        DataMap dataMap = new DataMap();
        dataMap.putString(C.DEVICE_ID, id);
        dataMap.putString(C.ARG_MEDIA_INFO, mediaInfoJson);
        dataMap.putString(C.REQUEST_NODE_ID, requestNodeId);
        sendMessage(context, node, C.START_CAST_PATH, dataMap.toByteArray());
    }

    public static void sendMessage(Context context, String message, byte[] data) {
        new WearMessageHelper(context, message, data).send();
    }

    public static void sendMessage(Context context, String nodeId, String message, byte[] data) {
        new WearMessageHelper(context, nodeId, message, data).send();
    }

    public static void sendMessage(GoogleApiClient client, String message, byte[] data) {
        new WearMessageHelper(client, message, data).send();
    }

    public static void sendMessage(GoogleApiClient client, String nodeId, String message, byte[] data) {
        new WearMessageHelper(client, nodeId, message, data).send();
    }

    private GoogleApiClient mClient;
    private String mNodeId;
    private String mMessage;

    private byte[] mData;

    public WearMessageHelper(Context context, String message, byte[] data) {
        this(context, null, message, data);
    }

    public WearMessageHelper(Context context, String nodeId, String message, byte[] data) {
        mMessage = message;
        mNodeId = nodeId;
        mData = data;
        mClient = new GoogleApiClient.Builder(context)
                .addApiIfAvailable(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        mClientWasConnected = false;
    }

    public WearMessageHelper(GoogleApiClient client, String message, byte[] data) {
        this(client, null, message, data);
    }

    public WearMessageHelper(GoogleApiClient client , String nodeId, String message, byte[] data) {
        mClient = client;
        mNodeId = nodeId;
        mMessage = message;
        mData = data;
        mClientWasConnected = client.isConnected();
    }



    private void send() {
        if (!mClient.isConnected())
            mClient.connect();
        else
            onConnected(null);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.NodeApi.getConnectedNodes(mClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult connectedNodesResult) {
                if (connectedNodesResult.getStatus().isSuccess()) {
                    if (mNodeId != null) {
                        Wearable.MessageApi.sendMessage(mClient, mNodeId, mMessage, mData).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                if(!mClientWasConnected)
                                    mClient.disconnect();
                            }
                        });
                    } else {
                        Batch.Builder batchBuilder = new Batch.Builder(mClient);
                        for (Node node : connectedNodesResult.getNodes()) {
                            batchBuilder.add(Wearable.MessageApi.sendMessage(mClient, node.getId(), mMessage, mData));
                        }
                        batchBuilder.build().setResultCallback(new ResultCallback<BatchResult>() {
                            @Override
                            public void onResult(BatchResult batchResult) {
                                if (!mClientWasConnected)
                                    mClient.disconnect();
                            }
                        });
                    }
                }
            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
