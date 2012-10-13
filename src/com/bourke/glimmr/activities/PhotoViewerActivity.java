package com.bourke.glimmr.activities;

import com.bourke.glimmr.fragments.viewer.CommentsFragment;
import android.content.Intent;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.view.Window;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.ViewPagerDisable;
import com.bourke.glimmr.fragments.viewer.PhotoViewerFragment;
import com.bourke.glimmr.R;

import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;

import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.PageIndicator;

import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.List;
import android.support.v4.app.FragmentTransaction;

/**
 * Activity for viewing photos.
 *
 * Receives a list of photos via an intent and shows the first one specified by
 * a startIndex in a zoomable ImageView.
 */
public class PhotoViewerActivity extends BaseActivity
        implements ViewPager.OnPageChangeListener,
                   PhotoViewerFragment.IPhotoViewerCallbacks {

    private static final String TAG = "Glimmr/PhotoViewerActivity";

    private List<Photo> mPhotos = new ArrayList<Photo>();

    private PhotoViewerPagerAdapter mAdapter;
    private ViewPagerDisable mPager;
    private List<WeakReference<Fragment>> mFragList =
        new ArrayList<WeakReference<Fragment>>();
    private CommentsFragment mCommentsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreate");

        /* Must be called before adding content */
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        super.onCreate(savedInstanceState);

        mActionBar.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.ab_bg_black));
        setContentView(R.layout.photoviewer_activity);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        handleIntent(getIntent());
    }

    /* Ignore the unchecked cast warning-
     * http://stackoverflow.com/a/262417/663370 */
    @SuppressWarnings("unchecked")
    private void handleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();

        mPhotos = (ArrayList<Photo>) bundle.getSerializable(Constants
                .KEY_PHOTOVIEWER_LIST);
        int startIndex = bundle.getInt(Constants.KEY_PHOTOVIEWER_START_INDEX);

        if (mPhotos != null) {
            if (Constants.DEBUG) {
                Log.d(getLogTag(), "Got list of photo urls, size: "
                    + mPhotos.size());
            }
            mAdapter =
                new PhotoViewerPagerAdapter(getSupportFragmentManager());
            mPager = (ViewPagerDisable) findViewById(R.id.pager);
            mPager.setAdapter(mAdapter);
            mPager.setOnPageChangeListener(mAdapter);
            /* Don't show the PageIndicator if there's a lot of items */
            if (mPhotos.size() <= Constants.LINE_PAGE_INDICATOR_LIMIT) {
                PageIndicator indicator =
                    (LinePageIndicator) findViewById(R.id.indicator);
                indicator.setOnPageChangeListener(this);
                indicator.setViewPager(mPager);
                indicator.setCurrentItem(startIndex);
            } else {
                mPager.setCurrentItem(startIndex);
            }
        } else {
            if (Constants.DEBUG) {
                Log.e(getLogTag(), "Photos from intent are null");
            }
            // TODO: show error / recovery
        }
    }

    @Override
    public User getUser() {
        return mUser;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onCommentsButtonClick(Photo photo) {
        boolean animateTransition = true;
        showCommentsFragment(photo, animateTransition);
    }

    private void showCommentsFragment(Photo photo, boolean animate) {
        if (Constants.DEBUG) Log.d(getLogTag(), "showCommentsFragment");
        if (photo != null) {
            mCommentsFragment = CommentsFragment.newInstance(photo);
            FragmentTransaction ft =
                getSupportFragmentManager().beginTransaction();
            if (animate) {
                ft.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
            }
            ft.replace(R.id.commentsFragment, mCommentsFragment);
            ft.addToBackStack(null);
            ft.commit();
        } else {
            Log.e(TAG, "showCommentsFragment: photo is null");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        /* Avoid IllegalStateException after rotate (commitAllowingStateLoss
         * doesn't help) - http://stackoverflow.com/a/10261438/663370 */
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        mFragList.add(new WeakReference(fragment));
    }

    @Override
    public void onVisibilityChanged() {
        for (WeakReference<Fragment> ref : mFragList) {
            PhotoViewerFragment f = (PhotoViewerFragment) ref.get();
            if (f != null) {
                f.refreshOverlayVisibility();
            }
        }
    }

    @Override
    public void onZoomed(boolean isZoomed) {
        mPager.setPagingEnabled(!isZoomed);
    }

    class PhotoViewerPagerAdapter extends FragmentStatePagerAdapter
            implements ViewPager.OnPageChangeListener {
        public PhotoViewerPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PhotoViewerFragment.newInstance(
                    mPhotos.get(position), PhotoViewerActivity.this);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            /* If comments fragment is showing update it for the current photo
             */
            if (mCommentsFragment != null && !mCommentsFragment.isHidden()) {
                getSupportFragmentManager().popBackStack();
                boolean animateTransition = false;
                showCommentsFragment(mPhotos.get(position), animateTransition);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public int getCount() {
            return mPhotos.size();
        }
    }
}
