package com.sibvisions.rjdbc;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

public class RemoteSecurityWrapper 
{
	public static byte[] createConnectionKey()
	{
		return RemoteSecurity.createConnectionKey();
	}
	
	public static byte[] encryptHandshake(PublicKey pPublicKey, String pToken, byte[] pConnectionKey, byte[] pPayload) throws GeneralSecurityException, 
    																												   	      IOException
	{
		return RemoteSecurity.encryptHandshake(pPublicKey, pToken, pConnectionKey, pPayload);
	}
}
