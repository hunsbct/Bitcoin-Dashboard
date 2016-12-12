package com.example.codyhunsberger.bitcoinclient;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity implements ListFragment.ListSelectionListener,
        WalletFragment.WalletListener,
        UrlToJsonString.onJsonReceivedListener,
        TickerFragment.RefreshListener {
    // class can implement several listeners separated with commas

    boolean twoPanes;
    String currentJsonString;
    ArrayList<String> savedWallets = new ArrayList<>();
    UrlToJsonString urlToJsonString;
    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction;

        twoPanes = (findViewById(R.id.fragmentContainer2) != null);
        //  Determine how many panes are visible

        fragment = fragmentManager.findFragmentByTag("Container1Frag");
        // Opted to use tag instead of ID here since several different fragments can swap in and
        // out of the same container.
        if (fragment == null) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragmentContainer, new ListFragment(), "Container1Tag");
            fragmentTransaction.commit();
            //  Load this fragment by default
    Log.d("cExceptionF2 - MA- oc", "New listfrag.");
        }
        else {
    Log.d("cExceptionF2 - MA- oc", "Frag1 exists. ID = " + fragment.getId());
        }

        fragment = fragmentManager.findFragmentByTag("Container2Frag");

        if(twoPanes && (fragment == null)) {
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.fragmentContainer2, new TickerFragment(),
                                        "Container2Frag").commit();
    Log.d("cExceptionF2 - MA - oc", "Fragment 2 added");
        }
        else if(!twoPanes && (fragment != null)) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.detach(fragment);
    Log.d("cExceptionF2 - MA - oc", "Fragment 2 removed");
        }
        else {
            if (fragment == null) {
    Log.d("cExceptionF2 - MA - oc", "!twoPanes and fragment 2 is null");
            }
            else {
    Log.d("cExceptionF2 - MA - oc", "Two panes and fragment 2 exists.");
    Log.d("cExceptionF2 - MA - oc", "Fragment 2 = " + fragment.getId());
            }
        }
        // Add second fragment if twoPanes, use TickerFragment by default as per prompt

    }

    // String wallet address →
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
        // TODO remove

        urlToJsonString = new UrlToJsonString(this);
        urlToJsonString.setOnJsonReceivedListener(this);
        fullUrl = "http://btc.blockr.io/api/v1/address/info/" + walletAddress;
        Log.d("cExceptionMA", "fullUrl = " + fullUrl);
        urlToJsonString.execute(fullUrl);

        String toastText;
        if ((sw2 - sw1) > 0) {
            toastText = walletAddress + " WAS added.\nLength is " + savedWallets.size() + ".";
        }
        // TODO keep and modify text
        else {
            toastText = walletAddress + " was NOT added.\nLength went from " + sw1 +
                    "to" + savedWallets.size() + ".";
        }
        // TODO remove


        Log.d("cExceptionMA", "currentJsonString passed to new wallet: " + currentJsonString);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer,
                WalletFragment.newInstance(savedWallets, currentJsonString));
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
    // FIX Still working on implementing this

    public void getTicker() {
        urlToJsonString = new UrlToJsonString(this);
        urlToJsonString.setOnJsonReceivedListener(this);
        urlToJsonString.execute("http://btc.blockr.io/api/v1/coin/info");
    }
    // Get json string for ticker data

    public void onJsonReceived(String json){
        currentJsonString = json;
        Log.d("cExceptionMA", "json string handled in MA listener: " + json);
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

/*

While it probably isn't the most efficient process to replace a fragment instance with a new one for
each Interface call, it's the best I could do with the time I had.

 */

// TODO add hamburger menu?
// TODO find memory leak
// TODO rename to bitcoin dashboard
// TODO add crappy ms paint icon
// TODO change banner to image + bg color
// FIX ListView
// Splash screen?

// NOTE Shift + Command + 8 toggles column/Insert mode