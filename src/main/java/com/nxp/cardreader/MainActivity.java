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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nxp.mifaresdksample.R;
import com.nxp.nfclib.CardType;
import com.nxp.nfclib.CustomModules;
import com.nxp.nfclib.KeyType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.exceptions.NxpNfcLibException;
import com.nxp.nfclib.interfaces.IKeyData;
import com.nxp.nfclib.ndef.NdefMessageWrapper;
import com.nxp.nfclib.ndef.NdefRecordWrapper;
import com.nxp.nfclib.plus.IPlus;
import com.nxp.nfclib.plus.IPlusSL0;
import com.nxp.nfclib.plus.IPlusSL1;
import com.nxp.nfclib.plus.IPlusSL3;
import com.nxp.nfclib.plus.PlusFactory;
import com.nxp.nfclib.plus.PlusSL1Factory;
import com.nxp.nfclib.plus.ValueBlockInfo;
import com.nxp.nfclib.utils.NxpLogUtils;

import org.spongycastle.util.Arrays;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author nxp70496 Main start activity.
 */
public class MainActivity extends Activity {

//    private Tag tag;

    public static final String TAG = "SampleTapLinx";

    private static final String ALIAS_KEY_AES128 = "key_aes_128";

    private static final String ALIAS_KEY_2KTDES = "key_2ktdes";

    private static final String ALIAS_KEY_2KTDES_ULC = "key_2ktdes_ulc";

    private static final String ALIAS_DEFAULT_FF = "alias_default_ff";

    private static final String ALIAS_KEY_AES128_ZEROES = "alias_default_00";

    private static final String EXTRA_KEYS_STORED_FLAG = "keys_stored_flag";

    private IKeyData objKEY_2KTDES_ULC = null;
    private IKeyData objKEY_2KTDES = null;
    private IKeyData objKEY_AES128 = null;
    private byte[] default_ff_key = null;
    private IKeyData default_zeroes_key = null;
    private byte[] bytesKey = null;
    /**
     * Package Key.
     */
    static String packageKey = "8f2a9ad0ff7cc797a1145e5f707c0a47";

    /**
     * NxpNfclib instance.
     */
    private NxpNfcLib libInstance = null;
    /**
     * text view instance.
     */
    private TextView tv = null;
    private Cipher cipher = null;
    private IPlusSL3 plusSL3 = null;
    private IvParameterSpec iv = null;
    private static final int STORAGE_PERMISSION_WRITE = 113;

