package Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.BaseActivity;
import com.example.myapplication.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends BaseActivity {

    private DatabaseReference databaseReference, adReference;
    private LinearLayoutManager layoutManager;
    private RecyclerView adRecyclerView;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    private static final int Request_User_Location_Code = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_main, baseLayout);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Ad details");
        databaseReference.keepSynced(true);

        adRecyclerView = findViewById(R.id.ad_recycler);
        layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        adRecyclerView.setLayoutManager(layoutManager);
        adRecyclerView.setHasFixedSize(true);

        makeAdHorizontal();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkUserLocationPermission();
        }

        final Button button = findViewById(R.id.button);
        final Animation an1 = AnimationUtils.loadAnimation(this, R.anim.rotate_slow);
        //final Animation an2 = AnimationUtils.loadAnimation(this, R.anim.alpha);
        button.startAnimation(an1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                getApplicationContext() .startActivity(intent);
            }
        });

        //Navigation Drawer
        drawerList.setItemChecked(position, true);
        page = 0;
    }

   /* @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<BusinessDetails, AdContentHolder>
                (BusinessDetails.class, R.layout.ad_view, AdContentHolder.class, databaseReference) {
            @Override
            protected void populateViewHolder(final AdContentHolder viewHolder, BusinessDetails model, final int position) {

                viewHolder.setName(model.getZ_businessName());
                viewHolder.setSlogan(model.getZ_businessSlogan());
                viewHolder.setLogo(getApplicationContext(), model.getZ_businessLogo());
                viewHolder.setAdContent(getApplicationContext(), model.getBusinessAdContent());
            }
            @Override
            public AdContentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                AdContentHolder viewHolder =  super.onCreateViewHolder(parent, viewType);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String parentID = firebaseRecyclerAdapter.getRef(adRecyclerView.getChildLayoutPosition(v)).getKey();
                        Log.d("myTagFoods", parentID);
                        adReference = databaseReference.child(parentID).child("z_businessName");
                        adReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String name = dataSnapshot.getValue(String.class);
                                String dataID = name;
                                Log.d("myTagFoods", dataID);
                                Intent intent = new Intent(getApplicationContext(), BusinessPage.class);
                                intent.putExtra("dataURL", dataID);
                                getApplicationContext() .startActivity(intent);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });
                return viewHolder;
            }
        };
        adRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }*/

    public static class AdContentHolder extends RecyclerView.ViewHolder {
        View view;
        public  AdContentHolder(View itemView) {
            super(itemView);
            view = itemView;
        }
        public void setName(String businessName) {
            TextView post_business_name = view.findViewById(R.id.business_name);
            post_business_name.setText(businessName);
        }
        public void setSlogan(String businessSlogan) {
            TextView post_business_slogan = view.findViewById(R.id.business_slogan);
            post_business_slogan.setText(businessSlogan);
        }
        public  void setLogo(Context context, String businessLogo) {
            CircleImageView post_business_logo = view.findViewById(R.id.business_logo);
            Glide.with(context).load(businessLogo).into(post_business_logo);
        }
        public  void setAdContent(Context context, String adContent) {
            ImageView post_ad_content = view.findViewById(R.id.ad_content);
            Glide.with(context).load(adContent).into(post_ad_content);
        }
    }

    public void makeAdHorizontal(){
        LinearSnapHelper snapHelper = new LinearSnapHelper() {
            @Override
            public int findTargetSnapPosition(RecyclerView.LayoutManager lm, int velocityX, int velocityY) {
                View centerView = findSnapView(lm);
                if (centerView == null)
                    return RecyclerView.NO_POSITION;

                int position = lm.getPosition(centerView);
                int targetPosition = -1;
                if (lm.canScrollHorizontally()) {
                    if (velocityX < 0) {
                        targetPosition = position - 1;
                    } else {
                        targetPosition = position + 1;
                    }
                }

                if (lm.canScrollVertically()) {
                    if (velocityY < 0) {
                        targetPosition = position - 1;
                    } else {
                        targetPosition = position + 1;
                    }
                }

                final int firstItem = 0;
                final int lastItem = lm.getItemCount() - 1;
                targetPosition = Math.min(lastItem, Math.max(targetPosition, firstItem));
                return targetPosition;
            }
        };
        snapHelper.attachToRecyclerView(adRecyclerView);
    }

    public boolean checkUserLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            return false;
        } else {

            return true;
        }
    }
}
