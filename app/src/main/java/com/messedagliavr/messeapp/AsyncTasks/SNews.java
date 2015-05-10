package com.messedagliavr.messeapp.AsyncTasks;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.messedagliavr.messeapp.Adapters.NewsAdapter;
import com.messedagliavr.messeapp.Databases.MainDB;
import com.messedagliavr.messeapp.ListItemSelectedNewsActivity;
import com.messedagliavr.messeapp.MainActivity;
import com.messedagliavr.messeapp.NewsActivity;
import com.messedagliavr.messeapp.Parsers.XMLParser;
import com.messedagliavr.messeapp.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class SNews extends
        AsyncTask<Void, Void, HashMap<String, ArrayList<Spanned>>> {
    Boolean showLoading = true;
    NewsActivity na;
    ProgressDialog mDialog;
    public SQLiteDatabase db;
    Boolean unknhost;
    Cursor data;

    public static final String TITLE = "title";
    public static final String DESC = "description";

    public SNews(NewsActivity na, Boolean showLoading) {
        this.na = na;
        this.showLoading = showLoading;
    }

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

    protected void onCancelled() {
        Intent main = new Intent(na, MainActivity.class);
        na.startActivity(main);
        Toast.makeText(na, R.string.cancelednews, Toast.LENGTH_LONG)
                .show();
    }

    public void onPreExecute() {
        MainDB databaseHelper = new MainDB(na.getBaseContext());
        db = databaseHelper.getWritableDatabase();
        String[] outdated = {"newsdate", "calendardate"};
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
        db.close();
        long l = getTimeDiff(past, now);
        if (NewsActivity.nointernet.equals("true") && showLoading && l / 10800000 >= 3) {
            mDialog = ProgressDialog.show(na, null,
                    na.getString(R.string.retrievingNews), true, true,
                    new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            SNews.this.cancel(true);
                        }
                    });
        } else if (showLoading && l / 10800000 >= 3) {
            mDialog = ProgressDialog.show(na, null,
                    na.getString(R.string.downloadingNews), true, true,
                    new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            SNews.this.cancel(true);
                        }
                    });
        } else if (past.equals("1995-01-19 23:40:20")){
            mDialog = ProgressDialog.show(na, null,
                    na.getString(R.string.downloadingNews), true, true,
                    new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            SNews.this.cancel(true);
                        }
                    });
        } else {
            showLoading=false;
        }
    }

    public HashMap<String, ArrayList<Spanned>> doInBackground(
            Void... params) {
        unknhost=false;
        MainDB databaseHelper = new MainDB(na.getBaseContext());
        db = databaseHelper.getWritableDatabase();
        HashMap<String, ArrayList<Spanned>> temhashmap = new HashMap<String, ArrayList<Spanned>>();
        ArrayList<Spanned> titoli = new ArrayList<Spanned>();
        ArrayList<Spanned> descrizioni = new ArrayList<Spanned>();
        ArrayList<Spanned> datePubList = new ArrayList<Spanned>();
        ArrayList<Spanned> titolib = new ArrayList<Spanned>();
        // All static variables
        final String URL = "https://www.messedaglia.it/index.php?option=com_ninjarsssyndicator&feed_id=1&format=raw";
        // XML node keys
        final String ITEM = "item"; // parent node
        final String TITLE = "title";
        final String DESC = "description";
        final String PUBDATE = "pubDate";
        Element e = null;
        ArrayList<HashMap<String, Spanned>> menuItems = new ArrayList<HashMap<String, Spanned>>();
        String[] outdated = {"newsdate", "calendardate"};
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
        if (l / 10800000 >= 3 && !NewsActivity.nointernet.equals("true")) {
            XMLParser parser = new XMLParser();
            String xml = parser.getXmlFromUrl(URL);
            if (xml.equals("UnknownHostException")) {
                unknhost = true;
                db.close();
                return temhashmap;
            } else {
                Document doc = parser.getDomElement(xml);
                NodeList nl = doc.getElementsByTagName(ITEM);
                ContentValues values = new ContentValues();
                SimpleDateFormat parserDatePub = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                DateFormat datePubFormat;
                Locale currentLocale = na.getResources().getConfiguration().locale;
                datePubFormat = DateFormat.getDateInstance(DateFormat.FULL, currentLocale);
                String datePubLocale = "";
                String titleb = "";
                String title="";
                String desc="";
                for (int i = 0; i < nl.getLength(); i++) {
                    HashMap<String, Spanned> map = new HashMap<String, Spanned>();
                    e = (Element) nl.item(i);
                    title= parser.getValue(e,TITLE);
                    titleb="<b>" + parser.getValue(e, TITLE)
                            + "</b>";
                    desc=parser.getValue(e, DESC);
                    values.put("_id", i);
                    values.put(TITLE, title);
                    values.put(DESC, desc);

                    try {
                        datePubLocale = datePubFormat.format(parserDatePub.parse(parser.getValue(e, PUBDATE))).toString();
                    } catch (java.text.ParseException dateP) {
                        System.out.println(dateP);

                    }
                    values.put(PUBDATE, datePubLocale);
                    values.put("titleb", titleb);
                    map.put(TITLE, Html.fromHtml(title));
                    map.put(DESC, Html.fromHtml(desc));
                    map.put(PUBDATE, Html.fromHtml(datePubLocale));

                    titoli.add(Html.fromHtml(title));
                    descrizioni
                            .add(Html.fromHtml(desc));
                    datePubList
                            .add(Html.fromHtml(datePubLocale));
                    titolib.add(Html.fromHtml(titleb));
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
                temhashmap.put("pubdate", datePubList);
                return temhashmap;
            }
        } else {
            String[] clmndata = {"title", "pubdate", "description", "titleb"};
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
                map.put(PUBDATE, Html.fromHtml(data.getString(data
                        .getColumnIndex("pubdate"))));

                titoli.add(Html.fromHtml(data.getString(data
                        .getColumnIndex("title"))));
                descrizioni.add(Html.fromHtml(data.getString(data
                        .getColumnIndex("description"))));
                titolib.add(Html.fromHtml(data.getString(data
                        .getColumnIndex("titleb"))));
                datePubList.add(Html.fromHtml(data.getString(data
                        .getColumnIndex("pubdate"))));
                // adding HashList to ArrayList
                menuItems.add(map);

            }
            data.close();
            db.close();
            temhashmap.put("titoli", titoli);
            temhashmap.put("descrizioni", descrizioni);
            temhashmap.put("titolib", titolib);
            temhashmap.put("pubdate", datePubList);
            return temhashmap;

        }

    }

    public void onPostExecute(HashMap<String, ArrayList<Spanned>> resultmap) {
        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) na.findViewById(R.id.listview_swipe_refresh_layout);
        if (unknhost) {
            Toast.makeText(na, R.string.connerr, Toast.LENGTH_LONG)
                    .show();
            Intent main = new Intent(na, MainActivity.class);
            na.startActivity(main);
        } else {

            if (resultmap.size() > 0) {

                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                       na.refreshNews();
                    }
                });
                mSwipeRefreshLayout.setColorSchemeColors(Color.parseColor("#fffbb901"),Color.parseColor("#ff1a171b"));
                final ArrayList<Spanned> titoli = resultmap.get("titoli");
                final ArrayList<Spanned> descrizioni = resultmap
                        .get("descrizioni");
                final ArrayList<Spanned> pubDate = resultmap
                        .get("pubdate");
                final ArrayList<Spanned> titolib = resultmap.get("titolib");

                NewsAdapter adapter = new NewsAdapter(
                        na, R.layout.item_news,
                        titolib, pubDate);
                ListView listView = (ListView) na.findViewById(R.id.list);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parentView,
                                            View childView, int position, long id) {
                        Intent intent = new Intent(na,
                                ListItemSelectedNewsActivity.class);
                        intent.putExtra(TITLE,
                                Html.toHtml(titoli.get(position)));
                        intent.putExtra(DESC,
                                Html.toHtml(descrizioni.get(position)));
                        if (Build.VERSION.SDK_INT >= 21) {
                            na.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(na).toBundle());
                        } else {
                            na.startActivity(intent);
                        }
                    }
                });
            }
        }
        if (showLoading) {
            mDialog.dismiss();
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
