package nl.rmokveld.wearcast;

import android.support.annotation.StringRes;

import nl.rmokveld.wearcast.shared.R;

public enum  State {
    SEARCHING {
        @Override
        public int getStatusText() {
            return R.string.wear_cast_searching;
        }
    }, RECEIVER_STARTING {
        @Override
        public int getStatusText() {
            return R.string.wear_cast_connecting;
        }
    }, LOADING_MEDIA {
        @Override
        public int getStatusText() {
            return R.string.wear_cast_loading;
        }
    }, PLAYING {
        @Override
        public int getStatusText() {
            return R.string.wear_cast_playing;
        }
    };

    @StringRes
    public abstract int getStatusText();
}
