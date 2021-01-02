package mirror42.dev.cinemates.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import mirror42.dev.cinemates.tmdbAPI.model.Movie;

public class FirebaseEventsLogger {
    private static FirebaseEventsLogger singletonInstance;
    private boolean haveUserConsensus; // user consensus for log collection





    //--------------------------------------------------------- CONSTRUCTORS

    private FirebaseEventsLogger() {
        //
    }




    //--------------------------------------------------------- GETTERS/SETTERS

    public void setUserConsensus(boolean consensus) {
        this.haveUserConsensus = consensus;
    }

    public static FirebaseEventsLogger getInstance() {
        if (singletonInstance == null)
            singletonInstance = new FirebaseEventsLogger();
        return singletonInstance;
    }




    //--------------------------------------------------------- METHODS

    public void logScreenEvent(Object screenClass, String screenName, Context context) {
        if(haveUserConsensus) {
            @SuppressLint("MissingPermission")
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass.getClass().getSimpleName());
            params.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params);
        }
    }

    public void logSearchTerm(String term, Object screenClass, Context context) {
        if(haveUserConsensus) {
            // send to firebase analytics
            @SuppressLint("MissingPermission")
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            //throw new RuntimeException("Test Crash"); // Force a crash for Crashlytics
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.SEARCH_TERM, term);
            params.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass.getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, params);
        }
    }

    public void logSelectedMovie(Movie movie, String itemCategory, Object screenClass, Context context) {
        if(haveUserConsensus) {
            // send to firebase analytics
            @SuppressLint("MissingPermission")
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, itemCategory);
            params.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(movie.getTmdbID()));
            params.putString(FirebaseAnalytics.Param.ITEM_NAME, movie.getTitle());
            params.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass.getClass().getSimpleName());
//        Bundle params = new Bundle();
//        params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, new Bundle[]{item1});
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params);
        }
    }

    public void logLoginEvent(String method, Context context) {
        @SuppressLint("MissingPermission")
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.METHOD, method);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }



}// end FirebaseAnalytics class
