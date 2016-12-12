package com.example.codyhunsberger.bitcoinclient;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class TickerFragment extends Fragment implements UrlToJsonString.onJsonReceivedListener{

    float value;
    String jsonString;
    String[] attributes;
    // Contains timestamp at index 0, lastDownloadedText at 1, and usdPerBtc at 2.
    NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
    JSONObject jsonObj, data, markets, btce;
    TextView timestampTV, valueTV, lastDownloadTV;

    public TickerFragment() {

    }

    public static TickerFragment newInstance() {
        TickerFragment tf = new TickerFragment();
        tf.updateJsonString();
        return tf;
    }

    // note Can newInstance be phased out via update methods?
    // todo clear unused imports in all classes

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_ticker, container, false);
        final String usdPerBtc = "",
                timestamp = "",
                timestampText,
                jsonString,
                lastDownloadedText,
                apiUrl = getResources().getString(R.string.ticker_api_url);

        valueTV = (TextView) v.findViewById(R.id.currentValueTextViewPrice);
        timestampTV = (TextView) v.findViewById(R.id.currentValueTextViewTimestamp);
        lastDownloadTV = (TextView) v.findViewById(R.id.lastDownloadedTimestamp);
        Log.d("moneyshot", "Step 1. url = " + apiUrl);

        try {
            jsonString = getArguments().getString("jsonString");
            Log.d("cException", "tickerfragment args received: jsonstring = " + jsonString);
        }
        catch(Exception e) {
            Log.d("cException", e.toString());
            // fix This is being thrown
        }

        timestampText = getResources().getString(R.string.last_updated, timestamp);
        lastDownloadedText = getResources().getString(R.string.last_download, Calendar.getInstance());
        timestampTV.setText(timestampText);
        valueTV.setText(usdPerBtc);
        lastDownloadTV.setText(lastDownloadedText);
        Log.d("moneyshot", "Step 8: see it for yourself");

        Button refresh = (Button) v.findViewById(R.id.currentValueRefreshButton);
        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("moneyshot", "Step 9: starting over");
                updateJsonString();
                // listener.onTickerRefreshButtonPress();
                // remove
            }
        };
        refresh.setOnClickListener(ocl);
        return v;
    }

    // Listener that assigns result of UrlToJsonString call to local var jsonString
    @Override
    public void onJsonReceived(String json) {
        jsonString = json;
        Log.d("moneyshot", "Steps 4 & 5. jsonString pulled from url = " + jsonString);
    }

    public String[] updateJsonString() {
        Log.d("moneyshot", "Step 2. and away she goes");
        UrlToJsonString urlToJsonString;
        urlToJsonString = new UrlToJsonString(getActivity());
        urlToJsonString.execute(getResources().getString(R.string.ticker_api_url));

        attributes = new String[3];

        Log.d("moneyshot", "Step 6. jsonString sent to setVariablesFromJson = " + jsonString);
        try {
            jsonObj = new JSONObject(jsonString);
            data = new JSONObject(jsonObj.getString("data"));
            markets = new JSONObject(data.getString("markets"));
            btce = new JSONObject(markets.getString("btce"));
            attributes[0] = formatTimestampData(btce.getString("last_update_utc"));
            attributes[1] = Calendar.getInstance().toString();
            attributes[2] = n.format(Float.parseFloat(btce.getString("value")));
        }
        catch (Exception e) {
            Log.d("moneyshot", e.toString());
        }
        Log.d("moneyshot",
              "Step 7. home stretch" +
                      "\ntimestamp = " + attributes[0] +
                      "\nusdPerBtc = " + attributes[2] +
                      "\nvalue = " + value
        );
        return attributes;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public String formatTimestampData(String timestamp) {
        return timestamp.replaceAll("T", " at ").replaceAll("Z", "");
    }

}

/*
// note this should work and be implemented across all fragments
1. URL is procured
2. URL -> update()
3. update() -> UrlToJsonString.class
4. UrlToJsonString.class -[onJsonReceived]-> Fragment interface
5. Fragment interface sets local jsonString variable
6. jsonString variable -> setVariablesFromJson
7. setVariablesFromJson parses text and sets variables
    7a. Use Logs here mostly
    7b. At this point data is passively checked, its work is done
8. Fragment checks local assigned variables and applies them to UI
9. Refresh Button fired
10. Update method is called
    10a. ocl -[blank listener]-> main activity onRefreshClickedListener
  ->10b. ocl calls class update method
11. Update method fires step 2
 */
