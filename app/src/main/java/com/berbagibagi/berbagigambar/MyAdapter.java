package com.berbagibagi.berbagigambar;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<ListItem> listItems;
    private Context context;

    public MyAdapter(List<ListItem> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ListItem listItem = listItems.get(position);

        Picasso.with(context)
                .load(listItem.getImageUrl())
                .into(holder.imageView);

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageLocally(holder.imageView);
            }
        });


    }

    private String saveImageLocally(ImageView iv) {
        iv.buildDrawingCache();

        Bitmap bmp = iv.getDrawingCache();

        File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        String nama_image = System.currentTimeMillis()+".jpeg";
        File file = new File(storageLoc, nama_image);

        try{
            Toast.makeText(context, "Gambar "+nama_image+" berhasil disimpan", Toast.LENGTH_LONG).show();
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            scanFile(context, Uri.fromFile(file));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "true";
    }

    private static void scanFile(Context context, Uri imageUri){
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(imageUri);
        context.sendBroadcast(scanIntent);

    }


    @Override
    public int getItemCount() {
        return listItems.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageView;
        public LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
        }
    }
}