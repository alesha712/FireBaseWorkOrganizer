package com.hqs.alx.mushalmapp2;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hqs.alx.mushalmapp2.data.MyAppConstants;
import com.hqs.alx.mushalmapp2.data.MyUser;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Alex on 20/01/2018.
 */

public class MyChatRecyclerAdapter extends RecyclerView.Adapter<MyChatRecyclerAdapter.MyViewHolder> {

    private ArrayList<ChatMessage> messagesList;
    Context context;
    private MyUser currentUser;
    private int previewType;

    public MyChatRecyclerAdapter(ArrayList<ChatMessage> messagesList, Context context, MyUser currentUser, int previewType) {
        this.messagesList = messagesList;
        this.context = context;
        this.currentUser = currentUser;
        this.previewType = previewType;
    }

    @Override
    public int getItemViewType(int position) {
        //numToReturn will determine if the message is from the currentUser or from other users
        int numToReturn;
        Log.d("ERRR", "DMAN");
        if(previewType == MyAppConstants.CHAT_PREVIEW_TYPE){
            if(messagesList.get(position).getUserUID().equals(currentUser.getFirebaseUID()))
                numToReturn = 4;
            else
                numToReturn = 3;
        }else if(messagesList.get(position).getUserUID().equals(currentUser.getFirebaseUID())){
            numToReturn = 2;
        }else{
            numToReturn = 0;
        }
        return numToReturn;
    }

    @Override
    public MyChatRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View chatView;
        MyViewHolder singleMessage = null;
        //2 different layouts - one for the current user (2) and one for others (0)
        switch (viewType){
            case 0:
                chatView = LayoutInflater.from(context).inflate(R.layout.single_chat_message, null);
                singleMessage = new MyViewHolder(chatView);
                break;
            case 2:
                chatView = LayoutInflater.from(context).inflate(R.layout.single_chat_message2, null);
                singleMessage = new MyViewHolder(chatView);
                break;
            case 3:
                chatView = LayoutInflater.from(context).inflate(R.layout.chat_preview, null);
                singleMessage = new MyViewHolder(chatView);
                break;
            case 4:
                chatView = LayoutInflater.from(context).inflate(R.layout.chat_preview2, null);
                singleMessage = new MyViewHolder(chatView);
                break;
        }

        return singleMessage;
    }

    @Override
    public void onBindViewHolder(MyChatRecyclerAdapter.MyViewHolder holder, int position) {
        ChatMessage currenMessage = messagesList.get(position);
        switch (holder.getItemViewType()){
            case 0:
                holder.bindMyMessageData(currenMessage, position);
                break;
            case 2:
                holder.bindMyMessageData2(currenMessage, position);
                break;
            case 3:
                holder.bindMyMessageData3(currenMessage, position);
                break;
            case 4:
                holder.bindMyMessageData4(currenMessage, position);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


    public class MyViewHolder extends  RecyclerView.ViewHolder{

        View itemView;
        ProgressBar chatImageProgressBar;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public void bindMyMessageData(final ChatMessage message, int currentPossition){

            chatImageProgressBar = (ProgressBar) itemView.findViewById(R.id.imageChatProgress);
            TextView UserName = (TextView) itemView.findViewById(R.id.userNameChatTV);
            TextView UserMessage = (TextView) itemView.findViewById(R.id.chatMessageTextTV);
            CircularImageView UserPhoto = (CircularImageView) itemView.findViewById(R.id.previewUserIV);
            ImageView sentImageIV = (ImageView) itemView.findViewById(R.id.sentImageIV);

            UserName.setText(message.getName());
            if(message.getUserPhotoUrl() != null && !message.getUserPhotoUrl().equals("")){
                chatImageProgressBar.setVisibility(View.VISIBLE);
                Glide.with(UserPhoto.getContext()).load(message.getUserPhotoUrl()).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        chatImageProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        chatImageProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(UserPhoto);
            }

            if(message.getPhotoUrl() != null){
                UserMessage.setVisibility(View.INVISIBLE);
                Glide.with(sentImageIV.getContext()).load(message.getPhotoUrl()).into(sentImageIV);
                sentImageIV.setBackground(context.getResources().getDrawable(R.drawable.message_wrapper_not_current_user));
                sentImageIV.setVisibility(View.VISIBLE);
                sentImageIV.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        shareImage(message.getPhotoUrl());
                        return false;
                    }
                });
            }else{
                chatImageProgressBar.setVisibility(View.GONE);
                sentImageIV.setVisibility(View.GONE);
                UserMessage.setVisibility(View.VISIBLE);
                UserMessage.setText(message.getText());
            }
        }

        public void bindMyMessageData2(final ChatMessage message, int currentPossition){

            chatImageProgressBar = (ProgressBar) itemView.findViewById(R.id.currentUserChatImageProgress);
            TextView UserMessage2 = (TextView) itemView.findViewById(R.id.chatMessageText2TV);
            ImageView sentImage2IV = (ImageView) itemView.findViewById(R.id.sentImage2IV);

            if(message.getPhotoUrl() != null){
                chatImageProgressBar.setVisibility(View.VISIBLE);
                UserMessage2.setVisibility(View.GONE);
                Glide.with(sentImage2IV.getContext()).load(message.getPhotoUrl()).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        chatImageProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        chatImageProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(sentImage2IV);
                sentImage2IV.setBackground(context.getResources().getDrawable(R.drawable.message_wrapper));
                sentImage2IV.setVisibility(View.VISIBLE);

                sentImage2IV.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        shareImage(message.getPhotoUrl());
                        return false;
                    }
                });

            }else{
                chatImageProgressBar.setVisibility(View.GONE);
                sentImage2IV.setVisibility(View.GONE);
                UserMessage2.setVisibility(View.VISIBLE);
                UserMessage2.setText(message.getText());
            }
        }

        public void bindMyMessageData3(ChatMessage message, int currentPossition){
            TextView UserName = (TextView) itemView.findViewById(R.id.previewUserNameChatTV);
            TextView UserMessage = (TextView) itemView.findViewById(R.id.previewChatMessageTextTV);
            CircularImageView UserPhoto = (CircularImageView) itemView.findViewById(R.id.previewChatUserIV);

            UserName.setText(message.getName());
            if(message.getUserPhotoUrl() != null && !message.getUserPhotoUrl().equals("")){
                Glide.with(UserPhoto.getContext()).load(message.getUserPhotoUrl()).into(UserPhoto);
            }

            if(message.getPhotoUrl() != null){
                UserMessage.setText("IMAGE");
            }else{
                UserMessage.setText(message.getText());
            }

        }

        public void bindMyMessageData4(ChatMessage message, int currentPossition){
            TextView UserMessage = (TextView) itemView.findViewById(R.id.previewChatMessageText2TV);

            if(message.getPhotoUrl() != null){
                UserMessage.setText("IMAGE");
            }else{
                UserMessage.setText(message.getText());
            }

        }

        public void shareImage(String string){
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, string);
            shareIntent.setType("text/plain");
            context.startActivity(Intent.createChooser(shareIntent, context.getResources().getText(R.string.share)));
        }
    }
}
