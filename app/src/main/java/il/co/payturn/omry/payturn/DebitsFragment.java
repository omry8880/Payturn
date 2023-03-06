package il.co.payturn.omry.payturn;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static android.support.constraint.Constraints.TAG;

public class DebitsFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private TextView tvEmptyPlaceholderDebits;
    private FloatingActionButton fabAddDebit;
    private ListView lvDebits;
    private EditText etEditDebitDeadline;
    private ImageView ivEditDebitPicture;

    private DatabaseReference database;
    private ArrayList<Debt> debits; //Debits List
    private DebitAdapter adapter; //Debits Adapter - required to show the list of debits
    private SharedPreferences prefs;
    private String userId;

    Uri imageURI;
    private String uriString;
    private StorageReference mStorageRef;

    private final int CAMERA_PERMISSION_REQUEST = 2;
    private final int REQUEST_CAMERA = 1;
    private final int REQUEST_GALLERY = 0;

    /**
     * onCreateView - Called to have the fragment instantiate its user interface view (basically shows the fragment layout and configures it)
     * @param inflater - the LayoutInflater object that can be used to inflate any views in the fragment
     * @param container - the parent view that the fragment's UI should be attached to
     * @param savedInstanceState - if not null, this fragment is being re-constructed from a previous saved state as given here
     * @return v - returns the fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_debits, container, false);

        ivEditDebitPicture = (ImageView) v.findViewById(R.id.ivEditDebitPicture);
        tvEmptyPlaceholderDebits = (TextView) v.findViewById(R.id.tvEmptyPlaceholderDebits);
        fabAddDebit = (FloatingActionButton) v.findViewById(R.id.fabAddDebit);
        lvDebits = (ListView) v.findViewById(R.id.lv_debits);

        lvDebits.setEmptyView(tvEmptyPlaceholderDebits); //If the ListView is empty, a placeholder "it's empty here..." text will instead appear

        fabAddDebit.setOnClickListener(this);

        prefs = getActivity().getSharedPreferences(RegisterActivity.SETTING_PREF, MODE_PRIVATE); //Importing register information (Email) to recognize the user currently adding the debit inside Firebase

        final String[] email = prefs.getString(RegisterActivity.EMAIL, "").split("@");
        mStorageRef = FirebaseStorage.getInstance().getReference("Images").child(email[0]);

        userId = prefs.getString(RegisterActivity.EMAIL, "");
        userId = userId.substring(0,userId.indexOf('@')).replace(".","");

        database = FirebaseDatabase.getInstance().getReference();

        database.child("Users").child(userId).child("Debits").addValueEventListener(new ValueEventListener() {

            /**
             * onDataChange - every time there's a change in Firebase in the selected path we created, this function will be called.
             * Inside the onDataChange we go over all of the debts objects in Firebase and place them in an ArrayList which will then be displayed inside a selected ListView with the help of an adapter
             * @param dataSnapshot - Any time you read Database data, you receive the data as a DataSnapshot, in this case it will be a debt object
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                debits = new ArrayList<>();
                for (DataSnapshot debtSnapshot : dataSnapshot.getChildren()) {

                    debits.add(debtSnapshot.getValue(Debt.class));

                }

                adapter = new DebitAdapter(debits, getContext());
                lvDebits.setAdapter(adapter);
                Collections.sort(debits);
            }

            /**
             * if the function onDataChange returns an error, this function will be called instead
             * @param databaseError
             */
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

                // ...
            }
        });

        lvDebits.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * onItemClick - every time an item inside the ListView is clicked, a custom-made dialog will open
             * @param parent - the AdapterView where the click happened
             * @param view - the view inside the AdapterView which was clicked
             * @param position - position of the item inside the adapter
             * @param id - The row id of the item that was clicked
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { // Debit preview dialog

                final Debt debit = debits.get(position);

                Dialog debitDialog = new Dialog(getContext()); // Shows the debit dialog (includes options to edit and/or delete a debit)
                debitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                debitDialog.setContentView(R.layout.dialog_debit_design);

                // Initializing and replacing the default variables with the debit variables
                TextView tvDialogDebitName = (TextView) debitDialog.findViewById(R.id.tvDialogDebitName);
                TextView tvDialogDebitContact = (TextView) debitDialog.findViewById(R.id.tvDialogDebitContact);
                TextView tvDialogDebitSum = (TextView) debitDialog.findViewById(R.id.tvDialogDebitSum);
                TextView tvDialogDebitDaysUntilDeadline = (TextView) debitDialog.findViewById(R.id.tvDialogDebitDaysUntilDeadline);
                TextView tvDialogDebitStatus = (TextView) debitDialog.findViewById(R.id.tvDialogDebitStatus);

                // Check the status of the debit (if unpaid = false, else = true)
                if (!debit.getStatus()) {
                    tvDialogDebitStatus.setText("Status: Unpaid");
                } else {
                    tvDialogDebitStatus.setText("Status: Paid");
                }

                Button btnDialogDebitEdit = (Button) debitDialog.findViewById(R.id.btnDialogDebitEdit);

                tvDialogDebitName.setText(debit.getName());
                tvDialogDebitContact.setText("Contact: " + debit.getDebtOwner_ID());
                tvDialogDebitSum.setText(NumberFormat.getNumberInstance(Locale.US).format(debit.getSum()) + "₪");

                // Get relative time to deadline
                String sDeadline= debit.getDeadline();
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
                ParsePosition pos = new ParsePosition(0);
                Date d = sdf.parse(sDeadline, pos);
                long due = d.getTime();
                tvDialogDebitDaysUntilDeadline.setText("Deadline: " + DateUtils.getRelativeTimeSpanString(due, System.currentTimeMillis(), 0L, DateUtils.FORMAT_ABBREV_RELATIVE));
                debitDialog.show();

                btnDialogDebitEdit.setOnClickListener(new View.OnClickListener() { // Edit debit dialog (opens on lick of the edit button inside the preview dialog)
                    /**
                     * onClick - this function will be called every time there's a click on an item that is clickable
                     * in this case, the onClick will be called every time a user clicks on the "Edit" button
                     * @param v - the item (view) that is clickable
                     */
                    @Override
                    public void onClick(View v) {
                        final Dialog editDebitDialog = new Dialog(getContext());
                        editDebitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        editDebitDialog.setContentView(R.layout.dialog_debit_edit);
                        editDebitDialog.setCancelable(false);

                        final TextView tvEditDebitTitle = (TextView) editDebitDialog.findViewById(R.id.tvEditDebitTitle);
                        final TextInputEditText etEditDebitName = (TextInputEditText) editDebitDialog.findViewById(R.id.etEditDebitName);
                        etEditDebitDeadline = (TextInputEditText) editDebitDialog.findViewById(R.id.etEditDebitDeadline);
                        final TextInputEditText etEditDebitSum = (TextInputEditText) editDebitDialog.findViewById(R.id.etEditDebitSum);
                        ivEditDebitPicture = (ImageView) editDebitDialog.findViewById(R.id.ivEditDebitPicture);
                        Button btnEditDebit = (Button) editDebitDialog.findViewById(R.id.btnEditDebit);
                        ImageView ivCloseEditDebitDialog = (ImageView) editDebitDialog.findViewById(R.id.ivCloseEditDebitDialog);

                        tvEditDebitTitle.setText(debit.getName()); //Title

                        etEditDebitName.setText(debit.getName());
                        etEditDebitDeadline.setText(debit.getDeadline());
                        ivEditDebitPicture.setImageURI(Uri.parse(debit.getImage()));
                        etEditDebitSum.setText(String.valueOf(debit.getSum()).replace(".0", ""));


                        etEditDebitDeadline.setOnClickListener(new View.OnClickListener() { // Date Picker Dialog
                            /**
                             * onClick - this function will be called every time the user clicks on the Deadline field inside the Edit Debit dialog, and opens the Date Picker Dialog
                             * @param v - the item (view) that is clickable
                             */
                            @Override
                            public void onClick(View v) {
                                // Date Picker values
                                String[] parts = debit.getDeadline().split("/");
                                int day = Integer.parseInt(parts[0]);
                                int month = Integer.parseInt(parts[1]) - 1;
                                int year = Integer.parseInt(parts[2]);

                                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), DebitsFragment.this, year, month, day);
                                datePickerDialog.show();
                            }
                        });

                        ivEditDebitPicture.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                directToCamera();
                            }
                        });

                        btnEditDebit.setOnClickListener(new View.OnClickListener() {
                            /**
                             * onClick - updates the values (if not null) of the edit text fields inside Firebase on click on the Finish button
                             * @param v - the item (view) that is clickable
                             */
                            @Override
                            public void onClick(View v) {

                                if (!etEditDebitName.getText().toString().isEmpty() && !etEditDebitName.getText().toString().equals(debit.getName())) { // Checks that the field is not empty and that the input is not the same as the values beforehand
                                    database.child("Users").child(userId).child("Debits").child(debit.getID()).child("name").setValue(etEditDebitName.getText().toString());
                                }
                                if (!etEditDebitDeadline.getText().toString().isEmpty() && !etEditDebitDeadline.getText().toString().equals(debit.getDeadline())) {
                                    database.child("Users").child(userId).child("Debits").child(debit.getID()).child("deadline").setValue(etEditDebitDeadline.getText().toString());
                                }
                                if (!etEditDebitSum.getText().toString().isEmpty() && Double.parseDouble(etEditDebitSum.getText().toString()) != debit.getSum()) {
                                    database.child("Users").child(userId).child("Debits").child(debit.getID()).child("sum").setValue(Double.parseDouble(etEditDebitSum.getText().toString()));
                                }
                                if (uriString != null) {
                                    // Deletes old image from Firebase Storage
                                    Uri img = Uri.parse(debit.getImage()); // Gets the URI of the old image from the debit values inside the Firebase database
                                    StorageReference photoRef = FirebaseStorage.getInstance().getReference("Images").child(email[0]).child("image" + img.getLastPathSegment()); // References the image
                                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "Image deleted successfully.");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Uh-oh, an error occurred!
                                            Log.d(TAG, "ERROR: Image was not deleted.");
                                        }
                                    });
                                    database.child("Users").child(email[0]).child("Debits").child(debit.getID()).child("image").setValue(uriString); // replaces old image with the new one
                                }

                                Intent i = new Intent(getContext(), HomeActivity.class);
                                startActivity(i);
                            }
                        });

                        ivCloseEditDebitDialog.setOnClickListener(new View.OnClickListener() {
                            @Override
                            /**
                             * onClick - dismisses the dialog when the close icon is clicked
                             */
                            public void onClick(View v) {
                                editDebitDialog.dismiss();
                            }
                        });

                        editDebitDialog.show();
                    }
                });

            }
        });

        lvDebits.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { //Deletes a debit on long click in adapter

            /**
             * onItemLongClick - this function is called when the user presses a long time on a view
             * @param parent - the AdapterView where the click happened
             * @param view - the view within the AdapterView that was clicked
             * @param position - the position of the view in the ListView
             * @param id - the row id of the item that was clicked
             * @return v - returns the view, in this case the Delete Debit dialog
             */
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final Debt debit = debits.get(position);

                AlertDialog.Builder deleteDebit = new AlertDialog.Builder(getContext());

                deleteDebit.setMessage("Are you sure you want to delete \"" + debit.getName() + "\"? \n\nNote: This action cannot be undone.");
                deleteDebit.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //user chooses to delete the debit

                        // Deletes Firebase Database values
                        String[] parts = prefs.getString(RegisterActivity.EMAIL, "").split("@");
                        String email = parts[0];
                        database.child("Users").child(email).child("Debits").child(debit.getID()).removeValue(); // Removes the value from the user that created the debit
                        database.child("Users").child(debit.getDebtOwner_ID()).child("Debts").child(debit.getID()).removeValue(); // Removes the value from the debt owner
                        // Deletes image from Firebase Storage
                        Uri img = Uri.parse(debit.getImage()); // Gets the URI from the debit values inside the Firebase database
                        StorageReference photoRef = FirebaseStorage.getInstance().getReference("Images").child(email).child("image" + img.getLastPathSegment()); // References the image
                        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Image deleted successfully.");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Uh-oh, an error occurred!
                                Log.d(TAG, "ERROR: Image was not deleted.");
                            }
                        });

                        adapter.remove(debit);
                        adapter.notifyDataSetChanged();
                    }
                });
                deleteDebit.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //user chooses to cancel the action
                    }
                });
                deleteDebit.show();

                return true; //if set to false - will also trigger the short click listener. Setting the return boolean to true ends the action completely.
            }
        });

        return v;
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

                    Bundle bundle = data.getExtras();
                    Bitmap bmp = (Bitmap) bundle.get("data");

                    Uri uri = convertBitmapToUri(getContext(), bmp); // Converting the Bitmap to URI
                    uriString = uri.toString();
                    StorageReference imageName = mStorageRef.child("image" + uri.getLastPathSegment()); // Uploading the image to Firebase Storage
                    imageName.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //
                        }
                    });
                    ivEditDebitPicture.setImageBitmap(bmp);
                }
                break;
            case (REQUEST_GALLERY):
                if (resultCode == RESULT_OK && data != null) {

                    imageURI = data.getData();
                    uriString = imageURI.toString();
                    //Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageURI);
                    //ByteArrayOutputStream out = new ByteArrayOutputStream();
                    //bmp.compress(Bitmap.CompressFormat.JPEG, 30, out);

                    // Uploading the new image (URI) to Firebase Storage
                    // mStorageRef.child(email).child("image" + img.getLastPathSegment())
                    StorageReference imageName = mStorageRef.child("image" + imageURI.getLastPathSegment());
                    imageName.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //
                        }
                    });
                    ivEditDebitPicture.setImageURI(imageURI);
                }
                break;
        }

    }

    /**
     * onClick - this function is called every time a user clicks an item, in this case on click on fabAddDebit, the directToAddDebitPage is called
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fabAddDebit) {
                directToAddDebitPage();
        }
    }

    /**
     * directToAddDebitPage - directs to AddDebitActivity class from DebitsFragment class with an intent
     */
    private void directToAddDebitPage() {
        Intent intent = new Intent(getContext(), AddDebitActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    /**
     * onDateSet - this function is called when a date was selected inside the DatePicker dialog. It takes the date parameters and makes a string that includes them
     * @param view - the selected DatePicker dialog
     * @param year - the selected year from the dialog
     * @param month - the selected month from the dialog
     * @param dayOfMonth - the selected day from the dialog
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String deadline = dayOfMonth + "/" + (month + 1) + "/" + year;
        etEditDebitDeadline.setText(deadline);
    }

    /**
     * directToCamera - opens an alert dialog in which the user will be able to choose between gallery or camera, and then will be directed according to the option he picked to the image gallery, or to the camera accordingly
     */
    private void directToCamera() {
        AlertDialog.Builder cameraOrGalleryDialog = new AlertDialog.Builder(getContext());

        cameraOrGalleryDialog.setMessage("Choose where to pick the image from.");
        cameraOrGalleryDialog.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //user chooses camera
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) { //Checks for user permission to use the camera
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
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
