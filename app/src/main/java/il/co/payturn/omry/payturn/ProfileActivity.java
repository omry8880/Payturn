package il.co.payturn.omry.payturn;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvProfileFullnameText;
    private TextView tvProfileEmailText;
    private TextView tvProfilePasswordText;
    private ImageView ivProfilePasswordEdit;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddProfilePicture;
    private ImageView ivProfilePicture;

    private SharedPreferences prefs;

    private SharedPreferences profilePicPrefs;
    public static final String DEBITS_IMAGE_PREF = "DEBITS_IMAGE_PREF";

    private final int REQUEST_CAMERA = 1;
    private final int REQUEST_GALLERY = 0;

    Uri imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Action bar stuff goes here
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        //

        tvProfileFullnameText = (TextView) findViewById(R.id.tvProfileFullNameText);
        tvProfileEmailText = (TextView) findViewById(R.id.tvProfileEmailText);
        tvProfilePasswordText = (TextView) findViewById(R.id.tvProfilePasswordText);
        ivProfilePasswordEdit = (ImageView) findViewById(R.id.ivProfilePasswordEdit);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_bar);
        fabAddProfilePicture = (FloatingActionButton) findViewById(R.id.fabAddProfilePicture);
        ivProfilePicture = (ImageView) findViewById(R.id.ivProfilePicture);

        ivProfilePasswordEdit.setOnClickListener(this);
        fabAddProfilePicture.setOnClickListener(this);

        prefs = this.getSharedPreferences(RegisterActivity.SETTING_PREF, MODE_PRIVATE); //Importing register information (name, email, password) to profile page
        tvProfileFullnameText.setText(prefs.getString(RegisterActivity.FULL_NAME, ""));
        tvProfileEmailText.setText(prefs.getString(RegisterActivity.EMAIL, ""));
        tvProfilePasswordText.setText(prefs.getString(RegisterActivity.PASSWORD, ""));

        profilePicPrefs = this.getSharedPreferences(DEBITS_IMAGE_PREF, MODE_PRIVATE); //Setting the profile picture from sp
        try { //Checking if the profile picture is either from the camera (bitmap) or the gallery (URI)
            Bitmap profilePic = decodeBase64(profilePicPrefs.getString("ivProfilePic", ""));
            if (profilePic != null) {
                ivProfilePicture.setImageBitmap(profilePic);
            }
            else {
                String uri = profilePicPrefs.getString("ivProfilePic", "");
                ivProfilePicture.setImageURI(Uri.parse(uri));
            }
        } catch (Exception error) {
            // Error
        }

        bottomNavigationView.setSelectedItemId(R.id.nav_profile); //set menu item state check

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            /**
             * onNavigationItemSelected - every time the user clicks on an item from the bottom navigation menu, this method is called.
             * The user can be redirected to the following activities from this navigation listener: DashboardActivity and HomeActivity.
             * @param item
             * @return
             */
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        directToHomePage();
                        break;
                    case R.id.nav_dashboard:
                        directToDashboardPage();
                        break;
                }
                return true;
            }
        });

    }

    /**
     * onCreateOptionsMenu - called when user clicks on the ActionBar menu and inflates (opens) it.
     * @param menu - the type of the parameter menu
     * @return returns the super class which will then return true (because HomeActivity does not extend Activity, a call to the super class is required)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.hamburger_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivProfilePasswordEdit:
                passwordEditPopup();
                break;
            case R.id.fabAddProfilePicture:
                directToCamera();
                break;
        }
    }

    /**
     * directToCamera - the user will receive a popup dialog, there he can choose to go either to his camera or to his gallery.
     * From there he will be directed to the camera/gallery, there he may take/choose a picture which will be displayed in his profile picture.
     */
    private void directToCamera() {
        AlertDialog.Builder cameraOrGalleryDialog = new AlertDialog.Builder(this);

        cameraOrGalleryDialog.setMessage("Choose where to pick the image from.");
        cameraOrGalleryDialog.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //user chooses camera
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //Directs to camera
                startActivityForResult(camera, REQUEST_CAMERA);
            }
        });
        cameraOrGalleryDialog.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //user chooses gallery
                Intent pickPhotoFromGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(pickPhotoFromGallery , REQUEST_GALLERY);
            }
        });
        cameraOrGalleryDialog.show();
    }

    /**
     * passwordEditPopup - a popup that will be displayed to the user if he wishes to change his password, there he will be able to do so by
     * writing his past password 2 times and his new password.
     */
    private void passwordEditPopup() {
        AlertDialog.Builder passwordEditDialog = new AlertDialog.Builder(this);

        passwordEditDialog.setMessage("Are you sure you want to edit your password? You may only do so once a month.");
        passwordEditDialog.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Dialog changePasswordDialog = new Dialog(ProfileActivity.this); // Shows the change password dialog
                changePasswordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                changePasswordDialog.setContentView(R.layout.dialog_change_password);

                final EditText tilOldPassword = (EditText) changePasswordDialog.findViewById(R.id.etOldPassword);
                final EditText tilNewPassword = (EditText) changePasswordDialog.findViewById(R.id.etNewPassword);
                final EditText tilNewConfirmPassword = (EditText) changePasswordDialog.findViewById(R.id.etConfirmNewPassword);
                Button btnSubmitNewPassword = (Button) changePasswordDialog.findViewById(R.id.btnSubmitNewPassword);

                btnSubmitNewPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (tilOldPassword.getText().toString().length() == 0) {
                            tilOldPassword.setError("You need to enter your old password.");
                        }
                        if (tilNewPassword.getText().toString().length() == 0) {
                            tilNewPassword.setError("You need to enter your new password.");
                        }
                        if (tilNewConfirmPassword.getText().toString().length() == 0) {
                            tilNewConfirmPassword.setError("You need to enter your new password again.");
                        }
                        else {
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                            String[] userID = prefs.getString(RegisterActivity.EMAIL, "").split("@");

                            if (tilOldPassword.getText().toString().equals(ref.child("Users").child(userID[0]).child("password"))) { // Check if old password is correct
                                ref.child("Users").child(userID[0]).child("password").setValue(tilNewPassword.getText().toString()); // Change the password
                                changePasswordDialog.dismiss();
                            }
                            else {
                                tilOldPassword.setError("Password is not valid.");
                            }
                        }
                    }
                });

                changePasswordDialog.show();
            }
        });
        passwordEditDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //user chooses no
            }
        });
        passwordEditDialog.show();
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

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA && data != null) {

                Bundle bundle = data.getExtras();
                Bitmap bmp = (Bitmap) bundle.get("data");
                ivProfilePicture.setImageBitmap(bmp);

                profilePicPrefs = this.getSharedPreferences(DEBITS_IMAGE_PREF, MODE_PRIVATE); //Importing register information (name, email, password) to profile page
                SharedPreferences.Editor editor = profilePicPrefs.edit(); //Saving the uploaded debit image to shared preferences & encoding it so we could pass it over to the adapter which then passes it over to the debits fragment
                editor.putString("ivProfilePic", encodeTobase64(bmp));
                editor.apply();

            } else if (requestCode == REQUEST_GALLERY && data != null) {

                imageURI = data.getData();
                ivProfilePicture.setImageURI(imageURI);

                profilePicPrefs = this.getSharedPreferences(DEBITS_IMAGE_PREF, MODE_PRIVATE);
                SharedPreferences.Editor editor = profilePicPrefs.edit();
                editor.putString("ivProfilePic", imageURI.toString());
                editor.apply();

            }
        }
    }

    private void directToHomePage() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void directToDashboardPage() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    /**
     * encodeTobase64 - To pass bitmaps (images) through shared preferences, we should first CONVERT them to strings (Base64 strings), because saving Bitmaps inside SharedPreferences is way too much information and will make the app slower
     * method to convert Bitmap Images to String Base64 so we could pass them on from this activity to the adapter
     * @param image - the bitmap image that was taken from the camera/gallery
     * @return string - the converted string base64 image
     */
    public static String encodeTobase64(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        return imageEncoded;
    }

    /**
     * decodeBase64 - method to convert String Base64 back to Bitmap (Basically convert the string we encoded earlier back to an actual image so we could display it on the debit cards)
     * @param input - the converted Bitmap string base64
     * @return bitmap - returns the bitmap that was decoded from the base64 string
     */
    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

}