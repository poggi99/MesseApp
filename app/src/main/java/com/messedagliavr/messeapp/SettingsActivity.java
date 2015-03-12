package com.messedagliavr.messeapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.messedagliavr.messeapp.Databases.Database;

/**
 * Created by Simone on 10/03/2015.
 */
public class SettingsActivity extends ActionBarActivity {
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.settings));
        EditText user = (EditText) findViewById(R.id.username);
        EditText password = (EditText) findViewById(R.id.password);
        CheckBox check = (CheckBox) findViewById(R.id.checkBox1);
        Button save = (Button) findViewById(R.id.savesett);
        ToggleButton toggle = (ToggleButton) findViewById(R.id.saveenabled);
        Database databaseHelpersettings = new Database(this);
        SQLiteDatabase dbsettings = databaseHelpersettings.getWritableDatabase();
        String[] columnssettings = {"enabled", "username", "password"};
        Cursor query = dbsettings.query("settvoti", // The table to query
                columnssettings, // The columns to return
                null, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                null // The sort order
        );
        query.moveToFirst();
        String enabled = query.getString(query.getColumnIndex("enabled"));
        dbsettings.close();
        if (enabled.matches("true")) {
            user.setVisibility(View.VISIBLE);
            user.setText(query.getString(query.getColumnIndex("username")));
            password.setVisibility(View.VISIBLE);
            password.setText(query.getString(query
                    .getColumnIndex("password")));
            save.setVisibility(View.VISIBLE);
            check.setVisibility(View.VISIBLE);
            toggle.setChecked(true);
        } else {
            user.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
            check.setVisibility(View.GONE);
        }
        query.close();

        EditText usernamepanini = (EditText) findViewById(R.id.usernamepanini);
        EditText passwordpanini = (EditText) findViewById(R.id.passwordpanini);
        SharedPreferences prefs = this.getSharedPreferences(
                "paniniauth", Context.MODE_PRIVATE);
        String usernsett = prefs.getString("username", "default");
        String passwsett = prefs.getString("password", "default");
        if (!usernsett.equals("default") && !passwsett.equals("default")) {
            usernamepanini.setText(usernsett);
            passwordpanini.setText(passwsett);
        }
    }

    public void onCheckClickedPanini(View view) {
        CheckBox toggle = (CheckBox) findViewById(R.id.checkBoxPaniniSettings);
        EditText password = (EditText) findViewById(R.id.passwordpanini);
        if (toggle.isChecked()) {
            password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            password.setInputType(129);
        }
        password.setSelection(password.getText().length());
    }

    public void onSaveClicked(View view) {
        EditText usert = (EditText) findViewById(R.id.username);
        EditText passwordt = (EditText) findViewById(R.id.password);
        String username = usert.getText().toString();
        String password = passwordt.getText().toString();
        Database databaseHelper = new Database(getBaseContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("enabled", "true");
        values.put("username", username);
        values.put("password", password);
        @SuppressWarnings("unused")
        long samerow = db.update("settvoti", values, null, null);
        db.close();
        Toast.makeText(this, getString(R.string.settingssaved),
                Toast.LENGTH_LONG).show();
    }

    public void onToggleClicked(View view) {
        ToggleButton toggle = (ToggleButton) findViewById(R.id.saveenabled);
        boolean on = toggle.isChecked();
        EditText user = (EditText) findViewById(R.id.username);
        EditText password = (EditText) findViewById(R.id.password);
        Button save = (Button) findViewById(R.id.savesett);
        CheckBox checkbox = (CheckBox) findViewById(R.id.checkBox1);
        if (on) {
            Database databaseHelper = new Database(getBaseContext());
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            String[] columns = {"username", "password"};
            Cursor query = db.query("settvoti", // The table to query
                    columns, // The columns to return
                    null, // The columns for the WHERE clause
                    null, // The values for the WHERE clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    null // The sort order
            );
            user.setVisibility(View.VISIBLE);
            query.moveToFirst();
            user.setText(query.getString(query.getColumnIndex("username")));
            password.setVisibility(View.VISIBLE);
            password.setText(query.getString(query.getColumnIndex("password")));
            save.setVisibility(View.VISIBLE);
            checkbox.setVisibility(View.VISIBLE);
            query.close();
            db.close();
        } else {
            user.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
            checkbox.setVisibility(View.GONE);
            Database databaseHelper = new Database(getBaseContext());
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("enabled", "false");
            @SuppressWarnings("unused")
            long samerow = db.update("settvoti", values, null, null);
            db.close();
            Toast.makeText(this, getString(R.string.noautologin),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onCheckClickedRegistro(View view) {
        CheckBox toggle = (CheckBox) findViewById(R.id.checkBox1);
        EditText password = (EditText) findViewById(R.id.password);
        if (toggle.isChecked()) {
            password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            password.setInputType(129);
        }
        password.setSelection(password.getText().length());
    }

    public void salvaPanini(View v){
        SharedPreferences prefs = MainActivity.context.getSharedPreferences(
                "paniniauth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username",((EditText) findViewById(R.id.usernamepanini)).getText().toString());
        editor.putString("password",((EditText) findViewById(R.id.passwordpanini)).getText().toString());
        editor.commit();
        Toast.makeText(this,"Impostazioni correttamente salvate",Toast.LENGTH_LONG).show();
    }
}