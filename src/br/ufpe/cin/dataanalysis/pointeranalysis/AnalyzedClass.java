package br.ufpe.cin.dataanalysis.pointeranalysis;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class AnalyzedClass {
	private String className;
	
	private Set<AnalyzedInstanceField> analyzedInstanceFields;
	private Set<AnalyzedStaticField> analyzedStaticFields;
	private Set<AnalyzedMethod> analyzedMethods;
	
	public AnalyzedClass(String className) {
		this.className = className;
		this.analyzedInstanceFields = new HashSet<AnalyzedInstanceField>();
		this.analyzedStaticFields = new HashSet<AnalyzedStaticField>();
		this.analyzedMethods = new HashSet<AnalyzedMethod>();
	}
	
	public boolean hasAnyAlias() {
		for(AnalyzedInstanceField instanceField : analyzedInstanceFields) {
			if(instanceField.getAliases().size()>0)
				return true;
		}
		for(AnalyzedStaticField staticField : analyzedStaticFields) {
			if(staticField.getAliases().size()>0)
				return true;
		}
		for(AnalyzedMethod method : analyzedMethods) {
			if(method.hasAnyAlias())
				return true;
		}
		return false;
	}
	
	public void addAnalyzedInstanceField(AnalyzedInstanceField analyzedInstanceField) {
		this.analyzedInstanceFields.add(analyzedInstanceField);
	}
	
	public void addAnalyzedStaticField(AnalyzedStaticField analyzedInstanceField) {
		this.analyzedStaticFields.add(analyzedInstanceField);
	}
	
	public void addAnalyzedMethod(AnalyzedMethod analyzedInstanceField) {
		this.analyzedMethods.add(analyzedInstanceField);
	}
	
	public Set<AnalyzedInstanceField> getAnalyzedInstanceFields(){
		return this.analyzedInstanceFields;
	}
	
	public Set<AnalyzedStaticField> getAnalyzedStaticFields(){
		return this.analyzedStaticFields;
	}
	
	public Set<AnalyzedMethod> getAnalyzedMethods(){
		return this.analyzedMethods;
	}
	
	public String getClassName() {
		return this.className;
	}
	
	@Override
	public String toString() {
		return this.className;
	}
	
}
