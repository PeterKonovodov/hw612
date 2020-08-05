package com.example.hw612;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ListViewActivity extends AppCompatActivity {

    static final String TAG = "HW612";
    private ListView listView;
    private ArrayList<Integer> deletedIndexes = new ArrayList<>();
    private SimpleAdapter listContentAdapter = null;
    private final ArrayList<HashMap<String, Object>> list = new ArrayList<>();
    private SharedPreferences notesSharedPref;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            deletedIndexes.addAll(savedInstanceState.getIntegerArrayList("deletedIndexes"));
            Log.i(TAG, String.format("onCreate, %d indexes restored", deletedIndexes.size()));
        }

        setContentView(R.layout.activity_list_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = findViewById(R.id.listView);
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);

        swipeRefreshLayoutInit(this);

        notesSharedPref = getSharedPreferences("Notes", MODE_PRIVATE);

        listViewInit(this);


    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList("deletedIndexes", deletedIndexes);
        Log.i(TAG, String.format("onSaveInstanceState, %d indexes saved", deletedIndexes.size()));
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        if(savedInstanceState.containsKey("deletedIndexes")) {
//            deletedIndexes.addAll(savedInstanceState.getIntegerArrayList("deletedIndexes"));
//            Log.i(TAG, String.format("onRestoreInstanceState, %d indexes restored", deletedIndexes.size()));
//        }
    }


    public void swipeRefreshLayoutInit(final Context context) {

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listViewInit(context);
                mSwipeRefreshLayout.setRefreshing(false);
                deletedIndexes.clear();
            }
        });
    }


    public void listViewInit(final Context context) {
        prepareContent();
        if (!deletedIndexes.isEmpty()) {
            for (Integer i : deletedIndexes) {
                list.remove((int) i);
            }
        }
        listContentAdapter = createAdapter();
        listView.setAdapter(listContentAdapter);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                list.remove(position);
                listContentAdapter.notifyDataSetChanged();
                deletedIndexes.add(position);
                Toast.makeText(context, String.format("Запись %d удалена, всего: %d записей", position, list.size()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    private SimpleAdapter createAdapter() {
        int[] to = {R.id.text, R.id.textLength, R.id.textImage};
        String[] from = {"text", "length", "icon"};
        return new SimpleAdapter(this, list, R.layout.list_item, from, to);
    }


    private void prepareContent() {
        Random rnd = new Random();
        HashMap<String, Object> map;
        boolean needToSave = false;
        int[] icons = {R.mipmap.bbicon_foreground, R.drawable.i1, R.drawable.i2,
                R.drawable.i3, R.drawable.i4, R.drawable.i5, R.drawable.i6,
                R.drawable.i7, R.drawable.i8,};

        String[] stringArray;
        stringArray = loadStrings();
        if (stringArray == null) {
            stringArray = getString(R.string.large_text).split("\n\n");
            Toast.makeText(this, String.format("Создано %d записей", stringArray.length), Toast.LENGTH_SHORT).show();
            needToSave = true;
        }
        list.clear();
        for (String s : stringArray) {
            map = new HashMap<>();
            map.put("text", s);
            map.put("length", s.length() + "");
            map.put("icon", icons[rnd.nextInt(icons.length)]);
            list.add(map);
        }
        if (needToSave) saveListStrings(false);
    }


    private String[] loadStrings() {
        int size = notesSharedPref.getAll().size();
        if (size == 0) return null;
        String[] stringArray = new String[size];
        boolean noDataInSharedPref = true;

        for (int i = 0; i < size; i++) {
            if (notesSharedPref.contains(String.format("Note%d", i))) {
                stringArray[i] = notesSharedPref.getString(String.format("Note%d", i), "");
                noDataInSharedPref = false;
            }
        }
        if (noDataInSharedPref) return null;
        Toast.makeText(this, String.format("Загружено %d записей", stringArray.length - deletedIndexes.size()), Toast.LENGTH_SHORT).show();
        return stringArray;
    }


    private void saveListStrings(boolean showToast) {
        SharedPreferences.Editor editor = notesSharedPref.edit();
        for (int i = 0; i < list.size(); i++) {
            editor.putString(String.format("Note%d", i), (String) (list.get(i).get("text")));
            editor.apply();
        }
        if (showToast)
            Toast.makeText(this, String.format("Сохранено %d записей", list.size()), Toast.LENGTH_SHORT).show();
    }


}
