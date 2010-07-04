package org.gephi.streaming.impl.json.parser;

public class JSONConstants {
	
	public enum Types {
		CE("ce"),
		CG("cg"),
		CN("cn"),
		AE("ae"),
		DE("de"),
		AN("an"),
		DN("dn");
		
		private String value;
		private Types(String value) {
			this.value = value;
		}
		
		public String value() {
			return value;
		}
	}
	
	public enum Fields {
		TYPE("type"),
		ID("id"),
		ATTRIBUTE("attribute"),
		VALUE("value"),
		SOURCE("source"),
		TARGET("target"),
		DIRECTED("directed");
		
		private String value;
		private Fields(String value) {
			this.value = value;
		}
		
		public String value() {
			return value;
		}
	};
	
	

}
