package com.example.amfr;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener {

    private JavaCameraView javaCameraView;
    private Mat grayScaleImage;
    private int absoluteFaceSize;
    private CascadeClassifier cascadeClassifier;
    boolean initCompleted = false, faceDetection = false;
    Rect[] facesArray;
    int c = 64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        javaCameraView = (JavaCameraView) findViewById(R.id.camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.enableView();
        initializeOpenCVDependencies();
    }

    public void initializeOpenCVDependencies() {
        Thread initThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
                    FileOutputStream os = new FileOutputStream(mCascadeFile);


                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    os.close();

                    cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                    initCompleted = true;
                }
                catch (Exception e) {
                    Log.e("Exception: ", e.toString());
                }
            }
        });

        initThread.start();
    }

    public void capturePhoto(View view) {
        if(faceDetection)
            javaCameraView.disableView();
        else {
            while (!faceDetection)
                try {
                    synchronized (this) {
                        wait(50);
                    }
                } catch (InterruptedException e) {
                    Log.e("Exception: ", e.toString());
                    e.printStackTrace();
                }
        }

        for (int i=0; i < facesArray.length; i++) {

            //Normalizing faces
            Mat face = new Mat(grayScaleImage, facesArray[i]);
            Imgproc.resize(face, face, new Size(100, 100));
            Mat norm_face = new Mat(face.rows(), face.cols(), CvType.CV_8UC1);
            Core.normalize(face, norm_face, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);

            //Saving to file
            try {
                String filename = "face" + i + ".jpg";
                File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/DetectedFaces/");
                dir.mkdir();
                File image_file = new File(dir, filename);
                Log.e("Exception: ", image_file.getAbsolutePath());
                Imgcodecs.imwrite(image_file.getAbsolutePath(), norm_face);
            }
            catch (Exception e) {
                Log.e("Exception: ", e.toString());
            }
        }

        Intent intent = new Intent(this, DetectedFacesActivity.class);
        intent.putExtra("NoOfFaces", facesArray.length+"");
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (javaCameraView != null)
            javaCameraView.enableView();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        grayScaleImage = new Mat(height, width, CvType.CV_8UC1);
        absoluteFaceSize = Math.round(height * 0.2f);
    }

    public void onCameraViewStopped() {

    }

    public Mat onCameraFrame(Mat inputFrame) {

        faceDetection = false;
        while (!initCompleted) {
            return inputFrame;
        }

        try {
            Imgproc.cvtColor(inputFrame, grayScaleImage, Imgproc.COLOR_BGR2GRAY, 1);
            MatOfRect faces = new MatOfRect();
            // Use the classifier to detect faces
            if (cascadeClassifier != null) {
                cascadeClassifier.detectMultiScale(grayScaleImage, faces, 1.1, 2,
                        Objdetect.CASCADE_SCALE_IMAGE,
                        new Size(absoluteFaceSize, absoluteFaceSize), new Size());
            }

            facesArray = faces.toArray();
            int numberOfFaces = facesArray.length;

            // writing number of faces found
            Imgproc.putText(inputFrame, "Faces Detected: " + numberOfFaces,
                    new Point(inputFrame.rows() + 200, 40),
                    Core.FONT_HERSHEY_TRIPLEX,
                    1.0, new Scalar(255, 0, 0));

            // If there are any faces found, draw a rectangle around it
            for (Rect r : facesArray) {
                // drawing rectangle
                Imgproc.rectangle(inputFrame, r.tl(), r.br(), new Scalar(0, 255, 50, 0), 2);

                /*
                //database
                try {
                    String filename = c + ".jpg";
                    File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/FaceDatabase/");
                    dir.mkdir();
                    File image_file = new File(dir, filename);
                    Mat tmp = new Mat(grayScaleImage, r);
                    Imgcodecs.imwrite(image_file.getAbsolutePath(), tmp);
                    c++;
                }
                catch (Exception e) {
                    Log.e("Exception: ", e.toString());
                }

                */
            }
        }
        catch (Exception e) {
            Log.e("Exception: ", e.toString());
        }
        faceDetection = true;
        return inputFrame;
    }
}
