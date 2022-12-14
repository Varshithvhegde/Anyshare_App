package com.varshith.anyshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.intellij.lang.annotations.RegExp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    DownloadManager manager;
    ProgressDialog progressDialog;
    private Uri filePath;
    String downloadUri;
    // request code
    private final int PICK_IMAGE_REQUEST = 22;
    int flag;
    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    TextView showuniquetext;
    StorageReference storageReference;
    Button filechoose,upload,getfile,sendorrec;
    ImageView copybtn;
    FirebaseDatabase database;
    DatabaseReference ref;
    int RandomNumber;
    EditText uni;
    String urldownload;
     String type;
     String[] et;
     String filename;
     LinearLayout rev;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filechoose=(Button) findViewById(R.id.file) ;
        upload=(Button) findViewById(R.id.upload);
        uni=(EditText) findViewById(R.id.uniqid);
        getfile=(Button) findViewById(R.id.getfile);
        showuniquetext=(TextView) findViewById(R.id.showunique);
        rev=(LinearLayout) findViewById(R.id.recieve);
        copybtn=(ImageView) findViewById(R.id.copy);
        sendorrec=(Button) findViewById(R.id.sendorrecieve);
        sendorrec.setVisibility(View.INVISIBLE);
        copybtn.setVisibility(View.INVISIBLE);
        showuniquetext.setVisibility(View.INVISIBLE);
        progressDialog=new ProgressDialog(this);
        database=FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
         ref= database.getReference().child("image");
        filechoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
        getfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!uni.getText().toString().equals("")) {
                    DownloadFile();
                }
            }
        });
        copybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Uniue Id",showuniquetext.getText().toString());
                Toast.makeText(MainActivity.this, "Copied Uniue Id", Toast.LENGTH_SHORT).show();
                clipboard.setPrimaryClip(clip);
            }
        });
        sendorrec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload.setVisibility(View.VISIBLE);
                showuniquetext.setVisibility(View.INVISIBLE);
                copybtn.setVisibility(View.INVISIBLE);
                sendorrec.setVisibility(View.INVISIBLE);

                rev.setVisibility(View.VISIBLE);
                filechoose.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void DownloadFile() {
        String numuni=uni.getText().toString();
        int u=Integer.parseInt(numuni);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds :snapshot.getChildren()) {
                    int num = ds.child("number").getValue(Integer.class);
                    if (num == u) {
                        urldownload = ds.child("url").getValue(String.class);
                        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(urldownload);
//                        Pattern p =Pattern.compile("%2..*%2F(.*?)\\?alt");
//                        String link = String.valueOf(storageReference.getMetadata());
                        String fname = storageReference.getName();
                        Toast.makeText(MainActivity.this, fname, Toast.LENGTH_SHORT).show();

                        manager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
                        Uri uri = Uri.parse(urldownload);
                        DownloadManager.Request request = new DownloadManager.Request(uri);
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fname);


                        manager.enqueue(request);
                    }



//                        String ff[]=urldownload.split(Pattern.quote("?"));
//                        String filenpm=("https://firebasestorage.googleapis.com/v0/b/image-share-4446c.appspot.com/o/images");
//                        String ex =filenpm[0];
//                        Toast.makeText(MainActivity.this, ff[0], Toast.LENGTH_SHORT).show();

                    }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }



    private void uploadImage() {
        if (filePath != null) {
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + filename);
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            downloadUri = uri.toString();

                                        }
                                    });
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(MainActivity.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Uploaddata();
                                        }
                                    }, 5 * 1000);
                                }

                                })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            // Error, Image not uploaded
//                            progressDialog.dismiss();
                            Toast
                                    .makeText(MainActivity.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
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
                                                    + (int)progress + "%");
                                    progressDialog.show();
                                }
                            });
        }
    }

    private void Uploaddata() {
         RandomNumber=uniquerandom();
        String key =  ref.push().getKey();
       ref.child(key).child("number").setValue(RandomNumber);
       ref.child(key).child("url").setValue(downloadUri);
//        Toast.makeText(this,String.valueOf(RandomNumber), Toast.LENGTH_SHORT).show();
        upload.setVisibility(View.INVISIBLE);
        showuniquetext.setVisibility(View.VISIBLE);
        copybtn.setVisibility(View.VISIBLE);
        sendorrec.setVisibility(View.VISIBLE);
        showuniquetext.setText(String.valueOf(RandomNumber));
        rev.setVisibility(View.INVISIBLE);
        filechoose.setVisibility(View.INVISIBLE);


    }

    private int uniquerandom() {
        Random r = new Random( System.currentTimeMillis() );
        int rand=((1 + r.nextInt(2)) * 10000 + r.nextInt(10000));
         flag=0;
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds :snapshot.getChildren()){
                    int num=ds.child("number").getValue(Integer.class);
                    if (num == rand) {
                       flag=1;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(flag==1){
            uniquerandom();
        }
        return rand;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
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

            Cursor mCursor =
                    getApplicationContext().getContentResolver().query(filePath, null, null, null, null);
            int indexedname = mCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            mCursor.moveToFirst();
             filename = mCursor.getString(indexedname);
            mCursor.close();

            Toast.makeText(this, filename, Toast.LENGTH_SHORT).show();
            filechoose.setText(filename);
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);

            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }
    private void SelectImage(){
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }
}