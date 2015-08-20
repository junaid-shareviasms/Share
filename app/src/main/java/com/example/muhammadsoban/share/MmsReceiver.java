package com.example.muhammadsoban.share;

/**
 * Created by Muhammad Soban on 7/25/2015.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MmsReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //---get the SMS message passed in---
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String str = "";
        if (bundle != null)
        {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                str += msgs[i].getMessageBody().toString();
            }
            int num= msgs.length;
           //Toast.makeText(context, "noOfRecievedMsgs : "+num, Toast.LENGTH_LONG).show();
            //Toast.makeText(context, "noOfReceievedChars : "+ str.length(), Toast.LENGTH_LONG).show();

            //save string to a file

            try {

                Toast.makeText(context,"in reciver",Toast.LENGTH_LONG).show();
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/saved_images");
                myDir.mkdirs();

                File file = new File (myDir, "myimage.jpg");
                if (file.exists ()) file.delete ();

                byte[] imageByteArray = ImageManipulation.decodeImage(str);

                FileOutputStream imageOutFile = new FileOutputStream(file);
                imageOutFile.write(imageByteArray);
                imageOutFile.close();

                Notification.Builder mBuilder =   new Notification.Builder(context)
                        .setSmallIcon(R.drawable.receive)
                        .setContentTitle("File Recieved!") // title for notification
                        .setContentText("New file recieved") // message for notification
                        .setAutoCancel(true); // clear notification after click

                Intent intent1=new Intent(context,MainActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent=PendingIntent.getActivity(context,0,intent1,0);
                Notification notification=mBuilder.build();
                notification.setLatestEventInfo(context,"File Recieved!","New file recieved",pendingIntent);
                notification.flags |=Notification.FLAG_AUTO_CANCEL;
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancelAll();
                mNotificationManager.notify(1212, notification);


            }catch (Exception ee)
            {
                Toast.makeText(context, ee.toString(), Toast.LENGTH_SHORT).show();
            }
            //---display the new SMS message---

        }
    }
}
