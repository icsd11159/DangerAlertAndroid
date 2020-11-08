package com.example.dangeralert;

import android.Manifest;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.app.ProgressDialog;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class Menu extends AppCompatActivity  implements SensorEventListener {
    TextView mymail, myspeed,mycurrentspeed,count;
    boolean Aborded=false,setV=false;
    SensorManager sensorManager;
    TextView myTextView;
    MediaPlayer mp=null;
    Sensor accelerometer;

    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    public final String userid=currentFirebaseUser.getUid();
    String email,phoneNo1,phoneNo2,phoneNo3;
    private GpsTracker gpsTracker;
    private TextView tvLatitude,tvLongitude;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Phone1 = "Phone1key";
    public static final String Phone2 = "Phone2key";
    public static final String Phone3 = "Phone3key";
    public boolean start=true;
    EditText ed1,ed2,ed3;
    Button b1;
    SharedPreferences sharedpreferences;
    // views for button
    private Button btnSelect, btnUpload,clear,abord;
    SmsManager smsManager ;
    ArrayList<String> msgArray ;
    double latitude,longitude;
    // view for image view
    private ImageView imageView;
    private final static String default_notification_channel_id = "default";
    // Uri indicates, where the image will be picked from
    private Uri filePath;
     public int ab;
    // request code
    private final int PICK_IMAGE_REQUEST = 22;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;
    String message;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

       // mymail = (TextView) findViewById(R.id.mail);
        myspeed = (TextView) findViewById(R.id.myspeed);
        mycurrentspeed = (TextView) findViewById(R.id.mycurrentspeed);
        count = (TextView) findViewById(R.id.count);
        //count.setVisibility(View.INVISIBLE);
        ed1=(EditText)findViewById(R.id.editText);
        ed2=(EditText)findViewById(R.id.editText2);
        ed3=(EditText)findViewById(R.id.editText3);

        b1=(Button)findViewById(R.id.button);
        abord=(Button)findViewById(R.id.abord);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (sharedpreferences.contains(Phone1)) {
            ed1.setText(sharedpreferences.getString(Phone1, ""));
        }
        if (sharedpreferences.contains(Phone2)) {
            ed2.setText(sharedpreferences.getString(Phone2, ""));

        }
        if (sharedpreferences.contains(Phone3)) {
            ed3.setText(sharedpreferences.getString(Phone3, ""));

        }
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String n  = ed1.getText().toString();
                String ph  = ed2.getText().toString();
                String e  = ed3.getText().toString();

                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putString(Phone1, n);
                editor.putString(Phone2, ph);
                editor.putString(Phone3, e);
                editor.commit();
                Toast.makeText(Menu.this,"Thanks they are saved in sharedPreferences",Toast.LENGTH_LONG).show();
            }
        });
        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable
                = new ColorDrawable(
                Color.parseColor("#0F9D58"));
        actionBar.setBackgroundDrawable(colorDrawable);

        // initialise views
        btnSelect = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        clear = findViewById(R.id.clear);
        imageView = findViewById(R.id.imgView);

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // on pressing btnSelect SelectImage() is called
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SelectImage();
            }
        });
        abord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Aborded=true;
                if(mp!=null) {
                    mp.stop();
                    setVisibilitys("false");
                    ab=0;
                    start=true;
                    saveAlarmToDB("abort");
                    Toast.makeText(getApplicationContext(), "The message aborted " ,
                            Toast.LENGTH_LONG).show();
                }
              //  count.setVisibility(View.INVISIBLE);
            }
        });
        // on pressing btnUpload uploadImage() is called
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                uploadImage();
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                clearImage();
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(Menu.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);



    }


