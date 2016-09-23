package lu.uni.ibrahimtahirou.fitpro.myfragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import lu.uni.ibrahimtahirou.fitpro.R;

/**
 * Created by ibrahimtahirou on 9/10/16.
 * credit to Ratan
 */
public class TabFragment extends Fragment {

    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int int_items = 2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

         /*Inflate tab_layout and setup Views.*/
        View x = inflater.inflate(R.layout.tab_layout, null);
        tabLayout = (TabLayout) x.findViewById(R.id.tabs);
        viewPager = (ViewPager) x.findViewById(R.id.viewpager);


         /*Set an Adapter for the View Pager*/
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));

        tabLayout.setupWithViewPager(viewPager);


        return x;

    }

    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }


        /*Return fragment with respect to Position .*/
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ActivityProfileFragment();
                case 1:
                    return new MobilityProfileFragment();

            }
            return null;
        }

        @Override
        public int getCount() {

            return int_items;

        }

        /**
         * This method returns the title of the tab according to the position.
         */
        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "Activity Profile";

                case 1:
                    return "Mobility Profile";

            }
            return null;
        }
    }

}
