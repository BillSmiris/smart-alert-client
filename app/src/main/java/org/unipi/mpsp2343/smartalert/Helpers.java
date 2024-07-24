package org.unipi.mpsp2343.smartalert;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.appcompat.app.AlertDialog;

import java.io.ByteArrayOutputStream;

//This class provides various helper functions
public class Helpers {
    //Provides a simpler interface to show alert dialogs
    public static void showMessage(Context context, String title, String message){
        new AlertDialog.Builder(context).setTitle(title).setMessage(message).setCancelable(true).show();
    }

    //Provides a simpler interface to retrieve strings using another string as a key
    public static String getString(Context context, String key){
        int resId = context.getResources().getIdentifier(key, "string", context.getPackageName());
        try {
            return  context.getString(resId);
        } catch (Exception e) {
            return context.getResources().getString(R.string.error_unexpected_error);
        }
    }

    //Encoded a bitmap image to base64, using the PNG format
    public static String bitmapToBase64(Bitmap bitmap) {
        if(bitmap == null){
            return "";
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    //Decodes a base64 image to a bitmap
    public static Bitmap base64ToBitmap(String base64) {
        byte[] imageAsBytes = Base64.decode(base64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

}
