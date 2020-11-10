package lets.digi.talk.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import lets.digi.talk.R;
import lets.digi.talk.adapter.ChatAdapter;
import lets.digi.talk.fragment.AlertFragment;
import lets.digi.talk.model.MessagePojo;
import lets.digi.talk.util.AdHelper;
import lets.digi.talk.util.GenerateNotification;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = ChatActivity.class.getSimpleName();
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private EditText messageField;
    private ImageView send;
    private ImageView newChat;
    private TextView disclaimer;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private boolean isChatActive = false;
    private String chatId = "";
    private ProgressDialog dialog;
    private List<MessagePojo> messages = new ArrayList<>();
    private boolean isActivityVisible = false;

    int MIN_KEYBOARD_HEIGHT_PX = 150;
    int lastVisibleDecorViewHeight = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        AdHelper.getInstance().loadInterstitialAd(this, false);

        //Get database reference
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        init();
        setToolbar();
        initKeyBoardListener(this);

        //set recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
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
                removeChat(chatId);
                finish();
            }
        });
        dialog.setMessage("Searching for chat partner");
        dialog.show();

        newChat = findViewById(R.id.new_chat);
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
                        listenForStatusChange(chatId, false);
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
                Log.d(TAG, error.getMessage());
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
                listenForStatusChange(chatId, true);
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

    private void listenForStatusChange(final String chat_Id, final boolean listenToMessage) {
        DatabaseReference mRef = mDatabaseRef.child("ongoing_chats").child(chat_Id).child("status");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String status = snapshot.getValue().toString();
                    Log.d("statusTag", status);
                    if (status.contentEquals("waiting")) {
                        try {
                            dialog.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (status.contentEquals("ongoing")) {
                        disclaimer.setVisibility(View.GONE);
                        getSupportFragmentManager().popBackStackImmediate();
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Start your chat", Toast.LENGTH_SHORT).show();
                        isChatActive = true;
                        if (listenToMessage)
                            listenForMessages(chatId);
                    } else if (status.contentEquals("finish")) {
                        if (isChatActive) {
                            isChatActive = false;
                            chatFinished();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "User added");
            }
        });
    }

    private void listenForMessages(String key) {
        newChat.setOnClickListener(this);
        disclaimer.setVisibility(View.GONE);
        final DatabaseReference mRef = mDatabaseRef.child("ongoing_chats").child(key).child("messages");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isChatActive) {
                    Log.d("msgTag", "received");
                    if (snapshot.getChildrenCount() > 0) {
                        List<MessagePojo> messageList = new ArrayList<>();
                        for (DataSnapshot message : snapshot.getChildren()) {
                            messageList.add(new MessagePojo(message.child("sender").getValue().toString(),
                                    message.child("message").getValue().toString(),
                                    Long.parseLong(message.child("timestamp").getValue().toString())));
                        }

                        //Notify adapter
                        if (!messageList.get(messageList.size() - 1).getSender().contentEquals(currentUser.getUid())) {
                            messages.add(0, messageList.get(messageList.size() - 1));
                            Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(0);
                            recyclerView.smoothScrollToPosition(0);
                            if (!isActivityVisible) {
                                //send notification
                                GenerateNotification.send("New message received", messageList.get(messageList.size() - 1).getMessage(), ChatActivity.this);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void chatFinished() {
        try {
            loadFragment(AlertFragment.getInstance("Partner left",
                    "Your partner has left this chat. Please restart to chat more.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!chatId.isEmpty()) {
                                isChatActive = false;
                                messages.clear();
                                Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
                                checkForAvailableChat();
                            }
                        }
                    }), AlertFragment.class.getSimpleName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(final String messageText) {
        //add to list
        disclaimer.setVisibility(View.GONE);
        messages.add(0, new MessagePojo(currentUser.getUid(), messageField.getText().toString().trim(),System.currentTimeMillis()));
        messageField.setText("");
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);

        final DatabaseReference mRef = mDatabaseRef.child("ongoing_chats").child(chatId);
        Map<String, String> message = new HashMap<>();
        message.put("sender", currentUser.getUid());
        message.put("message", messageText);
        message.put("timestamp",String.valueOf(System.currentTimeMillis()));
        mRef.child("messages").push().setValue(message)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "message sent");
                    }
                });
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send: {
                if (!isChatActive) {
                    Toast.makeText(this, "Chat is not active", Toast.LENGTH_LONG).show();
                    return;
                }
                if (messageField.getText().toString().trim().length() < 1) {

                    return;
                }
                sendMessage(messageField.getText().toString().trim());
                break;
            }
            case R.id.new_chat: {
                showNewChatDialog();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
            AdHelper.getInstance().showAd();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityVisible = true;
    }

    private void initKeyBoardListener(Activity activity) {

        final View decorView = activity.getWindow().getDecorView();
        final Rect windowVisibleDisplayFrame = new Rect();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
                int visibleDecorViewHeight = windowVisibleDisplayFrame.height();
                if (lastVisibleDecorViewHeight != 0) {
                    if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {
                        recyclerView.smoothScrollToPosition(0);
                    } else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX < visibleDecorViewHeight) {
                        recyclerView.smoothScrollToPosition(0);
                    }
                }
                lastVisibleDecorViewHeight = visibleDecorViewHeight;
            }
        });
    }

    private void showNewChatDialog() {
        loadFragment(AlertFragment.getInstance("New Chat",
                "Do you really want to switch to new chat?",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!chatId.isEmpty()) {
                            isChatActive = false;
                            messages.clear();
                            Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
                            DatabaseReference mRef = mDatabaseRef.child("ongoing_chats").child(chatId);
                            Map<String, String> message = new HashMap<>();
                            message.put("status", "finish");
                            mRef.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    checkForAvailableChat();
                                }
                            });
                        }
                    }
                }), AlertFragment.class.getSimpleName());
    }

    private void loadFragment(Fragment fragment, String name) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(android.R.id.content, fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(name);
        transaction.commit();
    }
}
