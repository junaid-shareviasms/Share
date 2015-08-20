package com.example.muhammadsoban.share;

/**
 * Created by Muhammad Soban on 7/25/2015.
 */


        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.os.Environment;
        import android.telephony.SmsManager;
        import android.util.Log;
        import android.widget.Toast;

        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.OutputStream;

        import org.apache.commons.codec.binary.Base64;


public class ImageManipulation {

    public static int currentCompressionRatio = 0;
    public static int minCompressionRatio = 0;

    public static Bitmap originalImage;
    public static Bitmap currentImage;

    public static String mOriginalImagePath;
    public static String mCurrentImagePath;

    public static int smsRequired = 0;

    public static void setOriginalScaledImage() {
        Bitmap bmp = BitmapFactory.decodeFile(mOriginalImagePath);
        mCurrentImagePath = mOriginalImagePath;

        if (bmp.getHeight() > bmp.getWidth()) {
            originalImage = Bitmap.createScaledBitmap(bmp, (int) ((double) bmp.getWidth() / bmp.getHeight() * 500), 500, true);

        } else {
            originalImage = Bitmap.createScaledBitmap(bmp, 500, (int) ((double) bmp.getHeight() / bmp.getWidth() * 500), true);

        }

        bmp.recycle();
        currentImage = originalImage;
    }


    //It provides the approximate compression ratio value nearest to the smsLimit
    public static int findMinCompressionRatioforTheRequiredSms(int smsLimit)
    {

        int min = 0, max = 100 , actual = ((max-min)/2) + min;
        int sms = getNoOfSmsForImagebyRatio(actual);
        for(int i = 0; i<4;i++)
        {
            if(sms >= smsLimit-10 && sms <= smsLimit+10) {
                currentCompressionRatio = actual;
                smsRequired = sms;
                return actual;
            }
            else
            {

                if(sms<smsLimit)
                {
                    max = actual;
                    actual = ((actual-min)/2)+min;
                    sms = getNoOfSmsForImagebyRatio(actual);
                }
                else
                {
                    min = actual;
                    actual = ((max-actual)/2) + actual;
                    sms = getNoOfSmsForImagebyRatio(actual);
                }
            }

        }
        currentCompressionRatio = actual;
        smsRequired = sms;
        return  actual;
    }

    public static String readAndConvert(Bitmap bmap) {
        try {

            File yourfile2 = ConvertAndCompressed(bmap);

            // File dir = Environment.getExternalStorageDirectory();

            String data = ImageManipulation.getImageString(yourfile2);


            return data;
        } catch (Exception ee) {
            Log.d("Exception",ee.toString());

            return null;
        }
    }



    public static File ConvertAndCompressed(Bitmap bmap) throws Exception {
        Bitmap resized = bmap;



        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();

        File dest = new File(myDir, "Sent.jpg");

        OutputStream out = new FileOutputStream(dest);

        resized.compress(Bitmap.CompressFormat.WEBP, 100 - currentCompressionRatio, out);

        mCurrentImagePath = dest.getAbsolutePath();
        currentImage = BitmapFactory.decodeFile(mCurrentImagePath);

        out.flush();
        out.close();

        return dest;
    }

    //Provides the sms needed for the image to be sent for the given compression Ratio
    public static int getNoOfSmsForImagebyRatio (int compRatio )
    {
        currentCompressionRatio = compRatio;

        String dat = readAndConvert(originalImage);
        int sms = getNumberOfsms(dat);

        return sms;

    }

    public static int getNumberOfsms(String imageString)
    {

        int sms = SmsManager.getDefault().divideMessage(imageString).size();
        return sms;
    }



    public static String getImageString(File file) {
        try {
			/*
			 * Reading a Image file from file system
			 */
            FileInputStream imageInFile = new FileInputStream(file);
            byte imageData[] = new byte[(int)file.length()];
            imageInFile.read(imageData);

            String string = encodeImage(imageData);

            return  string;

        } catch (FileNotFoundException e) {
            System.out.println("Image not found" + e);
        } catch (IOException e) {
            System.out.println("Exception while reading the Image " + e);
        }

        return null;
    }


    /**
     * Encodes the byte array into base64 string
     * @param imageByteArray - byte array
     * @return String a {@link java.lang.String}
     */
    public static String encodeImage(byte[] imageByteArray){
        return new String(Base64.encodeBase64(imageByteArray));
        //.encodeBase64URLSafeString(imageByteArray);
    }

    /**
     * Decodes the base64 string into byte array
     * @param imageDataString - a {@link java.lang.String}
     * @return byte array
     */
    public static byte[] decodeImage(String imageDataString) {
        return Base64.decodeBase64(imageDataString.getBytes());
    }

}


