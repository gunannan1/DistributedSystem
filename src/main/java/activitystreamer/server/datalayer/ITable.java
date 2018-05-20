package activitystreamer.server.datalayer;

import java.util.HashMap;

/**
 * ITable
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public interface ITable<T> {

	boolean lockRow(T row);
	boolean unlockRow(T row);
	boolean insert(T row);
	T updateOrInsert(T row);
	boolean delete(String id);
	T selectById(String id);
	HashMap<String, T> getAll();

}
