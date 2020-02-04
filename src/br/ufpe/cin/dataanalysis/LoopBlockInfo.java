package br.ufpe.cin.dataanalysis;

import java.util.List;
import java.util.Set;

import com.ibm.wala.shrikeBT.IBinaryOpInstruction.IOperator;
import com.ibm.wala.shrikeBT.IBinaryOpInstruction.Operator;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SymbolTable;

public class LoopBlockInfo {

	private BasicBlock loopHeader;
	private Set<ISSABasicBlock> loopBody;
	private List<ISSABasicBlock> loopTails;
	private ISSABasicBlock loopConditionalBlock;
	private boolean isDoWhileLoop;
	private boolean explicitlyInfiniteLoop;
	private IR ir;

	private int profundidade;
	private boolean foreachLoop;
	private boolean loopOverVariable;
	private IOperator loopOperator;
	private Complexity complexity;

	// may be a numeral or a literal, for instance a variable n.
	private String limit;
	
	public LoopBlockInfo(){
		super();
	}

	public LoopBlockInfo(BasicBlock loopHeader, Set<ISSABasicBlock> loopBody, List<ISSABasicBlock> loopTails, ISSABasicBlock loopConditionalBlock,
			boolean isDoWhileLoop, boolean explicitlyInfiniteLoop, IR ir) {
		super();
		this.loopHeader = loopHeader;
		this.loopBody = loopBody;
		this.loopTails = loopTails;
		this.loopConditionalBlock = loopConditionalBlock;
		this.isDoWhileLoop = isDoWhileLoop;
		this.explicitlyInfiniteLoop = explicitlyInfiniteLoop;
		this.ir = ir;
		if (!explicitlyInfiniteLoop) {
			foreachLoop = foreachLoop();
			fillLoopOverVariableInformation();
			fillConditionalBranchInterationNumber();
		}
		setLoopComplexity();

		// TODO: fill other informations like complexity, forOverArray,
		// loopOperator.
	}

	private void fillConditionalBranchInterationNumber() {

		// String nConditional = "";

		if (loopConditionalBlock != null) {

			SymbolTable symbolTable = ir.getSymbolTable();

			SSAInstruction conditionalInstruntion = loopConditionalBlock.getLastInstruction();

			conditionalInstruntion.toString(symbolTable);
			//System.out.println(conditionalInstruntion.toString(symbolTable));

			String val2Use = symbolTable.getValueString(conditionalInstruntion.getUse(1));
			String[] val2UseSplit = val2Use.split("#");
			if (val2UseSplit.length > 1) {
				if (!val2UseSplit[1].equals("null")) {
					limit = val2UseSplit[1];
				}
			}
		}
	}

