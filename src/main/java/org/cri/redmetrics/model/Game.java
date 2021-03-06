package org.cri.redmetrics.model;


import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@DatabaseTable(tableName = "games")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Game extends Entity {

    @DatabaseField(dataType = DataType.UUID)
    private UUID adminKey;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField
    private String author;

    @DatabaseField
    private String description;

}