package lu.uni.ibrahimtahirou.fitpro;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

public class ListViewActivity extends ListActivity {

    String[] city = {
            "Niamey",
            "Lome",
            "Accra",
            "Cotonou",
            "Abidjan",
            "Luxembourg",
            "Dakar",
            "Geneve",
            "Bern",
            "Bamako",
            "Lagos",
            "Douala"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        // -- Display mode of the ListView

        ListView listview = getListView();
        //	listview.setChoiceMode(listview.CHOICE_MODE_NONE);
        listview.setChoiceMode(listview.CHOICE_MODE_SINGLE);
        //listview.setChoiceMode(listview.CHOICE_MODE_MULTIPLE);

        //--	text filtering
        listview.setTextFilterEnabled(true);

        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_checked, city));
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        CheckedTextView item = (CheckedTextView) v;
        Toast.makeText(this, city[position] + " checked : " +
                item.isChecked(), Toast.LENGTH_SHORT).show();
    }
}