package com.chatty.app;

import com.chatty.app.model.ChatOptions;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class Constant {
    public static List<ChatOptions> chatOptions() {
        List<ChatOptions> list = new ArrayList<>();
        list.add(new ChatOptions("Start a call", R.drawable.ic_phone, R.drawable.grad_call));
        list.add(new ChatOptions("Send a message", R.drawable.ic_chat, R.drawable.grad_chat));
        return list;
    }

    public FirebaseUser user;
}
