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
	
	@Override
	public void startTransaction() throws SQLException
	{
		Statement q = c.createStatement();
		q.execute("BEGIN TRANSACTION");
		q.close();
	}
	
	
	@Override
	public void commit() throws SQLException
	{
		Statement q = c.createStatement();
		q.execute("COMMIT");
		q.close();	
	}
	
	@Override
	public void rollback() throws SQLException
	{
		Statement q = c.createStatement();
		q.execute("ROLLBACK");
		q.close();
	}
	
}
