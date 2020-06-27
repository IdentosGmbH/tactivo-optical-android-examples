package com.identos.android.tactivo.examples.basic.bio;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.identos.android.tactivo.sdk.BiometryAPI;
import com.identos.android.tactivo.sdk.FingerprintImage;
import com.identos.android.tactivo.sdk.TactivoException;

public class CaptureFingerprintTask extends AsyncTask<Void, Void, Bitmap>
{
    private static final int CAPTURE_TIMEOUT = 5000;
    private static final String TAG = CaptureFingerprintTask.class.getSimpleName();

    private BiometryAPI biometryAPI;
    private OnCaptureCallback onCaptureCallback;
    private boolean instant;

    public CaptureFingerprintTask(BiometryAPI biometryAPI, boolean instant, OnCaptureCallback onCaptureCallback)
    {
        this.biometryAPI = biometryAPI;
        this.onCaptureCallback = onCaptureCallback;
        this.instant = instant;
    }

    @Override
    protected Bitmap doInBackground(Void... voids)
    {
        try
        {
            if (biometryAPI.isDeviceConnected())
            {
                FingerprintImage fingerprintImage = instant ? biometryAPI.instantCapture() : biometryAPI.capture(CAPTURE_TIMEOUT);
                return fingerprintImage.convertToBitmap();
            }
        }
        catch (TactivoException e)
        {
            Log.e(TAG, "Error capturing fingerprint", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
        if (onCaptureCallback != null)
        {
            onCaptureCallback.onCapture(bitmap);
        }
    }

    interface OnCaptureCallback
    {
        void onCapture(Bitmap bitmap);
    }
}
