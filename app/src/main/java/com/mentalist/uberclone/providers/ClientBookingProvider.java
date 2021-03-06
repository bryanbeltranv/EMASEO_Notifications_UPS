package com.mentalist.uberclone.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mentalist.uberclone.models.ClientBooking;

import java.util.HashMap;
import java.util.Map;

public class ClientBookingProvider {

    private DatabaseReference mDatabase;

        public ClientBookingProvider(){
            mDatabase = FirebaseDatabase.getInstance().getReference().child("ClientBooking");

        }
        public Task<Void> create(ClientBooking clientBooking){
            return mDatabase.child(clientBooking.getIdClient()).setValue(clientBooking);

        }

        public Task<Void> updateStatus(String idClientBooking, String status){
            Map<String,Object> map = new HashMap<>();
            map.put("status",status);
            return mDatabase.child(idClientBooking).updateChildren(map);
        }

}

