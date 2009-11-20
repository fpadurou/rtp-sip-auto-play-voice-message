package com.sqlmanager;

import java.sql.*;
import java.util.ArrayList;
import com.common.*;

public class SqlManager 
{
	public static String m_strErrorMessage = null;
	
	static 
	{
		try
		{
			new JDCConnectionDriver( "com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1:3306/voice", "root", "POP");
        }
		catch(Exception e)
		{
		}
	}

	private static Connection getConnection() throws SQLException
	{
		return DriverManager.getConnection("jdbc:jdc:jdcpool");
	}

	private static void processError() throws Exception
	{
		if(m_strErrorMessage != null)
		{
			String message = m_strErrorMessage;
			m_strErrorMessage = null;
			throw new Exception(message);
		}
	}
	
	private static void closeConnection(Connection conn)
	{
		// --- close connection
		if(conn != null)
		{
			try
			{
				conn.close();
			}
			catch(SQLException e)
			{
				m_strErrorMessage = e.getMessage();
			}
		}
	}
	
	// --- creates a new user
	public static int createUser(int nLiferayId, String strLiferayName, String strLiferayIp) throws Exception
	{
		strLiferayName = strLiferayName.replaceAll("\'", "");
		strLiferayName = strLiferayName.trim();
		
		strLiferayIp   = strLiferayIp.replaceAll("\'", "");
		strLiferayIp   = strLiferayIp.trim();
		
		Connection conn = null;
		int nResult 	= -1;
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
			
			// --- call method in Sql
			ResultSet resultSet = stm.executeQuery("select CreateUser(" + nLiferayId + ", '" + strLiferayName + "', '" + strLiferayIp + "') as Result");
			
			// --- return first field, LiferayId should be unique key in database
			if(resultSet != null && resultSet.next())
			{
				nResult = resultSet.getInt("Result");
			}
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
		
		return nResult;
	}
	
	// --- login user
	public static int loginUser(int nLiferayId, String strSession) throws Exception
	{
		Connection conn = null;
		int nResult = -1;
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();

			strSession = strSession.replaceAll("\'", "");
			
			// --- call method in Sql
			ResultSet resultSet = stm.executeQuery("select LoginUser(" + nLiferayId + ", '" + strSession + "') as Result");
			
			// --- return first field, LiferayId should be unique key in database
			if(resultSet != null && resultSet.next())
			{
				nResult = resultSet.getInt("Result");
			}
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
		
		return nResult;
	}
	
	
	// --- logout user
	public static int logoutUser(String strSession) throws Exception
	{
		Connection conn = null;
		int nResult = -1;
		
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
	
			strSession = strSession.replaceAll("\'", "");
			
			// --- call method in Sql
			ResultSet resultSet = stm.executeQuery("select LogoutUser('" + strSession + "') as Result");
	
			// --- return first field, LiferayId should be unique key in database
			if(resultSet != null && resultSet.next())
			{
				nResult = resultSet.getInt("Result");
			}
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
		
		return nResult;		
	}
	
	// --- check if the phone can register : Liferay user is logged in
	public static boolean isOnline(int nLiferayId) throws Exception
	{
		Connection conn = null;
		boolean bResult = false;
		try
		{
			conn = getConnection();
			
			Statement stm = conn.createStatement();
	
			// --- execute the query
			ResultSet resultSet = stm.executeQuery("SELECT Online FROM users WHERE LiferayId = " + nLiferayId);
			
			// --- return first field, LiferayId should be unique key in database
			if(resultSet != null && resultSet.next())
			{
				bResult = resultSet.getBoolean("Online");
			}
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
		
		return bResult;		
	}
	
	// --- returns Sip address of a user based on it's liferayId
	public static String getSipAddress(int nLiferayId) throws Exception
	{
		Connection conn = null;
		String strResult = "";
		
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
			
			// --- execute the query
			ResultSet resultSet = stm.executeQuery("SELECT Sip FROM users WHERE LiferayId = " + nLiferayId);
			
			// --- return first address, LiferayId should be unique key in database
			if(resultSet != null && resultSet.next())
			{
				strResult = resultSet.getString("Sip");
				
				if(strResult == null || strResult == "null")
					strResult = "";
				
				strResult = strResult.trim();
			}
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
		
		// --- return the result
		return strResult;
	}
	
	// --- returns Sip address of a user based on it's liferayId
	public static void setSipAddress(int nLiferayId, String strSip) throws Exception
	{
		Connection conn = null;
		
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
			
			// --- execute the query
			if(strSip != null)
			{
				strSip = strSip.replaceAll("\'", "");
				strSip = strSip.trim();
				
				stm.executeUpdate("UPDATE users set Sip = '" + strSip + "' WHERE LiferayId = " + nLiferayId);
			}
			else
			{
				stm.executeUpdate("UPDATE users set Sip = null WHERE LiferayId = " + nLiferayId);
			}
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}		
	}
	
	
	public static String getUserName(int nLiferayId) throws Exception
	{
		Connection conn = null;
		String strResult = "";
		
		try
		{
			conn = getConnection();
			
			Statement stm = conn.createStatement();
			
			// --- execute the query
			ResultSet resultSet = stm.executeQuery("SELECT LiferayName FROM users WHERE LiferayId = " + nLiferayId);
	
			// --- return first name, LiferayId should be unique key in database
			if(resultSet != null && resultSet.next())
			{
				strResult = resultSet.getString("LiferayName");
			}
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}	
		
		
		// --- get the result
		return strResult;
	}
	
