package br.projeto.worldnews.view;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import br.projeto.worldnews.R;
import br.projeto.worldnews.adapter.DBAdapter;
import br.projeto.worldnews.model.Constants;
import br.projeto.worldnews.model.Topic;
import br.projeto.worldnews.network.GoogleXmlNews;
import br.projeto.worldnews.util.BootReceiver;
import br.projeto.worldnews.util.UtilityMethods;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String CHANNELNAME = "NewsApp";
    public static final String CHANNELDESCRIPTION = "NewsApp Notification";
    public static final String IDCHANNEL = "br.projeto.worldnews.ANDROID";
    private final static String[] MENSAGEM_ARRAY = {"Read the full article", "No results were found",
            "Finalizing adjustments to news topics", "Nothing to display today", "Do you want to exit ?",
            "The world News in your hands", "You have new News", "No", "Yes", "Search", "About the app",
            "An error has occurred", "Please turn on the Internet", "Share with", "Check out this article from the NewsApp app"};
    private final static String LOCALE_DEFAULT = "en";
    private String[] TOPIC_ARRAY = {"Google News", "Country", "World", "Businesses", "Finance", "Economy",
            "BitCoin", "Culture", "Gastronomy", "Travels", "Politics", "Science", "Health", "Sports", "Hacker",
            "Technology", "Videogame", "Entertainment", "Films", "Youtube", "Twitch", "Netflix", "Europe", "Asia", "Africa",
            "South America", "North America", "Middle East", "Rate us!", "Contact us", "About the app"};
    public static String url = "https://news.google.com/news?cf=all&hl=language&pz=1&ned=country&q=topic&sort=date&output=rss";
    private String SOURCE;
    private GoogleXmlNews googleXmlNews;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Drawer result;
    private AccountHeader accountHeader;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private Parcelable listState;
    private Typeface montserrat_regular;
    private TextView mTitle;
    private Locale locale;
    private List<Topic> topicList;
    private String countryCode;
    private ConstraintLayout layoutLoading;
    private FirebaseAnalytics mFirebaseAnalytics;

    private static String translate(String langFrom, String langTo, String text) {

        final String yourURL = "https://script.google.com/macros/s/AKfycbyHrs_kLCmXJB-fH_mS2ODtud3y0lR4Povq9nE2EqCSBPSiqjF80PNMKJohEV3TrZws/exec";

        try {

            String urlStr = yourURL + "?q=" + URLEncoder.encode(text, "UTF-8") + "&target=" + langTo + "&source="
                    + langFrom;

            URL url = new URL(urlStr);
            StringBuilder response = new StringBuilder();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setRequestProperty("Accept-Charset", "UTF-8");
            con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            con.setRequestProperty("encoding", "UTF-8");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();

        } catch (UnsupportedEncodingException | MalformedURLException e) {
            e.printStackTrace();
            return "";
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return "";
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        layoutLoading = findViewById(R.id.layoutLoading);

        locale = getResources().getConfiguration().locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            countryCode = getResources().getConfiguration().getLocales().get(0).getCountry();
        } else {
            countryCode = getResources().getConfiguration().locale.getCountry();
        }
        url = url.replace("language", locale.getLanguage()).
                replace("country", countryCode);

        AssetManager assetManager = this.getApplicationContext().getAssets();
        montserrat_regular = Typeface.createFromAsset(assetManager, "fonts/Montserrat-Regular.ttf");


        TOPIC_ARRAY[1] = locale.getDisplayCountry();
        DBAdapter dbAdapter = new DBAdapter(MainActivity.this);
        //populates database with topics to menu
        if (dbAdapter.getCountTopics() < TOPIC_ARRAY.length) {
            for (String t : TOPIC_ARRAY)
                dbAdapter.insertTopics(t, t);
        }

        if (dbAdapter.getCountMensagem() < MENSAGEM_ARRAY.length) {
            for (String m : MENSAGEM_ARRAY)
                dbAdapter.insertMensagem(m, m);
        }

        createToolbar();
        createRecyclerView();
        SOURCE = TOPIC_ARRAY[0];
        mTitle.setText(getString(R.string.toolbar_default_text) + " " + locale.getDisplayCountry());
        getSharedPreferences("topics", MODE_PRIVATE).edit().putString("topic", getString(R.string.toolbar_default_text) + " " + locale.getDisplayCountry()).apply();

        //translate topics to current language
        if (!locale.getLanguage().equals(LOCALE_DEFAULT)) {
            if (!(dbAdapter.getCountTopicsTranslated() == TOPIC_ARRAY.length))
                new TranslateTopics(MainActivity.this).execute();
            if (!(dbAdapter.getCountMensagemTranslated() == MENSAGEM_ARRAY.length))
                new TranslateMensagem(MainActivity.this).execute();

        } else {
            dbAdapter.deleteAllTitles();//delete the first 5 news read
            onLoadingSwipeRefreshLayout();
            layoutLoading.setVisibility(View.GONE);
            createDrawer(savedInstanceState, toolbar, montserrat_regular);
        }

        for (DBAdapter.Mensagem m : dbAdapter.getAllMensagem()) {
            Log.i("Mensagem", m.getId() + " - " + m.getMensagem() + "::" + m.getTranslated());
        }


        dbAdapter.close();
        createNotificationChannel();
        if (!checkAlarmExist())
            setAlarm();

    }

    private void createToolbar() {
        toolbar = findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        mTitle = findViewById(R.id.toolbar_title);
        mTitle.setTypeface(montserrat_regular);
    }

    private void createRecyclerView() {
        recyclerView = findViewById(R.id.card_recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
    }


    private void createDrawer(Bundle savedInstanceState, final Toolbar toolbar, Typeface montserrat_regular) {


        DBAdapter dbAdapter = new DBAdapter(MainActivity.this);
        topicList = dbAdapter.getAllTopics();
        dbAdapter.close();

        PrimaryDrawerItem item0 = new PrimaryDrawerItem().withIdentifier(0).withName(Html.fromHtml(topicList.get(0).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_googlenews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(Html.fromHtml(topicList.get(1).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_country).withTypeface(montserrat_regular);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName(Html.fromHtml(topicList.get(2).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_world).withTypeface(montserrat_regular);
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName(Html.fromHtml(topicList.get(3).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_business).withTypeface(montserrat_regular);
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(4).withName(Html.fromHtml(topicList.get(4).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_finance).withTypeface(montserrat_regular);
        PrimaryDrawerItem item5 = new PrimaryDrawerItem().withIdentifier(5).withName(Html.fromHtml(topicList.get(5).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_economy).withTypeface(montserrat_regular);
        PrimaryDrawerItem item6 = new PrimaryDrawerItem().withIdentifier(6).withName(Html.fromHtml(topicList.get(6).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_bitcoin).withTypeface(montserrat_regular);
        PrimaryDrawerItem item7 = new PrimaryDrawerItem().withIdentifier(7).withName(Html.fromHtml(topicList.get(7).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_culture).withTypeface(montserrat_regular);
        PrimaryDrawerItem item8 = new PrimaryDrawerItem().withIdentifier(8).withName(Html.fromHtml(topicList.get(8).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_gastronomy).withTypeface(montserrat_regular);
        PrimaryDrawerItem item9 = new PrimaryDrawerItem().withIdentifier(9).withName(Html.fromHtml(topicList.get(9).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_travels).withTypeface(montserrat_regular);
        PrimaryDrawerItem item10 = new PrimaryDrawerItem().withIdentifier(10).withName(Html.fromHtml(topicList.get(10).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_politic).withTypeface(montserrat_regular);
        PrimaryDrawerItem item11 = new PrimaryDrawerItem().withIdentifier(11).withName(Html.fromHtml(topicList.get(11).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_cience).withTypeface(montserrat_regular);
        PrimaryDrawerItem item12 = new PrimaryDrawerItem().withIdentifier(12).withName(Html.fromHtml(topicList.get(12).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_health).withTypeface(montserrat_regular);
        PrimaryDrawerItem item13 = new PrimaryDrawerItem().withIdentifier(13).withName(Html.fromHtml(topicList.get(13).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_sport).withTypeface(montserrat_regular);
        PrimaryDrawerItem item14 = new PrimaryDrawerItem().withIdentifier(14).withName(Html.fromHtml(topicList.get(14).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_hacker).withTypeface(montserrat_regular);
        PrimaryDrawerItem item15 = new PrimaryDrawerItem().withIdentifier(15).withName(Html.fromHtml(topicList.get(15).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_tecnology).withTypeface(montserrat_regular);
        PrimaryDrawerItem item16 = new PrimaryDrawerItem().withIdentifier(16).withName(Html.fromHtml(topicList.get(16).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_gamer).withTypeface(montserrat_regular);
        PrimaryDrawerItem item17 = new PrimaryDrawerItem().withIdentifier(17).withName(Html.fromHtml(topicList.get(17).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_entretenimento).withTypeface(montserrat_regular);
        PrimaryDrawerItem item18 = new PrimaryDrawerItem().withIdentifier(18).withName(Html.fromHtml(topicList.get(18).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_movie).withTypeface(montserrat_regular);
        PrimaryDrawerItem item19 = new PrimaryDrawerItem().withIdentifier(19).withName(Html.fromHtml(topicList.get(19).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_youtube).withTypeface(montserrat_regular);
        PrimaryDrawerItem item20 = new PrimaryDrawerItem().withIdentifier(20).withName(Html.fromHtml(topicList.get(20).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_twitch).withTypeface(montserrat_regular);
        PrimaryDrawerItem item21 = new PrimaryDrawerItem().withIdentifier(21).withName(Html.fromHtml(topicList.get(21).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_netflix).withTypeface(montserrat_regular);
        PrimaryDrawerItem item22 = new PrimaryDrawerItem().withIdentifier(22).withName(Html.fromHtml(topicList.get(22).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_world).withTypeface(montserrat_regular);
        PrimaryDrawerItem item23 = new PrimaryDrawerItem().withIdentifier(23).withName(Html.fromHtml(topicList.get(23).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_world).withTypeface(montserrat_regular);
        PrimaryDrawerItem item24 = new PrimaryDrawerItem().withIdentifier(24).withName(Html.fromHtml(topicList.get(24).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_world).withTypeface(montserrat_regular);
        PrimaryDrawerItem item25 = new PrimaryDrawerItem().withIdentifier(25).withName(Html.fromHtml(topicList.get(25).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_world).withTypeface(montserrat_regular);
        PrimaryDrawerItem item26 = new PrimaryDrawerItem().withIdentifier(26).withName(Html.fromHtml(topicList.get(26).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_world).withTypeface(montserrat_regular);
        PrimaryDrawerItem item27 = new PrimaryDrawerItem().withIdentifier(27).withName(Html.fromHtml(topicList.get(27).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_like_us).withTypeface(montserrat_regular);
        PrimaryDrawerItem item28 = new PrimaryDrawerItem().withIdentifier(28).withName(Html.fromHtml(topicList.get(28).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_mail).withTypeface(montserrat_regular);
        PrimaryDrawerItem item29 = new PrimaryDrawerItem().withIdentifier(29).withName(Html.fromHtml(topicList.get(29).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_about).withTypeface(montserrat_regular);
        PrimaryDrawerItem item30 = new PrimaryDrawerItem().withIdentifier(30).withName(Html.fromHtml(topicList.get(30).getTopicTranslate()).toString())
                .withIcon(R.drawable.ic_about).withTypeface(montserrat_regular);

        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.ic_back_header)
                .withSavedInstance(savedInstanceState)
                .build();

        result = new DrawerBuilder()
                .withAccountHeader(accountHeader)
                .withActivity(this)
                .withToolbar(toolbar)
                .withSelectedItem(0)
                .addDrawerItems(
                        item0, item1, item2,
                        new DividerDrawerItem(), item3, item4, item5, item6,
                        new DividerDrawerItem(), item7, item8, item9,
                        new DividerDrawerItem(), item10, item11, item12, item13,
                        new DividerDrawerItem(), item14, item15, item16,
                        new DividerDrawerItem(), item17, item18, item19, item20, item21,
                        new DividerDrawerItem(), item22, item23, item24, item25, item26,
                        new DividerDrawerItem(), item27, item28, item29, item30)
                .withOnDrawerItemClickListener(new OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        switch ((int) drawerItem.getIdentifier()) {

                            case 28:
                                rateApp();
                                break;
                            case 29:
                                sendEmail();
                                break;
                            case 30:
                                openAboutActivity();
                                break;
                            default:
                                SOURCE = topicList.get((int) drawerItem.getIdentifier()).getTopicTranslate();
                                getSharedPreferences("topics", MODE_PRIVATE).edit().putString("topic", SOURCE).apply();
                                //  mTitle.setText(((Nameable) drawerItem).getName().getText(MainActivity.this));
                                onLoadingSwipeRefreshLayout();
                                break;
                        }
                        return false;
                    }

                })
                .withSavedInstance(savedInstanceState)
                .build();
    }

    private void loadJSON() {

        swipeRefreshLayout.setRefreshing(true);
        //"https://news.google.com/news?cf=all&hl=language&pz=1&ned=country&q=topic&output=rss";
        String url = this.url.replace("topic", SOURCE);

        if (SOURCE.equals("Google News"))
            url = "https://news.google.com/rss?hl=" + locale.getLanguage() + "&sort=date&gl=" + countryCode;


        if (googleXmlNews != null)
            if (googleXmlNews.getStatus() == AsyncTask.Status.RUNNING)
                googleXmlNews.cancel(true);
        Log.i("URL", url);
        googleXmlNews = new GoogleXmlNews(url, MainActivity.this, recyclerView, swipeRefreshLayout);
        googleXmlNews.execute();
        //https://news.google.com/news?cf=all&hl=lang&pz=1&ned=coun&q=bbc-news&output=rss


    }

    @Override
    public void onRefresh() {
        loadJSON();
    }

    /*
     ** TODO: APP INDEXING(App is not indexable by Google Search; consider adding at least one Activity with an ACTION-VIEW) .
     ** TODO: ADDING ATTRIBUTE android:fullBackupContent
     **/
    private void onLoadingSwipeRefreshLayout() {
        if (!UtilityMethods.isNetworkAvailable()) {
            DBAdapter dbAdapter = new DBAdapter(MainActivity.this);
            Toast.makeText(MainActivity.this, dbAdapter.getMensagemTranslated(13), Toast.LENGTH_SHORT).show();
            dbAdapter.close();
        }
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        loadJSON();
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        DBAdapter dbAdapter = new DBAdapter(MainActivity.this);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_menu).setTitle(dbAdapter.getMensagemTranslated(11));
        dbAdapter.close();
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_menu:
                openAboutActivity();
                break;
            case R.id.action_search:
                openSearchActivity();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void openAboutActivity() {
        Intent aboutIntent = new Intent(this, AboutActivity.class);
        startActivity(aboutIntent);
        this.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    private void openSearchActivity() {
        Intent searchIntent = new Intent(this, SearchActivity.class);
        startActivity(searchIntent);
        this.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    private void sendEmail() {
        /*Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto: aplicativoparamobile@gmail.com"));
        startActivity(Intent.createChooser(emailIntent, "Send feedback"));*/
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ezequielportfolio.wordpress.com/contato/"));
        startActivity(browserIntent);
    }

    public void onBackPressed() {
        if (result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            DBAdapter dbAdapter = new DBAdapter(MainActivity.this);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
            builder.setTitle(R.string.app_name);
            builder.setIcon(R.mipmap.ic_launcher_round);
            builder.setMessage(Html.fromHtml(dbAdapter.getMensagemTranslated(5)).toString())
                    .setCancelable(false)
                    .setPositiveButton(dbAdapter.getMensagemTranslated(9), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setNegativeButton(dbAdapter.getMensagemTranslated(8), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            dbAdapter.close();
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        //add the values which need to be saved from the drawer to the bundle
        bundle = result.saveInstanceState(bundle);
        //add the values which need to be saved from the accountHeader to the bundle
        bundle = accountHeader.saveInstanceState(bundle);

        super.onSaveInstanceState(bundle);
        listState = recyclerView.getLayoutManager().onSaveInstanceState();
        bundle.putParcelable(Constants.RECYCLER_STATE_KEY, listState);
        bundle.putString(Constants.SOURCE, SOURCE);
        bundle.putString(Constants.TITLE_STATE_KEY, mTitle.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            SOURCE = savedInstanceState.getString(Constants.SOURCE);
            createToolbar();
            mTitle.setText(savedInstanceState.getString(Constants.TITLE_STATE_KEY));
            listState = savedInstanceState.getParcelable(Constants.RECYCLER_STATE_KEY);
            createDrawer(savedInstanceState, toolbar, montserrat_regular);
        }
    }

    private void setAlarm() {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(MainActivity.this, BootReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR * 8,
                AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
    }

    public boolean checkAlarmExist() {

        ComponentName receiver = new ComponentName(MainActivity.this, BootReceiver.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        Intent intent = new Intent(MainActivity.this, BootReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        boolean alarmUp = (PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_NO_CREATE) != null);

        return alarmUp;

    }

    private void rateApp() {
        final ReviewManager reviewManager = ReviewManagerFactory.create(MainActivity.this);
        //reviewManager = new FakeReviewManager(this);
        com.google.android.play.core.tasks.Task<ReviewInfo> request = reviewManager.requestReviewFlow();

        request.addOnCompleteListener(new com.google.android.play.core.tasks.OnCompleteListener<ReviewInfo>() {
            @Override
            public void onComplete(com.google.android.play.core.tasks.Task<ReviewInfo> task) {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    com.google.android.play.core.tasks.Task<Void> flow = reviewManager.launchReviewFlow(MainActivity.this, reviewInfo);
                    flow.addOnCompleteListener(new com.google.android.play.core.tasks.OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(com.google.android.play.core.tasks.Task<Void> task) {
                            Log.e("Rate Flow", "Complete");
                        }
                    });

                    flow.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e("Rate Flow", "Fail");
                            e.printStackTrace();
                        }
                    });

                } else {
                    Log.e("Rate Task", "Fail");
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Log.e("Rate Request", "Fail");
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (listState != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(listState);
        }


    }

    @Override
    protected void onDestroy() {
        if (googleXmlNews != null)
            googleXmlNews.cancel(true);
        super.onDestroy();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(IDCHANNEL, CHANNELNAME, importance);
            channel.setDescription(CHANNELDESCRIPTION);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private class TranslateTopics extends AsyncTask {

        private DBAdapter dbAdapter;
        private Activity activity;
        private AppBarLayout appBarLayout;

        public TranslateTopics(Context context) {
            dbAdapter = new DBAdapter(context);
            activity = (Activity) context;
            appBarLayout = findViewById(R.id.appBarLayout);
            appBarLayout.setVisibility(View.GONE);
            TextView textView = findViewById(R.id.textViewAdjustments);
            textView.setText(dbAdapter.getMensagemTranslated(3));
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            TextView textView = findViewById(R.id.textViewAdjustmentsProgress);
            List<Topic> topicList = dbAdapter.getAllTopics();
            for (int i = 2; i < topicList.size(); i++) {
                String topic = topicList.get(i).getTopic();
                String translate = "";
                if (!topicList.get(i).isTranslated() && (!topicList.get(i).getTopic().contains("Youtube")
                        && !topicList.get(i).getTopic().contains("Twitch") && !topicList.get(i).getTopic().contains("Netflix"))) {
                    translate = translate(LOCALE_DEFAULT, locale.getLanguage(), topic);

                    int finalI = i;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(finalI + "/" + topicList.size());
                        }
                    });


                    if (translate.length() > 0)
                        dbAdapter.updateTopics(topicList.get(i).getId(), translate);
                    else
                        dbAdapter.updateTopics(topicList.get(i).getId(), topic);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            onLoadingSwipeRefreshLayout();
            appBarLayout.setVisibility(View.VISIBLE);
            layoutLoading.setVisibility(View.GONE);
            dbAdapter.close();
            createDrawer(getIntent().getExtras(), toolbar, montserrat_regular);

        }
    }

    private class TranslateMensagem extends AsyncTask {

        private DBAdapter dbAdapter;

        public TranslateMensagem(Context context) {
            dbAdapter = new DBAdapter(context);
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            List<DBAdapter.Mensagem> mensagemList = dbAdapter.getAllMensagem();
            for (int i = 0; i < mensagemList.size(); i++) {
                String mensagem = mensagemList.get(i).getMensagem();
                String translate = "";
                if (!mensagemList.get(i).getIsTranlated()) {
                    translate = translate(LOCALE_DEFAULT, locale.getLanguage(), mensagem);

                    if (translate.length() > 0)
                        dbAdapter.updateMensagem(mensagemList.get(i).getId(), translate);
                    else
                        dbAdapter.updateMensagem(mensagemList.get(i).getId(), mensagem);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            layoutLoading.setVisibility(View.GONE);
            dbAdapter.close();

        }
    }


}

