package dao;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface PreSQL {

	/**
	 * 执行更新
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public PreSQL update(Object... params) throws SQLException;

	/**
	 * 执行查询
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public ResultSet query(Object... params) throws SQLException;

}
