package com.example.holmes.inclass06;

import android.os.AsyncTask;
import android.util.Log;


import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class GetNewsItemAsync extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        String result = null;
        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
               result = IOUtils.toString(connection.getInputStream(), "UTF-8");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Log.d("demo", result);
        } else {
            Log.d("demo", "null result");
        }
    }
}

