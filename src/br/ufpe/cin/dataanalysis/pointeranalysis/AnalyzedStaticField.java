package br.ufpe.cin.dataanalysis.pointeranalysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class AnalyzedStaticField implements IContainAlias {
	private transient AnalyzedClass declaringClass;
	private String fieldName;
	private Map<String,AnalyzedAlias> aliases;
	
	public AnalyzedStaticField(AnalyzedClass declaringClass, String fieldName) {
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
