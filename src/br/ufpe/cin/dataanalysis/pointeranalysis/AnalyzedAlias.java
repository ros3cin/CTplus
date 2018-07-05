package br.ufpe.cin.dataanalysis.pointeranalysis;

public class AnalyzedAlias {
	private String className;
	private String methodName;
	private String variableName;
	private PointerAnalysisVariableType type;
	
	public AnalyzedAlias(String className, String methodName, String variableName, PointerAnalysisVariableType type) {
		this.className=className;
		this.methodName = methodName;
		this.variableName = variableName;
		this.type = type;
	}
	
	@Override
	public String toString() {
		if(type == PointerAnalysisVariableType.LOCAL_VARIABLE) {
			return this.className+"."+this.methodName+"."+this.variableName;
		} else {
			return this.className+"."+this.variableName;
		}
	}

	public String getClassName() {
		return this.className;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public String getVariableName() {
		return this.variableName;
	}

	public PointerAnalysisVariableType getType() {
		return this.type;
	}

}
