package com.wolfie.odile.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to organise the phrases, prior to displaying them.  Typically all
 * the records in mPhrases have the same group (which is held in mHeading).
 */
public class PhraseGroup implements Parcelable {

    private String mHeading;
    private List<Phrase> mPhrases;

    private PhraseGroup() {
        mPhrases = new ArrayList<>();
    }

    public PhraseGroup(Parcel in) {
        this();
        read(in);
    }

    private void read(Parcel in) {
        mHeading = in.readString();
        mPhrases.clear();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            Phrase phrase = new Phrase(in);
            mPhrases.add(phrase);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<PhraseGroup> CREATOR =
            new Parcelable.Creator<PhraseGroup>() {
                public PhraseGroup createFromParcel(Parcel in) {
                    return new PhraseGroup(in);
                }

                public PhraseGroup[] newArray(int size) {
                    return new PhraseGroup[size];
                }
            };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mHeading);
        int size = mPhrases.size();
        dest.writeInt(size);
        for (int i = 0; i < size; i++) {
            Phrase phrase = mPhrases.get(i);
            phrase.writeToParcel(dest, flags);
        }
    }

    public PhraseGroup(String heading, List<Phrase> phrases) {
        mHeading = heading;
        mPhrases = phrases;
    }

    public String getHeading() {
        return mHeading;
    }
    public List<Phrase> getPhrases() {
        return mPhrases;
    }

    /**
     * Build a list of PhraseGroups from the DataSet.  If the heading is non-null, then return`
     * only a list of only one PhraseGroup, whose heading matches the specified heading.
     * Assumes the Phrases in the DataSet are ordered by group name.
     */
    @NonNull
    public static List<PhraseGroup> buildGroups(String heading, DataSet dataSet) {
        int totalPhrases = 0;
        List<PhraseGroup> groups = new ArrayList<>();
        String currentGroup = null;
        List<Phrase> currentPhrases = null;
        for (Phrase phrase : dataSet.getPhrases()) {
            if (!phrase.getGroup().equals(currentGroup)) {
                // This entry is in a different group to the previous one, close
                // off the current list (if one has been started).
                if (currentPhrases != null && currentGroup != null) {
                    PhraseGroup group = new PhraseGroup(currentGroup, currentPhrases);
                    groups.add(group);
                    currentPhrases = null;
                    currentGroup = null;
                    int p = group.getPhrases().size();
                    Log.d("PhraseGroup", "added group(" + group.getHeading() + ") with " + p + " phrases");
                    totalPhrases += p;
                }
            }
            boolean mustCollect = (heading == null || phrase.getGroup().equals(heading));
            if  (mustCollect) {
                if  (currentPhrases == null) {
                    // This group name must be collected and we have not yet started a
                    // group for, so start a new current group for it.
                    currentPhrases = new ArrayList<>();
                    currentGroup = phrase.getGroup();
                }
                currentPhrases.add(phrase);
            }
        }
        // If there is a current group being collected, close it off and add it in.
        if (currentPhrases != null && currentGroup != null) {
            PhraseGroup group = new PhraseGroup(currentGroup, currentPhrases);
            groups.add(group);
            int p = group.getPhrases().size();
            Log.d("PhraseGroup", "added group(" + group.getHeading() + ") with " + p + " phrases");
            totalPhrases += p;
        }
        Log.d("PhraseGroup", "total phrases: " + totalPhrases);
        return groups;
    }

    /**
     * Build a list of one PhraseGroup (with the specified heading) from the DataSet.
     * The single PhraseGroup contains all the Phrases from the DataSet, ordered.
     * As a convenience, a null DataSet parameter means create an empty dataSet in the target.
     */
    @NonNull
    public static List<PhraseGroup> buildSingleGroup(String heading, DataSet dataSet) {
        List<Phrase> phrases = new ArrayList<>();
        if (dataSet != null) {
            for (Phrase entry : dataSet.getPhrases()) {
                phrases.add(entry);
            }
            DataSet.sortOnId(phrases);          // Do the sort, based only on the entry order
        }
        PhraseGroup group = new PhraseGroup(heading, phrases);
        List<PhraseGroup> groups = new ArrayList<>();
        groups.add(group);
        return groups;
    }

    @NonNull
    public static List<String> buildHeadingsList(DataSet dataSet) {
        List<String> headings = new ArrayList<>();
        String currentGroup = null;
        for (Phrase entry : dataSet.getPhrases()) {
            if (!entry.getGroup().equals(currentGroup)) {
                // This entry has a different group to the previous one, add to the list
                currentGroup = entry.getGroup();
                headings.add(currentGroup);
            }
        }
        return headings;
    }

    /**
     * @return a list of all the Phrases in all of the PhraseGroups (which may be null).
     */
    @NonNull
    public static List<Phrase> getAllPhrases(@Nullable List<PhraseGroup> phraseGroups) {
        List<Phrase> phrases = new ArrayList<>();
        if (phraseGroups != null) {
            for (PhraseGroup phraseGroup : phraseGroups) {
                for (Phrase phrase : phraseGroup.getPhrases()) {
                    phrases.add(phrase);
                }
            }
        }
        return phrases;     // May be empty, but never null.
    }

}
