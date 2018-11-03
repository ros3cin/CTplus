package br.ufpe.cin.dataanalysis.pointeranalysis;

import java.util.Set;
import java.util.TreeSet;

public class AnalyzedMethod implements Comparable<AnalyzedMethod> {
	private AnalyzedClass declaringClass;
	private String methodName;
	private int sourceCodeLineNumber;
	private Set<AnalyzedLocalVariable> analyzedLocalVariables;
	
	public AnalyzedMethod(AnalyzedClass declaringClass, String methodName, int sourceCodeLineNumber) {
		this.declaringClass = declaringClass;
		this.methodName = methodName;
		this.analyzedLocalVariables = new TreeSet<AnalyzedLocalVariable>();
		this.sourceCodeLineNumber = sourceCodeLineNumber;
	}
	
	public int getSourceCodeLineNumber() {
		return this.sourceCodeLineNumber;
	}
	
	public boolean contains(AnalyzedLocalVariable analyzedLocalVariable) {
		return this.analyzedLocalVariables.contains(analyzedLocalVariable);
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

	@Override
	public int compareTo(AnalyzedMethod o) {
		int result = 0;
		result = this.declaringClass.getClassName().compareTo(o.getDeclaringClass().getClassName());
		if(result==0) {
			result = this.methodName.compareTo(o.getMethodName());
		}
		if(result == 0) {
			result = Integer.compare(this.sourceCodeLineNumber, o.getSourceCodeLineNumber());
		}
		return result;
	}
}
