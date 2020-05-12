package kibe.dev.genesis.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.List;

import kibe.dev.genesis.Activities.PostActivity;
import kibe.dev.genesis.Fragment.HomeFragment;
import kibe.dev.genesis.Models.Post;
import kibe.dev.genesis.Models.User;
import kibe.dev.genesis.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public Context mContext;
    public List<Post> mPost;

    private FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mPost.get(position);

        Glide.with(mContext).load(post.getPostimage()).into(holder.post_image);

        if (post.getDescription().equals("") && post.getPost_date().equals("")) {
            holder.description.setVisibility(View.GONE);
            holder.post_date.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.post_date.setVisibility(View.VISIBLE);
            holder.description.setText(post.getDescription());
            holder.post_date.setText(post.getPost_date());
        }

        publisherInfo(holder.image_profile, holder.publisher, holder.post_date, post.getPublisher());

    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile, post_image, cart;
        public TextView publisher, description, post_date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            post_date = itemView.findViewById(R.id.post_date);
            cart = itemView.findViewById(R.id.cart);
            publisher = itemView.findViewById(R.id.publisher);
            description = itemView.findViewById(R.id.description);

        }
    }

    private void publisherInfo(final ImageView image_profile, final TextView publisher, final TextView post_date, String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            private User post;
            private Object Timestamp = ServerValue.TIMESTAMP;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageUrl()).into(image_profile);
                publisher.setText(user.getUsername());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
