package com.example.codyhunsberger.bitcoinclient;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity implements ListFragment.ListSelectionListener,
        WalletFragment.WalletListener,
        JSONData.onJsonReceivedListener,
        TickerFragment.RefreshListener {
    // class can implement several listeners separated with commas

    boolean twoPanes, walletDataSetChanged;
    String jsonString;
    ArrayList<String> savedWallets = new ArrayList<>();
    JSONData jsonData;
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

    public void onEnterWallet(String walletAddress, ArrayList<String> savedWallets) {
        walletDataSetChanged = false;
        this.savedWallets = savedWallets;
        boolean walletExists = false;
        for(String w : savedWallets) {
            if (w.equals(walletAddress)) {
                walletExists = true;
            }
            break;
        }
        if (!walletExists) {
            savedWallets.add(walletAddress);
            walletDataSetChanged = true;
        }
        Toast.makeText(this, walletAddress + " added = " + !walletExists + "\n Saved Wallets - " +
                savedWallets.size(), Toast.LENGTH_LONG).show();
    }
    // Is called if entered wallet address is not already in listview of saved addresses

    @Override
    public void onButtonPress() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        getTicker();
        if (!twoPanes) {
            fragmentTransaction.replace(R.id.fragmentContainer, TickerFragment.newInstance(jsonString))
                    .addToBackStack(null)
                    .commit();
        }
        else {
            fragmentTransaction.replace(R.id.fragmentContainer2, TickerFragment.newInstance(jsonString))
                    .commit();
        }
    }
    // Still working on implementing this

    public void getAddress(String address) {
        jsonData = new JSONData(this);
        jsonData.setOnJsonReceivedListener(this);
        jsonData.execute("http://btc.blockr.io/api/v1/address/info/" + address);
    }
    // Still yet to get this wired to wallet address viewing fragment

    public void getTicker() {
        jsonData = new JSONData(this);
        jsonData.setOnJsonReceivedListener(this);
        jsonData.execute("http://btc.blockr.io/api/v1/coin/info");
    }
    // Get json string for ticker data

    @Override
    public void onDataReady(String json){
        jsonString = json;
    }

    @Override
    public void onOptionSelected(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (position) {
            case 0:
                getTicker();
                if (!twoPanes) {
                    fragmentTransaction.replace(R.id.fragmentContainer, TickerFragment.newInstance(jsonString))
                        .addToBackStack(null)
                        .commit();
                }
                else {
                    fragmentTransaction.replace(R.id.fragmentContainer2, TickerFragment.newInstance(jsonString))
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