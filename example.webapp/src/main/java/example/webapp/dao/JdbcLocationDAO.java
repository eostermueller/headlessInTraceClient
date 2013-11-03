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

import example.webapp.dao.interfaces.LocationDao;
import example.webapp.entity.Location;

/**
 * @author Richard Wilkinson - richard.wilkinson@jweekend.com
 * 
 */
@Transactional
public class JdbcLocationDAO extends SimpleJdbcDaoSupport implements LocationDao {

	private SimpleJdbcInsert insertLocation;

	private static class LocationMapper implements RowMapper<Location> {
		public Location mapRow(ResultSet rs, int rowNum) throws SQLException {
			Location location = new Location();
			location.setId(rs.getLong("id"));
			location.setName(rs.getString("name"));
			location.setAddress(rs.getString("address"));
			return location;
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
		insertLocation = new SimpleJdbcInsert(getJdbcTemplate()).withTableName("Location").usingGeneratedKeyColumns("id").usingColumns("name", "address");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see example.webapp.dao.interfaces.Dao#countAll()
	 */
	public int countAll() {
		return getJdbcTemplate().queryForInt("select count(*) from Location");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * example.webapp.dao.interfaces.Dao#delete(example.webapp.entity.DomainObject)
	 */
	public void delete(Location o) {
		getSimpleJdbcTemplate().update("delete from Location where id = ?", o.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see example.webapp.dao.interfaces.Dao#findAll()
	 */
	public List<Location> findAll() {
		final String sql = "select * from Location";
		return getSimpleJdbcTemplate().query(sql, new LocationMapper());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see example.webapp.dao.interfaces.Dao#load(java.io.Serializable)
	 */
	public Location load(Serializable id) {
		String sql = "select * from Location where id = ?";
		return getSimpleJdbcTemplate().queryForObject(sql, new LocationMapper(), id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * example.webapp.dao.interfaces.Dao#save(example.webapp.entity.DomainObject)
	 */
	public Location save(Location o) {
		final String update = "update Location set name = ?, address = ?";
		if (o.getId() != null) {
			// this is an update
			getSimpleJdbcTemplate().update(update, o.getName(), o.getAddress());
		}
		else {
			// this is an insert
			Map<String, Object> args = new HashMap<String, Object>(2);
			args.put("name", o.getName());
			args.put("address", o.getAddress());
			Number id = insertLocation.executeAndReturnKey(args);
			o.setId(id.longValue());
		}
		return o;
	}

}
