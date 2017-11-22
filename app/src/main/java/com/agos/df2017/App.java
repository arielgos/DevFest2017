package com.agos.df2017;

import android.app.Application;

/**
 * Created by arielortuno on 11/21/17.
 */

public class App extends Application {

    public final static String tag = "FireThings";

    private static Application instance = null;

    public static Application getInstance() {
        if (instance == null) {
            instance = new Application();
        }
        return instance;
    }

}
