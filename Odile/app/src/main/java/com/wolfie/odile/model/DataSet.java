package com.wolfie.odile.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This is a collection of Phrases passed between the database and the UI.
 */
public class DataSet {

    private List<Phrase> mPhrases = new ArrayList<>();

    public List<Phrase> getPhrases() {
        return mPhrases;
    }

    public DataSet(List<Phrase> phrases) {
        this.mPhrases = phrases;
    }

    /**
     * @param phrases will be sorted on the group string and then the id integer, so
     *                that Phrases in the same group are sorted on entry order.
     */
    public static void sort(List<Phrase> phrases) {
        Collections.sort(phrases, new Comparator<Phrase>() {
            /**
             * @return an integer < 0 if {@code lhs} is less than {@code rhs}, 0 if they are
             *         equal, and > 0 if {@code lhs} is greater than {@code rhs}.
             */
            @Override
            public int compare(Phrase lhs, Phrase rhs) {
                int compare = lhs.getGroup().compareToIgnoreCase(rhs.getGroup());
                if (compare == 0) {
                    compare = Integer.compare(lhs.getId(), rhs.getId());
                }
                return compare;
            }
        });
    }

    public static void sortOnId(List<Phrase> phrases) {
        Collections.sort(phrases, new Comparator<Phrase>() {
            @Override
            public int compare(Phrase lhs, Phrase rhs) {
                // the value 0 if x == y; a value less than 0 if x < y; and a value greater than 0 if x > y
                return Integer.compare(lhs.getId(), rhs.getId());
            }
        });
    }
}
