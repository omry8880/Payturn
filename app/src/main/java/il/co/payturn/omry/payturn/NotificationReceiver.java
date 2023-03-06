package il.co.payturn.omry.payturn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.MODE_PRIVATE;

public class NotificationReceiver extends BroadcastReceiver {

    private SharedPreferences prefs;
    private String userId;

    private Debt debt;

    /**
     * onReceive - as NotificationReceiver was registered in the manifest, this method will be called every time the application receives a notification broadcast.
     * A broadcast to this class will be sent rom the DebtService service activity, and will include an intent with the user preferred action button click.
     * The user will have the option to accept or to decline a debt that was sent to him by someone else, and the option he chooses will be sent here, with actions that follow his decision.
     * @param context - the context from where this class was referenced
     * @param intent - the intent that is received
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        prefs = context.getSharedPreferences(RegisterActivity.SETTING_PREF, MODE_PRIVATE); //Importing register information (Email) to recognize the user currently adding the debit inside Firebase

        userId = prefs.getString(RegisterActivity.EMAIL, "");
        userId = userId.substring(0,userId.indexOf('@')).replace(".","");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference();

        final String action = intent.getAction();
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            ref.child("Users").child(userId).child("Debts").addValueEventListener(new ValueEventListener() {
                /**
                 * onDataChange - every time there's a new update in the "Debts" path of a user inside Firebase, this method will be called.
                 * This method will check if the user accepted/declined the incoming debt through the intent action that was received in onReceive().
                 * If the user accepted the debt, the debt status will turn green (from red beforehand), and if not, it will stay red.
                 * @param dataSnapshot - the new debt object value
                 */
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot debtSnapshot : dataSnapshot.getChildren()) {
                        debt = debtSnapshot.getValue(Debt.class);
                    }

                    if (debt != null) {
                        if (dataSnapshot.hasChild(debt.getID())) {
                            if (action.equals("accept")) {
                                ref.child("Users").child(userId).child("Debts").child(debt.getID()).child("status").setValue(true);
                                ref.child("Users").child(debt.getDebtCollector_ID()).child("Debits").child(debt.getID()).child("status").setValue(true);
                                notificationManager.cancel(1); // dismisses the notification
                                Toast.makeText(context, "Debt was accepted.", Toast.LENGTH_LONG).show();
                            }
                            else if (action.equals("decline")) {
                                notificationManager.cancel(1); // dismisses the notification
                                Toast.makeText(context, "Debt was declined.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }
}
