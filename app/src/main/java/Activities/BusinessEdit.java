package activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ifo.userapp.BaseActivity;
import com.ifo.userapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


import adaptersandmore.BusProAdapter;
import adaptersandmore.BusinessDetails;
import adaptersandmore.ContactsAdapter;
import adaptersandmore.DownloadBusiness;
import de.hdodenhof.circleimageview.CircleImageView;

import static java.util.Calendar.FRIDAY;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.SATURDAY;
import static java.util.Calendar.SUNDAY;

public class BusinessPage extends BaseActivity implements OnMapReadyCallback {

    private MotionLayout bus_layout;
    private FloatingActionButton register, next, back;
    private MapView busMap;
    private static GoogleMap gMap;
    private Bundle mapViewBundle;
    private LatLng capeTown = new LatLng(-34.02289, 18.45532), bPosition;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private Button about, contact, products, shop;
    private TextView aboutDes, valueIntro, valueList, openState, timeState, foodIntro, medIntro, cosIntro, shopIntro,veggieBoxService, veggieBox;
    private RecyclerView contactList, foodList, medList, cosList;
    private ConstraintLayout aboutLayout, contactLayout, productLayout, shopLayout;
    private ConstraintSet constraintSet;
    private int height = 100;
    private int width = 100;
    private TextView bName, bSlogan;
    private CircleImageView bLogo;
    private ImageView bBanner, deliveryIcon;
    private String tempValues, tempVeggieBox;
    private BusProAdapter busProAdapter;
    private ContactsAdapter contactsAdapter;
    private Bitmap logo;
    private LayerDrawable layerLogo;
    private BusinessDetails checkBus;
    private String busName, busDes;
    private DownloadBusiness downloadBusiness;
    private Drawable bitLogo;
    private byte[] getLogo;
    //private DatabaseReference databaseReference;

