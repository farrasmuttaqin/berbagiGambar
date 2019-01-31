package com.berbagibagi.berbagigambar;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<ListItem> listItems;

    Button btnChoose,btnUpload;
    ImageView imageUpload;

    final int CODE_GALLERY_REQUEST= 999;

    Bitmap bitmap;

    private static String URL_ALL_IMAGES = "http://imagesimages.000webhostapp.com/api_all_images.php";
    private static String URL_UPLOAD = "http://imagesimages.000webhostapp.com/api_upload_images.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(MainActivity.this, "Silahkan klik salah satu gambar dibawah untuk men-download", Toast.LENGTH_LONG).show();
        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnUpload = (Button) findViewById(R.id.btnUpload);

        imageUpload = (ImageView) findViewById(R.id.imageUpload);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        CODE_GALLERY_REQUEST
                );
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Sabar dong, sedang proses nich");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(false);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPLOAD,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(MainActivity.this, "Upload Success, Thanks for uploading", Toast.LENGTH_LONG).show();
                                Intent intents = new Intent(MainActivity.this,MainActivity.class);
                                intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intents.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intents);
                                finish();
                                progressDialog.dismiss();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Pilih gambar dulu dong", Toast.LENGTH_LONG).show();
                            }
                        })
                {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params = new HashMap<>();
                        String imageData = imageToString(bitmap);
                        params.put("image",imageData);
                        return params;
                    }
                };

                RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                requestQueue.add(stringRequest);
            }
        });



        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listItems = new ArrayList<>();
        loadRecyclerViewData();
    }

    private String imageToString(Bitmap bitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();

        String encodedImage = Base64.encodeToString(imageBytes,Base64.DEFAULT);
        return encodedImage;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == CODE_GALLERY_REQUEST){
            if (grantResults.length> 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,"Select Image"),CODE_GALLERY_REQUEST);
            }else{
                Toast.makeText(getApplicationContext(),"You dont have permission",Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == CODE_GALLERY_REQUEST && resultCode == RESULT_OK && data!= null){
            Uri filePath = data.getData();
            try{
                InputStream inputStream = getContentResolver().openInputStream(filePath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                imageUpload.setImageBitmap(bitmap);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }






    private void loadRecyclerViewData(){

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Hold on, we're fetching your images");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_ALL_IMAGES,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("records");

                            JSONObject object;
                            String images;

                            int jumlah,i;
                            for (jumlah = 0; jumlah < jsonArray.length(); jumlah++) {
                                for (i = 0; i < jsonObject.length(); i++) {

                                    object = jsonArray.getJSONObject(jumlah);
                                    images = "http://imagesimages.000webhostapp.com/gambar/"+object.getString("gambar").trim();

                                    ListItem item = new ListItem(images);

                                    listItems.add(item);

                                }
                            }

                            adapter = new MyAdapter(listItems,getApplicationContext());
                            recyclerView.setAdapter(adapter);

                        }catch (JSONException e){
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "a", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "c = "+error.getMessage(), Toast.LENGTH_LONG).show();

                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
