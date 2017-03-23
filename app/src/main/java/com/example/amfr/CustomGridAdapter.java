package com.example.amfr;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomGridAdapter extends BaseAdapter {
    private Context mContext;
    private final String[] faces;
    private final Bitmap[] Imageid;

    public CustomGridAdapter(Context c, String[] faces, Bitmap[] Imageid) {
        mContext = c;
        this.Imageid = Imageid;
        this.faces = faces;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return faces.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.grid_single, null);
            TextView textView = (TextView) grid.findViewById(R.id.name);
            ImageView imageView = (ImageView)grid.findViewById(R.id.face);
            textView.setText(faces[position]);
            imageView.setImageBitmap(Imageid[position]);
        } else {
            grid = (View) convertView;
        }

        return grid;
    }
}
