package com.wolfie.odile.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.wolfie.odile.model.database.Helper;
import com.wolfie.odile.model.database.Source;
import com.wolfie.odile.model.loader.EntryLoader;
import com.wolfie.odile.model.loader.IoLoader;
import com.wolfie.odile.view.BaseUi;

/**
 * The MainPresenter doesn't use a gui, so the BaseUi parameter to the ctor can be null.
 * It extends BasePresenter simply so that it can be returned by BaseFragment.findPresenter.
 */
public class MainPresenter extends BasePresenter<BaseUi> {

    private Helper mHelper;
    private SQLiteDatabase mDatabase;
    private Source mSource;

    private EntryLoader mEntryLoader;

    // This presenter needs no ui (all the ui is performed by the other frags)
    public MainPresenter(BaseUi baseUi, Context context) {
        super(baseUi);

        mHelper = new Helper(context);
        mDatabase = mHelper.getWritableDatabase();

        mSource = new Source(mDatabase);

        mEntryLoader = new EntryLoader(context, mSource);
    }

    public EntryLoader getEntryLoader() {
        return mEntryLoader;
    }

}
