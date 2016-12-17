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
        TextView    valueTV = (TextView) v.findViewById(R.id.currentValueTextViewPrice),
                    timestampTV = (TextView) v.findViewById(R.id.currentValueTextViewSources);

        String[] values = getVariablesFromJson(getArguments().getString("json"));

        valueTV.setText(values[0]);
        timestampTV.setText(values[1]);

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

    /*
        Array indices as follows:
        0 - Value formatted
        1 - Timestamp formatted
     */
    public String[] getVariablesFromJson(String jsonString) {
        String[] values = new String[2];
        JSONObject jsonObj, data, markets, btce;
        try {
            jsonObj = new JSONObject(jsonString);
            data = new JSONObject(jsonObj.getString("data"));
            markets = new JSONObject(data.getString("markets"));
            btce = new JSONObject(markets.getString("btce"));


            values[0] = NumberFormat.getCurrencyInstance(Locale.US)
                        .format(Float.parseFloat(btce.getString("value")));
            values[1] = getResources().getString(
                    R.string.ticker_footer, formatTimestampData(btce.getString("last_update_utc")));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return values;
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
// todo use exchangerate url, spinner to select currency
