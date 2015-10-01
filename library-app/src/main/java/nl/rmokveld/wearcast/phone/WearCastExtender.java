package nl.rmokveld.wearcast.phone;

import android.app.Notification;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.cast.MediaInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import nl.rmokveld.wearcast.Debug;
import nl.rmokveld.wearcast.shared.C;


@SuppressWarnings("unused")
public class WearCastExtender implements NotificationCompat.Extender {
    private static final String TAG = "WearCastExtender";

    private String mContentTitle;
    private String mContentText;
    private MediaInfo mMediaInfo;

    public WearCastExtender(MediaInfo mediaInfo) {
        mMediaInfo = mediaInfo;
    }

    public WearCastExtender(Notification notification) {
        Bundle extras = NotificationCompat.getExtras(notification);
        Bundle wearCastExtras = extras != null ? extras.getBundle(C.ARG_WEARCAST_EXTENSIONS) : null;
        if (wearCastExtras != null) {
            mContentTitle = wearCastExtras.getString(C.ARG_CONTENT_TITLE);
            mContentText = wearCastExtras.getString(C.ARG_CONTENT_TEXT);
            String mediaInfoJson = wearCastExtras.getString(C.ARG_MEDIA_INFO);
            try {
                Constructor<MediaInfo> constructor = MediaInfo.class.getDeclaredConstructor(JSONObject.class);
                constructor.setAccessible(true);
                mMediaInfo = constructor.newInstance(new JSONObject(mediaInfoJson));
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | JSONException e) {
                Debug.loge("Failed to create MediaInfo from json", e);
            }
        }
    }

    public WearCastExtender setContentTitle(String contentTitle) {
        mContentTitle = contentTitle;
        return this;
    }

    public WearCastExtender setContentText(String contentText) {
        mContentText = contentText;
        return this;
    }

    public WearCastExtender setMediaInfo(MediaInfo mediaInfo) {
        mMediaInfo = mediaInfo;
        return this;
    }

    @Override
    public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
        Bundle wearCastBundle = new Bundle();
        if (mMediaInfo != null) {
            wearCastBundle.putString(C.ARG_MEDIA_INFO, mMediaInfo.toJson().toString());
        }
        if (mContentTitle != null) {
            wearCastBundle.putString(C.ARG_CONTENT_TITLE, mContentTitle);
        } else {
            //noinspection TryWithIdenticalCatches
            try {
                wearCastBundle.putString(C.ARG_CONTENT_TITLE, (String) NotificationCompat.Builder.class.getField("mContentTitle").get(builder));
            } catch (IllegalAccessException e) {
                Debug.loge("Failed to access field contentTitle in Builder", e);
            } catch (NoSuchFieldException e) {
                Debug.loge("Failed to access field contentTitle in Builder", e);
            }
        }
        if (mContentText != null) {
            wearCastBundle.putString(C.ARG_CONTENT_TEXT, mContentText);
        } else {
            //noinspection TryWithIdenticalCatches
            try {
                wearCastBundle.putString(C.ARG_CONTENT_TEXT, (String) NotificationCompat.Builder.class.getField("mContentText").get(builder));
            } catch (IllegalAccessException e) {
                Debug.loge("Failed to access field contentText in Builder", e);
            } catch (NoSuchFieldException e) {
                Debug.loge("Failed to access field contentText in Builder", e);
            }
        }
        builder.getExtras().putBundle(C.ARG_WEARCAST_EXTENSIONS, wearCastBundle);
        return builder;
    }
}
