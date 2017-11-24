package com.agos.df2017;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.agos.df2017.entities.Message;
import com.agos.df2017.entities.User;
import com.agos.df2017.fcm.Sender;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Chat extends AppCompatActivity {

    private FirebaseAnalytics firebaseAnalytics;

    private FirebaseUser user;
    private User destiny;

    private FirebaseFirestore fireStore;

    private List<Message> messages = new ArrayList<>();

    private ListView list;

    private CollectionReference referenceSource;
    private CollectionReference referenceDestiny;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle params = new Bundle();
        params.putString("Activity", "Chat");
        params.putString("Params", "");
        firebaseAnalytics.logEvent("Load", params);

        user = FirebaseAuth.getInstance().getCurrentUser();
        destiny = (User) getIntent().getExtras().getSerializable("user");

        getSupportActionBar().setTitle(destiny.getName());

        //Firestore
        fireStore = FirebaseFirestore.getInstance();
        referenceSource = fireStore.collection("users").document(user.getUid()).collection("messages").document(destiny.getId()).collection("chats");
        referenceDestiny = fireStore.collection("users").document(destiny.getId()).collection("messages").document(user.getUid()).collection("chats");
        if (destiny.getId().equals("0")) {
            referenceSource = fireStore.collection("chats");
        }

        referenceSource.orderBy("time", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                if (snapshots != null) {
                    for (DocumentChange document : snapshots.getDocumentChanges()) {
                        if (document.getType().equals(DocumentChange.Type.ADDED)) {
                            messages.add(document.getDocument().toObject(Message.class));
                        }
                    }
                    ((MessageAdapter) list.getAdapter()).notifyDataSetChanged();
                }
            }
        });

        MessageAdapter adapter = new MessageAdapter(Chat.this, messages);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);

        final EditText txtMessage = (EditText) findViewById(R.id.message);

        ((Button) findViewById(R.id.send)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtMessage.getText().toString() != "" && txtMessage.getText().toString().length() > 1) {
                    final Message message = new Message();
                    message.setTime(new Date().getTime());
                    message.setDate(new Date());
                    message.setUser(user.getDisplayName());
                    message.setDestinyId(destiny.getId());
                    message.setOriginId(user.getUid());
                    message.setMessage(txtMessage.getText().toString());
                    referenceSource.add(message);
                    if (!(destiny.getId().equals("0"))) {
                        referenceDestiny.add(message);
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Sender.post(user.getDisplayName() + " ha escrito", message.getMessage(), destiny.getToken(), user.getUid());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                    } else {
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Sender.post(user.getDisplayName() + " ha escrito", message.getMessage(), "0");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                    }
                    txtMessage.setText("");
                }
            }
        });
    }

    public class MessageAdapter extends ArrayAdapter<Message> {
        public MessageAdapter(Context context, List<Message> messages) {
            super(context, 0, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Message message = getItem(position);
            if (message.getOriginId().equals(user.getUid())) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_item_right, parent, false);
            } else {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_item, parent, false);
            }
            TextView tvMessage = (TextView) convertView.findViewById(R.id.message);
            TextView tvUserName = (TextView) convertView.findViewById(R.id.userName);
            TextView tvDate = (TextView) convertView.findViewById(R.id.date);
            tvMessage.setText(message.getMessage());
            tvUserName.setText(message.getUser());
            tvDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(message.getDate()));
            return convertView;
        }
    }
}
