package br.ufpe.cin.dataanalysis.pointeranalysis;

import java.util.HashMap;
import java.util.Map;

public class AnalyzedLocalVariable implements IContainAlias {
	private transient AnalyzedMethod declaringMethod;
	private String variableName;
	private Map<String,AnalyzedAlias> aliases;
	
	public AnalyzedLocalVariable(AnalyzedMethod declaringMethod, String variableName) {
		this.aliases = new HashMap<String,AnalyzedAlias>();
		this.declaringMethod = declaringMethod;
		this.variableName = variableName;
	}
	public void addAlias(AnalyzedAlias alias) {
		String key = alias.toString();
		if(!this.aliases.containsKey(key)) {
			this.aliases.put(key,alias);
		}
	}
	
	public String getVariableName() {
		return this.variableName;
	}
	
	@Override
	public String toString() {
		return this.variableName;
	}
	
	public AnalyzedMethod getDeclaringMethod() {
		return this.declaringMethod;
	}
	
	public void setDeclaringMethod(AnalyzedMethod declaringMethod) {
		this.declaringMethod = declaringMethod;
	}
	
	public Map<String,AnalyzedAlias> getAliases() {
		return this.aliases;
	}
}
