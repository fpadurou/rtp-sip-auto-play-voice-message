package com.common;

public interface IStatus
{
	// --- status between current user and a friend
	public final int STATUS_IDLE = -1;
	public final int STATUS_PROCESSING_REQUEST = 0;
	public final int STATUS_CALL_ACTIVE = 1;
	public final int STATUS_CALL_ON_HOLD_BY_USER = 2;
	public final int STATUS_CALL_ON_HOLD_BY_FRIEND = 3;
}