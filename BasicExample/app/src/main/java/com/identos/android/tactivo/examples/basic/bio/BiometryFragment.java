package com.identos.android.tactivo.examples.basic.bio;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.identos.android.tactivo.basic.R;
import com.identos.android.tactivo.examples.basic.OnDeviceConnectionCallback;
import com.identos.android.tactivo.examples.basic.ViewPagerCallback;
import com.identos.android.tactivo.sdk.BiometryAPI;
import com.identos.android.tactivo.sdk.TactivoException;

public class BiometryFragment extends Fragment implements ViewPagerCallback, View.OnClickListener, OnDeviceConnectionCallback
{
    private static final String TAG = BiometryFragment.class.getSimpleName();

    private Button captureButton;
    private ImageView fingerprintImageView;
    private ProgressBar progressBar;
    private Switch instantSwitch;

    private BiometryAPI biometryAPI;

    private CaptureFingerprintTask captureFingerprintTask;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            biometryAPI = new BiometryAPI(getContext());
        }
        catch (TactivoException e)
        {
            Log.e(TAG, "Error instantiating BiometryAPI", e);
            Toast.makeText(getContext(), "Error instantiating API", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_biometry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        captureButton = view.findViewById(R.id.bt_capture);
        captureButton.setOnClickListener(this);

        instantSwitch = view.findViewById(R.id.sw_instant);

        fingerprintImageView = view.findViewById(R.id.iv_fingerprint);

        progressBar = view.findViewById(R.id.progress_bar);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.bt_capture:
                setButtonEnabledState(false);
                progressBar.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), R.string.place_finger_on_reader, Toast.LENGTH_SHORT).show();

                captureFingerprintTask = new CaptureFingerprintTask(biometryAPI, instantSwitch.isChecked(), new CaptureFingerprintTask.OnCaptureCallback()
                {
                    @Override
                    public void onCapture(Bitmap bitmap)
                    {
                        if (bitmap != null)
                        {
                            fingerprintImageView.setImageBitmap(bitmap);
                            fingerprintImageView.setAlpha(1f);
                        }
                        else
                        {
                            fingerprintImageView.setAlpha(0.07f);
                            fingerprintImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_fingerprint_black_24dp, null));

                            Toast.makeText(getContext(), R.string.fingerprint_capture_fail, Toast.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.INVISIBLE);
                        setButtonEnabledState(true);
                    }
                });
                captureFingerprintTask.execute();
                break;
        }
    }

    @Override
    public void onShow()
    {
        try
        {
            biometryAPI.connectDevice();
            setButtonEnabledState(true);
            Toast.makeText(getContext(), "Successfully connected to fingerprint reader", Toast.LENGTH_SHORT).show();
        }
        catch (TactivoException.NoDeviceFoundException e)
        {
            Log.w(TAG, e);
        }
        catch (TactivoException e)
        {
            Log.e(TAG, "Unable to connect to fingerprint reader", e);
            Toast.makeText(getContext(), String.format("Unable to connect to fingerprint reader (%s).", e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onHide()
    {
        try
        {
            resetUI();
            biometryAPI.disconnectDevice();
        }
        catch (TactivoException e)
        {
            Log.e(TAG, "Error disconnecting device", e);
        }
        setButtonEnabledState(false);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (captureFingerprintTask != null)
        {
            captureFingerprintTask.cancel(true);    // prevents onPostExecute from being invoked!
        }

        try
        {
            if (biometryAPI.isDeviceConnected())
            {
                biometryAPI.disconnectDevice();
            }

            biometryAPI.close();
        }
        catch (TactivoException e)
        {
            Log.e(TAG, "Error destroying BiometryAPI", e);
        }
        finally
        {
            biometryAPI = null;
        }
    }

    @Override
    public void onDeviceAttached()
    {
        onShow();
    }

    @Override
    public void onDeviceDetached()
    {
        resetUI();
    }

    private void setButtonEnabledState(boolean enabled)
    {
        captureButton.setEnabled(enabled);
        instantSwitch.setEnabled(enabled);
    }

    private void resetUI()
    {
        setButtonEnabledState(false);

        fingerprintImageView.setAlpha(0.07f);
        fingerprintImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_fingerprint_black_24dp, null));
    }

}
