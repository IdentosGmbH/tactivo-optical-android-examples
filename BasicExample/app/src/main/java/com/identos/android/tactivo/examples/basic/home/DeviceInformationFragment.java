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
import com.identos.android.tactivo.sdk.DeviceInformation;

public class DeviceInformationFragment extends Fragment
{
    public static final String DEVICE_INFORMATION = "device_information";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_device_information, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(DEVICE_INFORMATION))
        {
            DeviceInformation deviceInformation = (DeviceInformation) bundle.getSerializable(DEVICE_INFORMATION);

            TextView bootloaderRevision = view.findViewById(R.id.tv_di_bootloader_revision);
            bootloaderRevision.setText(deviceInformation.getBootloaderRevision());

            TextView firmwareRevision = view.findViewById(R.id.tv_di_firmware_revision);
            firmwareRevision.setText(deviceInformation.getFirmwareInformation());

            TextView modelNumber = view.findViewById(R.id.tv_di_model_number);
            modelNumber.setText(deviceInformation.getModelNumber());

            TextView model = view.findViewById(R.id.tv_di_model);
            model.setText(deviceInformation.getModelName());

            TextView pcaRevision = view.findViewById(R.id.tv_di_pca_revision);
            pcaRevision.setText(String.valueOf(deviceInformation.getPcaRevision()));

            TextView serialNumber = view.findViewById(R.id.tv_di_serial_number);
            String serialNumberStr = deviceInformation.getSerialNumber();
            serialNumber.setText(serialNumberStr != null ? serialNumberStr : "");
        }
    }

}
