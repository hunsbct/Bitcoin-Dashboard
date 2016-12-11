package com.example.codyhunsberger.bitcoinclient;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Spinner;

public class ChartFragment extends Fragment {
    String enteredUrl;

    public static ChartFragment newInstance() {
        return new ChartFragment();
    }

    public ChartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chart, container, false);
        final Spinner spinner = (Spinner) v.findViewById(R.id.chart_spinner);
        final WebView cwv = (WebView) v.findViewById(R.id.chartWebView);
        cwv.getSettings().setUseWideViewPort(true);
        cwv.getSettings().setLoadWithOverviewMode(true);
        Button button = (Button) v.findViewById(R.id.chart_spinner_button);
        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedTime = spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString();

                switch (selectedTime) {
                    case "1 Day":
                        enteredUrl = "https://chart.yahoo.com/z?s=BTCUSD=X&t=1d";
                        break;
                    case "5 Days":
                        enteredUrl = "https://chart.yahoo.com/z?s=BTCUSD=X&t=5d";
                        break;
                    /*
                    case "1 Month":
                        enteredUrl = "https://chart.yahoo.com/z?s=BTCUSD=X&t=1M";
                        break;
                    case "6 Months":
                        enteredUrl = "https://chart.yahoo.com/z?s=BTCUSD=X&t=6M";
                        break;
                    case "1 Year":
                        enteredUrl = "https://chart.yahoo.com/z?s=BTCUSD=X&t=1Y";
                        break;
                    case "2 Years":
                        enteredUrl = "https://chart.yahoo.com/z?s=BTCUSD=X&t=2Y";
                        break;
                        */
                    // TODO add back in if Yahoo ever gets their chart links working
                    default:
                        enteredUrl = "https://chart.yahoo.com/z?s=BTCUSD=X&t=1d";
                        break;
                }

                cwv.loadUrl(enteredUrl);
            }
        };
        button.setOnClickListener(ocl);
        return v;
    }

}

// TODO implement periodic updates (15s)