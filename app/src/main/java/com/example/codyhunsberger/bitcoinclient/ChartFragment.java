package com.example.codyhunsberger.bitcoinclient;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ChartFragment extends Fragment {
	String chartUrl, chartRefreshTimerText;
	WebView cwv;
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

		return v;
	}

	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// find which radio button is selected
		if(checkedId == R.id.radioButton1d) {
			chartUrl = "https://chart.yahoo.com/z?s=BTCUSD=X&t=1d";
		}
		else if(checkedId == R.id.radioButton5d) {

		}
		else {
			Toast.makeText(getActivity(), "Error in radio button click listener method",
						   Toast.LENGTH_SHORT).show();
		}
	}

	public void onRadioButtonClicked(View view) {
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		switch(view.getId()) {
			case R.id.radioButton1d:
				if(checked) {
					chartUrl = "https://chart.yahoo.com/z?s=BTCUSD=X&t=1d";
				}
				break;
			case R.id.radioButton5d:
				if(checked) {
					chartUrl = "https://chart.yahoo.com/z?s=BTCUSD=X&t=5d";
				}
				break;
		}

		if(!isAirplaneModeOn(getActivity())) {
			loadUrl(chartUrl);
			newTimer();
		}

		else{
			Toast.makeText(getActivity(), "Unable to load, airplane mode is enabled.", Toast
					.LENGTH_SHORT).show();
		}
	}

	public void newTimer() {
		cdt = new CountDownTimer(15000, 1000) {
			public void onFinish() {
				loadUrl(chartUrl);
				newTimer();
			}

			public void onTick(long millisUntilFinished) {
				secondsUntilFinished = safeLongToInt(millisUntilFinished / 1000);
				chartRefreshTimerText = getResources().getString(
						R.string.chart_refresh_timer_text, secondsUntilFinished);
				timerText.setText(chartRefreshTimerText);
			}
		}.start();

		// Reloads the chart every 15s
		// Chart source does not update anywhere close to this frequency so it doesn't do
		// too much
	}

	public void loadUrl(String chartUrl) {
		Toast.makeText(getActivity(), "Loading...", Toast.LENGTH_SHORT).show();
		cwv.loadUrl(chartUrl);
	}

	public int safeLongToInt(long l) {
		if(l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(l +
													   " cannot be cast to int without changing its value.");
		}

		return (int) l;
	}
}

// TODO implement periodic updates (15s)