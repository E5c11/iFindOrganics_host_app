package com.ifo.userapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import activities.Categories;
import activities.Map;
import activities.Markets;
import activities.StoreDirectory;
import activities.Home;
import adaptersandmore.EndDrawerToggle;

public class BaseActivity extends AppCompatActivity {

    protected ConstraintLayout baseLayout;
    protected ListView drawerList;
    String[] menuList;
    protected static int position;
    protected int page = 0;
    private DrawerLayout drawerLayout;
    private EndDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_main);

        baseLayout = findViewById(R.id.main_frame);
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerList = findViewById(R.id.left_drawer);
        toolbar = findViewById(R.id.toolbar);
        menuList = getResources().getStringArray(R.array.menu);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        drawerList.setAdapter(new ArrayAdapter(this, R.layout.drawer_list, menuList));
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int pos, long id) {
                position = pos;
                openActivity(position);
            }
        });
        actionBarDrawerToggle = new EndDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer){};
        actionBarDrawerToggle.syncState();
    }

    protected void openActivity(int position) {
        drawerLayout.closeDrawer(drawerList);
        switch (position) {
            case 0:
                startActivity(new Intent(this, Home.class));
                break;
            case 1:
                startActivity(new Intent(this, StoreDirectory.class));
                break;
            case 2:
                startActivity(new Intent(this, Map.class));
                break;
            case 3:
                startActivity(new Intent(this, Categories.class));
                break;
            case 4:
                startActivity(new Intent(this, Markets.class));
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(drawerList)){
            drawerLayout.closeDrawer(drawerList);
        }else if(page==0){
            moveTaskToBack(true);
        }else {
            startActivity(new Intent(this, Home.class));
        }
    }
}
