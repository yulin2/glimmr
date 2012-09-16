package com.bourke.glimmr.activities;

import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.util.Log;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.fragments.viewer.CommentsFragment;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.people.User;

/**
 *
 */
public class CommentsDialogActivity extends BaseActivity {

    public static final String TAG = "Glimmr/CommentsDialogActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_activity);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public User getUser() {
        return mUser;
    }

    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            if (Constants.DEBUG)
                Log.e(TAG, "null bundle, CommentsDialogActivity requires " +
                        "a Photo");
            return;
        }

        Photo photo = (Photo) bundle.getSerializable(Constants
                .COMMENTS_DIALOG_ACTIVITY_PHOTO);
        if (photo != null) {
            if (Constants.DEBUG)
                Log.d(TAG, "Got photo to fetch comments for: " +
                        photo.getId());

            /* Create and add the fragment now we have the photo to fetch
             * comments for */
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
            CommentsFragment newFragment = CommentsFragment.newInstance(photo);
            fragmentTransaction.add(R.id.layout, newFragment);
            fragmentTransaction.commit();
        } else {
            if (Constants.DEBUG)
                Log.e(TAG, "photo from intent is null");
            // TODO: show error / recovery
        }
    }
}
