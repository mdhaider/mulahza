package edu.ehapplab.sidraenterprises;

import android.app.Application;
import android.content.Context;

import com.google.firebase.FirebaseApp;

/**
 * Created by nehal on 12/24/2017.
 */

public class MyApp extends Application {

    private static MyApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        try {
            FirebaseApp.initializeApp(this);
        }
        catch (Exception e) {
        }
    }

    public static Context getInstance() {
        return mInstance;
    }
}