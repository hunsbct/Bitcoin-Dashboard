package com.example.codyhunsberger.bitcoindashboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

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
	String 	results;
	ArrayList<String> savedWallets = new ArrayList<>();

	@SuppressLint("CommitTransaction")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		twoPanes = findViewById(R.id.fragmentContainerB) != null;

		if(!twoPanes) {
			switchFragments(new ListFragment());
		}
		else {
			switchFragments(TickerFragment.newInstance(getJsonString(getResources().getString(R.string.ticker_api_url))));
			// Only adding the second here, since in twoPane mode the list is static
		}
		// todo replace b with existing fragment not

	}


	@Override
	public void onListFragOptionSelected(int position) {
		// Don't perform frag transaction if it is already in place.
		switch(position) {
			case 0:
				switchFragments(TickerFragment.newInstance(getJsonString(getResources().getString(R.string.ticker_api_url))));
				break;
			case 1:
				switchFragments(new ChartFragment());
				break;
			case 2:
				switchFragments(WalletFragment.newInstance(savedWallets));
				break;
		}
	}

	public void switchFragments(Fragment frag) {
		FragmentManager fragMan = getFragmentManager();
		FragmentTransaction fragTrans = fragMan.beginTransaction();
		if (!twoPanes) {
			fragTrans.replace(R.id.fragmentContainerA, frag).addToBackStack(null).commit();
		}
		else {
			fragTrans.replace(R.id.fragmentContainerB, frag).commit();
		}

	}

	// Boolean indicates whether it was a button press or a long press
	public void onEnterWallet(String walletAddress, boolean calledByButton) {
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

		switchFragments(WalletFragment.newInstance(
							savedWallets,getJsonString(getResources()
						   					.getString(R.string.wallet_api_url, walletAddress))));

	}

	public void onRefresh() {
		switchFragments(TickerFragment.newInstance(getJsonString(getResources().getString(R.string.ticker_api_url))));
	}

	public String getJsonString(String url) {
		results = "";
		UrlToJsonString urlToJsonString = new UrlToJsonString();
		urlToJsonString.execute(url);

		while (urlToJsonString.isLocked()) {
			try {
				Thread.sleep(10);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (results.length() == 0)
		{
			Toast.makeText(this, "Error retrieving data from API.", Toast.LENGTH_SHORT).show();
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

			results = sb.toString();
			locked = false;
			return null;
		}
		// Would have set locked = false in onPostExecute but I can't get it to call that method
	}
}
