package com.wolfie.odile.model;

import android.database.Cursor;

import com.google.gson.annotations.Expose;
import com.wolfie.odile.model.database.MetaData;

/**
 * The primary data class, held on the database and represented in the ListFragment.
 * Only the mId field is unique.  There are no relationships between the entry-name,
 * group-name and any other field.
 */
public class Entry {
    private int mId = -1;
    @Expose
    private String mEntryName;
    @Expose
    private String mGroupName;
    @Expose
    private String mContent;

    public Entry() {
    }

    private Entry(int id, String entryName, String groupName, String content) {
        mId = id;
        mEntryName = entryName;
        mGroupName = groupName;
        mContent = content;
    }

    public boolean isNew() {
        return mId == -1;
    }

    public static Entry create(String entryName, String groupName, String content) {
        Entry entry = new Entry(-1, entryName, groupName, content);
        return entry;
    }

    public static Entry from(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(MetaData.ENTRIES_ID));
        String groupName = cursor.getString(cursor.getColumnIndex(MetaData.ENTRIES_GROUP));
        String entryName = cursor.getString(cursor.getColumnIndex(MetaData.ENTRIES_ENTRY));
        String content = cursor.getString(cursor.getColumnIndex(MetaData.ENTRIES_CONTENT));
        Entry entry = new Entry(id, entryName, groupName, content);
        return entry;
    }

    public int getId() {
        return mId;
    }

    public String getEntryName() {
        return mEntryName;
    }

    public void setEntryName(String entryName) {
        this.mEntryName = entryName;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupName(String groupName) {
        this.mGroupName = groupName;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }
}
