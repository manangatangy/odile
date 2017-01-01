package com.wolfie.odile.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to organise the entries, prior to displaying them.  Typically all
 * the records in mEntries have the same groupName (which is held in mHeading).
 * All the entries in mEntries belong to the same group (named as the heading).
 */
public class EntryGroup {
    private String mHeading;
    private List<Entry> mEntries;

    public EntryGroup(String heading, List<Entry> entries) {
        mHeading = heading;
        mEntries = entries;
    }

    public String getHeading() {
        return mHeading;
    }
    public List<Entry> getEntries() {
        return mEntries;
    }

    /**
     * Build a list of EntryGroups from the DataSet.  If the groupName is non-null, then return
     * only a list of only one EntryGroup, whose heading matches the specified heading.
     * Assumes the Entries in the DataSet are ordered by group name.
     */
    public static List<EntryGroup> buildGroups(String heading, DataSet dataSet) {
        List<EntryGroup> groups = new ArrayList<>();
        String currentGroupName = null;
        List<Entry> currentEntries = null;
        for (Entry entry : dataSet.getEntries()) {
            Log.d("eskey", "EntryGroup.buildGroups(): group:" + entry.getGroupName() + ", name=" + entry.getEntryName());
            if (!entry.getGroupName().equals(currentGroupName)) {
                // This entry is in a different group to the previous one, close
                // off the current list (if one has been started).
                if (currentEntries != null && currentGroupName != null) {
                    EntryGroup group = new EntryGroup(currentGroupName, currentEntries);
                    groups.add(group);
                    currentEntries = null;
                    currentGroupName = null;
                }
            }
            boolean mustCollect = (heading == null || entry.getGroupName().equals(heading));
            if  (mustCollect) {
                if  (currentEntries == null) {
                    // This group name must be collected and we have not yet started a
                    // group for, so start a new current group for it.
                    currentEntries = new ArrayList<>();
                    currentGroupName = entry.getGroupName();
                }
                currentEntries.add(entry);
            }
        }
        // If there is a current group being collected, close it off and add it in.
        if (currentEntries != null && currentGroupName != null) {
            EntryGroup group = new EntryGroup(currentGroupName, currentEntries);
            groups.add(group);
        }
        return groups;
    }

    /**
     * Build a list of one EntryGroup (with the specified heading) from the DataSet.
     * The single EntryGroup contains all the Entries from the DataSet, ordered.
     * As a convenience, a null DataSet parameter means create an empty dataSet in the target.
     */
    public static List<EntryGroup> buildSingleGroup(String heading, DataSet dataSet) {
        List<Entry> entries = new ArrayList<>();
        if (dataSet != null) {
            for (Entry entry : dataSet.getEntries()) {
                entries.add(entry);
            }
            DataSet.sortOnName(entries);          // Do the sort, based only on the entry.name
        }
        EntryGroup group = new EntryGroup(heading, entries);
        List<EntryGroup> groups = new ArrayList<>();
        groups.add(group);
        return groups;
    }

    public static List<String> buildHeadingsList(DataSet dataSet) {
        List<String> headings = new ArrayList<>();
        String currentGroupName = null;
        for (Entry entry : dataSet.getEntries()) {
            if (!entry.getGroupName().equals(currentGroupName)) {
                // This entry has a different group to the previous one, add to the list
                currentGroupName = entry.getGroupName();
                headings.add(currentGroupName);
            }
        }
        return headings;
    }
}
