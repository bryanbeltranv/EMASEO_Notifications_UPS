package com.mentalist.uberclone.activities.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mentalist.uberclone.R;
import com.mentalist.uberclone.activities.client.RegisterActivity;
import com.mentalist.uberclone.includes.MyToolbar;
import com.mentalist.uberclone.models.Client;
import com.mentalist.uberclone.models.Driver;
import com.mentalist.uberclone.providers.AuthProvider;
import com.mentalist.uberclone.providers.ClientProvider;
import com.mentalist.uberclone.providers.DriverProvider;

import dmax.dialog.SpotsDialog;

public class RegisterDriverActivity extends AppCompatActivity {

    AuthProvider mAuthProvider;
    DriverProvider mDriverProvider;

    //VIEWS
    Button mButtonRegister;
    EditText mTextInputEmail;
    EditText mTextInputName;
    EditText mTextInputPassword;
    EditText getmTextInputVehiculeBrand;
    EditText getmTextInputVehiculePlate;

    Toolbar mToolbar;
    AlertDialog mDialog;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);
        //ToolBar
        MyToolbar.show(this,"Registro de Conductor", true);

        mAuthProvider = new AuthProvider();
        mDriverProvider = new DriverProvider();

        //Dialog de espera
        mDialog = new SpotsDialog.Builder().setContext(RegisterDriverActivity.this).setMessage("Espere un momento").build();

        mButtonRegister = findViewById(R.id.bntRegister);
        mTextInputEmail = findViewById(R.id.textInputEmail);
        mTextInputName = findViewById(R.id.textInputName);
        mTextInputPassword = findViewById(R.id.textInputPassword);
        getmTextInputVehiculeBrand = findViewById(R.id.textInputVehicleBrand);
        getmTextInputVehiculePlate = findViewById(R.id.textInputVehiclePlate);

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
        final String password = mTextInputPassword.getText().toString();
        final String vehicleBrand = getmTextInputVehiculeBrand.getText().toString();
        final String vehiclePlate = getmTextInputVehiculePlate.getText().toString();

        if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !vehicleBrand.isEmpty()  && !vehiclePlate.isEmpty() ){
            if(password.length() >= 6 ){
                mDialog.show();
                register(name, email, password,vehicleBrand,vehiclePlate);
            }else{
                Toast.makeText(this, "La longitud minima de la contraseña es de 6 digitos",Toast.LENGTH_LONG).show();

            }

        }else{
            Toast.makeText(this, "Por favor ingrese todos los campos",Toast.LENGTH_LONG).show();

        }
    }

    private void register(final String name, final String email, String password,final String vehicleBrand, final String vehiclePlate){
        mAuthProvider.register(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if(task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Driver driver = new Driver(id, name, email,vehicleBrand,vehiclePlate);
                    create(driver);
                }else{
                    Toast.makeText(RegisterDriverActivity.this, "No se registro el usuario",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void create(Driver driver){
        mDriverProvider.create(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //Toast.makeText(RegisterDriverActivity.this, "Usuario registrado exitosamente",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterDriverActivity.this, MapDriverActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else{
                    Toast.makeText(RegisterDriverActivity.this, "No se registro el usuario",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}