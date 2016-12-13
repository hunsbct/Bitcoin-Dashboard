package com.example.codyhunsberger.bitcoindashboard;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

class UrlToJsonString extends AsyncTask<String, Void, String> {
    private onJsonReceivedListener listener;

    public interface onJsonReceivedListener {
        void onJsonReceived(String json);
    }

    public UrlToJsonString(Context context) {
        listener = (onJsonReceivedListener) context;
        //this.context = context;
        // remove
    }

    protected String doInBackground(String... urlString) {
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn;
        InputStreamReader in = null;
        try {
            URL url = new URL(urlString[0]);
            urlConn = url.openConnection();
            if (urlConn != null)
                urlConn.setReadTimeout(60 * 1000);
            if (urlConn != null && urlConn.getInputStream() != null) {
                in = new InputStreamReader(urlConn.getInputStream(),
                                           Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);
                int cp;
                while ((cp = bufferedReader.read()) != -1) {
                    sb.append((char) cp);
                }
                bufferedReader.close();
            }
            if (in != null) {
                in.close();
            }
            else {
                Log.d("json", "var in is null see asynctask");
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Exception while calling URL:" + urlString[0], e);
        }

        String result = sb.toString();
        listener.onJsonReceived(result);
        return result;
    }

    protected void onPostExecute (String result) {
        listener.onJsonReceived(result);
    }

}