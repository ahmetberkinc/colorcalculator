package com.example.colorcalculator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;
import java.util.Calendar;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    //view holder
    CameraBridgeViewBase cameraBridgeViewBase;
    //camera listener callback
    BaseLoaderCallback baseLoaderCallback;
    //image holder
    Mat img, grey;
    Mat bwIMG, hsvIMG, lrrIMG, urrIMG, dsIMG, usIMG, cIMG, hovIMG;
    MatOfPoint2f approxCurve;

    int threshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize treshold
        threshold = 100;

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.cameraViewer);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        //create camera listener callback
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        Log.v("ahmet", "Loader interface success");
                        bwIMG = new Mat();
                        dsIMG = new Mat();
                        hsvIMG = new Mat();
                        lrrIMG = new Mat();
                        urrIMG = new Mat();
                        usIMG = new Mat();
                        cIMG = new Mat();
                        hovIMG = new Mat();
                        approxCurve = new MatOfPoint2f();
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    int count = 0;

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        //get rgba image
        img = inputFrame.rgba();
        grey = inputFrame.gray();

        Mat dst = img.t();
        Mat gray = grey.t();
        Core.flip(img.t(),dst,1);
        Core.flip(grey.t(),gray,1);
        Imgproc.resize(dst,dst,img.size());
        Imgproc.resize(gray,gray,grey.size());

        //to get grayscale image using below line
        //img = inputFrame.gray();

        Imgproc.pyrDown(gray, dsIMG, new Size(gray.cols() / 2, gray.rows() / 2));
        Imgproc.pyrUp(dsIMG, usIMG, gray.size());

        //Imgproc.Canny(usIMG, bwIMG, 0, threshold);
        Imgproc.threshold(gray,gray,127,255,Imgproc.THRESH_BINARY);
        //Imgproc.dilate(bwIMG, bwIMG, new Mat(), new Point(-1, 1), 1);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        cIMG = bwIMG.clone();
        final Mat hierarchy = new Mat();
        Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        int index=0;
        ArrayList<MatOfPoint> mpoints = new ArrayList<>();
        ArrayList<MatOfPoint> real_shapes = new ArrayList<>();
        for (MatOfPoint cnt : contours) {

            MatOfPoint2f curve = new MatOfPoint2f(cnt.toArray());


            Imgproc.approxPolyDP(curve, approxCurve, 0.01 * Imgproc.arcLength(curve, true), true);

            int numberVertices = (int) approxCurve.total();
            double contourArea = Imgproc.contourArea(cnt);

            //Rectangle detected
            if (numberVertices == 4 && Math.abs(contourArea)>10000) {
                Rect r = Imgproc.boundingRect(cnt);
                double width = r.width;
                double height = r.height;
                double aspectratio = width/height;
                Log.d("test",""+aspectratio);
                if (aspectratio>=0.95 && aspectratio<=1.05 || (aspectratio>=1.75 && aspectratio<=1.85)){
                    mpoints.add(cnt);
                    Log.d("test2",""+aspectratio);
                    //Imgproc.drawContours(dst,contours,index, new Scalar(255,0,0),10);
                }
            }
            index++;
        }
        //saveimage(dst);
        if(mpoints.size()==9 ){
            int n = mpoints.size();
            for(int i=0; i<n-1; i++){
                for(int j=0; j<n-i-1; j++){
                    if(Imgproc.contourArea(mpoints.get(j))<Imgproc.contourArea(mpoints.get(j+1))){
                        Collections.swap(mpoints,j,j+1);
                    }
                }
            }
            for(int i=0; i<6; i++){
                if(i!=1) {
                    real_shapes.add(mpoints.get(i));
                    //Imgproc.drawContours(dst,real_shapes,i, new Scalar(0,0,0),1);
                }
            }
            setLabel(dst,"O",real_shapes);
        }

        return dst;

    }
    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "There is a problem", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    private int setLabel(Mat im, String label, ArrayList<MatOfPoint> contour) {
        int yesil = 0;
        int fontface = Core.FONT_HERSHEY_SIMPLEX;
        Mat original = im.clone();
        double scale = 3;//0.4;
        int thickness = 3;//1;
        int[] baseline = new int[1];
        Size text = Imgproc.getTextSize(label, fontface, scale, thickness, baseline);
        ArrayList<Rect> rects = new ArrayList<>();
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Double> total_colors = new ArrayList<>();
        double[][] colors = new double[5][3];
        int w_red = 0, w_green = 0, w_blue = 0;
        for (int i = 0; i < contour.size(); i++) {
            rects.add(Imgproc.boundingRect(contour.get(i)));
            points.add(new Point(rects.get(i).x + ((rects.get(i).width - text.width) / 2), rects.get(i).y + ((rects.get(i).height + text.height) / 2)));
        }
        int sayac2 = 0, start_i, start_j, stop_i, stop_j=0;
        int big_red=0, big_green=0, big_blue=0;
        Point bilgi = new Point(400, 200);
        Point avg = new Point(100, 100);
        for (int i = 0; i < rects.size(); i++) {
            start_i = rects.get(i).x + (rects.get(i).width / 5);
            start_j = rects.get(i).y + (rects.get(i).height / 5);
            stop_i = rects.get(i).width + start_i - (rects.get(i).width / 2);
            stop_j = rects.get(i).height + start_j - (rects.get(i).height / 2);
            Imgproc.putText(im, ".", new Point(start_i,start_j), 1, scale, new Scalar(255, 0, 0), thickness);
            Imgproc.putText(im, ".", new Point(start_i,stop_j), 1, scale, new Scalar(255, 0, 0), thickness);
            Imgproc.putText(im, ".", new Point(stop_i,start_j), 1, scale, new Scalar(255, 0, 0), thickness);
            Imgproc.putText(im, ".", new Point(stop_i,stop_j), 1, scale, new Scalar(255, 0, 0), thickness);
            double total_red = 0, total_green = 0, total_blue = 0, total_color = 0;
            int count = 0;
            for (int j = start_i; j < stop_i; j = j + 2) {
                for (int k = start_j; k < stop_j; k = k + 2) {
                    count++;
                    total_red += im.get(j, k)[0];
                    total_green += im.get(j, k)[1];
                    total_blue += im.get(j, k)[2];
                    //brightness += im.get(j, k)[0] * 0.299 + im.get(j, k)[1] * 0.587 + im.get(j, k)[2] * 0.114;
                }
            }
            //SADECE 2 BASAMAK GÖSTMEK İÇİN TRİCK iyileştir*****************************
            //brightness = brightness / count;
            total_color = total_blue + total_green + total_red;
            colors[sayac2][0] = total_red / count;
            colors[sayac2][1] = total_green / count;
            colors[sayac2][2] = total_blue / count;
            sayac2++;
            if (rects.get(i).area() < 70000) {
                total_color = total_color / (count * 3);
                w_red += colors[sayac2 - 1][0];
                w_green += colors[sayac2 - 1][1];
                w_blue += colors[sayac2 - 1][2];
                total_colors.add(total_color);
            }else{
                big_red = (int)colors[sayac2-1][0];
                big_green = (int)colors[sayac2-1][1];
                big_blue = (int)colors[sayac2-1][2];
            }

            //Imgproc.putText(im, new DecimalFormat("##.##").format(total_color), points.get(i), 1, scale, new Scalar(255, 0, 0), thickness);
            //Imgproc.putText(im, String.valueOf(rects.get(i).width)+" "+rects.get(i).height, points.get(i), 1, scale, new Scalar(255, 0, 0), thickness);
            //Imgproc.putText(im, new DecimalFormat("##.##").format(Imgproc.contourArea(contour.get(i))), points.get(i), 1, scale, new Scalar(255, 0, 0), thickness);
            //Imgproc.putText(im, String.valueOf(count), points.get(i), 1, scale, new Scalar(255, 0, 0), thickness);
            Imgproc.putText(im, new DecimalFormat("##").format(colors[sayac2 - 1][0]) + "," + new DecimalFormat("##").format(colors[sayac2 - 1][1]) + "," + new DecimalFormat("##").format(colors[sayac2 - 1][2]), points.get(i), 1, 2, new Scalar(255, 0, 0), 1);
        }
        double sum = 0;
        for (double color : total_colors) {
            sum += color;
        }
        Imgproc.putText(im, new DecimalFormat("##").format(w_red / 4) + "," + new DecimalFormat("##").format(w_green / 4) + "," + new DecimalFormat("##").format(w_blue / 4), avg, 1, scale, new Scalar(255, 0, 0), 1);
        count = 0;
        int sayac = 0;
        double average = sum / total_colors.size();
        for (int i = 0; i < rects.size(); i++) {
            if (rects.get(i).area() < 70000) {
                if ((total_colors.get(sayac) > average - 15) && (total_colors.get(sayac) < average + 15)) {
                    Imgproc.putText(im, "O", points.get(i), fontface, scale, new Scalar(0, 255, 0), thickness);
                    count++;
                } else {
                    Imgproc.putText(im, "O", points.get(i), fontface, scale, new Scalar(255, 0, 0), thickness);
                }
                sayac++;
            }
        }

        if (count == 4) {
            Imgproc.putText(im, "OK", bilgi, fontface, scale, new Scalar(0, 255, 0), thickness);
            int flag = 0;
            for (int i = 0; i < rects.size(); i++) {
                if (rects.get(i).area() > 70000) {
                    flag = 1;
                }
            }
            if (flag == 1) {
                saveimage(im,original);
                Intent intent = new Intent(this,SavedImage.class);
                intent.putExtra("red",big_red);
                intent.putExtra("green",big_green);
                intent.putExtra("blue",big_blue);
                intent.putExtra("w_red",w_red/4);
                intent.putExtra("w_green",w_green/4);
                intent.putExtra("w_blue",w_blue/4);
                startActivity(intent);
            }

        } else {
            Imgproc.putText(im, "Shadow!", bilgi, fontface, scale, new Scalar(255, 0, 0), thickness);
        }
        return yesil;
    }
    private void saveimage(Mat img, Mat original){
        Bitmap bmp = null;
        Bitmap bmp2 = null;
        Mat tmp = img.clone();
        try {
            bmp = Bitmap.createBitmap(tmp.width(), tmp.height(), Bitmap.Config.RGB_565);
            bmp2 = Bitmap.createBitmap(img.width(),img.height(),Bitmap.Config.RGB_565);
            Utils.matToBitmap(tmp, bmp);
            Utils.matToBitmap(original, bmp2);
        } catch (CvException e) {
            Log.d("TAG", e.getMessage());
        }

        tmp.release();
        original.release();



        FileOutputStream out = null;
        FileOutputStream out2 = null;

        String filename = Calendar.getInstance().getTime()+".png";
        String filename2 = Calendar.getInstance().getTime()+".png";


        File sd = new File(Environment.getExternalStorageDirectory() + "/frames");
        File sd2 = new File(Environment.getExternalStorageDirectory() + "/real_frames");
        boolean success = true;
        if (!sd.exists()) {
            success = sd.mkdir();
        }
        if (!sd2.exists()) {
            success = sd2.mkdir();
        }
        if (success) {
            File dest = new File(sd, filename);
            File dest2 = new File(sd2,filename2);

            try {
                out = new FileOutputStream(dest);
                out2 = new FileOutputStream(dest2);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                bmp2.compress(Bitmap.CompressFormat.PNG, 100, out2);// bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("TAG", e.getMessage());
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        Log.d("TAG", "test3!!");
                    }
                } catch (IOException e) {
                    Log.d("TAG", e.getMessage() + "test4");
                    e.printStackTrace();
                }
            }
        }
    }
}