    private CardType mCardType = CardType.UnknownCard;
    private static final String KEY_APP_MASTER = "This is my key  ";
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        boolean readPermission = (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (!readPermission) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_WRITE
            );
        }

        /* Initialize the library and register to this activity */
        initializeLibrary();

        initializeKeys();

     //   initializeCipherinitVector();

		/* Get text view handle to be used further */
        initializeView();


		/* Set the UI handler */
        initializeUIhandler();


    }
    protected byte[] encryptAESData(final byte[] data, final byte[] key)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        final SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        byte[] encdata = cipher.doFinal(data);
        return encdata;
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    private void initializeCipherinitVector() {

		/* Initialize the Cipher */
        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

		/* set Application Master Key */
        bytesKey = KEY_APP_MASTER.getBytes();

		/* Initialize init vector of 16 bytes with 0xCD. It could be anything */
        byte[] ivSpec = new byte[16];
        Arrays.fill(ivSpec, (byte) 0xCD);
        iv = new IvParameterSpec(ivSpec);

    }
    private void initializeKeys() {
        KeyInfoProvider infoProvider = KeyInfoProvider.getInstance(getApplicationContext());

        SharedPreferences sharedPrefs = getPreferences(Context.MODE_PRIVATE);
        boolean keysStoredFlag = sharedPrefs.getBoolean(EXTRA_KEYS_STORED_FLAG, false);
        if (!keysStoredFlag) {
            //Set Key stores the key in persistent storage, this method can be called only once if key for a given alias does not change.
            byte[] ulc24Keys = new byte[24];
            System.arraycopy(SampleAppKeys.KEY_2KTDES_ULC, 0, ulc24Keys, 0, SampleAppKeys.KEY_2KTDES_ULC.length);
            System.arraycopy(SampleAppKeys.KEY_2KTDES_ULC, 0, ulc24Keys, SampleAppKeys.KEY_2KTDES_ULC.length, 8);
            infoProvider.setKey(ALIAS_KEY_2KTDES_ULC, SampleAppKeys.EnumKeyType.EnumDESKey, ulc24Keys);

            infoProvider.setKey(ALIAS_KEY_2KTDES, SampleAppKeys.EnumKeyType.EnumDESKey, SampleAppKeys.KEY_2KTDES);
            infoProvider.setKey(ALIAS_KEY_AES128, SampleAppKeys.EnumKeyType.EnumAESKey, SampleAppKeys.KEY_AES128);
            infoProvider.setKey(ALIAS_KEY_AES128_ZEROES, SampleAppKeys.EnumKeyType.EnumAESKey, SampleAppKeys.KEY_AES128_ZEROS);
            infoProvider.setKey(ALIAS_DEFAULT_FF, SampleAppKeys.EnumKeyType.EnumMifareKey, SampleAppKeys.KEY_DEFAULT_FF);

            sharedPrefs.edit().putBoolean(EXTRA_KEYS_STORED_FLAG, true).commit();
            //If you want to store a new key after key initialization above, kindly reset the flag EXTRA_KEYS_STORED_FLAG to false in shared preferences.
        }
        objKEY_2KTDES_ULC = infoProvider.getKey(ALIAS_KEY_2KTDES_ULC, SampleAppKeys.EnumKeyType.EnumDESKey);
        objKEY_2KTDES = infoProvider.getKey(ALIAS_KEY_2KTDES, SampleAppKeys.EnumKeyType.EnumDESKey);
        objKEY_AES128 = infoProvider.getKey(ALIAS_KEY_AES128, SampleAppKeys.EnumKeyType.EnumAESKey);
        default_zeroes_key = infoProvider.getKey(ALIAS_KEY_AES128_ZEROES, SampleAppKeys.EnumKeyType.EnumAESKey);
        default_ff_key = infoProvider.getMifareKey(ALIAS_DEFAULT_FF);

    }

    /**
     * Initializing the UI thread.
     */
    private void initializeUIhandler() {

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

    }


    /**
     * Initializing the widget, and Get text view handle to be used further.
     */
    private void initializeView() {

		/* Get text view handle to be used further */
        tv = (TextView) findViewById(R.id.tvLog);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setText(R.string.info_string);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/AvenirNextLTPro-MediumCn.otf");
        tv.setTypeface(face);

    }

    /**
     * Initialize the library and register to this activity.
     */
    @TargetApi(19)
    private void initializeLibrary() {
        libInstance = NxpNfcLib.getInstance();
        try {
            libInstance.registerActivity(this, packageKey);
        } catch (NxpNfcLibException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * (non-Javadoc).
     *
     * @param intent NFC intent from the android framework.
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    public void onNewIntent(final Intent intent) {
        cardLogic(intent);
        super.onNewIntent(intent);
    }


    private void cardLogic(final Intent intent) {
        CardType type = CardType.UnknownCard;
        try {
            type = libInstance.getCardType(intent);
        } catch (NxpNfcLibException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        mCardType = CardType.PlusSL3;
        showMessage("Plus SL3 Card detected.", 't');
        tv.setText(" ");

        showMessage("Card Detected : Plus", 'n');
        plusSL3 = PlusFactory.getInstance().getPlusSL3(libInstance.getCustomModules());


        try {
            plusSL3.getReader().connect();

            tv.setText(" ");

            tv.setText("Card Detected : " + plusSL3.getType().getTagName());
            IPlus.CardDetails details = plusSL3.getCardDetails();

            tv.setText("UID : " + bytesToHex(details.uid));

        } catch (Throwable t) {
            t.printStackTrace();
            showMessage("Unknown Error Tap Again!", 't');
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

    /**
     * This will display message in toast or logcat or on screen or all three.
     *
     * @param str   String to be logged or displayed
     * @param where 't' for Toast; 'l' for Logcat; 'd' for Display in UI; 'n' for
     *              logcat and textview 'a' for All
     */
    protected void showMessage(final String str, final char where) {

        switch (where) {

            case 't':
                Toast.makeText(MainActivity.this, "\n" + str, Toast.LENGTH_SHORT)
                        .show();
                break;
            case 'l':
                NxpLogUtils.i(TAG, "\n" + str);
                break;
            case 'd':
                tv.setText(tv.getText() + "\n-----------------------------------\n"
                        + str);
                break;
            case 'a':
                Toast.makeText(MainActivity.this, "\n" + str, Toast.LENGTH_SHORT)
                        .show();
                NxpLogUtils.i(TAG, "\n" + str);
                tv.setText(tv.getText() + "\n-----------------------------------\n"
                        + str);
                break;
            case 'n':
                NxpLogUtils.i(TAG, "Dump Data: " + str);
                tv.setText(tv.getText() + "\n-----------------------------------\n"
                        + str);
                break;
            default:
                break;
        }
        return;
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
        showMessage("button clicked",'n');
        Intent intent = new Intent(MainActivity.this, QRcodeRecognition.class);
//        startActivity(intent);
    }
}
