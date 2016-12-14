package com.example.codyhunsberger.bitcoindashboard;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
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

import java.text.DecimalFormat;
import java.util.ArrayList;

public class WalletFragment extends Fragment {
	ArrayList<String> savedWallets = new ArrayList<>();
	ArrayAdapter<String> listAdapter;
	String walletAddress, walletAddressFormatted = "", walletBalanceFormatted = "", jsonString,
			balance = "", isUnknown = "";
	JSONObject jsonObj, data;
	TextView addressTV, balanceTV;
	private WalletListUpdateListener listener;

	public interface WalletListUpdateListener {
		void onEnterWallet(String walletAddress, boolean isAdding);
	}

	public WalletFragment() {

	}

	public static WalletFragment newInstance(ArrayList<String> wallets, String json) {
		WalletFragment fragment = new WalletFragment();
		Bundle args = new Bundle();
		args.putStringArrayList("savedWallets", wallets);
		args.putString("json", json);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_wallet, container, false);
		// True if adding wallet, false if removing
		ListView lv = (ListView) v.findViewById(R.id.walletListView);
		final EditText walletEditText = (EditText) v.findViewById(R.id.walletEditText);
		Button button = (Button) v.findViewById(R.id.createWalletButton);
		addressTV = (TextView) v.findViewById(R.id.walletAddresstextView);
		balanceTV = (TextView) v.findViewById(R.id.walletBalanceTextView);

		savedWallets = getArguments().getStringArrayList("savedWallets");
		jsonString = getArguments().getString("json");

		// If jsonString arg is null
		if(jsonString == null) {
			// If no json and some wallets
			if((savedWallets != null) && (savedWallets.size() > 0)) {
				walletAddress = savedWallets.get(0);
				listener.onEnterWallet(walletAddress, false);
			}
			// If no json and no wallets
		}
		else {
			setVariablesFromJson(jsonString);
			// Sets values of data, isUnknown, balance, balanceDouble, walletAddressFormatted,
			// and walletBalanceFormatted

			addressTV.setText(walletAddressFormatted);
			balanceTV.setText(walletBalanceFormatted);
		}

		listAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_rows, savedWallets);
		lv.setAdapter(listAdapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				hideKeyboard(getActivity(), view);
				walletAddress = savedWallets.get(position);
				walletEditText.setText(walletAddress);
			}
		});

		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long
					id) {
				String walletToRemove = savedWallets.get(position);
				savedWallets.remove(position);
				listener.onEnterWallet(walletToRemove, false);
				return true;
			}
		});


		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				hideKeyboard(getActivity(), view);
				// Hides keyboard on button press
				walletAddress = walletEditText.getText().toString();
				if(walletAddressEntryStructuralValidation(walletAddress)) {
					listener.onEnterWallet(walletAddress, true);
					// Pass true if adding wallet, false to delete from list
				}
				else {
					Toast.makeText(getActivity(), "Please enter a valid wallet address\n" +
										   "Length is currently " + walletAddress.length() + ".\n" +
										   "Must be 26-35 alphanumeric characters starting with 1 or 3.",
								   Toast.LENGTH_SHORT).show();
				}
			}
		});
		return v;
	}

	// Sets values of data, receivedWalletAddress, isUnknown, balance, balanceDouble,
	// walletAddressFormatted, and walletBalanceFormatted
	public void setVariablesFromJson(String jsonString) {
		DecimalFormat df = new DecimalFormat("0.000");
		try {
			jsonObj = new JSONObject(jsonString);
			data = new JSONObject(jsonObj.getString("data"));
			walletAddress = data.getString("address");
			isUnknown = data.getString("is_unknown");
			// The JSON also passes an item called "is_valid", not sure the difference yet

			if(isUnknown.equals("false")) {
				try {
					balance = data.getString("balance");
					Double balanceDouble = Double.parseDouble(balance);

					walletBalanceFormatted = df.format(balance);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				walletAddressFormatted = getResources().getString(R.string.wallet_address,
																   walletAddress);
				walletBalanceFormatted = getResources().getString(R.string.wallet_balance,
																  balance);
			}
			else {
				Toast.makeText(getActivity(), "Wallet address not found by API, please double " +
						"check address spelling and try again.", Toast.LENGTH_SHORT).show();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		listener = (WalletListUpdateListener) context;
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
