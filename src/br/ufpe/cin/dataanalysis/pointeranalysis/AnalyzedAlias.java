package br.ufpe.cin.dataanalysis.pointeranalysis;

public class AnalyzedAlias {
	private String className;
	private String methodName;
	private Integer methodNumOfParameters;
	private String variableName;
	private PointerAnalysisVariableType type;
	
	public AnalyzedAlias(String className, String methodName, String variableName, Integer methodNumOfParameters, PointerAnalysisVariableType type) {
		this.className=className;
		this.methodName = methodName;
		this.variableName = variableName;
		this.methodNumOfParameters = methodNumOfParameters;
		this.type = type;
	}
	
	@Override
	public String toString() {
		if(type == PointerAnalysisVariableType.LOCAL_VARIABLE) {
			return String.format("%s-%s-%d-%s",this.className,this.methodName,this.methodNumOfParameters,this.variableName);
		} else {
			return String.format("%s-%s",this.className,this.variableName);
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

	public Integer getMethodNumOfParameters() {
		return methodNumOfParameters;
	}

}
