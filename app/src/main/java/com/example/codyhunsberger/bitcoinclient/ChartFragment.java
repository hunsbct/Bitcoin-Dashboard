package com.example.codyhunsberger.bitcoinclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ChartFragment extends Fragment {
	String chartUrl, chartRefreshTimerText;
	WebView cwv;
	boolean loadingFinished, redirect;
	int secondsUntilFinished;
	CountDownTimer cdt;
	TextView timerText;

	public ChartFragment() {

	}

	// Detects whether or not Airplane Mode is on
	private static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(),
									  Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_chart, container, false);
		timerText = (TextView) v.findViewById(R.id.chartRefreshTimerTextview);
		RadioGroup rg = (RadioGroup) v.findViewById(R.id.chart_radio_group);
		cwv = (WebView) v.findViewById(R.id.chartWebView);
		cwv.getSettings().setUseWideViewPort(true);
		cwv.getSettings().setLoadWithOverviewMode(true);



		chartUrl = "https://chart.yahoo.com/z?s=BTCUSD=X&t=1d";

		displayUrl(chartUrl);

		rg.setOnCheckedChangeListener(
			new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					switch (checkedId) {
						case(R.id.radioButton1d):
							chartUrl = "https://chart.yahoo.com/z?s=BTCUSD=X&t=1d";
							break;
						case(R.id.radioButton5d):
							chartUrl = "https://chart.yahoo.com/z?s=BTCUSD=X&t=5d";
							break;
						default:
							chartUrl = "https://chart.yahoo.com/z?s=BTCUSD=X&t=1Y";
							Toast.makeText(getActivity(), "Error in RadioGroup onCheckedChanged " +
												   "listener. ID received does not match either button.",
										   Toast.LENGTH_SHORT).show();
						// I'm fairly certain this can't happen but you can't be too sure
					}
					displayUrl(chartUrl);
				}
		});
		return v;
	}

	// Reloads the chart every minute. The prompt suggested 15s but that seems unnecessary.
	// Chart source does not update anywhere close to this frequency so it doesn't do too much, but
	// it's in the requirements.
	public void newTimer() {
		cdt = new CountDownTimer(60000, 1000) {
			public void onFinish() {
				displayUrl(chartUrl);
				start();
			}

			public void onTick(long millisUntilFinished) {
				secondsUntilFinished = safeLongToInt(millisUntilFinished / 1000);
				chartRefreshTimerText = getResources().getString(
						R.string.chart_refresh_timer_text, secondsUntilFinished);
				timerText.setText(chartRefreshTimerText);
				// TODO progress bar for reload?
			}
		}.start();
	}

	// Load a fresh chart URL in the webview
	public void displayUrl(String chartUrl) {
		if(!isAirplaneModeOn(getActivity())) {
			Toast.makeText(getActivity(), "Loading...", Toast.LENGTH_SHORT).show();
			// It's bad design when the Toast outlasts the load time by a factor of 2
			cwv.loadUrl(chartUrl);
			newTimer();
		}
		else{
			Toast.makeText(getActivity(), "Unable to load, airplane mode is enabled.", Toast
					.LENGTH_SHORT).show();
		}

	}

	// Converts the long milliseconds from CountDownTimer to an integer number of seconds
	public int safeLongToInt(long l) {
		if(l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(l +
					   	" cannot be cast to int without changing its value.");
		}

		return (int) l;
	}
}
