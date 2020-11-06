package com.example.dangeralert;

import android.app.Application;
import android.content.Intent;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
      if(currentFirebaseUser!=null){
          startActivity(new Intent(Home.this,Menu.class));
      }

    }
}
