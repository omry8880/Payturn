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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DebitAdapter extends ArrayAdapter<Debt> {

    /**
     * the constructor for the DebitAdapter class
     * @param data - the Debt ArrayList in which all of our debts/debits are stored in
     * @param context - the context of the selected activity
     */
    public DebitAdapter(ArrayList<Debt> data, Context context) {
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

        LayoutInflater inflater = LayoutInflater.from(getContext()); // is used to get the View object which you define in a layout xml
        convertView = inflater.inflate(R.layout.debit_single_row, parent, false);

        TextView tvDebitName = (TextView) convertView.findViewById(R.id.tvDebitName);
        TextView tvDebitContact = (TextView) convertView.findViewById(R.id.tvDebitContact);
        TextView tvDebitDeadline = (TextView) convertView.findViewById(R.id.tvDebitDeadline);
        TextView tvDebitSum = (TextView) convertView.findViewById(R.id.tvDebitSum);
        TextView tvDebitAddedDate = (TextView) convertView.findViewById(R.id.tvDebitAddedDate);
        final ImageView ivDebitPicture = (ImageView) convertView.findViewById(R.id.ivDebitPicture);
        ImageView ivStatus = (ImageView) convertView.findViewById(R.id.ivStatus);

        if (debt.getStatus()) {
            // Changes the color of the status circle from red to green (basically once the debt/debit was approved by the user, the status image view in the top right turns from red to green)
            ImageViewCompat.setImageTintList(ivStatus, ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.greenish_done)));
        } // if not, status stays red

        tvDebitName.setText(debt.getName()); //sets the name of the debit

        tvDebitContact.setText(debt.getDebtOwner_ID() + " owes you."); //sets the person who owes the money/debt

        tvDebitDeadline.setText("Expires: " + debt.getDeadline()); //sets the date in which the debit deadline expires

        tvDebitSum.setText((DecimalFormat.getNumberInstance(Locale.US).format(debt.getSum()) + "₪")); //Sets the sum and Formats the number in the correct way (adds commas, deletes unnecessary 0's...)

        tvDebitAddedDate.setText("Added on: " + debt.getAddedDate()); //Get the date in which the debit was added

        ivDebitPicture.setImageURI(Uri.parse(debt.getImage())); //sets the picture of the debit

        // Return the completed view to render on screen
        return convertView;
    }

}
