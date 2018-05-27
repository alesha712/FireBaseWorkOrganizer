package com.hqs.alx.mushalmapp2;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hqs.alx.mushalmapp2.data.EmployeeForShift;
import com.hqs.alx.mushalmapp2.data.FireBaseConstants;
import com.hqs.alx.mushalmapp2.data.MyUser;
import com.hqs.alx.mushalmapp2.data.WorkPlaces;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.hqs.alx.mushalmapp2.MyResizeBitmap.getPath;

public class ChatFragment extends Fragment {

    private FirebaseDatabase myFireBaseDataBase;
    private DatabaseReference myMessagesDataBaseReference, myUserDataReference, myNotificationDataRef;
    private ChildEventListener myChildEventListener;
    private FirebaseAuth myFirebaseAuth;
    private FirebaseStorage myFirebaseStorage;
    private StorageReference myStorageReference;
    private FirebaseUser myFirebaseUser;

    public ArrayList<EmployeeForShift> workUsers;
    public WorkPlaces currentWorkPlace;

    MyUser currentUser;
    ArrayList<ChatMessage> allMessages;
    private EditText messageET;
    private String myUserName, user_UID, workName, workCode;
    private ImageView photoPickIV, sendMessageIV;
    RecyclerView myMessagesRecycler;
    MyChatRecyclerAdapter myChatRecyclerAdapter;

