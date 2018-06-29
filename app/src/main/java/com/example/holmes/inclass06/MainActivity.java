package com.example.holmes.inclass06;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    static String API_KEY = "0aa4014b64244469bdb4807bfa263884";
    static String[] categories = {"Business", "Entertainment", "General",
            "Health","Science","Sports","Technology"};
    String selection = "Show Categories";
    ProgressDialog progress;
    Context context;
    String selectionURL;
    int position, size;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Top Headlines");
        context = this;
        ImageView ivPrev = findViewById(R.id.ivPrevious);
        ImageView ivNext = findViewById(R.id.ivNext);
        ivPrev.setEnabled(false);
        ivNext.setEnabled(false);



        findViewById(R.id.btnGo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected()){

                    progress = new ProgressDialog(MainActivity.this);
                    progress.setMessage("Loading News...");
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.setIndeterminate(true);
                    progress.setProgress(0);
                    progress.show();
                    showAlertDialogButtonClicked(v);

                }else{
                    Toast.makeText(context, "No Internet Connection Detected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.ivNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == size - 1) {
                    position = 0;
                    new GetNewsItemsAsync().execute(selectionURL);
                }else{
                    position++;
                    new GetNewsItemsAsync().execute(selectionURL);
                }
            }
        });

        findViewById(R.id.ivPrevious).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == 0) {
                    position = size - 1;
                    new GetNewsItemsAsync().execute(selectionURL);
                }else{
                    position--;
                    new GetNewsItemsAsync().execute(selectionURL);
                }
            }
        });
    }

    public void showAlertDialogButtonClicked(View view){
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Category");

        builder.setItems(categories, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                position = 0;

                selection = categories[which];
                TextView tvCat = findViewById(R.id.tvCategory);
                tvCat.setText(selection);
                selectionURL = "https://newsapi.org/v2/top-headlines?country=us&apiKey=0aa4014b64244469bdb4807bfa263884&category=" + selection;
                new GetNewsItemsAsync().execute(selectionURL);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo == null
                || !networkInfo.isConnected()
                || (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)){
            return false;
        }else{
            return true;
        }
    }


    private class GetNewsItemsAsync extends AsyncTask<String, Void, ArrayList<NewsItem>> {
        @Override
        protected ArrayList<NewsItem> doInBackground(String... params) {
            HttpURLConnection connection = null;
        ArrayList<NewsItem> result = new ArrayList<>();
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String json = IOUtils.toString(connection.getInputStream(), "UTF-8");
                    JSONObject root = new JSONObject(json);
                    JSONArray articles = root.getJSONArray("articles");

                    for (int i = 0; i < articles.length(); i++){
                        JSONObject articleJSON = articles.getJSONObject(i);
                        NewsItem newsItem = new NewsItem();
                        newsItem.setTitle(articleJSON.getString("title"));
                        newsItem.setDescription(articleJSON.getString("description"));
                        newsItem.setImageURL(articleJSON.getString("urlToImage"));
                        newsItem.setDate(articleJSON.getString("publishedAt"));
                        result.add(newsItem);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<NewsItem> result) {
            if (result != null && result.size() != 0) {
                ImageView ivImage = findViewById(R.id.ivImage);
                TextView tvTitle = findViewById(R.id.tvTitle);
                TextView tvDescription = findViewById(R.id.tvDescription);
                TextView tvPublishTime = findViewById(R.id.tvPublishTime);
                TextView tvPosition = findViewById(R.id.tvPosition);
                ImageView ivPrev = findViewById(R.id.ivPrevious);
                ImageView ivNext = findViewById(R.id.ivNext);
                ivImage.setVisibility(View.VISIBLE);
                Picasso.get().load(result.get(position).getImageURL()).into(ivImage);
                if(!result.get(position).getDescription().toString().isEmpty()
                        && !result.get(position).getDescription().toString().equals("")
                        && !result.get(position).getDescription().equals("null")) {
                    tvDescription.setText(result.get(position).getDescription());
                }else{
                    tvDescription.setText("");
                }
                tvTitle.setText(result.get(position).getTitle());
                tvPublishTime.setText(result.get(position).getDate());
                int picPosition = position+1;
                size = result.size();
                String listPosition = picPosition + " of " + size;
                tvPosition.setText(listPosition);
                if(size < 2){
                    ivPrev.setEnabled(false);
                    ivNext.setEnabled(false);
                }else{
                    ivPrev.setEnabled(true);
                    ivNext.setEnabled(true);
                }
            }else{
                Toast.makeText(context, "No News Found", Toast.LENGTH_SHORT).show();
            }
            for (int i = 0; i < 10000; i++){
                double x;
                for (int j = 0; j < 2000; j++){
                    x=Math.random();
                }
            }
            progress.dismiss();
        }
    }
}
