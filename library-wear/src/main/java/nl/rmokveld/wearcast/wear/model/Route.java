package nl.rmokveld.wearcast.wear.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.wearable.DataMapItem;

import nl.rmokveld.wearcast.shared.C;

public class Route implements Parcelable {
    private final String mNode;
    private final String mId;
    private final String mName;

    public Route(DataMapItem dataMapItem) {
        mNode = dataMapItem.getUri().getHost();
        mId = dataMapItem.getDataMap().getString(C.DEVICE_ID);
        mName = dataMapItem.getDataMap().getString(C.DEVICE_NAME);
    }

    public String getNode() {
        return mNode;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mNode);
        dest.writeString(this.mId);
        dest.writeString(this.mName);
    }

    protected Route(Parcel in) {
        this.mNode = in.readString();
        this.mId = in.readString();
        this.mName = in.readString();
    }

    public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {
        public Route createFromParcel(Parcel source) {
            return new Route(source);
        }

        public Route[] newArray(int size) {
            return new Route[size];
        }
    };
}
