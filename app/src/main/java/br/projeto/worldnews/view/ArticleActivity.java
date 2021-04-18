package br.projeto.worldnews.view;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import br.projeto.worldnews.R;
import br.projeto.worldnews.model.Constants;

/*
* Article Activity is loaded when the user presses on one of the card views of the Recycler View
* which is present in the Main Activity.
* */

public class ArticleActivity extends AppCompatActivity {

    private Typeface montserrat_regular;
    private Typeface montserrat_semiBold;
    private String url;
    private LoadTask loadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        createToolbar();
        floatingButton();
        assetManager();
        receiveFromDataAdapter(montserrat_regular, montserrat_semiBold);
        buttonLinktoFullArticle(montserrat_regular);
    }

    private void assetManager() {
        AssetManager assetManager = this.getApplicationContext().getAssets();
        montserrat_regular = Typeface.createFromAsset(assetManager, "fonts/Montserrat-Regular.ttf");
        montserrat_semiBold = Typeface.createFromAsset(assetManager, "fonts/Montserrat-SemiBold.ttf");
    }

    private void buttonLinktoFullArticle(Typeface montserrat_regular) {
        Button linkToFullArticle = findViewById(R.id.article_button);
        linkToFullArticle.setTypeface(montserrat_regular);
        linkToFullArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebViewActivity();
            }
        });
    }

    private void openWebViewActivity() {
        Intent browserIntent = new Intent(ArticleActivity.this, WebViewActivity.class);
        browserIntent.putExtra(Constants.INTENT_URL, url);
        startActivity(browserIntent);
        this.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    private void receiveFromDataAdapter(Typeface montserrat_regular, Typeface montserrat_semiBold) {
        String headLine = getIntent().getStringExtra(Constants.INTENT_HEADLINE);
        String description = getIntent().getStringExtra(Constants.INTENT_DESCRIPTION);
        String date = getIntent().getStringExtra(Constants.INTENT_DATE);
        String imgURL = getIntent().getStringExtra(Constants.INTENT_IMG_URL);
        url = getIntent().getStringExtra(Constants.INTENT_ARTICLE_URL);

        TextView content_Headline = findViewById(R.id.content_Headline);
        content_Headline.setText(headLine);
        content_Headline.setTypeface(montserrat_semiBold);

        TextView content_Description = findViewById(R.id.content_Description);
        content_Description.setText(description);
        content_Description.setTypeface(montserrat_regular);

        TextView content_Date = findViewById(R.id.content_Date);
        content_Date.setText(date);
        content_Date.setTypeface(montserrat_regular);

        loadTask = new LoadTask(content_Description);
        loadTask.execute(new String[]{imgURL, url});

    }

    private void createToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_article);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void floatingButton() {
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this news! Send from MyTimes App\n" + Uri.parse(url));
                startActivity(Intent.createChooser(shareIntent, "Share with"));
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*
            * Override the Up/Home Button
            * */
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    protected void onDestroy() {

        if (loadTask != null)
            loadTask.cancel(true);
        super.onDestroy();
    }

    private class LoadTask extends AsyncTask<String, Void, String[]> {

        private String des;
        private TextView textView;


        public LoadTask(TextView textView) {
            this.textView = textView;
        }

        protected String[] doInBackground(String... url) {

            ImageView collapsingImage = findViewById(R.id.collapsingImage);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Glide.with(ArticleActivity.this)
                                    .load(url[0])
                                    .centerCrop()
                                    .error(R.drawable.default_news_image)
                                    .crossFade()
                                    .into(collapsingImage);
                        } catch (IllegalArgumentException i) {
                            i.printStackTrace();
                        }
                    }
                });

            Document document = null;
            try {
                document = Jsoup.connect(url[1]).get();
                des = document.select("meta[name=description]").first().attr("content");

            } catch (IOException ioException) {
                ioException.printStackTrace();
                des = "";

            } catch (NullPointerException e) {
                e.printStackTrace();
                des = "";
            }
            return new String[]{des};
        }

        protected void onPostExecute(String... result) {
            super.onPostExecute(result);
            if (result[0].length() > 0)
                textView.setText(result[0]);
        }
    }
}
