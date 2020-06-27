package com.identos.android.tactivo.examples.basic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.identos.android.tactivo.basic.R;
import com.identos.android.tactivo.examples.basic.bio.BiometryFragment;
import com.identos.android.tactivo.examples.basic.home.HomeFragment;
import com.identos.android.tactivo.examples.basic.sc.SmartCardFragment;
import com.identos.android.tactivo.sdk.TactivoException;
import com.identos.android.tactivo.sdk.TactivoUtils;


public class MainActivity extends SensorPortraitActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private Fragment[] fragments = {new HomeFragment(), new SmartCardFragment(), new BiometryFragment()};
    private ViewPager viewPager;
    private BottomNavigationView navigation;

    private BroadcastReceiver permissionResultBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // From https://developer.android.com/guide/topics/connectivity/usb/host#permission-d
            String action = intent.getAction();
            if (TactivoUtils.ACTION_USB_PERMISSION.equals(action))
            {
                synchronized (this)
                {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    {
                        if (device != null)
                        {
                            Toast.makeText(MainActivity.this, "Permission to talk to the device has been granted.", Toast.LENGTH_SHORT).show();
                            ((OnDeviceConnectionCallback) fragments[viewPager.getCurrentItem()]).onDeviceAttached();
                        }
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Permission has been denied. The app will not work correctly!", Toast.LENGTH_SHORT).show();
                    }

                    /*
                     * anyways, we can unregister the broadcast receiver:
                     * the next time a device is attached it either already has the permission
                     * OR this receiver will be registered before the request-permission dialog is shown
                     * */
                    unregisterReceiver(permissionResultBroadcastReceiver);
                }
            }
        }
    };

    private BroadcastReceiver deviceAttachedBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
            {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null)
                {
                    if (TactivoUtils.isTactivoDevice(device))
                    {
                        Toast.makeText(MainActivity.this, "Tactivo device attached", Toast.LENGTH_SHORT).show();

                        // we unregister the current BroadcastReceiver because two attaches can not happen
                        unregisterReceiver(deviceAttachedBroadcastReceiver);

                        // we register a receiver to listen if the device gets detached
                        registerReceiver(deviceDetachedBroadcastReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));

                        // handle the event
                        if (checkPermission())
                        {
                            ((OnDeviceConnectionCallback) fragments[viewPager.getCurrentItem()]).onDeviceAttached();
                        }
                    }
                }
            }
        }
    };

    private BroadcastReceiver deviceDetachedBroadcastReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            // From https://developer.android.com/guide/topics/connectivity/usb/host#terminating-d
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
            {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null)
                {
                    if (TactivoUtils.isTactivoDevice(device))
                    {
                        Toast.makeText(MainActivity.this, "Tactivo device detached", Toast.LENGTH_SHORT).show();

                        // we unregister the current BroadcastReceiver because two detaches can not happen
                        unregisterReceiver(deviceDetachedBroadcastReceiver);

                        // we register a receiver to listen if the device gets reattached
                        registerReceiver(deviceAttachedBroadcastReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));

                        // handle the event
                        ((OnDeviceConnectionCallback) fragments[viewPager.getCurrentItem()]).onDeviceDetached();
                    }
                }
            }
        }
    };

    private boolean checkPermission()
    {
        boolean hasPermission = false;

        try
        {
            hasPermission = TactivoUtils.hasPermission(this);

            if (!hasPermission)
            {
                Toast.makeText(this, "Please grant permission to talk to the Tactivo device", Toast.LENGTH_SHORT).show();

                registerReceiver(permissionResultBroadcastReceiver, new IntentFilter(TactivoUtils.ACTION_USB_PERMISSION));

                TactivoUtils.showRequestPermissionDialog(this);
            }
        }
        catch (TactivoException.NoDeviceFoundException e)
        {
            Toast.makeText(this, "Error checking permission", Toast.LENGTH_SHORT).show();
            Log.w(TAG, e);
        }

        return hasPermission;
    }

    // use this method to safely unregister any broadcast receiver (e.g. on stop of the activity)
    private void safelyUnregisterReceiver(BroadcastReceiver broadcastReceiver)
    {
        try
        {
            unregisterReceiver(broadcastReceiver);
        }
        catch (IllegalArgumentException ignored)
        {
            // thrown if the receiver has not been registered before.. we can safely ignore this
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new MyPageChangeListener());
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // We check if the device is connected
        if (!TactivoUtils.isUsbDeviceConnected(this))
        {
            Toast.makeText(this, "No Tactivo device connected", Toast.LENGTH_SHORT).show();

            // we register a receiver to listen if the device gets attached
            registerReceiver(deviceAttachedBroadcastReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));

            ((OnDeviceConnectionCallback) fragments[viewPager.getCurrentItem()]).onDeviceDetached(); // in case the device was removed while the activity was paused/stopped
        }
        else
        {
            // we register a receiver to listen if the device gets detached
            registerReceiver(deviceDetachedBroadcastReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
            checkPermission();  // show dialog if permission has not been granted yet
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        safelyUnregisterReceiver(deviceAttachedBroadcastReceiver);
        safelyUnregisterReceiver(deviceDetachedBroadcastReceiver);
        safelyUnregisterReceiver(permissionResultBroadcastReceiver);
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter
    {
        MyFragmentPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int i)
        {
            return fragments[i];
        }

        @Override
        public int getCount()
        {
            return fragments.length;
        }
    }

    private class MyPageChangeListener implements ViewPager.OnPageChangeListener
    {
        private int lastPage = 0;

        @Override
        public void onPageScrolled(int i, float v, int i1)
        {

        }

        @Override
        public void onPageSelected(int i)
        {
            ((ViewPagerCallback) fragments[lastPage]).onHide();
            lastPage = i;

            switch (i)
            {
                case 0:
                    navigation.setSelectedItemId(R.id.navigation_home);
                    break;
                case 1:
                    navigation.setSelectedItemId(R.id.navigation_smartcard);
                    break;
                case 2:
                    navigation.setSelectedItemId(R.id.navigation_biometry);
                    break;
            }

            ((ViewPagerCallback) fragments[i]).onShow();
        }

        @Override
        public void onPageScrollStateChanged(int i)
        {

        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0, true);
                    return true;
                case R.id.navigation_smartcard:
                    viewPager.setCurrentItem(1, true);
                    return true;
                case R.id.navigation_biometry:
                    viewPager.setCurrentItem(2, true);
                    return true;
            }
            return false;
        }
    };
}
