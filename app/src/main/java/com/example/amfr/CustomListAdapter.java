package com.example.amfr;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


public class CustomListAdapter extends ArrayAdapter<String> {
    private Bitmap[] faces;
    private boolean[] isPresent;

    public CustomListAdapter(@NonNull Context context, String[] names, Bitmap[] faces,
                             boolean[] isPresent) {
        super(context, R.layout.custom_row, names);
        this.faces = faces;
        this.isPresent = isPresent;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());

        View rowView = inflater.inflate(R.layout.custom_row, parent, false);

        String name = getItem(position);
        TextView face_name = (TextView) rowView.findViewById(R.id.name);
        ImageView face = (ImageView) rowView.findViewById(R.id.face_imageView);
        CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.present_check_box);

        //setting image
        face.setImageBitmap(faces[position]);

        //setting name
        face_name.setText(name);
        checkBox.setChecked(isPresent[position]);
        return rowView;
    }
}
