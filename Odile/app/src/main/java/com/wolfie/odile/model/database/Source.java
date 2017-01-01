package com.wolfie.odile.model.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.wolfie.odile.model.Phrase;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a database to provide the basic CRUD operations for the spends table.
 * These methods are best called in a background thread, using a loader.
 */
public class Source {

    private SQLiteDatabase mDatabase;

    public Source(SQLiteDatabase database) {
        mDatabase = database;
    }

    public boolean insert(Phrase phrase) {
        long result = mDatabase.insert(MetaData.PHRASE_TABLE, null, makeContentValues(phrase));
        return result != -1;
    }

    public boolean update(Phrase phrase) {
        int result = mDatabase.update(MetaData.PHRASE_TABLE, makeContentValues(phrase),
                MetaData.PHRASE_ID + "=" + phrase.getId(), null);
        return result != 0;
    }

    public boolean delete(Phrase phrase) {
        int result =  mDatabase.delete(MetaData.PHRASE_TABLE, MetaData.PHRASE_ID + "=" + phrase.getId(), null);
        return result != 0;
    }

    public void deleteAll() {
        mDatabase.delete(MetaData.PHRASE_TABLE, null, null);
    }

    public @NonNull List<Phrase> read() {
        List<Phrase> spends = new ArrayList<>();
        Cursor cursor = mDatabase.query(MetaData.PHRASE_TABLE, MetaData.PHRASE_ALL_COLUMNS,
                null, null, null, null, MetaData.PHRASE_ID);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Phrase phrase = Phrase.from(cursor);
                spends.add(phrase);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return spends;
    }

    private ContentValues makeContentValues(Phrase phrase) {
        ContentValues values = new ContentValues();
        values.put(MetaData.PHRASE_GROUP, phrase.getGroup());
        values.put(MetaData.PHRASE_RUSSIAN, phrase.getRussian());
        values.put(MetaData.PHRASE_ENGLISH, phrase.getEnglish());
        values.put(MetaData.PHRASE_TRANSLIT, phrase.getTranslit());
        values.put(MetaData.PHRASE_PATH, phrase.getPath());
        return values;
    }

}
