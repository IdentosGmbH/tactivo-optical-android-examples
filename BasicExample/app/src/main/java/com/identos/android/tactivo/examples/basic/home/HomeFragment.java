package com.identos.android.tactivo.examples.basic.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.identos.android.tactivo.basic.R;
import com.identos.android.tactivo.examples.basic.OnDeviceConnectionCallback;
import com.identos.android.tactivo.examples.basic.ViewPagerCallback;
import com.identos.android.tactivo.sdk.DeviceInformation;
import com.identos.android.tactivo.sdk.TactivoAPI;
import com.identos.android.tactivo.sdk.TactivoException;

public class HomeFragment extends Fragment implements ViewPagerCallback, OnDeviceConnectionCallback
{
    private static final String TAG = HomeFragment.class.getSimpleName();

    private TactivoAPI tactivoAPI;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            tactivoAPI = new TactivoAPI(getContext());
            // we initially call onShow, because or PagerAdapter does not trigger on show for the initial fragment
            onShow();
        }
        catch (TactivoException e)
        {
            Log.e(TAG, "Error instantiating TactivoAPI");
            Toast.makeText(getContext(), "Error instantiating TactivoAPI", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.device_information_container, new NoDeviceConnectedFragment());
        fragmentTransaction.commit();

        progressBar = view.findViewById(R.id.progress_bar);
    }

    @Override
    public void onShow()
    {
        new GetDeviceInformationTask(tactivoAPI, new GetDeviceInformationTask.DeviceInformationCallback()
        {
            @Override
            public void onDeviceInformationRetrieved(DeviceInformation deviceInformation)
            {
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment fragment;

                if (deviceInformation != null)
                {
                    Toast.makeText(getContext(), R.string.successfully_connected, Toast.LENGTH_SHORT).show();

                    fragment = new DeviceInformationFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(DeviceInformationFragment.DEVICE_INFORMATION, deviceInformation);
                    fragment.setArguments(bundle);
                    progressBar.setVisibility(View.INVISIBLE);  // progressBar will be visible after fw upgrade
                }
                else
                {
                    fragment = new NoDeviceConnectedFragment();
                }

                fragmentTransaction.replace(R.id.device_information_container, fragment);
                fragmentTransaction.commit();
            }
        }).execute();
    }

    @Override
    public void onHide()
    {

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        try
        {
            if (tactivoAPI.isDeviceConnected())
            {
                tactivoAPI.disconnectDevice();
            }

            tactivoAPI.close();
        }
        catch (TactivoException e)
        {
            Log.e(TAG, "Error destroying TactivoAPI");
        }
        finally
        {
            tactivoAPI = null;
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
        if (getActivity() != null)
        {
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.device_information_container, new NoDeviceConnectedFragment()).commitAllowingStateLoss();
        }
    }
}
