package il.co.payturn.omry.payturn;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DebtService extends Service { // Background Service

    static final String TAG = DebtService.class.getSimpleName();

    private SharedPreferences prefs;
    private String userId;

    private Debt debt;
    private Debt paidDebt;

    private static final int notificationId = 1;
    private static final String CHANNEL_ID = "DebtNotificationChannelID";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Log.d(TAG, "onCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) // Checks for the right API level
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

    }

    /**
     * onStartCommand - this method is called right after the onCreate() of the service, basically serves as the body of the service (which includes what you want the service to do)
     * @param intent - the intent in which the service is called from
     * @param flags - additional information (flags)
     * @param startId - a unique ID given to the service
     * @return returns a global integer related to the Service class, in this case START_NOT_STICKY, which tells the system not to bother to restart the service
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        prefs = getApplication().getSharedPreferences(RegisterActivity.SETTING_PREF, MODE_PRIVATE); //Importing register information (Email) to recognize the user currently adding the debit inside Firebase

        userId = prefs.getString(RegisterActivity.EMAIL, "");
        userId = userId.substring(0,userId.indexOf('@')).replace(".","");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference();

        ref.child("Users").child(userId).child("Debts").addValueEventListener(new ValueEventListener() {
            /**
             * onDataChange - this method is called every time there's a change in the 'Debts' path of a user, and checks if a user has received a new debt. Then, a notification will be sent to him alerting about the new debt. The user will be given a choice to accept or decline it.
             * @param dataSnapshot - the debt object value
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot debtSnapshot : dataSnapshot.getChildren()) {
                    debt = debtSnapshot.getValue(Debt.class);
                }

                if (debt != null) {
                    if (dataSnapshot.hasChild(debt.getID())) {
                        if (!debt.getStatus()) { // Check if notification wasn't sent
                            Log.d(TAG, "sent a notification!");
                            sendDebtVerification(debt); // Send notification
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ref.child("Users").child(userId).child("Debits").addValueEventListener(new ValueEventListener() { // Checks for debit changes in Firebase
            /**
             * onDataChange - this method is called every time there's a change in the 'Debits' path of a user - basically when a new debit was added, and then it adds the debt to the specific user which was specified in the debit details
             * @param dataSnapshot - the debt object value
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot debtSnapshot : dataSnapshot.getChildren()) {
                    debt = debtSnapshot.getValue(Debt.class);
                }

                if (debt != null) {
                    if (dataSnapshot.hasChild(debt.getID())) {
                        ref.child("Users").child(debt.getDebtOwner_ID()).child("Debts").child(debt.getID()).setValue(debt); // adds the debt to the user
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ref.child("Users").child(userId).child("History").addValueEventListener(new ValueEventListener() {
            /**
             * onDataChange - Checks for history changes in Firebase (everytime someone pays back a debt), and then adds the debt/debit to the history page for both sides (debt owner and debt collector)
             * @param dataSnapshot - the debt object value
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot paidDebtSnapshot : dataSnapshot.getChildren()) {
                    paidDebt = paidDebtSnapshot.getValue(Debt.class);
                }
                if (paidDebt != null) {
                    if (dataSnapshot.hasChild(paidDebt.getID())) {
                        Log.d(TAG, "entered history on data change");
                        ref.child("Users").child(userId).child("Debts").child(paidDebt.getID()).removeValue(); // Removes the debt for the user as the user already paid for it.
                        ref.child("Users").child(paidDebt.getDebtCollector_ID()).child("Debits").child(paidDebt.getID()).removeValue(); // Removes the debit from the debt collector as it was already paid
                        ref.child("Users").child(paidDebt.getDebtCollector_ID()).child("History").child(paidDebt.getID()).setValue(paidDebt); // Transfers the debit for the debt collector to the history list view
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        stopSelf();// stops the service

        return START_NOT_STICKY;

    }

    /**
     * onDestroy - this method is called every time the service is 'destroyed' (ends).
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * startMyOwnForeground - method to start the foreground service (for API 26+)
     */
    private void startMyOwnForeground(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // checks API level AGAIN --- if removed, compiler won't recognize that we've already made the check earlier in onCreate()
            String NOTIFICATION_CHANNEL_ID = "il.co.payturn.omry.payturn";
            String channelName = "Debt Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.pt_icon_notification_active)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
        }
    }

    /**
     * createNotificationChannel - Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is new and not in the support library
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = "debt notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * sendDebtVerification - this method creates the notification for the specified debt it was given, and sends it to the user which needs to accept/decline the debt that was sent to him
     * @param debt - the specified debt that was sent to the user
     */
    private void sendDebtVerification(Debt debt) {
        // Create an explicit intent for an Activity in your app
        Intent activityIntent = new Intent(this, DebtsFragment.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);

        Intent accept = new Intent(this, NotificationReceiver.class);
        accept.setAction("accept");
        PendingIntent pIntentAccept = PendingIntent.getBroadcast(this, 0, accept, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent decline = new Intent(this, NotificationReceiver.class);
        decline.setAction("decline");
        PendingIntent pIntentDecline = PendingIntent.getBroadcast(this, 0, decline, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.pt_icon_notification_active)
                .setContentTitle(debt.getDebtCollector_ID() + " has sent you a new debt.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(debt.getDebtCollector_ID() + " has sent you a new debt called \"" + debt.getID() + "\". Would you like to accept it?")
                        .setSummaryText("1 New Debt")
                )
                .setColor(Color.BLUE)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(contentIntent)
                .addAction(R.drawable.pt_icon_notification_active, "Accept",pIntentAccept)
                .addAction(R.drawable.pt_icon_notification_active, "Decline", pIntentDecline)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build()); // notificationId is a unique int for each notification that you must define
    }

}