public void getLocation(){
    try {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }
    } catch (Exception e){
        e.printStackTrace();
    }

    gpsTracker = new GpsTracker(Menu.this);

    if(gpsTracker.canGetLocation()){
        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();

        Log.d("GPS Enabled", "GPS Enabled");

    }else{
        gpsTracker.showSettingsAlert();
        return;
    }
}
    public void sendFireAlert(View view) {
        getLocation();
        sharedpreferences = getSharedPreferences(MyPREFERENCES,
                Context.MODE_PRIVATE);

        phoneNo1 =  "tel:"+sharedpreferences.getString(Phone1, "");
        phoneNo2 =  "tel:"+sharedpreferences.getString(Phone2, "");
        phoneNo3 =  "tel:"+sharedpreferences.getString(Phone3, "");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        String ts = simpleDateFormat.format(new Date());
        message = "Βρίσκομαι στην τοποθεσία με γεωγραφικό μήκος : "+String.valueOf(latitude) +" και γεωγραφικό πλάτος :"+String.valueOf(longitude) +" και παρατηρώ μια πυρκαγιά.Timestamp:"+ts;

        smsManager = SmsManager.getDefault();
        msgArray = smsManager.divideMessage(message);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
        else{
            Log.d("Sending SMS", "YES");
            if(!phoneNo1.trim().equals("tel:")  && phoneNo1!=null && !phoneNo1.isEmpty() && phoneNo1!="tel:") {
                smsManager.sendMultipartTextMessage(phoneNo1, null, msgArray, null, null);
                Toast.makeText(getApplicationContext(), "SMS Fire alert has sent to " + phoneNo1,
                        Toast.LENGTH_LONG).show();
            }
            Log.d("Sending SMS", phoneNo2 +":vdfv");
           if(!phoneNo2.trim().equals("tel:") && phoneNo2!=null && !phoneNo2.isEmpty() && phoneNo2!="tel:") {
               smsManager.sendMultipartTextMessage(phoneNo2, null, msgArray, null, null);
               Toast.makeText(getApplicationContext(), "SMS Fire alert has sent to " + phoneNo2,
                       Toast.LENGTH_LONG).show();
           }
            if(!phoneNo3.trim().equals("tel:")  && phoneNo3!=null && !phoneNo3.isEmpty() && phoneNo3!="tel:") {
                smsManager.sendMultipartTextMessage(phoneNo3, null, msgArray, null, null);
                Toast.makeText(getApplicationContext(), "SMS Fire alert has sent to " + phoneNo3,
                        Toast.LENGTH_LONG).show();
            }
        }
        //apothikefsi tou sumvantos sthn vasi
       saveAlarmToDB("fire");
    }
    public void saveAlarmToDB(String event){
        getLocation();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        //   userid=currentFirebaseUser.getUid();
        final DatabaseReference myRef = database.getReference(userid);
        // final DatabaseReference myspeedlimit = database.getReference("speedLimit");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        String ts = simpleDateFormat.format(new Date());
        myRef.child(ts).push();
        String key = myRef.child(ts).getKey();
        myRef.child(key).child("alarm").setValue(event);
        myRef.child(key).child("longtitude").setValue(String.valueOf(longitude));
        myRef.child(key).child("latitude").setValue(String.valueOf(latitude));
    }
    public void sendFallingAlert() {
        ab=0;
        getLocation();
        sharedpreferences = getSharedPreferences(MyPREFERENCES,
                Context.MODE_PRIVATE);

        phoneNo1 =  "tel:"+sharedpreferences.getString(Phone1, "");
        phoneNo2 =  "tel:"+sharedpreferences.getString(Phone2, "");
        phoneNo3 =  "tel:"+sharedpreferences.getString(Phone3, "");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        String ts = simpleDateFormat.format(new Date());
        message = "Έπεσα στο : "+String.valueOf(latitude) +" και γεωγραφικό πλάτος :"+String.valueOf(longitude) +" και βρισκομαι σε κίνδυνο.Timestamp:"+ts;

        smsManager = SmsManager.getDefault();
        msgArray = smsManager.divideMessage(message);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
        else{
            Log.d("Sending SMS", "YES");
            if(!phoneNo1.trim().equals("tel:")  && phoneNo1!=null && !phoneNo1.isEmpty() && phoneNo1!="tel:") {
                smsManager.sendMultipartTextMessage(phoneNo1, null, msgArray, null, null);
                Toast.makeText(getApplicationContext(), "SMS FALLING alert has sent to " + phoneNo1,
                        Toast.LENGTH_LONG).show();
            }
            Log.d("Sending SMS", phoneNo2 +":vdfv");
            if(!phoneNo2.trim().equals("tel:") && phoneNo2!=null && !phoneNo2.isEmpty() && phoneNo2!="tel:") {
                smsManager.sendMultipartTextMessage(phoneNo2, null, msgArray, null, null);
                Toast.makeText(getApplicationContext(), "SMS FALLING alert has sent to " + phoneNo2,
                        Toast.LENGTH_LONG).show();
            }
            if(!phoneNo3.trim().equals("tel:")  && phoneNo3!=null && !phoneNo3.isEmpty() && phoneNo3!="tel:") {
                smsManager.sendMultipartTextMessage(phoneNo3, null, msgArray, null, null);
                Toast.makeText(getApplicationContext(), "SMS FALLING alert has sent to " + phoneNo3,
                        Toast.LENGTH_LONG).show();
            }
        }
        saveAlarmToDB("fall");
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    if(!phoneNo1.trim().equals("tel:")  && phoneNo1!=null && !phoneNo1.isEmpty() && phoneNo1!="tel:") {
                        smsManager.sendMultipartTextMessage(phoneNo1, null, msgArray, null, null);
                        Toast.makeText(getApplicationContext(), "SMS FALLING alert has sent to " + phoneNo1,
                                Toast.LENGTH_LONG).show();
                    }
                    Log.d("Sending SMS", phoneNo2 +":vdfv");
                    if(!phoneNo2.trim().equals("tel:") && phoneNo2!=null && !phoneNo2.isEmpty() && phoneNo2!="tel:") {
                        smsManager.sendMultipartTextMessage(phoneNo2, null, msgArray, null, null);
                        Toast.makeText(getApplicationContext(), "SMS FALLING alert has sent to " + phoneNo2,
                                Toast.LENGTH_LONG).show();
                    }
                    if(!phoneNo3.trim().equals("tel:")  && phoneNo3!=null && !phoneNo3.isEmpty() && phoneNo3!="tel:") {
                        smsManager.sendMultipartTextMessage(phoneNo3, null, msgArray, null, null);
                        Toast.makeText(getApplicationContext(), "SMS FALLING alert has sent to " + phoneNo3,
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS FALLING failed, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }
    public void goToActivityspeedMap(View view) {
    //    Intent intent = new Intent(getApplicationContext(), ActivityMapsSpeed.class);
     //   startActivity(intent);
    }
    // Select Image method
    private void SelectImage()
    {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }

    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data)
    {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);

               // Bitmap d = new BitmapDrawable(getApplicationContext().getResources() , filePath.toString()).getBitmap();
               // Bitmap bitmap = BitmapFactory.decodeFile(filePath.getPath());
                int nh = (int) ( bitmap.getHeight() * (512.0 / bitmap.getWidth()) );
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                imageView.setImageBitmap(scaled);
                //imageView.setImageBitmap(bitmap);
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }
public void signout(View view){
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    if(currentFirebaseUser!=null){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
private void clearImage(){
    imageView.setImageResource(android.R.color.transparent);
}
    // UploadImage method
    private void uploadImage() {
        getLocation();
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    +"userId: "+userid+",latitude: "+latitude+",longitude: "+longitude);

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot) {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(Menu.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    imageView.setImageResource(android.R.color.transparent);
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(Menu.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                            imageView.setImageResource(android.R.color.transparent);
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int) progress + "%");
                                }
                            });
        }
    }
    public void MyAlarms(View view) {
        Intent intent = new Intent(getApplicationContext(), AlarmList.class);
        startActivity(intent);
    }
