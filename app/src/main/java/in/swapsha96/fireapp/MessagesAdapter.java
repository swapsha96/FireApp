package in.swapsha96.fireapp;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private Context context;
    private int lastPosition = -1;
    private String uid, markerUser;
    ArrayList<UserMessage> messages;
    Map<String, Integer> keys;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    public MessagesAdapter(Context context, String uid, String markerUser) {
        this.uid = uid;
        this.markerUser = markerUser;
        this.context = context;
        this.messages = new ArrayList<>();
        this.keys = new HashMap<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final UserMessage userMessage = messages.get(position);
        databaseReference.child(userMessage.getFrom()).child("details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
                holder.from.setText(userDetails.getDisplayName());
                holder.message.setText(userMessage.getMessage());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(DataSnapshot dataSnapshot) {
        int position = getItemCount();
        messages.add(dataSnapshot.getValue(UserMessage.class));
        keys.put(dataSnapshot.getKey(), position);
        notifyDataSetChanged();
    }

    public void changeMessage(DataSnapshot dataSnapshot) {
        int position = keys.get(dataSnapshot.getKey());
        messages.set(position, dataSnapshot.getValue(UserMessage.class));
        notifyDataSetChanged();
    }

    public void removeMessage(DataSnapshot dataSnapshot) {
        int position = keys.get(dataSnapshot.getKey());
        messages.remove(position);
        keys.remove(dataSnapshot.getKey());
        notifyDataSetChanged();
    }

    public void clearMessages() {
        messages.clear();
        keys.clear();
        this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView from, message;

        public ViewHolder(View itemView) {
            super(itemView);
            from = (TextView) itemView.findViewById(R.id.from);
            message = (TextView) itemView.findViewById(R.id.message);
            cardView = (CardView) itemView.findViewById(R.id.cardView);
        }
    }
}