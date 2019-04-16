package com.mapstutorial.simplerouting;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.search.GeocodeResult;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;

import static com.mapstutorial.simplerouting.RoutingActivity.geoCoordinatebatdau;
import static com.mapstutorial.simplerouting.RoutingActivity.map;


public class Adapter extends RecyclerView.Adapter<Adapter.ViewHoder> implements Filterable {
    static MapMarker mapMarker3;
    private ArrayList<GeocodeResult> geocodeResults;
    private ArrayList<GeocodeResult> geocodeResultsfull;

    public Adapter(MutableLiveData<ArrayList<GeocodeResult>> listsearchresults) {
        this.geocodeResults = listsearchresults.getValue();
        assert geocodeResults != null;
        geocodeResultsfull = new ArrayList<>(geocodeResults);
    }

    @NonNull
    @Override
    public ViewHoder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_row_layout, viewGroup, false);
        ViewHoder viewHoder = new ViewHoder(view);
        return viewHoder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHoder viewHoder, final int i) {
        viewHoder.tvad.setText(geocodeResults.get(i).getLocation().getAddress().getStreet());
        viewHoder.tvaddigi.setText(geocodeResults.get(i).getLocation().getCoordinate().toString());
        viewHoder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.setCenter(geocodeResults.get(i).getLocation().getCoordinate(), Map.Animation.NONE);
                mapMarker3 = new MapMarker(geocodeResults.get(i).getLocation().getCoordinate());
                map.addMapObject(mapMarker3);
                geoCoordinatebatdau = geocodeResults.get(i).getLocation().getCoordinate();

                Log.d("AAA", "" + i);
            }
        });


    }


    @Override
    public int getItemCount() {
        return geocodeResults.size();
    }

    @Override
    public Filter getFilter() {

        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<GeocodeResult> filterlist = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filterlist.addAll(geocodeResultsfull);
            } else {
                String filterpatern = constraint.toString().toLowerCase().trim();
                for (GeocodeResult item : geocodeResultsfull) {
                    if (item.getLocation().getAddress().getStreet().toLowerCase().contains(filterpatern)) {
                        filterlist.add(item);


                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filterlist;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            geocodeResults.clear();
            geocodeResults.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };


    public class ViewHoder extends RecyclerView.ViewHolder {
        TextView tvad;
        TextView tvaddigi;

        public ViewHoder(@NonNull View itemView) {
            super(itemView);

            tvad = itemView.findViewById(R.id.textViewad);
            tvaddigi = itemView.findViewById(R.id.textViewaddigi);
        }
    }
}