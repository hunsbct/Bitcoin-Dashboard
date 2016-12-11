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

public class TickerFragment extends Fragment {

    private RefreshListener listener;

    String usdPerBtc = "", timestamp = "", jsonString = "empty";
    JSONObject jsonObj, data, markets, btce;
    float value;
    NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);

    public interface RefreshListener {
        void onButtonPress();
    }

    public TickerFragment() {

    }

    public static TickerFragment newInstance(String json) {
        TickerFragment fragment = new TickerFragment();
        Bundle args = new Bundle();
        args.putString("jsonString", json);
        fragment.setArguments(args);
        return fragment;
    }
    // TODO clear unused imports in all classes

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_ticker, container, false);
        TextView timestampTV = (TextView) v.findViewById(R.id.currentValueTextViewTimestamp);
        TextView valueTV = (TextView) v.findViewById(R.id.currentValueTextViewPrice);

        // At this point jsonString contains json data

        try {
            jsonString = getArguments().getString("jsonString");
            jsonObj = new JSONObject(jsonString);
            data = new JSONObject(jsonObj.getString("data"));
            markets = new JSONObject(data.getString("markets"));
            btce = new JSONObject(markets.getString("btce"));
            timestamp = formatTimestampData(btce.getString("last_update_utc"));
            usdPerBtc = btce.getString("value");
            value = Float.parseFloat(usdPerBtc);
            usdPerBtc = n.format(value);
            Log.d("123btc", usdPerBtc);
            // TODO add daily change data also

        }
        catch (Exception e) {
            Log.d("TickerException", e.toString());
        }

        String timestampText = getResources().getString(R.string.last_updated, timestamp);
        timestampTV.setText(timestampText);
        valueTV.setText(usdPerBtc);
        Button refresh = (Button) v.findViewById(R.id.currentValueRefreshButton);

        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onButtonPress();
            }
        };
        refresh.setOnClickListener(ocl);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (RefreshListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public String formatTimestampData(String timestamp) {
        return timestamp.replaceAll("T", " at ").replaceAll("Z", "");
    }

}

// TODO since API returns value via both BTC-e and Coinbase, add display for both.
