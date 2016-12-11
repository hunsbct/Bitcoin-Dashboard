package com.example.codyhunsberger.bitcoinclient;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

// Add new form for entering wallet?
// FIX everything! ALL OF IT!
public class WalletFragment extends Fragment {

    ArrayList<String> savedWallets;
    ArrayAdapter<String> listAdapter;
    double balanceDouble;
    String walletAddress, walletAddressFormatted = "", walletBalanceFormatted = "", jsonString,
            balance = "", isUnknown = "", receivedWalletAddress;
    JSONObject jsonObj, data;
    DecimalFormat df = new DecimalFormat("#.###");
    // TODO set df to desired final format
    private WalletListener listener;

    public interface WalletListener {
        void onEnterWallet(String walletAddress);
    }

    public WalletFragment() {
        // Required empty public constructor
    }

    public static WalletFragment newInstance(ArrayList<String> wallets) {
        WalletFragment fragment = new WalletFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("savedWallets", wallets);
        args.putString("jsonString", "");
        fragment.setArguments(args);
        return fragment;
    }

    public static WalletFragment newInstance(ArrayList<String> wallets, String json) {
        WalletFragment fragment = new WalletFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("savedWallets", wallets);
        args.putString("jsonString", json);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
/*
        Method outline:
        -Declare variables
        -Get args
        -Check received JSON String for null or empty
            -If length not zero, set variables from JSON
                -Format text
        -Set button ocl
        -Set listView ocl
        -Display text
*/
        View v = inflater.inflate(R.layout.fragment_wallet, container, false);
        ListView lv = (ListView) v.findViewById(R.id.walletListView);
        final EditText walletEditText = (EditText) v.findViewById(R.id.walletEditText);
        Button button = (Button) v.findViewById(R.id.createWalletButton);
        TextView addressTV = (TextView) v.findViewById(R.id.walletAddresstextView);
        TextView balanceTV = (TextView) v.findViewById(R.id.walletBalanceTextView);
        walletAddressFormatted = "";
        walletBalanceFormatted = "";


        savedWallets = getArguments().getStringArrayList("savedWallets");
        jsonString = getArguments().getString("jsonString");
    Log.d("cException-WF-ocv", "jsonString pulled from bundle = " + jsonString);

        if (!isNullOrEmpty(jsonString)) {
            getInfoFromJson(jsonString);
            // Sets values of data, isUnknown, balance, balanceDouble, walletAddressFormatted,
            // and walletBalanceFormatted
        }
        else {
            if (savedWallets.size() > 0) {
                walletAddress = savedWallets.get(0);
                listener.onEnterWallet(walletAddress);
                // TODO check if this is top or bottom of displayed list
            }
    Log.d("cException-WF-ocv", "empty savedWallets list");
        }

        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(getActivity(), view);
                // Hides keyboard on button press
                walletAddress = walletEditText.getText().toString();
    Log.d("cException-WF-ocv-oc", "Button ocl walletAddress set to = " + walletAddress);
                if (walletAddressEntryStructuralValidation(walletAddress)) {
                    listener.onEnterWallet(walletAddress);
                    // TODO work from here to fix communication issues
                }
                else {
                    Toast.makeText(getActivity(), "Please enter a valid wallet address\n" +
                                    "Length is currently " + walletAddress.length() + ".\n" +
                                    "Must be 26-35 alphanumeric characters starting with 1 or 3.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        button.setOnClickListener(ocl);

        listAdapter = new ArrayAdapter<>(getActivity(), R.layout.main_list_rows, savedWallets);
        lv.setAdapter(listAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                hideKeyboard(getActivity(), view);
                walletAddress = savedWallets.get(position);
    Log.d("cException-WF-ocv-oic", "address selected in lv.onItemClickListener: " + walletAddress);
                walletEditText.setText(receivedWalletAddress);
                listener.onEnterWallet(walletAddress);
            }
        });

        if (!isNullOrEmpty(walletBalanceFormatted) && !isNullOrEmpty(walletAddressFormatted)) {
            addressTV.setText(walletAddressFormatted);
            balanceTV.setText(walletBalanceFormatted);
        }
        else {
    Log.d("cException-WF-ocv", "wbFormatted or waFormatted are null or empty");
        }

        return v;
    }

    // Sets values of data, receivedWalletAddress, isUnknown, balance, balanceDouble,
    // walletAddressFormatted, and walletBalanceFormatted
    public void getInfoFromJson(String jsonString) {
        try {
            jsonObj = new JSONObject(jsonString);
            data = new JSONObject(jsonObj.getString("data"));
            receivedWalletAddress = jsonObj.getString("address");
            isUnknown = jsonObj.getString("is_unknown");
            // The JSON also passes an item called "is_valid", not sure the difference yet

    Log.d("cException-WF-gifj", "data = " + data);
    Log.d("cException-WF-gifj", "isUnknown = " + isUnknown);
    Log.d("cException-WF-gifj", "receivedWalletAddress = " + receivedWalletAddress);

            if (isUnknown.equals("false")) {
                try {
                    balance = data.getString("balance");
                    balanceDouble = Double.parseDouble(balance);
                    df.setRoundingMode(RoundingMode.CEILING);

                    walletAddressFormatted = getResources().getString(R.string.wallet_address,
                            receivedWalletAddress);
                    walletBalanceFormatted = getResources().getString(R.string.wallet_balance,
                            df.format(balanceDouble));

    Log.d("cException-WF-gifj", "balance = " + balance);
    Log.d("cException-WF-gifj", "walletAddressFormatted = " + walletAddressFormatted);
    Log.d("cException-WF-gifj", "walletBalanceFormatted = " + walletAddressFormatted);

    Log.d("cExceptionWF", "Wallet is valid");
                    // TODO remove
                }
                catch (Exception e) {
    Log.d("cExceptionWF", e.toString());
                }
            }
            else {
                Toast.makeText(getActivity(), "Wallet address not found by API, please double " +
                        "check address spelling and try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
    Log.d("cException-WF-gifj", e.toString());
        }
    }

    // Returns true only if s is not null and has nonzero length
    public boolean isNullOrEmpty(String s) {
        boolean result;

        if (s == null) {
            Log.d("cException-WF-inoe", "jsonString pulled from bundle in WF is null");
            result = false;
        }
        else if (s.length() < 1) {
            Log.d("cException-WF-inoe", "jsonString pulled from bundle in WF is length 0");
            result = false;
        }
        else {
            Log.d("cException-WF-inoe", "jsonString length is " + jsonString.length());
            result = true;
        }

        return result;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (WalletListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    // Call this to force keyboard to hide
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Wallet address structural validation check
    public boolean walletAddressEntryStructuralValidation(String address) {
        String pattern = "^[a-zA-Z0-9]*$";
        // Regex pattern for letters and numbers only
        return ((address != null) &&
                (address.matches(pattern)) &&
                (address.length() > 25) &&
                (address.length() < 36) && (
                    (address.substring(0,1).equals("1") ||
                    (address.substring(0,1).equals("3"))))
                );
        // Wallet addresses are 26-35 alphanumeric characters and begin with 1 or 3
    }

}

// Wallet balance URL: http://btc.blockr.io/api/v1/address/balance/[wallet address]