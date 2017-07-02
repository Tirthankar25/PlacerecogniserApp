package com.example.userpc.placerecognizer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements FinishedUploading{

    private static final int CAMERA_CODE = 1;
    private static final int PICK_IMAGE = 2;

    private String mCurrentPhotoPath;
    private Bitmap mPhoto;
    private ImageView photo;
    private FloatingActionButton upload;
    private FloatingActionButton attach;
    private ProgressDialog progressDialog;
    private String url="https://skytourist.herokuapp.com/polls/";
    private final int MY_PERMISSIONS_REQUEST=0;


    public void createAlert(String str)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Error").setMessage(str).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }


    public Bitmap getRotatedImage(String file) throws IOException
    {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, bounds);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(file, opts);
        ExifInterface exif = new ExifInterface(file);
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;

        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        return rotatedBitmap;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                //Uri photoURI = FileProvider.getUriForFile(this,
                //        "com.example.android.fileprovider",
                //        photoFile);
                Uri photoURI=Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_CODE);
            }
            else
                createAlert("Couldn't access your SD card. Make sure its plugged in and accessible");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(intent.resolveActivity(getPackageManager())!=null) {
                    dispatchTakePictureIntent();
                }
                else
                    Snackbar.make(view, "Please install a better camera application", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

            }
        });
        photo= (ImageView) findViewById(R.id.pic);
        upload= (FloatingActionButton) findViewById(R.id.upbtn);
        attach= (FloatingActionButton) findViewById(R.id.attach);
        progressDialog=new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Please Wait");
        progressDialog.setCancelable(false);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Uploading image to server", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if(progressDialog!=null && !progressDialog.isShowing())
                    progressDialog.show();
                new UploadImage(MainActivity.this,mPhoto).execute();

            }
        });
        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
        if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);

            } else {


                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case CAMERA_CODE:
                if(resultCode==RESULT_OK)
                {
                    //Toast.makeText(this,mCurrentPhotoPath,Toast.LENGTH_SHORT).show();
                    try {
                        if(mPhoto!=null)
                            mPhoto.recycle();
                        mPhoto=getRotatedImage(mCurrentPhotoPath);
                        photo.setImageBitmap(mPhoto);
                        upload.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("AcrivityResult","Error processing image");
                        createAlert("Image couldnot be captured successfully. Please retry or contact the developers");
                    }
                }
                else
                {
                    createAlert("Image couldnot be captured successfully. Please retry or contact the developers");
                }
                break;
            case PICK_IMAGE:
                if(resultCode==RESULT_OK)
                {
                    if(data==null)
                    {
                        createAlert("The image could not be selected. Please try with a different image.");
                        return;
                    }
                    else
                    {
                        try {
                            if(mPhoto!=null)
                                mPhoto.recycle();
                            InputStream is=getContentResolver().openInputStream(data.getData());
                            mPhoto=BitmapFactory.decodeStream(is);
                            photo.setImageBitmap(mPhoto);
                            upload.setVisibility(View.VISIBLE);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            createAlert("Internal application error.");
                            return;
                        }
                    }
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFinished(String resp) {
        Log.e("response",resp+" ");
        if(progressDialog!=null && progressDialog.isShowing())
            progressDialog.dismiss();
        String alert_error=null;
        try{
            JSONObject obj=new JSONObject(resp);
            if(obj.getInt("status")==-1)
                alert_error="The image could not be successfully uploaded to the server. Please update the application or retry.";
            else if(obj.getInt("status")==1){
                Intent intent=new Intent(MainActivity.this, ScrollingActivity.class);
                intent.putExtra("speech",obj.getString("speech"));
                intent.putExtra("img",obj.getString("img"));
                intent.putExtra("output",obj.getString("output"));
                startActivity(intent);
                return;
            }
            else if(obj.getInt("status")==-2)
                alert_error="The image is too large and couldnot be processed on your phone. Try with another image or lower the resolutionof your camera.";
        }
        catch(Exception e)
        {
            e.printStackTrace();
            alert_error="The application could not connect to the internet. Check your data connection and retry.";
        }
        createAlert(alert_error);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    builder.setTitle("Success").setMessage("Thanks for the permissions. You can now proceed to using the application").setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    AlertDialog dialog=builder.create();
                    dialog.show();

                } else {
                    AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    builder.setTitle("Error").setCancelable(false).setMessage("You have not given this application proper permissions to run.").setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    AlertDialog dialog=builder.create();
                    dialog.show();
                }
                return;
            }

        }
    }
}
