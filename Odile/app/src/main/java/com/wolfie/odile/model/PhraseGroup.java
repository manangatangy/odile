package com.wolfie.odile.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to organise the phrases, prior to displaying them.  Typically all
 * the records in mPhrases have the same group (which is held in mHeading).
 */
public class PhraseGroup {

    private String mHeading;
    private List<Phrase> mPhrases;

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
     * Build a list of PhraseGroups from the DataSet.  If the heading is non-null, then return
     * only a list of only one PhraseGroup, whose heading matches the specified heading.
     * Assumes the Phrases in the DataSet are ordered by group name.
     */
    public static List<PhraseGroup> buildGroups(String heading, DataSet dataSet) {
        List<PhraseGroup> groups = new ArrayList<>();
        String currentGroup = null;
        List<Phrase> currentPhrases = null;
        for (Phrase entry : dataSet.getPhrases()) {
            Log.d("odile", "PhraseGroup.buildGroups(): group:" + entry.getGroup() + ", name=" + entry.getId());
            if (!entry.getGroup().equals(currentGroup)) {
                // This entry is in a different group to the previous one, close
                // off the current list (if one has been started).
                if (currentPhrases != null && currentGroup != null) {
                    PhraseGroup group = new PhraseGroup(currentGroup, currentPhrases);
                    groups.add(group);
                    currentPhrases = null;
                    currentGroup = null;
                }
            }
            boolean mustCollect = (heading == null || entry.getGroup().equals(heading));
            if  (mustCollect) {
                if  (currentPhrases == null) {
                    // This group name must be collected and we have not yet started a
                    // group for, so start a new current group for it.
                    currentPhrases = new ArrayList<>();
                    currentGroup = entry.getGroup();
                }
                currentPhrases.add(entry);
            }
        }
        // If there is a current group being collected, close it off and add it in.
        if (currentPhrases != null && currentGroup != null) {
            PhraseGroup group = new PhraseGroup(currentGroup, currentPhrases);
            groups.add(group);
        }
        return groups;
    }

    /**
     * Build a list of one PhraseGroup (with the specified heading) from the DataSet.
     * The single PhraseGroup contains all the Phrases from the DataSet, ordered.
     * As a convenience, a null DataSet parameter means create an empty dataSet in the target.
     */
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

}
