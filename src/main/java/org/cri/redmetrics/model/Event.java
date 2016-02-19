package org.cri.redmetrics.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.cri.redmetrics.db.IntegerArrayPersister;

@Data
@DatabaseTable(tableName = "events")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Event extends ProgressData {

    @DatabaseField(persisterClass = IntegerArrayPersister.class, columnDefinition = "integer[]")
    private Integer[] coordinates;

}
