package com.example.p7111463.bulksms.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

/**
 * Created by P7111463 on 4/4/2018.
 */

public class SMSService extends Service  {
    String msg;
    String [] contactList ;
    String contact;
    public SMSService() {

    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * method to initialise when the service firs creates
     */
    @Override
    public void onCreate() {

    }

    /**
     * method to specify action to be taken when the service destroys
     */
    @Override
    public void onDestroy() {
    }

    /**
     * method to specify what action to take everytime when service starts or activate by service.start()
     * @param intent of the activity
     * @param startid to uniquely identify the activity
     */
    @Override
    public void onStart(Intent intent, int startid) {
        /**
         * accessing the currently dialed number passed by the activity
         */
        if (intent !=null && intent.getExtras()!=null) {
            msg = intent.getExtras().getString("msg");
            contact = intent.getExtras().getString("contact");
            sendSMS(contact, msg);
//            contactList = intent.getStringArrayExtra("contact");
//            if (contactList != null && contactList.length > 0){
//                for (int i = 0; i < contactList.length-1; i++) {
//                    sendSMS(contactList[i], msg);
//                }
//            }
        }

    }

    /**
     * this method sends the message
     * @param phoneNumber, the number on which the msg is to be sent
     * @param message, the msg to be sent
     */
    public void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        /**
         * specifies the action to taken after the service is finished, when the msg is sent
         */
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        /**
         * specifies the action to taken after the service is finished, when the msg is delivered at receiver end
         */
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        /**
         * calling the receiver when the SMS has been sent by the sender
         */
        registerReceiver(new BroadcastReceiverImpl(), new IntentFilter(SENT));
        /**
         * calling the receiver when the SMS has been delivered to the receiver
         */
        registerReceiver(new BroadcastReceiver() {

            public void onReceive(Context arg0, Intent arg1) {
                /**
                 * specifying the msg to be displayed on screen according to the SMS send process, if delivered properly or not
                 */
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
        /**
         * initialising the SMSManager to access the default method to send the msg
         */
        SmsManager sms = SmsManager.getDefault();
        /**
         * sending the 'message' to the specified 'phoneNumber'
         */
        //Short port = 16001;
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        //getApplicationContext().registerReceiver(broadcastRec,outCallFilter);
    }




    /**
     * The BroadcastReceiver to track and handle the complete process of sending the SMS
     */
    private class BroadcastReceiverImpl extends BroadcastReceiver {

        public BroadcastReceiverImpl() {
        }

        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}