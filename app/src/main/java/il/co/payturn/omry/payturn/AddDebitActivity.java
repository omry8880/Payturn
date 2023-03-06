package il.co.payturn.omry.payturn;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddDebitActivity extends Activity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private ImageView ivBackArrow;
    private Button btnAddDebit;

    private SharedPreferences prefs;

    private final int CAMERA_PERMISSION_REQUEST = 2;
    private final int REQUEST_CAMERA = 1;
    private final int REQUEST_GALLERY = 0;

    Uri imageURI;
    private String uriString;

    private TextInputEditText etAddDebitName;
    private TextInputEditText etAddDebitContact;
    private TextInputEditText etAddDebitDeadline;
    private TextInputEditText etAddDebitSum;
    private Button btnAddDebitPicture;

    StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_debit);
        etAddDebitName = (TextInputEditText) findViewById(R.id.etAddDebitName);
        etAddDebitContact = (TextInputEditText) findViewById(R.id.etAddDebitContact);
        etAddDebitDeadline = (TextInputEditText) findViewById(R.id.etAddDebitDeadline);
        etAddDebitSum = (TextInputEditText) findViewById(R.id.etAddDebitSum);
        btnAddDebitPicture = (Button) findViewById(R.id.btnAddDebitPicture);
        btnAddDebit = (Button) findViewById(R.id.btnAddDebit);
        ivBackArrow = (ImageView) findViewById(R.id.ivAddDebitBack);

        ivBackArrow.setOnClickListener(this);
        btnAddDebit.setOnClickListener(this);
        etAddDebitDeadline.setOnClickListener(this);
        btnAddDebitPicture.setOnClickListener(this);

        prefs = this.getSharedPreferences(RegisterActivity.SETTING_PREF, MODE_PRIVATE);
        String[] email = prefs.getString(RegisterActivity.EMAIL, "").split("@");
        mStorageRef = FirebaseStorage.getInstance().getReference("Images").child(email[0]);

    }

    /**
     * onClick - will be called once the user clicks on an item which is mentioned inside the function
     * @param v - the item which is clickable and will call the function once it was clicked
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.etAddDebitDeadline:
                showDatePickerDialog();
                break;
            case R.id.btnAddDebitPicture:
                directToCamera();
                break;
            case R.id.ivAddDebitBack:
                onBackPressed();
                break;
            case R.id.btnAddDebit:
                addDebitComplete();
                //
                //
                break;
        }
    }

    /**
     * showDatePickerDialog - opens the date dialog in which a user can pick a date
     */
    private void showDatePickerDialog() { //Opens up a calendar with the local date
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    /**
     * onDateSet - once a user clicked on a certain date in the DatePickerDialog, this function will be called
     * @param view - the date picker dialog
     * @param year - year from the chosen date
     * @param month - month from the chosen date
     * @param dayOfMonth - day from chosen date
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) { //sets the date chosen to the text in the edittext
        String deadline = dayOfMonth + "/" + (month + 1) + "/" + year;
        etAddDebitDeadline.setText(deadline);
    }

    /**
     * directToCamera - opens an alert dialog in which the user will be able to choose between gallery or camera, and then will be directed according to the option he picked to the image gallery, or to the camera accordingly
     */
    private void directToCamera() {
        AlertDialog.Builder cameraOrGalleryDialog = new AlertDialog.Builder(this);

        cameraOrGalleryDialog.setMessage("Choose where to pick the image from.");
        cameraOrGalleryDialog.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //user chooses camera
                if (ContextCompat.checkSelfPermission(AddDebitActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { //Checks for user permission to use the camera
                    ActivityCompat.requestPermissions(AddDebitActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
                }
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //Directs to camera
                startActivityForResult(camera, REQUEST_CAMERA);
            }
        });
        cameraOrGalleryDialog.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //user chooses gallery
                Intent pickPhotoFromGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(pickPhotoFromGallery, REQUEST_GALLERY);
            }
        });
        cameraOrGalleryDialog.show();
    }

    /**
     * onActivityResult - gets the result back from an activity when it ends, basically transfers data and does something with it
     * @param requestCode - custom requestCode for different cases
     * @param resultCode - checks that the call startActivityForResult() was successful and did not return null
     * @param data - intent type, for example could be ACTION.PICK for gallery intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case (REQUEST_CAMERA):
                if (resultCode == RESULT_OK && data != null) {
                    btnAddDebitPicture.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.greenish_done, null)));
                    btnAddDebitPicture.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pt_icon_done, 0, 0, 0);

                    Bundle bundle = data.getExtras();
                    Bitmap bmp = (Bitmap) bundle.get("data");

                    Uri uri = convertBitmapToUri(getApplicationContext(), bmp); // Converting the Bitmap to URI
                    uriString = uri.toString();
                    StorageReference imageName = mStorageRef.child("image" + uri.getLastPathSegment()); // Uploading the image to Firebase Storage
                    imageName.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //
                        }
                    });
                }
                break;
            case (REQUEST_GALLERY):
                if (resultCode == RESULT_OK && data != null) {
                    btnAddDebitPicture.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.greenish_done, null)));
                    btnAddDebitPicture.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pt_icon_done, 0, 0, 0);

                    imageURI = data.getData();
                    uriString = imageURI.toString();

                    //Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageURI);
                    //ByteArrayOutputStream out = new ByteArrayOutputStream();
                    //bmp.compress(Bitmap.CompressFormat.JPEG, 30, out);

                    // Uploading the image (URI) to Firebase Storage
                    StorageReference imageName = mStorageRef.child("image" + imageURI.getLastPathSegment());
                    imageName.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //
                        }
                    });
                }
                break;
        }

    }

    /**
     * addDebitComplete - takes the information that was put in all the edit text fields and the date picker dialog and creates a debt object using the collected information, also updates the firebase and returns to HomeActivity
     */
    private void addDebitComplete() {

        if (etAddDebitName.getText().toString().length() == 0) {
            etAddDebitName.setError("You need to enter a debit name.");
        }
        if (etAddDebitContact.getText().toString().length() == 0) {
            etAddDebitContact.setError("You need to enter a username.");
        }
        if (etAddDebitDeadline.getText().toString().length() == 0) {
            etAddDebitDeadline.setError("You need to enter a deadline.");
        }
        if (etAddDebitSum.getText().toString().length() == 0) {
            etAddDebitSum.setError("You need to enter the sum of money.");
        }
        if (uriString == null) {
            //
        }
        else {
            ///Adding debit information to Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();

            prefs = this.getSharedPreferences(RegisterActivity.SETTING_PREF, MODE_PRIVATE); // Importing register information (name, email, password) to profile page
            String[] email = prefs.getString(RegisterActivity.EMAIL, "").split("@");

            String date = new SimpleDateFormat("d/M/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()); // Get the current (today's) date for the 'addedDate' field

            String[] contact = etAddDebitContact.getText().toString().split("@");

            Debt debit = new Debt(etAddDebitName.getText().toString(), etAddDebitName.getText().toString(), Double.parseDouble(etAddDebitSum.getText().toString()), etAddDebitDeadline.getText().toString(), date, contact[0], email[0], uriString, false);
            myRef.child("Users").child(email[0]).child("Debits").child(debit.getID()).setValue(debit);
            ///

            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

    }

    /**
     * onBackPressed - Required if we want to go back from Activity to previous fragment || Basically makes the "previous page" button in the AddDebit Activity operate the same as the android back button
     */
    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) { // Return the number of entries currently in the back stack. (if there's no previous page, nothing will happen)
            this.finish();
        } else {
            super.onBackPressed(); // else - replaced
        }
    }

    /**
     * converts Bitmap objects to Uri
     * @param context - the activity in which the function is called
     * @param inImage - the selected bitmap which will then be converted to Uri
     * @return Uri.parse(path) - returns the Uri from its path string
     */
    private Uri convertBitmapToUri(Context context, Bitmap inImage) { // Converts Bitmaps to URI's
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

}
