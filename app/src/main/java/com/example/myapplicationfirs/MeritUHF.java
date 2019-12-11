package com.example.myapplicationfirs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import cn.pda.serialport.Tools;

import com.android.volley.NetworkResponse;
import com.android.volley.toolbox.RequestFuture;
import com.example.myapplicationfirs.R;

//erpnext connection volley
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.myapplicationfirs.utils.Constants;
import com.example.myapplicationfirs.utils.CustomUrl;
import com.example.myapplicationfirs.utils.Utility;


//

public class MeritUHF extends AppCompatActivity implements  OnClickListener
{
    //Layouts
    LinearLayout linearlayout_rfid1 ;
    LinearLayout linearlayout_rfid2 ;
    LinearLayout linearlayout_rfid3 ;
    LinearLayout linearlayout_rfid4 ;
    LinearLayout linearlayout_rfid5 ;

    //buttons
    private  Button btnScan;
    private  Button btnScan1;
    private  Button btnScan2;
    private  Button btnScan3;
    private  Button btnScan4;
    private  Button btnScan5;
    private Button btnAssociate;
    private Button btnGetDetails;

    private EditText editRfid1 ;
    private EditText editRfid2 ;
    private EditText editRfid3 ;
    private EditText editRfid4 ;
    private EditText editRfid5 ;
    public static EditText editDocNo ; //accessing by barcode activity

    private TextView tvItemCode;
    private TextView tvEpcLabel;
    private TextView tv_doctype;


    private UhfReader manager;
    private ListView listViewData;
    private ArrayList<EPC> listEPC;
    private ArrayList<String> listepc = new ArrayList<String>();

    //flags
    private boolean startFlag = false;
    private boolean runFlag = true;
    private boolean rfid1Flag = false;
    private boolean rfid2Flag = false;
    private boolean rfid3Flag = false;
    private boolean rfid4Flag = false;
    private boolean rfid5Flag = false;

    //flags
    private SharedPreferences shared;
    private SharedPreferences.Editor editor;
    private TextView textVersion;


    private int power = 0 ;//rate of work
    private int area = 0;
    private int thread_count = 0 ;
    private int start_flag_count = 0 ;

    private KeyReceiver keyReceiver;
    private  Toast toast;

    //erpconnectiom
    String username = "administrator";
    JSONObject jsonObject;

    //rfidValidation
    JSONObject de_associate_rfid_details = new JSONObject();  //de_associate_rfid_details = {"RFID1" : {"duplicate_serial_no":"MeritSystems","matched_tag":"pch_rfid_tag2"}
    String as_ds_updated_details = "" ;

    //rfid_tag_count
    private  String selected_doctype ;
    private String jsonString;
    private JSONObject permitted_doctype_data ;
    private int rfid_tag_count; ;
    private String[] rfid_tag_data ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merit_uhf);
        shared = getSharedPreferences("UhfRfPower", 0);
        editor = shared.edit();
        power = shared.getInt("power", 30);
        area = shared.getInt("area", 3);

        initView();

        Thread thread = new InventoryThread();
        thread.start();
        Util.initSoundPool(this);

        getLoggedInUserData(); //session id checking from login screen
    } //onCreate ends

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        String powerString = "";
//		switch (UhfManager.Power) {
//			case SerialPort.Power_3v3:
//				powerString = "power_3V3";
//				break;
//			case SerialPort.Power_5v:
//				powerString = "power_5V";
//				break;
//			case SerialPort.Power_Scaner:
//				powerString = "scan_power";
//				break;
//			case SerialPort.Power_Psam:
//				powerString = "psam_power";
//				break;
//			case SerialPort.Power_Rfid:
        powerString = "rfid_power";
//				break;
//			default:
//				break;
//		}
        //TextView textView_title_config;
        //textView_title_config = (TextView) findViewById(R.id.textview_title_config);
        //textView_title_config.setText("Port:com" + 13+";Power:" + powerString + " (EU)");
        manager = UhfReader.getInstance();  //gets power from here
        if (manager == null) {
            textVersion.setText("initFails");
            return;
        }
        //debug
        /*if (manager != null) {
            textVersion.setText("getInstance method sucessfuly returned UhfReader's Object");
            return;
        } */

        //debug
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        registerReceiver();

//		Log.e("", "value" + power);
        manager.setOutputPower(power);
        manager.setWorkArea(area);
