package br.projeto.worldnews.view;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import br.projeto.worldnews.R;
import br.projeto.worldnews.adapter.DataAdapter;
import br.projeto.worldnews.model.ArticleStructure;
import br.projeto.worldnews.network.GoogleXmlNews;

public class SearchActivity extends AppCompatActivity {

    private EditText mEdtSearch;
    private TextView mTxvNoResultsFound;
    private SwipeRefreshLayout mSwipeRefreshSearch;
    private RecyclerView mRecyclerViewSearch;
    private DataAdapter adapter;
    private Typeface montserrat_regular;
    private ArrayList<ArticleStructure> articleStructure = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        AssetManager assetManager = this.getApplicationContext().getAssets();
        montserrat_regular = Typeface.createFromAsset(assetManager, "fonts/Montserrat-Regular.ttf");

        createToolbar();
        initViews();

        mEdtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    searchEverything(mEdtSearch.getText().toString().trim());
                    return true;
                }

                return false;
            }
        });

        mSwipeRefreshSearch.setEnabled(false);
        mSwipeRefreshSearch.setColorSchemeResources(R.color.colorPrimary);
    }

    private void createToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                SearchActivity.this.overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void initViews() {
        mEdtSearch = findViewById(R.id.editText_search);
        mEdtSearch.setTypeface(montserrat_regular);
        mSwipeRefreshSearch = findViewById(R.id.swipe_refresh_layout_search);
        mRecyclerViewSearch = findViewById(R.id.search_recycler_view);
        mTxvNoResultsFound = findViewById(R.id.tv_no_results);
        mRecyclerViewSearch.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
    }

    private void searchEverything(final String search) {
        mSwipeRefreshSearch.setEnabled(true);
        mSwipeRefreshSearch.setRefreshing(true);
        /*
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addNetworkInterceptor(new ResponseCacheInterceptor());
        httpClient.addInterceptor(new OfflineResponseCacheInterceptor());
        httpClient.cache(new Cache(new File(MyTimesApplication.getMyTimesApplicationInstance()
                .getCacheDir(), "ResponsesCache"), 10 * 1024 * 1024));
        httpClient.readTimeout(60, TimeUnit.SECONDS);
        httpClient.connectTimeout(60, TimeUnit.SECONDS);
        httpClient.addInterceptor(logging);

        ApiInterface request = ApiClient.getClient(httpClient).create(ApiInterface.class);

        String sortBy = "publishedAt";
        String language = "en";
        Call<NewsResponse> call = request.getSearchResults(search, sortBy, language, Constants.API_KEY_1);
        call.enqueue(new Callback<NewsResponse>() {

            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {

                if (response.isSuccessful() && response.body().getArticles() != null) {

                    if (response.body().getTotalResults() != 0) {
                        if (!articleStructure.isEmpty()) {
                            articleStructure.clear();
                        }

                        articleStructure = response.body().getArticles();
                        adapter = new DataAdapter(SearchActivity.this, articleStructure);
                        mRecyclerViewSearch.setVisibility(View.VISIBLE);
                        mTxvNoResultsFound.setVisibility(View.GONE);
                        mRecyclerViewSearch.setAdapter(adapter);
                        mSwipeRefreshSearch.setRefreshing(false);
                        mSwipeRefreshSearch.setEnabled(false);
                    } else if (response.body().getTotalResults() == 0){
                        mSwipeRefreshSearch.setRefreshing(false);
                        mSwipeRefreshSearch.setEnabled(false);
                        mTxvNoResultsFound.setVisibility(View.VISIBLE);
                        mRecyclerViewSearch.setVisibility(View.GONE);
                        mTxvNoResultsFound.setText("No Results found for \"" + search + "\"." );
                    }
                }
            }


            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                mSwipeRefreshSearch.setRefreshing(false);
                mSwipeRefreshSearch.setEnabled(false);
            }
        });*/

        String url = MainActivity.url.replace("topic", search);

        Log.e("URL", url);
        new GoogleXmlNews(url, SearchActivity.this, mRecyclerViewSearch, mSwipeRefreshSearch).execute();
        //https://news.google.com/news?cf=all&hl=lang&pz=1&ned=coun&q=bbc-news&output=rss


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                mEdtSearch.setText("");
                mEdtSearch.requestFocus();
                InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                mgr.showSoftInput(mEdtSearch, InputMethodManager.SHOW_IMPLICIT);
                mRecyclerViewSearch.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cancelSearch() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }
}
