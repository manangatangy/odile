package com.wolfie.odile.model.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.wolfie.odile.model.DataSet;
import com.wolfie.odile.model.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a database to provide the basic CRUD operations for both tables; master and entries.
 * These methods are best called in a background thread, using a loader.
 */
public class Source {

    private SQLiteDatabase mDatabase;

    public Source(SQLiteDatabase database) {
        mDatabase = database;
    }

    public boolean insert(Entry entry) {
        long result = mDatabase.insert(MetaData.ENTRIES_TABLE, null, makeContentValues(entry));
        return result != -1;
    }

    public boolean update(Entry entry) {
        int result = mDatabase.update(MetaData.ENTRIES_TABLE, makeContentValues(entry),
                    MetaData.ENTRIES_ID + "=" + entry.getId(), null);
        return result != 0;
    }

    public boolean delete(Entry entry) {
        int result =  mDatabase.delete(MetaData.ENTRIES_TABLE, MetaData.ENTRIES_ID + "=" + entry.getId(), null);
        return result != 0;
    }

    public void deleteAll() {
        mDatabase.delete(MetaData.ENTRIES_TABLE, null, null);
    }

    public @NonNull DataSet read() {
        List<Entry> entries = new ArrayList<>();
        // No point sorting here - the strings are encrypted - duh!
        Cursor cursor = mDatabase.query(MetaData.ENTRIES_TABLE, MetaData.ENTRIES_ALL_COLUMNS, null,
                null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Entry entry = Entry.from(cursor);
                entries.add(entry);
                cursor.moveToNext();
            }
            cursor.close();
        }
        DataSet dataSet = new DataSet(entries);
        return dataSet;
    }

    private ContentValues makeContentValues(Entry entry) {
        ContentValues values = new ContentValues();
        values.put(MetaData.ENTRIES_GROUP, entry.getGroupName());
        values.put(MetaData.ENTRIES_ENTRY, entry.getEntryName());
        values.put(MetaData.ENTRIES_CONTENT, entry.getContent());
        return values;
    }

}
