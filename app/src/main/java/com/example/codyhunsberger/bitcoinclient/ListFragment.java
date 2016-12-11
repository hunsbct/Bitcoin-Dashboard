package com.example.codyhunsberger.bitcoinclient;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class ListFragment extends Fragment {

    String[] listOptions;
    ArrayList<String> listOptionsAL;
    ArrayAdapter<String> listAdapter;
    private ListSelectionListener listener;

    public ListFragment() {
        // Required empty public constructor
    }

    public interface ListSelectionListener {
        void onOptionSelected(int position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ListSelectionListener) {
            listener = (ListSelectionListener) context;
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

    public static ListFragment newInstance() {
        return new ListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        ListView lv = (ListView) v.findViewById(R.id.listview);
        // Create and populate the list of options
        listOptions = new String[] {
                "Current Exchange Rate (USD to BTC)",
                "Value Charts",
                "View Blocks in Chain",
                "Wallet Address Utility"
        };
        // In the future I will streamline the wallet viewing/address adding, but for the sake of
        // time they are currently two main menu items
        listOptionsAL = new ArrayList<>();
        listOptionsAL.addAll(Arrays.asList(listOptions));
        listAdapter = new ArrayAdapter<>(getActivity(), R.layout.main_list_rows, listOptionsAL);

        lv.setAdapter(listAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                listener.onOptionSelected(position);
            }
        });

        return v;
    }

}
