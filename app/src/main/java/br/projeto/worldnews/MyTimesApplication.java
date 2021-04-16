package br.projeto.worldnews;

import android.app.Application;

/*
** Used for getting the application instance
**/
public class MyTimesApplication extends Application {
    private static MyTimesApplication myTimesApplicationInstance;

    public static MyTimesApplication getMyTimesApplicationInstance(){
        return myTimesApplicationInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myTimesApplicationInstance = this;
    }
}
