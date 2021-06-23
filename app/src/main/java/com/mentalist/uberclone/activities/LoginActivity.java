package com.mentalist.uberclone.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mentalist.uberclone.R;
import com.mentalist.uberclone.activities.client.MapClientActivity;
import com.mentalist.uberclone.activities.client.RegisterActivity;
import com.mentalist.uberclone.activities.driver.MapDriverActivity;
import com.mentalist.uberclone.includes.MyToolbar;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences mPref;

    EditText mTextInputEmail;
    EditText mTextInputPassword;
    Button mButtonLogin;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    AlertDialog mDialog;
    Toolbar mToolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Codigo toolbar
        MyToolbar.show(this,"Login", true);

        mTextInputEmail = findViewById(R.id.textEmail);
        mTextInputPassword = findViewById(R.id.textPassword);
        mButtonLogin = findViewById(R.id.btnLogin);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //Dialog de espera
        mDialog = new SpotsDialog.Builder().setContext(LoginActivity.this).setMessage("Espere un momento").build();

        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);



    }

    private void login() {
        String email = mTextInputEmail.getText().toString();//"prueba2@gmail.com";
        String password = mTextInputPassword.getText().toString();//"12345678";
        if(!email.isEmpty() && !password.isEmpty()){
            mDialog.show();
            if ( password.length() >= 6){
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            String user = mPref.getString("user" ,"");
                            if(user.equals("client")){
                                Intent intent = new Intent(LoginActivity.this, MapClientActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }else{
                                Intent intent = new Intent(LoginActivity.this, MapDriverActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            }
                            Toast.makeText(LoginActivity.this, "El login se realizo exitosamente", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "La contraseña o o el password son incorrectos", Toast.LENGTH_LONG).show();
                        }
                        mDialog.dismiss();
                    }
                });
            }else{
                Toast.makeText(LoginActivity.this, "La contraseña es muy corta minimo 6 caracteres", Toast.LENGTH_LONG).show();

            }
        }

    }
}