package com.example.codyhunsberger.bitcoindashboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends Activity implements
		ListFragment.ListSelectionListener,
		WalletFragment.WalletListUpdateListener,
		UrlToJsonString.onJsonReceivedListener{
	// class can implement several listeners separated with commas

	boolean twoPanes;
	String jsonString;
	FragmentManager fm;
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
		fm = getFragmentManager();

		if (savedInstanceState == null) {
			if(!twoPanes) {
				ft = fm.beginTransaction();
				ft.add(R.id.fragmentContainerA, new ListFragment());
				ft.commit();
				Toast.makeText(this, "new listfrag added", Toast.LENGTH_SHORT).show();
			}
			else {
				ft = fm.beginTransaction();
				ft.add(R.id.fragmentContainerB, new TickerFragment());
				ft.commit();
				// Only adding the second here, since in twoPane mode the list is static
			}
			// todo replace b with existing fragment not default ticker
		}
	}


	@Override
	public void onListFragOptionSelected(int position) {
		FragmentTransaction ft = fm.beginTransaction();
		switch(position) {
			case 0:
				if(!twoPanes) {
					ft.replace(R.id.fragmentContainerA,
												new TickerFragment()).addToBackStack(null).commit();
				}
				else {
					ft.replace(R.id.fragmentContainerB,
												new TickerFragment()).commit();
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
					ft.replace(R.id.fragmentContainerA, new BlockFragment())
							.addToBackStack(null)
							.commit();
				}
				else {
					ft.replace(R.id.fragmentContainerB, new BlockFragment())
							.commit();
				}
				break;
			case 3:
				if(!twoPanes) {
					ft.replace(R.id.fragmentContainerA, WalletFragment.newInstance(savedWallets))
							.addToBackStack(null)
							.commit();
				}
				else {
					ft.replace(R.id.fragmentContainerB, WalletFragment.newInstance(savedWallets))
							.commit();
				}
				break;
			default:
				Toast.makeText(this, "Broken onListFragOptionSelected", Toast.LENGTH_SHORT).show();
		}
	}

	// Boolean indicates whether it was a button press or a long press
	public void onEnterWallet(String walletAddress, boolean calledByButton) {
		String fullWalletApiUrl;
		WalletFragment wf;
		boolean walletExists = false;

		for(String w : savedWallets) {
			if(w.equals(walletAddress)) {
				walletExists = true;
				break;
			}
		}
		if(!walletExists && calledByButton) {
			savedWallets.add(walletAddress);
		}
		else if (walletExists && !calledByButton) {
			savedWallets.remove(savedWallets.indexOf(walletAddress));
		}

		fullWalletApiUrl = getResources().getString(R.string.wallet_api_url, walletAddress);
		urlToJsonString(fullWalletApiUrl);

	}

	public void urlToJsonString(String url) {
		if (URLUtil.isValidUrl(url)) {
			UrlToJsonString urlToJsonString = new UrlToJsonString(this);
			urlToJsonString.execute(url);
		}
		else {
			Log.d("json", "URL passed to urlToJsonString is invalid.");
		}
	}

	public void onJsonReceived(String json) {
		Log.d("json", "json received from async listener = " + json);
		jsonString = json;

	}
}

// todo rename to bitcoin dashboard

// note Shift + Command + 8 toggles column/Insert mode