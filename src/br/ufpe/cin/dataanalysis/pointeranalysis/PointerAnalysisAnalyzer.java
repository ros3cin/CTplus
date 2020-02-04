package br.ufpe.cin.dataanalysis.pointeranalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.analysis.typeInference.TypeAbstraction;
import com.ibm.wala.analysis.typeInference.TypeInference;
import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.ReturnValueKey;
import com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSALoadIndirectInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.types.TypeReference;

import br.ufpe.cin.dataanalysis.ComponentOfInterest;
import br.ufpe.cin.datarecommendation.CollectionsTypeResolver;
import br.ufpe.cin.datarecommendation.ICollectionsTypeResolver;
import br.ufpe.cin.debug.Debug;

public class PointerAnalysisAnalyzer {
	
	public void extractPointsToAnalysisInformation(AnalysisScope scope, java.util.List<ComponentOfInterest> componentsOfInterest, IClassHierarchy cha, String output) throws InvalidClassFileException {
		Debug.info("Starting pointer analysis...");
		try {

			java.util.List<Entrypoint> myEntrypoints = new ArrayList<Entrypoint>();
	
			for(IClass currClass : cha) {
				if(isApplicationClass(currClass)) {
					if(isClassOfInterest(currClass.toString(), componentsOfInterest)) {
						for(IMethod method : currClass.getDeclaredMethods()) {
							if(isMethodOfInterest(method, componentsOfInterest)) {
								myEntrypoints.add((new DefaultEntrypoint(method, cha)));
							}
						}
					}
				}
			}
			
			AnalysisOptions options = new AnalysisOptions(scope,myEntrypoints);
			AnalysisCache analysisCache = new AnalysisCacheImpl();
			CallGraphBuilder<?> cgBuilder = Util.makeZeroOneCFABuilder(options, analysisCache, cha, scope);
		
			Debug.info("Creating call graph. This step can take up to 1 hour...");
			CallGraph cg = cgBuilder.makeCallGraph(options,null);
			Debug.info("Call graph creationg ended...");
			Map<CGNode,Boolean> isNodeVisited = new HashMap<CGNode,Boolean>();
			Map<IClass,Boolean> isClassComputed = new HashMap<IClass,Boolean>();
			Iterator<CGNode> allNodes = cg.iterator();
			
			Map<String, AnalyzedClass> pointerResult = new HashMap<String, AnalyzedClass>();

			Debug.info("Gathering pointers and aliases...");
			while(allNodes.hasNext()) {
				CGNode node = allNodes.next();
				String declaringClassName = node.getMethod().getDeclaringClass().getName().toString();
				if(isClassOfInterest(declaringClassName, componentsOfInterest) && !declaringClassName.contains("exception")) {
					walkNodes(node,cg,isNodeVisited,isClassComputed,
							cgBuilder.getPointerAnalysis().getHeapModel(),
							cgBuilder.getPointerAnalysis().getHeapGraph(),
							componentsOfInterest,
							pointerResult);
				}
			}
			
			Map<String, AnalyzedClass> cleanResult = new HashMap<String, AnalyzedClass>();
			for(AnalyzedClass analyzedClass : pointerResult.values()) {
				if(analyzedClass.hasAnyAlias()) {
					cleanResult.put(analyzedClass.getClassName(), analyzedClass);
				}
			}
			
			Debug.info("Writing out the result...");
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			FileWriter fw = new FileWriter(output);
			gson.toJson(cleanResult, fw);
			fw.close();
			
			
			Debug.info("End of pointer analysis.");
	
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (CallGraphBuilderCancelException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void walkNodes(CGNode node, 
			CallGraph cg, 
			Map<CGNode,Boolean> isNodeVisited,
			Map<IClass,Boolean> isClassComputed,
			HeapModel heapModel,
			HeapGraph<?> heapGraph,
			java.util.List<ComponentOfInterest> componentsOfInterest,
			Map<String, AnalyzedClass> pointerResult) throws IOException, InvalidClassFileException {
		
		Iterator<?> childs = cg.getSuccNodes(node);
		isNodeVisited.put(node, true);
		
		
		IClass declaringClass = node.getMethod().getDeclaringClass();
		String declaringClassName = removeBytecodeStyleName(declaringClass.getName().toString());
		
		ICollectionsTypeResolver nameResolver = new CollectionsTypeResolver();
		
		AnalyzedClass analyzedClass = null;
		if((isClassComputed.get(declaringClass) == null) || !(isClassComputed.get(declaringClass))) {
			analyzedClass = new AnalyzedClass(declaringClassName);
			pointerResult.put(declaringClassName, analyzedClass);
			isClassComputed.put(declaringClass,true);
			Collection<IField> staticFields = declaringClass.getDeclaredStaticFields();
			Collection<IField> instanceFields = declaringClass.getDeclaredInstanceFields();
			
			Iterator<NewSiteReference> siteIterator = node.iterateNewSites();
			while(siteIterator.hasNext()) {
				NewSiteReference site = siteIterator.next();
				InstanceKey current = heapModel.getInstanceKeyForAllocation(node, site);
				if(current != null) {
					for(IField instanceField : instanceFields) {
						String typeRef = instanceField.getFieldTypeReference().getName().toString();
						if(nameResolver.isList(typeRef) || nameResolver.isMap(typeRef) || nameResolver.isSet(typeRef)) {
							AnalyzedInstanceField analyzedInstanceField = new AnalyzedInstanceField(analyzedClass, instanceField.getName().toString());
							PointerKey pInstanceField = heapModel.getPointerKeyForInstanceField(current, instanceField);
							getVariablesPointingToTheSamePlace(heapGraph, pInstanceField, analyzedInstanceField, componentsOfInterest, nameResolver);
							analyzedClass.addAnalyzedInstanceField(analyzedInstanceField);
						}
					}
				}
			}
			
			for(IField staticField : staticFields) {
				String typeRef = staticField.getFieldTypeReference().getName().toString();
				if(nameResolver.isList(typeRef) || nameResolver.isMap(typeRef) || nameResolver.isSet(typeRef)) {
					AnalyzedStaticField analyzedStaticField = new AnalyzedStaticField(analyzedClass, staticField.getName().toString());
					PointerKey pStaticField = heapModel.getPointerKeyForStaticField(staticField);
					getVariablesPointingToTheSamePlace(heapGraph, pStaticField, analyzedStaticField, componentsOfInterest, nameResolver);
					analyzedClass.addAnalyzedStaticField(analyzedStaticField);
				}
			}
		} else {
			analyzedClass = pointerResult.get(declaringClassName);
		}
		
		SSAInstruction[] instructions = node.getIR().getInstructions();
		TypeInference ti = TypeInference.make(node.getIR(), true);
		if( (instructions != null) && (instructions.length>0) ) {
			SSAInstruction firstNonNullInstruction = null;
			for(int i = 0; i < instructions.length; i++) {
				if(instructions[i]!=null) {
					firstNonNullInstruction=instructions[i];
					break;
				}
			}
			if(firstNonNullInstruction!=null) {
				AnalyzedMethod analyzedMethod = new AnalyzedMethod(
						analyzedClass, 
						node.getMethod().getName().toString(),
						node.getMethod().getLineNumber(((IBytecodeMethod)node.getMethod()).getBytecodeIndex(firstNonNullInstruction.iindex)),
						node.getMethod().getNumberOfParameters()
				);
				for(int i = 0; i < instructions.length; i++) {
					SSAInstruction instruction = instructions[i];
					PointerKey pLocalField = null;
					int vn = -1;
					boolean isInvokeInstruction = false;
					if(instruction instanceof SSAGetInstruction) {
						SSAGetInstruction getInstruction = (SSAGetInstruction)instruction;
						vn = getInstruction.getDef(0);
						if(vn>-1)
							pLocalField = heapModel.getPointerKeyForLocal(node, getInstruction.getDef(0));
					} else if (instruction instanceof SSAInvokeInstruction) {
						isInvokeInstruction = true;
						SSAInvokeInstruction invokeInstruction = (SSAInvokeInstruction)instruction;
						vn = invokeInstruction.getReturnValue(0);
						if(vn>-1) 
							pLocalField = heapModel.getPointerKeyForLocal(node, vn);
					} else if (instruction instanceof SSAReturnInstruction) {
						SSAReturnInstruction returnInstruction = (SSAReturnInstruction)instruction;
						vn = returnInstruction.getResult();
						if(vn>-1)
							pLocalField = heapModel.getPointerKeyForLocal(node, vn);
					} else if (instruction instanceof SSAPutInstruction) {
						SSAPutInstruction putInstruction = (SSAPutInstruction)instruction;
						vn = putInstruction.getVal();
						if(vn>-1)
							pLocalField = heapModel.getPointerKeyForLocal(node, vn);
					} else if(instruction instanceof SSALoadIndirectInstruction) {
						SSALoadIndirectInstruction loadIndirectInstruction = (SSALoadIndirectInstruction)instruction;
						vn = loadIndirectInstruction.getDef();
						if(vn>-1)
							pLocalField = heapModel.getPointerKeyForLocal(node, vn);
					}
					if((pLocalField != null) && (vn > -1)) {
						String[] variablePossibleNames = node.getIR().getLocalNames(instruction.iindex, vn);
						if (isInvokeInstruction) {
							variablePossibleNames = checkTheVariableNameFurther(
									node,
									instructions,
									instruction,
									vn,
									variablePossibleNames
							);
						}
						if( (variablePossibleNames != null)  && (variablePossibleNames.length > 0) && (variablePossibleNames[0] != null)) {
							TypeAbstraction typeAbstraction = ti.getType(vn);
							if(typeAbstraction != null) {
								TypeReference typeReference = ti.getType(vn).getTypeReference();
								if(typeReference != null) {
									String typeRef = typeReference.getName().toString();
									if(nameResolver.isList(typeRef) || nameResolver.isMap(typeRef) || nameResolver.isSet(typeRef)) {
										AnalyzedLocalVariable analyzedLocalVariable = new AnalyzedLocalVariable(analyzedMethod, variablePossibleNames[0]);
										if(!analyzedMethod.contains(analyzedLocalVariable)) {
											getVariablesPointingToTheSamePlace(heapGraph, pLocalField, analyzedLocalVariable, componentsOfInterest, nameResolver);
											analyzedMethod.addLocalVariable(analyzedLocalVariable);
										}
									}
								}
							}
						}
					}
				}
				
				analyzedClass.addAnalyzedMethod(analyzedMethod);
			
			}
		
		}
		
		while(childs.hasNext()) {
			CGNode next = (CGNode)childs.next();
			String nextChildDeclaringClassName = next.getMethod().getDeclaringClass().getName().toString();
			if(
					((isNodeVisited.get(next) == null) || !isNodeVisited.get(next)) 
					&& isClassOfInterest(nextChildDeclaringClassName, componentsOfInterest)
					&& !nextChildDeclaringClassName.toLowerCase().contains("exception")
			)
				walkNodes(next,cg,isNodeVisited,isClassComputed,heapModel,heapGraph,componentsOfInterest,pointerResult);
		}
	}

	private String[] checkTheVariableNameFurther(CGNode node, SSAInstruction[] instructions, SSAInstruction instruction,
			int vn, String[] variablePossibleNames) {
		if (variablePossibleNames == null || variablePossibleNames.length == 0 || variablePossibleNames[0] == null) {
			for(int j = instruction.iindex; j < instructions.length; j++) {
				if (instructions[j] != null) {
					variablePossibleNames = node.getIR().getLocalNames(j, vn);
					if( (variablePossibleNames != null)  && (variablePossibleNames.length > 0) && (variablePossibleNames[0] != null)) {
						break;
					}
				}
			}
		}
		return variablePossibleNames;
	}

	private void getVariablesPointingToTheSamePlace(HeapGraph<?> heapGraph, 
			PointerKey pKey, 
			IContainAlias aliasContainer,
			List<ComponentOfInterest> componentsOfInterest,
			ICollectionsTypeResolver nameResolver) throws IOException {
		Iterator<?> instanceKeys = heapGraph.getSuccNodes(pKey);
		while(instanceKeys.hasNext()) {
			InstanceKey instanceKey = (InstanceKey)instanceKeys.next();
			Iterator<?> aliasesIterator = heapGraph.getPredNodes(instanceKey);
			while(aliasesIterator.hasNext()) {
				PointerKey alias = (PointerKey) aliasesIterator.next();
				if(!pKey.equals(alias)) {
					if(alias instanceof InstanceFieldKey) {
						InstanceFieldKey fieldKey = (InstanceFieldKey)alias;
						String declaringClassName = fieldKey.getField().getDeclaringClass().getName().toString();
						if(isClassOfInterest(declaringClassName, componentsOfInterest)) {
							AnalyzedAlias analyzedAlias = new AnalyzedAlias(
									removeBytecodeStyleName(declaringClassName), 
									null, 
									fieldKey.getField().getName().toString(),
									null,
									PointerAnalysisVariableType.INSTANCE_FIELD
							);
							aliasContainer.addAlias(analyzedAlias);
						}
					} else if (alias instanceof StaticFieldKey) {
						StaticFieldKey fieldKey = (StaticFieldKey)alias;
						String declaringClassName = fieldKey.getField().getDeclaringClass().getName().toString();
						if(isClassOfInterest(declaringClassName, componentsOfInterest)) {
							AnalyzedAlias analyzedAlias = new AnalyzedAlias(
									removeBytecodeStyleName(declaringClassName), 
									null, 
									fieldKey.getField().getName().toString(),
									null,
									PointerAnalysisVariableType.STATIC_FIELD
							);
							aliasContainer.addAlias(analyzedAlias);
						}
					} else if (alias instanceof LocalPointerKey) {
						CGNode node = ((LocalPointerKey)alias).getNode();
						if(node.getIR() != null) {
							TypeInference ti = TypeInference.make(node.getIR(), true);
							TypeAbstraction typeAbs = ti.getType(((LocalPointerKey) alias).getValueNumber());
							if(typeAbs != null && nameResolver.isInterface(typeAbs.getTypeReference().getName().toString())) {
								SSAInstruction[] instructions = node.getIR().getInstructions();
								boolean indexFound = false;
								for(SSAInstruction instruction : instructions) {
									if(instruction != null) {
										for(int i = 0; (i < instruction.getNumberOfUses()) && !indexFound; i++) {
											if(instruction.getUse(i)==((LocalPointerKey)alias).getValueNumber()) {
												String[] possibleVariableNames = node.getIR().getLocalNames(instruction.iindex, instruction.getUse(i));
												if( (possibleVariableNames!=null) && (possibleVariableNames.length > 0) && (possibleVariableNames[0] != null) ) {
													String declaringClassName = node.getMethod().getDeclaringClass().getName().toString();
													if(isClassOfInterest(declaringClassName, componentsOfInterest)) {
														AnalyzedAlias analyzedAlias = new AnalyzedAlias(
																removeBytecodeStyleName(declaringClassName), 
																node.getMethod().getName().toString(), 
																possibleVariableNames[0], 
																node.getMethod().getNumberOfParameters(),
																PointerAnalysisVariableType.LOCAL_VARIABLE);
														aliasContainer.addAlias(analyzedAlias);
													}
												}
												indexFound = true;
												break;
											}
										}
									}
								}
							}
						}
					} /*else if (alias instanceof ReturnValueKey) {
						ReturnValueKey valueKey = (ReturnValueKey)alias;
						CGNode node = valueKey.getNode();
						String declaringClassName = node.getMethod().getDeclaringClass().getName().toString();
						if(isClassOfInterest(declaringClassName, componentsOfInterest)) {
							AnalyzedAlias analyzedAlias = new AnalyzedAlias(
									removeBytecodeStyleName(node.getMethod().getDeclaringClass().getName().toString()),
									node.getMethod().getName().toString(),
									"method-returned-value",
									node.getMethod().getNumberOfParameters(),
									PointerAnalysisVariableType.LOCAL_VARIABLE);
							aliasContainer.addAlias(analyzedAlias);
						}
					}*/
				}
			}
		}
	}
	
	private boolean isApplicationClass(IClass clazz) {
		String classLoader = clazz.getClassLoader().toString();
		return classLoader.equals("Application");
	}
	private boolean isMethodOfInterest(IMethod method, java.util.List<ComponentOfInterest> componentsOfInterest) {
		boolean result=false;
		if(method!=null) {
			for(ComponentOfInterest component : componentsOfInterest) {
				if(component.checkIfMethodMeetsComponent(method)) {
					result=true;
					break;
				}
			}
		}
		return result;
	}
	private static boolean isClassOfInterest(String className, java.util.List<ComponentOfInterest> componentsOfInterest) {
		boolean result=false;
		if(className!=null) {
			for(ComponentOfInterest component : componentsOfInterest) {
				if(component.checkIfClassNameMeetsComponent(className)) {
					result=true;
					break;
				}
			}
		}
		return result;
	}
	//TODO extract method to a new class, it is also used on JavaCollectionsAnalzyer
	private static String removeBytecodeStyleName(String concreteType) {
		if (concreteType.contains(",")) {
			concreteType = concreteType.substring(concreteType.indexOf(',')+1, concreteType.length()-1);
		}
		if (concreteType.charAt(0) == 'L') {
			concreteType = concreteType.substring(1);
		}
		concreteType = concreteType.replace('/','.');
		return concreteType;
	}
}
