package org.gephi.streaming.test;

public class JsonStreamProcessorTest extends DGSStreamProcessorTest {
	
	private static final String JSON_RESOURCE = "amazon.json";

	public JsonStreamProcessorTest() {
		this.streamType = "JSON";
		this.resource = JSON_RESOURCE;
	}
	
}
