package lu.uni.ibrahimtahirou.fitpro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MyListViewActivity extends AppCompatActivity {
    ImageView img;
    ListView list;
    private static final int MY_REQUEST_CODE = 1;
    String[] itemNames = {
            "Google Plus",
            "Twitter",
            "Windows",
            "Bing",
            "Itunes",
            "Wordpress",
            "Drupal"
    };
    Integer[] imageId = {
            R.drawable.ic_home_black_18dp,
            R.drawable.ic_account_circle_black_18dp,
            R.drawable.ic_fingerprint_black_18dp,
            R.drawable.ic_help_black_18dp,
            R.drawable.ic_settings_black_18dp,
            R.drawable.ic_power_settings_new_black_18dp,
            R.drawable.ic_directions_run_black_18dp

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list_view);

        CustomList adapter = new CustomList(MyListViewActivity.this, itemNames, imageId);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(MyListViewActivity.this, "You Clicked at " + itemNames[+position], Toast.LENGTH_SHORT).show();

                //get the image that has been clicked
                img = (ImageView) view.findViewById(R.id.img);

                //Starting the CheckBoxAtivity for  result
                Intent mIntent = new Intent(getBaseContext(), CheckboxActivity.class);
                startActivityForResult(mIntent, MY_REQUEST_CODE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MY_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                switch (result) {
                    case "CheckBox1":
                        img.setImageResource(R.drawable.ic_gps_fixed_black_18dp);
                        break;
                    case "CheckBox2":
                        img.setImageResource(R.drawable.ic_fingerprint_black_18dp);
                        break;
                }
            }

        }
    }//onActivityResult

    public class CustomList extends ArrayAdapter<String> {

        private final Activity context;
        private final String[] itemNames;
        private final Integer[] imageId;

        public CustomList(Activity context, String[] itemNames, Integer[] imageId) {
            super(context, R.layout.list_single, itemNames);
            this.context = context;
            this.itemNames = itemNames;
            this.imageId = imageId;

        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.list_single, null, true);
            TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);

            ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
            txtTitle.setText(itemNames[position]);

            imageView.setImageResource(imageId[position]);
            return rowView;
        }
    }
}
