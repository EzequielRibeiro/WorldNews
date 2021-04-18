package br.projeto.worldnews.network;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import br.projeto.worldnews.MyTimesApplication;
import br.projeto.worldnews.R;
import br.projeto.worldnews.adapter.DBAdapter;
import br.projeto.worldnews.adapter.DataAdapterArticle;
import br.projeto.worldnews.model.Article;
import br.projeto.worldnews.network.interceptors.OfflineResponseCacheInterceptor;
import br.projeto.worldnews.network.interceptors.ResponseCacheInterceptor;
import br.projeto.worldnews.view.SearchActivity;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

public class GoogleXmlNews extends AsyncTask<String, Void, String> {

    // We don't use namespaces
    private static final String ns = null;
    private okhttp3.Response response;
    private ArrayList<Article> list;
    private Activity context;
    private String url;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Exception exception;
    private DataAdapterArticle adapter;
    private DBAdapter dbAdapter;


    public GoogleXmlNews(String url, Activity context, RecyclerView recyclerView, SwipeRefreshLayout swipeRefreshLayout) {
        list = new ArrayList<>();
        dbAdapter = new DBAdapter(context);
        this.context = context;
        this.url = url;
        this.recyclerView = recyclerView;
        this.swipeRefreshLayout = swipeRefreshLayout;

    }

    public GoogleXmlNews(Activity context) {
        this.context = context;
        dbAdapter = new DBAdapter(context);
    }

    public static String readTitle(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }

    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = " ";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private String readLink(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String link = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "link");
        return link;
    }

    private void readXML() {

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
                            news.setTitle(readTitle(xpp)); //extract the headline
                    } else if (xpp.getName().equalsIgnoreCase("link")) {
                        if (insideItem) {
                            news.setUrl(readLink(xpp)); //extract the link of article
                            //  news.setUrlToImage(extractImageUrl(news.getUrl()));
                            //  news.setUrlToImage(ImageExtractor.extractImageUrl(news.getUrl()));
                        }
                    } else if (xpp.getName().equalsIgnoreCase("description")) {
                        if (insideItem)
                            news.setDescription(readDescription(xpp)); //extract the link of article
                    } else if (xpp.getName().equalsIgnoreCase("pubDate")) {
                        if (insideItem)
                            news.setPublishedAt(readPubDate(xpp)); //extract the link of article
                    } else if (xpp.getName().equalsIgnoreCase("source")) {
                        if (insideItem)
                            news.setSource(readSource(xpp)); //extract the link of article
                    }

                } else if (xpp.getEventType() == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {

                    list.add(news);
                    insideItem = false;

                    if (dbAdapter != null)
                        if (dbAdapter.getCountTitles() < 5) {
                            if (dbAdapter.existTitleInTable(news.getTitle()) == 0) {
                                dbAdapter.insertTitle(news.getTitle());
                            }
                        } else {
                            dbAdapter.close();
                            dbAdapter = null;
                        }

                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            exception = e;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            exception = e;
        } catch (IOException e) {
            e.printStackTrace();
            exception = e;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            exception = e;
        }

        if (exception != null) {

            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "an error has occurred", Toast.LENGTH_LONG).show();
                }
            });

        }

    }
 /*
    private String extractImageUrl(String urlImage) {

        Document doc = null;
        String imageUrl = "";
        try {
            doc = Jsoup.connect(urlImage).get();
            Element elementImage = doc.select("meta[property=og:image]").first();
            if (elementImage != null && !elementImage.attr("content").isEmpty()) {
                imageUrl = elementImage.attr("content");
            } else {
                elementImage = doc.select("link[rel=image_src]").first();
                if (elementImage != null && !elementImage.attr("href").isEmpty()) {
                    imageUrl = elementImage.attr("href");
                } else {
                    elementImage = doc.select("img[src~=(?i)\\.(png|jpe?g)]").first();
                    if (elementImage != null) {
                        imageUrl = elementImage.attr("src");
                    }
                }
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return imageUrl;

    }

   private void getImagemArticle(String url, String title){
        Document document;
        try {
            //Get Document object after parsing the html from given url.
            document = Jsoup.connect(url).get();

            //Get images from document object.
            Elements images =
                    document.select("img[src~=(?i)\\.(png|jpe?g|gif)]");

            //Iterate images and print image attributes.
            for (Element image : images) {
                System.out.println("Image Source: " + image.attr("src"));
                System.out.println("Image Height: " + image.attr("height"));
                System.out.println("Image Width: " + image.attr("width"));
                System.out.println("Image Alt Text: " + image.attr("alt"));
                System.out.println("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/

    private String readDescription(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        try {
            description = description.substring(description.indexOf("target=\"_blank\">") + 16, description.indexOf("</a>"));
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            return " ";
        }
        return description;
    }

    private String readPubDate(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "pubDate");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "pubDate");
        return title;
    }

    private String readSource(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "source");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "source");
        return title;
    }

    ////http://news.google.com/news?q=apple&output=rss
    public InputStream getInputStream(String mUrl) {
        try {
            URL url = new URL(mUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(20000);
            con.setReadTimeout(20000);
            return con.getInputStream();
        } catch (IOException e) {
            Log.w("ERROR: ", "Exception while retrieving the input stream", e);
            return null;
        }
    }

    @Override
    protected String doInBackground(String... params) {

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
                .url(url)
                .build();

        OkHttpClient httpClient1 = httpClient.build();
        try {
            response = httpClient1.newCall(request1).execute();

            if (response.isSuccessful()) {
                readXML();
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
            exception = ioException;

            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "an error has occurred", Toast.LENGTH_LONG).show();
                }
            });
        }
        return null;
    }

    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        adapter = new DataAdapterArticle(context, list);
        recyclerView.setAdapter(adapter);

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = context.findViewById(R.id.tv_no_results);

                if (list.isEmpty()) {
                    swipeRefreshLayout.setRefreshing(false);
                    swipeRefreshLayout.setEnabled(false);
                    recyclerView.setVisibility(View.GONE);
                    if (context instanceof SearchActivity)
                        textView.setVisibility(View.VISIBLE);

                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                    swipeRefreshLayout.setEnabled(false);

                    if (context instanceof SearchActivity)
                        textView.setVisibility(View.GONE);
                }
            }
        });


    }

}
