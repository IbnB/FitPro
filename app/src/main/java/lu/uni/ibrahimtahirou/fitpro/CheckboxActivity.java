package lu.uni.ibrahimtahirou.fitpro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class CheckboxActivity extends AppCompatActivity {

    CheckBox mCheckBox1;
    CheckBox mCheckBox2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkbox);

        mCheckBox1 = (CheckBox) findViewById(R.id.checkBox1);
        mCheckBox2 = (CheckBox) findViewById(R.id.checkBox2);

        mCheckBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mCheckBox1.isChecked()) {
                    sendResultToLisViewActivity((String) mCheckBox1.getText()); //send the text string of mCheckBox1 to MyListViewActivity
                }
            }
        });

        mCheckBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mCheckBox2.isChecked()) {
                    sendResultToLisViewActivity((String) mCheckBox2.getText()); //send the text string of mCheckBox1 to MyListViewActivity
                }
            }
        });

    }

    public void sendResultToLisViewActivity(String mStringExtra) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", mStringExtra);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
