package com.wolfie.odile.model;

import android.database.Cursor;

import com.google.gson.annotations.Expose;
import com.wolfie.odile.model.database.MetaData;

/**
 * The primary data class, held on the database and represented in the ListFragment.
 * Only the mId field is unique.  There are no relationships between any other fields.
 */
public class Phrase {

    private int mId = -1;
    @Expose
    private String mGroup;
    @Expose
    private String mRussian;
    @Expose
    private String mEnglish;
    @Expose
    private String mTranslit;
    @Expose
    private String mPath;

    public Phrase() {
    }

    private Phrase(int id, String group, String russian, String english, String translit, String path) {
        mId = id;
        mGroup = group;
        mRussian = russian;
        mEnglish = english;
        mTranslit = translit;
        mPath = path;
    }

    public boolean isNew() {
        return mId == -1;
    }

    public static Phrase create(String group, String russian, String english, String translit, String path) {
        Phrase phrase = new Phrase(-1, group, russian, english, translit, path);
        return phrase;
    }

    public static Phrase from(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(MetaData.PHRASE_ID));
        String group = cursor.getString(cursor.getColumnIndex(MetaData.PHRASE_GROUP));
        String russian = cursor.getString(cursor.getColumnIndex(MetaData.PHRASE_RUSSIAN));
        String english = cursor.getString(cursor.getColumnIndex(MetaData.PHRASE_ENGLISH));
        String translit = cursor.getString(cursor.getColumnIndex(MetaData.PHRASE_TRANSLIT));
        String path = cursor.getString(cursor.getColumnIndex(MetaData.PHRASE_PATH));
        Phrase phrase = new Phrase(id, group, russian, english, translit, path);
        return phrase;
    }

    public int getId() {
        return mId;
    }

    public String getGroup() {
        return mGroup;
    }

    public void setGroup(String group) {
        this.mGroup = group;
    }

    public String getRussian() {
        return mRussian;
    }

    public void setRussian(String russian) {
        this.mRussian = russian;
    }

    public String getEnglish() {
        return mEnglish;
    }

    public void setEnglish(String english) {
        this.mEnglish = english;
    }

    public String getTranslit() {
        return mTranslit;
    }

    public void setTranslit(String translit) {
        this.mTranslit = translit;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        this.mPath = path;
    }
}
