package com.example.codyhunsberger.bitcoindashboard;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ChartFragment extends Fragment {
	String chartUrl1d, chartUrl5d;
	int secondsUntilFinished;
	Timer timer;
	TextView timerText;
	WebView cwv;

	public ChartFragment() {

	}

	private static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(),
									  Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_chart, container, false);
		chartUrl1d = getResources().getString(R.string.chart_url_1d);
		chartUrl5d = getResources().getString(R.string.chart_url_5d);

		timerText = (TextView) v.findViewById(R.id.chartRefreshTimerTextview);
		timerText.setText("");
		RadioGroup rg = (RadioGroup) v.findViewById(R.id.chart_radio_group);
		cwv = (WebView) v.findViewById(R.id.chartWebView);

		cwv.getSettings().setUseWideViewPort(true);
		cwv.getSettings().setLoadWithOverviewMode(true);

		displayUrl(chartUrl1d);

		rg.setOnCheckedChangeListener(
			new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					switch (checkedId) {
						case(R.id.radioButton1d):
							chartReloader(chartUrl1d);
							break;
						case(R.id.radioButton5d):
							chartReloader(chartUrl5d);
							break;
					}
				}
		});
		return v;
	}

	public void chartReloader(String url) {
		displayUrl(url);
		timer = new Timer(60000, 1000, url);
		timer.start();
		Log.d("timer", "chart reloader finish");
	}

	// Reloads the chart every minute. The prompt suggested 15s but that seems unnecessary.
	// Chart source does not update anywhere close to this frequency so it doesn't do too much, but
	// it's in the requirements.
	class Timer extends CountDownTimer {
		String url, chartTimerText;
		public Timer(long millisInFuture, long countDownInterval, String url) {
			super(millisInFuture, countDownInterval);
			this.url = url;
		}

		public void onFinish() {
			chartReloader(url);
			Log.d("timer", "chart reloader call");
		}

		public void onTick(long millisUntilFinished) {
			secondsUntilFinished = safeLongToInt(millisUntilFinished / 1000);
			chartTimerText = getResources().getString(
					R.string.chart_timer_text, secondsUntilFinished);
			timerText.setText(chartTimerText);
		}
	}

	// Load a current chart URL in the webview
	public void displayUrl(String chartUrlBase) {
		if(!isAirplaneModeOn(getActivity())) {
			Toast.makeText(getActivity(), "Loading...", Toast.LENGTH_SHORT).show();
			cwv.loadUrl(chartUrlBase);
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


// note: Yahoo chart resource pulls 512x288px PNG
