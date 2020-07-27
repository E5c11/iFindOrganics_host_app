package adaptersandmore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ifo.userapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import activities.BusinessPage;

import static java.util.Calendar.FRIDAY;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.SATURDAY;
import static java.util.Calendar.SUNDAY;

public class DownloadBusiness {

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Context context;
    private BusinessPage businessPage;
    private String name, wdot, wdct, satot, satct, sunot, sunct;
    private Bitmap bm;
    private ContactsAdapter contactsAdapter;
    private BusProAdapter busProAdapter;
    private ArrayList<String> contactName = new ArrayList<>(), contactInfo = new ArrayList<>();
    private ArrayList<String> food = new ArrayList<>(), med = new ArrayList<>(), well = new ArrayList<>();

    public DownloadBusiness(Context context, String name) {
        this.context = context;
        this.name = name;
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Business Details").child(name);
        storageReference = FirebaseStorage.getInstance().getReference().child("iFindOrganics/Businesses");
        businessPage = new BusinessPage();
    }

    public void getDes(TextView desText) {
        databaseReference.child("businessDes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                desText.setText(dataSnapshot.getValue().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public  void getSlogan(TextView sloganText) {
        databaseReference.child("businessSlogan").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String slogan = dataSnapshot.getValue().toString();
                sloganText.setText(slogan);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getValues(TextView valuesText) {
        databaseReference.child("businessValues").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String values = dataSnapshot.getValue().toString();
                values = values.replace("[", "");
                values = values.replace("]", "");
                valuesText.setText(values);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getTime(TextView openState, TextView timeState) {
        String[] times = {"WeekdaysOpen", "WeekdaysClose", "SaturdayOpen", "SaturdayClose", "SundayOpen", "SundayClose"};
        for (int i = 0; i < times.length; i++) {
            final int j = i;
            final String time = times[i];
            databaseReference.child("businessHours").child(time).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String tempTime = dataSnapshot.getValue().toString();
                    if (time.equals("WeekdaysOpen")) wdot = tempTime;
                    else if (time.equals("WeekdaysClose")) wdct = tempTime;
                    else if (time.equals("SaturdayOpen")) satot = tempTime;
                    else if (time.equals("SaturdayClose")) satct = tempTime;
                    else if (time.equals("SundayOpen")) sunot = tempTime;
                    else {
                        sunct = tempTime;
                        checkTime(openState, timeState);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    public void getLocation(String des, GoogleMap gMap) {
        storageReference.child(name).child("LogoTn.png").getBytes(1024*1024).addOnSuccessListener(bytes -> {
            bm = Bitmap.createScaledBitmap(new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length)).getBitmap(), 100, 100, false);
            databaseReference.child("businessLocation").child("lat").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double lat = Double.parseDouble(dataSnapshot.getValue().toString());
                    databaseReference.child("businessLocation").child("lng").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            double lng = Double.parseDouble(dataSnapshot.getValue().toString());
                            LatLng latLng = new LatLng(lat, lng);
                            if (gMap != null) {
                                gMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(name)
                                        .snippet(des)
                                        .icon(BitmapDescriptorFactory.fromBitmap(bm)));
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11f));
                                databaseReference.child("businessDelivery").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Double radius = Double.parseDouble(dataSnapshot.getValue().toString());
                                            Circle circle = gMap.addCircle(new CircleOptions()
                                                    .center(latLng)
                                                    .radius((radius * 1000))
                                                    .strokeWidth(3)
                                                    .strokeColor(context.getResources().getColor(R.color.colorPrimary))
                                                    .fillColor(Color.argb(50, 102, 153, 51)));
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        });
    }

    public void getLogo(ImageView logoIv) {
        databaseReference.child("businessLogo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Uri logo = Uri.parse(dataSnapshot.getValue().toString());
                Glide.with(context).load(logo).into(logoIv);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getBanner(ImageView bannerIv) {
        databaseReference.child("businessBanner").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Uri banner = Uri.parse(dataSnapshot.getValue().toString());
                Glide.with(context).load(banner).into(bannerIv);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void checkTime(TextView openState, TextView timeState) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String getCurrentTime = sdf.format(c.getTime());
        Log.d("myTag", "" + c.get(Calendar.DAY_OF_WEEK));
        int open = context.getResources().getColor(R.color.colorPrimary),
                close = context.getResources().getColor(R.color.colorPrimaryDark),
                neutral = context.getResources().getColor(R.color.colorPrimaryLight);

        if (SUNDAY == c.get(Calendar.DAY_OF_WEEK)) {
            if (getCurrentTime.compareTo(sunot) > 0 && getCurrentTime.compareTo(sunct) < 0) {
                openState.setText("Open");
                openState.setTextColor(open);
                timeState.setText("Closes at " + sunct);
            } else if (getCurrentTime.compareTo(sunot) < 0) {
                openState.setText("Opens soon ");
                openState.setTextColor(neutral);
                timeState.setText("Opens at " + sunot);
            } else {
                openState.setText("Closed");
                openState.setTextColor(close);
                timeState.setText("Opens at " + wdot);
            }
        } else if (c.get(Calendar.DAY_OF_WEEK) >= MONDAY && c.get(Calendar.DAY_OF_WEEK) < SATURDAY) {
            if (getCurrentTime.compareTo(wdot) > 0 && getCurrentTime.compareTo(wdct) < 0) {
                openState.setText("Open");
                openState.setTextColor(open);
                timeState.setText("Closes at " + wdct);
            } else if (getCurrentTime.compareTo(wdot) < 0) {
                openState.setText("Opens soon");
                openState.setTextColor(neutral);
                timeState.setText("Opens at " + wdot);
            } else {
                openState.setText("Closed");
                openState.setTextColor(close);
                if (c.get(Calendar.DAY_OF_WEEK) == FRIDAY) {
                    timeState.setText("Opens at " + satot);
                }else {
                    timeState.setText("Opens at " + wdot);
                }
            }
        } else {
            if (getCurrentTime.compareTo(satot) > 0 && getCurrentTime.compareTo(satct) < 0) {
                openState.setText("Open");
                openState.setTextColor(open);
                timeState.setText("Closes at " + satct);
            } else if (getCurrentTime.compareTo(satot) < 0) {
                openState.setText("Opens soon");
                openState.setTextColor(neutral);
                timeState.setText("Opens at " + satot);
            } else {
                openState.setText("Closed");
                openState.setTextColor(close);
                timeState.setText("Opens at " + sunot);
            }
        }
    }

    public void getContacts(RecyclerView contactRecycler) {
        DatabaseReference contactReference = databaseReference.child("businessContacts");
        String[] contacts = {"Phone", "Email" , "Whatsapp", "Website", "Facebook", "Instagram", "Twitter"};
        contactsAdapter = new ContactsAdapter(context, contactName, contactInfo);
        for (int i = 0; i < contacts.length; i++) {
            contactReference.child(contacts[i]).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        contactName.add(dataSnapshot.getKey());
                        contactInfo.add(dataSnapshot.getValue().toString());
                    }
                    if (dataSnapshot.getKey().equals("Twitter")) {
                        contactRecycler.setVisibility(View.VISIBLE);
                        contactRecycler.setAdapter(contactsAdapter);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    public void getProducts(RecyclerView foodRecycler, TextView foodIntro, RecyclerView medRecycler, TextView medIntro, RecyclerView cosRecycler, TextView cosIntro) {
        DatabaseReference foodReference = databaseReference.child("businessFood");
        foodReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String foods = dataSnapshot.getValue().toString();
                    foods = foods.replace("[", "");
                    foods = foods.replace("]", "");
                    food = new ArrayList<>(Arrays.asList(foods.split(",")));
                    busProAdapter = new BusProAdapter(context, food, "Food");
                    foodRecycler.setAdapter(busProAdapter);
                    foodRecycler.setVisibility(View.VISIBLE);
                    foodIntro.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        DatabaseReference medReference = databaseReference.child("businessMedicinal");
        medReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String meds = dataSnapshot.getValue().toString();
                    meds = meds.replace("[", "");
                    meds = meds.replace("]", "");
                    med = new ArrayList<>(Arrays.asList(meds.split(",")));
                    busProAdapter = new BusProAdapter(context, med, "Medicinal");
                    medRecycler.setAdapter(busProAdapter);
                    medRecycler.setVisibility(View.VISIBLE);
                    medIntro.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        DatabaseReference wellReference = databaseReference.child("businessCosmetic");
        wellReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String cos = dataSnapshot.getValue().toString();
                    cos = cos.replace("[", "");
                    cos = cos.replace("]", "");
                    well = new ArrayList<>(Arrays.asList(cos.split(",")));
                    busProAdapter = new BusProAdapter(context, well, "Cosmetic");
                    cosRecycler.setAdapter(busProAdapter);
                    cosRecycler.setVisibility(View.VISIBLE);
                    cosIntro.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void getVegDays(TextView vegText, TextView vegIntro) {
        databaseReference.child("businessVegBox").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String veg = dataSnapshot.getValue().toString();
                    veg = veg.replace("[", "");
                    veg = veg.replace("]", "");
                    vegText.append(veg);
                }else {
                    vegText.setText("Service not available");
                    vegText.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void getMarkets(GoogleMap googleMap) {
        databaseReference.child("businessMarkets").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String marketName = ds.getKey();
                    databaseReference.child("businessMarkets").child(marketName).child("lat").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            double lat = Double.parseDouble(dataSnapshot.getValue().toString());
                            databaseReference.child("businessMarkets").child(marketName).child("lng").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    double lng = Double.parseDouble(dataSnapshot.getValue().toString());
                                    BitmapDrawable bitmapdraw = (BitmapDrawable)context.getResources().getDrawable(R.mipmap.marker_logo);
                                    Bitmap b = bitmapdraw.getBitmap();
                                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(lat,lng))
                                            .title(marketName).snippet("Attended market")
                                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
