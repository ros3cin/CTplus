package br.ufpe.cin.dataanalysis.pointeranalysis;

import java.util.Set;
import java.util.TreeSet;

public class AnalyzedLocalVariable implements IContainAlias {
	private AnalyzedMethod declaringMethod;
	private String variableName;
	private Set<AnalyzedAlias> aliases;
	
	public AnalyzedLocalVariable(AnalyzedMethod declaringMethod, String variableName) {
		this.aliases = new TreeSet<AnalyzedAlias>(new AnalyzedAliasComparator());
		this.declaringMethod = declaringMethod;
		this.variableName = variableName;
	}
	public void addAlias(AnalyzedAlias alias) {
		if(!this.aliases.contains(alias)) {
			this.aliases.add(alias);
		}
	}
	
	public String getVariableName() {
		return this.variableName;
	}
	
	@Override
	public String toString() {
		return this.declaringMethod.getDeclaringClass().getClassName()+"."+this.declaringMethod.getMethodName()+"."+this.variableName;
	}
	
	public AnalyzedMethod getDeclaringMethod() {
		return this.declaringMethod;
	}
	
	public Set<AnalyzedAlias> getAliases() {
		return this.aliases;
	}
}
