package com.example.amfr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class DetectedFacesActivity extends AppCompatActivity {

    int no_of_faces = 0;
    String photoPath = "";
    Bitmap[] faces;
    Mat[] normalizedFaces;
    String names[];
    boolean isPresent[];
    ListView listView;
    CustomListAdapter adapter;

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
        isPresent = new boolean[no_of_faces];

        normalizedFaces = new Mat[no_of_faces];

        loadFaces();
        loadNames();

        listView = (ListView) findViewById(R.id.detected_faces_list);
        adapter = new CustomListAdapter(this, names, faces, isPresent);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.present_check_box);
                if(checkBox.isChecked()) {
                    isPresent[position] = false;
                    checkBox.setChecked(false);
                }
                else {
                    isPresent[position] = true;
                    checkBox.setChecked(true);
                }
                Toast.makeText(getApplicationContext(), names[position], Toast.LENGTH_SHORT).show();
            }
        });

        recognizeFaces();
    }

    private void loadNames() {
        for(int i=0; i<no_of_faces; i++) {
            names[i] = "recognizing... ";
            isPresent[i] = true;
        }
    }

    private void loadFaces() {
        for (int i=0; i<no_of_faces; i++) {
            faces[i] = BitmapFactory.decodeFile(photoPath+"face"+i+".jpg");
        }
    }

    // it recognizes faces... hah ha , its that simple ...lol

    public void recognizeFaces() {
        //normalization
        for(int i=0; i<no_of_faces; i++) {
            normalizedFaces[i] = new Mat(faces[i].getHeight(), faces[i].getWidth(), CvType.CV_8UC1);
            Utils.bitmapToMat(faces[i], normalizedFaces[i]);
            Imgproc.cvtColor(normalizedFaces[i], normalizedFaces[i], Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(normalizedFaces[i], normalizedFaces[i]);
        }

        // recognition thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FaceRecognitionClient frClient = new FaceRecognitionClient("192.168.43.15", 9000);
                frClient.connectToServer();
                if(frClient.connected) {
                    for (int i = 0; i < no_of_faces; i++) {
                        names[i] = frClient.recognize(normalizedFaces[i]);
                    }
                    handler.sendEmptyMessage(0);
                }
                else {
                    Log.e("Exception", "Not Connected!!");
                }
            }
        });
        thread.start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            adapter.notifyDataSetChanged();
        }
    };

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