	// --- stores in database a call session between two users
	public static void setCallSession(int nFirstLiferayId, int secondLiferayId, String strSession) throws Exception
	{		
		if(strSession == null)
			return;
	
		Connection conn = null;
		try
		{
			conn = getConnection();
			
			Statement stm = conn.createStatement();
			
			strSession = strSession.replaceAll("\'", "");
			strSession = strSession.trim();
			
			stm.executeUpdate("delete from calls where (FirstUser = " + nFirstLiferayId + " and SecondUser = " + secondLiferayId + ") or (FirstUser = " + secondLiferayId + " and SecondUser = " + nFirstLiferayId + ");");
			stm.executeUpdate("INSERT INTO calls(FirstUser, SecondUser, CallSession) values("+nFirstLiferayId+", " + secondLiferayId + ", '"+strSession+"');");
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
	}
	
	// --- gets user id that initiated the call
	/*
	public static String getCallInitiator(String strSession) throws Exception
	{
		if(strSession == null)
			return null;
		
		String strResult = null;
		Connection conn = null;
		
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
	
			strSession = strSession.replaceAll("\'", "");
			strSession = strSession.trim();
			
			ResultSet resultSet = stm.executeQuery("select FirstUser from calls where CallSession = '" + strSession + "'");
	
			if(resultSet != null && resultSet.next())
			{
				strResult = resultSet.getString("FirstUser");
			}
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
		
		return strResult;
	}
	*/

/*
	public static void setSDP(String strSDP, int nFirstLiferayId, int secondLiferayId, boolean bFirst) throws Exception
	{
		if(strSDP == null)
			return;
	
		Connection conn = null;
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
			
			if(bFirst)
				stm.executeUpdate("UPDATE calls set FirstSdp = '"+strSDP+"' where (FirstUser = " + nFirstLiferayId + " and SecondUser = " + secondLiferayId + ") or (FirstUser = " + secondLiferayId + " and SecondUser = " + nFirstLiferayId + ");");
			else
				stm.executeUpdate("UPDATE calls set SecondSdp = '"+strSDP+"' where (FirstUser = " + nFirstLiferayId + " and SecondUser = " + secondLiferayId + ") or (FirstUser = " + secondLiferayId + " and SecondUser = " + nFirstLiferayId + ");");
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
	}
*/	
/*
	public static String getSDP(int nFirstLiferayId, int secondLiferayId, boolean bFirst) throws Exception
	{
		Connection conn = null;
		String strResult = "";
		
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
	
			// --- get the session
			ResultSet resultSet;
			if(bFirst)
				resultSet = stm.executeQuery("select FirstSdp as result from calls where (FirstUser = " + nFirstLiferayId + " and SecondUser = " + secondLiferayId + ") or (FirstUser = " + secondLiferayId + " and SecondUser = " + nFirstLiferayId + ");");
			else
				resultSet = stm.executeQuery("select SecondSdp as result from calls where (FirstUser = " + nFirstLiferayId + " and SecondUser = " + secondLiferayId + ") or (FirstUser = " + secondLiferayId + " and SecondUser = " + nFirstLiferayId + ");");
	
			// --- return first name, LiferayId should be unique key in database
			if(resultSet != null && resultSet.next())
			{
				strResult = resultSet.getString("result");
			}
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
		// --- get the result
		return strResult;
	}	
*/	
	public static String getCallSession(int nFirstLiferayId, int secondLiferayId, boolean remove) throws Exception
	{
		Connection conn = null;
		String strResult = "";
		
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
	
			// --- get the session
			ResultSet resultSet = stm.executeQuery("select CallSession from calls where (FirstUser = " + nFirstLiferayId + " and SecondUser = " + secondLiferayId + ") or (FirstUser = " + secondLiferayId + " and SecondUser = " + nFirstLiferayId + ");");
	
			// --- return first name, LiferayId should be unique key in database
			if(resultSet != null && resultSet.next())
			{
				strResult = resultSet.getString("CallSession");
				strResult = strResult.trim();
			}
	
			// --- remove it from database
			if(remove)
			{
				stm.executeUpdate("delete from calls where (FirstUser = " + nFirstLiferayId + " and SecondUser = " + secondLiferayId + ") or (FirstUser = " + secondLiferayId + " and SecondUser = " + nFirstLiferayId + ");");
			}
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
		// --- get the result
		return strResult;
	}
	
	// --- removes a session from database
	public static void removeCallSession(String strSession) throws Exception
	{
		if(strSession == null)
			return;
		
		Connection conn = null;
		
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
	
			strSession = strSession.replaceAll("\'", "");
			strSession = strSession.trim();
			
			stm.executeUpdate("delete from calls where CallSession = '" + strSession + "'");
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
	}

	// --- marks in database the status between 2 users
	public static void setStatus(String strSessionId, int status) throws Exception
	{
		Connection conn = null;
		
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
			
			// --- only one from these two lines will process
			stm.executeUpdate("update calls set Status = " + status + " where CallSession = '" + strSessionId + "';");
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
	}
	
	
	// --- marks in database the status between 2 users
	public static void setStatus(int nFirstLiferayId, int secondLiferayId, boolean bOnHold) throws Exception
	{
		Connection conn = null;
		
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
			
			// --- only one from these two lines will process
			stm.executeUpdate("update calls set Status = " + (bOnHold ? IStatus.STATUS_CALL_ON_HOLD_BY_USER : IStatus.STATUS_CALL_ACTIVE) + " where (FirstUser = " + nFirstLiferayId + " and SecondUser = " + secondLiferayId + ");");
			stm.executeUpdate("update calls set Status = " + (bOnHold ? IStatus.STATUS_CALL_ON_HOLD_BY_FRIEND : IStatus.STATUS_CALL_ACTIVE) + " where (FirstUser = " + secondLiferayId + " and SecondUser = " + nFirstLiferayId + ");");
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
	}
	
	// --- returns the status between 2 users: no call, active call, on hold by, processing etc...
	public static int getStatus(int nFirstLiferayId, int secondLiferayId) throws Exception
	{
		Connection conn = null;
		int nResult = IStatus.STATUS_IDLE;
		
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();

			ResultSet resultSet = stm.executeQuery("select Status, FirstUser from calls where (FirstUser = " + nFirstLiferayId + " and SecondUser = " + secondLiferayId + ") or (FirstUser = " + secondLiferayId + " and SecondUser = " + nFirstLiferayId + ");");
	
			// --- return first name, LiferayId should be unique key in database
			if(resultSet != null && resultSet.next())
			{
				// --- to make a difference between Status 0 and Status null
				if(resultSet.getObject("Status") == null)
					nResult = IStatus.STATUS_IDLE;
				else
				{
					nResult = resultSet.getInt("Status");
					
					// --- if FirstUser is indeed FirstLiferayId then the status is ok, else is reversed
					if(nResult == IStatus.STATUS_CALL_ON_HOLD_BY_USER || nResult == IStatus.STATUS_CALL_ON_HOLD_BY_FRIEND)
					{
						int nFirstUser = resultSet.getInt("FirstUser");
						if(nFirstUser == nFirstLiferayId)
							;
						else
						{
							if(nResult == IStatus.STATUS_CALL_ON_HOLD_BY_USER)
								nResult = IStatus.STATUS_CALL_ON_HOLD_BY_FRIEND;
							else
								nResult = IStatus.STATUS_CALL_ON_HOLD_BY_USER;
						}
					}
				}
			}
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
		
		// --- get the result
		return nResult;
	}
	
	public static ArrayList<String[]> getUsersOnline() throws Exception
	{
		Connection conn = null;
		ArrayList<String[]> result = new ArrayList<String[]>();
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
			
			// --- execute the query
			ResultSet resultSet = stm.executeQuery("select LiferayId, LiferayName, Sip from users where Sip is not null");
			
			if(resultSet != null)
			{
				while(resultSet.next())
				{
					String[] rQuery = new String[3];
					
					rQuery[0] = resultSet.getString("LiferayId");
					rQuery[1] = resultSet.getString("LiferayName");
					rQuery[2] = resultSet.getString("Sip");
					
					result.add(rQuery);
				}
			}
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
		// --- get the result
		return result;
	}

	// --- inserts in database a new VoceFlowRequest
	public static void setRequest(int nSenderId, int nReceiverId, String strText) throws Exception
	{
		Connection conn = null;
		
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
			
			strText = strText.replace('\'', ' ');
			// --- only one from these two lines will process
			stm.executeUpdate("insert into requests (SenderId, ReceiverId, Text, Status) values (" + nSenderId + ", " + nReceiverId + ", '" + strText + "', 'pending');");
		}
		catch(SQLException e)
		{
			m_strErrorMessage = "Text: " + strText + "Error: " + e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
	}	
	
	// --- gets all the voice members from database
	public static ArrayList<String[]> getMembers(int userId) throws Exception
	{
		Connection conn = null;
		ArrayList<String[]> result = new ArrayList<String[]>();
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
			
			// --- execute the query
			ResultSet resultSet = stm.executeQuery("select LiferayId, LiferayName from users where LiferayId != " + userId);
			
			if(resultSet != null)
			{
				while(resultSet.next())
				{
					String[] rQuery = new String[2];
					
					rQuery[0] = resultSet.getString("LiferayId");
					rQuery[1] = resultSet.getString("LiferayName");
					
					result.add(rQuery);
				}
			}
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
		// --- get the result
		return result;
	}	
	
	// --- gets all the requests made by a user from database
	public static ArrayList<String[]> getRequests(int userId, boolean received) throws Exception
	{
		Connection conn = null;
		ArrayList<String[]> result = new ArrayList<String[]>();
		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
			
			// --- execute the query
			ResultSet resultSet = null;
			
			if(received)
				resultSet = stm.executeQuery("select Id, LiferayName, Text, Status, LiferayId from requests inner join users on SenderId = LiferayId where ReceiverId = " + userId);
			else
				resultSet = stm.executeQuery("select Id, LiferayName, Text, Status, LiferayId from requests inner join users on ReceiverId = LiferayId where SenderId = " + userId);
			
			if(resultSet != null)
			{
				while(resultSet.next())
				{
					String[] rQuery = new String[5];
					
					rQuery[0] = resultSet.getString("LiferayName");
					rQuery[1] = resultSet.getString("Text");
					rQuery[2] = resultSet.getString("Status");
					rQuery[3] = resultSet.getString("Id");
					rQuery[4] = resultSet.getString("LiferayId");
					
					result.add(rQuery);
				}
			}
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
		// --- get the result
		return result;
	}
	
	// --- updates status database for a VoceFlowRequest
	public static void executeRequest(int nReceiverId, ArrayList<String> acceptRequests, ArrayList<String> denyRequests) throws Exception
	{
		Connection conn = null;

		try
		{
			conn = getConnection();
			Statement stm = conn.createStatement();
			
			// --- accept
			if(acceptRequests.size() > 0)
			{
				String strRequest = "update requests set Status = 'approved' where ReceiverId = " + nReceiverId + " and (";
				
				for(int i=0; i<acceptRequests.size(); i++)
				{
					strRequest += "Id = " + acceptRequests.get(i).replaceAll("\'", "");
					
					if(i != acceptRequests.size() - 1)
						strRequest += " or ";
				}
				strRequest += ")";
				
				stm.executeUpdate(strRequest);
			}
			
			// --- deny
			if(denyRequests.size() > 0)
			{
				String strRequest = "update requests set Status = 'denied' where ReceiverId = " + nReceiverId + " and (";
				
				for(int i=0; i<denyRequests.size(); i++)
				{
					strRequest += "Id = " + denyRequests.get(i).replaceAll("\'", "");
				
					if(i != denyRequests.size() - 1)
						strRequest += " or ";
				}						
				strRequest += ")";
				stm.executeUpdate(strRequest);
			}			
		}
		catch(SQLException e)
		{
			m_strErrorMessage = e.getMessage();
		}
		finally
		{
			closeConnection(conn);
			processError();
		}
	}
}
