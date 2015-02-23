package org.cri.redmetrics.json;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import spark.ResponseTransformer;

/**
 * Created by himmelattack on 23/02/15.
 */
public class ApplicationErrorJsonConverter implements ResponseTransformer {

    Gson gson;

    @Inject
    public ApplicationErrorJsonConverter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }
}
