package com.example.codyhunsberger.bitcoinclient;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class WalletFragment extends Fragment {

    ArrayList<String> savedWallets;
    ArrayAdapter<String> listAdapter;
    String newWalletAddress, jsonString, address, balance, isUnknown;
    JSONObject jsonObj, data;
    Double balanceDouble;
    DecimalFormat df = new DecimalFormat("#.####");
    private WalletListener listener;

    public interface WalletListener {
        void onEnterWallet(String walletAddress, ArrayList<String> savedWallets);
    }

    public WalletFragment() {
        // Required empty public constructor
    }

    public static WalletFragment newInstance(ArrayList<String> wallets) {
        WalletFragment fragment = new WalletFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("savedWallets", wallets);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WalletListener) {
            listener = (WalletListener) context;
        }
        else {
            throw new ClassCastException(context.toString()
                    + " must implement MyListFragment.OnItemSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wallet, container, false);
        ListView lv = (ListView) v.findViewById(R.id.walletListView);
        df.setRoundingMode(RoundingMode.CEILING);

        final EditText walletAddressEditText = (EditText) v.findViewById(R.id.walletAddressEditText);
        TextView addressTV = (TextView) v.findViewById(R.id.walletAddresstextView);
        TextView balanceTV = (TextView) v.findViewById(R.id.walletBalanceTextView);

        savedWallets = getArguments().getStringArrayList("savedWallets");

        if (savedWallets == null) {
            savedWallets = new ArrayList<>();
        }

        jsonString = getArguments().getString("jsonString");

        if (jsonString != null) {
            try {
                jsonString = getArguments().getString("jsonString");
                jsonObj = new JSONObject(jsonString);
                data = new JSONObject(jsonObj.getString("data"));
                isUnknown = jsonObj.getString("is_unknown");
            } catch (Exception e) {
                Log.d("cException", e.toString());
            }

            if (isUnknown.equals("false")) {
                try {
                    balance = data.getString("balance");
                    balanceDouble = Double.parseDouble(balance);
                    address = data.getString("address");

                    String walletAddressText = getResources().getString(R.string.wallet_address, address);
                    addressTV.setText(walletAddressText);

                    double balanceDouble = 8000.00236957;

                    String walletBalanceText = getResources().getString(R.string.wallet_balance, df.format(balanceDouble));
                    balanceTV.setText(walletBalanceText);

                    // TODO add viewing for other wallet data at some point
                }
                catch (Exception e) {
                    Log.d("cException", e.toString());
                }
            }
            else {
                Toast.makeText(getActivity(), "Wallet address not found, please double check " +
                        "address spelling and try again.", Toast.LENGTH_SHORT).show();
            }
        }

        EditText testEt = (EditText) v.findViewById(R.id.walletTestEditText);

        listAdapter = new ArrayAdapter<>(getActivity(), R.layout.main_list_rows, savedWallets);
        lv.setAdapter(listAdapter);
        lv.deferNotifyDataSetChanged();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String address = savedWallets.get(position);
                Log.d("wallet", "Selected: " + address);
                walletAddressEditText.setText(address);
            }
        });

        Button button = (Button) v.findViewById(R.id.createWalletButton);
        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newWalletAddress = walletAddressEditText.getText().toString();
                if (walletAddressEntryStructuralValidation(newWalletAddress)) {
                    listener.onEnterWallet(newWalletAddress, savedWallets);
                }
                else {
                    Toast.makeText(getActivity(), "Please enter a valid wallet address " +
                            "(length is currently " + newWalletAddress.length() +
                            "). Must be 26-35 alphanumeric characters starting with 1 or 3.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        button.setOnClickListener(ocl);

        return v;
    }

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
    }
    // Wallet addresses are 26-35 alphanumeric characters and begin with 1 or 3

}

// TODO use this for wallet balance: http://btc.blockr.io/api/v1/address/balance/[wallet address]