    public BusinessPage() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.business_activity, baseLayout);

        Log.d("myTag", "Review");

        bus_layout = findViewById(R.id.business_review);

        register = findViewById(R.id.floating_submit);

        busMap = findViewById(R.id.bus_map_view);

        busMap.onCreate(mapViewBundle);
        busMap.getMapAsync(this);

        about = findViewById(R.id.bus_about);
        //aboutLayout = findViewById(R.id.about_layout);
        aboutDes = findViewById(R.id.bus_des);
        valueIntro = findViewById(R.id.bus_value_intro);
        valueList = findViewById(R.id.bus_values);
        openState = findViewById(R.id.open_state);
        timeState = findViewById(R.id.time_state);

        contact = findViewById(R.id.bus_contact);
        //contactLayout = findViewById(R.id.contact_layout);
        contactList = findViewById(R.id.contact_recycler);

        products = findViewById(R.id.bus_products);
        productLayout = findViewById(R.id.products_layout);
        foodIntro = findViewById(R.id.food_products);
        medIntro = findViewById(R.id.medicinal_products);
        cosIntro = findViewById(R.id.cosmetic_products);
        foodList = findViewById(R.id.food_list);
        medList = findViewById(R.id.medicinal_list);
        cosList = findViewById(R.id.cosmetic_list);
        veggieBoxService = findViewById(R.id.veggiebox_products);
        veggieBox = findViewById(R.id.veggiebox_days);
        deliveryIcon = findViewById(R.id.delivery_icon);

        shop = findViewById(R.id.bus_shop);
        //shopLayout = findViewById(R.id.shop_layout);
        shopIntro = findViewById(R.id.shop_text);

        bName = findViewById(R.id.bus_name);
        bSlogan = findViewById(R.id.bus_slogan);
        bLogo = findViewById(R.id.floating_logo);
        layerLogo = (LayerDrawable) getResources().getDrawable(R.drawable.round_frame);
        bBanner = findViewById(R.id.bus_banner);

        mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        busMap.onCreate(mapViewBundle);
        busMap.getMapAsync(this);

        setToolbar();
        setButtonListeners();
    }

    public void setToolbar() {
        Intent getTb = getIntent();
        busName = getTb.getStringExtra("name");
        bName.setText(busName);
        downloadBusiness = new DownloadBusiness(this, busName);
        if (getTb.hasExtra("des")) {
            busDes = getTb.getStringExtra("des");
            aboutDes.setText(busDes);
        }
        else downloadBusiness.getDes(aboutDes);
        downloadBusiness.getLogo(bLogo);
        downloadBusiness.getSlogan(bSlogan);
        downloadBusiness.getValues(valueList);
        downloadBusiness.getTime(openState, timeState);
    }

    public void aboutClicked() {
        about.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryTrans));
        contact.setBackgroundTintList(getResources().getColorStateList(R.color.quantum_grey300));
        products.setBackgroundTintList(getResources().getColorStateList(R.color.quantum_grey300));
        shop.setBackgroundTintList(getResources().getColorStateList(R.color.quantum_grey300));
        aboutVisibility(View.VISIBLE);
        contactVisibility(View.GONE);
        productVisibility(View.GONE);
        shopVisibility(View.GONE);
    }

    public void contactClicked() {
        if (contactList.getAdapter() == null) {
            contactList.setLayoutManager(new LinearLayoutManager(this));
            downloadBusiness.getContacts(contactList);
        }
        contact.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryTrans));
        about.setBackgroundTintList(getResources().getColorStateList(R.color.quantum_grey300));
        products.setBackgroundTintList(getResources().getColorStateList(R.color.quantum_grey300));
        shop.setBackgroundTintList(getResources().getColorStateList(R.color.quantum_grey300));
        contactVisibility(View.VISIBLE);
        aboutVisibility(View.GONE);
        productVisibility(View.GONE);
        shopVisibility(View.GONE);
    }

    public void productsClicked() {
        if (foodList.getAdapter() == null && medList.getAdapter() == null && cosList.getAdapter() == null) {
            downloadBusiness.getVegDays(veggieBox, veggieBoxService);
            foodList.setLayoutManager(new LinearLayoutManager(this));
            medList.setLayoutManager(new LinearLayoutManager(this));
            cosList.setLayoutManager(new LinearLayoutManager(this));
            downloadBusiness.getProducts(foodList, foodIntro, medList, medIntro, cosList, cosIntro);
        }
        bus_layout.transitionToEnd();
        products.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryTrans));
        contact.setBackgroundTintList(getResources().getColorStateList(R.color.quantum_grey300));
        about.setBackgroundTintList(getResources().getColorStateList(R.color.quantum_grey300));
        shop.setBackgroundTintList(getResources().getColorStateList(R.color.quantum_grey300));
        productVisibility(View.VISIBLE);
        contactVisibility(View.GONE);
        aboutVisibility(View.GONE);
        shopVisibility(View.GONE);
    }

    public void shopClicked() {
        shop.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimaryTrans));
        contact.setBackgroundTintList(getResources().getColorStateList(R.color.quantum_grey300));
        products.setBackgroundTintList(getResources().getColorStateList(R.color.quantum_grey300));
        about.setBackgroundTintList(getResources().getColorStateList(R.color.quantum_grey300));
        shopVisibility(View.VISIBLE);
        contactVisibility(View.GONE);
        productVisibility(View.GONE);
        aboutVisibility(View.GONE);
    }

    public void setButtonListeners() {
        about.setOnClickListener(v -> aboutClicked());
        about.performClick();
        contact.setOnClickListener(v -> contactClicked());
        products.setOnClickListener(v -> productsClicked());
        shop.setOnClickListener(v -> shopClicked());
    }

    public void aboutVisibility(int vis) {
        aboutDes.setVisibility(vis);
        valueIntro.setVisibility(vis);
        valueList.setVisibility(vis);
    }
    public void contactVisibility(int vis) {
        contactList.setVisibility(vis);
    }
    public void productVisibility(int vis) {
        if (foodList.getAdapter() != null) {
            foodIntro.setVisibility(vis);
            foodList.setVisibility(vis);
        }
        if (medList.getAdapter() != null) {
            medIntro.setVisibility(vis);
            medList.setVisibility(vis);
        }
        if (cosList.getAdapter() != null) {
            cosIntro.setVisibility(vis);
            cosList.setVisibility(vis);
        }
        veggieBoxService.setVisibility(vis);
        veggieBox.setVisibility(vis);
        deliveryIcon.setVisibility(vis);
    }
    public void shopVisibility(int vis) {
        shopIntro.setVisibility(vis);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        downloadBusiness.getBanner(bBanner);
        UiSettings uiSettings = gMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        gMap.setMyLocationEnabled(true);
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(capeTown, 10.75f));
        downloadBusiness.getLocation(busDes, gMap);
        downloadBusiness.getMarkets(gMap);

        View locationButton = ((View) busMap.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams clp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        clp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        clp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        clp.setMargins(0, 180, 180, 0);


        busMap.onResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("myTag","restart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("myTag","destroy");
    }

}
