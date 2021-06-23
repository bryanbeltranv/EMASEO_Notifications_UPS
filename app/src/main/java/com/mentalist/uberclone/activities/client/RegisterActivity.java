package com.mentalist.uberclone.activities.client;
import android.app.AlertDialog;
import android.annotation.SuppressLint;
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
import com.mentalist.uberclone.R;
import com.mentalist.uberclone.activities.driver.MapDriverActivity;
import com.mentalist.uberclone.activities.driver.RegisterDriverActivity;
import com.mentalist.uberclone.includes.MyToolbar;
import com.mentalist.uberclone.models.Client;
import com.mentalist.uberclone.providers.AuthProvider;
import com.mentalist.uberclone.providers.ClientProvider;

import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {


    AuthProvider mAuthProvider;
    ClientProvider mClientProvider;

    //VIEWS
    Button mButtonRegister;
    EditText mTextInputEmail;
    EditText mTextInputName;
    EditText mTextInputPassword;
    Toolbar mToolbar;
    AlertDialog mDialog;



    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //ToolBar
        MyToolbar.show(this,"Registro de Usuario", true);

        mAuthProvider = new AuthProvider();
        mClientProvider = new ClientProvider();

        //Dialog de espera
        mDialog = new SpotsDialog.Builder().setContext(RegisterActivity.this).setMessage("Espere un momento").build();

        mButtonRegister = findViewById(R.id.bntRegister);
        mTextInputEmail = findViewById(R.id.textInputEmail);
        mTextInputName = findViewById(R.id.textInputName);
        mTextInputPassword = findViewById(R.id.textInputPassword);

        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRegister();
            }
        });



    }

    private void clickRegister() {
        final String name = mTextInputName.getText().toString();
        final String email = mTextInputEmail.getText().toString();
        String password = mTextInputPassword.getText().toString();

        if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty()){
            if(password.length() >= 6 ){
                mDialog.show();
                register(name, email, password);
            }else{
                Toast.makeText(this, "La longitud minima de la contraseña es de 6 digitos",Toast.LENGTH_LONG).show();

            }

        }else{
            Toast.makeText(this, "Por favor ingrese todos los campos",Toast.LENGTH_LONG).show();

        }
    }

    private void register(final String name, final String email, String password){
        mAuthProvider.register(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if(task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Client client = new Client(id, name, email);
                    create(client);
                }else{
                    Toast.makeText(RegisterActivity.this, "No se registro el usuario",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void create(Client client){
        mClientProvider.create(client).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //Toast.makeText(RegisterActivity.this, "Usuario registrado exitosamente",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this, MapClientActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else{
                    Toast.makeText(RegisterActivity.this, "No se registro el usuario",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
/*
    private void saveUser(String id, String name, String email) {
        String selectedUser =  mPref.getString("user","");
        User user = new User();
        user.setEmail(email);
        user.setName(name);

       // Toast.makeText(this, "El valor seleccionado es: "+ selectedUser,Toast.LENGTH_LONG).show();
        if(selectedUser.equals("driver")){

            mDatabase.child("Users").child("Drivers").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registro exitoso",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "Fallo el registro",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else if (selectedUser.equals("client")){
            mDatabase.child("Users").child("Clients").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registro exitoso",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "Fallo el registro",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }*/
}