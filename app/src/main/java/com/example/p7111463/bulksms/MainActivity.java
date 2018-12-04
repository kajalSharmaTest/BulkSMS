package com.example.p7111463.bulksms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.p7111463.bulksms.service.SMSService;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText msgText ;
    Button btnSendSMS, btnSaveContacts;
    private  String TAG = MainActivity.this.getClass().getSimpleName();
    String filePath ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        msgText = (EditText)findViewById(R.id.msg);
        btnSaveContacts = (Button)findViewById(R.id.btn_save_contact);
        btnSaveContacts.setOnClickListener(this);
        btnSendSMS = (Button)findViewById(R.id.btn_send_msg);
        btnSendSMS.setOnClickListener(this);
        if (shouldAskPermissions()) {
            askPermissions();
        }
       File file =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        filePath = file + "/contacts.xls";
    }

    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_send_msg:
                readExcelFile(this,filePath);
                break;
            case R.id.btn_save_contact:
                readExcelFileToSaveContacts(this,filePath);
                break;
        }
    }

    private  void readExcelFile(Context context, String filename) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
        {
            Log.e(TAG, "Storage not available or read only");
            return;
        }

        try{
            // Creating Input Stream
           // File file = new File(context.getExternalFilesDir(null), filename);
            File file = new File( filePath );
            FileInputStream myInput = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            /** We now need something to iterate through the cells.**/
            Iterator rowIter = mySheet.rowIterator();

            while(rowIter.hasNext()){
                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator cellIter = myRow.cellIterator();
                while(cellIter.hasNext()){
                    HSSFCell myCell = (HSSFCell) cellIter.next();
                   String cellValue =  myCell.toString().replace(".", "");
                   String contact = cellValue.toString().replace("E9", "");
                    if(isNumber(contact)) {
                        Log.d(TAG, "Cell Value: " + contact);
                        Toast.makeText(context, "cell Value: " + contact, Toast.LENGTH_SHORT).show();
                        Intent serviceIntent = new Intent(MainActivity.this, SMSService.class);
                        serviceIntent.putExtra("msg", msgText.getText().toString());
                        serviceIntent.putExtra("contact", contact);
                        context.startService(serviceIntent);
                    }
                }
            }
        }catch (Exception e){e.printStackTrace(); }

        return;
    }

    private  void readExcelFileToSaveContacts(Context context, String filename) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
        {
            Log.e(TAG, "Storage not available or read only");
            return;
        }

        try{
            // Creating Input Stream
            // File file = new File(context.getExternalFilesDir(null), filename);
            File file = new File( filePath );
            FileInputStream myInput = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            /** We now need something to iterate through the cells.**/
            Iterator rowIter = mySheet.rowIterator();

            while(rowIter.hasNext()){
                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator cellIter = myRow.cellIterator();
                String contactNumber ="";
                String Name="";
                while(cellIter.hasNext()){
                    HSSFCell myCell = (HSSFCell) cellIter.next();
                    String cellValue =  myCell.toString().replace(".", "");
                     String value  = cellValue.toString().replace("E9", "");
                     if(isNumber(value)){
                         contactNumber = value;
                     } else {
                         Name = value;
                     }
                     if(!Name.isEmpty() && !contactNumber.isEmpty()) {
                         Log.d(TAG, "Name:: " + Name);
                         Log.d(TAG, "Number :: " + contactNumber);
                         saveContacts(Name,contactNumber);
                     }
                }

            }
        }catch (Exception e){e.printStackTrace(); }

        return;
    }

    private void saveContacts(String name, String number){
        // Below uri can avoid java.lang.UnsupportedOperationException: URI: content://com.android.contacts/data/phones error.
        Uri addContactsUri = ContactsContract.Data.CONTENT_URI;

        // Add an empty contact and get the generated id.
        long rowContactId = getRawContactId();

        // Add contact name data.
        String displayName = name;
        insertContactDisplayName(addContactsUri, rowContactId, displayName);

        // Add contact phone data.
        String phoneNumber = number;
        String phoneTypeStr = "Work";
        insertContactPhoneNumber(addContactsUri, rowContactId, phoneNumber, phoneTypeStr);

        Toast.makeText(getApplicationContext(),"New contact has been added, check it in contacts list." , Toast.LENGTH_LONG).show();
    }

    // This method will only insert an empty data to RawContacts.CONTENT_URI
    // The purpose is to get a system generated raw contact id.
    private long getRawContactId()
    {
        // Inser an empty contact.
        ContentValues contentValues = new ContentValues();
        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
        // Get the newly created contact raw id.
        long ret = ContentUris.parseId(rawContactUri);
        return ret;
    }


    // Insert newly created contact display name.
    private void insertContactDisplayName(Uri addContactsUri, long rawContactId, String displayName)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

        // Each contact must has an mime type to avoid java.lang.IllegalArgumentException: mimetype is required error.
        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);

        // Put contact display name value.
        contentValues.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, displayName);

        getContentResolver().insert(addContactsUri, contentValues);

    }

    private void insertContactPhoneNumber(Uri addContactsUri, long rawContactId, String phoneNumber, String phoneTypeStr)
    {
        // Create a ContentValues object.
        ContentValues contentValues = new ContentValues();

        // Each contact must has an id to avoid java.lang.IllegalArgumentException: raw_contact_id is required error.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

        // Each contact must has an mime type to avoid java.lang.IllegalArgumentException: mimetype is required error.
        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

        // Put phone number value.
        contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);

        // Calculate phone type by user selection.
        int phoneContactType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;

        if("home".equalsIgnoreCase(phoneTypeStr))
        {
            phoneContactType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
        }else if("mobile".equalsIgnoreCase(phoneTypeStr))
        {
            phoneContactType = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
        }else if("work".equalsIgnoreCase(phoneTypeStr))
        {
            phoneContactType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
        }
        // Put phone type value.
        contentValues.put(ContactsContract.CommonDataKinds.Phone.TYPE, phoneContactType);

        // Insert new contact data into phone contact list.
        getContentResolver().insert(addContactsUri, contentValues);

    }

    public static boolean isNumber(String string) {
        return string.matches("^\\d+$");
    }


    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.SEND_SMS",
                "android.permission.READ_CONTACTS",
                "android.permission.WRITE_CONTACTS"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }


}
