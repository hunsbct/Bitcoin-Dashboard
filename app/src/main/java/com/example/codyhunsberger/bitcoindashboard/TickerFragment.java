package com.example.codyhunsberger.bitcoindashboard;

import android.annotation.SuppressLint;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
                    timestampTV = (TextView) v.findViewById(R.id.footerTv1),
                    refreshedTV = (TextView) v.findViewById(R.id.footerTv2),
                    sourcesTV = (TextView) v.findViewById((R.id.footerTv3)),
                    coinsTotalTV = (TextView) v.findViewById(R.id.coinsTotal);
        Date date = new Date();

        String[] values = getVariablesFromJson(getArguments().getString("json"));

        valueTV.setText(values[0]);
        coinsTotalTV.setText(values[2]);
        timestampTV.setText(getResources().getString(R.string.ticker_footer1,
                                                     formatTimestamp(values[1])));
        refreshedTV.setText(getResources().getString(R.string.ticker_footer2,
                                     formatCurrentDate(date)));
        sourcesTV.setText(getResources().getString(R.string.ticker_footer_sources));

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
        1 - Timestamp unformatted
        2 - Coins mined so far
     */
    public String[] getVariablesFromJson(String jsonString) {
        String[] values = new String[3];
        JSONObject jsonObj, data, volume, markets, btce;
        try {
            jsonObj = new JSONObject(jsonString);
            data = new JSONObject(jsonObj.getString("data"));
            volume = data.getJSONObject("volume");
            markets = new JSONObject(data.getString("markets"));
            btce = new JSONObject(markets.getString("btce"));


            values[0] = NumberFormat.getCurrencyInstance(Locale.US)
                        .format(Float.parseFloat(btce.getString("value")));
            values[1] = btce.getString("last_update_utc");
            values[2] = volume.getInt("current") + " of " + volume.getInt("all") + "(" +
                    volume.getDouble("perc") + "%)";
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

    // API JSON response with UTC timestamp
    @SuppressLint("SimpleDateFormat")
    public String formatTimestamp(String timestamp) {
        String output = "";
        Date date;
        SimpleDateFormat    in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
                            out = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        in.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            date = in.parse(timestamp);
            in.setTimeZone(Calendar.getInstance().getTimeZone());
            output = out.format(date);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }

    @SuppressLint("SimpleDateFormat")
    public String formatCurrentDate(Date date) {
        String output = "";
        SimpleDateFormat    in = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy"),
                            out = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            date = in.parse(date.toString());
            output = out.format(date);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }
}
// todo use exchangerate url, spinner to select currency
// todo expand on ticker layout, add more data starting with number in existence
// todo expand on wallet layout, make readable
