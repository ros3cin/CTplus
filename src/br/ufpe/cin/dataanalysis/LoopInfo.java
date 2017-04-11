package br.ufpe.cin.dataanalysis;

import java.util.ArrayList;
import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAGotoInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SymbolTable;

@Deprecated
public class LoopInfo {

	// private boolean isForEach;

	// private int conditionalBranchInterationNumber;

	private SSAConditionalBranchInstruction conditionalInstruntion;
	private SSAGotoInstruction loopInstruction;

	private int loopInstructionIRPosition;
	private IR ir;
	private ArrayList<SSAInstruction> loopInstrutions;
	IMethod method;

	public LoopInfo(SSAGotoInstruction loopInstruction, int loopInstructionIRPosition, IR ir, IMethod method/*
																											 * ,
																											 * SSAConditionalBranchInstruction
																											 * conditionalInstruntion
																											 */) throws InvalidClassFileException {
		// this.conditionalInstruntion = conditionalInstruntion;
		loopInstrutions = new ArrayList<SSAInstruction>();
		this.loopInstruction = loopInstruction;
		this.loopInstructionIRPosition = loopInstructionIRPosition;
		this.ir = ir;
		this.method = method;

		fillLoopInstructions(); // get all instructions inside the loop
		setConditionalBrach();
	}

	public int getconditionalBranchInterationNumber(SymbolTable symbleTable) {

		int nConditional = 0;

		conditionalInstruntion.toString(symbleTable);
		System.out.println(conditionalInstruntion.toString(symbleTable));

		String val2Use = symbleTable.getValueString(conditionalInstruntion.getUse(1));
		String[] val2UseSplit = val2Use.split("#");
		if (val2UseSplit.length > 1) {
			nConditional = Integer.parseInt(val2UseSplit[1]);
		}

		return nConditional;
	}

	public boolean isForeachLoop() {

		boolean isForeachLoop = false;

		for (int j = loopInstructionIRPosition + 1; j < ir.getInstructions().length; j++) {
			if (ir.getInstructions()[j] != null) {
				SSAInstruction instructionLoop = ir.getInstructions()[j];
				if (instructionLoop instanceof SSAInvokeInstruction) {
					SSAInvokeInstruction invokeLoopInst = (SSAInvokeInstruction) instructionLoop;
					if (invokeLoopInst.getDeclaredTarget().toString().contains("Application, Ljava/util/Iterator, next()Ljava/lang/Object")) {
						isForeachLoop = true;
					} else {
						break;
					}

				} else {
					break;
				}
			}
		}

		return isForeachLoop;
	}

	public int getLoopTarget() {
		return loopInstruction.getTarget();
	}

	public SSAGotoInstruction getLoopInstruction() {
		return loopInstruction;
	}

	private void fillLoopInstructions() {
		// ArrayList<SSAInstruction> loopInstrutions = new ArrayList<SSAInstruction>();

		for (int j = loopInstructionIRPosition; j < ir.getInstructions().length; j++) {
			if (ir.getInstructions()[j] != null) {
				// if (ir.getInstructions()[j].iindex <= loopInstruction.getTarget() + 2) { trying get the conditional loop too
				// instructions only in interval of loop target index
				if (ir.getInstructions()[j].iindex <= loopInstruction.getTarget()) {
					loopInstrutions.add(ir.getInstructions()[j]);
				} else {
					break;
				}
			}
		}
	}

	public SSAConditionalBranchInstruction getConditionalInstruntion() {
		return conditionalInstruntion;
	}

	private void setConditionalBrach() throws InvalidClassFileException {
		// Conditional branch is the last instrunction of the loop (target +2)

		for (int j = loopInstructionIRPosition; j < ir.getInstructions().length; j++) {

			SSAInstruction ssaInstruction = ir.getInstructions()[j];
			if (ssaInstruction != null && ssaInstruction.iindex > loopInstruction.getTarget()) {
				if (ssaInstruction instanceof SSAConditionalBranchInstruction) {

					int bcIndex = ((IBytecodeMethod) method).getBytecodeIndex(ssaInstruction.iindex);
					int conditionalLineNumber = method.getLineNumber(bcIndex);

					bcIndex = ((IBytecodeMethod) method).getBytecodeIndex(loopInstruction.iindex);
					int loopLineNumber = method.getLineNumber(bcIndex);

					if (loopLineNumber == conditionalLineNumber) {
						conditionalInstruntion = (SSAConditionalBranchInstruction) ssaInstruction;
					}
				}
			}
		}

		/*
		 * if (!loopInstrutions.isEmpty()) { if (loopInstrutions.get(loopInstrutions.size() - 1) instanceof SSAConditionalBranchInstruction)
		 * { conditionalInstruntion = (SSAConditionalBranchInstruction) loopInstrutions.get(loopInstrutions.size() - 1); } }
		 */
	}
}
