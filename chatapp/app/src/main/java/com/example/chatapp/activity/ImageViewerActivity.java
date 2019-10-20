package com.example.chatapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.chatapp.R;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageView imageView;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView = findViewById(R.id.image_viewer);
        imageUrl = getIntent().getStringExtra("url");
        Picasso.get().load(imageUrl).into(imageView);
        
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                CharSequence options[] = new CharSequence[]
                        {
                                "Manage image",
                                "Cancel"
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder(imageView.getContext());
                builder.setTitle("What's next?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0){
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
                            startActivity(intent);

                        }
                    }
                });
                builder.show();
            }
        });
    }
}
