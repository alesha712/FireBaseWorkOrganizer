package com.hqs.alx.mushalmapp2.recyclerAdapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dx.dxloadingbutton.lib.LoadingButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hqs.alx.mushalmapp2.R;
import com.hqs.alx.mushalmapp2.WorkPlaceFragment;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.MyUser;
import com.hqs.alx.mushalmapp2.data.ToDoItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.Inflater;

/**
 * Created by Alex on 20/05/2018.
 */

public class MyToDOListAdapter extends RecyclerView.Adapter<MyToDOListAdapter.MyViewHolder> {

    private MyUser currentUser;
    private Context context;
    private ArrayList<ToDoItem> allItems;
    private DatabaseReference work_reference, user_reference;

    public MyToDOListAdapter (Context context, ArrayList<ToDoItem> allItems, MyUser currentUser){
        this.context = context;
        this.allItems = allItems;
        this.currentUser = currentUser;
    }

    @Override
    public MyToDOListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_todo_item, null);
        work_reference = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.ALL_APP_WORKPLACES).child(WorkPlaceFragment.currentWorkPlace.getWorkCode())
                .child(WorkPlaceFragment.currentWorkPlace.getWorkName()).child(FireBaseConstants.CHILD_TODO_ITEMS);
        user_reference = FirebaseDatabase.getInstance().getReference().child(FireBaseConstants.ALL_APP_USERS);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyToDOListAdapter.MyViewHolder holder, int position) {
        ToDoItem singleItem = allItems.get(position);
        holder.bindData(singleItem, position);
    }

    @Override
    public int getItemCount() {
        return allItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        AlertDialog alertDialogEditItem;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }


        public void bindData(final ToDoItem currentItem, final int position){

            ImageView itemStar = (ImageView) itemView.findViewById(R.id.todoItemStarIV);
            TextView itemHeader = (TextView) itemView.findViewById(R.id.todoItemHeaderTV);
            ImageView imageIncluded = (ImageView) itemView.findViewById(R.id.todoListImageIncludeIV);
            TextView itemDescription = (TextView) itemView.findViewById(R.id.todoItemTextTV);
            TextView todoItemPublisherTV = (TextView) itemView.findViewById(R.id.todoItemPublisherTV);
            TextView todoItemDateTV = (TextView) itemView.findViewById(R.id.todoItemDateTV);
            final ImageView todoItemOptionsIV = (ImageView) itemView.findViewById(R.id.todoItemOptionsIV);
            final RelativeLayout container = (RelativeLayout) itemView.findViewById(R.id.itemContainerRL);

            itemHeader.setText(currentItem.getSubject());
            itemDescription.setText(currentItem.getText());
            todoItemPublisherTV.setText(currentItem.getPublisherName());
            todoItemDateTV.setText(currentItem.getDate());
            String color = currentItem.getColor();

            if(!color.equals("") && color != null)
                container.setBackgroundColor(Color.parseColor(color));

            if(currentItem.isImportant())
                itemStar.setVisibility(View.VISIBLE);
            else
                itemStar.setVisibility(View.GONE);

            if(currentItem.getImageURL() != null)
                imageIncluded.setVisibility(View.VISIBLE);
            else
                imageIncluded.setVisibility(View.GONE);

            todoItemOptionsIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, todoItemOptionsIV);
                    popupMenu.inflate(R.menu.todo_item_menu);

                    if(currentItem.isComplete()){
                        popupMenu.getMenu().findItem(R.id.markComplete).setVisible(false);
                        popupMenu.getMenu().findItem(R.id.unMarkComplete).setVisible(true);
                    }else{
                        popupMenu.getMenu().findItem(R.id.markComplete).setVisible(true);
                        popupMenu.getMenu().findItem(R.id.unMarkComplete).setVisible(false);
                    }

                    if(currentUser.getFirebaseUID().equals(currentItem.getPublisherUID())){
                        popupMenu.getMenu().findItem(R.id.deleteItem).setVisible(true);
                    }else
                        popupMenu.getMenu().findItem(R.id.deleteItem).setVisible(false);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.edit:
                                    if(currentUser.getFirebaseUID().equals(currentItem.getPublisherUID()))
                                        editToDoItem(currentItem, position);
                                    else
                                        Toast.makeText(context, context.getResources().getString(R.string.onlyOriginalPublisher), Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.markComplete:
                                    currentItem.setComplete(true);
                                    currentItem.setCompleteBy(currentUser.getFirebaseUID());
                                    work_reference.child(currentItem.getKeyRef()).setValue(currentItem);
                                    allItems.remove(position);
                                    allItems.add(0 , currentItem);
                                    notifyDataSetChanged();
                                    break;
                                case R.id.unMarkComplete:
                                    currentItem.setComplete(false);
                                    currentItem.setCompleteBy(null);
                                    work_reference.child(currentItem.getKeyRef()).setValue(currentItem);
                                    allItems.remove(position);
                                    allItems.add(allItems.size()-1 , currentItem);
                                    notifyDataSetChanged();
                                    break;
                                case R.id.deleteItem:
                                    if(currentUser.getFirebaseUID().equals(currentItem.getPublisherUID())){
                                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                                        alertDialog.setTitle(context.getResources().getString(R.string.areYouSure)).setIcon(R.drawable.ic_warning_24dp)
                                                .setMessage(context.getResources().getString(R.string.todoDeleteMessage))
                                                .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        allItems.remove(position);
                                                        notifyDataSetChanged();
                                                        work_reference.child(currentItem.getKeyRef()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(context, context.getResources().getString(R.string.itemDeleted), Toast.LENGTH_SHORT).show();
                                                                }else
                                                                    Toast.makeText(context, context.getResources().getString(R.string.todoError), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                }).setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });

                                        AlertDialog dialog = alertDialog.create();
                                        dialog.show();
                                    }else
                                        Toast.makeText(context, context.getResources().getString(R.string.onlyOriginalPublisher), Toast.LENGTH_SHORT).show();

                            }
                            return false;
                        }
                    });

                    popupMenu.show();
                }
            });


        }

        public void editToDoItem(final ToDoItem currentItem, int position){

            AlertDialog.Builder alertDialogBuider = new AlertDialog.Builder(context);
            final View popUpCreatorView = LayoutInflater.from(context).inflate(R.layout.todo_item_creator, null);

            final TextInputEditText itemName = (TextInputEditText) popUpCreatorView.findViewById(R.id.todoItemNameET);
            final TextInputEditText itemDescription = (TextInputEditText) popUpCreatorView.findViewById(R.id.todoItemDescriptionET);
            final ImageView starIV = (ImageView) popUpCreatorView.findViewById(R.id.todoItemStarBorderIV);
            final LoadingButton createTodoItemBtn = (LoadingButton) popUpCreatorView.findViewById(R.id.createToDoItemBtn);
            final LinearLayout itemCreatorContainer = (LinearLayout) popUpCreatorView.findViewById(R.id.itemCreatorContainer);
            final LinearLayout colorsLayout = (LinearLayout) popUpCreatorView.findViewById(R.id.colorsLayout);

            itemName.setText(currentItem.getSubject());
            itemDescription.setText(currentItem.getText());
            itemCreatorContainer.setBackgroundColor(Color.parseColor(currentItem.getColor()));

            View.OnClickListener ppp = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()){
                        case R.id.itemBlueColor:
                            currentItem.setColor(context.getString(R.string.itemBlueString));
                            itemCreatorContainer.setBackgroundColor(context.getResources().getColor(R.color.ItemBlue));
                            break;
                        case R.id.itemRedColor:
                            currentItem.setColor(context.getString(R.string.itemRedString));
                            itemCreatorContainer.setBackgroundColor(context.getResources().getColor(R.color.itemRed));
                            break;
                        case R.id.itemWhiteColor:
                            currentItem.setColor(context.getString(R.string.itemWhiteString));
                            itemCreatorContainer.setBackgroundColor(context.getResources().getColor(R.color.itemWhite));
                            break;
                        case R.id.itemGreenColor:
                            currentItem.setColor(context.getString(R.string.itemGreenString));
                            itemCreatorContainer.setBackgroundColor(context.getResources().getColor(R.color.itemGreen));
                            break;
                        case R.id.itemPurpleColor:
                            currentItem.setColor(context.getString(R.string.itemPurpleString));
                            itemCreatorContainer.setBackgroundColor(context.getResources().getColor(R.color.itemPurple));
                            break;
                        case R.id.itemGreyColor:
                            currentItem.setColor(context.getString(R.string.itemGreyString));
                            itemCreatorContainer.setBackgroundColor(context.getResources().getColor(R.color.itemGrey));
                            break;
                        case R.id.itemOrangeColor:
                            currentItem.setColor(context.getString(R.string.itemOrangeString));
                            itemCreatorContainer.setBackgroundColor(context.getResources().getColor(R.color.itemOrange));
                            break;
                    }
                }
            };

            for (int i = 0; i < colorsLayout.getChildCount(); i++) {
                final ImageButton button = (ImageButton) colorsLayout.getChildAt(i);
                button.setOnClickListener(ppp);
            }

            if(currentItem.isImportant()){
                starIV.setImageResource(R.drawable.ic_star_24dp);
            }else{
                starIV.setImageResource(R.drawable.ic_star_border_24dp);
            }

            starIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!currentItem.isImportant()){
                        currentItem.setImportant(true);
                        starIV.setImageResource(R.drawable.ic_star_24dp);
                    }else{
                        currentItem.setImportant(false);
                        starIV.setImageResource(R.drawable.ic_star_border_24dp);
                    }
                }
            });

            createTodoItemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createTodoItemBtn.startLoading();
                    if(itemName.getText().toString().equals("")){
                        Toast.makeText(context, context.getResources().getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
                        createTodoItemBtn.loadingFailed();
                    }else{
                        currentItem.setSubject(itemName.getText().toString().trim());
                        currentItem.setText(itemDescription.getText().toString().trim());

                        work_reference.child(currentItem.getKeyRef()).setValue(currentItem).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(context, context.getResources().getString(R.string.todoSaved), Toast.LENGTH_SHORT).show();
                                    createTodoItemBtn.loadingSuccessful();
                                    notifyDataSetChanged();
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            // Actions to do after 1 second
                                            alertDialogEditItem.dismiss();
                                        }
                                    }, 750);
                                }else{
                                    Toast.makeText(context, context.getResources().getString(R.string.todoError), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                }
            });


            alertDialogBuider.setView(popUpCreatorView);
            alertDialogEditItem = alertDialogBuider.create();
            alertDialogEditItem.show();



        }
    }
}
