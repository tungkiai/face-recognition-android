package com.example.facerecognition_android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Result;

public class MainActivity extends AppCompatActivity {
    Bitmap bmp;
    private ImageView mImageView;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_IMAGE_UPLOAD = 102;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imageView);
    }

    public void takePicture(View view) {
        Intent imageTakeContent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (imageTakeContent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(imageTakeContent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void uploadPicture(View view) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), REQUEST_IMAGE_UPLOAD);
    }

    public void predict(View view) {
        // Log.d("Height: " , String.valueOf(bmp.getHeight()));
        ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bAOS);
        byte[] byteArray = bAOS.toByteArray();
        String encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        // Log.d("Encoded: " , encodedString);
        HashMap postDataParams = new HashMap();
        // List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        int timeout=5000;
        URL url;
        HttpURLConnection connection = null;
        try {
            url = new URL("http://10.0.2.2:5000/predict");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/json");

            // connection.setRequestProperty("Content-Length",
            //        "" + Integer.toString(urlParameters.getBytes().length));
            // connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
        } catch (SocketTimeoutException ex) {
            ex.printStackTrace();
        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException ex) {

            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            bmp = imageBitmap;
            mImageView.setImageBitmap(imageBitmap);
        } else if (requestCode == REQUEST_IMAGE_UPLOAD && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                bmp = bitmap;
                mImageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
