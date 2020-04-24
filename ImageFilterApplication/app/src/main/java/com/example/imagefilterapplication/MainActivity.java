package com.example.imagefilterapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Layout;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //Create the variables for images, buttons etc
    ImageView showImage, filterImage;
    Button filterButton, removeButton;
    FloatingActionButton FAB;
    TextView showText;
    Menu mMenu; //holds the reference for the menu bar

    //will hold the value for the interval between the back button being pressed
    private  long backPressedTime;
    //Toast message when back button is pressed
    private Toast backToast;

    //empty container to store image file name when assigned
    String currentImage = "";

    //Permission requests
    static final int REQUEST_CAMERA = 1, GALLERY_REQUEST = 0;
    static  final int ALL_PERMISSION_REQUEST = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //layout, toolbar and action bar setup
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //assign each image, button or text to their respective variable
        showImage = (ImageView) findViewById(R.id.showImage);
        filterImage = (ImageView) findViewById(R.id.filterImage);

        filterButton = (Button) findViewById(R.id.filterButton);
        removeButton = (Button) findViewById(R.id.removeButton);

        showText = (TextView) findViewById(R.id.textView);

        showText.setVisibility(View.VISIBLE);

        //buttons are turned off by default, until an image is added
        filterButton.setEnabled(false);
        removeButton.setEnabled(false);

        //fab set up, with on click listener
        FAB = (FloatingActionButton) findViewById(R.id.fab);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //run the method which handles the users options for adding images
                addImage();
            }
        });

        //filter button set up, with on click listener
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hold all the filter options
                final CharSequence[] filters = {"Default", "Rainbow", "Stars", "Hearts", "Angel", "Cancel"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose a Filter");

                //alert dialog box which listens for the clicks
                builder.setItems(filters, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if(filters[i].equals("Default")){
                            //puts this image onto the ImageView 'filterImage' container, gets it from drawable folder
                            filterImage.setImageResource(R.drawable.ic_filter);
                        }
                        else if(filters[i].equals("Rainbow")){
                            filterImage.setImageResource(R.drawable.rainbow);
                        }
                        else if(filters[i].equals("Stars")){
                            filterImage.setImageResource(R.drawable.stars);
                        }
                        else if(filters[i].equals("Hearts")){
                            filterImage.setImageResource(R.drawable.hearts);
                        }
                        else if(filters[i].equals("Angel")){
                            filterImage.setImageResource(R.drawable.wings_and_halo);
                        }
                        else if(filters[i].equals("Cancel")){
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();

                //Image can now be saved or shared
                mMenu.findItem(R.id.share_action).setVisible(true);
                mMenu.findItem(R.id.save_action).setVisible(true);
            }
        });

        //remove button set up, with on click listener
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //remove the images from these containers
                showImage.setImageResource(0);
                filterImage.setImageResource(0);

                //turn off those buttons, until another image is added
                filterButton.setEnabled(false);
                removeButton.setEnabled(false);
                //Hide the menu buttons, until another image is added
                mMenu.findItem(R.id.save_action).setVisible(false);
                mMenu.findItem(R.id.share_action).setVisible(false);

                //turned on, so images can be added after previous image was removed
                FAB.setEnabled(true);

                //text to remind user to add image
                showText.setVisibility(View.VISIBLE);

                Toast.makeText(MainActivity.this, "Image removed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //listener for the back button
    @Override
    public void onBackPressed() {
            //if the back button is pressed within 2 seconds, it will run this code
        if (backPressedTime + 2000 > System.currentTimeMillis()){
            //cancel toast message, so it closes with the app
            backToast.cancel();
            super.onBackPressed();
            return;
        }
        //if the back button is pressed after 2 seconds
        else {
            backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    //method which makes an alert dialog and lets the user choose to add an image through gallery or camera
    private void addImage() {

        //hold these values for the alert dialog builder
        final CharSequence[] options = {"Camera", "Gallery", "Cancel"};

        //Set up the alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Choose an option");

        //choose what each option does on click
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                //option Camera was chosen
                if(options[i].equals("Camera")){
                    //if the OS is greater than Marshmallow, check for runtime permission for the camera
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                            //permission not granted, request it
                            requestPermissionCamera();
                        }
                        else {
                            //permission was already granted, start new intent to use camera
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, REQUEST_CAMERA);
                        }
                    }
                    else {
                        //OS is less than marshmallow, start new intent to use camera
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, REQUEST_CAMERA);
                    }
                }
            //option Gallery was chosen
            else if(options[i].equals("Gallery")){
                    //if the OS is greater than Marshmallow, check for runtime permission for storage access
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            //permission not granted for storage access, request it
                            requestPermissionGallery();
                        }
                        else {
                            //permission already granted. Start a new intent, so the user can choose an image from the gallery
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent, GALLERY_REQUEST);
                        }
                    }
                    else {
                        //OS is less than Marshmallow. Start a new intent, so the user can choose an image from the gallery
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(intent, GALLERY_REQUEST);

                    }
                }
            //The Cancel option was picked
            else if(options[i].equals("Cancel")){
                //close the alert dialog
                dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //Takes a screenshot, of the layout holding the filtered image and returns it
    private static Bitmap getScreenShot(View view) {
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    //This method will save the bitmap as a file and store it into internal storage
    private void saveImage(Bitmap bm) {
        //get the path from storage
        String imgDirPath = Environment.getExternalStorageDirectory().toString();
        //Assign a variable which holds the path for the image file and puts it into a folder
        File imgDir = new File(imgDirPath + "/ImageFilterApp");
        //if the directory doesn't already exist, create it
        if (!imgDir.exists()){
            imgDir.mkdirs();
        }
        //the name for the image
        currentImage = "FilteredImage" + System.currentTimeMillis() + ".png";
        //holds the value for the path and name of the file
        File file = new File(imgDir, currentImage);
        try {
            //variable for where the file will be output
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            //writes out buffered bytes
            fos.flush();
            fos.close();
            Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
    //Handles the request popup and alert dialog for the camera permission
    private void requestPermissionCamera() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.CAMERA)) {
            //Explanation for the user, so they understand why permission is needed
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Permission is Required!")
                    .setMessage("Images can't be added with the Camera without permission!")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //User understands and agrees to see the requests pop up again
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[] {Manifest.permission.CAMERA}, ALL_PERMISSION_REQUEST);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //The alert dialog for the explanation is dismissed
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();

        } else {
            //The popup for the request will show up
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, ALL_PERMISSION_REQUEST);
        }
    }

    //Handles the request popup and alert dialog for the gallery permission
    private void requestPermissionGallery() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //Explanation for the user, so they understand why permission is needed
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Permission is Required!")
                    .setMessage("Images can't be added or saved, without permission!")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //User understands and agrees to see the requests pop up again
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, ALL_PERMISSION_REQUEST);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //The alert dialog for the explanation is dismissed
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();

        } else {
            //The popup for the request will show up
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, ALL_PERMISSION_REQUEST);
        }
    }

    //Handles the permission requests if they are Denied or granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //if the request code is the same as the request code
        if (requestCode == ALL_PERMISSION_REQUEST) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission was granted
                addImage();
            }
            else {
                //permission was not granted
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Checks the identity of called functions/intents
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Checking for request code
        if (resultCode == RESULT_OK) {
            //Matches the camera request code, therefore run this line of code
            if (requestCode == REQUEST_CAMERA) {
                //turn the new image into bitmap
                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");
                showImage.setImageBitmap(bmp);

                //turn on the buttons
                filterButton.setEnabled(true);
                removeButton.setEnabled(true);

                //Floating Action Button turned off, to avoid button flag issues
                FAB.setEnabled(false);

                //hide the text
                showText.setVisibility(View.GONE);

            }
            //Matches the gallery request code, therefore run this line of code
            else if (requestCode == GALLERY_REQUEST && null != data) {
                Uri selectedImageUri = data.getData();
                try {
                    //convert the acquired image into bitmap
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    showImage.setImageBitmap(bitmap);

                    //turn on the buttons
                    filterButton.setEnabled(true);
                    removeButton.setEnabled(true);

                    //Floating Action Button turned off, to avoid button flag issues
                    FAB.setEnabled(false);

                    //hide the text
                    showText.setVisibility(View.GONE);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //menu bar buttons turned off until an image is added
        menu.findItem(R.id.save_action).setVisible(false);
        menu.findItem(R.id.share_action).setVisible(false);

        //menu reference
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            //checks the on click listener for the save button
            case R.id.save_action:
                //Get's whatever is in that layout
                View saveContent = findViewById(R.id.lay);
                //puts that into a bitmap and sends it to that method
                Bitmap saveBitmap = getScreenShot(saveContent);
                //the bitmap from getScreenShot, is sent to this method
                saveImage(saveBitmap);

                return true;

            //checks the on click listener for the share button
            case R.id.share_action:
                //Get's whatever is in that layout
                View shareContent = findViewById(R.id.lay);
                //puts that into a bitmap and sends it to that method
                Bitmap shareBitmap = getScreenShot(shareContent);

                try {
                    //saves the bitmap into a cache directory
                    File cachePath = new File(MainActivity.this.getCacheDir(), "images");
                    //makes a directory for the cache path
                    cachePath.mkdirs();
                    FileOutputStream fos = new FileOutputStream(cachePath + "/image.png");
                    shareBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //sharing the image
                File imagePath = new File(MainActivity.this.getCacheDir(), "images");
                File newFile = new File(imagePath, "image.png");
                //file provider used to acquire the file
                Uri contentUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", newFile);

                //if the uri holding file is not null
                if (contentUri != null){
                    //new intent for sharing action
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    // temp permission for receiving app to read this file
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    startActivity(Intent.createChooser(shareIntent, "Share via"));
                }
                return true;

            //checks the on click listener for the permissions option
            case R.id.Permissions:
                //new intent to open the app info page
                Intent intent = new Intent();
                //take the user to the app details page
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
