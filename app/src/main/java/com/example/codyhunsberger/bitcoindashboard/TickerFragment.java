package com.example.codyhunsberger.bitcoindashboard;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
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
    float value;
    String jsonString = null, timestamp, timestampText, valueFormatted;
    NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
    JSONObject jsonObj = null, data, markets, btce;
    TextView timestampTV, valueTV;

    public TickerFragment() {

    }

    public static TickerFragment newInstance(String json) {
        TickerFragment fragment = new TickerFragment();
        Bundle args = new Bundle();
        args.putString("json", json);
        fragment.setArguments(args);
        return fragment;
    }

    public interface RefreshListener {
        void onRefresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_ticker, container, false);

        valueTV = (TextView) v.findViewById(R.id.currentValueTextViewPrice);
        timestampTV = (TextView) v.findViewById(R.id.currentValueTextViewSources);

        jsonString = getArguments().getString("json");

        setTickerVariablesFromJson(jsonString);

        Button refresh = (Button) v.findViewById(R.id.currentValueRefreshButton);
        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRefresh();
            }
        };
        refresh.setOnClickListener(ocl);
        return v;
    }

    public void setTickerVariablesFromJson(String jsonString) {
        try {
            jsonObj = new JSONObject(jsonString);
            data = new JSONObject(jsonObj.getString("data"));
            markets = new JSONObject(data.getString("markets"));
            btce = new JSONObject(markets.getString("btce"));

            value = Float.parseFloat(btce.getString("value"));
            valueFormatted = n.format(value);
            valueTV.setText(valueFormatted);

            timestamp = formatTimestampData(btce.getString("last_update_utc"));
            timestampText = getResources().getString(R.string.ticker_footer, timestamp);
            timestampTV.setText(timestampText);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

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