	/*
	 * Example: for (int i = 0; i < array.length; i++) {
	 */
	public void fillLoopOverVariableInformation() {

		SymbolTable symbolTable = ir.getSymbolTable();
		SSAInstruction headerLastInstruction = loopHeader.getLastInstruction();

		// 1) LOOP HEAD NUMBER equals LOOP CONDITIONAL BLOCK NUMBER

		// TODO: descomentar caso apresente falso positivo
		// if (loopHeader.getNumber() == loopConditionalBlock.getNumber()
		// || headerLastInstruction instanceof SSAInvokeInstruction
		// || headerLastInstruction instanceof SSAArrayLengthInstruction) {

		// 2) LOOP TAILS HAS A BINARY OP INSTRUNCION (for example "i++")

		IOperator tailOperator;

		for (ISSABasicBlock tail : loopTails) {
			for (SSAInstruction ssaInstruction : tail) {
				if (ssaInstruction instanceof SSABinaryOpInstruction) {
					SSABinaryOpInstruction instruction = (SSABinaryOpInstruction) ssaInstruction;
					IOperator operator = instruction.getOperator();
					tailOperator = operator;

					int val1 = instruction.getUse(0);
					String val1StringTail = symbolTable.getValueString(val1);

					// in the example "i"
					String[] variableTailName = ir.getLocalNames(instruction.iindex, instruction.getUse(0));

					// 3) if the variable in conditional block ( in
					// the example "i < array.length" ) is the same ("i")
					SSAInstruction conditionalLastInstrunction = loopConditionalBlock.getLastInstruction();

					if (conditionalLastInstrunction instanceof SSAConditionalBranchInstruction) {

						SSAConditionalBranchInstruction conditionalBranchInstruction = (SSAConditionalBranchInstruction) conditionalLastInstrunction;
						conditionalBranchInstruction.toString(symbolTable);
						// System.out.println(conditionalBranchInstruction.toString(symbolTable));

						int conditionalUseVal1 = conditionalBranchInstruction.getUse(0);
						String conditionalUseVal1String = symbolTable.getValueString(conditionalUseVal1);

						int conditionalUseVal2 = conditionalBranchInstruction.getUse(1);
						String conditionalUseVal2String = symbolTable.getValueString(conditionalUseVal2);

						String[] variableConditionalName = ir.getLocalNames(conditionalBranchInstruction.iindex, conditionalBranchInstruction.getUse(0));

						// FOR OVER ARRAY IS TRUE
						if (val1StringTail.equals(conditionalUseVal1String) || val1StringTail.equals(conditionalUseVal2String)
								|| (variableTailName != null && variableConditionalName != null && variableTailName.length > 0 && variableConditionalName.length > 0 && variableTailName[0] != null && variableTailName[0].equals(variableConditionalName[0]))) {
							loopOverVariable = true;
							loopOperator = tailOperator;
						}

						// String[] localNames = null;
						// localNames =
						// ir.getLocalNames(conditionalBranchInstruction.iindex,
						// conditionalBranchInstruction.getUse(0));
						// System.out.println(localNames);
						// localNames =
						// ir.getLocalNames(conditionalBranchInstruction.iindex,
						// conditionalBranchInstruction.getUse(1));
						// System.out.println(localNames);
					}

				}
			}
		}

		// LOOP OVER OTHERS INSTRUCTION LIKE hasMoreElements,
		// hasMoreTokens , NEXT
		if (!loopOverVariable) {

			
			//IF THE LOOP HEAD HAS A INVOKE INSTRUCTION 
			if (headerLastInstruction instanceof SSAInvokeInstruction) {
				SSAInvokeInstruction headerInvokeInstruction = (SSAInvokeInstruction) headerLastInstruction;

				// TODO:CHECK WITH USES -> SEARCH IN INTRUCTIONS IN LOOP
				// BLOCKS WITH A "NEXT" INSTRUNCTION USING THE SAME VARIABLE
				// AT HEADER INVOKE INSTRUCTION(hasMore)
				if (headerInvokeInstruction.toString().contains("hasMore")) {
					loopOverVariable = true;
				}

				// CASE: INTERATOR -> HEAD and .NEXT() in the body
				else if (headerInvokeInstruction.toString().contains("< Application, Ljava/util/List, iterator()Ljava/util/Iterator; >")) {
					int def = headerInvokeInstruction.getDef();

					for (ISSABasicBlock issaBasicBlock : loopBody) {

						for (SSAInstruction ssaInstruction : issaBasicBlock) {

							if (ssaInstruction instanceof SSAInvokeInstruction) {
								SSAInvokeInstruction iinvokeBlock = (SSAInvokeInstruction) ssaInstruction;

								int use = iinvokeBlock.getUse(0);

								if (def == use && iinvokeBlock.toString().contains("< Application, Ljava/util/Iterator, next()Ljava/lang/Object; >")) {
									loopOverVariable = true;
								}
							}

						}
					}

				}
			} else {

				SSAInstruction conditionalLastInstrunction = loopConditionalBlock.getLastInstruction();

				if (conditionalLastInstrunction instanceof SSAConditionalBranchInstruction) {

					// GET CONDITIONAL VARIABLE USE
					SSAConditionalBranchInstruction conditionalBranchInstruction = (SSAConditionalBranchInstruction) conditionalLastInstrunction;
					conditionalBranchInstruction.toString(symbolTable);

					
					//VERIFY USE OF NEXT METHOD ON VARIABLE OF LOOP CONDITIONAL
					for (ISSABasicBlock issaBasicBlock : loopBody) {

						for (SSAInstruction ssaInstruction : issaBasicBlock) {

							if (ssaInstruction instanceof SSAInvokeInstruction) {
								SSAInvokeInstruction iinvokeBlock = (SSAInvokeInstruction) ssaInstruction;
								if(iinvokeBlock.getNumberOfUses()>0) {
									int use = iinvokeBlock.getUse(0);
	
									if ((conditionalBranchInstruction.getUse(0) == use || conditionalBranchInstruction.getUse(0) == iinvokeBlock.getDef())){
											//&& iinvokeBlock.toString().toLowerCase().contains("next")) {
										loopOverVariable = true;
									}
								}
							}

						}
					}

				}
			}
		}

	}

