package il.co.payturn.omry.payturn;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class DebtJobService extends JobService {

    private static final String TAG = "DebtJobService";

    /**
     * onStartJob - called when a job is scheduled, basically starts the job
     * @param jobParameters - contains the parameters used to identify the job
     * @return returns true from this method if your job needs to continue
     */
    @Override
    public boolean onStartJob(JobParameters jobParameters) { // Starts the job
        Intent service = new Intent(getApplicationContext(), DebtService.class); // creates the service intent, links the service class
        if (!isMyServiceRunning(DebtService.class)) {
            getApplicationContext().stopService(service); // the job is placed inside the service
        }
        //getApplicationContext().startService(service); // OLD || PRE-ANDROID 8.0
        ContextCompat.startForegroundService(getApplicationContext(), service); // Starting from Android 8.0, you cannot start a service in background if your application is not in foreground
        HomeActivity.scheduleJob(getApplicationContext()); // calls the method again and again every 10 minutes (as was set inside the static method 'scheduleJob()')
        Log.d(TAG, "entered job class, calling the service");
        return true;
    }

    /**
     * onStopJob - stops the called job
     * @param jobParameters - contains the parameters used to identify the job
     * @return returns true to reschedule the job or false if we want to cancel it
     */
    @Override
    public boolean onStopJob(JobParameters jobParameters) { return true; }

    private boolean isMyServiceRunning(Class<?> serviceClass) { // Checks if the service is already running
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
