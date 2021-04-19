package br.projeto.worldnews.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import br.projeto.worldnews.MyTimesApplication;
import br.projeto.worldnews.R;
import br.projeto.worldnews.adapter.DBAdapter;
import br.projeto.worldnews.model.Article;
import br.projeto.worldnews.network.interceptors.OfflineResponseCacheInterceptor;
import br.projeto.worldnews.network.interceptors.ResponseCacheInterceptor;
import br.projeto.worldnews.view.MainActivity;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

import static android.content.Context.ALARM_SERVICE;
import static br.projeto.worldnews.network.GoogleXmlNews.readTitle;

public class BootReceiver extends BroadcastReceiver {

    private Context context;
    private List<Article> list = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Locale locale = context.getResources().getConfiguration().locale;
        String countryCode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            countryCode = context.getResources().getConfiguration().getLocales().get(0).getCountry();
        } else {
            countryCode = context.getResources().getConfiguration().locale.getCountry();
        }

        String url = "https://news.google.com/rss?hl=" + locale.getLanguage() + "&gl=" + countryCode;

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            Intent i = new Intent(context, BootReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(
                    context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR * 8,
                    AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
            Log.e("Alarm", "testando1");
        } else {
            Log.e("Alarm", "testando");

            Article a = new Article();
            a.setTitle("aaaaaaaaaaaaa");
            Article b = new Article();
            a.setTitle("bbbbbbbbbbbbb");
            Article c = new Article();
            a.setTitle("ccccccccccccccc");
            Article d = new Article();
            a.setTitle("dddddddddddddddd");
            Article e = new Article();
            a.setTitle("eeeeeeeeeeeeeee");
            list.add(a);
            list.add(b);
            list.add(c);
            list.add(d);
            list.add(e);
            notification(context);
            if (UtilityMethods.isNetworkAvailable())
                new myTask().execute(url);
        }
    }

    private void notification(Context context) {

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_round);
        String NOTIFICATION_CHANNEL_ID = "101016";
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel notificationChannel = null;
        NotificationManager notificationManager;

        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NewApp", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        } else {
            notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setAutoCancel(true)
                        .setSound(alarmSound)
                        .setSmallIcon(R.drawable.ic_launcher_round)
                        .setLargeIcon(bipmap)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setSubText(context.getString(R.string.versiculo_do_dia))
                        .setContentText(versDoDia.getAssunto() +
                                " - " + versDoDia.getBooksName() +
                                " " + versDoDia.getChapter() +
                                ":" + versDoDia.getVersesNum() + " ");

        mBuilder.setContentIntent(resultPendingIntent);
        notificationManager.notify(1518, mBuilder.build());
    }

    private class myTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String[] url) {

            okhttp3.Response response = null;
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addNetworkInterceptor(new ResponseCacheInterceptor());
            httpClient.addInterceptor(new OfflineResponseCacheInterceptor());
            httpClient.cache(new Cache(new File(MyTimesApplication.getMyTimesApplicationInstance().getCacheDir(), "ResponsesCache"), 10 * 1024 * 1024));
            httpClient.readTimeout(60, TimeUnit.SECONDS);
            httpClient.connectTimeout(60, TimeUnit.SECONDS);
            httpClient.addInterceptor(logging);

            Request request1 = new Request.Builder()
                    .url(url[0])
                    .build();

            OkHttpClient httpClient1 = httpClient.build();
            try {
                response = httpClient1.newCall(request1).execute();

            } catch (IOException ioException) {
                ioException.printStackTrace();
                response = null;
            }


            int count = 0;

            if (response != null)
                if (response.isSuccessful()) {
                    list = readXML(response);
                    DBAdapter dbAdapter = new DBAdapter(context);

                    for (int i = 0; i < 5; i++) {

                        if (dbAdapter.existTitleInTable(list.get(i).getTitle()) == 0) {
                            count++;
                        }
                    }
                    dbAdapter.close();
                }
            if (count == 5)
                return true;
            else
                return false;
        }

        @Override
        protected void onPostExecute(Boolean value) {
            super.onPostExecute(value);

            DBAdapter dbAdapter = new DBAdapter(context);

            if (value) {
                dbAdapter.deleteAllTitles();
                if (list.size() > 5)
                    for (int i = 0; i < 5; i++) {
                        dbAdapter.insertTitle(list.get(i).getTitle());
                    }
                dbAdapter.close();
                notification(context);
            }
        }


        private List<Article> readXML(okhttp3.Response response) {
            List<Article> list = new ArrayList<>();

            try {

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                // InputStream inputStream = getInputStream(stringUrl);
                // We will get the XML from an input stream
                xpp.setInput(response.body().byteStream(), "UTF_8");
                xpp.nextTag();

                boolean insideItem = false;

                Article news = null;
                while (xpp.next() != XmlPullParser.END_DOCUMENT) {

                    if (xpp.getEventType() == XmlPullParser.START_TAG) {

                        if (xpp.getName().equalsIgnoreCase("item")) {
                            insideItem = true;
                            news = new Article();

                        } else if (xpp.getName().equalsIgnoreCase("title")) {
                            if (insideItem)
                                news.setTitle(readTitle(xpp));
                        }

                    } else if (xpp.getEventType() == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {

                        list.add(news);
                        insideItem = false;

                    }
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (XmlPullParserException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            } catch (IllegalArgumentException e) {
                e.printStackTrace();

            }

            return list;

        }
    }
}
