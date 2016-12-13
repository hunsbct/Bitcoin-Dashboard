package com.example.codyhunsberger.bitcoinclient;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity implements
		ListFragment.ListSelectionListener,
		WalletFragment.WalletListUpdateListener,
		UrlToJsonString.onJsonReceivedListener {
	// class can implement several listeners separated with commas

	boolean twoPanes, twoFrags;
	String currentJsonString;
	ArrayList<String> savedWallets = new ArrayList<>();
	FragmentManager fragmentManager = getFragmentManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		FragmentTransaction fragmentTransaction;
		twoPanes = findViewById(R.id.fragmentContainerB) != null;
		twoFrags = fragmentManager.findFragmentByTag("frag2") != null;
		// Determine how many panes are visible
		// Added fragment check for memory leak hunting purposes

		if(!twoPanes) {
			// One pane
			fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.replace(R.id.fragmentContainerA, new ListFragment(), "frag1");
			fragmentTransaction.commit();
			//  Load this fragment by default
			Toast.makeText(this, "Single pane fragment added.", Toast.LENGTH_SHORT).show();
		}
		else {
			fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.replace(R.id.fragmentContainerA, new ListFragment(), "frag1");
			fragmentTransaction.replace(R.id.fragmentContainerB, new TickerFragment(), "frag2");
			fragmentTransaction.commit();
			//  Load two default side by side fragments
			Toast.makeText(this, "Two panes detected, second fragment added.", Toast.LENGTH_SHORT)
					.show();
		}

	}

	// Listener fired when valid wallet address is sent from WalletFragment
	public void onEnterWallet(String walletAddress) {
		String fullWalletApiUrl;
		WalletFragment wf;
		boolean walletExists = false;

		d("onEnterWallet received wallet address: " + walletAddress);

		for(String w : savedWallets) {
			if(w.equals(walletAddress)) {
				walletExists = true;
			}
			break;
		}
		if(!walletExists) {
			savedWallets.add(walletAddress);
		}

		fullWalletApiUrl = getResources().getString(R.string.wallet_api_url, walletAddress);
		d("fullWalletApiUrl = " + fullWalletApiUrl);
		//getTickerStringFromJson(fullWalletApiUrl);
		d("currentJsonString passed to new wallet: " + currentJsonString);
		wf = (WalletFragment) fragmentManager.findFragmentById(R.id.fragment_wallet);

		if (wf != null) {
			Log.d("cExceptionMAoew", "string passed to wallet update method = " +
					currentJsonString);
			wf.update(currentJsonString);
		}
		else {
			Toast.makeText(this, "wf is somehow null in its own triggered event listener", Toast
					.LENGTH_SHORT).show();
		}
	}

	// add method to decide if it's two panes or not rather than a trillion if/elses

	public void onJsonReceived(String json) {
		currentJsonString = json;
		d("json passed to onJsonReceived MainActivity method = " + currentJsonString);
	}

	@Override
	public void onListFragOptionSelected(int position) {
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		switch(position) {
			case 0:
				if(!twoPanes) {
					fragmentTransaction.replace(R.id.fragmentContainerA,
							new TickerFragment()).addToBackStack(null)
							.commit();
				}
				else {
					fragmentTransaction.replace(R.id.fragmentContainerB,
												new TickerFragment()).commit();
				}

				break;
			case 1:
				if(!twoPanes) {
					fragmentTransaction.replace(R.id.fragmentContainerA, new ChartFragment())
							.addToBackStack(null)
							.commit();
				}
				else {
					fragmentTransaction.replace(R.id.fragmentContainerB, new ChartFragment())
							.commit();
				}
				break;
			case 2:
				if(!twoPanes) {
					fragmentTransaction.replace(R.id.fragmentContainerA, new BlockFragment())
							.addToBackStack(null)
							.commit();
				}
				else {
					fragmentTransaction.replace(R.id.fragmentContainerB, new BlockFragment())
							.commit();
				}
				break;
			case 3:
				if(!twoPanes) {
					fragmentTransaction.replace(R.id.fragmentContainerA, WalletFragment.newInstance(savedWallets))
							.addToBackStack(null)
							.commit();
				}
				else {
					fragmentTransaction.replace(R.id.fragmentContainerB, WalletFragment.newInstance(savedWallets))
							.commit();
				}
				break;
			default:
				Toast.makeText(this, "Broken onListFragOptionSelected", Toast.LENGTH_SHORT).show();
		}
	}

	// Reduce Log.d clutter
	public void d (String msg) {
		Log.d("cExceptionMA", msg);
	}
	// remove
}

// todo add hamburger menu?
// todo find memory leak
// todo rename to bitcoin dashboard
// todo reuse fragments
// fix ListView

// note Shift + Command + 8 toggles column/Insert mode