package com.mentalist.uberclone.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mentalist.uberclone.R;
import com.mentalist.uberclone.activities.client.RegisterActivity;
import com.mentalist.uberclone.activities.driver.RegisterDriverActivity;
import com.mentalist.uberclone.includes.MyToolbar;

public class SelectOptionAuthActivity extends AppCompatActivity {

    Toolbar mToolbar;
    Button mButtonGotoLogin, mButtonGotoRegister;
    SharedPreferences mPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option_auth);
        //Toolbar
        MyToolbar.show(this,"Selecciona una opción", true);

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        mButtonGotoLogin = findViewById(R.id.btnGoToLogin);
        mButtonGotoRegister = findViewById(R.id.btnGotoRegister);

        mButtonGotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });
        mButtonGotoRegister.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View v) {
           goToRegister();
        }
        });

    }

    private void goToLogin() {
        Intent intent = new Intent(SelectOptionAuthActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void goToRegister() {
        String typeUser = mPref.getString("user" ,"");
        if(typeUser.equals("client")){
            Intent intent = new Intent(SelectOptionAuthActivity.this, RegisterActivity.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(SelectOptionAuthActivity.this, RegisterDriverActivity.class);
            startActivity(intent);
        }

    }
}