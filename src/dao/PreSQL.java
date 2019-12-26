package dao;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface PreSQL {

	/**
	 * ִ�и���
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public PreSQL update(Object... params) throws SQLException;

	/**
	 * ִ�в�ѯ
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public ResultSet query(Object... params) throws SQLException;

}
