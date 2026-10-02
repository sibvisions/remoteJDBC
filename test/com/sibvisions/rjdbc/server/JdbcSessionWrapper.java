package com.sibvisions.rjdbc.server;

public class JdbcSessionWrapper 
{
	public static void clearConnectionKey(JdbcSession pSession)
	{
		pSession.close();
	}
	
}
