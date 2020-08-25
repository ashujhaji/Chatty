package com.chatty.app.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chatty.app.R;
import com.chatty.app.adapter.ChatAdapter;
import com.chatty.app.model.MessagePojo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = ChatActivity.class.getSimpleName();
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private EditText messageField;
    private ImageView send;
    private TextView disclaimer;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private boolean isChatActive = false;
    private String chatId = "";
    private ProgressDialog dialog;
    private List<MessagePojo> messages = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Get database reference
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        init();
        setToolbar();

        //set recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(new ChatAdapter(messages, this));

        checkForAvailableChat();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isChatActive) {
            //Remove chat
            removeChat(chatId);
        }
    }

    private void init() {
        dialog = new ProgressDialog(this);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        dialog.setMessage("Searching for chat partner");
        dialog.show();

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        messageField = findViewById(R.id.messageField);
        send = findViewById(R.id.send);
        disclaimer = findViewById(R.id.disclaimer);

        send.setOnClickListener(this);
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
    }

    private void removeChat(String chatId) {
        if (!chatId.isEmpty()) {
            DatabaseReference mRef = mDatabaseRef.child("ongoing_chats").child(chatId);
            Map<String, String> message = new HashMap<>();
            message.put("status", "finish");
            mRef.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    dialog.dismiss();
                }
            });
        }
    }

    private void checkForAvailableChat() {
        DatabaseReference mRef = mDatabaseRef.child("ongoing_chats");
        mRef.orderByChild("status")
                .equalTo("waiting").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0) {
                    //Chat room available
                    for (DataSnapshot chat : snapshot.getChildren()) {
                        chatId = chat.getKey();
                        addToChat(chatId);
                        break;
                    }
                } else {
                    //No chat room available. Create new one
                    createNewChat();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createNewChat() {
        DatabaseReference mRef = mDatabaseRef.child("ongoing_chats");
        chatId = UUID.randomUUID().toString();
        Map<String, String> message = new HashMap<>();
        //message.put("chat_id", chatId);
        message.put("status", "waiting");
        mRef.child(chatId).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listenForStatusChange(chatId);
                Log.d(TAG, "chat created");
            }
        });
    }

    private void addToChat(final String chatId) {
        DatabaseReference mRef = mDatabaseRef.child("ongoing_chats").child(chatId);
        Map<String, String> message = new HashMap<>();
        message.put("status", "ongoing");
        mRef.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Start your chat", Toast.LENGTH_SHORT).show();
                isChatActive = true;
                listenForMessages(chatId);
                Log.d(TAG, "chat created");
            }
        });
    }

    private void listenForStatusChange(final String chat_Id) {
        DatabaseReference mRef = mDatabaseRef.child("ongoing_chats").child(chat_Id).child("status");
        mRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String status = snapshot.getValue().toString();
                        if (status.contentEquals("waiting")){
                            dialog.show();
                        }else if (status.contentEquals("ongoing")){
                            disclaimer.setVisibility(View.GONE);
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Start your chat", Toast.LENGTH_SHORT).show();
                            isChatActive = true;
                            listenForMessages(chatId);
                        }else if (status.contentEquals("finish")){
                            isChatActive = false;
                            chatFinished();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "User added");
                    }
                });
    }

    private void listenForMessages(String key) {
        final DatabaseReference mRef = mDatabaseRef.child("ongoing_chats").child(key).child("messages");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0) {
                    List<MessagePojo> messageList = new ArrayList<>();
                    for (DataSnapshot message : snapshot.getChildren()) {
                        Log.d("chatTag",String.valueOf(message));
                        messageList.add(new MessagePojo(message.child("sender").getValue().toString(), message.child("message").getValue().toString()));
                    }

                    //Notify adapter
                    if (!messageList.get(messageList.size() - 1).getSender().contentEquals(currentUser.getUid())) {
                        messages.add(messageList.get(messageList.size() - 1));
                        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(messages.size() - 1);
                        Log.d("messageTag", String.valueOf(messages.size()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void chatFinished() {
        final Snackbar snackbar = Snackbar.make(this.findViewById(R.id.bottomView),
                "Your partner has left this chat. Please restart to chat more.",
                Snackbar.LENGTH_INDEFINITE
        );
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
                finish();
            }
        });
        snackbar.show();
    }

    private void sendMessage(final String messageText) {
        //add to list
        messages.add(new MessagePojo(currentUser.getUid(), messageField.getText().toString()));
        messageField.setText("");
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(messages.size() - 1);

        final DatabaseReference mRef = mDatabaseRef.child("ongoing_chats").child(chatId);
        Map<String, String> message = new HashMap<>();
        message.put("sender", currentUser.getUid());
        message.put("message", messageText);
        mRef.child("messages").push().setValue(message)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "message sent");
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send: {
                if (!isChatActive) {
                    Toast.makeText(this, "Chat is not active", Toast.LENGTH_LONG).show();
                    return;
                }
                if (messageField.getText().toString().isEmpty()) {

                    return;
                }
                 sendMessage(messageField.getText().toString());
                break;
            }
        }
    }
}
