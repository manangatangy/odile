package com.wolfie.odile.model.database;

public class MetaData {

    public static final String DATABASE_NAME = "eskey.db";
    public static final int DATABASE_VERSION = 1;

    public static final String MASTER_TABLE = "master";
    public static final String MASTER_SALT = "salt";
    public static final String MASTER_KEY = "master_key";
    public static final String[] MASTER_ALL_COLUMNS = {
            MASTER_SALT, MASTER_KEY
    };

    public static final String ENTRIES_TABLE = "entries";
    public static final String ENTRIES_ID = "_id";
    public static final String ENTRIES_GROUP = "group_name";
    public static final String ENTRIES_ENTRY = "entry_name";
    public static final String ENTRIES_CONTENT = "content";
    public static final String[] ENTRIES_ALL_COLUMNS = {
            ENTRIES_ID, ENTRIES_GROUP, ENTRIES_ENTRY, ENTRIES_CONTENT
    };

}
