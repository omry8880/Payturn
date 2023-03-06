package il.co.payturn.omry.payturn;

import android.content.Context;
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

public class DebtAdapter extends ArrayAdapter<Debt> {

    public DebtAdapter(ArrayList<Debt> data, Context context) {
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

        tvDebitContact.setText("You owe " + debt.getDebtCollector_ID() + "."); //sets the person who owes the money/debt

        tvDebitDeadline.setText("Expires: " + debt.getDeadline()); //sets the date in which the debit deadline expires

        tvDebitSum.setText((DecimalFormat.getNumberInstance(Locale.US).format(debt.getSum()) + "₪")); //Sets the sum and Formats the number in the correct way (adds commas, deletes unnecessary 0's...)

        tvDebitAddedDate.setText("Added on: " + debt.getAddedDate()); //Get the date in which the debit was added

        if (debt.getStatus()) {
            // Changes the color of the status circle from red to green (basically once the debt/debit was approved by the user, the status image view in the top right turns from red to green)
            ImageViewCompat.setImageTintList(ivStatus, ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.greenish_done)));
        } // if not, status stays red

        Uri imageURI = Uri.parse(debt.getImage());
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("Images").child(debt.getDebtCollector_ID());

        Task<Uri> imageTask = mStorageRef.child("image" + imageURI.getLastPathSegment()).getDownloadUrl(); //Receives the task from firebase (basically the reference to the image)

        imageTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
            /**
             * onSuccess - this function is inside a addOnSuccessListener which converts the Uri Task we retrieved from Firebase earlier and converts it to Uri. The onSuccess function is called to take the created Uri and work on it.
             * Inside the function, we call Picasso (an external library that uploads images from Firebase to the application) and upload the Uri to the application inside an ImageView.
             * @param uri - the Uri we retrieved from Firebase after the task listener
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
