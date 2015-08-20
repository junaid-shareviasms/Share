package com.example.muhammadsoban.share;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    private static final int CAMERA_CODE = 99;
    private static final int FILE_SELECT_CODE = 0;
    //public static String myPath = null;


    public SeekBar seek1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // registerReceiver(new SmsReceiver(), new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        setContentView(R.layout.activity_main);
        setImage();

    }

    public void cameraClick(View view) {
        String number = ((EditText) findViewById(R.id.numbox)).getText().toString();
        if (!number.isEmpty()) {
            Intent picIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (picIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;

                photoFile = getFileforCameraIntent();

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    picIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    startActivityForResult(picIntent, CAMERA_CODE);
                }
            }


        } else {
            Toast.makeText(this, "Enter a valid number", Toast.LENGTH_LONG).show();
        }

    }

    //Need to provide the file location to the camera intent to save image in the gallery (public directory)
    private File getFileforCameraIntent() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(imageFileName,
                 /* prefix */".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            ImageManipulation.mOriginalImagePath = image.getAbsolutePath();

        } catch (Exception ee) {
            Log.d("Camera Exception", ee.toString());
        }

        return image;
    }


    public void setImage() {
//        String root = Environment.getExternalStorageDirectory().toString();
//        File myDir = new File(root + "/saved_images/myimage.jpg");
//
//        if (myDir.exists()) {
//
//            myPath = root + "/saved_images/myimage.jpg";
//            Bitmap myBitmap = BitmapFactory.decodeFile(myDir.getAbsolutePath());
//
        if(ImageManipulation.currentImage!=null) {
            ImageView myImage = (ImageView) findViewById(R.id.image);
            myImage.setImageBitmap(ImageManipulation.currentImage);
        }

    }

    public void imageClick(View view) {
        if (ImageManipulation.mCurrentImagePath != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + ImageManipulation.mCurrentImagePath), "image/*");
            startActivity(intent);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();

                   // Toast.makeText(this, "File Uri: " + uri.toString(), Toast.LENGTH_LONG).show();
                    Log.d("TAG", "File Uri: " + uri.toString());
                    // Get the path
                    String path = getFilePathFromUri(this, uri);
                   // Toast.makeText(this, "File Path: " + path, Toast.LENGTH_LONG).show();
                    Log.d("TAG", "File Path: " + path);

                    ImageManipulation.mOriginalImagePath = path;

                    ImageManipulation.setOriginalScaledImage();

                    ImageManipulation.minCompressionRatio = ImageManipulation.findMinCompressionRatioforTheRequiredSms(150);


                    ShowPopup();


                    //sendSms(((EditText) findViewById(R.id.numbox)).getText().toString(), data2);


                }
                break;
            case CAMERA_CODE:
                if (resultCode == RESULT_OK) {
                   ImageManipulation.setOriginalScaledImage();

                    ImageManipulation.minCompressionRatio = ImageManipulation.findMinCompressionRatioforTheRequiredSms(150);


                    ShowPopup();


                    //sendSms(((EditText) findViewById(R.id.numbox)).getText().toString(), data2);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    public String getFilePathFromUri(Context context, Uri uri) {
        try {
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                String[] projection = {"_data"};
                Cursor cursor = null;

                try {
                    cursor = context.getContentResolver().query(uri, projection, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow("_data");
                    if (cursor.moveToFirst()) {
                        return cursor.getString(column_index);
                    }
                } catch (Exception e) {
                    // Eat it
                }
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }

            return null;
        } catch (Exception ee) {

            Toast.makeText(this, ee.toString(), Toast.LENGTH_LONG).show();
            return null;
        }
    }


    public void attachFile(View view) {
        try {

            String number = ((EditText) findViewById(R.id.numbox)).getText().toString();
            if (!number.isEmpty()) {
                showFileChooser();
            } else {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_LONG).show();
            }

        } catch (Exception ee) {
            Toast.makeText(this, "Exception : " + ee.toString(), Toast.LENGTH_LONG).show();
        }

    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }




    public void sendSms(String number, String sms) {
        //encrypt & decrypt



        String SENT_ACTION = "SMS_SENT_ACTION";
        String DELIVERED_ACTION = "SMS_DELIVERED_ACTION";

        ArrayList<String> msgs = SmsManager.getDefault().divideMessage(sms);
        ImageManipulation.smsRequired = msgs.size();
       // Toast.makeText(this, "noOfSentMsgs : " + msgs.size(), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "noOfSentChars : " + sms.length(), Toast.LENGTH_SHORT).show();


        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT_ACTION), 0);

        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED_ACTION), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                new AlertDialog.Builder(context)
                        .setMessage("Message Delivered")
                        .setTitle("Delivery Report")
                        .setCancelable(true)
                        .show();
            }
        }, new IntentFilter(DELIVERED_ACTION));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                new AlertDialog.Builder(context)
                        .setMessage("Message Sent")
                        .setTitle("Sent Report")
                        .setCancelable(true)
                        .show();
            }
        }, new IntentFilter(SENT_ACTION));

        ArrayList<PendingIntent> sents = new ArrayList<>();
        sents.add(sentIntent);

        ArrayList<PendingIntent> delivers = new ArrayList<>();
        sents.add(deliveredIntent);

        //SmsManager.getDefault().sendMultipartTextMessage(number, null, msgs, null, null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


 /// Popup Popup Popup Popup Popup Popup Popup Popup Popup Popup Popup Popup Popup
    TextView txtv;
    ImageView imgv;
    Dialog myDialog;

    public void ShowPopup() {
        Bitmap bmp = ImageManipulation.currentImage;
        myDialog = new Dialog(this,android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth); //android.R.style.Theme_NoTitleBar
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        final View Viewlayout = inflater.inflate(R.layout.dialog_image_editor,
                (ViewGroup) findViewById(R.id.layout_dialog));

        myDialog.setContentView(Viewlayout);

        myDialog.setTitle("Edit");


        txtv = (TextView) myDialog.findViewById(R.id.tv);
        txtv.setText("Compression : 0%        SMS:" + ImageManipulation.smsRequired );

        imgv = (ImageView) myDialog.findViewById(R.id.imgv);



        if (bmp != null)
            imgv.setImageBitmap(bmp);
        else
            Toast.makeText(this, "Image not Found", Toast.LENGTH_SHORT).show();



        seek1 = (SeekBar) Viewlayout.findViewById(R.id.Seek_Compression_Ratio);
        seek1.setMax(100);
        seek1.setProgress(0);


        try {
            seek1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //Do something here with new value
                    txtv.setText("Compression : " + seekBar.getProgress() + "%        SMS:" + ImageManipulation.smsRequired);

                }

                public void onStartTrackingTouch(SeekBar arg0) {
                    // TODO Auto-generated method stub

                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub

                    float diff = (100f-ImageManipulation.minCompressionRatio)/100f;
                    ImageManipulation.currentCompressionRatio = (int)(seekBar.getProgress()*diff)+ImageManipulation.minCompressionRatio;
                    String data2 = ImageManipulation.readAndConvert(ImageManipulation.originalImage);

                    imgv.setImageBitmap(ImageManipulation.currentImage);
                    ImageManipulation.smsRequired = ImageManipulation.getNumberOfsms(data2);
                    txtv.setText("Compression : " + seekBar.getProgress() + "%        SMS:" + ImageManipulation.smsRequired);

                }
            });
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        Button sendButton = (Button) myDialog.findViewById(R.id.btnsend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data2 = ImageManipulation.readAndConvert(ImageManipulation.originalImage);
                sendSms(((EditText) findViewById(R.id.numbox)).getText().toString(), data2);
                setImage();
                ImageManipulation.originalImage.recycle();

                myDialog.cancel();

            }
        });


        myDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        myDialog.show();




    }




//    public void PreviewOriginalImage() {
//        try {
//
//            currentCompressionRatio = 0;
//            seek1.setProgress(currentCompressionRatio);
//
//            imgv.setImageBitmap(originalImage);
//
//            txtv.setText("Original Image");
//        } catch (Exception e) {
//            Log.e("Exception", e.toString());
//        }
//    }

}