	// Set the complexity of the loop based in its attributes
	private void setLoopComplexity() {

		if (limit != null && !limit.equals("") && !isForeachLoop() && !loopOverVariable) {
			complexity = Complexity.O1;
		} else if (isForeachLoop()) {
			complexity = Complexity.ON;
		} else if (loopOverVariable && loopOperator == null) {
			complexity = Complexity.ON;
		} else if (loopOverVariable && (loopOperator.equals(Operator.ADD) || loopOperator.equals(Operator.SUB))) {
			complexity = Complexity.ON;
		} else if (loopOverVariable && (loopOperator.equals(Operator.DIV) || loopOperator.equals(Operator.MUL))) {
			complexity = Complexity.OLOGN;
		} else if (explicitlyInfiniteLoop) {
			complexity = Complexity.INFINITE;
		}
	}
	
	
	public void setComplexity(Complexity complexity){
		this.complexity = complexity;
	}

	/*
	 * Verify if the loop is a foreach
	 */
	private boolean foreachLoop() {

		boolean isForeachLoop = false;
		boolean isIteratorNextFirstOp = false;
		boolean headerHasNext = false;

		ISSABasicBlock firstBodyBlock = getBodyFirstBlock();
		for (SSAInstruction ssaInstruction : firstBodyBlock) {
			if (ssaInstruction instanceof SSAInvokeInstruction) {
				SSAInvokeInstruction invokeLoopInst = (SSAInvokeInstruction) ssaInstruction;
				if (invokeLoopInst.getDeclaredTarget().toString().contains("Application, Ljava/util/Iterator, next()Ljava/lang/Object")) {
					isIteratorNextFirstOp = true;
				}

			}

			break;
		}

		for (SSAInstruction instruction : loopHeader) {
			if (instruction instanceof SSAInvokeInstruction) {
				SSAInvokeInstruction invokeLoopInst = (SSAInvokeInstruction) instruction;
				if (invokeLoopInst.getDeclaredTarget().toString().contains("Application, Ljava/util/Iterator, hasNext()Z")) {
					headerHasNext = true;
				}
			}
			break;
		}

		isForeachLoop = isIteratorNextFirstOp && headerHasNext;
		return isForeachLoop;
	}

	private ISSABasicBlock getBodyFirstBlock() {

		ISSABasicBlock firstBlock = null;

		for (ISSABasicBlock issaBasicBlock : loopBody) {
			if (firstBlock == null) {
				firstBlock = issaBasicBlock;
			} else if (issaBasicBlock.getNumber() < firstBlock.getNumber()) {
				firstBlock = issaBasicBlock;
			}
		}

		return firstBlock;
	}

	public String informationToString() {

		return profundidade + ";" + complexity + ";" + limit + ";";

	}

	public String getconditionalBranchInterationNumber() {
		return this.limit;
	}

	public boolean isLoopOverVariable() {

		return this.loopOverVariable;

	}

	public BasicBlock getLoopHeader() {
		return loopHeader;
	}

	public void setLoopHeader(BasicBlock loopHeader) {
		this.loopHeader = loopHeader;
	}

	public Set<ISSABasicBlock> getLoopBody() {
		return loopBody;
	}

	public void setLoopBody(Set<ISSABasicBlock> loopBody) {
		this.loopBody = loopBody;
	}

	public List<ISSABasicBlock> getLoopTails() {
		return loopTails;
	}

	public void setLoopTails(List<ISSABasicBlock> loopTails) {
		this.loopTails = loopTails;
	}

	public ISSABasicBlock getLoopConditionalBlock() {
		return loopConditionalBlock;
	}

	public void setLoopConditionalBlock(ISSABasicBlock loopConditionalBlock) {
		this.loopConditionalBlock = loopConditionalBlock;
	}

	public boolean isDoWhileLoop() {
		return isDoWhileLoop;
	}

	public void setDoWhileLoop(boolean isDoWhileLoop) {
		this.isDoWhileLoop = isDoWhileLoop;
	}

	public boolean isExplicitlyInfiniteLoop() {
		return explicitlyInfiniteLoop;
	}

	public void setExplicitlyInfiniteLoop(boolean explicitlyInfiniteLoop) {
		this.explicitlyInfiniteLoop = explicitlyInfiniteLoop;
	}

	public int getProfundidade() {
		return profundidade;
	}

	public void setProfundidade(int profundidade) {
		this.profundidade = profundidade;
	}

	public boolean isForeachLoop() {
		return foreachLoop;
	}
	
	public Complexity getComplexity() {
		return complexity;
	}

}
