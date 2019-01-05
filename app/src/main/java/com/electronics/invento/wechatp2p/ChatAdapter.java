package com.electronics.invento.wechatp2p;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private Context mContext;
    private List<Chats> mChatsArrayLists;

    public ChatAdapter(Context context, List<Chats> mChatsArrayLists) {
        this.mContext = context;
        this.mChatsArrayLists = mChatsArrayLists;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.all_users_chat, parent, false);
        return new ChatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatViewHolder holder, int position) {
        Chats currentChat = mChatsArrayLists.get(position);
        String message = currentChat.getMessage();
        String type = currentChat.getType();

        if (type.equals("receiver")) {
            holder.cv_receiver.setVisibility(View.VISIBLE);
            holder.textView_receiver.setVisibility(View.VISIBLE);
            holder.textView_sender.setVisibility(View.GONE);
            holder.cv_sender.setVisibility(View.GONE);

            holder.textView_receiver.setText(message);
        } else if (type.equals("sender")) {
            holder.cv_receiver.setVisibility(View.GONE);
            holder.textView_receiver.setVisibility(View.GONE);
            holder.textView_sender.setVisibility(View.VISIBLE);
            holder.cv_sender.setVisibility(View.VISIBLE);

            holder.textView_sender.setText(message);
        }
    }

    @Override
    public int getItemCount() {
        return mChatsArrayLists.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView_receiver_profileImage, imageView_sender_profileImage;
        TextView textView_receiver, textView_sender;
        CardView cv_sender, cv_receiver;

        public ChatViewHolder(View itemView) {
            super(itemView);

            imageView_receiver_profileImage = itemView.findViewById(R.id.imageView_all_users_receiver_profile);
            imageView_sender_profileImage = itemView.findViewById(R.id.imageView_all_users_sender_profile);
            textView_receiver = itemView.findViewById(R.id.textView_allusers_chat_receiver_message);
            textView_sender = itemView.findViewById(R.id.textView_allusers_chat_sender_message);
            cv_sender = itemView.findViewById(R.id.cv_all_users_sender);
            cv_receiver = itemView.findViewById(R.id.cv_all_users_receiver);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final CharSequence[] items = {"delete"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ProcessExternalDBHelper deleteProcess = new ProcessExternalDBHelper(mContext);
                            deleteProcess.openWrite();
                            deleteProcess.deleteChatRow(mChatsArrayLists,getAdapterPosition());
                            mChatsArrayLists.clear();
                            mChatsArrayLists.addAll(deleteProcess.findallChats("dummy","dummy"));
                            deleteProcess.close();

                            notifyDataSetChanged();
                        }
                    });
                    builder.show();
                    return true;
                }
            });
        }
    }

}