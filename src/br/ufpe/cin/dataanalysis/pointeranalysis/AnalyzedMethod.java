package br.ufpe.cin.dataanalysis.pointeranalysis;

import java.util.HashMap;
import java.util.Map;

public class AnalyzedMethod {
	private transient AnalyzedClass declaringClass;
	private String methodName;
	private int sourceCodeLineNumber;
	private int numberOfParameters;
	private Map<String,AnalyzedLocalVariable> analyzedLocalVariables;
	
	public AnalyzedMethod(AnalyzedClass declaringClass, String methodName, int sourceCodeLineNumber, int numberOfParameters) {
		this.declaringClass = declaringClass;
		this.methodName = methodName;
		this.analyzedLocalVariables = new HashMap<String,AnalyzedLocalVariable>();
		this.sourceCodeLineNumber = sourceCodeLineNumber;
		this.numberOfParameters = numberOfParameters;
	}
	
	public int getSourceCodeLineNumber() {
		return this.sourceCodeLineNumber;
	}
	
	public boolean contains(AnalyzedLocalVariable analyzedLocalVariable) {
		String key = analyzedLocalVariable.toString();
		return this.analyzedLocalVariables.containsKey(key);
	}
	
	public void addLocalVariable(AnalyzedLocalVariable analyzedLocalVariable) {
		String key = analyzedLocalVariable.toString();
		this.analyzedLocalVariables.put(key,analyzedLocalVariable);
	}
	
	public Map<String,AnalyzedLocalVariable> getAnalyzedLocalVariables() {
		return this.analyzedLocalVariables;
	}
	
	public boolean hasAnyAlias() {
		for(AnalyzedLocalVariable localVariable : this.analyzedLocalVariables.values()) {
			if(localVariable.getAliases().size()>0)
				return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return String.format("%s-%d",this.methodName,this.numberOfParameters);
	}
	
	public String getMethodName() {
		return this.methodName;
	}
	
	public AnalyzedClass getDeclaringClass() {
		return this.declaringClass;
	}
	
	public void setDeclaringClass(AnalyzedClass declaringClass) {
		this.declaringClass = declaringClass;
	}

	public int getNumberOfParameters() {
		return numberOfParameters;
	}

	public void setNumberOfParameters(int numberOfParameters) {
		this.numberOfParameters = numberOfParameters;
	}
}
