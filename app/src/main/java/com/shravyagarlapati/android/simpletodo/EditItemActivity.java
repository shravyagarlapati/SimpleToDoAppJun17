package com.shravyagarlapati.android.simpletodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditItemActivity extends AppCompatActivity {

    EditText etEditText_form2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ToDoItemDatabaseHelper databaseHelper = ToDoItemDatabaseHelper.getInstance(this);

        //Fetch an intent and pass data between activities
        final Intent get_data = getIntent();
        etEditText_form2 = (EditText) findViewById(R.id.etEditTextForm2);
        final String old_text = get_data.getExtras().getString("listItemText");
        etEditText_form2.append(old_text);
        final int list_index = get_data.getExtras().getInt("position");

        Button fab = (Button) findViewById(R.id.btnSave);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Onclick save button sends the data back to Launch activity for persistence
                get_data.putExtra("listItemText", etEditText_form2.getText().toString());
                get_data.putExtra("position", list_index);
                get_data.putExtra("old_text", old_text);
                setResult(RESULT_OK, get_data);
                finish();
            }
        });

    }
}
