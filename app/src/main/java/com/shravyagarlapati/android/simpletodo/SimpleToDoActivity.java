package com.shravyagarlapati.android.simpletodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SimpleToDoActivity extends AppCompatActivity {

    ArrayList<String> todoListItems;
    ArrayAdapter<String> todoAdapter;
    ListView lvItems;
    EditText etEditText;
    ToDoItemDatabaseHelper databaseHelper ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_to_do);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get the DB instance
        databaseHelper = ToDoItemDatabaseHelper.getInstance(this);

        lvItems = (ListView) findViewById(R.id.lvItems);
        populateArrayItems();
        lvItems.setAdapter(todoAdapter);

        etEditText = (EditText)findViewById(R.id.etEditText);
        removeArrayItems();

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Take me to the edit screen
                Intent i = new Intent(SimpleToDoActivity.this, EditItemActivity.class);
                i.putExtra("listItemText", todoListItems.get(position));
                i.putExtra("position",position);
                //startActivity(i);
                startActivityForResult(i,20);
            }
        });
    }

    public void populateArrayItems()
    {
        //Read the list of items from file and populate in the app
        //readItems();

        //ToDoItemDatabaseHelper databaseHelper = ToDoItemDatabaseHelper.getInstance(this);
        List<ToDo> tempToDo = databaseHelper.getAllToDoItems();
        todoListItems = new ArrayList<String>();
        for (ToDo each_item : tempToDo)
        {
            System.out.println("Populated Data:"+each_item.itemValue);
            todoListItems.add(each_item.itemValue);
        }

        todoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, todoListItems);
    }


    public void removeArrayItems()
    {
        //Remove the item from the list view on Long Click
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                String item_to_be_deleted = todoListItems.get(position);
                databaseHelper.deleteToDoItem(item_to_be_deleted);

                todoListItems.remove(position);
                todoAdapter.notifyDataSetChanged();

                //writeItems();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_simple_to_do, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAddItem(View view) {
        //Add the items to listView
        String text_val = etEditText.getText().toString();
        if(text_val.length()>0)
            todoAdapter.add(text_val);
        etEditText.setText("");

        ToDo todo = new ToDo();
        todo.itemValue = text_val;
        databaseHelper.addOrUpdateToDoItem(todo);

        //writeItems();
    }

    /*
    private void readItems() {
        //Read list of items from the file
        File fileDir = getFilesDir();
        File todoFile = new File(fileDir,"todo.txt");
        try {
            todoListItems = new ArrayList<String>(FileUtils.readLines(todoFile));
        }
        catch (IOException e) {
            todoListItems = new ArrayList<String>();
            e.printStackTrace();
        }
    }

    private void writeItems() {
        //Write to the file
        File fileDir = getFilesDir();
        File todoFile = new File(fileDir,"todo.txt");
        try {
            FileUtils.writeLines(todoFile, todoListItems);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Edit the listview and persist them in file
        if (resultCode == RESULT_OK && requestCode == 20) {
            String name = data.getExtras().getString("listItemText");
            int index = data.getExtras().getInt("position");
            String old_text = data.getExtras().getString("old_text");

            todoListItems.set(index, name);
            todoAdapter.notifyDataSetChanged();

            ToDo toDo = new ToDo();
            toDo.itemValue = todoListItems.get(index);
            databaseHelper.addOrUpdateToDoItem(toDo);
            databaseHelper.deleteToDoItem(old_text);
        }
    }
}