public void setVisibilitys(String t){
    final String f=t;
        if(String.valueOf(t)=="true"){
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Log.i("setvosibility", f);
                    count.setVisibility(View.VISIBLE);
                }
            });
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("setvosinvisibility", f);
                    count.setVisibility(View.INVISIBLE);
                }
            });
        }
}
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (start == true){
            ab = 0;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double loX = event.values[0];
            double loY = event.values[1];
            double loZ = event.values[2];

            double loAccelerationReader = Math.sqrt(Math.pow(loX, 2)
                    + Math.pow(loY, 2)
                    + Math.pow(loZ, 2));
            long mlPreviousTime = System.currentTimeMillis();
            // Log.d( "loX : " , loX + " loY : " + loY + " loZ : " + loZ);
            boolean moIsMin = false;
            if (loAccelerationReader <= 6.0) {
                moIsMin = true;
                Log.i("TAG", "min");
            }

            int i = 0;
            boolean moIsMax = false;
            if (moIsMin) {
                i++;
                Log.i("TAG", " loAcceleration : " + loAccelerationReader);
              //  loAccelerationReader = 31;
                if (loAccelerationReader >= 5) {
                    long llCurrentTime = System.currentTimeMillis();
                    long llTimeDiff = llCurrentTime - mlPreviousTime;
                    //  Log.i("TAG", "loTime :" + llTimeDiff);
                   // llTimeDiff = 11;
                    Log.i("TAG", " llTimeDiff : " + llTimeDiff);
                    if (llTimeDiff >= 1) {
                        moIsMax = true;
                        Log.i("TAG", "max");
                    }
                }

            }

            if (moIsMin && moIsMax) {
                Log.d("LOG_TAG", "x:" + loX + ";y:" + loY + ";z:" + loZ);
                Log.d("onsensor", "epese");
                Log.i("TAG", "loX : " + loX + " loY : " + loY + " loZ : " + loZ);
                Log.i("TAG", "FALL DETECTED!!!!!");
                Toast.makeText(Menu.this, "FALL DETECTED!!!!!The SMS alert will be sent to 30 second!If you don't want to send SMS press Abort", Toast.LENGTH_LONG).show();
                final Timer updateTimer = new Timer("velocityUpdate");
                Uri alarmSound =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mp = MediaPlayer.create(getApplicationContext(), alarmSound);
                mp.start();
                ab = 30;

                setVisibilitys("true");
                start=false;
                updateTimer.scheduleAtFixedRate(new TimerTask() {

                    public void run() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                count.setText(String.valueOf(ab));
                            }
                        });
                        Log.d("RUNinjnn: ", String.valueOf(ab));

                        ab--;
                        if (ab == -1) {
                            updateTimer.cancel();

                        }
                    }
                }, 0, 1000);
                // ab = 0;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    public void run() {
                        Log.d("handler: ", "yes");
                        if (Aborded == false) {

                            mp.stop();
                            sendFallingAlert();
                            start=true;
                            setVisibilitys("false");
                            ab=0;
                        }
                    }
                }, 30000);


                i = 0;
                moIsMin = false;
                moIsMax = false;

            }

            if (i > 5) {
                i = 0;
                moIsMin = false;
                moIsMax = false;
            }
        }
    }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d( "onAccuracyChanged","onAccuracyChanged");
    }
}
