package com.codewizards.meshifyanalyzer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<SelectedDevice> {

    private ArrayList<SelectedDevice> deviceList;
    private Context context;

    CustomAdapter(Context context, int textViewResourceId,
                  ArrayList<SelectedDevice> deviceList) {
        super(context, textViewResourceId, deviceList);
        this.context = context;
        this.deviceList = new ArrayList<>();
        this.deviceList.addAll(deviceList);
    }

    public ArrayList<SelectedDevice> getDeviceList() {
        return deviceList;
    }

    private class ViewHolder {
        CheckBox name;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.device_row, null);

            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.checkBox);
            convertView.setTag(holder);

            holder.name.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    SelectedDevice device = (SelectedDevice) cb.getTag();
                    //Toast.makeText(context,"Clicked on Checkbox: " + cb.getText() + " is " + cb.isChecked(), Toast.LENGTH_LONG).show();
                    device.setSelected(cb.isChecked());
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SelectedDevice selectedDevice = deviceList.get(position);
        holder.name.setText(selectedDevice.toString());
        holder.name.setChecked(selectedDevice.isSelected());
        holder.name.setTag(selectedDevice);

        return convertView;

    }

}