    public static final int RC_PHOTO_PICKER = 522;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(savedInstanceState != null){
            currentWorkPlace = savedInstanceState.getParcelable("currentWorkPlace");
            currentUser = savedInstanceState.getParcelable("currentUser");
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        //setBackButtonResult(view);

        if(currentWorkPlace != null){
            workName = currentWorkPlace.getWorkName();
            workCode = currentWorkPlace.getWorkCode();
        }

        sendMessageIV = (ImageView) view.findViewById(R.id.sendIV);
        sendMessageIV.setEnabled(false);
        messageET = (EditText) view.findViewById(R.id.messageET);
        photoPickIV = (ImageView) view.findViewById(R.id.imagePickerIV);
        myMessagesRecycler = (RecyclerView) view.findViewById(R.id.messageRecyclerView);

        myFirebaseStorage = FirebaseStorage.getInstance();
        myFirebaseAuth = FirebaseAuth.getInstance();
        myFireBaseDataBase = FirebaseDatabase.getInstance();
        myUserDataReference = myFireBaseDataBase.getReference();
        myNotificationDataRef = FirebaseDatabase.getInstance().getReference();
        myFirebaseUser = myFirebaseAuth.getCurrentUser();

        if(currentUser != null){
            user_UID = currentUser.getFirebaseUID();
            myUserDataReference.child(FireBaseConstants.ALL_APP_USERS).child(currentUser.getFirebaseUID()).child(FireBaseConstants.CHILD_USER_WORK_PLACES).child(workName)
                    .child(FireBaseConstants.CHILD_UNREAD_MESSAGES).setValue(null);

            //picking image to send on clicking the image
            photoPickIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
                }
            });

            //set a listener on the ET
            messageET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().trim().length() > 0) {
                        sendMessageIV.setEnabled(true);
                    } else {
                        sendMessageIV.setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            //set on back presses after focus on edit text
            //setBackButtonResult(messageET);

            sendMessageIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //sending the message
                    ChatMessage newMessage = new ChatMessage(messageET.getText().toString().trim(), currentUser.getFirebaseUID(), currentUser.getFullName(),
                            currentUser.getProfileImageString(), null, true);
                    myMessagesDataBaseReference.push().setValue(newMessage);

                    for (int i = 0; i < workUsers.size(); i++) {
                        if(!workUsers.get(i).getUid().equals(currentUser.getFirebaseUID()))
                            myUserDataReference.child(FireBaseConstants.ALL_APP_USERS).child(workUsers.get(i).getUid())
                                    .child(FireBaseConstants.CHILD_USER_WORK_PLACES).child(workName).child(FireBaseConstants.CHILD_UNREAD_MESSAGES)
                                    .push().setValue(newMessage);
                    }

                    //clearing the edit text value
                    messageET.setText("");

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", user_UID);
                    notificationData.put("type", "chat_message");

                    myNotificationDataRef.child(FireBaseConstants.ALL_APP_WORKPLACES).child(workCode).child(workName)
                            .child(FireBaseConstants.CHILD_NOTIFICATION).push().setValue(notificationData)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Log.d("Notification adding ", " to DB is Successful" );
                                    }else{
                                        Log.d("Notification adding ", " to DB is Not Successful" );
                                    }
                                }
                            });

                }
            });
        }else{
            Toast.makeText(getActivity(), "ERROR, TRY AGAIN", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    public void setBackButtonResult(View v){
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d("keyCode: " , "" + keyCode);
                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP || event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.FLAG_CANCELED) {
                    /*FragmentChanger fragmentChanger = (FragmentChanger) getActivity();
                    fragmentChanger.changeFragments();*/

                    Log.i("WOOOW: ", "onKey Back listener is working!!!");
                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        ((MainActivity) getActivity()).setActionBarTitle(currentWorkPlace.getWorkName());
        setChildListener();
        //if user connected to chat - he is online and will not get "not read messages"
        myUserDataReference.child(FireBaseConstants.ALL_APP_WORKPLACES).child(currentWorkPlace.getWorkCode()).child(currentWorkPlace.getWorkName())
                .child(FireBaseConstants.CHILD_USERS).child(user_UID).child("online").setValue(true);
        super.onResume();
    }

    @Override
    public void onPause() {
        myMessagesDataBaseReference.removeEventListener(myChildEventListener);
        //if user is not connected to chat - he is offline and will get "not read messages"
        myUserDataReference.child(FireBaseConstants.ALL_APP_WORKPLACES).child(currentWorkPlace.getWorkCode()).child(currentWorkPlace.getWorkName())
                .child(FireBaseConstants.CHILD_USERS).child(user_UID).child("online").setValue(false);
        super.onPause();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){

                Uri selectedImageUri = data.getData();
                MyResizeBitmap myResizeBitmap = new MyResizeBitmap();
                Bitmap resizedBitmap = myResizeBitmap.resizeBitmap(getPath(getActivity(), selectedImageUri), 1000, 1000);

                long timeStamp = System.currentTimeMillis();
                String pathString = selectedImageUri.getLastPathSegment() + "" + timeStamp;
                StorageReference photoReference = myStorageReference.child(pathString);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                byte[] bitmapData = baos.toByteArray();

                UploadTask uploadTask = photoReference.putBytes(bitmapData);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.eror_updating_user), Toast.LENGTH_SHORT).show();
                        Log.e("Chat_IMAGE_UPLOAD_ERROR", e.getMessage().toString());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadImageUrl = taskSnapshot.getDownloadUrl();

                        ChatMessage newMessage = new ChatMessage("", currentUser.getFirebaseUID(), currentUser.getFullName(),
                                currentUser.getProfileImageString(), downloadImageUrl.toString(), true);
                        myMessagesDataBaseReference.push().setValue(newMessage);
                    }
                });
        }
    }

    private void setChildListener(){

        allMessages = new ArrayList<ChatMessage>();

        myChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatMessage message = dataSnapshot.getValue(ChatMessage.class);

                allMessages.add(message);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                linearLayoutManager.setStackFromEnd(true);
                myMessagesRecycler.setLayoutManager(linearLayoutManager);

                myChatRecyclerAdapter.notifyItemInserted(allMessages.size() - 1);
                int position = myMessagesRecycler.getAdapter().getItemCount()-1;
                myMessagesRecycler.smoothScrollToPosition(position);
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

        myMessagesDataBaseReference = myFireBaseDataBase.getReference().child(FireBaseConstants.ALL_APP_WORKPLACES).child(workCode).child(workName).child(FireBaseConstants.CHILD_MESSAGES);
        myStorageReference = myFirebaseStorage.getReference().child(workCode).child(workName).child(FireBaseConstants.CHILD_CHAT_PHOTOS);
        myMessagesDataBaseReference.addChildEventListener(myChildEventListener);

        myChatRecyclerAdapter = new MyChatRecyclerAdapter(allMessages, getActivity(), currentUser, 0);
        myMessagesRecycler.setAdapter(myChatRecyclerAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("currentWorkPlace", currentWorkPlace);
        outState.putParcelable("currentUser", currentUser);
    }
}
