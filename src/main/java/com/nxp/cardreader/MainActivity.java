/*
 * =============================================================================
 *
 *                       Copyright (c), NXP Semiconductors
 *
 *                        (C)NXP Electronics N.V.2013
 *         All rights are reserved. Reproduction in whole or in part is
 *        prohibited without the written consent of the copyright owner.
 *    NXP reserves the right to make changes without notice at any time.
 *   NXP makes no warranty, expressed, implied or statutory, including but
 *   not limited to any implied warranty of merchantability or fitness for any
 *  particular purpose, or that the use will not infringe any third party patent,
 *   copyright or trademark. NXP must not be liable for any loss or damage
 *                            arising from its use.
 *
 * =============================================================================
 */

package com.nxp.cardreader;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nxp.mifaresdksample.R;
import com.nxp.nfclib.CardType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.exceptions.NxpNfcLibException;
import com.nxp.nfclib.plus.IPlus;
import com.nxp.nfclib.plus.IPlusSL3;
import com.nxp.nfclib.plus.PlusFactory;


public class MainActivity extends Activity {

    static String packageKey = "8f2a9ad0ff7cc797a1145e5f707c0a47";

    private NxpNfcLib libInstance = null;

    private TextView tv = null;
    private IPlusSL3 plusSL3 = null;
    private static final int STORAGE_PERMISSION_WRITE = 113;

    private CardType mCardType = CardType.UnknownCard;
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();
        initializeLibrary();
        tv = (TextView) findViewById(R.id.tvLog);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @TargetApi(19)
    private void initializeLibrary() {
        libInstance = NxpNfcLib.getInstance();
        try {
            libInstance.registerActivity(this, packageKey);
        } catch (NxpNfcLibException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNewIntent(final Intent intent) {
        cardLogic(intent);
        super.onNewIntent(intent);
    }

    public void getPermission(){
        boolean readPermission = (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (!readPermission) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_WRITE
            );
        }

    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private void cardLogic(final Intent intent) {
        CardType type = CardType.UnknownCard;
        try {
            type = libInstance.getCardType(intent);
        } catch (NxpNfcLibException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        mCardType = CardType.PlusSL3;
        plusSL3 = PlusFactory.getInstance().getPlusSL3(libInstance.getCustomModules());

        try {
            plusSL3.getReader().connect();
            IPlus.CardDetails details = plusSL3.getCardDetails();
            tv.setText("UID : " + bytesToHex(details.uid));

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        libInstance.stopForeGroundDispatch();
    }


    @Override
    protected void onResume() {
        super.onResume();
        libInstance.startForeGroundDispatch();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMISSION_WRITE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Requested permission granted", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(MainActivity.this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }

    }

    public void qr_scan(View view) {
       // showMessage("button clicked",'n');
        Intent intent = new Intent(MainActivity.this, QRcodeRecognition.class);
//        startActivity(intent);
    }
}
