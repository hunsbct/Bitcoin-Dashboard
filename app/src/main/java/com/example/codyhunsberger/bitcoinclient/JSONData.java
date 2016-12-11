package com.example.codyhunsberger.bitcoinclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

class JSONData extends AsyncTask<String, Void, String> {
    // TODO rename this class to be more descriptive
    Context context;
    private onJsonReceivedListener listener;

    public void setOnJsonReceivedListener(onJsonReceivedListener listener){
        this.listener = listener;
    }

    public interface onJsonReceivedListener {
        void onDataReady(String json);
    }

    public JSONData (Context context) {
        this.context = context;
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
                Log.d("null", "in is null");
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Exception while calling URL:" + urlString[0], e);
        }

        String result = sb.toString();
        listener.onDataReady(result);
        Log.d("out", result);
        return result;
    }

    //protected void onPostExecute (String result) {
        //listener.onDataReady(result);
    //}

}