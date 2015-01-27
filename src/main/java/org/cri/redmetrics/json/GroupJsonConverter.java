
package org.cri.redmetrics.json;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import org.cri.redmetrics.model.Group;
import java.util.ArrayList;

public class GroupJsonConverter extends EntityJsonConverter<Group>{
    
    @Inject
    GroupJsonConverter(Gson gson, JsonParser jsonParser) {
        super(Group.class, Group[].class, gson, jsonParser);
    }
}
