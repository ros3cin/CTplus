/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation. All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package br.ufpe.cin.dataanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ibm.wala.analysis.typeInference.TypeAbstraction;
import com.ibm.wala.analysis.typeInference.TypeInference;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.callgraph.impl.ArgumentTypeEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.DefaultIRFactory;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.ref.ReferenceCleanser;
import com.ibm.wala.util.strings.Atom;

import br.ufpe.cin.commandline.CommandLine;
import br.ufpe.cin.dataanalysis.pointeranalysis.PointerAnalysisAnalyzer;
import br.ufpe.cin.datarecommendation.CollectionsTypeResolver;
import br.ufpe.cin.datarecommendation.ICollectionsTypeResolver;
import br.ufpe.cin.debug.Debug;
import edu.colorado.walautil.LoopUtil;
import picocli.CommandLine.MissingParameterException;
import scala.Option;
import scala.collection.immutable.List;
import scala.collection.immutable.Set;


/**
 * @authors BenitoAvell, Renato
 *
 */
public class JavaCollectionsAnalyser {

	private static String JAVA_UTIL = "java/util";

	private static String COLLECTION = "Collection";
	private static String LISTS = "List,AbstractList, AbstractSequentialList, ArrayList, AttributeList, CopyOnWriteArrayList, LinkedList, RoleList, RoleUnresolvedList, Stack, Vector";
	private static String MAPS = "Map,AbstractMap, Attributes, AuthProvider, ConcurrentHashMap, ConcurrentSkipListMap, EnumMap, HashMap, Hashtable, IdentityHashMap, LinkedHashMap, PrinterStateReasons, Properties, Provider, RenderingHints, SimpleBindings, TabularDataSupport, TreeMap, UIDefaults, WeakHashMap";
	private static String SETS = "Set,AbstractSet, ConcurrentSkipListSet, CopyOnWriteArraySet, EnumSet, HashSet, JobStateReasons, LinkedHashSet, TreeSet";

	private static String DEFAULT_ANALYSIS_OUTPUT_FILE = "analysis.csv";
	
	private static String JAVA_LANG_RUNNABLE = "java/lang/Runnable";
	private static String JAVA_LANG_THREAD = "java/lang/Thread";

	private static int LIMITE = 30;

	// valor incial da profundidade da an�lise inter procedural
	private static int VALOR_INICIAL_PROFUNDIDADE = 1;

	// come�a do zero, assumindo que inicialmente n�o existe loop.
	private static final int PROFUNDIDADE_LOOP_INICIAL = 0;

	private static ArrayList<CollectionMethod> analyzedMethods;
	private static ArrayList<IClass> threadsRunnableClasses;

	// count only if the method of collections is inside the loop
	private static boolean ONLY_LOOP = false;

	/**
	 * Interval which defines the period to clear soft reference caches
	 */
	public final static int WIPE_SOFT_CACHE_INTERVAL = 2500;

	/**
	 * Counter for wiping soft caches
	 */
	private static int wipeCount = 0;
	
	public static void run(String target, String exclusions, String[] packages, String analysisOutputFile, String pointsToAnalysisFile, boolean pointsToAnalysis, boolean analyze) {
		if (StringUtils.isEmpty(analysisOutputFile)) {
			analysisOutputFile = DEFAULT_ANALYSIS_OUTPUT_FILE;
		}
		if (StringUtils.isEmpty(exclusions)) {
			exclusions = "dat/bm-tomcatExclusions.txt";
		}
		
		analyzedMethods = new ArrayList<CollectionMethod>();
		threadsRunnableClasses = new ArrayList<IClass>();
		
		try {
			AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(target, new File(exclusions));
			
			Debug.logger.info("Building class hierarchy...");
			IClassHierarchy classHierarchy = ClassHierarchyFactory.make(scope);
			Debug.logger.info("Done");
			
			java.util.List<ComponentOfInterest> cois = new ArrayList<ComponentOfInterest>();
			if (packages != null) {
				for (String currPackage : packages) {
					cois.add(new ComponentOfInterest(currPackage.replace('.', '/'), null, null));
				}
			}
			
			if (pointsToAnalysis) {
				PointerAnalysisAnalyzer pAnalyzer = new PointerAnalysisAnalyzer();
				pAnalyzer.extractPointsToAnalysisInformation(scope,cois,classHierarchy,pointsToAnalysisFile);
			}
			
			if (analyze) {
				Debug.logger.info("Running analyzer, this may take a few minutes...");
				traverseMethods(classHierarchy,cois);
				Debug.logger.info("Done");
			}
			
			Debug.logger.info(String.format("Generating analysis file at %s",analysisOutputFile));
			generateAnalysisFile(analysisOutputFile);
			Debug.logger.info("Done");
		} catch (Exception e) {
			Debug.logger.error("Exception occurred on pointer analysis", e);
		}
	}

