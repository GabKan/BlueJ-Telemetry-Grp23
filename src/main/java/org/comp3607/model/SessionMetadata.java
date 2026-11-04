package org.comp3607.model;

public class SessionMetadata {
	private final String sessionId;
	private final String studentId;
	private final long startMs;
	private final long endMs;

	public SessionMetadata(String sessionId, String studentId, long startMs, long endMs) {
		this.sessionId = sessionId;
		this.studentId = studentId;
		this.startMs = startMs;
		this.endMs = endMs;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getStudentId() {
		return studentId;
	}

	public long getStartMs() {
		return startMs;
	}

	public long getEndMs() {
		return endMs;
	}
}
