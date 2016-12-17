package com.example.codyhunsberger.bitcoindashboard;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

public class ListFragment extends Fragment {

    String[] menuOptionsArray;
    ArrayList<String> listOptionsAL;

    private ListSelectionListener listener;

    public ListFragment() {

    }

    public interface ListSelectionListener {
        void onListFragOptionSelected(int position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (ListSelectionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        ListView lv = (ListView) v.findViewById(R.id.listview);

        // Set menuOptionsArray to string-array resource
        menuOptionsArray = getResources().getStringArray(R.array.main_menu_items);
        listOptionsAL = new ArrayList<>();
        listOptionsAL.addAll(Arrays.asList(menuOptionsArray));
        lv.setAdapter(
                new ArrayAdapter<>(getActivity(),
                                   R.layout.list_rows, listOptionsAL)
        );

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onListFragOptionSelected(position);
            }
        });
        return v;
    }

}
