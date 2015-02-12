
package org.cri.redmetrics.controller;

import com.google.inject.Inject;
import org.cri.redmetrics.csv.CsvEntityConverter;
import org.cri.redmetrics.dao.GroupDao;
import org.cri.redmetrics.json.GroupJsonConverter;
import org.cri.redmetrics.model.Group;

public class GroupController extends Controller<Group, GroupDao>{
    
    @Inject
    GroupController(GroupDao dao, GroupJsonConverter jsonConverter, CsvEntityConverter<Group> csvEntityConverter) {
        super("group", dao, jsonConverter, csvEntityConverter);
    }
    
}
