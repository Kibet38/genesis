package kibe.dev.genesis.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.data.DataBufferObserverSet;
import com.google.android.gms.common.data.DataBufferSafeParcelable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kibe.dev.genesis.Activities.LoginActivity;
import kibe.dev.genesis.Activities.PostActivity;
import kibe.dev.genesis.Adapters.MyFotoAdapter;
import kibe.dev.genesis.Models.Post;
import kibe.dev.genesis.Models.User;
import kibe.dev.genesis.R;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ProfileFragment extends Fragment {

    ImageView back, signOut, pick;
    CircleImageView image_profile;
    TextView username, category, following_no, followers_no, posts;
    TextView bio = null;
    Button edit_profile;

    RecyclerView recyclerView;
    MyFotoAdapter myFotoAdapter;
    List<Post> postList;

    FirebaseUser firebaseUser;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    String profileid;


 @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
     View view = inflater.inflate(R.layout.fragment_profile, container, false);

     firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

     SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
     profileid = prefs.getString("profileid", "none");

     back = view.findViewById(R.id.back);
     signOut = view.findViewById(R.id.options);
     image_profile = view.findViewById(R.id.image_profile);
     pick = view.findViewById(R.id.pick);
     username = view.findViewById(R.id.username);
     category = view.findViewById(R.id.category);
     posts = view.findViewById(R.id.posts);
     following_no = view.findViewById(R.id.following_no);
     followers_no = view.findViewById(R.id.followers_no);
     edit_profile = view.findViewById(R.id.edit_profile);

     signOut.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             Log.d(TAG, "onClick: attempting to sign out");
             FirebaseAuth.getInstance().signOut();
         }
     });

     pick.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             startActivity(new Intent(getActivity(), PostActivity.class));

         }
     });

     recyclerView = view.findViewById(R.id.home_recycler_view);
     recyclerView.setHasFixedSize(true);
     LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
     recyclerView.setLayoutManager(linearLayoutManager);
     postList = new ArrayList<>();
     myFotoAdapter = new MyFotoAdapter(getContext(), postList);
     recyclerView.setAdapter(myFotoAdapter);


     userInfo();
     getFollowers();
     getNrPost();
     myFotos();
     setupFirebaseListener();

     if (profileid.equals(firebaseUser.getUid())){
         edit_profile.setText("Edit Profile");
     } else {
         checkFollow();
     }


     edit_profile.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             String btn = edit_profile.getText().toString();

             if (btn.equals("Edit Profile")){
                 //go to edit profile
             } else if (btn.equals("Follow")){
                 FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                         .child("Following").child(profileid).setValue(true);
                 FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                         .child("Followers").child(firebaseUser.getUid()).setValue(true);
             } else if (btn.equals("Following")){

                 FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                         .child("following").child(profileid).removeValue();
                 FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                         .child("followers").child(firebaseUser.getUid()).removeValue();

             }
         }
     });

     return view;
    }

    private void userInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getContext() == null) {
                    return;
                }

                User user = dataSnapshot.getValue(User.class);

//                try{
//                    Glide.with(getContext()).load(user.getImageUrl()).into(image_profile);
//                    username.setText(user.getUsername());
//                    bio.setText(user.getBio());
//                }catch(NullPointerException e){
//                    Log.e(TAG, e.toString());
//                }
////
////
////                if (user.getImageUrl() != null){
////                    Glide.with(getContext()).load(user.getImageUrl()).into(image_profile);
////                }
                Glide.with(getContext()).load(user.getImageUrl()).into(image_profile);
                username.setText(user.getUsername());
                //bio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("Following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(profileid).exists()){
                    edit_profile.setText("Following");
                } else {
                    edit_profile.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowers() {
     DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
             .child("Follow").child(profileid).child("Followers");
     reference.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             followers_no.setText( "Followers: " + dataSnapshot.getChildrenCount());
         }

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

         }
     });
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileid).child("Following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                following_no.setText("Following: "+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getNrPost() {
     DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts");
     reference.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             int i = 0;
             for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                 Post post = snapshot.getValue(Post.class);
                 if (post.getPublisher().equals(profileid)) {
                     i++;
                 }
             }
             posts.setText("Posts: "+ i);

         }

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

         }
     });
    }

    private void myFotos() {
     DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
     reference.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             postList.clear();
             for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                 Post post = snapshot.getValue(Post.class);
                 if (post.getPublisher().equals(profileid)) {
                     postList.add(post);
                 }
             }
             Collections.reverse(postList);
             myFotoAdapter.notifyDataSetChanged();
         }

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

         }
     });
    }

    private void setupFirebaseListener() {
        Log.d(TAG, "setupFirebaseListener: setting up auth state listener");
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged: Signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    Toast.makeText(getActivity(), "Signed Out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        }
    }
}
