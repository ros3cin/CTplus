package br.ufpe.cin.dataanalysis;

import java.util.HashSet;
import java.util.Set;

public class ExplicitInstance {
	private String concreteType;
	private Set<Integer> sourceCodeLineNumbers;
	public ExplicitInstance(String concreteType, int sourceCodeLineNumber) {
		this.concreteType = concreteType;
		this.sourceCodeLineNumbers = new HashSet<Integer>();
		this.sourceCodeLineNumbers.add(sourceCodeLineNumber);
	}
	public String getConcreteType() {
		return concreteType;
	}
	public void setConcreteType(String concreteType) {
		this.concreteType = concreteType;
	}
	public Set<Integer> getSourceCodeLineNumbers() {
		return sourceCodeLineNumbers;
	}
	public void setSourceCodeLineNumber(Set<Integer> sourceCodeLineNumbers) {
		this.sourceCodeLineNumbers = sourceCodeLineNumbers;
	}
	public void addSourceCodeLineNumber(int sourceCodeLineNumber) {
		this.sourceCodeLineNumbers.add(sourceCodeLineNumber);
	}
	
}
