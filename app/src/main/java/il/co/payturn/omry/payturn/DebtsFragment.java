package il.co.payturn.omry.payturn;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class DebtsFragment extends Fragment {

    private TextView tvEmptyPlaceholderDebts;
    private ListView lv_debts;

    private SharedPreferences prefs;
    String userId;
    private DatabaseReference database;
    private ArrayList<Debt> debts; //Debits List
    private DebtAdapter adapter; //Debits Adapter - required to show the list of debits


    public DebtsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

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
        View v =  inflater.inflate(R.layout.fragment_debts, container, false);

        tvEmptyPlaceholderDebts = (TextView) v.findViewById(R.id.tvEmptyPlaceholderDebts);
        lv_debts = (ListView) v.findViewById(R.id.lv_debts);

        lv_debts.setEmptyView(tvEmptyPlaceholderDebts); //If the ListView is empty, a placeholder "it's empty here..." text will instead appear

        prefs = getActivity().getSharedPreferences(RegisterActivity.SETTING_PREF, MODE_PRIVATE); //Importing register information (Email) to recognize the user currently adding the debit inside Firebase

        userId = prefs.getString(RegisterActivity.EMAIL, "");
        userId = userId.substring(0,userId.indexOf('@')).replace(".","");

        database = FirebaseDatabase.getInstance().getReference();

        database.child("Users").child(userId).child("Debts").addValueEventListener(new ValueEventListener() {

            /**
             * onDataChange - called every time there's a new update to the "Debts" path in Firebase - updates the adapter with the new information and sorts it by order of date
             * @param dataSnapshot
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                debts = new ArrayList<>();
                for (DataSnapshot debtSnapshot : dataSnapshot.getChildren()) {

                    debts.add(debtSnapshot.getValue(Debt.class));

                }

                adapter = new DebtAdapter(debts, getContext());
                lv_debts.setAdapter(adapter);
                Collections.sort(debts);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

                // ...
            }
        });

        lv_debts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * onItemClick - every time an item inside the ListView is clicked, a custom-made dialog will open
             * @param parent - the AdapterView where the click happened
             * @param view - the view inside the AdapterView which was clicked
             * @param position - position of the item inside the adapter
             * @param id - The row id of the item that was clicked
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { // Debt preview dialog

                final Debt debt = debts.get(position);

                final Dialog debtDialog = new Dialog(getContext()); // Shows the debt dialog
                debtDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                debtDialog.setContentView(R.layout.dialog_debt_design);

                // Initializing and replacing the default variables with the debt variables
                TextView tvDialogDebitName = (TextView) debtDialog.findViewById(R.id.tvDialogDebtName);
                TextView tvDialogDebitContact = (TextView) debtDialog.findViewById(R.id.tvDialogDebtContact);
                TextView tvDialogDebitSum = (TextView) debtDialog.findViewById(R.id.tvDialogDebtSum);
                TextView tvDialogDebitDaysUntilDeadline = (TextView) debtDialog.findViewById(R.id.tvDialogDebtDaysUntilDeadline);
                TextView tvDialogDebitStatus = (TextView) debtDialog.findViewById(R.id.tvDialogDebtStatus);
                Button btnPayDebt = (Button) debtDialog.findViewById(R.id.btnPayDebt);

                // Check the status of the debt (if unpaid = false, else = true)
                if (!debt.getStatus()) {
                    tvDialogDebitStatus.setText("Status: Unpaid");
                } else {
                    tvDialogDebitStatus.setText("Status: Paid");
                }

                tvDialogDebitName.setText(debt.getName());
                tvDialogDebitContact.setText("Contact: " + debt.getDebtCollector_ID());
                tvDialogDebitSum.setText(NumberFormat.getNumberInstance(Locale.US).format(debt.getSum()) + "₪");

                // Get relative time to deadline
                String sDeadline= debt.getDeadline();
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
                ParsePosition pos = new ParsePosition(0);
                Date d = sdf.parse(sDeadline, pos);
                long due = d.getTime();
                tvDialogDebitDaysUntilDeadline.setText("Deadline: " + DateUtils.getRelativeTimeSpanString(due, System.currentTimeMillis(), 0L, DateUtils.FORMAT_ABBREV_RELATIVE));

                btnPayDebt.setText("Pay " + debt.getDebtCollector_ID());

                btnPayDebt.setOnClickListener(new View.OnClickListener() { // Pay debt to debt collector on button click
                    @Override
                    public void onClick(View v) {
                        String date = new SimpleDateFormat("d/M/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()); // Get the current (today's) date
                        debt.setDeadline(date); // Changes the deadline to today's date (the date in which the user paid the debt)
                        database.child("Users").child(userId).child("History").child(debt.getID()).setValue(debt);
                        debtDialog.dismiss(); // Close the debt dialog
                    }
                });

                debtDialog.show(); // Show the debt dialog

            }
        });

        return v;
    }


}
