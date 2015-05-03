package org.cri.redmetrics.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.cri.redmetrics.model.Entity;
import org.cri.redmetrics.model.ResultsPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public abstract class EntityDao<E extends Entity> {

    protected static final Logger logger = LoggerFactory.getLogger(EntityDao.class);

    protected final Dao<E, UUID> orm;

    EntityDao(ConnectionSource connectionSource, Class<E> type) throws SQLException {
        this.orm = DaoManager.createDao(connectionSource, type);
        try {
            TableUtils.createTableIfNotExists(connectionSource, type);
        } catch (SQLException e) {
            logger.info(e.getMessage());
        }
    }

    public E create(E entity) {
        try {
            orm.create(entity);
            return entity;
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public E read(UUID id) {
        try {
            return orm.queryForId(id);
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public E update(E entity) {
        try {
            orm.update(entity);
            return read(entity.getId());
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public E delete(UUID id) {
        try {
            E entity = read(id);
            orm.delete(entity);
            return entity;
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public List<E> listAll() {
        try {
            return orm.queryForAll();
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public ResultsPage<E> list(long page, long perPage) {
        try {
            List<E> results = orm.queryBuilder().offset((page - 1) * perPage).limit(perPage).query();
            return new ResultsPage<E>(countAllEntities(), page, perPage, results);
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public long countAllEntities() {
        try {
            return orm.countOf();
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public void refresh(E entity) {
        try {
            orm.refresh(entity);
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }
}
