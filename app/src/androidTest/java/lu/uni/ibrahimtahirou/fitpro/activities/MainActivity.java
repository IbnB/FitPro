package lu.uni.ibrahimtahirou.fitpro.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import lu.uni.ibrahimtahirou.fitpro.R;
import lu.uni.ibrahimtahirou.fitpro.constants.Constants;
import lu.uni.ibrahimtahirou.fitpro.database.Database;
import lu.uni.ibrahimtahirou.fitpro.models.RouteModel;
import lu.uni.ibrahimtahirou.fitpro.myservices.MyMainService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,
        AdapterView.OnItemClickListener {


    private FloatingActionButton fcbAddRoute;
    private ListView lvRoutes;
    private ArrayList<RouteModel> routeModelsList = new ArrayList<>();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView tvToolBar = (TextView) toolbar.findViewById(R.id.toolbar_title);

        tvToolBar.setText("Fitness Trail Assistant");
        setSupportActionBar(toolbar);

        Intent scanIntent = new Intent(mContext, MyMainService.class);
        startService(scanIntent);


        setBasicViews();
    }


    private void setBasicViews() {
        fcbAddRoute = (FloatingActionButton) findViewById(R.id.fcbAddRoute);
        lvRoutes = (ListView) findViewById(R.id.lvRoutes);
        lvRoutes.setEmptyView(findViewById(android.R.id.empty));

        /**
         * Set click listeners
         */
        fcbAddRoute.setOnClickListener(this);
        lvRoutes.setOnItemClickListener(this);

        /**
         * Display data into list
         */
        handleRoutesList();
    }


    @Override
    protected void onResume() {
        super.onResume();
        handleRoutesList();
    }

    private void handleRoutesList() {
        routeModelsList = Database.Route.getRouteList(this);
        lvRoutes.setAdapter(new RouteAdapter(this, routeModelsList));
    }

    /**
     * Handle view clicks
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fcbAddRoute:

                Bundle bundle = new Bundle();
                Intent intent = new Intent(this, RouteActivity.class);
                bundle.putInt(Constants.CHOICE, Constants.Route.ADD);
                intent.putExtras(bundle);
                startActivity(intent);

                break;
        }
    }

    /**
     * Handle list item clicks
     *
     * @param adapterView
     * @param view
     * @param position
     * @param l
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, RouteActivity.class);
        bundle.putInt(Constants.CHOICE, Constants.Route.DETAIL);
        bundle.putString(Constants.Route.ID, routeModelsList.get(position).getRouteId());
        intent.putExtras(bundle);
        startActivity(intent);
    }


    /**
     * Routes List Adapter
     */

    private class RouteAdapter extends ArrayAdapter<RouteModel> {

        private ArrayList<RouteModel> modelList;
        private Context context;
        private LayoutInflater inflater;

        public RouteAdapter(Context context, ArrayList<RouteModel> list) {
            super(context, 0, list);
            this.context = context;
            this.modelList = list;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(final int position, View view, ViewGroup parent) {
            final RouteAdapter.ViewHolder holder;
            if (view == null) {
                holder = new RouteAdapter.ViewHolder();
                view = inflater.inflate(R.layout.lv_route_row, null);

                holder.tvRouteNum = (TextView) view.findViewById(R.id.tvRouteNum);
                holder.tvRouteName = (TextView) view.findViewById(R.id.tvRouteName);
                holder.tvRouteDistance = (TextView) view.findViewById(R.id.tvRouteDistance);
                holder.tvRouteTimeDuration = (TextView) view.findViewById(R.id.tvRouteTimeDuration);

                view.setTag(holder);
            } else {
                holder = (RouteAdapter.ViewHolder) view.getTag();
            }


            holder.tvRouteNum.setText("#" + (position + 1));
            holder.tvRouteName.setText(modelList.get(position).getRouteName());
            holder.tvRouteDistance.setText("Distance: " + modelList.get(position).getRouteDistance());
            holder.tvRouteTimeDuration.setText("Duration: " + modelList.get(position).getRouteTimeDuration());


            return view;
        }

        public class ViewHolder {
            TextView tvRouteNum, tvRouteName, tvRouteDistance, tvRouteTimeDuration;
        }

        @Override
        public int getCount() {
            return modelList.size();
        }


        @Override
        public long getItemId(int position) {
            return position;
        }


    }


}
