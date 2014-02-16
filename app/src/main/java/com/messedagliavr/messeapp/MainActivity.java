package com.messedagliavr.messeapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    //GENERAL
    public static String nointernet;
    public static View rootView;
    public static int section=0;
    //NEWS
    public Boolean unknhost = false;
    public SQLiteDatabase db;
    public Cursor data;
    //CALENDAR
    public ArrayList<Spanned> icalarr = new ArrayList<Spanned>();
    public static final String TITLE = "title";
    public static final String DESC = "description";
    public static final String ICAL = "ical";
    ProgressDialog mDialog;
    public String idical = null;

    //INFO
    static PackageInfo pinfo = null;
     /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }



    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        if (position==1){
            Toast.makeText(MainActivity.this, R.string.notavailable,
                    Toast.LENGTH_LONG).show();
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position))
                        .commit();
        }

    }

    @Override
    public void onBackPressed() {
        if (section==0){
            super.finish();
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, PlaceholderFragment.newInstance(0))
                    .commit();
        }
    }


    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.app_name);
                break;
            case 1:
                //mTitle = getString(R.string.panini);
                break;
            case 2:
                mTitle = getString(R.string.settings);
                break;
            case 5:
                mTitle = getString(R.string.Info);
                break;
            case 7:
                mTitle = getString(R.string.notizie);
                break;
            case 8:
                mTitle = getString(R.string.eventi);
                break;
            case 11:
                mTitle = getString(R.string.fine_scuola);
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            switch(section){
                case 7:
                    //News
                    getMenuInflater().inflate(R.menu.news, menu);
                    break;
                case 8:
                    //Calendar
                    getMenuInflater().inflate(R.menu.calendar, menu);
                    break;
                case 11:
                    //Fine Scuola
                    getMenuInflater().inflate(R.menu.fine_scuola, menu);
                    break;
                default:
                    getMenuInflater().inflate(R.menu.activity_main, menu);
                    break;
            }
        } else {
            getMenuInflater().inflate(R.menu.global, menu);
        }

        restoreActionBar();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        switch(section){
            case 8:
                //News
                getMenuInflater().inflate(R.menu.news, menu);
                break;
            case 9:
                //Calendar
                getMenuInflater().inflate(R.menu.calendar, menu);
                break;
            case 11:
                //Fine Scuola
                getMenuInflater().inflate(R.menu.fine_scuola, menu);
                break;
            default:
                getMenuInflater().inflate(R.menu.activity_main, menu);
                break;
        }
        restoreActionBar();

        return super.onCreateOptionsMenu(menu);
    }

    private final int FINE_SCUOLA_ID = 1;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_MENU){
           DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (mNavigationDrawerFragment.isDrawerOpen()){
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            } else {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        MyDifferenceFromToday diff;
        switch (id){
            case R.id.refreshend:
                diff = new MyDifferenceFromToday(2014,6,7,13,0);
                TextView end = (TextView) rootView.findViewById(R.id.fine_scuola);
                end.setText("Fine della scuola in:\n"+diff.getDays(diff.getDiff())+"g "+diff.getHours(diff.getDiff())+"h "+diff.getMinutes(diff.getDiff())+"m");
                break;
            case FINE_SCUOLA_ID:
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(11))
                        .commit();
                break;
            case R.id.timetoend:
                diff = new MyDifferenceFromToday(2014,6,7,13,0);
                item.getSubMenu().clear();
                item.getSubMenu().add(Menu.NONE, Menu.NONE, Menu.NONE, "Fine della scuola in:");
                item.getSubMenu().add(""+diff.getDays(diff.getDiff())+"giorni").setEnabled(false);
                item.getSubMenu().add(""+diff.getHours(diff.getDiff())+"ore").setEnabled(false);
                item.getSubMenu().add("" + diff.getMinutes(diff.getDiff()) + "min").setEnabled(false);
                break;
            case R.id.refresh:
                if (CheckInternet() == true) {
                    Database databaseHelper = new Database(getBaseContext());
                    SQLiteDatabase db = databaseHelper.getWritableDatabase();
                    ContentValues nowdb = new ContentValues();
                    nowdb.put("newsdate", "2012-02-20 15:00:00");
                    long samerow = db.update("lstchk", nowdb, null, null);
                    db.close();
                    MainActivity.nointernet = "false";
                    new connection().execute();
                } else {
                    Toast.makeText(this, R.string.noconnection,
                            Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.refreshcal:
                if (CheckInternet() == true) {
                    Database databaseHelper = new Database(getBaseContext());
                    SQLiteDatabase db = databaseHelper.getWritableDatabase();
                    ContentValues nowdb = new ContentValues();
                    nowdb.put("calendardate", "2012-02-20 15:00:00");
                    long samerow = db.update("lstchk", nowdb, null, null);
                    db.close();
                    MainActivity.nointernet = "false";
                    new connectioncalendar().execute();
                } else {
                    Toast.makeText(this, R.string.noconnection,
                            Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.palestre:
                Toast.makeText(MainActivity.this, R.string.notavailable,
                        Toast.LENGTH_LONG).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        switch (item.getItemId()) {
            case R.id.ical:
                if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) < 14) {
                    Toast.makeText(this, R.string.noapilevel,
                            Toast.LENGTH_LONG).show();
                } else {
                    idical = icalarr.get(info.position).toString();
                    new eventparser().execute();
                }

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
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
            String[] columns = { "username", "password" };
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

    public void onCheckClicked(View view) {
        CheckBox toggle = (CheckBox) findViewById(R.id.checkBox1);
        EditText password = (EditText) findViewById(R.id.password);
        if (toggle.isChecked()) {
            password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            password.setInputType(129);
        }
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

    public void social(View v) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(6))
                .commit();
    }

    public void youtube(View v) {
        Intent youtube = new Intent(Intent.ACTION_VIEW);
        youtube.setData(Uri.parse("http://www.youtube.com/user/MessedagliaWeb"));
        startActivity(youtube);
    }

    public void moodle(View v) {
        Intent moodle = new Intent(Intent.ACTION_VIEW);
        moodle.setData(Uri.parse("http://corsi.messedaglia.it"));
        startActivity(moodle);
    }

    public void facebook(View v) {
        String fbapp = "fb://group/110918169016604";
        Intent fbappi = new Intent(Intent.ACTION_VIEW, Uri.parse(fbapp));
        try {
            startActivity(fbappi);
        } catch (ActivityNotFoundException ex) {
            String uriMobile = "http://touch.facebook.com/groups/110918169016604";
            Intent fb = new Intent(Intent.ACTION_VIEW, Uri.parse(uriMobile));
            startActivity(fb);
        }
    }

    public void voti(View v) {
        Database databaseHelper = new Database(getBaseContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String[] columns = { "enabled", "username", "password" };
        Cursor query = db.query("settvoti", // The table to query
                columns, // The columns to return
                null, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                null // The sort order
        );
        query.moveToFirst();
        String enabled = query.getString(query.getColumnIndex("enabled"));
        db.close();
        if (enabled.matches("true")) {
            String user = query.getString(query.getColumnIndex("username"));
            String password = query.getString(query.getColumnIndex("password"));
            Intent voti = new Intent(Intent.ACTION_VIEW);
            voti.setData(Uri
                    .parse("https://web.spaggiari.eu/home/app/default/login.php?custcode=VRLS0003&login="
                            + user + "&password=" + password));
            query.close();
            startActivity(voti);
        } else {
            Intent voti = new Intent(Intent.ACTION_VIEW);
            voti.setData(Uri
                    .parse("https://web.spaggiari.eu/home/app/default/login.php?custcode=VRLS0003"));
            query.close();
            startActivity(voti);
        }
    }

    public void news(View v) {
        supportInvalidateOptionsMenu();
        if (CheckInternet()) {
            nointernet = "false";
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, PlaceholderFragment.newInstance(7))
                    .commit();
            new connection().execute();
        } else {
            String[] outdated = { "newsdate", "calendardate" };
            Database databaseHelper = new Database(getBaseContext());
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            String nodata = "1995-01-19 23:40:20";
            Cursor date = db.query("lstchk", // The table to query
                    outdated, // The columns to return
                    null, // The columns for the WHERE clause
                    null, // The values for the WHERE clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    null // The sort order
            );
            date.moveToFirst();
            String verifydatenews = date.getString(date
                    .getColumnIndex("newsdate"));
            date.close();
            db.close();
            if (!nodata.equals(verifydatenews)) {
                nointernet = "true";
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(7))
                        .commit();
                new connection().execute();
            } else {
                Toast.makeText(this, R.string.noconnection,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private Long getTimeDiff(String time, String curTime) throws ParseException {
        Date curDate = null;
        Date oldDate = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            curDate = formatter.parse(curTime);
            oldDate = formatter.parse(time);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        long oldMillis = oldDate.getTime();
        long curMillis = curDate.getTime();
        long diff = curMillis - oldMillis;
        return diff;
    }

    public void calendar(View v) {
        supportInvalidateOptionsMenu();
        if (CheckInternet()) {
            nointernet = "false";
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, PlaceholderFragment.newInstance(8))
                    .commit();
            new connectioncalendar().execute();
        } else {
            String[] outdated = { "newsdate", "calendardate" };
            Database databaseHelper = new Database(getBaseContext());
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            String nodata = "1995-01-19 23:40:20";
            Cursor date = db.query("lstchk", // The table to query
                    outdated, // The columns to return
                    null, // The columns for the WHERE clause
                    null, // The values for the WHERE clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    null // The sort order
            );
            date.moveToFirst();
            String verifydatenews = date.getString(date
                    .getColumnIndex("newsdate"));
            date.close();
            db.close();
            if (nodata != verifydatenews) {
                nointernet = "true";
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(8))
                        .commit();

                new connectioncalendar().execute();
            } else {
                Toast.makeText(this,
                        R.string.noconnection, Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    public void orario(View v) {
        startActivity(new Intent(this, timetable.class));
    }

    public void notavailable(View v) {
        Toast.makeText(this, R.string.notavailable,
                Toast.LENGTH_LONG).show();
    }

    public boolean CheckInternet() {
        boolean connected = false;
        ConnectivityManager connec = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connec
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connec
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnected()) {
            connected = true;
        } else {
            try {
                if (mobile.isConnected()) connected = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return connected;

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            section=sectionNumber;
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, section);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            if (rootView!=null) ((ViewGroup)rootView.getParent()).removeView(rootView);
            getActivity().supportInvalidateOptionsMenu();
            switch(section){
                case 0:
                    //home
                    rootView = inflater.inflate(R.layout.home, container, false);
                    break;
                case 1:
                    //Panini
                    rootView = inflater.inflate(R.layout.home, container, false);
                    break;
                case 2:
                    //settings
                    rootView = inflater.inflate(R.layout.settings, container, false);
                    EditText user = (EditText) rootView.findViewById(R.id.username);
                    EditText password = (EditText) rootView.findViewById(R.id.password);
                    CheckBox check = (CheckBox) rootView.findViewById(R.id.checkBox1);
                    Button save = (Button) rootView.findViewById(R.id.savesett);
                    ToggleButton toggle = (ToggleButton) rootView.findViewById(R.id.saveenabled);
                    Database databaseHelper = new Database(getActivity());
                    SQLiteDatabase db = databaseHelper.getWritableDatabase();
                    String[] columns = { "enabled", "username", "password" };
                    Cursor query = db.query("settvoti", // The table to query
                            columns, // The columns to return
                            null, // The columns for the WHERE clause
                            null, // The values for the WHERE clause
                            null, // don't group the rows
                            null, // don't filter by row groups
                            null // The sort order
                    );
                    query.moveToFirst();
                    String enabled = query.getString(query.getColumnIndex("enabled"));
                    db.close();
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
                    break;
                case 3:
                    //contacts
                    startActivity(new Intent(getActivity(), contacts.class));
                    break;
                case 4:
                    //suggestion
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getString(R.string.suggestion));
                    final EditText input = new EditText(getActivity());
                    input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    input.setVerticalScrollBarEnabled(true);
                    input.setSingleLine(false);
                    builder.setView(input);
                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String m_Text = input.getText().toString();
                                    Intent emailIntent = new Intent(
                                            Intent.ACTION_SENDTO, Uri.fromParts(
                                            "mailto", "support@nullpointerapps.com",
                                            null));
                                    emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                                            getString(R.string.suggestion));
                                    emailIntent.putExtra(Intent.EXTRA_TEXT,
                                            Html.fromHtml(m_Text));
                                    startActivity(Intent.createChooser(emailIntent,
                                            getString(R.string.suggestion)));
                                }
                            });
                    builder.setNegativeButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    builder.show();
                    break;
                case 5:
                    //info
                    rootView = inflater.inflate(R.layout.info, container, false);
                    String versionName = MainActivity.pinfo.versionName;
                    TextView vername = (TextView) rootView.findViewById(R.id.versionname);
                    vername.setText(versionName);
                    break;
                case 6:
                    //social
                    rootView = inflater.inflate(R.layout.social, container, false);
                    break;
                case 7:
                    //News
                    rootView = inflater.inflate(R.layout.list_item, container, false);
                    break;
                case 8:
                    //Calendar
                    rootView = inflater.inflate(R.layout.list_item, container, false);
                    break;
                case 11:
                    //Fine Scuola
                    rootView = inflater.inflate(R.layout.fine_scuola, container, false);
                    MyDifferenceFromToday diff = new MyDifferenceFromToday(2014,6,7,13,0);
                    TextView end = (TextView)rootView.findViewById(R.id.fine_scuola);
                    end.setText("Fine della scuola in:\n" + diff.getDays(diff.getDiff()) + "g " + diff.getHours(diff.getDiff()) + "h " + diff.getMinutes(diff.getDiff()) + "m");
                    break;
            }
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public class connection extends
            AsyncTask<Void, Void, HashMap<String, ArrayList<Spanned>>> {
        protected void onCancelled() {
            Intent main = new Intent(MainActivity.this, MainActivity.class);
            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(main);
            Toast.makeText(MainActivity.this, R.string.cancelednews, Toast.LENGTH_LONG)
                    .show();
        }

        public void onPreExecute() {
            if (MainActivity.nointernet == "true") {
                mDialog = ProgressDialog.show(MainActivity.this, getString(R.string.retrieving),
                        getString(R.string.retrievingNews), true, true,
                        new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                connection.this.cancel(true);
                            }
                        });
            } else {
                mDialog = ProgressDialog.show(MainActivity.this, getString(R.string.downloading),
                        getString(R.string.downloadingNews), true, true,
                        new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                connection.this.cancel(true);
                            }
                        });
            }
        }

        public HashMap<String, ArrayList<Spanned>> doInBackground(
                Void... params) {
            Database databaseHelper = new Database(getBaseContext());
            db = databaseHelper.getWritableDatabase();
            HashMap<String, ArrayList<Spanned>> temhashmap = new HashMap<String, ArrayList<Spanned>>();
            ArrayList<Spanned> titoli = new ArrayList<Spanned>();
            ArrayList<Spanned> descrizioni = new ArrayList<Spanned>();
            ArrayList<Spanned> titolib = new ArrayList<Spanned>();
            // All static variables
            final String URL = "http://www.messedaglia.it/index.php?option=com_ninjarsssyndicator&feed_id=1&format=raw";
            // XML node keys
            final String ITEM = "item"; // parent node
            final String TITLE = "title";
            final String DESC = "description";
            Element e = null;
            ArrayList<HashMap<String, Spanned>> menuItems = new ArrayList<HashMap<String, Spanned>>();
            String[] outdated = { "newsdate", "calendardate" };
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = df.format(c.getTime());
            Cursor date = db.query("lstchk", // The table to query
                    outdated, // The columns to return
                    null, // The columns for the WHERE clause
                    null, // The values for the WHERE clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    null // The sort order
            );
            date.moveToFirst();
            String past = date.getString(date.getColumnIndex("newsdate"));
            date.close();
            long l = getTimeDiff(past, now);
            if (l / 10800000 >= 3 && MainActivity.nointernet != "true") {
                XMLParser parser = new XMLParser();
                String xml = parser.getXmlFromUrl(URL);
                if (xml == "UnknownHostException") {
                    unknhost = true;
                    db.close();
                    return temhashmap;
                } else {
                    Document doc = parser.getDomElement(xml);
                    NodeList nl = doc.getElementsByTagName(ITEM);
                    ContentValues values = new ContentValues();
                    for (int i = 0; i < nl.getLength(); i++) {
                        HashMap<String, Spanned> map = new HashMap<String, Spanned>();
                        e = (Element) nl.item(i);
                        values.put("_id", i);
                        values.put(TITLE, parser.getValue(e, TITLE));
                        values.put(DESC, parser.getValue(e, DESC));
                        values.put("titleb", "<b>" + parser.getValue(e, TITLE)
                                + "</b>");
                        map.put(TITLE, Html.fromHtml(parser.getValue(e, TITLE)));
                        map.put(DESC, Html.fromHtml(parser.getValue(e, DESC)));

                        titoli.add(Html.fromHtml(parser.getValue(e, TITLE)));
                        descrizioni
                                .add(Html.fromHtml(parser.getValue(e, DESC)));
                        titolib.add(Html.fromHtml("<b>"
                                + parser.getValue(e, TITLE) + "</b>"));
                        // adding HashList to ArrayList
                        menuItems.add(map);
                        long newRowId = db.insertWithOnConflict("news", null,
                                values, SQLiteDatabase.CONFLICT_REPLACE);
                    }
                    ContentValues nowdb = new ContentValues();
                    nowdb.put("newsdate", now);
                    long samerow = db.update("lstchk", nowdb, null, null);
                    db.close();
                    temhashmap.put("titoli", titoli);
                    temhashmap.put("descrizioni", descrizioni);
                    temhashmap.put("titolib", titolib);
                    return temhashmap;

                }
            } else {
                String[] clmndata = { "title", "description", "titleb" };
                String sortOrder = "_id";

                data = db.query("news", // The table to query
                        clmndata, // The columns to return
                        null, // The columns for the WHERE clause
                        null, // The values for the WHERE clause
                        null, // don't group the rows
                        null, // don't filter by row groups
                        sortOrder // The sort order
                );
                for (data.move(0); data.moveToNext(); data.isAfterLast()) {
                    HashMap<String, Spanned> map = new HashMap<String, Spanned>();
                    map.put(TITLE, Html.fromHtml(data.getString(data
                            .getColumnIndex("title"))));
                    map.put(DESC, Html.fromHtml(data.getString(data
                            .getColumnIndex("description"))));

                    titoli.add(Html.fromHtml(data.getString(data
                            .getColumnIndex("title"))));
                    descrizioni.add(Html.fromHtml(data.getString(data
                            .getColumnIndex("description"))));
                    titolib.add(Html.fromHtml(data.getString(data
                            .getColumnIndex("titleb"))));
                    // adding HashList to ArrayList
                    menuItems.add(map);

                }
                data.close();
                db.close();
                temhashmap.put("titoli", titoli);
                temhashmap.put("descrizioni", descrizioni);
                temhashmap.put("titolib", titolib);
                return temhashmap;

            }

        }

        public void onPostExecute(HashMap<String, ArrayList<Spanned>> resultmap) {
            mDialog.dismiss();
            if (unknhost) {
                Toast.makeText(MainActivity.this, R.string.connerr, Toast.LENGTH_LONG)
                        .show();
                Intent main = new Intent(MainActivity.this, MainActivity.class);
                main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(main);
            } else {
                if (resultmap.size() > 0) {
                    final ArrayList<Spanned> titoli = resultmap.get("titoli");
                    final ArrayList<Spanned> descrizioni = resultmap
                            .get("descrizioni");
                    final ArrayList<Spanned> titolib = resultmap.get("titolib");

                    ArrayAdapter<Spanned> adapter = new ArrayAdapter<Spanned>(
                            MainActivity.this, android.R.layout.simple_list_item_1,
                            titolib);
                    ListView listView = (ListView) rootView.findViewById(android.R.id.list);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parentView,
                                                View childView, int position, long id) {
                            Intent intent = new Intent(MainActivity.this,
                                    ListItemSelectedNews.class);
                            intent.putExtra(TITLE,
                                    Html.toHtml(titoli.get(position)));
                            intent.putExtra(DESC,
                                    Html.toHtml(descrizioni.get(position)));
                            startActivity(intent);
                        }
                    });
                }
            }
        }
    }

    public class connectioncalendar extends
            AsyncTask<Void, Void, HashMap<String, ArrayList<Spanned>>> {

        Boolean unknhost = false;

        protected void onCancelled() {
            Intent main = new Intent(MainActivity.this, MainActivity.class);
            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(main);
            Toast.makeText(MainActivity.this, R.string.canceledcalendar,
                    Toast.LENGTH_LONG).show();
        }

        public void onPreExecute() {
            if (MainActivity.nointernet == "true") {
                mDialog = ProgressDialog.show(MainActivity.this, getString(R.string.retrieving),
                        getString(R.string.retrievingEvents), true, true,
                        new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                connectioncalendar.this.cancel(true);
                            }
                        });

            } else {
                mDialog = ProgressDialog.show(MainActivity.this, getString(R.string.downloading),
                        getString(R.string.downloadingEvents), true, true,
                        new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                connectioncalendar.this.cancel(true);
                            }
                        });
            }
        }

        @SuppressLint("SimpleDateFormat")
        public HashMap<String, ArrayList<Spanned>> doInBackground(
                Void... params) {
            Database databaseHelper = new Database(getBaseContext());
            db = databaseHelper.getWritableDatabase();
            HashMap<String, ArrayList<Spanned>> temhashmap = new HashMap<String, ArrayList<Spanned>>();
            ArrayList<Spanned> titoli = new ArrayList<Spanned>();
            ArrayList<Spanned> descrizioni = new ArrayList<Spanned>();
            ArrayList<Spanned> titolib = new ArrayList<Spanned>();
            final String URL = "http://www.messedaglia.it/index.php?option=com_jevents&task=modlatest.rss&format=feed&type=rss&Itemid=127&modid=162";
            String URLE = "http://www.messedaglia.it/caltoxml.php?id=";
            final String ITEM = "item";
            final String TITLE = "title";
            final String DESC = "description";
            Element e, e2 = null;
            ArrayList<HashMap<String, Spanned>> menuItems = new ArrayList<HashMap<String, Spanned>>();
            String[] outdated = { "newsdate", "calendardate" };
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = df.format(c.getTime());
            Cursor date = db.query("lstchk", // The table to query
                    outdated, // The columns to return
                    null, // The columns for the WHERE clause
                    null, // The values for the WHERE clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    null // The sort order
            );
            date.moveToFirst();
            String past = date.getString(date.getColumnIndex("calendardate"));
            date.close();
            long l = getTimeDiff(past, now);
            if (l / 10800000 >= 3 && MainActivity.nointernet != "true") {
                XMLParser parser = new XMLParser();
                String xml = parser.getXmlFromUrl(URL);
                if (xml == "UnknownHostException") {
                    unknhost = true;
                    db.close();
                    return temhashmap;
                } else {
                    Document doc = parser.getDomElement(xml);
                    NodeList nl;
                    nl = doc.getElementsByTagName(ITEM);
                    ContentValues values = new ContentValues();
                    Boolean ok = false;
                    HashMap<String, Integer> doppioni = new HashMap<String, Integer>();
                    for (int i = 1; i < nl.getLength(); i++) {
                        HashMap<String, Spanned> map = new HashMap<String, Spanned>();
                        e = (Element) nl.item(i);
                        e2 = (Element) nl.item(i - 1);
                        String idnp = parser.getValue(e, "link");
                        String idnp2 = parser.getValue(e2, "link");
                        char[] idnpa = idnp.toCharArray();
                        char[] idnpa2 = idnp2.toCharArray();
                        String icalr = "";
                        String icalr2 = "";
                        int cnt = 0;
                        int lnt = idnp.length();
                        for (int j = lnt - 1; j > 0; j--) {
                            if (idnpa[j] == '/') {
                                cnt++;
                            }
                            if (cnt == 2) {
                                icalr += idnpa[j - 1];
                                icalr2 += idnpa2[j - 1];
                            }
                            if (cnt > 2) {
                                j = 0;
                            }
                        }
                        char[] icalar = icalr.toCharArray();
                        char[] icalar2 = icalr2.toCharArray();
                        String ical = "";
                        String ical2 = "";
                        for (int k = icalr.length() - 2; k > -1; k--) {
                            ical += icalar[k];
                            ical2 += icalar2[k];
                        }
                        values.put("ical", ical);
                        values.put("_id", i);
                        map.put("ical", Html.fromHtml(ical));
                        if (doppioni.containsKey(ical)) {
                            int d = doppioni.get(ical);
                            doppioni.remove(ical);
                            d++;
                            doppioni.put(ical, d++);
                        } else {
                            doppioni.put(ical, 0);
                        }
                        String tito = parser.getValue(e, TITLE);
                        int n = tito.charAt(0);
                        int n2 = tito.charAt(1);
                        StringBuffer buf = new StringBuffer(tito);

                        switch (tito.charAt(3) + tito.charAt(4)
                                + tito.charAt(5)) {
                            case 282:// GEN
                                if (n2 + doppioni.get(ical) >= 58) {
                                    if (n + 1 < 51
                                            || (n + 1 == 51 && n2
                                            + doppioni.get(ical) <= 59)) {
                                        buf.setCharAt(0, (char) (n + 1));

                                        buf.setCharAt(
                                                1,
                                                (char) (n2 + doppioni.get(ical) - 10));

                                    } else {
                                        buf.setCharAt(
                                                1,
                                                (char) (n2 + doppioni.get(ical) - 11));
                                        buf.setCharAt(0, '0');
                                        buf.setCharAt(3, 'F');
                                        buf.setCharAt(4, 'e');
                                        buf.setCharAt(5, 'b');
                                    }
                                } else {
                                    if (n == 51) {
                                        if (n2 + doppioni.get(ical) <= 49) {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical)));
                                        } else {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical) - 1));
                                            buf.setCharAt(0, '0');
                                            buf.setCharAt(3, 'F');
                                            buf.setCharAt(4, 'e');
                                            buf.setCharAt(5, 'b');
                                        }
                                    } else {
                                        buf.setCharAt(1,
                                                (char) (n2 + doppioni.get(ical)));
                                    }
                                }
                                break;
                            case 269: // FEB
                                if (n2 + doppioni.get(ical) >= 58) {
                                    if (n + 1 < 50
                                            || (n + 1 == 51 && n2
                                            + doppioni.get(ical) <= 66)) {
                                        buf.setCharAt(0, (char) (n + 1));

                                        buf.setCharAt(
                                                1,
                                                (char) (n2 + doppioni.get(ical) - 10));

                                    } else {
                                        buf.setCharAt(
                                                1,
                                                (char) (n2 + doppioni.get(ical) - 18));
                                        buf.setCharAt(0, '0');
                                        buf.setCharAt(3, 'M');
                                        buf.setCharAt(4, 'a');
                                        buf.setCharAt(5, 'r');
                                    }
                                } else {
                                    if (n == 50) {
                                        if (n2 + doppioni.get(ical) <= 56) {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical)));
                                        } else {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical) - 8));
                                            buf.setCharAt(0, '0');
                                            buf.setCharAt(3, 'M');
                                            buf.setCharAt(4, 'a');
                                            buf.setCharAt(5, 'r');
                                        }
                                    } else {
                                        buf.setCharAt(1,
                                                (char) (n2 + doppioni.get(ical)));
                                    }
                                }
                                break;
                            case 288: // Mar
                                if (n2 + doppioni.get(ical) >= 58) {
                                    if (n + 1 < 51
                                            || (n + 1 == 51 && n2
                                            + doppioni.get(ical) <= 59)) {
                                        buf.setCharAt(0, (char) (n + 1));

                                        buf.setCharAt(
                                                1,
                                                (char) (n2 + doppioni.get(ical) - 10));

                                    } else {
                                        buf.setCharAt(
                                                1,
                                                (char) (n2 + doppioni.get(ical) - 11));
                                        buf.setCharAt(0, '0');
                                        buf.setCharAt(3, 'A');
                                        buf.setCharAt(4, 'p');
                                        buf.setCharAt(5, 'r');
                                    }
                                } else {
                                    if (n == 51) {
                                        if (n2 + doppioni.get(ical) <= 49) {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical)));
                                        } else {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical) - 1));
                                            buf.setCharAt(0, '0');
                                            buf.setCharAt(3, 'A');
                                            buf.setCharAt(4, 'p');
                                            buf.setCharAt(5, 'r');
                                        }
                                    } else {
                                        buf.setCharAt(1,
                                                (char) (n2 + doppioni.get(ical)));
                                    }
                                }
                                break;
                            case 291: // Apr
                                if (n2 + doppioni.get(ical) >= 58) {
                                    buf.setCharAt(1,
                                            (char) (n2 + doppioni.get(ical) - 10));
                                    if (n + 1 < 51
                                            || (n + 1 == 51 && n2
                                            + doppioni.get(ical) == 58)) {
                                        buf.setCharAt(0, (char) (n + 1));

                                    } else {
                                        buf.setCharAt(0, '0');
                                        buf.setCharAt(3, 'M');
                                        buf.setCharAt(4, 'a');
                                        buf.setCharAt(5, 'g');
                                    }
                                } else {
                                    buf.setCharAt(1,
                                            (char) (n2 + doppioni.get(ical)));
                                }
                                break;
                            case 277: // Mag
                                if (n2 + doppioni.get(ical) >= 58) {
                                    if (n + 1 < 51
                                            || (n + 1 == 51 && n2
                                            + doppioni.get(ical) <= 59)) {
                                        buf.setCharAt(0, (char) (n + 1));

                                        buf.setCharAt(
                                                1,
                                                (char) (n2 + doppioni.get(ical) - 10));

                                    } else {
                                        buf.setCharAt(
                                                1,
                                                (char) (n2 + doppioni.get(ical) - 11));
                                        buf.setCharAt(0, '0');
                                        buf.setCharAt(3, 'G');
                                        buf.setCharAt(4, 'i');
                                        buf.setCharAt(5, 'u');
                                    }
                                } else {
                                    if (n == 51) {
                                        if (n2 + doppioni.get(ical) <= 49) {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical)));
                                        } else {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical) - 1));
                                            buf.setCharAt(0, '0');
                                            buf.setCharAt(3, 'G');
                                            buf.setCharAt(4, 'i');
                                            buf.setCharAt(5, 'u');
                                        }
                                    } else {
                                        buf.setCharAt(1,
                                                (char) (n2 + doppioni.get(ical)));
                                    }
                                }
                                break;
                            case 293: // Giu
                                if (n2 + doppioni.get(ical) >= 58) {
                                    buf.setCharAt(1,
                                            (char) (n2 + doppioni.get(ical) - 10));
                                    if (n + 1 < 51
                                            || (n + 1 == 51 && n2
                                            + doppioni.get(ical) == 58)) {
                                        buf.setCharAt(0, (char) (n + 1));

                                    } else {
                                        buf.setCharAt(0, '0');
                                        buf.setCharAt(3, 'L');
                                        buf.setCharAt(4, 'u');
                                        buf.setCharAt(5, 'g');
                                    }
                                } else {
                                    buf.setCharAt(1,
                                            (char) (n2 + doppioni.get(ical)));
                                }
                                break;
                            case 296: // Lug
                                if (n2 + doppioni.get(ical) >= 58) {
                                    if (n + 1 < 51
                                            || (n + 1 == 51 && n2
                                            + doppioni.get(ical) <= 59)) {
                                        buf.setCharAt(0, (char) (n + 1));

                                        buf.setCharAt(
                                                1,
                                                (char) (n2 + doppioni.get(ical) - 10));

                                    } else {
                                        buf.setCharAt(
                                                1,
                                                (char) (n2 + doppioni.get(ical) - 11));
                                        buf.setCharAt(0, '0');
                                        buf.setCharAt(3, 'A');
                                        buf.setCharAt(4, 'g');
                                        buf.setCharAt(5, 'g');
                                    }
                                } else {
                                    if (n == 51) {
                                        if (n2 + doppioni.get(ical) <= 49) {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical)));
                                        } else {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical) - 1));
                                            buf.setCharAt(0, '0');
                                            buf.setCharAt(3, 'A');
                                            buf.setCharAt(4, 'g');
                                            buf.setCharAt(5, 'o');
                                        }
                                    } else {
                                        buf.setCharAt(1,
                                                (char) (n2 + doppioni.get(ical)));
                                    }
                                }
                                break;
                            case 279: // Ago
                                if (n2 + doppioni.get(ical) >= 58) {
                                    if (n + 1 < 51
                                            || (n + 1 == 51 && n2
                                            + doppioni.get(ical) <= 59)) {
                                        buf.setCharAt(0, (char) (n + 1));

                                        buf.setCharAt(
                                                1,
                                                (char) (n2 + doppioni.get(ical) - 10));

                                    } else {
                                        buf.setCharAt(
                                                1,
                                                (char) (n2 + doppioni.get(ical) - 11));
                                        buf.setCharAt(0, '0');
                                        buf.setCharAt(3, 'S');
                                        buf.setCharAt(4, 'e');
                                        buf.setCharAt(5, 't');
                                    }
                                } else {
                                    if (n == 51) {
                                        if (n2 + doppioni.get(ical) <= 49) {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical)));
                                        } else {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical) - 1));
                                            buf.setCharAt(0, '0');
                                            buf.setCharAt(3, 'S');
                                            buf.setCharAt(4, 'e');
                                            buf.setCharAt(5, 't');
                                        }
                                    } else {
                                        buf.setCharAt(1,
                                                (char) (n2 + doppioni.get(ical)));
                                    }
                                }
                                break;
                            case 300: // Set
                                if (n2 + doppioni.get(ical) >= 58) {
                                    buf.setCharAt(1,
                                            (char) (n2 + doppioni.get(ical) - 10));
                                    if (n + 1 < 51
                                            || (n + 1 == 51 && n2
                                            + doppioni.get(ical) == 58)) {
                                        buf.setCharAt(0, (char) (n + 1));

                                    } else {
                                        buf.setCharAt(0, '0');
                                        buf.setCharAt(3, 'O');
                                        buf.setCharAt(4, 't');
                                        buf.setCharAt(5, 't');
                                    }
                                } else {
                                    buf.setCharAt(1,
                                            (char) (n2 + doppioni.get(ical)));
                                }
                                break;
                            case 311: // Ott
                                if (n2 + doppioni.get(ical) == 58) {
                                    if (n + 1 <= 51) {
                                        buf.setCharAt(0, (char) (n + 1));
                                        buf.setCharAt(1, '0');
                                    } else {
                                        buf.setCharAt(1, '1');
                                        buf.setCharAt(0, '0');
                                        buf.setCharAt(3, 'N');
                                        buf.setCharAt(4, 'o');
                                        buf.setCharAt(5, 'v');
                                    }
                                } else {
                                    if (n == 51) {
                                        if (n2 + doppioni.get(ical) <= 49) {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical)));
                                        } else {
                                            buf.setCharAt(1, '1');
                                            buf.setCharAt(0, '0');
                                            buf.setCharAt(3, 'N');
                                            buf.setCharAt(4, 'o');
                                            buf.setCharAt(5, 'v');
                                        }
                                    } else {
                                        buf.setCharAt(1,
                                                (char) (n2 + doppioni.get(ical)));
                                    }
                                }
                                break;
                            case 307: // Nov
                                if (n2 + doppioni.get(ical) >= 58) {
                                    buf.setCharAt(1,
                                            (char) (n2 + doppioni.get(ical) - 10));
                                    if (n + 1 < 51
                                            || (n + 1 == 51 && n2
                                            + doppioni.get(ical) == 58)) {
                                        buf.setCharAt(0, (char) (n + 1));

                                    } else {
                                        buf.setCharAt(0, '0');
                                        buf.setCharAt(3, 'D');
                                        buf.setCharAt(4, 'i');
                                        buf.setCharAt(5, 'c');
                                    }
                                } else {
                                    buf.setCharAt(1,
                                            (char) (n2 + doppioni.get(ical)));
                                }
                                break;
                            case 272: // Dic
                                if (n2 + doppioni.get(ical) >= 58) {
                                    if (n + 1 < 51
                                            || (n + 1 == 51 && n2
                                            + doppioni.get(ical) <= 59)) {
                                        buf.setCharAt(0, (char) (n + 1));

                                        buf.setCharAt(
                                                1,
                                                (char) (n2 + doppioni.get(ical) - 10));

                                    } else {
                                        buf.setCharAt(
                                                1,
                                                (char) (n2 + doppioni.get(ical) - 11));
                                        buf.setCharAt(0, '0');
                                        buf.setCharAt(3, 'G');
                                        buf.setCharAt(4, 'e');
                                        buf.setCharAt(5, 'n');
                                    }
                                } else {
                                    if (n == 51) {
                                        if (n2 + doppioni.get(ical) <= 49) {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical)));
                                        } else {
                                            buf.setCharAt(
                                                    1,
                                                    (char) (n2 + doppioni.get(ical) - 1));
                                            buf.setCharAt(0, '0');
                                            buf.setCharAt(3, 'G');
                                            buf.setCharAt(4, 'e');
                                            buf.setCharAt(5, 'n');
                                        }
                                    } else {
                                        buf.setCharAt(1,
                                                (char) (n2 + doppioni.get(ical)));
                                    }
                                }
                                break;
                        }

                        tito = buf.toString();
                        values.put(TITLE, tito);
                        map.put(TITLE, Html.fromHtml(tito));
                        titoli.add(Html.fromHtml(tito));
                        titolib.add(Html.fromHtml("<b>" + tito + "</b>"));

                        values.put(DESC, parser.getValue(e, DESC));
                        values.put("titleb", "<b>" + tito + "</b>");
                        map.put(DESC, Html.fromHtml(parser.getValue(e, DESC)));
                        descrizioni
                                .add(Html.fromHtml(parser.getValue(e, DESC)));
                        icalarr.add(Html.fromHtml(ical));
                        menuItems.add(map);
                        long newRowId = db.insertWithOnConflict("calendar",
                                null, values, SQLiteDatabase.CONFLICT_REPLACE);

                    }
                    ContentValues nowdb = new ContentValues();
                    nowdb.put("calendardate", now);
                    long samerow = db.update("lstchk", nowdb, null, null);
                    db.close();
                    temhashmap.put("titoli", titoli);
                    temhashmap.put("descrizioni", descrizioni);
                    temhashmap.put("ical", icalarr);
                    temhashmap.put("titolib", titolib);
                    return temhashmap;

                }

            } else {
                String[] clmndata = { "title", "description", "titleb", "ical" };
                String sortOrder = "_id";

                data = db.query("calendar", // The table to query
                        clmndata,
                        // The columns to return
                        null, // The columns for the WHERE clause
                        null, // The values for the WHERE clause
                        null, // don't group the rows
                        null, // don't filter by row groups
                        sortOrder // The sortorder
                );

                for (data.move(0); data.moveToNext(); data.isAfterLast()) {
                    HashMap<String, Spanned> map = new HashMap<String, Spanned>();
                    map.put(TITLE, Html.fromHtml(data.getString(data
                            .getColumnIndex("title"))));
                    map.put(DESC, Html.fromHtml(data.getString(data
                            .getColumnIndex("description"))));
                    map.put("ical", Html.fromHtml(data.getString(data
                            .getColumnIndex("ical"))));

                    titoli.add(Html.fromHtml(data.getString(data
                            .getColumnIndex("title"))));
                    descrizioni.add(Html.fromHtml(data.getString(data
                            .getColumnIndex("description"))));
                    icalarr.add(Html.fromHtml(data.getString(data
                            .getColumnIndex("ical"))));
                    titolib.add(Html.fromHtml(data.getString(data
                            .getColumnIndex("titleb"))));
                    menuItems.add(map);

                }
                data.close();
                db.close();
                temhashmap.put("titoli", titoli);
                temhashmap.put("descrizioni", descrizioni);
                temhashmap.put("ical", icalarr);
                temhashmap.put("titolib", titolib);
                return temhashmap;

            }

        }

        public void onPostExecute(HashMap<String, ArrayList<Spanned>> resultmap) {
            if (unknhost) {
                Toast.makeText(MainActivity.this, R.string.connerr,
                        Toast.LENGTH_LONG).show();
                Intent main = new Intent(MainActivity.this, MainActivity.class);
                main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(main);
            } else {
                if (resultmap.size() > 0) {
                    final ArrayList<Spanned> titoli = resultmap.get("titoli");
                    final ArrayList<Spanned> descrizioni = resultmap
                            .get("descrizioni");
                    final ArrayList<Spanned> titolib = resultmap.get("titolib");
                    final ArrayList<Spanned> icalarr = resultmap.get("ical");
                    ArrayAdapter<Spanned> adapter = new ArrayAdapter<Spanned>(
                            MainActivity.this, android.R.layout.simple_list_item_1,
                            titolib);
                    ListView listView = (ListView) rootView.findViewById(android.R.id.list);
                    listView.setAdapter(adapter);

                    registerForContextMenu(findViewById(android.R.id.list));

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parentView,
                                                View childView, int position, long id) {
                            if (Html.toHtml(descrizioni.get(position)) != "") {
                                Intent intent = new Intent(MainActivity.this,
                                        ListItemSelectedCalendar.class);
                                intent.putExtra(TITLE,
                                        Html.toHtml(titoli.get(position)));
                                intent.putExtra(DESC,
                                        Html.toHtml(descrizioni.get(position)));
                                intent.putExtra(ICAL,
                                        Html.toHtml(icalarr.get(position)));
                                startActivity(intent);
                            } else {
                                Toast.makeText(MainActivity.this,
                                        R.string.nodescription,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
            mDialog.dismiss();
        }
    }

    @SuppressLint("SimpleDateFormat")
    public class eventparser extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String ical = "http://www.messedaglia.it/caltoxml.php?id=" + idical;
            XMLParser parser = new XMLParser();
            String xml = parser.getXmlFromUrl(ical);
            if (xml == "UnknownHostException") {
            } else {
                Document doc = parser.getDomElement(xml);
                NodeList nl = doc.getElementsByTagName("VEVENT");

                String[] dati = { "", "", "", "", "" };
                Element e = (Element) nl.item(0);
                dati[0] = parser.getValue(e, "SUMMARY");
                int l = parser.getValue(e, "DESCRIPTION").length();
                if (l == 0) {
                    dati[1] = "Nessuna descrizione";
                } else {
                    dati[1] = parser.getValue(e, "DESCRIPTION").substring(4,
                            l - 3);
                }
                dati[2] = parser.getValue(e, "LOCATION");
                dati[3] = parser.getValue(e, "DTSTART");
                dati[4] = parser.getValue(e, "DTEND");
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "yyyyMMdd'T'HHmmss");
                Date fine = null;
                Date inizio = null;
                try {
                    fine = dateFormat.parse(dati[4].toString());
                    inizio = dateFormat.parse(dati[3].toString());
                    Intent intent = new Intent(Intent.ACTION_INSERT)
                            .setType("vnd.android.cursor.item/event")
                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                    inizio.getTime())
                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                                    fine.getTime())
                            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY,
                                    false)
                            .putExtra(CalendarContract.Events.TITLE, dati[0])
                            .putExtra(CalendarContract.Events.DESCRIPTION, dati[1])
                            .putExtra(CalendarContract.Events.EVENT_LOCATION,
                                    dati[2] + " A. Messedaglia");
                    startActivity(intent);
                } catch (java.text.ParseException e1) {
                    e1.printStackTrace();
                } catch (Exception e2) {
                    e2.printStackTrace();
                    Toast.makeText(MainActivity.this, R.string.noapilevel,
                            Toast.LENGTH_LONG).show();
                }
            }
            return null;
        }

    }

}
