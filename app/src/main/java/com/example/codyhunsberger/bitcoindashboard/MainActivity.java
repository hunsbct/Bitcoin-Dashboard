package com.example.codyhunsberger.bitcoindashboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class MainActivity extends Activity implements
		TickerFragment.RefreshListener,
		ListFragment.ListSelectionListener,
		WalletFragment.WalletListUpdateListener {
	// class can implement several listeners separated with commas

	boolean twoPanes;
	String jsonString, tickerJsonString, walletJsonString;
	FragmentManager fm = getFragmentManager();
	FragmentTransaction ft;
	String tickerApiUrl, walletApiUrl;

	ArrayList<String> savedWallets = new ArrayList<>();

	@SuppressLint("CommitTransaction")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tickerApiUrl = getResources().getString(R.string.ticker_api_url);
		walletApiUrl = getResources().getString(R.string.wallet_api_url);

		twoPanes = findViewById(R.id.fragmentContainerB) != null;

		getJsonObject(tickerApiUrl);
		tickerJsonString = jsonString;

		if(!twoPanes) {
			ft = fm.beginTransaction();
			ft.replace(R.id.fragmentContainerA, new ListFragment());
			ft.commit();
		}
		else {
			ft = fm.beginTransaction();
			ft.replace(R.id.fragmentContainerB, TickerFragment.newInstance(tickerJsonString));
			ft.commit();
			// Only adding the second here, since in twoPane mode the list is static
		}
		// todo replace b with existing fragment not

	}


	@Override
	public void onListFragOptionSelected(int position) {
		FragmentTransaction ft = fm.beginTransaction();
		switch(position) {
			case 0:
				if(!twoPanes) {
					ft.replace(R.id.fragmentContainerA,
							   TickerFragment.newInstance(tickerJsonString))
							.addToBackStack(null).commit();
				}
				else {
					ft.replace(R.id.fragmentContainerB, TickerFragment.newInstance(tickerJsonString))
							.commit();
				}
				break;
			case 1:
				if(!twoPanes) {
					ft.replace(R.id.fragmentContainerA, new ChartFragment())
							.addToBackStack(null)
							.commit();
				}
				else {
					ft.replace(R.id.fragmentContainerB, new ChartFragment())
							.commit();
				}
				break;
			case 2:
				if(!twoPanes) {
					ft.replace(R.id.fragmentContainerA, WalletFragment.newInstance
							(savedWallets, walletJsonString)).addToBackStack(null).commit();
				}
				else {
					ft.replace(R.id.fragmentContainerB, WalletFragment.newInstance
							(savedWallets, walletJsonString)).commit();
				}
				break;
		}
	}

	// Boolean indicates whether it was a button press or a long press
	public void onEnterWallet(String walletAddress, boolean calledByButton) {
		String fullWalletApiUrl;
		boolean walletExists = false;

		for(String w : savedWallets) {
			if(w.equals(walletAddress)) {
				walletExists = true;
				break;
			}
		}

		// If wallet was entered
		if(!walletExists && calledByButton) {
			savedWallets.add(walletAddress);
		}
		// If wallet was long-clicked
		else if (walletExists && !calledByButton) {
			savedWallets.remove(savedWallets.indexOf(walletAddress));
		}

		fullWalletApiUrl = getResources().getString(R.string.wallet_api_url, walletAddress);
		getJsonObject(fullWalletApiUrl);
		walletJsonString = jsonString;
		onListFragOptionSelected(2);

	}

	public void onRefresh() {
		getJsonObject(getResources().getString(R.string.ticker_api_url));
		onListFragOptionSelected(0);
	}

	public JSONObject getJsonObject(String url) {
		jsonString = "";
		JSONObject results = null;
		UrlToJsonString urlToJsonString = new UrlToJsonString();
		urlToJsonString.execute(url);

		while (urlToJsonString.isLocked()) {
			try {
				Thread.sleep(10);
			}
			catch (Exception e) {

			}
		}
		if (jsonString.length() > 0)
		{
			try {
				results = new JSONObject(jsonString);
			}
			catch(Exception e) {
				Toast.makeText(this, "Error retrieving data from API.", Toast.LENGTH_SHORT).show();
			}
		}

		return results;
	}

	class UrlToJsonString extends AsyncTask<String, Void, Void> {
		public boolean locked;

		public boolean isLocked() {
			return locked;
		}

		protected void onPreExecute() {
			locked = true;
		}

		protected Void doInBackground(String... urlString) {
			StringBuilder sb = new StringBuilder();
			URLConnection urlConn;
			InputStreamReader in = null;
			try {
				URL url = new URL(urlString[0]);
				urlConn = url.openConnection();
				if(urlConn != null)
					urlConn.setReadTimeout(60 * 1000);
				if(urlConn != null && urlConn.getInputStream() != null) {
					in = new InputStreamReader(urlConn.getInputStream(),
											   Charset.defaultCharset());
					BufferedReader bufferedReader = new BufferedReader(in);
					int cp;
					while((cp = bufferedReader.read()) != -1) {
						sb.append((char) cp);
					}
					bufferedReader.close();
				}
				if(in != null) {
					in.close();
				}
				else {
				}
			}
			catch(Exception e) {
				throw new RuntimeException("Exception while calling URL:" + urlString[0], e);
			}

			String result = sb.toString();
			jsonString = result;
			locked = false;
			return null;
		}
		// Would have set locked = false in onPostExecute but I can't get it to call that method
	}

}

// todo signed apk