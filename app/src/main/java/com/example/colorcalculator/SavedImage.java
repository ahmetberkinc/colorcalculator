package com.example.colorcalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class SavedImage extends AppCompatActivity {

    Button new_picture_button;
    private ArrayList<Color> colors = new ArrayList<>();
    public int camera_red, camera_green, camera_blue, camera_square_red, camera_square_green, camera_square_blue;
    TextView ralname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_image);

        new_picture_button = findViewById(R.id.new_picture_button);
        read_colors();

        Intent intent = getIntent();

         camera_red=intent.getIntExtra("red",0);
         camera_green=intent.getIntExtra("green",0);
         camera_blue=intent.getIntExtra("blue",0);
         camera_square_red=intent.getIntExtra("w_red",0);
         camera_square_green=intent.getIntExtra("w_green",0);
         camera_square_blue=intent.getIntExtra("w_blue",0);
         ralname=findViewById(R.id.ral_name);

        Paint paint = new Paint();
        paint.setColor(android.graphics.Color.rgb(camera_red, camera_green, camera_blue));
        Bitmap bitmap = Bitmap.createBitmap(480,800,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(50,50,200,200,paint);
        RelativeLayout layout = findViewById(R.id.rect);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            layout.setBackground(new BitmapDrawable(bitmap));
        }

        new_picture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_picture();
            }
        });
        find_color();


    }
    public void new_picture(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
    private void read_colors(){

        InputStream is = getResources().openRawResource(R.raw.name);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line= "";
        try{
            while ((line = reader.readLine()) != null) {

                //Virgüllere bölelim
                String[] tokens = line.split(",");
                //Datayı oku
                Color sample = new Color();
                sample.setRed(Integer.parseInt(tokens[0]));
                sample.setGreen(Integer.parseInt(tokens[1]));
                sample.setBlue(Integer.parseInt(tokens[2]));
                sample.setColor_red(Integer.parseInt(tokens[3]));
                sample.setColor_green(Integer.parseInt(tokens[4]));
                sample.setColor_blue(Integer.parseInt(tokens[5]));
                sample.setName(tokens[6]);
                Log.d("teyet",sample.toString());
                colors.add(sample);

            }
        }catch (IOException e){
            e.printStackTrace();

        }

    }
    public void find_color(){
        double min_distance=9999;
        double distance=0;
        int index=0;
        for(int i=0; i<colors.size(); i++){
            distance=Math.abs(colors.get(i).getBlue()-camera_blue) + Math.abs(colors.get(i).getGreen()-camera_green)+
                    Math.abs(colors.get(i).getRed()-camera_red) + (Math.abs(colors.get(i).getColor_blue()-camera_square_blue) +
                    Math.abs(colors.get(i).getColor_green()-camera_square_green) + Math.abs(colors.get(i).getColor_red()-camera_square_red))*(2);
            if(distance<min_distance){
                min_distance=distance;
                index=i;
                Log.d("index",""+index);
            }
        }
        //ralname.setText(colors.get(index).getName());
        ralname.setText("Camera Red="+String.valueOf(camera_square_red)+"Camera Green="+String.valueOf(camera_square_green)+
                "Camera Blue="+String.valueOf(camera_square_blue));

    }
}
