package lets.digi.talk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import lets.digi.talk.R;
import lets.digi.talk.model.MessagePojo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MessagePojo> messages;
    private Context context;
    private FirebaseUser currentUser;

    public ChatAdapter(List<MessagePojo> listdata, Context context) {
        this.messages = listdata;
        this.context = context;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSender().contentEquals(currentUser.getUid()))
            return 0;
        else return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == 0) {
            //receiver
            View listItem = layoutInflater.inflate(R.layout.layout_receiver, parent, false);
            return new ReceiverViewHolder(listItem, context);
        } else {
            //sender
            View listItem = layoutInflater.inflate(R.layout.layout_sender, parent, false);
            return new SenderViewHolder(listItem, context);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SenderViewHolder){
            ((SenderViewHolder) holder).textView.setText(messages.get(position).getMessage());
        }else if (holder instanceof ReceiverViewHolder){
            ((ReceiverViewHolder) holder).textView.setText(messages.get(position).getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ReceiverViewHolder(View itemView, final Context context) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.textview);
        }
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public SenderViewHolder(View itemView, final Context context) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.textview);
        }
    }

}