	/**
	 * Use the 'CountParameters' launcher to run this program with the
	 * appropriate classpath
	 * @throws InvalidClassFileException 
	 */
	public static void main(String[] args) throws IOException, ClassHierarchyException, InvalidClassFileException {
		
		final String ESCOPO = "dat/bm-tomcat";
		final String EXCLUSOES = "dat/bm-tomcatExclusions.txt";

		analyzedMethods = new ArrayList<CollectionMethod>();
		threadsRunnableClasses = new ArrayList<IClass>();

		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(
				"C:\\Users\\RENATO\\Documents\\Hasan Apps\\Built jars\\commons-math3-3.4-original.jar",
				new File(EXCLUSOES)
		);

		// build a class hierarchy
		System.out.print("Building class hierarchy...");
		IClassHierarchy cha = ClassHierarchyFactory.make(scope);
		System.out.println("Done");
		
		java.util.List<ComponentOfInterest> tomcatComponentsOfInterest = new ArrayList<ComponentOfInterest>();
		tomcatComponentsOfInterest.add(new ComponentOfInterest("org/apache/catalina", null, null));
		
		java.util.List<ComponentOfInterest> xalanComponentsOfInterest = new ArrayList<ComponentOfInterest>();
		xalanComponentsOfInterest.add(new ComponentOfInterest("org/apache/xalan", null, null));
		
		java.util.List<ComponentOfInterest> nlgservice = new ArrayList<ComponentOfInterest>();
		nlgservice.add(new ComponentOfInterest("NLGService/simplenlg/realiser", "", ""));
		
		java.util.List<ComponentOfInterest> automata = new ArrayList<ComponentOfInterest>();
		automata.add(new ComponentOfInterest("pl/edu/amu", "", ""));
		
		java.util.List<ComponentOfInterest> tomcat85 = new ArrayList<ComponentOfInterest>();
		tomcat85.add(new ComponentOfInterest("org/apache/catalina/util", "", ""));
		
		java.util.List<ComponentOfInterest> xstreamComponentsOfInterest = new ArrayList<ComponentOfInterest>();
		xstreamComponentsOfInterest.add(new ComponentOfInterest("com/thoughtworks/xstream", null, null));
		
		java.util.List<ComponentOfInterest> gsonComponentsOfInterest = new ArrayList<ComponentOfInterest>();
		gsonComponentsOfInterest.add(new ComponentOfInterest("com/google/gson", null, null));
		
		java.util.List<ComponentOfInterest> commonsMath3ComponentsOfInterest = new ArrayList<ComponentOfInterest>();
		commonsMath3ComponentsOfInterest.add(new ComponentOfInterest("org/apache/commons/math3", null, null));
		
		PointerAnalysisAnalyzer pAnalyzer = new PointerAnalysisAnalyzer();
		//pAnalyzer.extractPointsToAnalysisInformation(scope,tomcatComponentsOfInterest,cha);
		
		System.out.print("Running analyzer...");
		traverseMethods(cha,commonsMath3ComponentsOfInterest);
		System.out.println("Done");
		System.out.printf("Program finished at %s\n", Debug.getCurrentTime());
	}
	
