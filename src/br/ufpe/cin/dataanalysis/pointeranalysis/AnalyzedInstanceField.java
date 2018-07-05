package br.ufpe.cin.dataanalysis.pointeranalysis;

import java.util.Set;
import java.util.TreeSet;

public class AnalyzedInstanceField implements IContainAlias {
	private AnalyzedClass declaringClass;
	private String fieldName;
	private Set<AnalyzedAlias> aliases;
	
	public AnalyzedInstanceField(AnalyzedClass declaringClass, String fieldName) {
		this.aliases = new TreeSet<AnalyzedAlias>(new AnalyzedAliasComparator());
		this.declaringClass = declaringClass;
		this.fieldName = fieldName;
	}
	
	public void addAlias(AnalyzedAlias alias) {
		if(!this.aliases.contains(alias)) {
			this.aliases.add(alias);
		}
	}
	
	public String getFieldName() {
		return this.fieldName;
	}
	
	@Override
	public String toString() {
		return this.declaringClass.getClassName()+"."+this.fieldName;
	}
	
	public AnalyzedClass getDeclaringClass() {
		return this.declaringClass;
	}
	
	public Set<AnalyzedAlias> getAliases() {
		return this.aliases;
	}
}
