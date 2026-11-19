package org.comp3607.model;

/**
 * Identifies a telemetry session and records its start and end times.
 */
public class SessionMetadata {
	/** Identifier shared by the events and exports for this session. */
	private final String sessionId;
	/** Student identifier associated with the session, when available. */
	private final String studentId;
	/** Session start time in milliseconds since the epoch. */
	private final long startMs;
	/** Session end time in milliseconds since the epoch. */
	private final long endMs;

	/**
	 * Creates metadata for a telemetry session.
	 *
	 * @param sessionId session identifier
	 * @param studentId student identifier or placeholder
	 * @param startMs session start time in milliseconds since the epoch
	 * @param endMs session end time in milliseconds since the epoch
	 */
	public SessionMetadata(String sessionId, String studentId, long startMs, long endMs) {
		this.sessionId = sessionId;
		this.studentId = studentId;
		this.startMs = startMs;
		this.endMs = endMs;
	}

	/**
	 * Returns this session's identifier.
	 *
	 * @return session identifier
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Returns the student identifier associated with this session.
	 *
	 * @return student identifier or placeholder
	 */
	public String getStudentId() {
		return studentId;
	}

	/**
	 * Returns the session start time.
	 *
	 * @return start time in milliseconds since the epoch
	 */
	public long getStartMs() {
		return startMs;
	}

	/**
	 * Returns the session end time.
	 *
	 * @return end time in milliseconds since the epoch
	 */
	public long getEndMs() {
		return endMs;
	}
}