	private static void traverseMethods(IClassHierarchy cha, java.util.List<ComponentOfInterest> componentsOfInterest) {
		try {
			for (IClass c : cha) {

				ReferenceCleanser.registerClassHierarchy(cha);

				if (isApplicationClass(c)) {
					Collection<Atom> allowedFields = new ArrayList<Atom>();
					computeAllowedFields(c, allowedFields);
					
					for (IMethod method : c.getDeclaredMethods()) {
						if(isMethodOfInterest(method,componentsOfInterest)) {
							ArrayList<LoopBlockInfo> loops = new ArrayList<LoopBlockInfo>();
							java.util.List<IMethod> alreadyVisited = new ArrayList<IMethod>();
							searchMethodsLoopInside(method, VALOR_INICIAL_PROFUNDIDADE, false, null, loops, PROFUNDIDADE_LOOP_INICIAL,alreadyVisited,componentsOfInterest,allowedFields,cha);
						}

					}
				}
			}
			
			//Search for the methods inside the run method of the thread class or runnable
			while (!threadsRunnableClasses.isEmpty()){
				ArrayList<IClass> classesTemp = new ArrayList<IClass>();
				classesTemp.addAll(threadsRunnableClasses);
				
				for (int i = 0; i < classesTemp.size(); i++) {
					IClass c = classesTemp.get(i);
					Collection<Atom> allowedFields = new ArrayList<Atom>();
					computeAllowedFields(c, allowedFields);
					IMethod methodRun = callMethods("Run", c.getDeclaredMethods());
					if(methodRun!=null){
						ArrayList<LoopBlockInfo> loops = new ArrayList<LoopBlockInfo>();
						java.util.List<IMethod> alreadyVisited = new ArrayList<IMethod>();
						searchMethodsLoopInside(methodRun, VALOR_INICIAL_PROFUNDIDADE, false, null, loops, PROFUNDIDADE_LOOP_INICIAL,alreadyVisited,componentsOfInterest,allowedFields,cha);
					}
					threadsRunnableClasses.remove(0);
				}
				
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}

	/**
	 * Pre computing which class variables can receive a recommendation.
	 * We are leaving out the public variables and the variables that are
	 * returned or passed as parameter to some other method.
	 * 
	 * @param c
	 * @param allowedFields
	 */
	private static void computeAllowedFields(IClass c, Collection<Atom> allowedFields) {
		for(IField field : c.getDeclaredInstanceFields()) {
			if(field.isPrivate()) {
				allowedFields.add(field.getName());
			}
		}
		for(IField field : c.getDeclaredStaticFields()) {
			if(field.isPrivate()) {
				allowedFields.add(field.getName());
			}
		}
		DefaultIRFactory irFactory = new DefaultIRFactory();
		for (IMethod method : c.getDeclaredMethods()) {
			if(method.isAbstract())
				continue;
			IR methodIR = irFactory.makeIR(method, Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
			Map<Integer,Atom> classVariableRef = new HashMap<Integer,Atom>();
			for(SSAInstruction ssaInstruction : methodIR.getInstructions()) {
				if(ssaInstruction instanceof SSAGetInstruction) {
					SSAGetInstruction getInstruction = (SSAGetInstruction)ssaInstruction;
					Atom variableName = getInstruction.getDeclaredField().getName();
					if(allowedFields.contains(variableName)) {
						classVariableRef.put(getInstruction.getDef(0), getInstruction.getDeclaredField().getName());
					}
				} else if(ssaInstruction instanceof SSAReturnInstruction) {
					SSAReturnInstruction returnInstruction = (SSAReturnInstruction)ssaInstruction;
					if(classVariableRef.containsKey(returnInstruction.getUse(0))){
						allowedFields.remove(classVariableRef.get(returnInstruction.getUse(0)));
					}
				} else if(ssaInstruction instanceof SSAInvokeInstruction) {
					SSAInvokeInstruction invokeInstruction = (SSAInvokeInstruction)ssaInstruction;
					int i = invokeInstruction.isStatic() ? 0 : 1;
					for(;i<invokeInstruction.getNumberOfUses(); i++) {
						if(classVariableRef.containsKey(invokeInstruction.getUse(i))){
							allowedFields.remove(classVariableRef.get(invokeInstruction.getUse(i)));
						}
					}
				}
			}
		}
	}
	
	private static boolean isMethodOfInterest(IMethod method, java.util.List<ComponentOfInterest> componentsOfInterest) {
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

	private static ArrayList<String> iniciarPacotes() {
		ArrayList<String> pacotes = new ArrayList<String>();
		pacotes.add(JAVA_UTIL);
		return pacotes;
	}

	// TODO: Create classe FileWriter
	private static void generateAnalysisFile(String destino) {
		try {
			FileWriter writer = new FileWriter(destino);

			writer.append("Pacote");
			writer.append(',');
			writer.append("Concrete Type");
			writer.append(',');
			writer.append("Call Method Name");
			writer.append(',');
			writer.append("Field Name");
			writer.append(',');
			writer.append("Metodo");
			writer.append(',');
			writer.append("Line Code");
			writer.append(',');
			writer.append("Class");
			writer.append(',');
			writer.append("Ocorrencias");
			writer.append(',');
			writer.append("Into Loop");
			writer.append(',');
			writer.append("Loops Info");
			// writer.append(',');
			// writer.append("N Conditional Block");
			writer.append(',');
			writer.append("Inside Recursive Method");
			writer.append('\n');

			for (CollectionMethod elemento : analyzedMethods) {
				writer.append(elemento.getPacote());
				writer.append(',');
				writer.append(elemento.getConcreteType());
				writer.append(',');
				writer.append(elemento.getCallMethodName());
				writer.append(',');
				writer.append(elemento.getFieldName());
				writer.append(',');
				writer.append(elemento.getNome());
				writer.append(',');
				writer.append(Integer.toString(elemento.getInvokeLineNumber()));
				writer.append(',');
				writer.append(elemento.getClasse());
				writer.append(',');
				writer.append(Integer.toString(elemento.getOcorrencias()));
				writer.append(',');
				writer.append(Boolean.toString(elemento.isIntoLoop()));
				writer.append(',');
				writer.append(elemento.loopsToString());
				// writer.append(',');
				// writer.append(Integer.toString(elemento.getConditionalBlockN()));
				writer.append(',');
				writer.append(Boolean.toString(elemento.isInsideRecursive()));
				writer.append('\n');
			}

			// generate whatever data you want

			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void searchMethodsLoopInside(IMethod method, int profundidade, boolean isIntoLoop, LoopBlockInfo outerLoop, ArrayList<LoopBlockInfo> loops,
			int loopProfundidade, java.util.List<IMethod> alreadyVisited,java.util.List<ComponentOfInterest> componentsOfInterest, Collection<Atom> allowedFields, IClassHierarchy classHierarchy) throws InvalidClassFileException {

		alreadyVisited.add(method);
		IAnalysisCacheView cache = new AnalysisCacheImpl();
		//ReferenceCleanser.registerCache(cache);

		if (method == null) {
			return;
		} 
		if(profundidade==1) {
			Debug.println(String.format("Method name:%s.%s",method.getDeclaringClass().getName(),method.getName()));
		}
		

		if (method.isAbstract()) {
			return;
		}

		IR ir;
		try {
			
			wipeSoftCaches();
			ir = cache.getIRFactory().makeIR(method, Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
			

		

			Set<Object> loopHeaders = LoopUtil.getLoopHeaders(ir);
			java.util.Set<Object> loopHeadersSet = scala.collection.JavaConversions.setAsJavaSet(loopHeaders);

			SSACFG cfg = ir.getControlFlowGraph();

			ArrayList<LoopBlockInfo> methodLoops = new ArrayList<LoopBlockInfo>();

			for (Object blockNumber : loopHeadersSet) {
				BasicBlock basicBlockLoopHeader = cfg.getBasicBlock((Integer) blockNumber);

				// Operacoes com o Loop Header

				// Get Loop body
				Set<ISSABasicBlock> loopBody = LoopUtil.getLoopBody(basicBlockLoopHeader, ir);
				java.util.Set<ISSABasicBlock> javaLoopBody = scala.collection.JavaConversions.setAsJavaSet(loopBody);

				// Get loop tails
				List<ISSABasicBlock> loopTails = LoopUtil.getLoopTails(basicBlockLoopHeader, ir);
				java.util.List<ISSABasicBlock> javaLoopTails = new ArrayList<ISSABasicBlock>();
				scala.collection.Iterator<ISSABasicBlock> iterator = loopTails.iterator();
				for (int i = 0; i < loopTails.length(); i++) {
					ISSABasicBlock loopTailBlock = iterator.next();
					javaLoopTails.add(loopTailBlock);
				}

				Option<ISSABasicBlock> loopConditionalBlock = LoopUtil.getLoopConditionalBlock(basicBlockLoopHeader, ir);
				
				boolean isDoWhileLoop = LoopUtil.isDoWhileLoop(basicBlockLoopHeader, ir);
				
				boolean explicitlyInfiniteLoop = false;
				try {
					explicitlyInfiniteLoop = LoopUtil.isExplicitlyInfiniteLoop(basicBlockLoopHeader, ir);
				} catch (Exception e) {

				}

				ISSABasicBlock loopConditional = null;
				if (!loopConditionalBlock.isEmpty()) {
					loopConditional = loopConditionalBlock.get();
				}
				LoopBlockInfo loopBlockInfo = new LoopBlockInfo(basicBlockLoopHeader, javaLoopBody, javaLoopTails, loopConditional, isDoWhileLoop,
						explicitlyInfiniteLoop, ir);
				methodLoops.add(loopBlockInfo);
			}

			boolean isRecursive = isMethodRecursive(method, ir.getInstructions());
			
			Map<Integer,Atom> classVariableRef = new HashMap<Integer,Atom>();
			for (int i = 0; i < ir.getInstructions().length; i++) {

				SSAInstruction instruction = ir.getInstructions()[i];
				if (instruction instanceof SSAGetInstruction) {
					SSAGetInstruction getInstruction = (SSAGetInstruction)instruction;
					classVariableRef.put(getInstruction.getDef(0), getInstruction.getDeclaredField().getName());
				}
				else if (instruction instanceof SSAInvokeInstruction) { // save
																	// method of
																	// collections
					SSAInvokeInstruction invokeIR = (SSAInvokeInstruction) instruction;
					//System.out.println(invokeIR);
					MethodReference invokedMethodRef = invokeIR.getDeclaredTarget();


					if (invokedMethodRef.getDeclaringClass().toString().contains("Application,")) {

						String concreteType = "";
						if (invokeIR.getNumberOfUses() > 0) {
							TypeInference ti = TypeInference.make(ir, false);
							ti.getType(invokeIR.getUse(0));

							concreteType = ti.getType(invokeIR.getUse(0)).toString();
							if (concreteType.contains(",")) {
								concreteType = concreteType.substring(concreteType.indexOf(',')+1, concreteType.length()-1);
							}
						}
			

						if (invokedMethodRef != null) {

							ISSABasicBlock basicBlockForInstruction = ir.getBasicBlockForInstruction(invokeIR);

							if (!isIntoLoop && methodLoops != null && !methodLoops.isEmpty()) {

								for (LoopBlockInfo loopBlockInfo : methodLoops) {
									if (loopBlockInfo.getLoopBody().contains(basicBlockForInstruction)
											|| loopBlockInfo.getLoopHeader().equals(basicBlockForInstruction)
											|| 
											(loopBlockInfo.getLoopConditionalBlock() != null && loopBlockInfo.getLoopConditionalBlock().equals(
													basicBlockForInstruction))) {
										isIntoLoop = true;
										loopProfundidade = profundidade;
										outerLoop = loopBlockInfo;
										outerLoop.setProfundidade(loopProfundidade);
										loops.add(0, outerLoop);
									}
								}

							} else { // Se n�o ele verifica se esta dentro de um
										// inner loop, e insere

								for (LoopBlockInfo loopBlockInfo : methodLoops) {
									if (loopBlockInfo.getLoopBody().contains(basicBlockForInstruction)) {
										loopBlockInfo.setProfundidade(profundidade);
										loops.add(loopBlockInfo);
									}
								}
							}


							CallSiteReference callSite = invokeIR.getCallSite();

							ArrayList<String> pacotesJava = iniciarPacotes();

							if (isJavaCollectionMethod(callSite.toString(), pacotesJava)) {

								int bcIndex = ((IBytecodeMethod) method).getBytecodeIndex(invokeIR.iindex);
								int invokeLineNumber = method.getLineNumber(bcIndex);

								// method.getLocalVariableName(bcIndex,0);
								CollectionMethod metodo = null;
								
								boolean isAllowed = true;
								
								/*if(classVariableRef.containsKey(invokeIR.getUse(0))) {
									isAllowed = allowedFields.contains(classVariableRef.get(invokeIR.getUse(0)));
								}*/
								
								if(isAllowed) {
									metodo = criarMetodo(callSite.getDeclaredTarget(), ir.getMethod(), concreteType, isIntoLoop, outerLoop,
										invokeLineNumber,ir,invokeIR);
								}

								if (metodo != null) {

									metodo.setProfundidade(profundidade);
									metodo.setOuterLoops(loops);

									String fieldName = "";
									fieldName = getFieldName(ir, invokeIR, fieldName);
									metodo.setFieldName(fieldName);
									metodo.setInsideRecursiveMethod(isRecursive);

									if (!ONLY_LOOP) {
										adicionarMetodo(metodo);
									} else if (isIntoLoop) {
										adicionarMetodo(metodo);
									}									
									
								}
							} else {

								if (callSite.getDeclaredTarget().getDeclaringClass().getClassLoader().toString().equals("Application classloader\n")
										&& profundidade < LIMITE) {
									
									//verify if method is a start() from thread									
									if(invokedMethodRef.toString().contains("start()") || invokedMethodRef.toString().contains("ExecutorService, submit(")){
										
										//Add to the list if the method start is from a thread/runnable class
										addThreadRunnableClass(ir, invokeIR, invokedMethodRef,classHierarchy);
									}
									
									IMethod resolveMethod = classHierarchy.resolveMethod(invokedMethodRef);									
									// TODO verificar com primordial !!!!
									if (resolveMethod != null && resolveMethod.getDeclaringClass().getClassLoader().toString().equals("Application")/*&&!sameSignature*/) {
										if(!alreadyVisited.contains(resolveMethod) && isMethodOfInterest(method, componentsOfInterest)){
											searchMethodsLoopInside(resolveMethod, profundidade + 1, isIntoLoop, outerLoop, loops, loopProfundidade,alreadyVisited,componentsOfInterest,allowedFields,classHierarchy);
										}
									}
								}
							}

						}
					}
				} 


				// delete the loops in the same depth or deeper
				deleteLoopsOutside(loops, profundidade);

				// Checagem para zerar se estiver dentro do loop
				if (profundidade == loopProfundidade) {
					isIntoLoop = false;
					outerLoop = null;
				}

			}

		} catch (NullPointerException e) {

			e.printStackTrace();
		}

	}

	private static void addThreadRunnableClass(IR ir, SSAInvokeInstruction invokeIR, MethodReference invokedMethodRef, IClassHierarchy classHierarchy) {
		System.out.println("start");
		System.out.println(classHierarchy.lookupClass(invokedMethodRef.getDeclaringClass()));
		
		
		//ALL THREAD OF CHA
		//Collection<IClass> threads = cha.computeSubClasses(TypeReference.JavaLangThread);
		
		TypeAbstraction type = getInvokeConcreteType(ir, invokeIR.getUse(0));	
		IClass threadConcreteClass = classHierarchy.lookupClass(type.getTypeReference());
		System.out.println(type);
		
		int use = invokeIR.getUse(0);
		
		//se for execute
		if(invokeIR.toString().contains("ExecutorService, submit(")){
			use = invokeIR.getUse(1);
		}		
		
		IClass runnableClassOfInvokeInstruction = getRunnableClassOfInvokeInstructions(ir,use,classHierarchy);
		
		IClass concreteClass = null;
		
		if(runnableClassOfInvokeInstruction!=null && 
				(implementsInterface(JAVA_LANG_RUNNABLE, runnableClassOfInvokeInstruction.getAllImplementedInterfaces())
				 || implementsInterfaceByHierarchy(JAVA_LANG_RUNNABLE, runnableClassOfInvokeInstruction.getSuperclass()))){
			
			concreteClass = runnableClassOfInvokeInstruction;
			
		} else if (extendsThread(threadConcreteClass)) {
			concreteClass = threadConcreteClass;
		}
		
		if(concreteClass != null && !threadsRunnableClasses.contains(concreteClass)){
			threadsRunnableClasses.add(concreteClass);
		}
		
	}
	
	//Get the runnable class of a instruction of thread start, thats pass a runnable interface in contructor
	//Example of instruction:
	//19   invokespecial < Application, Ljava/lang/Thread, <init>(Ljava/lang/Runnable;)V > v11,v12 @41 exception:v14(line 23)
	//22   invokevirtual < Application, Ljava/lang/Thread, start()V > v11 @46 exception:v15(line 24) {11=[t2]}
	private static IClass getRunnableClassOfInvokeInstructions(IR ir,int use, IClassHierarchy classHierarchy){
		for (int i=0; i < ir.getInstructions().length; i++) {
			
			SSAInstruction instruction = ir.getInstructions()[i];

			if (instruction instanceof SSAInvokeInstruction) {
				if(instruction.getNumberOfUses() > 0 && instruction.getUse(0) == use){
					if(instruction.toString().contains("invokespecial < Application, Ljava/lang/Thread, <init>(Ljava/lang/Runnable;)V >")){
						
						TypeAbstraction invokeConcreteType = getInvokeConcreteType(ir,instruction.getUse(1));
						IClass lookupClass = classHierarchy.lookupClass(invokeConcreteType.getTypeReference());
						return lookupClass;
					}
				}
			}
			
		}
		return null;
	}
	

	//Get concrete type of the invoke instruction object
	private static TypeAbstraction getInvokeConcreteType(IR ir,int use) {
		
		TypeInference ti = TypeInference.make(ir, false);
//		String concreteType;
//		ti.getType(invokeIR.getUse(0));
//
//		concreteType = ti.getType(invokeIR.getUse(0)).toString();
//												
//		if (concreteType.contains(":")) {
//			concreteType = concreteType.split(":")[1];
//		}
		return ti.getType(use);
	}
	
	
	
	
	//Verify if the method is recursive
	private static boolean isMethodRecursive(IMethod method,SSAInstruction[] instructions){
		
		for (int i = 0; i < instructions.length; i++) {
			if (instructions[i] instanceof SSAInvokeInstruction) {
				
				SSAInvokeInstruction invokeIR = (SSAInvokeInstruction) instructions[i];
				MethodReference invokedMethodRef = invokeIR.getDeclaredTarget();
				
				if(method.getSignature().equals(invokedMethodRef.getSignature())){
					return true;
				}
			}
		}
		
		return false;
	}
	
	//Verify if the methods have the same signature 
	private static boolean sameSignature(IMethod method1,IMethod method2){
		
		boolean isRecursive = false;
		if (method1.getSignature().equals(method2.getSignature())) {
			isRecursive = true;
		}
		
		return isRecursive;
		
	}

	// Get the field name of variable that did a invoke instruction
	private static String getFieldName(IR ir, SSAInvokeInstruction invokeIR, String fieldName) {

		// invokeIR.getNumberOfUses();
		String[] localNames = null;
		localNames = ir.getLocalNames(invokeIR.iindex, invokeIR.getUse(0));
		
		if (localNames == null) { // if isn't a local variable
			for (int j = 0; j < ir.getInstructions().length; j++) {
				SSAInstruction inst = ir.getInstructions()[j];

				// GetInstrutions to get declaradField from
				if (inst instanceof SSAGetInstruction && inst.iindex <= invokeIR.iindex) {
					SSAGetInstruction getInstruction = (SSAGetInstruction) inst;
					FieldReference declaredField = getInstruction.getDeclaredField();
					if (getInstruction.getDef() == invokeIR.getUse(0)) {
						fieldName = declaredField.getName().toString();
						Debug.println(getInstruction.getDef() + " " + declaredField.getName());
						break;
					}
				}
			}
		} else {
			fieldName = localNames[0];
		}
		return fieldName;
	}

	/*
	 * Delete the loops outside of the method depth
	 */
	private static ArrayList<LoopBlockInfo> deleteLoopsOutside(ArrayList<LoopBlockInfo> loops, int profundidade) {

		int size = loops.size();
		for (int i = size - 1; i >= 0; i--) {
			if (loops.get(i).getProfundidade() >= profundidade) {
				loops.remove(i);
			} else {
				break;
			}
		}

		return loops;
	}
	
	private static boolean isCollectionReturnedOrPassedAsParameter(IMethod scope, SSAInvokeInstruction invks, IR methodIR) {
		int collectionReference = invks.getUse(0);//the this parameter
		boolean returnValue = false;
		
		for(int i = scope.isStatic()?0:1; i < methodIR.getNumberOfParameters(); i++) {
			if(collectionReference == methodIR.getParameter(i))
				returnValue = true;
		}
		
		SSAInstruction[] methodInstructions = methodIR.getInstructions();
		for(SSAInstruction instruction : methodInstructions) {
			if(instruction instanceof SSAReturnInstruction) {
				SSAReturnInstruction returnInstruction = (SSAReturnInstruction)instruction;
				if(!returnInstruction.returnsVoid()) {
					if(returnInstruction.getUse(0)==collectionReference)
						returnValue = true;
				}
			} else if (instruction instanceof SSAInvokeInstruction) {
				SSAInvokeInstruction invokeInstruction = (SSAInvokeInstruction)instruction;
				if(invokeInstruction.getNumberOfUses() > 0) {
					if(!invokeInstruction.isStatic()) {
						for(int i = 1; i < invokeInstruction.getNumberOfUses(); i++) {
							if(invokeInstruction.getUse(i) == collectionReference) {
								returnValue = true;
								break;
							}
						}
					} else {
						for(int i = 0; i < invokeInstruction.getNumberOfUses(); i++) {
							if(invokeInstruction.getUse(i) == collectionReference) {
								returnValue = true;
								break;
							}
						}
					}
				}
			}
		}
		
		return returnValue;
	}
	
	private static CollectionMethod criarMetodo(MethodReference methodReference, IMethod metodoPai, String concreteType, boolean isIntoLoop,
			LoopBlockInfo loop, int invokeLineNumber, IR ir, SSAInvokeInstruction invks) {

		String nome = methodReference.getName().toString();
		String pacote[] = methodReference.toString().split(",");
		
		nome = treatMethodSignature(methodReference, metodoPai, ir, invks, nome, concreteType,loop);

		if (nome.equals("<init>") || isCollectionReturnedOrPassedAsParameter(metodoPai,invks,ir)) {
			return null;
		}

		CollectionMethod metodo = new CollectionMethod();
		metodo.setNome(nome);
		metodo.setClasse(metodoPai.getDeclaringClass().toString().split(",")[1]);
		metodo.setCallMethodName(metodoPai.getName().toString());
		metodo.setPacote(pacote[1].substring(2));
		metodo.setOcorrencias(1);
		metodo.setIntoLoop(isIntoLoop);
		metodo.setConcreteType(concreteType);
		metodo.setInvokeLineNumber(invokeLineNumber);
		if (loop != null && loop.getLoopConditionalBlock() != null) {
			metodo.setConditionalBlock(loop.getLoopConditionalBlock().toString());
			metodo.setConditionalBlockN(loop.getconditionalBranchInterationNumber());
		}
		// if (isIntoLoop && loop != null) {
		// if (loop.getConditionalInstruntion() != null) {
		// System.out.println(loop.getConditionalInstruntion());
		// }
		// metodo.setInsideForeach(loop.isForeachLoop());
		// }

		return metodo;

	}

	/**
	 * Heuristic - the get is sequential if its parameter is a variable declared on a loop header or tail
	 * @param ir
	 * @param loopBlock
	 * @param invks
	 * @return true if, according to this heuristic, the get is sequential
	 */
	private static boolean isSequentialGet(IR ir, LoopBlockInfo loopBlock, SSAInvokeInstruction invks) {
		boolean isSequential = false;
		int parameterValueNumber = invks.getUse(1);
		BasicBlock loopHeaderBlock = loopBlock.getLoopHeader();

		for (SSAInstruction instruction :loopHeaderBlock.getAllInstructions()) {
			int numOfDefs = instruction.getNumberOfDefs();
			for(int i = 0; i < numOfDefs; i++) {
				if(instruction.getDef(i) == parameterValueNumber) {
					isSequential= true;
					break;
				}
			}
			if(isSequential)
				break;
		}
		
		if(!isSequential) {
			for(ISSABasicBlock basicBlock : loopBlock.getLoopTails()) {
				Iterator<SSAInstruction> instructionIterator = basicBlock.iterator();
				while(instructionIterator.hasNext()) {
					SSAInstruction instruction = instructionIterator.next();
					int numOfDefs = instruction.getNumberOfDefs();
					for(int i = 0; i < numOfDefs; i++) {
						if(instruction.getDef(i) == parameterValueNumber) {
							isSequential= true;
							break;
						}
					}
					if(isSequential)
						break;
				}
				if(isSequential)
					break;
			}
		}
		
		return isSequential;
	}
	
	private static String treatMethodSignature(MethodReference methodReference, IMethod metodoPai, IR ir,
			SSAInvokeInstruction invks, String nome, String concreteType, LoopBlockInfo loopBlock) {
		ICollectionsTypeResolver nameResolver = new CollectionsTypeResolver();
		TypeInference ti = TypeInference.make(ir, true);
		if(nameResolver.isList(concreteType)){
			if(nome.equals("add")){
				if(methodReference.getNumberOfParameters()==1)
					nome="add(value)";
				else {
					boolean isStartingIndex=ir.getSymbolTable().isConstant(invks.getUse(1))&& ir.getSymbolTable().getValue(invks.getUse(1)).toString().equals("#0");
					
					if(isStartingIndex) {
						nome="add(starting-index;value)";
					} else
						nome="add(middle-index;value)";
				}
			}
			else if (nome.equals("remove")){
				if(ti.getType(invks.getUse(1)).toString().equals("int")){
					boolean isStartingIndex=ir.getSymbolTable().isConstant(invks.getUse(1))&& ir.getSymbolTable().getValue(invks.getUse(1)).toString().equals("#0");
					if(isStartingIndex) {
						nome="remove(starting-index)";
					} else
						nome="remove(middle-index)";
				} else {
					nome = "remove(value)";
				}
				
			}
			else if (nome.equals("get")) {
				boolean isSequentialGet= loopBlock != null? isSequentialGet(ir,loopBlock,invks) : false;
				nome = isSequentialGet ? "sequentialGet" : "randomGet" ;
			}
		} else if (nameResolver.isMap(concreteType)) {
			if(nome.equals("put")) {
				nome="put(key;value)";
			} else if (nome.equals("remove")) {
				nome="remove(key)";
			} else if (nome.equals("get")) {
				nome="iterator";
			}
		} else if (nameResolver.isSet(concreteType)) {
			if(nome.equals("add")) {
				nome = "add(value)";
			} else if (nome.equals("remove")) {
				nome = "remove(key)";
			} else if (nome.equals("get")) {
				nome="iterator";
			}
		} 
		
		return nome;
	}

	private static void adicionarMetodo(CollectionMethod metodo) {
		boolean metodoExistente = false;

		if (analyzedMethods.size() == 0) {
			analyzedMethods.add(metodo);
			metodoExistente = true;
		} else {
			for (int i = 0; i < analyzedMethods.size(); i++) {

				if (analyzedMethods.get(i).equals(metodo)) {
					analyzedMethods.get(i).incrementarOcorrencias();
					metodoExistente = true;
				}

			}
		}
		if (!metodoExistente) {
			analyzedMethods.add(metodo);
		}
	}

	private static boolean isJavaCollectionMethod(String nomePacote, ArrayList<String> listaPacotes) {

		String partePacote[] = nomePacote.split(",");
		String pacote = partePacote[1] != null ? partePacote[1].substring(2) : null;

		if (!pacote.contains("/")) {
			return false;
		}

		String pacoteAuxiliar[] = pacote.split("/");
		String pacotePrincipal = pacoteAuxiliar[0] + "/" + pacoteAuxiliar[1];

		if (listaPacotes.contains(pacotePrincipal)
				&& (COLLECTION.contains(pacoteAuxiliar[2]) || LISTS.contains(pacoteAuxiliar[2]) || MAPS.contains(pacoteAuxiliar[2]) || SETS
						.contains(pacoteAuxiliar[2]))) {
			return true;
		}

		return false;

	}
	
	private static boolean extendsThread(IClass c) {
		boolean result=false;
		if(c!=null) {
			if(c.getSuperclass()!=null) {
				result = c.getSuperclass().getName().toString().substring(1).equals(JAVA_LANG_THREAD);
			}
		}
		return result;
	}

	

	/*
	 * source entrypoints
	 */
	public static HashSet<Entrypoint> getAllSourceApplicationEntrypoints(final IClassHierarchy cha) {
		if (cha == null)
			throw new IllegalArgumentException("cha is null");

		final HashSet<Entrypoint> result = HashSetFactory.make();

		for (IClass klass : cha)
			if (!klass.isInterface() && isSourceClassLoader(klass))
				for (IMethod method : klass.getDeclaredMethods())
					if (!method.isAbstract())
						result.add(new ArgumentTypeEntrypoint(method, cha));
		return result;
	}

	private static boolean isSourceClassLoader(IClass klass) {
		return klass.getClassLoader().getReference().equals(JavaSourceAnalysisScope.SOURCE);
	}

	/**
	 * Retorna true se o metodo for da aplicacao, false caso contrario.
	 */
	public static boolean isApplicationMethod(IMethod method) {

		String classLoader = method.getDeclaringClass().getClassLoader().toString();
		return classLoader.equals("Application");

	}

	/**
	 * Retorna true se o metodo for da aplicacao, false caso contrario.
	 */
	public static boolean isApplicationClass(IClass c) {

		String classLoader = c.getClassLoader().toString();
		return classLoader.equals("Application");

	}

	public static boolean implementsInterface(String interfaceName, Collection loadedSuperInterfaces) {

		if (loadedSuperInterfaces != null) {
			for (Iterator it3 = loadedSuperInterfaces.iterator(); it3.hasNext();) {
				final IClass iface = (IClass) it3.next();

				if (iface.isInterface()) {
					String nomeAuxiliar = iface.toString().replace("<", "");
					nomeAuxiliar = nomeAuxiliar.replace(">", "");
					String parteInterface[] = nomeAuxiliar.split(",");
					String interfacePacote = parteInterface[1].substring(1);

					if (interfacePacote.equals(interfaceName)) {
						return true;
					}

				}

			}
		}

		return false;
	}

	public static boolean implementsInterfaceByHierarchy(String interfaceName, IClass superClass) {

		if (superClass != null) {

			Collection loadedSuperInterfaces = superClass.getAllImplementedInterfaces();

			for (Iterator it3 = loadedSuperInterfaces.iterator(); it3.hasNext();) {
				final IClass iface = (IClass) it3.next();

				if (iface.isInterface()) {
					String nomeAuxiliar = iface.toString().replace("<", "");
					nomeAuxiliar = nomeAuxiliar.replace(">", "");
					String parteInterface[] = nomeAuxiliar.split(",");
					String interfacePacote = parteInterface[1].substring(1);

					if (interfacePacote.equals(interfaceName)) {
						return true;
					}

				}

			}

			implementsInterfaceByHierarchy(interfaceName, superClass.getSuperclass());
		}

		return false;
	}

	public static IMethod callMethods(String methodName, Collection methods) {

		if (methods != null) {
			for (Iterator it3 = methods.iterator(); it3.hasNext();) {
				final IMethod imethod = (IMethod) it3.next();
				if (imethod.getName().toString().equalsIgnoreCase(methodName)) {
					return imethod;
				}

			}
		}

		return null;
	}

	public static void listMethodCalls(IMethod method) {

		if (method != null) {
			System.out.println(method.getDescriptor());
		}
	}

	private static Graph<CGNode> pruneGraph(CallGraph callgraph) {
		Graph<CGNode> finalGraph = GraphSlicer.prune(callgraph, new Predicate<CGNode>() {

			@Override
			public boolean test(CGNode t) {
				// aqui dentro podemos incluir o criterio para remover os nos
				// indesejados do grafo
				// pode-se fazer um filtro para classes relevantes

				// cada CGNode eh representado assim:
				// Node: < Application, Lmain/A, addQuotes()V > Context:
				// Everywhere
				// Application eh o ClassLoader
				// Lmain/A eh o pacote e nome da classe
				// addQuotes()V eh o metodo chamado e o V significa void
				//

				// Subject Search nao pode remover o usuario
				// policy deleteUser:
				// Search auth- UserRepository {deleteUser()};
				IMethod meth = t.getMethod();
				t.iterateCallSites();
				String classLoader = meth.getDeclaringClass().getClassLoader().toString();
				return classLoader.equals("Application");
			}
		});
		return finalGraph;
	}

	public static ArrayList<String> carregarEnderecoProjeto(String listaDiretorioProjetos) throws IOException {

		File fileSourceFolder = new File(listaDiretorioProjetos);

		BufferedReader in = new BufferedReader(new FileReader(fileSourceFolder));

		String str;

		ArrayList<String> caminhoProjetos = new ArrayList<String>();

		while ((str = in.readLine()) != null) {
			caminhoProjetos.add(str);
		}

		in.close();

		return caminhoProjetos;
	}

	private static void wipeSoftCaches() {
		wipeCount++;
		if (wipeCount >= WIPE_SOFT_CACHE_INTERVAL) {
			wipeCount = 0;
			ReferenceCleanser.clearSoftCaches();
		}
	}

}
