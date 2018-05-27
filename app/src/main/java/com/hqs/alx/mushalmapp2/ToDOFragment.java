package com.hqs.alx.mushalmapp2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.Fragment;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dx.dxloadingbutton.lib.LoadingButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.MyUser;
import com.hqs.alx.mushalmapp2.data.ToDoItem;
import com.hqs.alx.mushalmapp2.data.WorkPlaces;
import com.hqs.alx.mushalmapp2.recyclerAdapters.MyToDOListAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class ToDOFragment extends Fragment{

    public MyUser currentUser;
    private DatabaseReference work_reference;
    ChildEventListener todoListener;

    private RecyclerView importantRecycler, regularRecycler;
    private MyToDOListAdapter importantAdapter, regularAdapter;
    private ArrayList<ToDoItem> importantItems, regularItems;
    private AlertDialog alertDialogNewItem;
    String color;
    boolean isImportant = false;

    public ToDOFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_to_do, container, false);

        importantRecycler = (RecyclerView) view.findViewById(R.id.importantRecycler);
        regularRecycler = (RecyclerView) view.findViewById(R.id.regularRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        importantRecycler.setLayoutManager(layoutManager);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());
        layoutManager2.setReverseLayout(true);
        layoutManager2.setStackFromEnd(true);
        regularRecycler.setLayoutManager(layoutManager2);
        importantItems = new ArrayList<ToDoItem>();
        regularItems = new ArrayList<ToDoItem>();

        importantAdapter = new MyToDOListAdapter(getActivity(), importantItems, currentUser);
        regularAdapter = new MyToDOListAdapter(getActivity(), regularItems, currentUser);

        importantRecycler.setAdapter(importantAdapter);
        regularRecycler.setAdapter(regularAdapter);

        work_reference = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.ALL_APP_WORKPLACES).child(WorkPlaceFragment.currentWorkPlace.getWorkCode())
                .child(WorkPlaceFragment.currentWorkPlace.getWorkName()).child(FireBaseConstants.CHILD_TODO_ITEMS);

        (view.findViewById(R.id.addTodoItemBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemCreatorDialog();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).setActionBarTitle(WorkPlaceFragment.currentWorkPlace.getWorkName());

        if (importantItems != null || regularItems != null ) {
            importantItems.clear();
            regularItems.clear();
        }

        if(todoListener == null){
            todoListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ToDoItem item = dataSnapshot.getValue(ToDoItem.class);
                    if(item.isImportant()){
                        if(item.isComplete()){
                            importantItems.add(0, item);
                        }else{
                            importantItems.add(item);
                        }
                        importantAdapter.notifyDataSetChanged();
                        int position = importantRecycler.getAdapter().getItemCount()-1;
                        importantRecycler.smoothScrollToPosition(position);
                    }else {
                        if(item.isComplete()){
                            regularItems.add(0, item);
                        }else{
                            regularItems.add(item);
                        }
                        regularAdapter.notifyDataSetChanged();
                        int position = regularRecycler.getAdapter().getItemCount()-1;
                        regularRecycler.smoothScrollToPosition(position);
                    }

                    if(importantItems.size() == 0){
                        importantRecycler.setVisibility(View.GONE);
                    }else{
                        importantRecycler.setVisibility(View.VISIBLE);
                    }

                    if(regularItems.size() == 0){
                        regularRecycler.setVisibility(View.GONE);
                    }else{
                        regularRecycler.setVisibility(View.VISIBLE);
                    }

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }

        work_reference.addChildEventListener(todoListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        work_reference.removeEventListener(todoListener);
    }

    private void itemCreatorDialog(){

        AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(getActivity());
        final View popUpCreatorView = LayoutInflater.from(getActivity()).inflate(R.layout.todo_item_creator, null);

        final TextInputEditText itemName = (TextInputEditText) popUpCreatorView.findViewById(R.id.todoItemNameET);
        final TextInputEditText itemDescription = (TextInputEditText) popUpCreatorView.findViewById(R.id.todoItemDescriptionET);
        final ImageView starIV = (ImageView) popUpCreatorView.findViewById(R.id.todoItemStarBorderIV);
        final LoadingButton createTodoItemBtn = (LoadingButton) popUpCreatorView.findViewById(R.id.createToDoItemBtn);
        final LinearLayout itemCreatorContainer = (LinearLayout) popUpCreatorView.findViewById(R.id.itemCreatorContainer);
        final LinearLayout colorsLayout = (LinearLayout) popUpCreatorView.findViewById(R.id.colorsLayout);

        color = getActivity().getString(R.string.itemWhiteString);

        View.OnClickListener ppp = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.itemBlueColor:
                        color = getActivity().getString(R.string.itemBlueString);
                        itemCreatorContainer.setBackgroundColor(getActivity().getResources().getColor(R.color.ItemBlue));
                        break;
                    case R.id.itemRedColor:
                        color = getActivity().getString(R.string.itemRedString);
                        itemCreatorContainer.setBackgroundColor(getActivity().getResources().getColor(R.color.itemRed));
                        break;
                    case R.id.itemWhiteColor:
                        color = getActivity().getString(R.string.itemWhiteString);
                        itemCreatorContainer.setBackgroundColor(getActivity().getResources().getColor(R.color.itemWhite));
                        break;
                    case R.id.itemGreenColor:
                        color = getActivity().getString(R.string.itemGreenString);
                        itemCreatorContainer.setBackgroundColor(getActivity().getResources().getColor(R.color.itemGreen));
                        break;
                    case R.id.itemPurpleColor:
                        color = getActivity().getString(R.string.itemPurpleString);
                        itemCreatorContainer.setBackgroundColor(getActivity().getResources().getColor(R.color.itemPurple));
                        break;
                    case R.id.itemGreyColor:
                        color = getActivity().getString(R.string.itemGreyString);
                        itemCreatorContainer.setBackgroundColor(getActivity().getResources().getColor(R.color.itemGrey));
                        break;
                    case R.id.itemOrangeColor:
                        color = getActivity().getString(R.string.itemOrangeString);
                        itemCreatorContainer.setBackgroundColor(getActivity().getResources().getColor(R.color.itemOrange));
                        break;
                    default:
                        color = getActivity().getString(R.string.itemWhiteString);
                }
            }
        };

        for (int i = 0; i < colorsLayout.getChildCount(); i++) {
            final ImageButton button = (ImageButton) colorsLayout.getChildAt(i);
            button.setOnClickListener(ppp);
        }

        starIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isImportant){
                    isImportant = true;
                    starIV.setImageResource(R.drawable.ic_star_24dp);
                }else{
                    isImportant = false;
                    starIV.setImageResource(R.drawable.ic_star_border_24dp);
                }
            }
        });

        createTodoItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTodoItemBtn.startLoading();
                if(itemName.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "EROR", Toast.LENGTH_SHORT).show();
                    createTodoItemBtn.loadingFailed();
                }else{
                    DateFormat df = new SimpleDateFormat(" dd/MM, HH:mm");
                    String date = df.format(Calendar.getInstance().getTime());
                    String keyRef = work_reference.push().getKey();
                    ToDoItem newItem = new ToDoItem(currentUser.getFullName(), currentUser.getFirebaseUID(), itemName.getText().toString().trim(),
                            itemDescription.getText().toString().trim(), date, null, color, null, keyRef,
                            false, isImportant);
                    work_reference.child(keyRef).setValue(newItem).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), "Item Saved", Toast.LENGTH_SHORT).show();
                                createTodoItemBtn.loadingSuccessful();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        // Actions to do after 1 second
                                        alertDialogNewItem.dismiss();
                                        isImportant = false;
                                    }
                                }, 750);
                            }else{
                                Toast.makeText(getActivity(), "Eror", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });


        alertDialogBuider.setView(popUpCreatorView);
        alertDialogNewItem = alertDialogBuider.create();
        alertDialogNewItem.show();


    }

}

