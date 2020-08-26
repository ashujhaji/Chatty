package com.chatty.app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chatty.app.Constant;
import com.chatty.app.R;
import com.chatty.app.activity.ChatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CallConnectionFragment extends Fragment {

    private static String TAG = CallConnectionFragment.class.getSimpleName();
    private boolean isChatActive = false;
    private String chatId = "";
    private DatabaseReference mDatabaseRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_call_connection,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        checkForAvailableChat();
    }

    private void checkForAvailableChat() {
        DatabaseReference mRef = mDatabaseRef.child("ongoing_calls");
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
        DatabaseReference mRef = mDatabaseRef.child("ongoing_calls");
        chatId = UUID.randomUUID().toString();
        Map<String, String> message = new HashMap<>();
        message.put("room_id", Constant.getRoomId());
        message.put("status", "waiting");
        mRef.child(chatId).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listenForStatusChange(chatId);
                Log.d(TAG, "chat created");
            }
        });
    }

    private void removeCall(String chatId) {
        if (!chatId.isEmpty()) {
            DatabaseReference mRef = mDatabaseRef.child("ongoing_calls").child(chatId);
            Map<String, String> message = new HashMap<>();
            message.put("status", "finish");
            mRef.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
//                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                }
            });
        }
    }

    private void listenForStatusChange(final String chat_Id) {
        DatabaseReference mRef = mDatabaseRef.child("ongoing_calls").child(chat_Id).child("status");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue()!=null){
                    String status = snapshot.getValue().toString();
                    if (status.contentEquals("waiting")){
                        Log.d(TAG,"waiting");
                    }else if (status.contentEquals("ongoing")){
                        isChatActive = true;
                        //  listenForMessages(chatId);
                    }else if (status.contentEquals("finish")){
                        isChatActive = false;
                        Log.d(TAG,"finish");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "User added");
            }
        });
    }

    private void addToChat(final String chatId) {
        DatabaseReference mRef = mDatabaseRef.child("ongoing_calls").child(chatId);
        Map<String, String> message = new HashMap<>();
        message.put("status", "ongoing");
        mRef.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                isChatActive = true;
               // listenForMessages(chatId);
                Log.d(TAG, "chat created");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeCall(chatId);
    }
}
