package com.identos.android.tactivo.examples.basic.home;

import android.os.AsyncTask;
import android.util.Log;

import com.identos.android.tactivo.sdk.DeviceInformation;
import com.identos.android.tactivo.sdk.TactivoAPI;
import com.identos.android.tactivo.sdk.TactivoException;

public class GetDeviceInformationTask extends AsyncTask<Void, Void, DeviceInformation>
{
    private static final String TAG = GetDeviceInformationTask.class.getSimpleName();

    private DeviceInformationCallback deviceInformationCallback;
    private TactivoAPI tactivoAPI;

    public GetDeviceInformationTask(TactivoAPI tactivoAPI, DeviceInformationCallback deviceInformationCallback)
    {
        this.deviceInformationCallback = deviceInformationCallback;
        this.tactivoAPI = tactivoAPI;
    }

    @Override
    protected DeviceInformation doInBackground(Void... voids)
    {
        try
        {
            tactivoAPI.connectDevice();
            DeviceInformation deviceInformation = tactivoAPI.getDeviceInformation();
            tactivoAPI.disconnectDevice();

            return deviceInformation;
        }
        catch (TactivoException.NoDeviceFoundException e)
        {
            Log.w(TAG, e);
        }
        catch (TactivoException e)
        {
            Log.e(TAG, "Error retrieving device information", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(DeviceInformation deviceInformation)
    {
        deviceInformationCallback.onDeviceInformationRetrieved(deviceInformation);
    }

    public interface DeviceInformationCallback
    {
        void onDeviceInformationRetrieved(DeviceInformation deviceInformation);
    }
}
