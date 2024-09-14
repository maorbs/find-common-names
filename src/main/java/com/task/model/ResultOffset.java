package com.task.model;

public class ResultOffset {

	private long lineOffset;
	private long charOffset;
	
	public ResultOffset(long l, long m) {
		this.lineOffset = l;
		this.charOffset = m;
	}
	
	public long getLineOffset() {
		return lineOffset;
	}
	public void setLineOffset(long lineOffset) {
		this.lineOffset = lineOffset;
	}
	public long getCharOffset() {
		return charOffset;
	}
	public void setCharOffset(long charOffset) {
		this.charOffset = charOffset;
	}
	
	@Override
	public String toString() {
		return "[lineOffset= " + lineOffset + ", charOffset=" + charOffset + "]";
	}
	
	
}
