package example.webapp.dao.interfaces;

import java.io.Serializable;
import java.util.List;

import example.webapp.entity.DomainObject;

/**
 * @author Richard Wilkinson - richard.wilkinson@jweekend.com
 * 
 */
public interface Dao<T extends DomainObject> {
	
	public void delete(T o);

	public T load(Serializable id);

	public T save(T o);

	public List<T> findAll();

	public int countAll();
}
