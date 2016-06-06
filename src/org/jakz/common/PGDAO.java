package org.jakz.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.concurrent.locks.ReentrantReadWriteLock;



public class PGDAO 
{
	protected final DateTimeFormatter formatPGDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	protected Connection c;
	
	public PGDAO(String target, String username, String password) throws ClassNotFoundException, SQLException
	{
		c=null;
		Class.forName("org.postgresql.Driver");
		connect(target,username,password);
	}
	
	public void connect(String target, String username, String password) throws SQLException
	{
		c = DriverManager.getConnection(target, username, password);
		c.setAutoCommit(false);
	}
	
	
	
	public void begin() throws SQLException
	{
		Statement q = c.createStatement();
		q.execute("START TRANSACTION;");
		q.close();
	}
	
	public void startTransaction() throws SQLException
	{
		begin();
	}
	
	public void commit() throws SQLException
	{
		if(c.getAutoCommit())
			c.commit();
		else
		{
			Statement q = c.createStatement();
			q.execute("COMMIT;");
			q.close();
		}
	}
	
	public void rollback() throws SQLException
	{
		if(c.getAutoCommit())
			c.rollback();
		else
		{
			Statement q = c.createStatement();
			q.execute("ROLLBACK;");
			q.close();
		}
	}
	
	public Connection getConnection()
	{
		return c;
	}
}
