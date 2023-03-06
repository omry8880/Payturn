package il.co.payturn.omry.payturn;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class HistoryAdapter extends ArrayAdapter<Debt> {

    public HistoryAdapter(ArrayList<Debt> data, Context context) {
        super(context, R.layout.debit_single_row, data);
    }

    /**
     * getView - the function is for generating item's view of a ListView
     * @param position - get the data item for this position
     * @param convertView - the new view which will be inflated upon loading the listview
     * @param parent - the parent layout which contains the item's view which getView() generates.
     * @return convertView - returns the view that will be displayed in the ListView
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Debt debt = getItem(position);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.debit_single_row, parent, false);

        TextView tvDebitName = (TextView) convertView.findViewById(R.id.tvDebitName);
        TextView tvDebitContact = (TextView) convertView.findViewById(R.id.tvDebitContact);
        TextView tvDebitDeadline = (TextView) convertView.findViewById(R.id.tvDebitDeadline);
        TextView tvDebitSum = (TextView) convertView.findViewById(R.id.tvDebitSum);
        TextView tvDebitAddedDate = (TextView) convertView.findViewById(R.id.tvDebitAddedDate);
        final ImageView ivDebitPicture = (ImageView) convertView.findViewById(R.id.ivDebitPicture);
        ImageView ivStatus = (ImageView) convertView.findViewById(R.id.ivStatus);



        tvDebitName.setText(debt.getName()); //sets the name of the debit

        SharedPreferences prefs = getContext().getSharedPreferences(RegisterActivity.SETTING_PREF, MODE_PRIVATE); // Importing the user ID (Email)
        String[] userID = prefs.getString(RegisterActivity.EMAIL, "").split("@");

        if (debt.getDebtCollector_ID().equals(userID[0])) { // This if statement basically checks if the adapter received a debt or a debit and changes the text accordingly.
            //it checks if the user who made the debt is the current user, which means it was a debit.
            tvDebitContact.setText(debt.getDebtOwner_ID() + " owed you.");
        } else { // else = meaning someone else created the debit, which means it was a debt the current user had to return to another user.
            tvDebitContact.setText("You owed " + debt.getDebtCollector_ID() + "."); //sets the person who owes the money/debt
        }

        // Changes the color of the status circle from red to green (basically once the debt/debit was paid, the status image view in the top right turns from red to green)
        ImageViewCompat.setImageTintList(ivStatus, ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.greenish_done)));

        tvDebitDeadline.setText("Paid in: " + debt.getDeadline()); //sets the date in which the debit deadline expires

        tvDebitSum.setText((DecimalFormat.getNumberInstance(Locale.US).format(debt.getSum()) + "₪")); //Sets the sum and Formats the number in the correct way (adds commas, deletes unnecessary 0's...)

        tvDebitAddedDate.setText("Added on: " + debt.getAddedDate()); //Get the date in which the debit was added

        Uri imageURI = Uri.parse(debt.getImage());
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("Images").child(debt.getDebtCollector_ID());

        Task<Uri> imageTask = mStorageRef.child("image" + imageURI.getLastPathSegment()).getDownloadUrl();
        imageTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
            /**
             * onSuccess - Recieves the task (link reference to the image) from Firebase, and if this is successful - converts the Task object to URI.
             * @param uri - the URI that was created from the Task object we retrieved
             */
            @Override
            public void onSuccess(Uri uri) {
                //use Picasso to insert image
                Picasso.get()
                        .load(uri.toString())
                        .placeholder(R.drawable.pt_loading_dots)
                        .into(ivDebitPicture);
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

}
