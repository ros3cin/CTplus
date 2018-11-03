package br.ufpe.cin.dataanalysis;

import java.util.ArrayList;

public class CollectionMethod {

	private String nome;
	private String superType;
	private int ocorrencias;
	private int numeroVariaveisCompartilhadas;
	private String classe;
	private String concreteType;

	private boolean isSyncMethod;
	private boolean isSyncBlock;
	private boolean isLock;
	private boolean isIntoLoop;
	
	private boolean recursivo;

	//private boolean insideForeach;
	
	

	private ArrayList<Integer> interationLoopSize = new ArrayList<Integer>();
	
	//Field name that calls the method;,
	private String fieldName;
	private boolean isFieldLocal;
	private String callMethodName;

	//TODO: method has loopblock 
	private String conditionalBlock = "";
	
	//TODO: delete
	private String conditionalBlockN = "";
	
	private int invokeLineNumber = 0;
	
	private ArrayList<LoopBlockInfo> outerLoops;
	public ArrayList<LoopBlockInfo> getOuterLoops() {
		return outerLoops;
	}

	public void setOuterLoops(ArrayList<LoopBlockInfo> outerLoops) {
		this.outerLoops = new ArrayList<LoopBlockInfo>();
		this.outerLoops.addAll(outerLoops);
	}

	public int getProfundidade() {
		return profundidade;
	}

	public void setProfundidade(int profundidade) {
		this.profundidade = profundidade;
	}

	private int profundidade;

	
	public String getConditionalBlockN() {
		return conditionalBlockN;
	}

	public void setConditionalBlockN(String conditionalBlockN) {
		this.conditionalBlockN = conditionalBlockN;
	}
	
	public String getConditionalBlock() {
		return conditionalBlock;
	}

	public void setConditionalBlock(String conditionalBlock) {
		this.conditionalBlock = conditionalBlock;
	}

	public boolean isSyncMethod() {
		return isSyncMethod;
	}

//	public boolean isInsideForeach() {
//		return insideForeach;
//	}
//
//	public void setInsideForeach(boolean insideForeach) {
//		this.insideForeach = insideForeach;
//	}

	public ArrayList<Integer> getInterationLoopSize() {
		return interationLoopSize;
	}

	public void setSyncMethod(boolean isSyncMethod) {
		this.isSyncMethod = isSyncMethod;
	}

	public boolean isSyncBlock() {
		return isSyncBlock;
	}

	public void setSyncBlock(boolean isSyncBlock) {
		this.isSyncBlock = isSyncBlock;
	}

	public boolean isLock() {
		return isLock;
	}

	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}

	public int getNumeroVariaveisCompartilhadas() {
		return numeroVariaveisCompartilhadas;
	}

	public void setNumeroVariaveisCompartilhadas(int numeroVariaveisCompartilhadas) {
		this.numeroVariaveisCompartilhadas = numeroVariaveisCompartilhadas;
	}

	public String getClasse() {
		return classe;
	}

	public void setClasse(String classe) {
		this.classe = classe;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSuperType() {
		return superType;
	}

	public void setSuperType(String pacote) {
		this.superType = pacote;
	}

	public int getOcorrencias() {
		return ocorrencias;
	}

	public void setOcorrencias(int ocorrencias) {
		this.ocorrencias = ocorrencias;
	}

	public void incrementarOcorrencias() {
		this.ocorrencias++;
	}

	public void incrementarVariaveisCompartilhadas() {
		this.numeroVariaveisCompartilhadas++;
	}

	@Override
	public String toString() {
		return "Metodo: " + this.getNome() + " Pacote: " + this.getSuperType() + " Ocorrencias: " + this.getOcorrencias()
				+ " Numero Variaveis Compartilhadas: " + this.getNumeroVariaveisCompartilhadas();
	}

	public boolean equals(CollectionMethod metodo) {
		
		if (this.getNome().equals(metodo.getNome()) 
				&& this.getInvokeLineNumber() == metodo.getInvokeLineNumber() 
				&& this.isIntoLoop() == metodo.isIntoLoop() 
				&& this.getClasse().equals(metodo.getClasse()) && this.getConcreteType().equals(metodo.getConcreteType())
				&& this.getSuperType().equals(metodo.getSuperType()) 
				&& this.getCallMethodName().equals(metodo.getCallMethodName()) 
				&& this.getFieldName().equals(metodo.getFieldName())
				&& this.loopsToString().equals(metodo.loopsToString())) { 
			return true; 
		}
		
		return false;
	}
	
	public String loopsToString(){
		
		String s = "";
		int n =0;
		for (int i = 0; i < outerLoops.size(); i++) {
			n = i+1;
			s += n +") "+ outerLoops.get(i).informationToString() + " ";
		}
		
		return s;
		
	}

	public boolean equalsLight(CollectionMethod metodo) {
		if (this.getNome().equals(metodo.getNome()) && this.getSuperType().equals(metodo.getSuperType())) { return true; }
		return false;
	}

	public boolean isIntoLoop() {
		return isIntoLoop;
	}

	public void setIntoLoop(boolean isIntoLoop) {
		this.isIntoLoop = isIntoLoop;
	}

	public String getConcreteType() {
		return concreteType;
	}

	public void setConcreteType(String concreteType) {
		this.concreteType = concreteType;
	}

	public void setInvokeLineNumber(int invokeLineNumber) {
		this.invokeLineNumber = invokeLineNumber;		
	}

	public int getInvokeLineNumber() {
		return invokeLineNumber;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getCallMethodName() {
		return callMethodName;
	}

	public void setCallMethodName(String callMethodName) {
		this.callMethodName = callMethodName;
	}
	
	public boolean isInsideRecursive() {
		return recursivo;
	}

	public void setInsideRecursiveMethod(boolean recursivo) {
		this.recursivo = recursivo;
	}

	public boolean isFieldLocal() {
		return isFieldLocal;
	}

	public void setFieldLocal(boolean isLocal) {
		this.isFieldLocal = isLocal;
	}

}
