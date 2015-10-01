package nl.rmokveld.wearcast.shared;

public class C {
    public static final String ARG_WEARCAST_EXTENSIONS = "wear_cast_extras";
    public static final String ARG_MEDIA_INFO = "media_info";
    public static final String ARG_CONTENT_TITLE = "content_title";
    public static final String ARG_CONTENT_TEXT = "content_text";


    public static final String WEAR_CAST_BASE_PATH = "/wear-cast";
    public static final String DEVICE_PATH = C.WEAR_CAST_BASE_PATH + "/device";
    public static final String NOTIFICATION_PATH = WEAR_CAST_BASE_PATH + "/notification";
    public static final String DISCOVERY_PATH = WEAR_CAST_BASE_PATH + "/discovery";
    public static final String DELETE_NOTIFICATION_PATH = WEAR_CAST_BASE_PATH + "/delete-notification";
    public static final String START_CAST_PATH = WEAR_CAST_BASE_PATH + " /cast";
    public static final String STATE_PATH = WEAR_CAST_BASE_PATH + "/cast-state";
    public static final String TIMEOUT_PATH = C.STATE_PATH + "/timeout";

    public static final String DEVICE_ID = "device_id";
    public static final String DEVICE_NAME = "device_name";
    public static final String TAG = "WearCast";
    public static final String REQUEST_NODE_ID = "request_node_id";
    public static final String PING_PATH = WEAR_CAST_BASE_PATH + "/ping";
    public static final String NOTIFICATION_ID = "notification_id";
    public static final String TIMESTAMP = "timestamp";
}
