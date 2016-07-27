package org.jakz.common;

import java.sql.SQLException;
import java.sql.Statement;

public class PGDAO extends DAO
{
	
	public PGDAO(String target, String username, String password, boolean autoCommit) throws ClassNotFoundException, SQLException
	{
		Class.forName("org.postgresql.Driver");
		connect(target,username,password,autoCommit);
	}
	
	public PGDAO(String target, String username, String password) throws ClassNotFoundException, SQLException
	{
		Class.forName("org.postgresql.Driver");
		connect(target,username,password,false);
	}

	@Override
	public void startTransaction() throws SQLException 
	{
		Statement q = c.createStatement();
		q.execute("START TRANSACTION;");
		q.close();
	}
	
	@Override
	public void commit() throws SQLException
	{
		if(!c.getAutoCommit())
			c.commit();
		else
		{
			Statement q = c.createStatement();
			q.execute("COMMIT;");
			q.close();
		}
	}
	
	@Override
	public void rollback() throws SQLException
	{
		if(!c.getAutoCommit())
			c.rollback();
		else
		{
			Statement q = c.createStatement();
			q.execute("ROLLBACK;");
			q.close();
		}
	}
	
}
