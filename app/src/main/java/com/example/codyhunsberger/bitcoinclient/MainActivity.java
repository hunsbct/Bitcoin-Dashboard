package com.example.codyhunsberger.bitcoinclient;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity implements ListFragment.ListSelectionListener,
        WalletFragment.WalletListener,
        UrlToJsonString.onJsonReceivedListener,
        TickerFragment.RefreshListener {
    // class can implement several listeners separated with commas

    boolean twoPanes, walletDataSetChanged;
    String currentJsonString;
    ArrayList<String> savedWallets = new ArrayList<>();
    UrlToJsonString urlToJsonString;
    // TODO save wallets between runs via sharedpreferences(?)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        twoPanes = (findViewById(R.id.fragmentContainer2) != null);
        //  Determine if only one or two panes are visible

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentContainer, new ListFragment());
        fragmentTransaction.commit();
        //  Load this fragment by default

        if (twoPanes){
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragmentContainer2, new TickerFragment());
            fragmentTransaction.commit();
        }
        // Add second fragment if twoPanes, use TickerFragment by default as per prompt

    }

    public void onEnterWallet(String walletAddress) {
        String fullUrl;
        // TODO remove

        boolean walletExists = false;

        int sw1 = savedWallets.size();
        // TODO remove

        Toast.makeText(this, "onEnterWallet received wallet address: " + walletAddress, Toast.LENGTH_SHORT).show();

        for (String w : savedWallets) {
            if (w.equals(walletAddress)) {
                walletExists = true;
            }
            break;
        }
        if (!walletExists) {
            savedWallets.add(walletAddress);
        }

        int sw2 = savedWallets.size();

        urlToJsonString = new UrlToJsonString(this);
        urlToJsonString.setOnJsonReceivedListener(this);
        fullUrl = "http://btc.blockr.io/api/v1/address/info/" + walletAddress;
        Toast.makeText(this, "fullUrl: " + fullUrl, Toast.LENGTH_SHORT).show();
        urlToJsonString.execute(fullUrl);

        String toastText;
        if ((sw2 - sw1) > 0) {
            toastText = walletAddress + " WAS added.\nLength is " + savedWallets.size() + ".";
        }
        else {
            toastText = walletAddress + " was NOT added.\nLength went from " + sw1 +
                    "to" + savedWallets.size() + ".";
        }
        Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
        // TODO remove


        Toast.makeText(this, "jsonString passed to Wallet.newInstance: " + currentJsonString, Toast.LENGTH_SHORT).show();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer,
                WalletFragment.newInstance(savedWallets, currentJsonString, walletDataSetChanged));
    }

    @Override
    public void onTickerRefreshButtonPress() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        getTicker();
        if (!twoPanes) {
            fragmentTransaction.replace(R.id.fragmentContainer, TickerFragment.newInstance(currentJsonString))
                    .addToBackStack(null)
                    .commit();
        }
        else {
            fragmentTransaction.replace(R.id.fragmentContainer2, TickerFragment.newInstance(currentJsonString))
                    .commit();
        }
    }
    // Still working on implementing this

    public void getTicker() {
        urlToJsonString = new UrlToJsonString(this);
        urlToJsonString.setOnJsonReceivedListener(this);
        urlToJsonString.execute("http://btc.blockr.io/api/v1/coin/info");
    }
    // Get json string for ticker data

    public void onJsonReceived(String json){
        currentJsonString = json;
    }

    @Override
    public void onOptionSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (position) {
            case 0:
                if (!twoPanes) {
                    getTicker();
                    fragmentTransaction.replace(R.id.fragmentContainer, TickerFragment.newInstance(currentJsonString))
                        .addToBackStack(null)
                        .commit();
                }
                else {
                    fragmentTransaction.replace(R.id.fragmentContainer2, TickerFragment.newInstance(currentJsonString))
                        .commit();
                }
                break;
            case 1:
                if (!twoPanes) {
                    fragmentTransaction.replace(R.id.fragmentContainer, new ChartFragment())
                            .addToBackStack(null)
                            .commit();
                }
                else {
                    fragmentTransaction.replace(R.id.fragmentContainer2, new ChartFragment())
                            .commit();
                }
                break;
            case 2:
                if (!twoPanes) {
                    fragmentTransaction.replace(R.id.fragmentContainer, new BlockFragment())
                            .addToBackStack(null)
                            .commit();
                }
                else {
                    fragmentTransaction.replace(R.id.fragmentContainer2, new BlockFragment())
                            .commit();
                }
                break;
            case 3:
                if (!twoPanes) {
                    fragmentTransaction.replace(R.id.fragmentContainer, WalletFragment.newInstance(savedWallets))
                            .addToBackStack(null)
                            .commit();
                }
                else {
                    fragmentTransaction.replace(R.id.fragmentContainer2, WalletFragment.newInstance(savedWallets))
                            .commit();
                }
                break;
            default:
                Toast.makeText(this, "Broken onOptionSelected", Toast.LENGTH_SHORT).show();
        }

    }

}

// TODO add hamburger menu?
// TODO find memory leak
// TODO rename to bitcoin dashboard
// TODO add crappy ms paint icon
// TODO change banner to image + bg color
// FIXME ListView
// Splash screen?