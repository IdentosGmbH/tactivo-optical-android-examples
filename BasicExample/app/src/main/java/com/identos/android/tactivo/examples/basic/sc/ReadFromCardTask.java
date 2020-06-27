package com.identos.android.tactivo.examples.basic.sc;

import android.os.AsyncTask;
import android.util.Log;

import com.identos.android.tactivo.sdk.SmartcardAPI;
import com.identos.android.tactivo.sdk.SmartcardException;
import com.identos.android.tactivo.sdk.TactivoUtils;

public class ReadFromCardTask extends AsyncTask<Void, Void, String>
{
    private static final String TAG = ReadFromCardTask.class.getSimpleName();

    private SmartcardAPI api;
    private OnReadCardCallback onReadCardCallback;

    public ReadFromCardTask(SmartcardAPI api, OnReadCardCallback onReadCardCallback)
    {
        this.api = api;
        this.onReadCardCallback = onReadCardCallback;
    }

    @Override
    protected String doInBackground(Void... voids)
    {
        try
        {
            if (!api.isReaderConnected())
            {
                api.connectReader();
            }

            // loop until the task gets aborted or until a card is detected
            while (!isCancelled())
            {
                if (api.waitForCardStateChange(SmartcardAPI.CARD_PRESENT, 100))
                {
                    return TactivoUtils.byteArrayToHexString(api.getATR());
                }
            }
        }
        catch (SmartcardException.NoReaderFound e)
        {
            Log.w(TAG, e);
        }
        catch (SmartcardException e)
        {
            Log.e(TAG,"Error retrieving ATR", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String atr)
    {
        super.onPostExecute(atr);

        if (onReadCardCallback != null)
        {
            if (atr != null)
            {
                onReadCardCallback.onReadSuccessfullyCompleted(atr);
            }
            else
            {
                onReadCardCallback.onError();
            }
        }
    }

    interface OnReadCardCallback
    {
        void onReadSuccessfullyCompleted(String atr);

        void onError();
    }
}
