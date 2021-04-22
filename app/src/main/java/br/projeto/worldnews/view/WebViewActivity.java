package br.projeto.worldnews.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.DefaultAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.monstertechno.adblocker.AdBlockerWebView;
import com.monstertechno.adblocker.util.AdBlocker;

import br.projeto.worldnews.R;
import br.projeto.worldnews.model.Constants;


public class WebViewActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private String url;
    private TextView mTitle;
    private Typeface montserrat_regular;
    private float m_downX;
    private LinearLayout adContainer;
    private AdView mAdView;
    private com.amazon.device.ads.AdLayout amazonAdView;
    private com.google.android.gms.ads.AdView admobAdView;
    private com.amazon.device.ads.InterstitialAd interstitialAdAmazon;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        AssetManager assetManager = this.getApplicationContext().getAssets();
        montserrat_regular = Typeface.createFromAsset(assetManager, "fonts/Montserrat-Regular.ttf");

        url = getIntent().getStringExtra(Constants.INTENT_URL);

        /*
         ** Custom Toolbar ( App Bar )
         **/
        createToolbar();
        adContainer = findViewById(R.id.containerAd);
        webView = findViewById(R.id.webView_article);
        webView.getSettings().setBuiltInZoomControls(true);
        new AdBlockerWebView.init(this).initializeWebView(webView);
        progressBar = findViewById(R.id.progressBar);

        if (savedInstanceState == null) {
            webView.loadUrl(url);
            initWebView();
        }

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(
                    InitializationStatus initializationStatus) {
            }
        });
        com.amazon.device.ads.AdRegistration.setAppKey(getString(R.string.amazon_ad_unit_id));
        // com.amazon.device.ads.AdRegistration.enableTesting(true);
        /*
        List<String> testDeviceIds = Arrays.asList("DB530A1BBBDBFE8567328113528A19EF", "49EB8CE6C2EA8D132E11FA3F75D28D0B");
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);

         */
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        loadInterstitialAd(adRequest);
        adListner();

    }

    private void adListner() {

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {

            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.i("AdMob", "banner fail code " + adError.getCode() + ": " + adError.getMessage());
                loadAdAmazon();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });
    }

    private void loadInterstitialAd(AdRequest adRequest) {
        InterstitialAd.load(this, getString(R.string.intersticial_unit_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;

            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i("Admob", "Interstitial fail code " + loadAdError.getCode() + ": " + loadAdError.getMessage());
                loadAdAmazonInterstitial();
            }
        });

    }

    private void loadAdAmazonInterstitial() {
        interstitialAdAmazon = new com.amazon.device.ads.InterstitialAd(WebViewActivity.this);
        interstitialAdAmazon.loadAd();

        interstitialAdAmazon.setListener(new DefaultAdListener() {
            @Override
            public void onAdLoaded(Ad ad, AdProperties adProperties) {
            }

            @Override
            public void onAdFailedToLoad(Ad ad, com.amazon.device.ads.AdError error) {
                super.onAdFailedToLoad(ad, error);
                Log.i("AdAmazon", "Interstitial fail code " + error.getCode() + ": " + error.getMessage());
            }

        });


    }

    private void loadAdAmazon() {
        adContainer.removeView(mAdView);
        amazonAdView = new com.amazon.device.ads.AdLayout(this, com.amazon.device.ads.AdSize.SIZE_320x50);
        admobAdView = new com.google.android.gms.ads.AdView(this);
        admobAdView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        admobAdView.setAdUnitId(getString(R.string.amazon_ad_unit_id));
        adContainer.addView(amazonAdView);
        amazonAdView.loadAd(new com.amazon.device.ads.AdTargetingOptions());

        amazonAdView.setListener(new com.amazon.device.ads.AdListener() {
            @Override
            public void onAdLoaded(Ad ad, AdProperties adProperties) {

            }

            @Override
            public void onAdFailedToLoad(Ad ad, AdError adError) {
                adContainer.removeView(amazonAdView);
                Log.i("AdAmazon", "banner fail code " + adError.getCode() + ": " + adError.getMessage());
            }

            @Override
            public void onAdExpanded(Ad ad) {

            }

            @Override
            public void onAdCollapsed(Ad ad) {

            }

            @Override
            public void onAdDismissed(Ad ad) {

            }
        });


    }

    private void createToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_web_view);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTitle = findViewById(R.id.toolbar_title_web_view);
        mTitle.setTypeface(montserrat_regular);
        if (url.length() > "https//".length())
            mTitle.setText(url);
        else
            mTitle.setText("");
    }

    private void initWebView() {

        webView.setWebChromeClient(new MyWebChromeClient(this));
        webView.setWebViewClient(new WebViewClient() {


            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

                return AdBlockerWebView.blockAds(view, url) ? AdBlocker.createEmptyResource() :
                        super.shouldInterceptRequest(view, url);

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                progressBar.setVisibility(View.GONE);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        });

        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setHorizontalScrollBarEnabled(false);

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getPointerCount() > 1) {
                    //Multi touch detected
                    return true;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        // save the x
                        m_downX = event.getX();
                    }
                    break;

                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        // set x so that it doesn't move
                        event.setLocation(m_downX, event.getY());
                    }
                    break;
                }

                return false;
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
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(Constants.TITLE_WEBVIEW_KEY, url);
        webView.saveState(bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            createToolbar();
            webView.restoreState(savedInstanceState);
            mTitle.setText(savedInstanceState.getString(Constants.TITLE_WEBVIEW_KEY));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
        if (mInterstitialAd != null) {
            mInterstitialAd.show(WebViewActivity.this);
        } else {
            if (interstitialAdAmazon != null)
                interstitialAdAmazon.showAd();
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        Context context;

        public MyWebChromeClient(Context context) {
            super();
            this.context = context;
        }
    }

}
