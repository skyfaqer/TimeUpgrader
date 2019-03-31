package com.example.timeupgrader;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
// import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class MainFragment extends Fragment {
    private FloatingActionButton fab;
    private RecyclerView mRecyclerView;
    private MainAdapter adapter;
    private List<SingleAct> mData;
    private TaskDatabaseHelper dbHelper;
    private DatabaseReference mDatabase;
    private FireBaseHelper fbHelper;

    public MainFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "in Main onCreateView called!!!");
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        dbHelper = new TaskDatabaseHelper(getContext());
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mData = new ArrayList<>();
        mRecyclerView = v.findViewById(R.id.mainRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        final User u = User.getCurrentUser();
        Log.i("uEmail", u != null ? u.getEmail() : "");
        Log.i("curEmail", Email.getCurrentEmail().getEmail());
        Log.i(TAG, "in Main begin query database!!!");
        if (ConnectionUtils.isConn(getContext())) {
            Query query;
            if (u != null && u.getEmail().equals(Email.getCurrentEmail().getEmail())) {
                query = mDatabase.child("userAct").child(u.getEmail().replace('.', ','));
            } else {
                query = mDatabase.child("userAct").child(Email.getCurrentEmail().getEmail().replace('.', ','));
            }
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mData.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("status").getValue(Integer.class) < SingleAct.PAUSE) {
                            mData.add(new SingleAct(snapshot.child("id").getValue().toString(),
                                    snapshot.child("name").getValue().toString(),
                                    snapshot.child("description").getValue().toString(),
                                    snapshot.child("type").getValue(Integer.class),
                                    (long) snapshot.child("startTime").getValue(),
                                    (boolean) snapshot.child("notify").getValue(),
                                    (boolean) snapshot.child("timing").getValue(),
                                    (long) snapshot.child("rewardPoint").getValue(),
                                    snapshot.child("owner").getValue().toString(),
                                    snapshot.child("status").getValue(Integer.class),
                                    (long) snapshot.child("duration").getValue(),
                                    (long) snapshot.child("currentTime").getValue(),
                                    (boolean) snapshot.child("synced").getValue()));
                        }
                    }
                    Collections.sort(mData, new Comparator<SingleAct>() {
                        public int compare(SingleAct o1, SingleAct o2) {
                            return Long.compare(o1.getStartTime(), o2.getStartTime());
                        }
                    });
                    adapter = new MainAdapter(mData, getContext());
                    mRecyclerView.setAdapter(adapter);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Toast.makeText(getContext(), "Firebase error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    mData = dbHelper.loadActivityByStatus(u != null ? u.getEmail() : Email.getCurrentEmail().getEmail(), new int[]{SingleAct.SET, SingleAct.START});
                    Collections.sort(mData, new Comparator<SingleAct>() {
                        public int compare(SingleAct o1, SingleAct o2) {
                            return Long.compare(o1.getStartTime(), o2.getStartTime());
                        }
                    });
                    adapter = new MainAdapter(mData, getContext());
                    mRecyclerView.setAdapter(adapter);
                }
            });
        }
        else {
            mData = dbHelper.loadActivityByStatus(u != null ? u.getEmail() : Email.getCurrentEmail().getEmail(), new int[]{SingleAct.SET, SingleAct.START});
            if (mData == null || mData.size() == 0) {
                Toast.makeText(getContext(), "No local data, please check your network connection, then sync your data in More.", Toast.LENGTH_LONG).show();
            }
            else {
                Collections.sort(mData, new Comparator<SingleAct>() {
                    public int compare(SingleAct o1, SingleAct o2) {
                        return Long.compare(o1.getStartTime(), o2.getStartTime());
                    }
                });
            }
            adapter = new MainAdapter(mData, getContext());
            mRecyclerView.setAdapter(adapter);
        }
        Log.i(TAG, "in Main finish query database!!!");
        /*adapter = new MainAdapter(mData);
        mRecyclerView.setAdapter(adapter);*/

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "in Main onActivityCreated called!!!");

        fab = getActivity().findViewById(R.id.addactivity);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ViewActivity.class));
            }
        });
    }

    private void showNormalDialog(){
        Log.i(TAG, "in Main showNormalDialog called!!!");
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(getActivity());
        normalDialog.setTitle("Exit");
        normalDialog.setMessage("Are you sure to exit?");
        normalDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getActivity().finish();
            }
        });
        normalDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        normalDialog.create().show();
    }

    /*private void fetch() {
        User u = User.getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference().child("userAct").child(u.getEmail().replace('.', ','))
                .endAt(SingleAct.END, "status").orderByChild("startTime");

        FirebaseRecyclerOptions<SingleAct> options =
                new FirebaseRecyclerOptions.Builder<SingleAct>().setQuery(query, new SnapshotParser<SingleAct>() {
                    @NonNull
                    @Override
                    public SingleAct parseSnapshot(@NonNull DataSnapshot snapshot) {
                        if (snapshot == null) return new SingleAct("","","",0,0,false,false,0,"",-1,0,0,false);
                        return new SingleAct(snapshot.child("id").getValue().toString(),
                                snapshot.child("name").getValue().toString(),
                                snapshot.child("description").getValue().toString(),
                                (int) snapshot.child("type").getValue(),
                                (long) snapshot.child("startTime").getValue(),
                                (boolean) snapshot.child("notify").getValue(),
                                (boolean) snapshot.child("isTiming").getValue(),
                                (long) snapshot.child("rewardPoint").getValue(),
                                snapshot.child("owner").getValue().toString(),
                                (int) snapshot.child("status").getValue(),
                                (long) snapshot.child("duration").getValue(),
                                (long) snapshot.child("currentTime").getValue(),
                                (boolean) snapshot.child("synced").getValue());
                    }
                }).build();

        adapter = new FirebaseRecyclerAdapter<SingleAct, ViewHolder>(options) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_main, parent, false);
                return new ViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(ViewHolder holder, final int position, SingleAct act) {
                if (act.getId().equals("")) return;
                holder.name.setText(act.getName());
                holder.description.setText(act.getDescription());
                holder.status.setText(act.getStatus());
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat sdf2 = new SimpleDateFormat("HH hr mm min ss sec");
                holder.startTime.setText(sdf1.format(act.getStartTime()));
                holder.duration.setText(sdf2.format(act.getDuration()));
                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {}
                });
            }
        };

        mRecyclerView.setAdapter(adapter);
    }*/

    @Override
    public void onStart() {
        super.onStart();
        //adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        //adapter.stopListening();
    }
}
