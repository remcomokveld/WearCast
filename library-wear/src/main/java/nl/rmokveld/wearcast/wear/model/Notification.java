package nl.rmokveld.wearcast.wear.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.android.gms.wearable.DataMapItem;

import nl.rmokveld.wearcast.shared.C;

public class Notification implements Parcelable {

    private final String mOriginNode;
    private final int mId;
    private final String mContentTitle;
    private final String mContentText;
    private final String mMediaInfoJson;

    public Notification(DataMapItem dataMapItem) {
        mOriginNode = dataMapItem.getUri().getHost();
        mId = Integer.parseInt(dataMapItem.getUri().getLastPathSegment());
        mContentTitle = dataMapItem.getDataMap().getString(C.ARG_CONTENT_TITLE);
        mContentText = dataMapItem.getDataMap().getString(C.ARG_CONTENT_TEXT);
        mMediaInfoJson = dataMapItem.getDataMap().getString(C.ARG_MEDIA_INFO);
    }

    public String getOriginNode() {
        return mOriginNode;
    }

    public int getId() {
        return mId;
    }

    @Nullable
    public String getContentTitle() {
        return mContentTitle;
    }

    @Nullable
    public String getContentText() {
        return mContentText;
    }

    public String getMediaInfoJson() {
        return mMediaInfoJson;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mOriginNode);
        dest.writeInt(this.mId);
        dest.writeString(this.mContentTitle);
        dest.writeString(this.mContentText);
        dest.writeString(this.mMediaInfoJson);
    }

    protected Notification(Parcel in) {
        this.mOriginNode = in.readString();
        this.mId = in.readInt();
        this.mContentTitle = in.readString();
        this.mContentText = in.readString();
        this.mMediaInfoJson = in.readString();
    }

    public static final Parcelable.Creator<Notification> CREATOR = new Parcelable.Creator<Notification>() {
        public Notification createFromParcel(Parcel source) {
            return new Notification(source);
        }

        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };
}
