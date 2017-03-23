package com.example.amfr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.Vector;

public class DetectedFacesActivity extends AppCompatActivity {

    int no_of_faces = 0;
    String photoPath = "";
    Bitmap[] faces;
    String names[];
    GridView photoGrid;
    CustomGridAdapter adapter;
    Vector<Integer> faceVectors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detected_faces);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            no_of_faces = Integer.parseInt(bundle.getString("NoOfFaces"));
        }

        photoPath = Environment.getExternalStorageDirectory()+"/DetectedFaces/";
        faces = new Bitmap[no_of_faces];
        names = new String[no_of_faces];

        loadFaces();
        loadNames();

        photoGrid = (GridView) findViewById(R.id.detected_faces_grid);
        adapter = new CustomGridAdapter(this, names, faces);
        photoGrid.setAdapter(adapter);

        photoGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Name", Toast.LENGTH_SHORT).show();
            }
        });

        recognizeFaces();
    }

    private void loadNames() {
        for(int i=0; i<no_of_faces; i++) {
            names[i] = "Face "+(i+1);
        }
    }

    private void loadFaces() {
        for (int i=0; i<no_of_faces; i++) {
            faces[i] = BitmapFactory.decodeFile(photoPath+"face"+i+".jpg");
        }
    }

    // it recognizes faces... hah ha , its that simple ...lol

    public void recognizeFaces() {
        for(int i=0; i<no_of_faces; i++) {
            Mat img = new Mat(faces[i].getHeight(), faces[i].getWidth(), CvType.CV_8UC1);
            Utils.bitmapToMat(faces[i], img);
            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(img, img);
            Utils.matToBitmap(img, faces[i]);
        }
        adapter.notifyDataSetChanged();
        //Toast.makeText(this, "Histogram Equalized !!", Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        //deleting the pictures captured...
        File dir = new File(Environment.getExternalStorageDirectory()+"DetectedFaces");
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }
}
