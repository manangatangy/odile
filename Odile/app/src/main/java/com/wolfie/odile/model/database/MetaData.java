package com.wolfie.odile.model.database;

public class MetaData {

    public static final String DATABASE_NAME = "odile.db";
    public static final int DATABASE_VERSION = 1;

    public static final String PHRASE_TABLE = "phrases";
    public static final String PHRASE_ID = "_id";
    public static final String PHRASE_GROUP = "category";
    public static final String PHRASE_RUSSIAN = "russian";
    public static final String PHRASE_ENGLISH = "english";
    public static final String PHRASE_TRANSLIT = "translit";
    public static final String PHRASE_PATH= "path";
    public static final String[] PHRASE_ALL_COLUMNS = {
            PHRASE_ID, PHRASE_GROUP, PHRASE_RUSSIAN, PHRASE_ENGLISH, PHRASE_TRANSLIT, PHRASE_PATH
    };

}
