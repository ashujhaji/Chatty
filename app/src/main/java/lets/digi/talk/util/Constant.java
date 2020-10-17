package lets.digi.talk.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;


import lets.digi.talk.R;
import lets.digi.talk.model.ChatOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Constant {
    public static List<ChatOptions> chatOptions() {
        List<ChatOptions> list = new ArrayList<>();
        list.add(new ChatOptions("Start a call", R.drawable.ic_phone, R.drawable.grad_call));
        list.add(new ChatOptions("Send a message", R.drawable.ic_chat, R.drawable.grad_chat));
        return list;
    }

    public static String getRoomId() {
        String SALTCHARS = "1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 9) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    public static void openPlaystore(Context context){
        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        context.startActivity(intent);
    }
}
