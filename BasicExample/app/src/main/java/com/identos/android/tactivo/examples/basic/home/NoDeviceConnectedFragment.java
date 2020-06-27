package com.identos.android.tactivo.examples.basic.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.identos.android.tactivo.basic.R;

public class NoDeviceConnectedFragment extends Fragment
{
    public static final String EXTRA_SHOW_TAP_TO_RETRY = "extra_tap_to_retry";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_no_device_connected, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null)
        {
            boolean showTapToRetry = getArguments().getBoolean(EXTRA_SHOW_TAP_TO_RETRY, false);
            if (showTapToRetry)
            {
                TextView tvTapToRetry = view.findViewById(R.id.tv_tap_to_retry);
                tvTapToRetry.setVisibility(View.VISIBLE);
            }
        }
    }
}
