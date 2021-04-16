package br.projeto.worldnews.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

import br.projeto.worldnews.R;
import br.projeto.worldnews.model.Article;
import br.projeto.worldnews.model.Constants;
import br.projeto.worldnews.network.ImageExtractor;
import br.projeto.worldnews.view.ArticleActivity;

/*
 ** This Class is Used to fetch the data from the POJO Article and bind them to the views.
 **/
public class DataAdapterArticle extends RecyclerView.Adapter<DataAdapterArticle.ViewHolder> {

    private ArrayList<Article> articles;
    private Context mContext;
    private int lastPosition = -1;

    public DataAdapterArticle(Context mContext, ArrayList<Article> articles) {
        this.mContext = mContext;
        this.articles = articles;
    }

    /*
     ** inflating the cardView.
     **/
    @Override
    public DataAdapterArticle.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapterArticle.ViewHolder holder, int position) {

        String title = articles.get(position).getTitle();
        if (title.endsWith("- Times of India")) {
            title = title.replace("- Times of India", "");
        } else if(title.endsWith(" - Firstpost")) {
            title = title.replace(" - Firstpost", "");
        }

        holder.tv_card_main_title.setText(title);

        String urlImage = articles.get(position).getUrl();

        try {
            articles.get(position).setUrlToImage(ImageExtractor.extractImageUrl(urlImage));
            Glide.with(mContext)
                    .load(articles.get(position).getUrlToImage())
                    .thumbnail(0.1f)
                    .centerCrop()
                    .error(R.drawable.ic_placeholder)
                    .into(holder.img_card_main);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.item_animation_fall_down);
            holder.cardView.startAnimation(animation);
            lastPosition = position;
        }
    }

    /*
     ** Last parameter for binding the articles in OnBindViewHolder.
     **/
    @Override
    public int getItemCount() {
        return articles.size();
    }

    /*
     ** ViewHolder class which holds the different views in the recyclerView .
     **/
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        AssetManager assetManager = mContext.getApplicationContext().getAssets();
        Typeface montserrat_regular = Typeface.createFromAsset(assetManager, "fonts/Montserrat-Regular.ttf");
        private TextView tv_card_main_title;
        private ImageView img_card_main;
        private CardView cardView;

        public ViewHolder(View view) {
            super(view);
            tv_card_main_title = view.findViewById(R.id.tv_card_main_title);
            tv_card_main_title.setTypeface(montserrat_regular);
            img_card_main = view.findViewById(R.id.img_card_main);
            cardView = view.findViewById(R.id.card_row);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String headLine = articles.get(getAdapterPosition()).getTitle();
            if (headLine.endsWith(" - Times of India")) {
                headLine = headLine.replace(" - Times of India", "");
            } else if(headLine.endsWith(" - Firstpost")) {
                headLine = headLine.replace(" - Firstpost", "");
            }
            String description = articles.get(getAdapterPosition()).getDescription();
            String date = articles.get(getAdapterPosition()).getPublishedAt();
            String imgURL = articles.get(getAdapterPosition()).getUrlToImage();
            String URL = articles.get(getAdapterPosition()).getUrl();

            Intent intent = new Intent(mContext, ArticleActivity.class);

            intent.putExtra(Constants.INTENT_HEADLINE, headLine);
            intent.putExtra(Constants.INTENT_DESCRIPTION, description);
            intent.putExtra(Constants.INTENT_DATE, date);
            intent.putExtra(Constants.INTENT_IMG_URL, imgURL);
            intent.putExtra(Constants.INTENT_ARTICLE_URL, URL);

            mContext.startActivity(intent);

            ((Activity) mContext).overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }
    }
}

