package org.cri.redmetrics.json;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.cri.redmetrics.model.Snapshot;

public class SnapshotJsonConverter extends ProgressDataJsonConverter<Snapshot> {

    @Inject
    SnapshotJsonConverter(@Named("ProgressData") Gson gson, JsonParser jsonParser) {
        super(Snapshot.class, gson, jsonParser);
    }

}
