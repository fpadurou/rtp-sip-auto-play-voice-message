package com.ericsson.sip.listener;

import javax.servlet.sip.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sqlmanager.*;

public class SASListener implements SipApplicationSessionListener 
{
	private Logger logger = Logger.getLogger("CallSetup");
	
	public void sessionCreated(SipApplicationSessionEvent ev)
	{
		try
		{
			SipApplicationSession sas = ev.getApplicationSession();
			
			if(ev != null && sas != null)
			{
				logger.log(Level.INFO, "SASListener: sessionCreated " + sas.getId());
			}
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "SASListener: sessionCreated " + e);
		}
	}

	public void sessionDestroyed(SipApplicationSessionEvent ev)
	{
		// --- look in database for user sip address
		try
		{
			SipApplicationSession sas = ev.getApplicationSession();
			String sessionId = sas.getId();
			
			logger.log(Level.INFO, "SASListener: sessionDestroyed : " + sessionId);
			
			SqlManager.removeCallSession(sessionId);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "SASListener: sessionDestroyed error: " + e.getMessage());
		}
	}
	
	public void sessionExpired(SipApplicationSessionEvent ev)
	{		
		SipApplicationSession sas = ev.getApplicationSession();
		if(sas!= null)
		{
			if(sas.isValid() && sas.getAttribute("CallSession") != null)
			{
				logger.log(Level.INFO, "SASListener: extra time for sessionExpired " + sas.getId());
				sas.setExpires(3);
			}
			else
				logger.log(Level.INFO, "SASListener: sessionExpired " + sas.getId());
		}
		else
		{
			logger.log(Level.INFO, "SASListener: sessionExpired null");
		}
	}
	
	public void sessionReadyToInvalidate(SipApplicationSessionEvent ev)
	{
		if(ev != null && ev.getApplicationSession() != null)
		{
			logger.log(Level.INFO, "SASListener: sessionReadyToInvalidate " + ev.getApplicationSession().getId());
		}
		else
		{
			logger.log(Level.INFO, "SASListener: sessionReadyToInvalidate null");
		}
	}
}
