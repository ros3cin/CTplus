package br.ufpe.cin.dataanalysis.pointeranalysis;

import java.util.HashMap;
import java.util.Map;

public class AnalyzedClass {
	private String className;
	
	private Map<String,AnalyzedInstanceField> analyzedInstanceFields;
	private Map<String,AnalyzedStaticField> analyzedStaticFields;
	private Map<String,AnalyzedMethod> analyzedMethods;
	
	public AnalyzedClass(String className) {
		this.className = className;
		this.analyzedInstanceFields = new HashMap<String,AnalyzedInstanceField>();
		this.analyzedStaticFields = new HashMap<String,AnalyzedStaticField>();
		this.analyzedMethods = new HashMap<String,AnalyzedMethod>();
	}
	
	public boolean hasAnyAlias() {
		for(AnalyzedInstanceField instanceField : analyzedInstanceFields.values()) {
			if(instanceField.getAliases().size()>0)
				return true;
		}
		for(AnalyzedStaticField staticField : analyzedStaticFields.values()) {
			if(staticField.getAliases().size()>0)
				return true;
		}
		for(AnalyzedMethod method : analyzedMethods.values()) {
			if(method.hasAnyAlias())
				return true;
		}
		return false;
	}
	
	public void addAnalyzedInstanceField(AnalyzedInstanceField analyzedInstanceField) {
		String key = analyzedInstanceField.toString();
		this.analyzedInstanceFields.put(key,analyzedInstanceField);
	}
	
	public void addAnalyzedStaticField(AnalyzedStaticField analyzedStaticField) {
		String key = analyzedStaticField.toString();
		this.analyzedStaticFields.put(key,analyzedStaticField);
	}
	
	public void addAnalyzedMethod(AnalyzedMethod analyzedMethod) {
		this.analyzedMethods.put(
				analyzedMethod.toString(),
				analyzedMethod
		);
	}
	
	public Map<String,AnalyzedInstanceField> getAnalyzedInstanceFields(){
		return this.analyzedInstanceFields;
	}
	
	public Map<String,AnalyzedStaticField> getAnalyzedStaticFields(){
		return this.analyzedStaticFields;
	}
	
	public Map<String,AnalyzedMethod> getAnalyzedMethods(){
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
