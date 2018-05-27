package com.hqs.alx.mushalmapp2;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.MyUser;
import com.hqs.alx.mushalmapp2.recyclerAdapters.MyEmployeesListRecyclerAdapter;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmployeesListFragment extends Fragment {

    private RecyclerView employeesListRecyclerView;
    private MyEmployeesListRecyclerAdapter adapter;

    //FireBase:
    private DatabaseReference all_employees_ref;

    public EmployeesListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_employees_list, container, false);

        employeesListRecyclerView = (RecyclerView) view.findViewById(R.id.allEmployeesList);

        all_employees_ref = FirebaseDatabase.getInstance().getReference();

        //reference to all users of the current work place
        all_employees_ref = all_employees_ref.child(FireBaseConstants.ALL_APP_WORKPLACES).child(WorkPlaceFragment.currentWorkPlace.getWorkCode())
                                                                    .child(WorkPlaceFragment.currentWorkPlace.getWorkName())
                                                                    .child(FireBaseConstants.CHILD_USERS);
        final ArrayList<String> keyValues = new ArrayList<String>();

        all_employees_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(keyValues.size() != 0){
                    keyValues.clear();
                }

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    String key = snapshot.getKey();
                    keyValues.add(key);
                }
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(),
                        DividerItemDecoration.VERTICAL);
                employeesListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                employeesListRecyclerView.addItemDecoration(dividerItemDecoration);
                MyEmployeesListRecyclerAdapter adapter = new MyEmployeesListRecyclerAdapter(getActivity(), keyValues);
                employeesListRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("DatabaseError : ", "" + databaseError.getMessage().toString());
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(WorkPlaceFragment.currentWorkPlace.getWorkName());
    }
}
