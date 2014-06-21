package com.bourke.glimmrpro.tasks;

import com.bourke.glimmrpro.BuildConfig;
import android.os.AsyncTask;
import android.util.Log;
import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.FlickrHelper;
import com.bourke.glimmrpro.event.Events;

public class LoadGroupIdTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "Glimmr/LoadGroupIdTask";

    private final Events.IGroupIdReadyListener mListener;
    private final String mUrl;
    private Exception mException;

    public LoadGroupIdTask(Events.IGroupIdReadyListener listener, String url) {
        mListener = listener;
        mUrl = url;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            if (BuildConfig.DEBUG) Log.d(TAG, "Fetching id for " + mUrl);
            return FlickrHelper.getInstance().getUrlsInterface()
                    .lookupGroup(mUrl).getId();
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(final String result) {
        mListener.onGroupIdReady(result, mException);
    }
}