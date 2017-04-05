package com.example.amfr;


import android.util.Log;

import org.opencv.core.Mat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FaceRecognitionClient {

    private int port;
    private String server_ip;
    private Socket socket;
    public boolean connected = false;

    public FaceRecognitionClient(String ip, int port) {
        this.port = port;
        this.server_ip = ip;
    }

    public boolean connectToServer() {
        try {
            socket = new Socket(server_ip, port);
            connected = true;
        }
        catch (Exception e) {
            Log.e("Exception", e.toString());
        }
        return connected;
    }

    private static void writeInts(OutputStream out, int[] ints) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeInt(ints.length);
        for (int e : ints) dataOut.writeInt(e);
        dataOut.flush();
    }

    public String recognize(Mat face) {
        String name = "Not Found";
        try {
            //sending image
            OutputStream oos = socket.getOutputStream();
            int r = face.rows(), c = face.cols();
            int data[] = new int[r*c];
            int k = 0;

            for (int i=0; i<r; i++) {
                for(int j=0; j<c; j++) {
                    data[k++] = (int)face.get(i, j)[0];
                }
            }

            writeInts(oos, data);

            //receiving recognized face..
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            String str = (String) ois.readObject();
            String tmp = str.substring(18).split(" ")[0];
            name = tmp.split("_")[0] + " " + tmp.split("_")[1];
        }
        catch (Exception e) {
            Log.e("Exception", e.toString());
        }
        return name;
    }
}
