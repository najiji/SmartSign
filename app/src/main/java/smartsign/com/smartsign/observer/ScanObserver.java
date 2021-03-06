package smartsign.com.smartsign.observer;

/**
 * Created by najiji on 17/09/16.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.sec.android.ngen.common.lib.ssp.Result;
import com.sec.android.ngen.common.lib.ssp.SpsCause;
import com.sec.android.ngen.common.lib.ssp.scanner.Scanlet;
import com.sec.android.ngen.common.lib.ssp.scanner.ScannerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import smartsign.com.smartsign.MainActivity;

/**
 * Observer for submitted Scan operation
 */
public class ScanObserver extends ScannerService.AbstractScanletObserver {

    private static final String TAG = "ScanObserver";

    private Activity activity;

    private ProgressDialog progressDialog;

    private MainActivity.ScanFinishedHandler scanFinishedHandler;

    /**
     * Default constructor.
     *
     * @param handler {@link android.os.Handler} to launch observer on
     */
    public ScanObserver(final Handler handler, Activity activity, MainActivity.ScanFinishedHandler scanFinishedHandler) {
        super(handler);
        this.activity = activity;
        this.scanFinishedHandler = scanFinishedHandler;
    }

    private void showToast(final String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onProgress(final String rid, final Bundle bundle) {
        if (bundle.containsKey(Scanlet.Keys.KEY_JOBID)) {

            int jobId = bundle.getInt(Scanlet.Keys.KEY_JOBID);

            Log.d(TAG, "Received jobID as " + jobId);
            showToast("Job Id " + jobId);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(activity);
                    progressDialog.setTitle("Scanning");
                    progressDialog.setMessage("Please wait...");
                    progressDialog.show();
                }
            });
        }
    }

    @Override
    public void onCancel(final String rid) {
        Log.d(TAG, "Received Scan Cancel");
        showToast("Scan cancelled!");

        progressDialog.dismiss();
    }

    @Override
    public void onComplete(final String rid, final Bundle bundle) {
        Log.d(TAG, "Task completed. ");
        progressDialog.dismiss();

        final ArrayList<String> images = bundle.getStringArrayList(Scanlet.Keys.KEY_FILENAME_LIST);

        if (images != null && images.size() > 0) {
            Log.d(TAG, Arrays.toString(images.toArray()));
            showToast("Images: " + Arrays.toString(images.toArray()));
        }

        if (scanFinishedHandler != null && images != null && images.size() > 0) {
            scanFinishedHandler.scanFinished(images.get(0));
        }
    }

    @Override
    public void onFail(final String rid, final Result result) {
        Log.e(TAG, result.toString());
        showToast("Failed: " + result);
        progressDialog.dismiss();

        if (result.mCode == Result.RESULT_WS_FAILURE) {
            final Result.WSCause cause = Result.getWSCause(result);

            if (cause != null) {
                Log.w(TAG, cause.toString());
            } else {
                Log.w(TAG, "Failed without any cause");
            }

            showToast("Web service cause: " + cause);
        } else {
            final List<SpsCause> causes = result.getSpsCause();

            showToast("Sps results: " + causes);
        }
    }
}
