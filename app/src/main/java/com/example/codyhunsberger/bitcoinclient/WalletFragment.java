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
public class WalletFragment extends Fragment {
	ArrayList<String> savedWallets;
	ArrayAdapter<String> listAdapter;
	double balanceDouble;
	String walletAddress, walletAddressFormatted = "", walletBalanceFormatted = "", jsonString,
			balance = "", isUnknown = "", receivedWalletAddress;
	JSONObject jsonObj, data;
	TextView addressTV, balanceTV;
	DecimalFormat df = new DecimalFormat("#.###");
	// todo set df to desired final format
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
        -Check received JSON String and walletList for null or empty
            -If json length not zero, set variables from JSON
                -Format text
			-Else if wallet has 1+ items
				-set edittext to first item
        -Set button ocl
        -Set listView ocl
        -Display text
*/
		View v = inflater.inflate(R.layout.fragment_wallet, container, false);
		ListView lv = (ListView) v.findViewById(R.id.walletListView);
		final EditText walletEditText = (EditText) v.findViewById(R.id.walletEditText);
		Button button = (Button) v.findViewById(R.id.createWalletButton);
		addressTV = (TextView) v.findViewById(R.id.walletAddresstextView);
		balanceTV = (TextView) v.findViewById(R.id.walletBalanceTextView);
		jsonString = "";
		savedWallets = new ArrayList<>();
		walletAddressFormatted = "";
		walletBalanceFormatted = "";

		try {
			savedWallets = getArguments().getStringArrayList("savedWallets");
		}
		catch(Exception e) {
			Log.d("cExceptionWallets", e.toString());
			Toast.makeText(getActivity(), "WF has no wallet list arg yet",
						   Toast.LENGTH_SHORT).show();
		}

		try {
			jsonString = getArguments().getString("jsonString");
		}
		catch(Exception e) {
			Log.d("cExceptionWallets", e.toString());
			Toast.makeText(getActivity(), "WF has no json string arg yet",
						   Toast.LENGTH_SHORT).show();
		}

		Log.d("cException-WF-ocv", "jsonString before being pulled from the bundle in that one " +
				"weird bad try catch mess = " + jsonString);

		// If jsonString arg is null
		if(isNullOrEmpty(jsonString)) {
			// If jsonString is null but there's a wallet we can use as a placeholder
			if((savedWallets != null) && (savedWallets.size() > 0)) {
				walletAddress = savedWallets.get(0);
				walletEditText.setText(walletAddress);
				// todo check if this is top or bottom of displayed list
			}
			else {
				Toast.makeText(getActivity(), "WF has no json or wallet list args yet", Toast
						.LENGTH_SHORT).show();
			}
		}
		else {
			jsonString = getArguments().getString("jsonString");
			Log.d("cException-WF-ocv", "jsonString pulled from bundle = " + jsonString);

			setVariablesFromJson(jsonString);
			// Sets values of data, isUnknown, balance, balanceDouble, walletAddressFormatted,
			// and walletBalanceFormatted
		}

		listAdapter = new ArrayAdapter<>(getActivity(), R.layout.main_list_rows, savedWallets);
		lv.setAdapter(listAdapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				hideKeyboard(getActivity(), view);
				walletAddress = savedWallets.get(position);
				walletEditText.setText(receivedWalletAddress);
			}
		});

		View.OnClickListener ocl = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				hideKeyboard(getActivity(), view);
				// Hides keyboard on button press
				walletAddress = walletEditText.getText().toString();
				Log.d("cException-WF-ocv-oc", "walletAddress set to = " + walletAddress +
						"at button ocl");
				if(walletAddressEntryStructuralValidation(walletAddress)) {
					listener.onEnterWallet(walletAddress);
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

		if(!isNullOrEmpty(walletBalanceFormatted) && !isNullOrEmpty(walletAddressFormatted)) {
			addressTV.setText(walletAddressFormatted);
			balanceTV.setText(walletBalanceFormatted);
		}
		else {
			Log.d("cException-WF-ocv", "wbFormatted or waFormatted are null or empty");
		}

		return v;
	}

	// Update fragment without creating new instance
	public void update(String newJsonString) {
		setVariablesFromJson(newJsonString);
		addressTV.setText(walletAddressFormatted);
		balanceTV.setText(walletBalanceFormatted);
		Toast.makeText(getActivity(), "Wallet update method complete", Toast.LENGTH_SHORT).show();
	}

	// Sets values of data, receivedWalletAddress, isUnknown, balance, balanceDouble,
	// walletAddressFormatted, and walletBalanceFormatted
	public void setVariablesFromJson(String jsonString) {
		try {
			jsonObj = new JSONObject(jsonString);
			data = new JSONObject(jsonObj.getString("data"));
			receivedWalletAddress = jsonObj.getString("address");
			isUnknown = jsonObj.getString("is_unknown");
			// The JSON also passes an item called "is_valid", not sure the difference yet

			Log.d("cException-WF-gifj", "data = " + data);
			Log.d("cException-WF-gifj", "isUnknown = " + isUnknown);
			Log.d("cException-WF-gifj", "receivedWalletAddress = " + receivedWalletAddress);

			if(isUnknown.equals("false")) {
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

					Log.d("cException-WF-gifj", "Wallet is valid");
					// remove
				}
				catch(Exception e) {
					Log.d("cExceptionWF", e.toString());
				}
			}
			else {
				Toast.makeText(getActivity(), "Wallet address not found by API, please double " +
						"check address spelling and try again.", Toast.LENGTH_SHORT).show();
			}
		}
		catch(Exception e) {
			Log.d("cException-WF-gifj", e.toString());
		}
	}

	// Returns true only if s is not null and has nonzero length
	public boolean isNullOrEmpty(String s) {
		boolean result;
		if(s == null) {
			Log.d("cException-WF-inoe", "jsonString pulled from bundle in WF is null");
			result = false;
		}
		else if(s.length() < 1) {
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
				(address.substring(0, 1).equals("1") ||
						(address.substring(0, 1).equals("3"))))
		);
		// Wallet addresses are 26-35 alphanumeric characters and begin with 1 or 3
	}
}

// Wallet balance URL: http://btc.blockr.io/api/v1/address/balance/[wallet address]