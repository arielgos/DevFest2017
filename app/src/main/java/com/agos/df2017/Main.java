package com.agos.df2017;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.agos.df2017.entities.User;
import com.agos.df2017.fcm.Sender;
import com.androidquery.AQuery;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAnalytics firebaseAnalytics;

    private FirebaseRemoteConfig firebaseRemoteConfig;

    private FirebaseAuth auth;

    private FirebaseUser firebaseUser;

    private AQuery aquery;

    private GoogleApiClient googleApiClient;

    private FirebaseFirestore fireStore;

    private List<User> users = new ArrayList<>();

    private ListView list;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String uid = intent.getExtras().getString("user");
        if (uid.equals("0")) {
            User group = new User();
            group.setId("0");
            group.setName("GDG Santa Cruz");
            group.setEmail("mail@gdgsantacruz.org");
            group.setPhoto("https://lh6.googleusercontent.com/-fe6PusUqlWI/AAAAAAAAAAI/AAAAAAAAA4E/cMmA69H3GuU/photo.jpg");
            openChat(group);
        } else {
            FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
            DocumentReference reference = fireStore.collection("users").document(uid);
            reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    User user = documentSnapshot.toObject(User.class);
                    openChat(user);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle params = new Bundle();
        params.putString("Activity", "Main");
        params.putString("Params", "");
        firebaseAnalytics.logEvent("Load", params);

        //Firebase Remote Config
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        firebaseRemoteConfig.fetch(1).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                firebaseRemoteConfig.activateFetched();
                getSupportActionBar().setTitle(firebaseRemoteConfig.getString("app_name"));
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        //Firebase Auth
        auth = FirebaseAuth.getInstance();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        aquery = new AQuery(this);
        aquery.id(R.id.profile_image).image(firebaseUser.getPhotoUrl().toString());

        ((TextView) findViewById(R.id.profile_name)).setText(firebaseUser.getDisplayName());

        ((Button) findViewById(R.id.close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });


        //cargamos el grupo
        User group = new User();
        group.setId("0");
        group.setName("GDG Santa Cruz");
        group.setEmail("mail@gdgsantacruz.org");
        group.setPhoto("https://lh6.googleusercontent.com/-fe6PusUqlWI/AAAAAAAAAAI/AAAAAAAAA4E/cMmA69H3GuU/photo.jpg");
        users.add(group);

        UserAdapter adapter = new UserAdapter(Main.this, users);
        list = (ListView) findViewById(R.id.users);
        list.setAdapter(adapter);

        EditText filter = (EditText) findViewById(R.id.filter);

        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ((UserAdapter) list.getAdapter()).getFilter().filter(charSequence.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = (User) list.getAdapter().getItem(i);
                openChat(user);
            }
        });

        //Firestore
        fireStore = FirebaseFirestore.getInstance();
        CollectionReference reference = fireStore.collection("users");
        reference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                if (snapshots != null) {
                    for (DocumentChange document : snapshots.getDocumentChanges()) {
                        if (document.getType().equals(DocumentChange.Type.ADDED)) {
                            User user = document.getDocument().toObject(User.class);
                            if (!(firebaseUser.getUid().equals(user.getId()))) {
                                users.add(user);
                            }
                        }
                    }
                    ((UserAdapter) list.getAdapter()).notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void signOut() {
        // Firebase sign out
        auth.signOut();
        // Google sign out
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                startActivity(new Intent(Main.this, Register.class));
                finish();
            }
        });
    }

    public class UserAdapter extends ArrayAdapter<User> {

        private ItemFilter filter = new ItemFilter();

        public UserAdapter(Context context, List<User> messages) {
            super(context, 0, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            User user = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_item, parent, false);
            }

            TextView profileName = (TextView) convertView.findViewById(R.id.profile_name);
            ImageView profileImage = (ImageView) convertView.findViewById(R.id.profile_image);
            profileName.setText(user.getName());
            aquery.id(profileImage).image(user.getPhoto());
            return convertView;
        }

        public Filter getFilter() {
            return filter;
        }

        private class ItemFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                String filterString = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();
                int count = users.size();
                final ArrayList<User> nlist = new ArrayList<User>(count);

                for (int i = 0; i < count; i++) {
                    if (users.get(i).getEmail().toLowerCase().contains(filterString) || users.get(i).getName().toLowerCase().contains(filterString)) {
                        nlist.add(users.get(i));
                    }
                }
                results.values = nlist;
                results.count = nlist.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                list.setAdapter(new UserAdapter(Main.this, (ArrayList<User>) results.values));
            }
        }
    }

    private void openChat(User user) {
        Intent intent = new Intent(Main.this, Chat.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }
}
