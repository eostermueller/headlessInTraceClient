package example.webapp.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import example.webapp.dao.interfaces.EventDao;
import example.webapp.dao.interfaces.LocationDao;
import example.webapp.entity.Event;

/**
 * @author Richard Wilkinson - richard.wilkinson@jweekend.com
 * 
 */
@Transactional
public class JdbcEventDAO extends SimpleJdbcDaoSupport implements EventDao {

	private LocationDao locationDAO;

	private SimpleJdbcInsert insertEvent;

	private class EventMapper implements RowMapper<Event> {
		public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
			Event event = new Event();
			event.setId(rs.getLong("id"));
			event.setName(rs.getString("name"));
			event.setDescription(rs.getString("description"));
			event.setDate(rs.getTimestamp("date"));
			Object location = rs.getObject("location");
			if (location != null) {
				event.setLocation(locationDAO.load((Serializable) location));
			}
			return event;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport#initTemplateConfig
	 * ()
	 */
	@Override
	protected void initTemplateConfig() {
		super.initTemplateConfig();
		insertEvent = new SimpleJdbcInsert(getJdbcTemplate()).withTableName("Event").usingGeneratedKeyColumns("id").usingColumns("name", "description", "date", "location");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see example.webapp.dao.interfaces.Dao#countAll()
	 */
	public int countAll() {
		return getJdbcTemplate().queryForInt("select count(*) from Event");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * example.webapp.dao.interfaces.Dao#delete(example.webapp.entity.DomainObject)
	 */
	public void delete(Event o) {
		getSimpleJdbcTemplate().update("delete from Event where id = ?", o.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see example.webapp.dao.interfaces.Dao#findAll()
	 */
	public List<Event> findAll() {
		final String sql = "select * from Event";
		return getSimpleJdbcTemplate().query(sql, new EventMapper());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see example.webapp.dao.interfaces.Dao#load(java.io.Serializable)
	 */
	public Event load(Serializable id) {
		String sql = "select * from Event where id = ?";
		return getSimpleJdbcTemplate().queryForObject(sql, new EventMapper(), id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * example.webapp.dao.interfaces.Dao#save(example.webapp.entity.DomainObject)
	 */
	public Event save(Event o) {
		final String update = "update Event set name = ?, description = ?, date = ?, location = ?";

		if (o.getLocation() != null) {
			// update location
			o.setLocation(locationDAO.save(o.getLocation()));
		}

		if (o.getId() != null) {
			// this is an update
			getSimpleJdbcTemplate().update(update, o.getName(), o.getDescription(), o.getDate(), o.getLocation() != null ? o.getLocation().getId() : null);
		}
		else {
			// this is an insert
			Map<String, Object> args = new HashMap<String, Object>(4);
			args.put("name", o.getName());
			args.put("description", o.getDescription());
			args.put("date", o.getDate());
			args.put("location", o.getLocation() != null ? o.getLocation().getId() : null);
			Number id = insertEvent.executeAndReturnKey(args);
			o.setId(id.longValue());
		}
		return o;
	}

	public void setLocationDAO(LocationDao locationDAO) {
		this.locationDAO = locationDAO;
	}

}
