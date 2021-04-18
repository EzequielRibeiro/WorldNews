package br.projeto.worldnews.view;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.projeto.worldnews.R;
import br.projeto.worldnews.adapter.DBAdapter;
import br.projeto.worldnews.model.ArticleStructure;
import br.projeto.worldnews.model.Constants;
import br.projeto.worldnews.model.Topic;
import br.projeto.worldnews.network.GoogleXmlNews;
import br.projeto.worldnews.util.BootReceiver;
import br.projeto.worldnews.util.UtilityMethods;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static String url = "https://news.google.com/news?cf=all&hl=language&pz=1&ned=country&q=topic&output=rss";
    private final static String LOCALE_DEFAULT = "en";
    private String[] TOPIC_ARRAY = {"Google News", "Country", "Business", "World", "Finance", "Culture", "Gastronomy",
            "Youtube", "Twitch", "Hacker", "Politics", "Science", "Technology", "Economy", "Entertainment",
            "Sports", "Health", "Videogame", "BitCoin", "Films", "Travels", "Europe", "South America",
            "North America", "Asia", "Africa", "Middle East", "Oceania", "Rate us!", "Contact us", "About the app"};

    private String SOURCE;
    private GoogleXmlNews googleXmlNews;
    private ArrayList<ArticleStructure> articleStructure = new ArrayList<>();
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
    private LinearLayout linearLayoutLoading;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        linearLayoutLoading = findViewById(R.id.linearLayoutLoading);

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
        if (dbAdapter.getCountTopics() == 0) {
            for (String t : TOPIC_ARRAY)
                dbAdapter.insertTopics(t, t);
        }
        dbAdapter.close();
        createToolbar();
        createRecyclerView();
        SOURCE = TOPIC_ARRAY[0];
        mTitle.setText(getString(R.string.toolbar_default_text) + " " + locale.getDisplayCountry());


        //translate topics to current language
        if (!locale.getLanguage().equals(LOCALE_DEFAULT)) {
            new Loading().execute();
        } else {
            onLoadingSwipeRefreshLayout();
            linearLayoutLoading.setVisibility(View.GONE);
            createDrawer(savedInstanceState, toolbar, montserrat_regular);
        }

        createNotificationChannel();
        if (!checkAlarmExist())
            setAlarm();

    }

    private static String translate(String langFrom, String langTo, String text) {

        final String yourURL = "https://script.google.com/macros/s/AKfycbyHrs_kLCmXJB-fH_mS2ODtud3y0lR4Povq9nE2EqCSBPSiqjF80PNMKJohEV3TrZws/exec";

        try {

            String urlStr = yourURL + "?q=" + URLEncoder.encode(text, "UTF-8") + "&target=" + langTo + "&source="
                    + langFrom;

            URL url = new URL(urlStr);
            StringBuilder response = new StringBuilder();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
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

        PrimaryDrawerItem item0 = new PrimaryDrawerItem().withIdentifier(0).withName(topicList.get(0).getTopicTranslate())
                .withIcon(R.drawable.ic_googlenews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(topicList.get(1).getTopicTranslate())
                .withIcon(R.drawable.ic_googlenews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName(topicList.get(2).getTopicTranslate())
                .withIcon(R.drawable.ic_bbcnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName(topicList.get(3).getTopicTranslate())
                .withIcon(R.drawable.ic_thehindu).withTypeface(montserrat_regular);
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(4).withName(topicList.get(4).getTopicTranslate())
                .withIcon(R.drawable.ic_timesofindia).withTypeface(montserrat_regular);
        PrimaryDrawerItem item5 = new PrimaryDrawerItem().withIdentifier(5).withName(topicList.get(5).getTopicTranslate())
                .withIcon(R.drawable.ic_googlenews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item6 = new PrimaryDrawerItem().withIdentifier(6).withName(topicList.get(6).getTopicTranslate())
                .withIcon(R.drawable.ic_buzzfeednews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item7 = new PrimaryDrawerItem().withIdentifier(7).withName(topicList.get(7).getTopicTranslate())
                .withIcon(R.drawable.ic_mashablenews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item8 = new PrimaryDrawerItem().withIdentifier(8).withName(topicList.get(8).getTopicTranslate())
                .withIcon(R.drawable.ic_mtvnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item9 = new PrimaryDrawerItem().withIdentifier(9).withName(topicList.get(9).getTopicTranslate())
                .withIcon(R.drawable.ic_googlenews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item10 = new PrimaryDrawerItem().withIdentifier(10).withName(topicList.get(10).getTopicTranslate())
                .withIcon(R.drawable.ic_bbcsports).withTypeface(montserrat_regular);
        PrimaryDrawerItem item11 = new PrimaryDrawerItem().withIdentifier(11).withName(topicList.get(11).getTopicTranslate())
                .withIcon(R.drawable.ic_espncricinfo).withTypeface(montserrat_regular);
        PrimaryDrawerItem item12 = new PrimaryDrawerItem().withIdentifier(12).withName(topicList.get(12).getTopicTranslate())
                .withIcon(R.drawable.ic_talksport).withTypeface(montserrat_regular);
        PrimaryDrawerItem item13 = new PrimaryDrawerItem().withIdentifier(13).withName(topicList.get(13).getTopicTranslate())
                .withIcon(R.drawable.ic_googlenews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item14 = new PrimaryDrawerItem().withIdentifier(14).withName(topicList.get(14).getTopicTranslate())
                .withIcon(R.drawable.ic_medicalnewstoday).withTypeface(montserrat_regular);
        PrimaryDrawerItem item15 = new PrimaryDrawerItem().withIdentifier(15).withName(topicList.get(15).getTopicTranslate())
                .withIcon(R.drawable.ic_nationalgeographic).withTypeface(montserrat_regular);
        PrimaryDrawerItem item16 = new PrimaryDrawerItem().withIdentifier(16).withName(topicList.get(16).getTopicTranslate())
                .withIcon(R.drawable.ic_googlenews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item17 = new PrimaryDrawerItem().withIdentifier(17).withName(topicList.get(17).getTopicTranslate())
                .withIcon(R.drawable.ic_ccnnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item18 = new PrimaryDrawerItem().withIdentifier(18).withName(topicList.get(18).getTopicTranslate())
                .withIcon(R.drawable.ic_ccnnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item19 = new PrimaryDrawerItem().withIdentifier(19).withName(topicList.get(19).getTopicTranslate())
                .withIcon(R.drawable.ic_ccnnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item20 = new PrimaryDrawerItem().withIdentifier(20).withName(topicList.get(20).getTopicTranslate())
                .withIcon(R.drawable.ic_ccnnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item21 = new PrimaryDrawerItem().withIdentifier(21).withName(topicList.get(21).getTopicTranslate())
                .withIcon(R.drawable.ic_ccnnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item22 = new PrimaryDrawerItem().withIdentifier(22).withName(topicList.get(22).getTopicTranslate())
                .withIcon(R.drawable.ic_ccnnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item23 = new PrimaryDrawerItem().withIdentifier(23).withName(topicList.get(23).getTopicTranslate())
                .withIcon(R.drawable.ic_ccnnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item24 = new PrimaryDrawerItem().withIdentifier(24).withName(topicList.get(24).getTopicTranslate())
                .withIcon(R.drawable.ic_ccnnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item25 = new PrimaryDrawerItem().withIdentifier(25).withName(topicList.get(25).getTopicTranslate())
                .withIcon(R.drawable.ic_ccnnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item26 = new PrimaryDrawerItem().withIdentifier(26).withName(topicList.get(26).getTopicTranslate())
                .withIcon(R.drawable.ic_ccnnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item27 = new PrimaryDrawerItem().withIdentifier(27).withName(topicList.get(27).getTopicTranslate())
                .withIcon(R.drawable.ic_ccnnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item28 = new PrimaryDrawerItem().withIdentifier(28).withName(topicList.get(28).getTopicTranslate())
                .withIcon(R.drawable.ic_ccnnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item29 = new PrimaryDrawerItem().withIdentifier(29).withName(topicList.get(29).getTopicTranslate())
                .withIcon(R.drawable.ic_ccnnews).withTypeface(montserrat_regular);
        PrimaryDrawerItem item30 = new PrimaryDrawerItem().withIdentifier(30).withName(topicList.get(30).getTopicTranslate())
                .withIcon(R.drawable.ic_ccnnews).withTypeface(montserrat_regular);


        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.ic_back)
                .withSavedInstance(savedInstanceState)
                .build();

        result = new DrawerBuilder()
                .withAccountHeader(accountHeader)
                .withActivity(this)
                .withToolbar(toolbar)
                .withSelectedItem(0)
                .addDrawerItems(item0, item1, item2, item3, item4, item5, item6, item7, item8, item9,
                        item10, item11, item12, item13, item14, item15, item16, item17, item18, item19, item20,
                        item21, item22, item23, item24, item25, item26, item27, item28, item29, item30)
                .withOnDrawerItemClickListener(new OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        int selected = (int) (long) position - 1;

                        if (selected <= 27) {
                            SOURCE = topicList.get(selected).getTopicTranslate();
                            mTitle.setText(((Nameable) drawerItem).getName().getText(MainActivity.this));
                            onLoadingSwipeRefreshLayout();
                        }

                        switch (selected) {

                            case 28:
                                Toast.makeText(MainActivity.this, "rate us!", Toast.LENGTH_LONG).show();
                                break;

                            case 29:
                                sendEmail();
                                break;
                            case 30:
                                openAboutActivity();
                                break;
                            default:
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
            url = "https://news.google.com/rss?hl=" + locale.getLanguage() + "&gl=" + countryCode;

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
            Toast.makeText(MainActivity.this, "Could not load latest News. Please turn on the Internet.", Toast.LENGTH_SHORT).show();
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto: aplicativoparamobile@gmail.com"));
        startActivity(Intent.createChooser(emailIntent, "Send feedback"));
    }

    public void onBackPressed() {
        if (result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
            builder.setTitle(R.string.app_name);
            builder.setIcon(R.drawable.ic_launcher);
            builder.setMessage("Do you want to Exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
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
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_HOUR,
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
            CharSequence name = "NewsApp";
            String description = "NewsApp";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("NewsApp", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private class Loading extends AsyncTask {

        DBAdapter dbAdapter = new DBAdapter(getApplicationContext());

        @Override
        protected Object doInBackground(Object[] objects) {

            topicList = dbAdapter.getAllTopics();
            for (int i = 2; i < topicList.size(); i++) {
                String topic = topicList.get(i).getTopic();
                String translate = "";
                if (!topicList.get(i).isTranslated() && (!topicList.get(i).getTopic().contains("Youtube")
                        && !topicList.get(i).getTopic().contains("Twitch"))) {
                    translate = translate(LOCALE_DEFAULT, locale.getLanguage(), topic);
                    if (translate.length() > 0)
                        dbAdapter.updateTopics(topicList.get(i).getId(), translate);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            onLoadingSwipeRefreshLayout();
            linearLayoutLoading.setVisibility(View.GONE);
            dbAdapter.close();
            createDrawer(getIntent().getExtras(), toolbar, montserrat_regular);

        }
    }


}

