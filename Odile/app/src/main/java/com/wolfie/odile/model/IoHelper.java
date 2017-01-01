package com.wolfie.odile.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;

import java.io.InputStreamReader;
import java.util.List;

/**
 * Support for passing data between the database form and the file form.
 * This class also is the structure used for json serialisation.
 */
public class IoHelper {

    @Expose
    private List<Phrase> phrases;

    public IoHelper() {
        // No arg ctor for deserialiser.
    }

    public String export(List<Phrase> phrases) {
        this.phrases = phrases;
        DataSet.sort(this.phrases);
        Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

    public List<Phrase> inport(InputStreamReader isr) throws JsonSyntaxException, JsonIOException {
        Gson gson = new Gson();
        IoHelper ioHelper = gson.fromJson(isr, IoHelper.class);
        return ioHelper.phrases;
    }

    public List<Phrase> getPhrases() {
        return phrases;
    }

}
