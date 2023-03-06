package il.co.payturn.omry.payturn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DebtReceiver extends BroadcastReceiver { // broadcast receiver

    /**
     * onReceive - as DebtReceiver was registered in the manifest, this method will be called every time the application receives a broadcast.
     * In this case, this method will reschedule the job scheduler every time the user opens the app AFTER a boot (restart in his phone).
     * @param context - the context from where this class was referenced
     * @param intent - the intent that is received
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Toast.makeText(context, "Boot completed", Toast.LENGTH_SHORT).show();
            HomeActivity.scheduleJob(context);
        }
    }

}
