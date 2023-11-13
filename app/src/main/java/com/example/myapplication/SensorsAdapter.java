package com.example.myapplication;
import android.content.Context;
import android.hardware.Sensor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

/**
 * The public class SensorsAdapter provides a reusable ArrayAdapter that can be used to
 * display lists of Sensor objects in a ListView or Spinner. It uses the ViewHolder pattern to
 * improve performance and sets the text of the itemView TextView based on the properties of the Sensor
 * object at each position in the list.
 */
public class SensorsAdapter extends ArrayAdapter<Sensor> {

    /**
     * The variable textViewResourceId is used to
     * store the resource ID of the
     */
    private int textViewResourceId;

    private static class ViewHolder {
        private TextView itemView;
    }

    public SensorsAdapter(Context context, int textViewResourceId, List<Sensor> items) {
        super(context, textViewResourceId, items);
        this.textViewResourceId = textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(textViewResourceId, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.itemView = (TextView) convertView.findViewById(R.id.content);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Sensor item = getItem(position);

        if (item != null) {
            viewHolder.itemView.setText("Name : " + item.getName()
                    + " / Int Type : " + item.getType()
                    + " / String Type : "
                    + Utils.sensorTypeToString(item.getType()) + " / Vendor : "
                    + item.getVendor() + " / Version : "
                    + item.getVersion() + " / Resolution : "
                    + item.getResolution() + " / Power : "
                    + item.getPower() + " mAh"
                    + " / Maximum Range : " + item.getMaximumRange());
        }

        return convertView;
    }
}