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

	public static WalletFragment newInstance(ArrayList<String> wallets) {
		WalletFragment fragment = new WalletFragment();
		Bundle args = new Bundle();
		args.putStringArrayList("savedWallets", wallets);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_wallet, container, false);
		ListView lv = (ListView) v.findViewById(R.id.walletListView);
		final EditText walletEditText = (EditText) v.findViewById(R.id.walletEditText);
		Button button = (Button) v.findViewById(R.id.createWalletButton);
		addressTV = (TextView) v.findViewById(R.id.walletAddresstextView);
		balanceTV = (TextView) v.findViewById(R.id.walletBalanceTextView);

		String json;
		String[] values;

		savedWallets = getArguments().getStringArrayList("savedWallets");
		listAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_rows, savedWallets);
		lv.setAdapter(listAdapter);

		json = getArguments().getString("json");

		// If jsonString arg is null
		if(json != null) {
			values = getVariablesFromJson(json);
			// Sets values of data, isUnknown, balance, balanceDouble, walletAddressFormatted,
			// and walletBalanceFormatted
			addressTV.setText(values[0]);
			balanceTV.setText(values[1]);
		}
		else {
			Toast.makeText(getActivity(), "Select a wallet from the list or enter a new address",
						   Toast.LENGTH_SHORT).show();
		}

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				hideKeyboard(getActivity(), view);
				listener.onEnterWallet(savedWallets.get(position), true);
			}
		});

		// Passes true to listener because it should be added if not in list
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				hideKeyboard(getActivity(), view);
				// Hides keyboard on button press
				String walletAddress = walletEditText.getText().toString();
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

		// Passes false to listener because it should be removed if in list
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

		return v;
	}

	/*
		Array indices as follows:
		0 - Address
		1 - Balance
	 */

	public String[] getVariablesFromJson(String jsonString) {
		String[] values = new String[2];
		JSONObject jsonObj, data;
		DecimalFormat df = new DecimalFormat("#.###");
		try {
			jsonObj = new JSONObject(jsonString);
			data = new JSONObject(jsonObj.getString("data"));
			values[0] = data.getString("address");
			// The JSON also passes an item called "is_valid", not sure the difference yet

			if(data.getString("is_unknown").equals("false")) {
				try {
					values[1] = df.format(Double.parseDouble(data.getString("balance"))) + " BTC";
					Toast.makeText(getActivity(), "Wallet Found", Toast.LENGTH_SHORT).show();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
			else {
				Toast.makeText(getActivity(), "Wallet address not found by API, please double " +
						"check address spelling and try again.", Toast.LENGTH_SHORT).show();
				// todo delete from list if not found
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return values;
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
