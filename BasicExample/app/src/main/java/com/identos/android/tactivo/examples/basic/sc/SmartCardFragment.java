package com.identos.android.tactivo.examples.basic.sc;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.identos.android.tactivo.basic.R;
import com.identos.android.tactivo.examples.basic.OnDeviceConnectionCallback;
import com.identos.android.tactivo.examples.basic.ViewPagerCallback;
import com.identos.android.tactivo.sdk.SmartcardAPI;
import com.identos.android.tactivo.sdk.SmartcardException;

public class SmartCardFragment extends Fragment implements ViewPagerCallback, OnDeviceConnectionCallback, ReadFromCardTask.OnReadCardCallback
{
    private static final String TAG = SmartCardFragment.class.getSimpleName();

    private SmartcardAPI smartcardAPI;

    private ReadFromCardTask readCardTask;

    private ImageView smartcardIcon;
    private TextView message;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            smartcardAPI = new SmartcardAPI(getContext());
        }
        catch (SmartcardException e)
        {
            Log.e(TAG, "Error instantiating SmartcardAPI", e);
            Toast.makeText(getContext(), "Error instantiating SmartcardAPI", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_smart_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        smartcardIcon = view.findViewById(R.id.iv_sc);
        smartcardIcon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startReadTask();
            }
        });

        message = view.findViewById(R.id.tv_message);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    @Override
    public void onShow()
    {
        try
        {
            smartcardAPI.connectReader();
            Toast.makeText(getContext(), "Successfully connected to Smartcard reader", Toast.LENGTH_SHORT).show();
        }
        catch (SmartcardException.NoReaderFound e)
        {
            Log.w(TAG, e);
        }
        catch (SmartcardException e)
        {
            Log.e(TAG, "Unable to connect to Smartcard reader", e);
            Toast.makeText(getContext(), String.format("Unable to connect to Smartcard reader (%s).", e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onHide()
    {
        if (readCardTask != null)
        {
            readCardTask.cancel(true);
            onError();
        }

        try
        {
            resetUI();
            smartcardAPI.disconnectReader();
        }
        catch (SmartcardException e)
        {
            Log.e(TAG, "Error disconnecting smartcard SDK", e);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (readCardTask != null)
        {
            readCardTask.cancel(true);  // prevents onPostExecute from being invoked!
        }

        try
        {
            if (smartcardAPI.isReaderConnected())
            {
                smartcardAPI.disconnectReader();
            }

            smartcardAPI.close();
        }
        catch (SmartcardException e)
        {
            Log.e(TAG, "Error destroying SmartcardAPI", e);
        }
        finally
        {
            smartcardAPI = null;
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

    private void startReadTask()
    {
        resetUI();
        progressBar.setVisibility(View.VISIBLE);
        readCardTask = new ReadFromCardTask(smartcardAPI, this);
        readCardTask.execute();
    }

    private void resetUI()
    {
        message.setText("");
        message.setVisibility(View.INVISIBLE);

        smartcardIcon.setAlpha(0.07f);
        smartcardIcon.setColorFilter(Color.BLACK);
    }

    @Override
    public void onReadSuccessfullyCompleted(String atr)
    {
        progressBar.setVisibility(View.INVISIBLE);

        smartcardIcon.setAlpha(1f);
        smartcardIcon.setColorFilter(getResources().getColor(R.color.colorSuccessGreen));

        message.setVisibility(View.VISIBLE);
        message.setText(getString(R.string.atr, atr));
    }

    @Override
    public void onError()
    {
        progressBar.setVisibility(View.INVISIBLE);

        smartcardIcon.setAlpha(1f);
        smartcardIcon.setColorFilter(getResources().getColor(R.color.colorErrorRed));

        message.setText("");
        message.setVisibility(View.INVISIBLE);

        Toast.makeText(getContext(), "Error reading smartcard", Toast.LENGTH_SHORT).show();
    }
}
