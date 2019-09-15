package com.example.rentbd.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.rentbd.Model.InfoWindowData;
import com.example.rentbd.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {


    private Context context;

    public CustomInfoWindow(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.custom_info_window_layout, null);

        TextView title_tv = view.findViewById(R.id.info_title);
        TextView type_tv = view.findViewById(R.id.info_type);

        TextView rent_tv = view.findViewById(R.id.info_rent);
        TextView phn_tv = view.findViewById(R.id.info_phn);
        TextView address_tv = view.findViewById(R.id.info_address);
        TextView avilable_date_tv = view.findViewById(R.id.info_available_date);

        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();
        if(infoWindowData!=null){
            title_tv.setText(infoWindowData.getTitle());
            type_tv.setText(infoWindowData.getType());
            rent_tv.setText(infoWindowData.getRent());
            phn_tv.setText(infoWindowData.getPhn());
            address_tv.setText(infoWindowData.getAddress());
            avilable_date_tv.setText(infoWindowData.getAvailableDate());
        }

        return view;
    }
}