//		byte[] version_bs = manager.getFirmware();
//		if (version_bs!=null){
//			textView_title_config.append("("+new String(version_bs)+")");
//		}
    }

    @Override
    protected void onPause() {
        startFlag = false;
        btnScan.setText("Start");
        manager.close();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        startFlag = false;
        runFlag = false;
        if (manager != null) {
            manager.close();
        }
        super.onDestroy();
    }

    private void initView() {

        //Layout
        linearlayout_rfid1 = (LinearLayout) findViewById(R.id.linearlayout_rfid1);
        linearlayout_rfid2 = (LinearLayout) findViewById(R.id.linearlayout_rfid2);
        linearlayout_rfid3 = (LinearLayout) findViewById(R.id.linearlayout_rfid3);
        linearlayout_rfid4 = (LinearLayout) findViewById(R.id.linearlayout_rfid4);
        linearlayout_rfid5 = (LinearLayout) findViewById(R.id.linearlayout_rfid5);


        btnScan = (Button)findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this);

        btnScan1 = (Button)findViewById(R.id.btnScan1);
        btnScan1.setOnClickListener(this);

        btnScan2 = (Button)findViewById(R.id.btnScan2);
        btnScan2.setOnClickListener(this);

        btnScan3 = (Button)findViewById(R.id.btnScan3);
        btnScan3.setOnClickListener(this);

        btnScan4 = (Button)findViewById(R.id.btnScan4);
        btnScan4.setOnClickListener(this);

        btnScan5 = (Button)findViewById(R.id.btnScan5);
        btnScan5.setOnClickListener(this);

        btnAssociate = (Button)findViewById(R.id.btnAssociate);
        btnAssociate.setOnClickListener(this);

        btnGetDetails = (Button)findViewById(R.id.btnGetDetails);
        btnGetDetails.setOnClickListener(this);

        editRfid1 = (EditText) findViewById(R.id.editRfid1);
        editRfid2 = (EditText) findViewById(R.id.editRfid2);
        editRfid3 = (EditText) findViewById(R.id.editRfid3);
        editRfid4 = (EditText) findViewById(R.id.editRfid4);
        editRfid5 = (EditText) findViewById(R.id.editRfid5);
        editDocNo = (EditText) findViewById(R.id.editDocNo);

        tvItemCode =  (TextView)  findViewById(R.id.tvItemCodeVal);
        tvEpcLabel = (TextView)findViewById(R.id.tvEpcLabel1);
        textVersion = (TextView) findViewById(R.id.textView_version);
        tv_doctype = (TextView) findViewById(R.id.tv_doctype);

        listEPC = new ArrayList<EPC>();

        //selected_doctype and rfid_tag_count for selected_doctype

        selected_doctype = getIntent().getExtras().getString("selected_doctype");
        tv_doctype.setText(selected_doctype);
        jsonString = getIntent().getExtras().getString("permitted_doctype_data");
        try {
            permitted_doctype_data = new JSONObject(jsonString);
            rfid_tag_count = get_rfid_tag_count( permitted_doctype_data ,selected_doctype );
        } catch (Exception e) {
            Log.e("ERROR",e.toString());
        }

        set_scanning_buttons(rfid_tag_count);

        //End selected_doctype and rfid_tag_count for selected_doctype


        try {
            de_associate_rfid_details.put("RFID_TAG1","empty");
            de_associate_rfid_details.put("RFID_TAG2","empty");
            de_associate_rfid_details.put("RFID_TAG3","empty");
            de_associate_rfid_details.put("RFID_TAG4","empty");
            de_associate_rfid_details.put("RFID_TAG5","empty");
        } catch (Exception e) {
            Log.e("ERROR",e.toString());
        }
    } //initView ends




    class InventoryThread extends Thread {
        private List<TagModel> tagList;
        @Override
        public void run() {
            super.run();
            while (runFlag) {
                if (startFlag) {
                    tagList = manager.inventoryRealTime(); //实时盘存
                    if(tagList != null && !tagList.isEmpty()){
                        //播放提示音
                        Util.play(1, 0);
                        for(TagModel tag:tagList){
                            if(tag == null){
                                String epcStr = "";
//								String epcStr = new String(epc);
                                addToList(listEPC, epcStr, (byte)-1);
                            }else{
                                String epcStr = Tools.Bytes2HexString(tag.getmEpcBytes(), tag.getmEpcBytes().length);
//								String epcStr = new String(epc);
                                addToList(listEPC, epcStr, tag.getmRssi());
                            }

                        }
                    }
                    tagList = null ;
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void addToList(final List<EPC> list, final String epc, final byte rssi)  {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                tvEpcLabel.setText(epc);
                Util.play(1, 0);

                if(rfid1Flag){
                    editRfid1.setText(epc);
                    rfid1Flag = false;

                    //RFID tag1 validattion against all tag1 of all serial numbers
                    String rfid_tag1 = editRfid1.getText().toString() ;

                    if (rfid_tag1 != null){
                        rfid_validation_against_doc("RFID_TAG1",rfid_tag1);
                    }
                }
                if(rfid2Flag){
                    editRfid2.setText(epc);
                    rfid2Flag = false;

                    //RFID tag2 validattion against all tag1 of all serial numbers
                    String rfid_tag2 = editRfid2.getText().toString() ;

                    if (rfid_tag2 != null){
                        rfid_validation_against_doc("RFID_TAG2",rfid_tag2);
                    }
                }
                if(rfid3Flag){
                    editRfid3.setText(epc);
                    rfid3Flag = false;

                    //RFID tag3 validattion against all tag1 of all serial numbers
                    String rfid_tag3 = editRfid3.getText().toString() ;

                    if (rfid_tag3 != null){
                        rfid_validation_against_doc("RFID_TAG3",rfid_tag3);
                    }
                }
                if(rfid4Flag){
                    editRfid4.setText(epc);
                    rfid4Flag = false;

                    //RFID tag4 validattion against all tag1 of all serial numbers
                    String rfid_tag4 = editRfid4.getText().toString() ;

                    if (rfid_tag4 != null){
                        rfid_validation_against_doc("RFID_TAG4",rfid_tag4);
                    }
                }if(rfid5Flag){
                    editRfid5.setText(epc);
                    rfid5Flag = false;

                    //RFID tag5 validattion against all tag1 of all serial numbers
                    String rfid_tag5 = editRfid5.getText().toString() ;

                    if (rfid_tag5 != null){
                        rfid_validation_against_doc("RFID_TAG5",rfid_tag5);
                    }
                }

            }
        });
    } //addlist ends


    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnScan:
                Toast.makeText(MeritUHF.this, "You have clicked Scan Button" ,Toast.LENGTH_SHORT).show();
                System.out.println("***************************scan Button clicked**************************************");

                if (!startFlag) {
                    startFlag = true;
                    btnScan.setText("Stop");
                } else {
                    startFlag = false;
                    btnScan.setText("Start");
                }
                break;
            case R.id.btnGetDetails:
                String doc_type = tv_doctype.getText().toString() ;
                String  doc_no = editDocNo.getText().toString();
                get_rfid_details_ac_doc_number( doc_type,doc_no);
                break;
            case R.id.btnScan1:
                Toast.makeText(MeritUHF.this, "You have clicked RFID Scan Button" ,Toast.LENGTH_SHORT).show();
                if (!startFlag) {
                    startFlag = true;
                    btnScan1.setText("Stop");
                    rfid1Flag = true ;
                } else {
                    startFlag = false;
                    btnScan1.setText("Scan-1");
                }
                break;
            case R.id.btnScan2:
                Toast.makeText(MeritUHF.this, "You have clicked RFID Scan Button" ,Toast.LENGTH_SHORT).show();
                if (!startFlag) {
                    startFlag = true;
                    btnScan2.setText("Stop");
                    rfid2Flag = true;
                } else {
                    startFlag = false;
                    btnScan2.setText("Scan-2");
                }
                break;
            case R.id.btnScan3:
                Toast.makeText(MeritUHF.this, "You have clicked RFID Scan Button" ,Toast.LENGTH_SHORT).show();
                if (!startFlag) {
                    startFlag = true;
                    btnScan3.setText("Stop");
                    rfid3Flag = true;
                } else {
                    startFlag = false;
                    btnScan3.setText("Scan-3");
                }
                break;
            case R.id.btnScan4:
                Toast.makeText(MeritUHF.this, "You have clicked RFID Scan Button" ,Toast.LENGTH_SHORT).show();
                if (!startFlag) {
                    startFlag = true;
                    btnScan4.setText("Stop");
                    rfid4Flag = true;
                } else {
                    startFlag = false;
                    btnScan4.setText("Scan-4");
                }
                break;
            case R.id.btnScan5:
                Toast.makeText(MeritUHF.this, "You have clicked RFID Scan Button" ,Toast.LENGTH_SHORT).show();
                if (!startFlag) {
                    startFlag = true;
                    btnScan5.setText("Stop");
                    rfid5Flag = true;
                } else {
                    startFlag = false;
                    btnScan5.setText("Scan-5");
                }
                break;
            case R.id.btnAssociate :
                System.out.println("***************************Associate Button clicked**************************************");
                boolean is_unique_tags_scanned = true ;

                String[] scanned_rfid_tag_data =  new String[5];
                scanned_rfid_tag_data[0] = editRfid1.getText().toString() ;
                scanned_rfid_tag_data[1] = editRfid2.getText().toString() ;
                scanned_rfid_tag_data[2] = editRfid3.getText().toString() ;
                scanned_rfid_tag_data[3] = editRfid4.getText().toString() ;
                scanned_rfid_tag_data[4] = editRfid5.getText().toString() ;
                boolean is_unique_rfid_tags_scanned=true;


                for (int i = 0; i < scanned_rfid_tag_data.length; i++) {
                    if( scanned_rfid_tag_data[i] != null) {
                        if(scanned_rfid_tag_data[i].trim().length() > 0  ) {
                            System.out.println(scanned_rfid_tag_data[i]);

                            for (int j = i+1; j < scanned_rfid_tag_data.length; j++) {
                                if (scanned_rfid_tag_data[i].equals(scanned_rfid_tag_data[j])) {
                                    System.out.println("duplicate_found"+scanned_rfid_tag_data[i]);

                                    is_unique_rfid_tags_scanned = false ;
                                }
                            }
                        }
                    }
                }
                System.out.println("UNique id scan status :"+is_unique_rfid_tags_scanned);
                if(is_unique_rfid_tags_scanned){
                    associateButtonAction();
                }
                else{
                    Toast.makeText(MeritUHF.this,"Please scan unique RFID tags" , Toast.LENGTH_LONG).show();
                }

                break;
        }
    }//Onclick ends
    private void registerReceiver() {
        keyReceiver = new KeyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        filter.addAction("android.intent.action.FUN_KEY");
        registerReceiver(keyReceiver , filter);
    }

    private class KeyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra("keyCode", 0);
            if (keyCode == 0) {
                keyCode = intent.getIntExtra("keycode", 0);
            }
            boolean keyDown = intent.getBooleanExtra("keydown", false);
            if (keyDown) {
                if (toast == null) {
                    toast = Toast.makeText(MeritUHF.this, "KeyReceiver:keyCode = down" + keyCode, Toast.LENGTH_SHORT);
                } else {
                    toast.setText("KeyReceiver:keyCode = down" + keyCode);
                }
                toast.show();
                switch (keyCode) {
                    case KeyEvent.KEYCODE_F1:
                    case KeyEvent.KEYCODE_F2:
                    case KeyEvent.KEYCODE_F3:
                    case KeyEvent.KEYCODE_F4:
                    case KeyEvent.KEYCODE_F5:
                        onClick(btnScan);
                        break;
                }
            }
        }
    }

    public void set_scanning_buttons(int rfid_tag_count){



        if (rfid_tag_count == 1){
            linearlayout_rfid2.setVisibility(View.INVISIBLE);
            linearlayout_rfid3.setVisibility(View.INVISIBLE);
            linearlayout_rfid4.setVisibility(View.INVISIBLE);
            linearlayout_rfid5.setVisibility(View.INVISIBLE);
        }
        if (rfid_tag_count == 2){
            linearlayout_rfid3.setVisibility(View.INVISIBLE);
            linearlayout_rfid4.setVisibility(View.INVISIBLE);
            linearlayout_rfid5.setVisibility(View.INVISIBLE);
        }
        if (rfid_tag_count == 3){
            linearlayout_rfid4.setVisibility(View.INVISIBLE);
            linearlayout_rfid5.setVisibility(View.INVISIBLE);
        }
        if (rfid_tag_count == 4){
            linearlayout_rfid5.setVisibility(View.INVISIBLE);
        }

    }

    public void startBarCodeScanning(View view) {
        Intent startUtilitiesActivity = new Intent(MeritUHF.this,BarCodeScanning.class);
        startActivity(startUtilitiesActivity);
    }

    private void associateButtonAction( )  {

        final String rf1 = editRfid1.getText().toString();
        final String rf2 = editRfid2.getText().toString();
        final String rf3 = editRfid3.getText().toString();
        final String rf4 = editRfid4.getText().toString();
        final String rf5 = editRfid5.getText().toString();
        final String doc_no_fi = editDocNo.getText().toString();
        final String doc_type_as  = tv_doctype.getText().toString() ;

        System.out.println("*************************** from Associate Button clicked**************************************rf1,rf2"+rf1 +" " +rf2 );
        System.out.println ("***************************from Associate Button clicked de_associate_rfid_details**************************************de_associate_rfid_details "+de_associate_rfid_details );

        //dde_associate_rfid_details : {"RFID_TAG1":{"duplicate_serial_no":"MeritSystems","matched_tag":"pch_rfid_tag2"},"RFID_TAG2":{"duplicate_serial_no":"MeritSystems","matched_tag":"pch_rfid_tag2"}}eAssociate

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (de_associate_rfid_details.getString("RFID_TAG1") != "empty" ){

                        System.out.println("***************************from Associate Button clicked DUPLICATE RFID_TAG1 exist ************************************** " );

                        JSONObject RFID_TAG1_detail = de_associate_rfid_details.getJSONObject("RFID_TAG1");

                        String deas_tag_position   =  RFID_TAG1_detail.getString("rfid_position");
                        String deas_doc_id =  RFID_TAG1_detail.getString("matched_doc_id");
                        String deas_doc_type =  RFID_TAG1_detail.getString("matched_docType");

                        deAssociateRFID(deas_tag_position,deas_doc_id,deas_doc_type);

                        System.out.println("****from Associate Button clicked RFID_TAG1 and before remove data local de_associate_rfid_details:"+de_associate_rfid_details );
                        de_associate_rfid_details.put("RFID_TAG1","empty");
                        System.out.println("***************************from Associate Button clicked RFID_TAG1 and removed data local de_associate_rfid_details:"+de_associate_rfid_details );

                    }

                    if (de_associate_rfid_details.getString("RFID_TAG2") != "empty") {

                        System.out.println("***************************from Associate Button clicked DUPLICATE RFID_TAG2 exist ************************************** " );

                        JSONObject RFID_TAG2_detail = de_associate_rfid_details.getJSONObject("RFID_TAG2");

                        String deas_tag_position   =  RFID_TAG2_detail.getString("rfid_position");
                        String deas_doc_id =  RFID_TAG2_detail.getString("matched_doc_id");
                        String deas_doc_type =  RFID_TAG2_detail.getString("matched_docType");

                        deAssociateRFID(deas_tag_position,deas_doc_id,deas_doc_type);

                        System.out.println("***************************from Associate Button clicked RFID_TAG2 and before remove data local de_associate_rfid_details:"+de_associate_rfid_details );
                        de_associate_rfid_details.put("RFID_TAG2","empty");
                        System.out.println("***************************from Associate Button clicked RFID_TAG2 and removed data local de_associate_rfid_details:"+de_associate_rfid_details );

                    }

                    if (de_associate_rfid_details.getString("RFID_TAG3") != "empty") {

                        System.out.println("***************************from Associate Button clicked DUPLICATE RFID_TAG3 exist ************************************** " );

                        JSONObject RFID_TAG3_detail = de_associate_rfid_details.getJSONObject("RFID_TAG3");

                        String deas_tag_position   =  RFID_TAG3_detail.getString("rfid_position");
                        String deas_doc_id =  RFID_TAG3_detail.getString("matched_doc_id");
                        String deas_doc_type =  RFID_TAG3_detail.getString("matched_docType");

                        deAssociateRFID(deas_tag_position,deas_doc_id,deas_doc_type);

                        System.out.println("***************************from Associate Button clicked RFID_TAG3 and before remove data local de_associate_rfid_details:"+de_associate_rfid_details );
                        de_associate_rfid_details.put("RFID_TAG3","empty");
                        System.out.println("***************************from Associate Button clicked RFID_TAG3 and removed data local de_associate_rfid_details:"+de_associate_rfid_details );

                    }

                    if (de_associate_rfid_details.getString("RFID_TAG4") != "empty") {

                        System.out.println("***************************from Associate Button clicked DUPLICATE RFID_TAG4 exist ************************************** " );

                        JSONObject RFID_TAG4_detail = de_associate_rfid_details.getJSONObject("RFID_TAG4");

                        String deas_tag_position   =  RFID_TAG4_detail.getString("rfid_position");
                        String deas_doc_id =  RFID_TAG4_detail.getString("matched_doc_id");
                        String deas_doc_type =  RFID_TAG4_detail.getString("matched_docType");

                        deAssociateRFID(deas_tag_position,deas_doc_id,deas_doc_type);

                        System.out.println("***************************from Associate Button clicked RFID_TAG4 and before remove data local de_associate_rfid_details:"+de_associate_rfid_details );
                        de_associate_rfid_details.put("RFID_TAG4","empty");
                        System.out.println("***************************from Associate Button clicked RFID_TAG4 and removed data local de_associate_rfid_details:"+de_associate_rfid_details );

                    }
                    if (de_associate_rfid_details.getString("RFID_TAG5") != "empty") {

                        System.out.println("***************************from Associate Button clicked DUPLICATE RFID_TAG5 exist ************************************** " );

                        JSONObject RFID_TAG5_detail = de_associate_rfid_details.getJSONObject("RFID_TAG5");

                        String deas_tag_position   =  RFID_TAG5_detail.getString("rfid_position");
                        String deas_doc_id =  RFID_TAG5_detail.getString("matched_doc_id");
                        String deas_doc_type =  RFID_TAG5_detail.getString("matched_docType");

                        deAssociateRFID(deas_tag_position,deas_doc_id,deas_doc_type);

                        System.out.println("***************************from Associate Button clicked RFID_TAG5 and before remove data local de_associate_rfid_details:"+de_associate_rfid_details );
                        de_associate_rfid_details.put("RFID_TAG5","empty");
                        System.out.println("***************************from Associate Button clicked RFID_TAG5 and removed data local de_associate_rfid_details:"+de_associate_rfid_details );
                    }

                    //validation over


                    //association starts
                    System.out.println(" ************ from association  rfid_tag_count : "+  rfid_tag_count);
                    if(rfid_tag_count == 1){
                        String[] scanned_rfid_tag_data =  new String[rfid_tag_count];
                        scanned_rfid_tag_data[0] = rf1 ;
                        if(rf1.trim().length() > 0 ){
                            associateRFIDTags(username,scanned_rfid_tag_data,doc_type_as,doc_no_fi);
                            System.out.println("***************************from Associate Button clicked  associateRFIDTags  fun  called ");
                        }
                    }else if (rfid_tag_count == 2){
                        String[] scanned_rfid_tag_data =  new String[rfid_tag_count];
                        scanned_rfid_tag_data[0] = rf1 ;
                        scanned_rfid_tag_data[1] = rf2 ;

                        if(rf1.trim().length() > 0 && rf2.trim().length() > 0){
                            associateRFIDTags(username,scanned_rfid_tag_data,doc_type_as,doc_no_fi);
                            System.out.println("*************serial_num*from Associate Button clicked  associateRFIDTags  fun  called rf1:"+"rf2"+rf2);
                        }else{
                            System.out.println("*************Couldnot associate"+"rf2"+rf2);

                        }
                    }
                    else if (rfid_tag_count == 3){
                        String[] scanned_rfid_tag_data =  new String[rfid_tag_count];
                        scanned_rfid_tag_data[0] = rf1 ;
                        scanned_rfid_tag_data[1] = rf2 ;
                        scanned_rfid_tag_data[2] = rf3 ;

                        if(rf1 != null && rf2 != null && rf3 != null){
                            associateRFIDTags(username,scanned_rfid_tag_data,doc_type_as,doc_no_fi);
                            System.out.println("***************************from Associate Button clicked  associateRFIDTags  fun  called ");
                        }
                    }
                    else if (rfid_tag_count == 4){
                        String[] scanned_rfid_tag_data =  new String[rfid_tag_count];
                        scanned_rfid_tag_data[0] = rf1 ;
                        scanned_rfid_tag_data[1] = rf2 ;
                        scanned_rfid_tag_data[2] = rf3 ;
                        scanned_rfid_tag_data[3] = rf4 ;

                        if(rf1 != null && rf2 != null && rf3 != null && rf4 != null ){
                            associateRFIDTags(username,scanned_rfid_tag_data,doc_type_as,doc_no_fi);
                            System.out.println("***************************from Associate Button clicked  associateRFIDTags  fun  called ");
                        }
                    }
                    else if (rfid_tag_count == 5){
                        String[] scanned_rfid_tag_data =  new String[rfid_tag_count];
                        scanned_rfid_tag_data[0] = rf1 ;
                        scanned_rfid_tag_data[1] = rf2 ;
                        scanned_rfid_tag_data[2] = rf3 ;
                        scanned_rfid_tag_data[3] = rf4 ;
                        scanned_rfid_tag_data[4] = rf5 ;

                        if(rf1 != null && rf2 != null && rf3 != null && rf4 != null && rf5 != null){
                            associateRFIDTags(username,scanned_rfid_tag_data,doc_type_as,doc_no_fi);
                            System.out.println("***************************from Associate Button clicked  associateRFIDTags  fun  called ");
                        }
                    }
                } catch (JSONException e) {
                    System.out.println("***************************from Associate Button clicked Error in try ************************************** "+e );
                }
            }
        });
        thread.start();
        System.out.println("***************************from Associate Button clicked Sample  check for associateRFIDTags  fun ");

    }



    private int  get_rfid_tag_count(JSONObject permitted_doctype_data,String selected_doctype) throws JSONException {

        System.out.println("***************************from get_rfid_tag_count permitted_doctype_data :"+permitted_doctype_data+"selected_doctype :"+selected_doctype);

        JSONArray jsonArray = permitted_doctype_data.getJSONArray("message");
        String temp_pemitted_doctypes[]= new String[jsonArray.length()];
        int rfid_tag_count = 0;

        if (jsonArray.length() != 0 ){  //valid doc no
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject objects = jsonArray.getJSONObject(i);
                String doctype = objects.getString("permitted_doctype");

                if( doctype.equals( selected_doctype )){
                    rfid_tag_count = objects.getInt("number_of_rfid_tags_per_record");
                }

            }
        }
        else{ //pemitted_doctypes  has not been configured case
        }
        return rfid_tag_count;
    }

    private void getLoggedInUserData() {

        //using this function top lines to check urls
        String scanned_rfid_tag_data = "5848161" ;



        System.out.println("Suresh ************ From getLoggedInUserData  Entered ");
        String myUrl2 = Utility.getInstance().buildUrl(CustomUrl.API_METHOD, null, CustomUrl.GET_LOGGED_USER);
        //String myUrl2 = "http://192.168.0.15/api/method/frappe.auth.get_logged_user";//localhost url
        System.out.println("Suresh ************ From getLoggedInUserData  customurl came "+myUrl2);

        //?fields=["pch_rfid_tag1","pch_rfid_tag2","item_code"]&filters=[["Serial%20No","name","=","dummy"]]

        String  fields_str = "[\"pch_rfid_tag1\",\"pch_rfid_tag2\",\"item_code\"]" ;
        String  filters = "[[\"Serial%20No\",\"name\",\"=\",\"dummy\"]]" ;




        //get_rfid_details_ac_doc_number
        //http:/api/resource/Serial%20No?fields=["pch_rfid_tag1","pch_rfid_tag2","item_code"]&filters=[["Serial%20No","name","=","dummy"]]


        String serial_no = "dummy";
        String doc_name = "Stock  Entry ";
        String doc_no = "10000";

        doc_name = doc_name.trim();
        doc_name = doc_name.replaceAll(" +", "%20");


        String url = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE,null,null);
        url += "/"+doc_name +"?fields=[\"pch_rfid_tag1\",\"pch_rfid_tag2\",\"item_code\"]&filters=[[\""+doc_name +"\",\"name\",\"=\",\""+ doc_no+"\"]]" ;
        System.out.println("Suresh ************ From getLoggedInUserData  get_rfid_details_ac_doc_number came "+url);

       /* Sample url checkings
       //actual --> String rfid_val_url = "http://192.168.0.15/api/resource/Serial%20No?fields=[\"name\"]&filters=[[\"Serial%20No\",\"pch_rfid_tag1\",\"=\",\""+rfid_tag+ "\"]]";//localhost url
        String rfid_tag = "178999";
        String rfid_validation1 = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE, null, CustomUrl.SERIAL_NO);
        rfid_validation1 += "?fields=[\"name\"]&filters=[[\"Serial%20No\",\"pch_rfid_tag1\",\"=\",\""+rfid_tag+ "\"]] " ;
        System.out.println("Suresh ************ From getLoggedInUserData  rfid_validation1 came "+rfid_validation1);
        //actual --> String rfid_val_url = "http://192.168.0.15/api/resource/Serial%20No?fields=[\"name\"]&filters=[[\"Serial%20No\",\"pch_rfid_tag2\",\"=\",\""+rfid_tag+ "\"]]";//localhost url
        String rfid_validation2 = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE, null, CustomUrl.SERIAL_NO);
        rfid_validation2 += "?fields=[\"name\"]&filters=[[\"Serial%20No\",\"pch_rfid_tag2\",\"=\",\""+rfid_tag+ "\"]] " ;
        System.out.println("Suresh ************ From getLoggedInUserData  rfid_validation2 came "+rfid_validation2);
        //deassociate
        String deassociate = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE, null, CustomUrl.SERIAL_NO,serialNum);
        System.out.println("Suresh ************ From getLoggedInUserData  deassociate came "+deassociate);
        */

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //String myUrl2 = "http://192.168.0.62:8000/api/method/frappe.auth.get_logged_user"; //dev lap url

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, myUrl2, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("Suresh ************ From getLoggedInUserData  Response came  ");
                try {
                    JSONObject object = new JSONObject(response);
                    //String loggedInUser = object.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }//end of sucess responseP
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Suresh ************ From getLoggedInUserData  Error in request  came  ");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return MeritUHF.this.getHeaders();
            }

        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(30 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    public Map<String, String> getHeaders () {
        Map<String, String> headers = new HashMap<>();
        SharedPreferences prefs = this.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        String userId = prefs.getString(Constants.USER_ID, null);
        String sid = prefs.getString(Constants.SESSION_ID, null);
        headers.put("user_id", userId);
        headers.put("sid", sid);

        System.out.println("Suresh ************ From Home userId : "+ userId);
        System.out.println("Suresh ************ From Home sid : "+ sid);

        return headers;
    } //end of getuser data

    private void associateRFIDTags(String username,final String[] scanned_rfid_tag_data,final String doc_type, final String doc_no) throws JSONException {

        System.out.println("**********Enters associateRFIDTags");
        System.out.println("********** from associateRFIDTags scanned_rfid_tag_data array : "+ Arrays.toString(scanned_rfid_tag_data)+"doc_type :"+doc_type +"doc_no :"+doc_no);

        String asso_url = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE, null, doc_type,doc_no);
        System.out.println("*************Suresh from associateRFIDTags asso_url :: "+ asso_url);

        //new_url += "/"+ doc_type  + "/" + doc_no ;
        JSONObject scanned_rfid_tags_data_list = new JSONObject();

        for (int i = 0; i < scanned_rfid_tag_data.length ; i++) {
            String key = "pch_rfid_tag" + (i+1) ;
            scanned_rfid_tags_data_list.put(key,scanned_rfid_tag_data[i]);
        }

        System.out.println("*****from associateRFIDTags scanned_rfid_tags_data_list"+ scanned_rfid_tags_data_list);

        final RequestQueue requestQueue = Volley.newRequestQueue(MeritUHF.this);

        JsonObjectRequest JsonRequest = new JsonObjectRequest(Request.Method.PUT, asso_url,scanned_rfid_tags_data_list,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        System.out.println("*********from associateRFIDTags JSON Object Response came  for associateRFIDTags"+response.toString());

                        try{
                            JSONObject dataObject = response.getJSONObject("data");



                            if(rfid_tag_count == 1){
                                String updated_rfid_tag1 = dataObject.getString("pch_rfid_tag1");

                                if (updated_rfid_tag1.equals( scanned_rfid_tag_data[0] ) )
                                {
                                    as_ds_updated_details += doc_type+" : "+ doc_no +" has been associated with given RFID Tags" ;
                                    rfidTagDetailsEntry(scanned_rfid_tag_data ,doc_type ,doc_no );
                                }
                            }else if (rfid_tag_count == 2){

                                String updated_rfid_tag1 = dataObject.getString("pch_rfid_tag1");
                                String updated_rfid_tag2 = dataObject.getString("pch_rfid_tag2");

                                System.out.println("******* from associateRFIDTags updated_rfid_tag1 :"+updated_rfid_tag1 + "updated_rfid_tag2" +updated_rfid_tag2);

                                if (updated_rfid_tag1.equals(scanned_rfid_tag_data[0]) && updated_rfid_tag2.equals(scanned_rfid_tag_data[1])  ){
                                    System.out.println("******* from associateRFIDTags came inside  ifupdated_rfid_tag1 ");

                                    as_ds_updated_details += doc_type+" : "+ doc_no +" has been associated with given RFID Tags" ;
                                    rfidTagDetailsEntry(scanned_rfid_tag_data ,doc_type ,doc_no );

                                }
                            }else if (rfid_tag_count == 3){

                                String updated_rfid_tag1 = dataObject.getString("pch_rfid_tag1");
                                String updated_rfid_tag2 = dataObject.getString("pch_rfid_tag2");
                                String updated_rfid_tag3 = dataObject.getString("pch_rfid_tag3");


                                System.out.println("******* from associateRFIDTags updated_rfid_tag1 :"+updated_rfid_tag1 + "updated_rfid_tag2" +updated_rfid_tag2 + "updated_rfid_tag3"+ updated_rfid_tag3);

                                if (updated_rfid_tag1.equals(scanned_rfid_tag_data[0]) && updated_rfid_tag2.equals(scanned_rfid_tag_data[1]) && updated_rfid_tag3.equals(scanned_rfid_tag_data[2])  ){
                                    System.out.println("******* from associateRFIDTags came inside  ifupdated_rfid_tag1 ");
                                    as_ds_updated_details += doc_type+" : "+ doc_no +" has been associated with given RFID Tags" ;
                                    rfidTagDetailsEntry(scanned_rfid_tag_data ,doc_type ,doc_no );

                                }
                            }else if (rfid_tag_count == 4){

                                String updated_rfid_tag1 = dataObject.getString("pch_rfid_tag1");
                                String updated_rfid_tag2 = dataObject.getString("pch_rfid_tag2");
                                String updated_rfid_tag3 = dataObject.getString("pch_rfid_tag3");
                                String updated_rfid_tag4 = dataObject.getString("pch_rfid_tag4");


                                System.out.println("******* from associateRFIDTags updated_rfid_tag1 :"+updated_rfid_tag1 + "updated_rfid_tag2" +updated_rfid_tag2 + "updated_rfid_tag3"+ updated_rfid_tag3 + "updated_rfid_tag4"+ updated_rfid_tag4);

                                if (updated_rfid_tag1.equals(scanned_rfid_tag_data[0]) && updated_rfid_tag2.equals(scanned_rfid_tag_data[1]) && updated_rfid_tag3.equals(scanned_rfid_tag_data[2]) && updated_rfid_tag4.equals(scanned_rfid_tag_data[3])  ){
                                    System.out.println("******* from associateRFIDTags came inside  ifupdated_rfid_tag1 ");

                                    as_ds_updated_details += doc_type+" : "+ doc_no +" has been associated with given RFID Tags" ;
                                    rfidTagDetailsEntry(scanned_rfid_tag_data ,doc_type ,doc_no );

                                }
                            }else if (rfid_tag_count == 5){

                                String updated_rfid_tag1 = dataObject.getString("pch_rfid_tag1");
                                String updated_rfid_tag2 = dataObject.getString("pch_rfid_tag2");
                                String updated_rfid_tag3 = dataObject.getString("pch_rfid_tag3");
                                String updated_rfid_tag4 = dataObject.getString("pch_rfid_tag4");
                                String updated_rfid_tag5 = dataObject.getString("pch_rfid_tag5");

                                System.out.println("******* from associateRFIDTags updated_rfid_tag1 :"+updated_rfid_tag1 + "updated_rfid_tag2" +updated_rfid_tag2 + "updated_rfid_tag3"+ updated_rfid_tag3 + "updated_rfid_tag4"+ updated_rfid_tag4 + "updated_rfid_tag5"+ updated_rfid_tag5);

                                if (updated_rfid_tag1.equals(scanned_rfid_tag_data[0]) && updated_rfid_tag2.equals(scanned_rfid_tag_data[1]) && updated_rfid_tag3.equals(scanned_rfid_tag_data[2]) && updated_rfid_tag4.equals(scanned_rfid_tag_data[3]) && updated_rfid_tag5.equals(scanned_rfid_tag_data[4])  ){
                                    System.out.println("******* from associateRFIDTags came inside  ifupdated_rfid_tag1 ");

                                    as_ds_updated_details += doc_type+" : "+ doc_no +" has been associated with given RFID Tags" ;
                                    rfidTagDetailsEntry(scanned_rfid_tag_data ,doc_type ,doc_no );

                                }
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }

                        String dialog_title = "Association Details"; //{"duplicate_serial_no":"MeritSystems","matched_tag":"pch_rfid_tag2"}

                        AlertDialog.Builder builder = new AlertDialog.Builder(MeritUHF.this);

                        builder.setMessage(as_ds_updated_details).setTitle(dialog_title)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        System.out.println("*************************** Dialog box  Ok clicked**************************************");

                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();
                        as_ds_updated_details ="";

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("****************************JSON Object Erro responce came  for associateRFIDTags**************************************");
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return MeritUHF.this.getHeaders_one();
            }

        };
        requestQueue.add(JsonRequest);
    }

    private void rfidTagDetailsEntry(final String[] scanned_rfid_tag_data,final String doc_type, final String doc_no) throws JSONException {

        for (int i = 0; i < scanned_rfid_tag_data.length ; i++) {
            set_rfidTagDetailsEntry(scanned_rfid_tag_data[i],i+1,doc_type,doc_no) ;
        }

    } // end rfidTagDetailsEntry

    private void set_rfidTagDetailsEntry(final String scanned_rfid_tag_data,final  int rfid_tag_index ,final String doc_type, final String doc_no) throws JSONException {

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String get_rfidTagDetails_url = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE, null, CustomUrl.RFID_TAG_HISTORY_TABLE);
        get_rfidTagDetails_url += "?fields=[\"name\"]&filters=[[\"RFID%20Tag%20Details\",\"rfid_tag\",\"=\",\""+scanned_rfid_tag_data+ "\"]] " ;
        System.out.println("***** From set_rfidTagDetailsEntry get_rfidTagDetails_url" +get_rfidTagDetails_url);

        //{"data":[{"name":"RFID-Tag-00001"}]}
        JsonObjectRequest JsonRequest = new JsonObjectRequest(Request.Method.GET, get_rfidTagDetails_url,null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("***** From response  set_rfidTagDetailsEntry   : "+response );
                        try{

                            JSONArray jsonArray = response.getJSONArray("data");

                            if (jsonArray.length() != 0 ){  //RFID tag already exist
                                JSONObject objects = jsonArray.getJSONObject(0);
                                String matched_rfid_tag_details_name = objects.getString("name");
                                System.out.println("***** From response  set_rfidTagDetailsEntry  matched_rfid_tag_details_name : "+matched_rfid_tag_details_name );
                                update_rfidTagDetailsDoc( scanned_rfid_tag_data,rfid_tag_index ,doc_type,doc_no,matched_rfid_tag_details_name) ;

                            }
                            else{ //create new RFID tag details doc
                                create_rfidTagDetailsDoc( scanned_rfid_tag_data,rfid_tag_index ,  doc_type,   doc_no) ;
                            }

                        }catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("***************From  set_rfidTagDetailsEntry  error : "+error );
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return MeritUHF.this.getHeaders();
            }
        };
        requestQueue.add(JsonRequest);

    } // end rfidTagDetailsEntry

    private void create_rfidTagDetailsDoc(final String scanned_rfid_tag_data,final int  rfid_tag_index,final String doc_type, final String doc_no ) throws JSONException {

        System.out.println("***********Enters create_rfidTagDetailsDoc " );
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String create_rfidTagDetailsDoc_url = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE, null, CustomUrl.RFID_TAG_HISTORY_TABLE);
        System.out.println(" ******** From update_create_rfidTagDetailsEntry get_rfidTagDetails_url" +create_rfidTagDetailsDoc_url);

        String rfid_tag_position =  "RFID Tag" + rfid_tag_index;
        String manufacturer = "Suresh" ;
        JSONArray rfid_tag_deails_item_doc = new JSONArray();

        JSONObject rfid_tag_row = new JSONObject();
        rfid_tag_row.put("tag_association",rfid_tag_position);
        rfid_tag_row.put("pch_rfid_docid_associated_with",doc_no);
        rfid_tag_row.put("pch_rfid_doctype_associated_with",doc_type);
        rfid_tag_row.put("idx",1);

        rfid_tag_deails_item_doc.put(rfid_tag_row);

        JSONObject rfid_tag_deails_doc = new JSONObject();

        //rfid_tag_deails_doc.put("pch_manufacturer",manufacturer);
        rfid_tag_deails_doc.put("rfid_tag",scanned_rfid_tag_data);
        rfid_tag_deails_doc.put("rfid_tag_association_details",rfid_tag_deails_item_doc);

        System.out.println("***********Enters create_rfidTagDetailsDoc  rfid_tag_deails_item_doc json : "+rfid_tag_deails_doc );


        JsonObjectRequest JsonRequest = new JsonObjectRequest(Request.Method.POST, create_rfidTagDetailsDoc_url,rfid_tag_deails_doc,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("***************From  create_rfidTagDetailsDoc  response : "+response );
                        try{

                            JSONArray jsonArray = response.getJSONArray("message");
                            String temp_pemitted_doctypes[]= new String[jsonArray.length()];

                        }catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("***************From  create_rfidTagDetailsDoc  error : "+error );
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return MeritUHF.this.getHeaders_one();
            }
        };
        requestQueue.add(JsonRequest);

    } // End  create_rfidTagDetailsDoc

    public Map<String, String> getHeaders_one () {
        Map<String, String> headers = new HashMap<>();
        SharedPreferences prefs = this.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        String userId = prefs.getString(Constants.USER_ID, null);
        String sid = prefs.getString(Constants.SESSION_ID, null);
        headers.put("user_id", userId);
        headers.put("sid", sid);
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        return headers;
    }

    //validation functions  // {"duplicate_serial_no":"MeritSystems","matched_tag":"pch_rfid_tag2"}
    private void rfid_validation_against_serno(final String tagName, String rfid_tag) {

        System.out.println("*****************Enters rfid_validation_against_serno for  tagName :" +tagName+ " rfid_tag : " +rfid_tag);


        RequestQueue requestQueue1 = Volley.newRequestQueue(this);

        String rfid_val_url = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE, null, CustomUrl.SERIAL_NO);
        rfid_val_url += "?fields=[\"name\"]&filters=[[\"Serial%20No\",\"pch_rfid_tag1\",\"=\",\""+rfid_tag+ "\"]] " ;

        //String rfid_val_url = "http://192.168.0.15/api/resource/Serial%20No?fields=[\"name\"]&filters=[[\"Serial%20No\",\"pch_rfid_tag1\",\"=\",\""+rfid_tag+ "\"]]";//localhost url

        System.out.println(" From rfid_validation_against_serno rfid_val_url" +rfid_val_url);

        //JSon Request
        JsonObjectRequest JsonRequest = new JsonObjectRequest(Request.Method.GET, rfid_val_url,null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        try {
                            System.out.println("From  response of "+tagName+"  validation response :   "+response );

                            JSONArray jsonArray = response.getJSONArray("data");

                            if (jsonArray.length() != 0 ){                                //{"data":[{"name":"MeritSystems"}]}

                                System.out.println("From  response of "+tagName+" rfid_validation_against_serno jsonArray  "+jsonArray );

                                //for sure only one duplicate value will be there so i am itreating array
                                JSONObject objectInArray = jsonArray.getJSONObject(0);
                                System.out.println("From  response of "+tagName+" rfid_validation_against_serno objectInArray  "+objectInArray );

                                String duplicate_serial_no = objectInArray.getString("name");
                                System.out.println("From  response of "+tagName+" rfid_validation_against_serno duplicate_serial_no  "+duplicate_serial_no );

                                JSONObject dup_rfid_tag_details = new JSONObject() ;


                                dup_rfid_tag_details.put("duplicate_serial_no",duplicate_serial_no);
                                dup_rfid_tag_details.put("matched_tag","pch_rfid_tag1");   //{"duplicate_serial_no":"MeritSystems","matched_tag":"pch_rfid_tag2"}

                                System.out.println("From  response of "+tagName+" rfid_validation_against_serno dup_rfid_tag_details  "+dup_rfid_tag_details );
                                show_alert_dialog(tagName,dup_rfid_tag_details);
                                System.out.println("From  response of "+tagName+" called alert box" );
                                //alert box rfid1

                            }
                            else{
                                System.out.println("From  response of "+tagName+" rfid_validation_against_serno no matching found for pch_rfid_tag1 " );
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Suresh ************ Error rfid_validation_against_serno response type " + error);
                        Toast.makeText(getApplicationContext(),"Error in getLoggedInUserData connection" , Toast.LENGTH_LONG).show();
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return MeritUHF.this.getHeaders();
            }

        };
        requestQueue1.add(JsonRequest);



        //req2
        RequestQueue requestQueue2 = Volley.newRequestQueue(this);

        String rfid_val_url2 = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE, null, CustomUrl.SERIAL_NO);
        rfid_val_url2 += "?fields=[\"name\"]&filters=[[\"Serial%20No\",\"pch_rfid_tag2\",\"=\",\""+rfid_tag+ "\"]] " ;

        //String rfid_val_url2 = "http://192.168.0.15/api/resource/Serial%20No?fields=[\"name\"]&filters=[[\"Serial%20No\",\"pch_rfid_tag2\",\"=\",\""+rfid_tag+ "\"]]";//localhost url

        System.out.println("Suresh ************ From rfid_validation_against_serno rfid_val_url2" +rfid_val_url2);

        //JSon Request
        JsonObjectRequest JsonRequest2 = new JsonObjectRequest(Request.Method.GET, rfid_val_url2,null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        try {
                            System.out.println("From  response of "+tagName+"  validation response :   "+response );
                            JSONArray jsonArray = response.getJSONArray("data");


                            if (jsonArray.length() != 0 ){
                                //{"data":[{"name":"MeritSystems"}]}

                                System.out.println("From  response of "+tagName+" rfid_validation_against_serno jsonArray  "+jsonArray );

                                //for sure only one duplicate value will be there so i am itreating array
                                JSONObject objectInArray = jsonArray.getJSONObject(0);
                                System.out.println("From  response of "+tagName+" rfid_validation_against_serno objectInArray  "+objectInArray );

                                String duplicate_serial_no = objectInArray.getString("name");
                                System.out.println("From  response of "+tagName+" rfid_validation_against_serno duplicate_serial_no  "+duplicate_serial_no );

                                JSONObject dup_rfid_tag_details = new JSONObject() ;

                                dup_rfid_tag_details.put("duplicate_serial_no",duplicate_serial_no);
                                dup_rfid_tag_details.put("matched_tag","pch_rfid_tag2");

                                System.out.println("From  response of "+tagName+" rfid_validation_against_serno dup_rfid_tag_details  "+dup_rfid_tag_details );

                                show_alert_dialog(tagName,dup_rfid_tag_details);
                                System.out.println("From  response of "+tagName+" called alert box" );


                            }
                            else{
                                System.out.println("From  response of "+tagName+" rfid_validation_against_serno no matching found for pch_rfid_tag2 " );
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Suresh ************ Error rfid_validation_against_serno response type " + error);

                        Toast.makeText(getApplicationContext(),"Error in getLoggedInUserData connection" , Toast.LENGTH_LONG).show();                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return MeritUHF.this.getHeaders();
            }

        };

        //Json Request
        requestQueue2.add(JsonRequest2);

    }



    //de_associate_rfid_details : {"RFID_TAG1":{"duplicate_serial_no":"MeritSystems","matched_tag":"pch_rfid_tag2"},"RFID_TAG2":{"duplicate_serial_no":"MeritSystems","matched_tag":"pch_rfid_tag2"}}eAssociate
    private void deAssociateRFID(String deas_tag_position, String deas_doc_id,String deas_doc_type ) throws JSONException {

        System.out.println("****************************Enters deAssociateRFID deas_tag_position :"+ deas_tag_position+" deas_doc_id :"+deas_doc_id+"deas_doc_type :"+deas_doc_type);
        String deas_url = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE, null, deas_doc_type,deas_doc_id);
        System.out.println("***********from deAssociateRFID  new_url"+deas_url);


        String erpnext_tag_position_field_name = "pch_rfid_tag" ;
        char position_number = deas_tag_position.charAt(8);
        String tag_position = Constants.RFID_CUSTOM_FIELD + position_number ;
        System.out.println("************* rfid_position "+tag_position );


        JSONObject rfid_data = new JSONObject();
        rfid_data.put( tag_position,"");

        RequestQueue volleyRequestQueue = Volley.newRequestQueue(this);

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, deas_url,rfid_data, future, future)
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return MeritUHF.this.getHeaders_one();
            }
        };
        volleyRequestQueue.add(request);

        try {
            //JSONObject response = future.get();
            JSONObject response = future.get(60,TimeUnit.SECONDS);
            System.out.println("*************from deAssociateRFID Came inside try after    response:"+response);
            JSONObject dataObject = response.getJSONObject("data");
            String deleted_rfidTag = dataObject.getString(tag_position);

            if(deleted_rfidTag =="null"){
                as_ds_updated_details +=  deas_tag_position + " has been disassociated from "+deas_doc_type  +" : "+ deas_doc_id + ". \n";
            }

        } catch(InterruptedException | ExecutionException ex)
        {
            //check to see if the throwable in an instance of the volley error
            System.out.println("***********from deAssociateRFID  Exception enters 1 exc");

            if(ex.getCause() instanceof VolleyError)
            {
                //grab the volley error from the throwable and cast it back
                VolleyError volleyError = (VolleyError)ex.getCause();
                //now just grab the network response like normal
                NetworkResponse networkResponse = volleyError.networkResponse;
                System.out.println("***********from deAssociateRFID  Exception networkResponse:"+networkResponse);

            }
        }
        catch(TimeoutException te)
        {
            System.out.println("****************************from deAssociateRFID  Exception TimeoutException:"+te);
        }
    }

    //get doc no details start
    //String url = "http://192.168.0.15/api/resource/Serial%20No?fields=[\"pch_rfid_tag1\",\"pch_rfid_tag2\",\"item_code\"]&filters=[[\"Serial%20No\",\"name\",\"=\",\""+ serial_no+"\"]]";//localhost url // \""+rfid_tag+ "\"

    private void get_rfid_details_ac_doc_number(String doc_type,String doc_no) {

        RequestQueue requestQueue1 = Volley.newRequestQueue(this);

        String url = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE,null, doc_type);

        if(rfid_tag_count == 1){
            url += "?fields=[\"pch_rfid_tag1\"]&filters=[[\""+doc_type +"\",\"name\",\"=\",\""+ doc_no+"\"]]" ;

        }else if (rfid_tag_count == 2){
            url += "?fields=[\"pch_rfid_tag1\",\"pch_rfid_tag2\"]&filters=[[\""+doc_type +"\",\"name\",\"=\",\""+ doc_no+"\"]]" ;
        }else if (rfid_tag_count == 3){
            url += "?fields=[\"pch_rfid_tag1\",\"pch_rfid_tag2\",\"pch_rfid_tag3\"]&filters=[[\""+doc_type +"\",\"name\",\"=\",\""+ doc_no+"\"]]" ;

        }else if (rfid_tag_count == 4){
            url += "?fields=[\"pch_rfid_tag1\",\"pch_rfid_tag2\",\"pch_rfid_tag3\",\"pch_rfid_tag4\"]&filters=[[\""+doc_type +"\",\"name\",\"=\",\""+ doc_no+"\"]]" ;
        }else if (rfid_tag_count == 5){
            url += "?fields=[\"pch_rfid_tag1\",\"pch_rfid_tag2\",\"pch_rfid_tag3\",\"pch_rfid_tag4\",\"pch_rfid_tag5\"]&filters=[[\""+doc_type +"\",\"name\",\"=\",\""+ doc_no+"\"]]" ;
        }

        System.out.println("***************Enters get_rfid_details_ac_doc_number, url :::: "+url );

        JsonObjectRequest JsonRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("***************From  get_rfid_details_ac_doc_number  response : "+response );
                        // {"data":[{"pch_rfid_tag2":null,"pch_rfid_tag1":null}]} ,, Invalid ser no = {"data":[]}
                        try{
                            JSONArray jsonArray = response.getJSONArray("data");
                            if (jsonArray.length() != 0 ){  //valid doc no
                                JSONObject objectInArray = jsonArray.getJSONObject(0);

                                //new code
                                if(rfid_tag_count == 1){

                                    String rfid_tag1 = objectInArray.getString("pch_rfid_tag1");

                                    editRfid1.setText(rfid_tag1); //for null now entering null only
                                    if ( rfid_tag1 == "null" )
                                    {
                                        System.out.println("***************From  get_rfid_details_ac_doc_number  No RFID tags have Associated Given Serial Number has : ");
                                        Toast.makeText(MeritUHF.this, "No RFID tags have Associated Given DOC ID  " ,Toast.LENGTH_SHORT).show();
                                    }

                                }else if (rfid_tag_count == 2){

                                    String rfid_tag1 = objectInArray.getString("pch_rfid_tag1");
                                    String rfid_tag2 = objectInArray.getString("pch_rfid_tag2");

                                    editRfid1.setText(rfid_tag1); //for null now entering null only
                                    editRfid2.setText(rfid_tag2);
                                    if ( rfid_tag2 == "null" &&  rfid_tag1 == "null" )
                                    {
                                        System.out.println("***************From  get_rfid_details_ac_doc_number  No RFID tags have Associated Given Serial Number has : ");
                                        Toast.makeText(MeritUHF.this, "No RFID tags have Associated Given DOC ID  " ,Toast.LENGTH_SHORT).show();
                                    }


                                }else if (rfid_tag_count == 3){

                                    String rfid_tag1 = objectInArray.getString("pch_rfid_tag1");
                                    String rfid_tag2 = objectInArray.getString("pch_rfid_tag2");
                                    String rfid_tag3 = objectInArray.getString("pch_rfid_tag3");

                                    editRfid1.setText(rfid_tag1); //for null now entering null only
                                    editRfid2.setText(rfid_tag2);
                                    editRfid3.setText(rfid_tag3);

                                    if ( rfid_tag1 == "null" &&  rfid_tag2 == "null" &&  rfid_tag3 == "null" )
                                    {
                                        System.out.println("***************From  get_rfid_details_ac_doc_number  No RFID tags have Associated Given Serial Number has : ");
                                        Toast.makeText(MeritUHF.this, "No RFID tags have Associated Given DOC ID  " ,Toast.LENGTH_SHORT).show();
                                    }

                                }else if (rfid_tag_count == 4){
                                    String rfid_tag1 = objectInArray.getString("pch_rfid_tag1");
                                    String rfid_tag2 = objectInArray.getString("pch_rfid_tag2");
                                    String rfid_tag3 = objectInArray.getString("pch_rfid_tag3");
                                    String rfid_tag4 = objectInArray.getString("pch_rfid_tag4");

                                    editRfid1.setText(rfid_tag1); //for null now entering null only
                                    editRfid2.setText(rfid_tag2);
                                    editRfid3.setText(rfid_tag3);
                                    editRfid4.setText(rfid_tag4);

                                    if ( rfid_tag1 == "null" &&  rfid_tag2 == "null" &&  rfid_tag3 == "null"&&  rfid_tag4 == "null")
                                    {
                                        System.out.println("***************From  get_rfid_details_ac_doc_number  No RFID tags have Associated Given Serial Number has : ");
                                        Toast.makeText(MeritUHF.this, "No RFID tags have Associated Given DOC ID  " ,Toast.LENGTH_SHORT).show();
                                    }
                                }else if (rfid_tag_count == 5){

                                    String rfid_tag1 = objectInArray.getString("pch_rfid_tag1");
                                    String rfid_tag2 = objectInArray.getString("pch_rfid_tag2");
                                    String rfid_tag3 = objectInArray.getString("pch_rfid_tag3");
                                    String rfid_tag4 = objectInArray.getString("pch_rfid_tag4");
                                    String rfid_tag5 = objectInArray.getString("pch_rfid_tag5");

                                    editRfid1.setText(rfid_tag1); //for null now entering null only
                                    editRfid2.setText(rfid_tag2);
                                    editRfid3.setText(rfid_tag3);
                                    editRfid4.setText(rfid_tag4);
                                    editRfid5.setText(rfid_tag5);

                                    if ( rfid_tag1 == "null" &&  rfid_tag2 == "null" &&  rfid_tag3 == "null"&&  rfid_tag4 == "null" &&  rfid_tag5 == "null")
                                    {
                                        System.out.println("***************From  get_rfid_details_ac_doc_number  No RFID tags have Associated Given Serial Number has : ");
                                        Toast.makeText(MeritUHF.this, "No RFID tags have Associated Given DOC ID  " ,Toast.LENGTH_SHORT).show();
                                    }
                                }
                                //new code
                            }
                            else{ //invalid ser no
                                Toast.makeText(MeritUHF.this, "Please enter the valid Serial No " ,Toast.LENGTH_SHORT).show();
                            }

                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("***************From  get_rfid_details_ac_doc_number  error : "+error );
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return MeritUHF.this.getHeaders();
            }
        };
        requestQueue1.add(JsonRequest);
    }



    private void update_rfidTagDetailsDoc(final String scanned_rfid_tag_data,final int  rfid_tag_index,final String doc_type, final String doc_no,final String matched_rfid_tag_details_name ) throws JSONException {
        //write synchronous fetch and update functions
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject temp_exist_rfidTagDetailsDoc = fetch_exist_rfidTagDetailsDoc( matched_rfid_tag_details_name);
                System.out.println("*******from update_rfidTagDetailsDoc temp_exist_rfidTagDetailsDoc "+temp_exist_rfidTagDetailsDoc);

                try {
                    JSONObject exist_rfidTagDetailsDoc = temp_exist_rfidTagDetailsDoc.getJSONObject("data");
                    System.out.println("*******from update_rfidTagDetailsDoc exist_rfidTagDetailsDoc "+exist_rfidTagDetailsDoc);

                    JSONArray child_doc = exist_rfidTagDetailsDoc.getJSONArray("rfid_tag_association_details");
                    System.out.println("*******from update_rfidTagDetailsDoc child_doc "+child_doc);

                    //find largest idx
                    int largest_idx=0 ;
                    for(int i = 0; i < child_doc.length(); i++){ //Array
                        JSONObject child_row = child_doc.getJSONObject(i);
                        int idx = child_row.getInt("idx");
                        if(idx > largest_idx){
                            largest_idx =idx ;
                        }
                    }
                    System.out.println("*******from update_rfidTagDetailsDoc largest_idx : "+largest_idx);

                    //update second to last last row
                    String end_association_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    JSONObject second_to_last_row = child_doc.getJSONObject(largest_idx-1);
                    second_to_last_row.put("pch_rfid_association_end_date", end_association_date);
                    child_doc.put(largest_idx-1,second_to_last_row);
                    System.out.println("*******from update_rfidTagDetailsDoc second_to_last_row"+child_doc);

                    //update last row
                    String rfid_tag_position =  "RFID Tag" + rfid_tag_index;
                    String start_association_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

                    JSONObject last_row = new JSONObject();
                    last_row.put("pch_rfid_association_start_date",start_association_date);
                    last_row.put("tag_association",rfid_tag_position);
                    last_row.put("pch_rfid_docid_associated_with",doc_no);
                    last_row.put("pch_rfid_doctype_associated_with",doc_type);
                    last_row.put("idx",largest_idx+1);

                    child_doc.put(largest_idx,last_row);
                    System.out.println("*******from update_rfidTagDetailsDoc last_row"+child_doc);

                    JSONObject  updated_child_doc = new JSONObject();
                    updated_child_doc.put("rfid_tag_association_details",child_doc);
                    System.out.println("*******from update_rfidTagDetailsDoc formed json updated_child_doc "+updated_child_doc);
                    update_rfidTagDetails_child_doc(matched_rfid_tag_details_name , updated_child_doc);
                    System.out.println("*******from update_rfidTagDetailsDoc child doc update called ");


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }// End update_rfidTagDetailsDoc

    private void update_rfidTagDetails_child_doc(final String matched_rfid_tag_details_name,JSONObject updated_child_doc ) {

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String update_rfidTagDetails_child_doc_url = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE, null, CustomUrl.RFID_TAG_HISTORY_TABLE,matched_rfid_tag_details_name);
        System.out.println(" ******** From update_create_rfidTagDetailsEntry update_rfidTagDetails_child_doc_url" +update_rfidTagDetails_child_doc_url);

        JsonObjectRequest JsonRequest = new JsonObjectRequest(Request.Method.PUT, update_rfidTagDetails_child_doc_url,updated_child_doc,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("***************From  update_rfidTagDetails_child_doc  response : "+response );
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("***************From  update_rfidTagDetails_child_doc  error : "+error );
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return MeritUHF.this.getHeaders();
            }
        };
        requestQueue.add(JsonRequest);

    }

    //using future
    private JSONObject fetch_exist_rfidTagDetailsDoc(final String matched_rfid_tag_details_name){

        JSONObject exist_rfidTagDetailsDoc = null;
        System.out.println("*******Enters fetch_exist_rfidTagDetailsDoc");
        System.out.println("***** from fetch_exist_rfidTagDetailsDoc matched_rfid_tag_details_name"+ matched_rfid_tag_details_name);

        String exist_rfidTagDetailsDoc_url = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE, null, CustomUrl.RFID_TAG_HISTORY_TABLE,matched_rfid_tag_details_name);
        System.out.println("***** from fetch_exist_rfidTagDetailsDoc exist_rfidTagDetailsDoc_url"+ exist_rfidTagDetailsDoc_url);

        RequestQueue volleyRequestQueue = Volley.newRequestQueue(this);

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, exist_rfidTagDetailsDoc_url,null, future, future)
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return MeritUHF.this.getHeaders_one();
            }
        };
        volleyRequestQueue.add(request);
        try {
            //JSONObject response = future.get();
            exist_rfidTagDetailsDoc = future.get(60,TimeUnit.SECONDS);
            System.out.println("****************************from fetch_exist_rfidTagDetailsDoc Came inside try after    exist_rfidTagDetailsDoc:"+exist_rfidTagDetailsDoc);
        } catch(InterruptedException | ExecutionException ex)
        {
            //check to see if the throwable in an instance of the volley error
            System.out.println("****************************from fetch_exist_rfidTagDetailsDoc  Exception enters 1 exc**************************************");

            if(ex.getCause() instanceof VolleyError)
            {
                //grab the volley error from the throwable and cast it back
                VolleyError volleyError = (VolleyError)ex.getCause();
                //now just grab the network response like normal
                NetworkResponse networkResponse = volleyError.networkResponse;
                System.out.println("****************************from fetch_exist_rfidTagDetailsDoc  Exception networkResponse:"+networkResponse);
            }
        }
        catch(TimeoutException te)
        {
            System.out.println("****************************from deAssocfetch_exist_rfidTagDetailsDociateRFID  Exception TimeoutException:"+te);
        }
        return  exist_rfidTagDetailsDoc ;
    }

    private void rfid_validation_against_doc(final String tagName,final String rfid_tag) {


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String rfid_validation_against_doc_url = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE, null, CustomUrl.RFID_TAG_HISTORY_TABLE);
        rfid_validation_against_doc_url += "?fields=[\"name\"]&filters=[[\"RFID%20Tag%20Details\",\"rfid_tag\",\"=\",\""+rfid_tag+ "\"]] " ;
        System.out.println("***** From set_rfidTagDetailsEntry rfid_validation_against_doc_url" +rfid_validation_against_doc_url);

        //{"data":[{"name":"RFID-Tag-00001"}]}
        JsonObjectRequest JsonRequest = new JsonObjectRequest(Request.Method.GET, rfid_validation_against_doc_url,null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("***** From response  set_rfidTagDetailsEntry   : "+response );
                        try{

                            JSONArray jsonArray = response.getJSONArray("data");

                            if (jsonArray.length() != 0 ){  //RFID tag already exist
                                JSONObject objects = jsonArray.getJSONObject(0);
                                String matched_rfid_tag_details_name = objects.getString("name");
                                System.out.println("***** From response  rfid_validation_against_doc  matched_rfid_tag_details_name : "+matched_rfid_tag_details_name );
                                fetch_rfidTagDetailsDoc_data(tagName, matched_rfid_tag_details_name) ;

                            }
                            else{ //New  RFID tag,NO duplication
                                System.out.println("***** From response  rfid_validation_against_doc  No duplicate found for tag :"+rfid_tag);
                            }

                        }catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("***************From  set_rfidTagDetailsEntry  error : "+error );
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return MeritUHF.this.getHeaders();
            }
        };
        requestQueue.add(JsonRequest);

    } // end rfidTagDetailsEntry

    //synchronous fetch_rfidTagDetailsDoc_data and update duplication  dialog box data
    private void fetch_rfidTagDetailsDoc_data(final String tagName ,final String matched_rfid_tag_details_name) {

        String exist_rfidTagDetailsDoc_url = Utility.getInstance().buildUrl(CustomUrl.API_RESOURCE, null, CustomUrl.RFID_TAG_HISTORY_TABLE,matched_rfid_tag_details_name);
        System.out.println(" ******** From fetch_rfidTagDetailsDoc_data exist_rfidTagDetailsDoc_url" +exist_rfidTagDetailsDoc_url);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest JsonRequest = new JsonObjectRequest(Request.Method.GET, exist_rfidTagDetailsDoc_url,null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("***************From  fetch_rfidTagDetailsDoc_data  response : "+response );

                        try {
                            JSONObject rfidTagDetailsDoc = response.getJSONObject("data");

                            JSONArray child_doc = rfidTagDetailsDoc.getJSONArray("rfid_tag_association_details");
                            System.out.println("*******from fetch_rfidTagDetailsDoc_data child_doc "+child_doc);

                            int largest_idx=0 ;
                            for(int i = 0; i < child_doc.length(); i++){ //Array
                                JSONObject child_row = child_doc.getJSONObject(i);
                                int idx = child_row.getInt("idx");
                                if(idx > largest_idx){
                                    largest_idx =idx ;
                                }
                            }
                            System.out.println("*******from fetch_rfidTagDetailsDoc_data largest_idx : "+largest_idx);

                            JSONObject child_doc_last_row = child_doc.getJSONObject(largest_idx-1);
                            String matched_docType = child_doc_last_row.getString("pch_rfid_doctype_associated_with");
                            String matched_doc_id = child_doc_last_row.getString("pch_rfid_docid_associated_with");
                            String rfid_position = child_doc_last_row.getString("tag_association") ;

                            JSONObject dup_rfid_tag_details = new JSONObject() ;

                            dup_rfid_tag_details.put("matched_doc_id",matched_doc_id);
                            dup_rfid_tag_details.put("rfid_position",rfid_position);
                            dup_rfid_tag_details.put("matched_docType",matched_docType);

                            show_alert_dialog(tagName,dup_rfid_tag_details);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("***************From  fetch_rfidTagDetailsDoc_data  error : "+error );
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return MeritUHF.this.getHeaders();
            }
        };
        requestQueue.add(JsonRequest);
    }//end fetch_rfidTagDetailsDoc_data

    public void  show_alert_dialog(final String tagName, final JSONObject dup_rfid_tag_details ) throws JSONException{

        System.out.print("******* Enters show_alert_dialog for tagName : " +tagName+" dup_rfid_tag_details : "+dup_rfid_tag_details);

        String dialog_message ;
        String dialog_title ;

        dialog_title = "The Selected   "+tagName +"Already Exist"; //{"duplicate_serial_no":"MeritSystems","matched_tag":"pch_rfid_tag2"}

        dialog_message = tagName + "is already bound with "+ dup_rfid_tag_details.getString("rfid_position")+" of "+  dup_rfid_tag_details.getString("matched_docType")+". ID : "+ dup_rfid_tag_details.getString("matched_doc_id") + ".Do you want to reassociate this RFID Tag with this Item";

        AlertDialog.Builder builder = new AlertDialog.Builder(MeritUHF.this);

        builder.setMessage(dialog_message).setTitle(dialog_title)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("*************************** Dialog box  yes clicked**************************************"+tagName);
                        try {
                            de_associate_rfid_details.put(tagName,dup_rfid_tag_details);
                            System.out.println("************yes Pressed  de_associate_rfid_details : "+de_associate_rfid_details);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (tagName == "RFID_TAG1"){
                            editRfid1.setText("");
                        }
                        else if (tagName == "RFID_TAG2") {
                            editRfid2.setText("");
                        }else if (tagName == "RFID_TAG3") {
                            editRfid3.setText("");
                        }else if (tagName == "RFID_TAG4") {
                            editRfid4.setText("");
                        }else if (tagName == "RFID_TAG5") {
                            editRfid5.setText("");
                        }
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();

        //stop scanning
        startFlag = false;
        if (tagName == "RFID_TAG1"){
            btnScan1.setText("Scan-1");
        } else if (tagName == "RFID_TAG2") {
            btnScan2.setText("Scan-2");
        }
        else if (tagName == "RFID_TAG3") {
            btnScan3.setText("Scan-3");
        }else if (tagName == "RFID_TAG4") {
            btnScan4.setText("Scan-4");
        }else if (tagName == "RFID_TAG5") {
            btnScan5.setText("Scan-5");
        }
        alert.show();

    }
} //whole class ends
//end