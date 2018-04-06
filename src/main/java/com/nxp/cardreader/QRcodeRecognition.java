package com.nxp.cardreader;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nxp.mifaresdksample.R;

public class QRcodeRecognition extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
//        final Activity activity = this;
//        IntentIntegrator integrator = new IntentIntegrator(activity);
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
//        integrator.setPrompt("scan");
//        integrator.setCameraId(0);
//        integrator.setBeepEnabled(false);
//        integrator.setBarcodeImageEnabled(false);
//        integrator.initiateScan();

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode,data);
//        if(result != null){
//            if(result.getContents() == null){
//                Toast.makeText(this, "you cancelled the scanning", Toast.LENGTH_LONG).show();
//            }
//            else{
//                Toast.makeText(this,result.getContents(),Toast.LENGTH_LONG).show();
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

}
