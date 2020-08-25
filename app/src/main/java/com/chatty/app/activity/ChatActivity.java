package com.chatty.app.activity;

import android.app.ProgressDialog;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        init();
        setToolbar();

        //Get database reference
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //set recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(new ChatAdapter(messages, this));

        addUserInWaitingRoom();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isChatActive) {
            //Remove chat
            removeChat(chatId);
        } else {
            //Remove user from waiting list
            removeUserFromWaiting(currentUser.getUid());
        }
    }

    private void init() {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
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

    private void removeUserFromWaiting(String userId) {
        Query dataQuery = mDatabaseRef.child("text_chat_waiting").orderByChild("user_id").equalTo(userId);

        dataQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    userSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    private void removeChat(String chatId) {
        if (!chatId.isEmpty()) {
            DatabaseReference mRef = mDatabaseRef.child("ongoing_chat").child(chatId);
            mRef.removeValue();
        }
    }

    private void addUserInWaitingRoom() {
        //Add user in chat waiting room
        DatabaseReference mRef = mDatabaseRef.child("text_chat_waiting").push().child("user_id");
        mRef.setValue(currentUser.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "User added");
                checkForPartner();
            }
        });
    }

    private void checkForPartner() {
        DatabaseReference mRef = mDatabaseRef.child("text_chat_waiting");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() == 1) {
                    //Wait
                    try {
                        dialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (snapshot.getChildrenCount() > 1) {
                    //Start chat
                    List<String> users = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        users.add(child.child("user_id").getValue().toString());
                    }
                    //Start chat
                    startChat(users);
                } else {
                    //No data found
                    dialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    private void startChat(final List<String> users) {
        Toast.makeText(this, "Chat starting", Toast.LENGTH_LONG).show();
        isChatActive = true;
        chatId = users.get(0) + users.get(1);
        disclaimer.setVisibility(View.GONE);
        dialog.dismiss();
        listenForMessages(mDatabaseRef.child("ongoing_chat").child(users.get(0) + users.get(1)));
        for (String user : users) {
            removeUserFromWaiting(user);
        }
    }

    private void sendMessage(String messageText) {
        //add to list
        messages.add(new MessagePojo(currentUser.getUid(), messageField.getText().toString()));
        messageField.setText("");
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(messages.size() - 1);

        DatabaseReference mRef = mDatabaseRef.child("ongoing_chat").child(chatId).child("messages").push();
        Map<String, String> message = new HashMap<>();
        message.put("sender", currentUser.getUid());
        message.put("message", messageText);
        mRef.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "message sent");
            }
        });
    }

    private void listenForMessages(DatabaseReference mRef) {
        mRef.child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0) {
                    // messages.clear();
                    List<MessagePojo> messageList = new ArrayList<>();
                    for (DataSnapshot message : snapshot.getChildren()) {
                        messageList.add(new MessagePojo(message.child("sender").getValue().toString(), message.child("message").getValue().toString()));
                    }

                    //Notify adapter
                    if (!messageList.get(messageList.size() - 1).getSender().contentEquals(currentUser.getUid())) {
                        messages.add(messageList.get(messageList.size() - 1));
                        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(messages.size() - 1);
                        Log.d("messageTag", String.valueOf(messages.size()));
                    }
                } else {
                    if (!messages.isEmpty()) {
                        //Chat exit
                      //  isChatActive = false;
                        chatFinished();
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
