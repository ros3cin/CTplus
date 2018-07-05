package br.ufpe.cin.dataanalysis.pointeranalysis;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class AnalyzedMethod {
	private AnalyzedClass declaringClass;
	private String methodName;
	private Set<AnalyzedLocalVariable> analyzedLocalVariables;
	
	public AnalyzedMethod(AnalyzedClass declaringClass, String methodName) {
		this.declaringClass = declaringClass;
		this.methodName = methodName;
		this.analyzedLocalVariables = new HashSet<AnalyzedLocalVariable>();
	}
	
	public void addLocalVariable(AnalyzedLocalVariable analyzedLocalVariable) {
		this.analyzedLocalVariables.add(analyzedLocalVariable);
	}
	
	public Set<AnalyzedLocalVariable> getAnalyzedLocalVariables() {
		return this.analyzedLocalVariables;
	}
	
	public boolean hasAnyAlias() {
		for(AnalyzedLocalVariable localVariable : this.analyzedLocalVariables) {
			if(localVariable.getAliases().size()>0)
				return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return this.declaringClass.getClassName()+"."+this.methodName;
	}
	
	public String getMethodName() {
		return this.methodName;
	}
	
	public AnalyzedClass getDeclaringClass() {
		return this.declaringClass;
	}
}
