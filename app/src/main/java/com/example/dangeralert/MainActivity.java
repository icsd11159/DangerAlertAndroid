package com.example.dangeralert;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.content.res.Configuration;
import android.content.res.Resources;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//public static final String EXTRA_MESSAGE = "com.example.movieslover.MESSAGE";


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.movieslover.MESSAGE";
    // Declare references
    private FirebaseAuth mAuth;
    EditText password,singpassword,email,mymailsign,mypasssign;
    TextView wronglogin;
    String userid;
    Spinner spinner;
    Locale myLocale;
    String currentLanguage = "en", currentLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentLanguage = getIntent().getStringExtra(currentLang);

        spinner = (Spinner) findViewById(R.id.spinner);

        List<String> list = new ArrayList<String>();

        list.add("Select language");
        list.add("English");
        list.add("Greek");
        list.add("German");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        setLocale("en");
                        break;
                    case 2:
                        setLocale("gr");
                        break;
                    case 3:
                        setLocale("de");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        password = (EditText) findViewById(R.id.password);
        email = (EditText) findViewById(R.id.email);
        singpassword = (EditText) findViewById(R.id.SignIN);
        //recordsTextView = (TextView) findViewById(R.id.records_TextView);
        wronglogin = (TextView) findViewById(R.id.wronglogin);
        mAuth = FirebaseAuth.getInstance();
        mymailsign = (EditText) findViewById(R.id.SignIN);
        mypasssign = (EditText) findViewById(R.id.passwordsign);
        //dbHandler = new SpeedDBHandler(this, null, null, 4);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //printDatabase();
    }

    public void setLocale(String localeName) {
        if (!localeName.equals(currentLanguage)) {
            myLocale = new Locale(localeName);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            Intent refresh = new Intent(this, MainActivity.class);
            refresh.putExtra(currentLang, localeName);
            startActivity(refresh);
        } else {
            Toast.makeText(MainActivity.this, "Language already selected!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        System.exit(0);
    }
    //add your elements onclick methods.
    //Add a product to the database
    public void loginButtonClicked(View view) {
        // dbHandler.add needs an object parameter.
        final String mail = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        if(!mail.isEmpty() && !pass.isEmpty()){
            mAuth.signInWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                //create the users table;
                                Toast.makeText(getApplicationContext(), "Successfully Logged In", Toast.LENGTH_LONG).show();
                                FirebaseUser user = mAuth.getCurrentUser();

                                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;

                                userid=currentFirebaseUser.getUid();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("userid", userid);
                                startActivity(intent);
                                finish();
                                //updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                //Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                // updateUI(null);
                            }

                            // ...
                        }
                    });
        }
    }

    public void signinButton(View view){
        final  String mail=mymailsign.getText().toString().trim();
        String pass=mypasssign.getText().toString().trim();
        if(!mail.isEmpty() && !pass.isEmpty()){
            mAuth.createUserWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
                                userid=currentFirebaseUser.getUid();
                                final DatabaseReference myRef = database.getReference(userid);
                                myRef.child(userid).push().setValue(mail);
                                Toast.makeText(getApplicationContext(), "Successfully Logged In", Toast.LENGTH_LONG).show();

                                //  updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }


                        }
                    });
        }

    }

}
