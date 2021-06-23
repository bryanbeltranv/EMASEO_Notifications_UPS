package com.mentalist.uberclone.includes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mentalist.uberclone.R;

public class MyToolbar {
    public static void show(AppCompatActivity activity, String title, Boolean upButton ){
        //Codigo toolbar
        Toolbar mToolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(mToolbar);
        activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
        //Fin Codigo Toolbar
    }
}
