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
import br.projeto.worldnews.adapter.DBAdapter;
import br.projeto.worldnews.model.ArticleStructure;
import br.projeto.worldnews.network.GoogleXmlNews;

public class SearchActivity extends AppCompatActivity {

    private EditText mEdtSearch;
    private TextView mTxvNoResultsFound;
    private SwipeRefreshLayout mSwipeRefreshSearch;
    private RecyclerView mRecyclerViewSearch;
    private Typeface montserrat_regular;
    private ArrayList<ArticleStructure> articleStructure = new ArrayList<>();
    private GoogleXmlNews googleXmlNews;

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
        DBAdapter dbAdapter = new DBAdapter(SearchActivity.this);
        mEdtSearch.setHint(dbAdapter.getMensagemTranslated(10));
        mSwipeRefreshSearch = findViewById(R.id.swipe_refresh_layout_search);
        mRecyclerViewSearch = findViewById(R.id.search_recycler_view);
        mTxvNoResultsFound = findViewById(R.id.tv_no_results);
        mTxvNoResultsFound.setText(dbAdapter.getMensagemTranslated(2));
        dbAdapter.close();
        mRecyclerViewSearch.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
    }

    private void searchEverything(final String search) {
        mSwipeRefreshSearch.setEnabled(true);
        mSwipeRefreshSearch.setRefreshing(true);
        String url = MainActivity.url.replace("topic", search);

        Log.e("URL", url);
        googleXmlNews = new GoogleXmlNews(url, SearchActivity.this, mRecyclerViewSearch, mSwipeRefreshSearch);
        googleXmlNews.execute();
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

    @Override
    protected void onDestroy() {
        if (googleXmlNews != null)
            googleXmlNews.cancel(true);
        super.onDestroy();
    }
}
