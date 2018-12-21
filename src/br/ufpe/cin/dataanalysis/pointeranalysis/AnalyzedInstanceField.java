package br.ufpe.cin.dataanalysis.pointeranalysis;

import java.util.HashMap;
import java.util.Map;

public class AnalyzedInstanceField implements IContainAlias {
	private transient AnalyzedClass declaringClass;
	private String fieldName;
	private Map<String,AnalyzedAlias> aliases;
	
	public AnalyzedInstanceField(AnalyzedClass declaringClass, String fieldName) {
		this.aliases = new HashMap<String,AnalyzedAlias>();
		this.declaringClass = declaringClass;
		this.fieldName = fieldName;
	}
	
	public void addAlias(AnalyzedAlias alias) {
		String key = alias.toString();
		if(!this.aliases.containsKey(key)) {
			this.aliases.put(key,alias);
		}
	}
	
	public String getFieldName() {
		return this.fieldName;
	}
	
	@Override
	public String toString() {
		return this.fieldName;
	}
	
	public AnalyzedClass getDeclaringClass() {
		return this.declaringClass;
	}
	
	public Map<String,AnalyzedAlias> getAliases() {
		return this.aliases;
	}
}
