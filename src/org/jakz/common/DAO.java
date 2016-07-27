package org.jakz.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;



public abstract class DAO 
{
	protected final DateTimeFormatter formatPGDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	protected Connection c = null;
	
	public void connect(String target, String username, String password, boolean autoCommit) throws SQLException
	{
		c = DriverManager.getConnection(target, username, password);
		c.setAutoCommit(autoCommit);
	}
	
	public void connect(String connectionUrl, boolean autoCommit) throws SQLException
	{
		c = DriverManager.getConnection(connectionUrl);
		c.setAutoCommit(autoCommit);
	}
	
	public void begin() throws SQLException
	{
		beginTransaction();
	}
	
	public void start() throws SQLException
	{
		startTransaction();
	}
	
	public void beginTransaction() throws SQLException
	{
		startTransaction();
	}
	
	public abstract void startTransaction() throws SQLException;
	
	public void commit() throws SQLException
	{
		c.commit();
	}
	
	public void rollback() throws SQLException
	{
		c.rollback();
	}
	
	public Connection getConnection()
	{
		return c;
	}
}
