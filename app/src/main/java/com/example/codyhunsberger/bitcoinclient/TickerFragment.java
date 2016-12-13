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
import java.util.Locale;

public class TickerFragment extends Fragment implements UrlToJsonString.onJsonReceivedListener{

    float value;
    String jsonString = "", timestamp, timestampText, valueFormatted;
    NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
    JSONObject jsonObj = null, data, markets, btce;
    TextView timestampTV, valueTV;

    public TickerFragment() {

    }

    // todo clear unused imports in all classes

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_ticker, container, false);
        String apiUrl = getResources().getString(R.string.ticker_api_url);

        valueTV = (TextView) v.findViewById(R.id.currentValueTextViewPrice);
        timestampTV = (TextView) v.findViewById(R.id.currentValueTextViewSources);
        Log.d("pathtrace", "Step 1. url = " + apiUrl);

        Log.d("pathtrace", "Step 1a. jsonstring before null check = " + jsonString);
        if (jsonObj == null) {
            updateJsonString();
        }
        Log.d("pathtrace", "Step 1b. jsonstring after null check " + jsonString);

        Log.d("pathtrace", "Step 1c. timestamp = " + timestamp);
        Log.d("pathtrace", "Step 1c. valueFormatted = " + valueFormatted);
        timestampText = getResources().getString(R.string.ticker_footer, timestamp);
        Log.d("pathtrace", "Step 1c. timestampText = " + timestampText);
        timestampTV.setText(timestampText);
        valueTV.setText(valueFormatted);
        Log.d("pathtrace", "Step 1d: ?????");

        Button refresh = (Button) v.findViewById(R.id.currentValueRefreshButton);
        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateJsonString();
                timestampText = getResources().getString(R.string.ticker_footer, timestamp);
                timestampTV.setText(timestampText);
                valueTV.setText(valueFormatted);
            }
        };
        refresh.setOnClickListener(ocl);
        return v;
    }

    // Listener that assigns result of UrlToJsonString call to local var jsonString
    @Override
    public void onJsonReceived(String json) {
        jsonString = json;
        Log.d("pathtrace", "Step 4. jsonString got by receiving listener= " +
                jsonString);
    }

    public void updateJsonString() {
        Log.d("pathtrace", "Step 2. upDateJson fired");
        Log.d("pathtrace", "Step 2a. jsonString before call to urltojsonstring = " + jsonString);
        UrlToJsonString urlToJsonString;
        urlToJsonString = new UrlToJsonString(getActivity());
        urlToJsonString.execute(getResources().getString(R.string.ticker_api_url));
        Log.d("pathtrace", "Step 2b. jsonString after call to urltojsonstring = " + jsonString);

        try {
            jsonObj = new JSONObject(jsonString);
            data = new JSONObject(jsonObj.getString("data"));
            markets = new JSONObject(data.getString("markets"));
            btce = new JSONObject(markets.getString("btce"));
            timestamp = formatTimestampData(btce.getString("last_update_utc"));
            value = Float.parseFloat(btce.getString("value"));
            valueFormatted = n.format(value);
            Log.d("pathtrace", "Step 2 complete");
        }
        catch (Exception e) {
            Log.d("pathtrace", "Step 2 exception thrown: " + e.toString());
        }
        Log.d("pathtrace",
              "Step 3. variables set:" +
                      "\ntimestamp = " + timestamp +
                      "\nvalueFormatted = " + valueFormatted +
                      "\nvalue = " + value
        );
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

    // Returns true only if s is not null and has nonzero length
    public boolean isNullOrEmpty(String s) {
        boolean result;
        if(s == null) {
            Log.d("cException-WF-inoe", "jsonString pulled from bundle in WF is null");
            result = false;
        }
        else if(s.length() < 1) {
            Log.d("cException-WF-inoe", "jsonString pulled from bundle in WF is length 0");
            result = false;
        }
        else {
            Log.d("cException-WF-inoe", "jsonString length is " + jsonString.length());
            result = true;
        }
        return result;
    }
    // remove if unused

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
