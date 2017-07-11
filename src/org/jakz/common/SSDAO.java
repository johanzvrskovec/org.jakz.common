package org.jakz.common;

import java.sql.SQLException;
import java.sql.Statement;

public class SSDAO extends DAO
{
	
	public SSDAO(String connectionUrl) throws ClassNotFoundException, SQLException
	{
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		connect(connectionUrl,true); //autocommit false does not work
	}
	
	/**
	 * Start a transaction by setting auto commit to false in SQL Server
	 */
	@Override
	public void startTransaction() throws SQLException
	{
		c.setAutoCommit(false);
	}
	
	/**
	 * Commits a transaction by using the jdbc commit() method. Also resets SQL Server auto commit to true.
	 */
	@Override
	public void commit() throws SQLException
	{
		c.commit();
		c.setAutoCommit(true);
	}
	
	/**
	 * Rollbacks the transaction by using the jdbc rollback() method. Also resets SQL server auto commit to true.
	 */
	@Override
	public void rollback() throws SQLException
	{
		c.rollback();
		c.setAutoCommit(true);
	}
	
}
