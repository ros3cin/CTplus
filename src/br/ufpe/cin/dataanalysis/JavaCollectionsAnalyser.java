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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

import scala.Option;
import scala.collection.immutable.List;
import scala.collection.immutable.Set;

import com.ibm.wala.analysis.typeInference.TypeAbstraction;
import com.ibm.wala.analysis.typeInference.TypeInference;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.CodeScanner;
import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.ArgumentTypeEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.ref.ReferenceCleanser;

import edu.colorado.walautil.LoopUtil;

//import collection.JavaConversions._;

/**
 * This is a simple example WALA application. This counts the number of parameters to each method in the primordial loader (the J2SE
 * standard libraries), and prints the result.
 * 
 * @author sfink
 */
/**
 * @author BenitoAvell
 *
 */
/**
 * @author BenitoAvell
 *
 */
public class JavaCollectionsAnalyser {

	private static String JAVA_UTIL = "java/util";

	private static String COLLECTION = "Collection";
	private static String LISTS = "List,AbstractList, AbstractSequentialList, ArrayList, AttributeList, CopyOnWriteArrayList, LinkedList, RoleList, RoleUnresolvedList, Stack, Vector";
	private static String MAPS = "Map,AbstractMap, Attributes, AuthProvider, ConcurrentHashMap, ConcurrentSkipListMap, EnumMap, HashMap, Hashtable, IdentityHashMap, LinkedHashMap, PrinterStateReasons, Properties, Provider, RenderingHints, SimpleBindings, TabularDataSupport, TreeMap, UIDefaults, WeakHashMap";
	private static String SETS = "Set,AbstractSet, ConcurrentSkipListSet, CopyOnWriteArraySet, EnumSet, HashSet, JobStateReasons, LinkedHashSet, TreeSet";

	private static String CAMINHO_CSV = "D:/projectsToAnalyzer/resultsWala/";
	
	private static String MAIN = "XSLTBenchOld";
	
	private static String JAVA_LANG_RUNNABLE = "java/lang/Runnable";
	private static String JAVA_LANG_THREAD = "java/lang/Thread";

	private static int LIMITE = 30;

	// valor incial da profundidade da an�lise inter procedural
	private static int VALOR_INICIAL_PROFUNDIDADE = 1;

	// come�a do zero, assumindo que inicialmente n�o existe loop.
	private static final int PROFUNDIDADE_LOOP_INICIAL = 0;

	private static ArrayList<CollectionMethod> listaMetodos;
	private static ArrayList<IClass> threadsRunnableClasses;

	static IClassHierarchy cha;

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

	/**
	 * Use the 'CountParameters' launcher to run this program with the
	 * appropriate classpath
	 */
	public static void main(String[] args) throws IOException, ClassHierarchyException {

		

		
		File projeto = new File("hello.txt");

		
		exibirHora();

		listaMetodos = new ArrayList<CollectionMethod>();
		threadsRunnableClasses = new ArrayList<IClass>();

		try {
			File scopeFile;
			//scopeFile = new File("dat/Project_to_analyse");
			//scopeFile = new File("dat/TesteLoop_jar");
			//scopeFile = new File("dat/bm-xalan");
			scopeFile = new File("dat/bm-tomcat");

			File scopeExclusion;
			//scopeExclusion = new File("dat/AllLibraryExclusion.txt");
		    //scopeExclusion = new File("dat/bm-xalanExclusions.txt");
			scopeExclusion = new File("dat/bm-tomcatExclusions.txt");

			// AnalysisScope scope = getSplashScope();
			AnalysisScope scope = AnalysisScopeReader.readJavaScope(scopeFile.getAbsolutePath(), scopeExclusion,
					JavaCollectionsAnalyser.class.getClassLoader());


			// build a class hierarchy
			System.err.print("Build class hierarchy...");
			cha = ClassHierarchy.make(scope);

			System.err.println("Done");

			// Main Entrypoints
//			CallGraph cg = construirCallGraph(scope, cha);
//
//			// Public methods entrypoints
//			// CallGraph cg = construirCallGraphClassEntrypoints(scope, cha);
//
//			Collection<CGNode> entrypointNodes = cg.getEntrypointNodes();
//
//			for (CGNode cgNode : entrypointNodes) {
//				// System.out.println(cgNode.toString());
//				// TODO: Comentado apenas para imprimir todos os n�s,
//				// DESCOMENTAR.
//				// IClassHierarchy classHierarchy = cgNode.getClassHierarchy();
//
//				IMethod method = cgNode.getMethod();
//
//				ArrayList<LoopBlockInfo> loops = new ArrayList<LoopBlockInfo>();
//				searchMethodsLoopInside(method, VALOR_INICIAL_PROFUNDIDADE, false, null, loops, PROFUNDIDADE_LOOP_INICIAL);
//			} 
			


			// Graph<CGNode> pruneGraph = null;
			// if (cg != null) {
			// pruneGraph = pruneGraph(cg);
			// }

			// ATUAL -> TODO: descomentar
			// for (IClass c: cha) {
			//
			// ReferenceCleanser.registerClassHierarchy(cha);
			//
			// if (isApplicationClass(c)) {
			//
			// for (IMethod method: c.getDeclaredMethods()) {
			//
			// System.out.println(method.toString());
			//
			// if (!method.isAbstract()) {
			// searchMethodsLoopInside(method, VALOR_INICIAL_PROFUNDIDADE,
			// false, null, PROFUNDIDADE_LOOP_INICIAL);
			// }
			//
			// }
			//
			//
			// }
			// }
			// FIM

			// TODO:DESCOMENTAR PARA OS BENCHMARK - TORNAR PARAMETRIZADO
			for (IClass c : cha) {

				ReferenceCleanser.registerClassHierarchy(cha);

				if (isApplicationClass(c)) {

					for (IMethod method : c.getDeclaredMethods()) {
						
						//TOMCAT
						if ((method.toString().contains("org/dacapo/Main")&& method.toString().contains("main"))
								||
								 (method.toString().toLowerCase().contains("catalina/startup/catalina") && method.toString().toLowerCase().contains("start()"))
								|| (method.toString().toLowerCase().contains("catalina/startup/catalina") && method.toString().toLowerCase().contains("load()"))
								 ||(method.toString().toLowerCase().contains("catalina/core/standardserver") && method.toString().toLowerCase().contains("start"))
									|| (method.toString().toLowerCase().contains("catalina/core/standardserver") && method.toString().toLowerCase().contains("initialize"))
									
									//mais detalhado
									||(method.toString().contains("catalina/core/StandardContext") && method.toString().contains("resourcesStart"))
									|| (method.toString().contains("catalina/core/StandardSession") && method.toString().toLowerCase().contains("readobject"))
									|| (method.toString().contains("catalina/util/StringManager") && method.toString().contains("getManager"))
									|| (method.toString().contains("org/apache/commons/httpclient/URI") && method.toString().contains("getCharset"))
									|| (method.toString().contains("org/apache/coyote/http11/Http11NioProtocol") && method.toString().contains("setAttribute"))
									|| (method.toString().contains("org/apache/jk/core/WorkerEnv") && method.toString().contains("addHandler"))
									|| (method.toString().contains("org/apache/naming/resources/DirContextURLStreamHandler") && method.toString().contains("bind")) 
									|| (method.toString().contains("org/apache/naming/resources/ProxyDirContext"))
									|| (method.toString().contains("org/apache/naming/ContextAccessController"))
									|| (method.toString().contains("org/apache/naming/ContextBindings"))
									|| (method.toString().contains("org/apache/naming/NamingContext"))
									|| (method.toString().contains("org/apache/tomcat/util/modeler/modulesMbeansDescriptorsIntrospectionSource"))
									|| (method.toString().contains("org/apache/tomcat/util/res/StringManager"))
									|| (method.toString().contains("org/apache/tomcat/util/threads/ThreadPool") && method.toString().contains("Thread"))
									|| (method.toString().contains("org/apache/tomcat/util/IntrospectionUtils"))
							//	){
								||
								(method.toString().contains("org/dacapo/tomcat/Client")&& method.toString().contains("run"))) {
							
						//XALAN
//						if ((method.toString().contains("org/apache/xalan/CopyOfXSLTBenchOld")
//								&& /*(method.toString().contains("run") ||*/ method.toString().contains("main"))
//								|| (method.toString().contains("org/apache/xalan/processor/StylesheetHandler") && method.toString().contains("startElement"))
//								|| (method.toString().contains("org/apache/xalan/processor/StylesheetHandler") && method.toString().contains("endDocument"))
//								|| (method.toString().contains("org/apache/xalan/processor/StylesheetHandler") && method.toString().contains("startPrefixMapping"))
//								
//								
//								) {

							ArrayList<LoopBlockInfo> loops = new ArrayList<LoopBlockInfo>();
							searchMethodsLoopInside(method, VALOR_INICIAL_PROFUNDIDADE, false, null, loops, PROFUNDIDADE_LOOP_INICIAL);

						}
					}
				}
			}
			
			//Search for the methods inside the method run of thread class or runnable
			while (!threadsRunnableClasses.isEmpty()){
				ArrayList<IClass> classesTemp = new ArrayList<IClass>();
				classesTemp.addAll(threadsRunnableClasses);
				
				for (int i = 0; i < classesTemp.size(); i++) {
					IClass c = classesTemp.get(i);
					IMethod methodRun = callMethods("Run", c.getDeclaredMethods());
					if(methodRun!=null){
						ArrayList<LoopBlockInfo> loops = new ArrayList<LoopBlockInfo>();
						searchMethodsLoopInside(methodRun, VALOR_INICIAL_PROFUNDIDADE, false, null, loops, PROFUNDIDADE_LOOP_INICIAL);
					}
					threadsRunnableClasses.remove(0);
				}
				
			}

		} catch (Exception ex) {

			System.out.println("Mensagem de erro: " + ex.getMessage());
			System.out.println("Mensagem 2 de erro:" + ex.getStackTrace());
			// System.out.println("Projeto com problema: " + projeto.getName());
			// System.out.println("Mensagem de erro: " + ex.getMessage());
			// System.out.println("Mensagem 2 de erro:" + ex.getStackTrace());
			//
		}

		// System.out.println("Terminou projeto: " + caminhoProjeto);
		exibirHora();
		gerarArquivoCsv(criarNomeArquivo(projeto.getName()));
		// }

	}

	private static void exibirHora() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
		System.out.println(sdf.format(new Date()));
	}

	private static ArrayList<String> iniciarPacotes() {
		ArrayList<String> pacotes = new ArrayList<String>();
		pacotes.add(JAVA_UTIL);
		return pacotes;
	}

	// TODO: Create classe FileWriter
	private static void gerarArquivoCsv(String destino) {
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

			for (CollectionMethod elemento : listaMetodos) {
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

	private static ArrayList<String> getClassesNames(IClassHierarchy cha) {
		ArrayList<String> nomes = new ArrayList<String>();
		for (IClass c : cha) {

			if (isApplicationClass(c) && !c.isInterface()) {
				nomes.add(c.getName().toString());

				// for (IMethod m: c.getDeclaredMethods()) {
				// if (m.getName().toString().equals("run")) {
				// System.out.println(c.getName());
				// }
				// nMethods++;
				// nParameters += m.getNumberOfParameters();
				// }
			}
		}
		return nomes;
	}

	private static void searchMethodsLoopInside(IMethod method, int profundidade, boolean isIntoLoop, LoopBlockInfo outerLoop, ArrayList<LoopBlockInfo> loops,
			int loopProfundidade) throws InvalidClassFileException {

		AnalysisCache cache = new AnalysisCache();
		ReferenceCleanser.registerCache(cache);

		if (method == null) {
			System.out.println("method null");
		}

		if (method.isAbstract()) {
			return;
		}

		IR ir;
		try {
			System.out.println("Creating IR...");
			wipeSoftCaches();
			ir = cache.getIRFactory().makeIR(method, Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
			System.out.println("IR OK");

			// ir.toString();

			// LOOP.UTIL
			// Dominators<ISSABasicBlock> dominators =
			// LoopUtil.getDominators(ir);

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
				// boolean isLoopHeader =
				// LoopUtil.isLoopHeader(basicBlockLoopHeader, ir);
				boolean isDoWhileLoop = LoopUtil.isDoWhileLoop(basicBlockLoopHeader, ir);
				// TODO: verivar a exceção Exception in thread "main"
				// java.lang.AssertionError: assertion failed
				boolean explicitlyInfiniteLoop = LoopUtil.isExplicitlyInfiniteLoop(basicBlockLoopHeader, ir);

				// no condBlk; we suspect that BB[SSA:68..69]27 -
				// org.apache.commons.beanutils.converters.AbstractArrayConverter.parseElements(Ljava/lang/String;)Ljava/util/List;
				// is an explicitly infinite loop
				// None.get
				// CHECK IF GET IS NULL or NONE or NONE$ ?
				ISSABasicBlock loopConditional = null;
				if (!loopConditionalBlock.isEmpty()) {
					loopConditional = loopConditionalBlock.get();
				}
				LoopBlockInfo loopBlockInfo = new LoopBlockInfo(basicBlockLoopHeader, javaLoopBody, javaLoopTails, loopConditional, isDoWhileLoop,
						explicitlyInfiniteLoop, ir);
				methodLoops.add(loopBlockInfo);
			}

			// DEBUG
			// boolean loopInside = false;
			// MethodReference invokedMethod = null;

			// v
			// Integer iindexTarget = null;
			// HashMap<Integer, String> collectionsType = new HashMap<Integer,
			// String>();
			// LoopInfo loop = null;
			// for (SSAInstruction instruction: ir.getInstructions()) {

			boolean isRecursive = isMethodRecursive(method, ir.getInstructions());
			
			
			for (int i = 0; i < ir.getInstructions().length; i++) {

				SSAInstruction instruction = ir.getInstructions()[i];

				if (instruction instanceof SSAInvokeInstruction) { // save
																	// method of
																	// collections
					SSAInvokeInstruction invokeIR = (SSAInvokeInstruction) instruction;
					System.out.println(invokeIR);
					MethodReference invokedMethodRef = invokeIR.getDeclaredTarget();

					// for (int i = 1; i <=
					// ir.getSymbolTable().getMaxValueNumber(); i++) {
					// System.err.println(i + " " + ti.getType(i));
					// ti.getStatements().toString();
					// }

					if (invokedMethodRef.getDeclaringClass().toString().contains("Application,")) {

						String concreteType = "";
						if (invokeIR.getNumberOfUses() > 0) {
							TypeInference ti = TypeInference.make(ir, false);
							ti.getType(invokeIR.getUse(0));

							concreteType = ti.getType(invokeIR.getUse(0)).toString();
							if (concreteType.contains(",")) {
								concreteType = concreteType.split(",")[1];
							}
						}

						/*
						 * if (invokeIR.getNumberOfUses() > 0) { if
						 * (collectionsType.containsKey(invokeIR.getUse(0))) {
						 * concreteType =
						 * collectionsType.get(invokeIR.getUse(0))
						 * .split(",")[1]; } }
						 */

						if (invokedMethodRef != null) {
							// boolean loopInside = false;

							// Se o metodo ja nao esta dentro de um loop, checa
							// se ele esta (pega o outer loop)
							// LoopBlockInfo loop = null;

							ISSABasicBlock basicBlockForInstruction = ir.getBasicBlockForInstruction(invokeIR);

							if (!isIntoLoop && methodLoops != null && !methodLoops.isEmpty()) {

								for (LoopBlockInfo loopBlockInfo : methodLoops) {
									if (loopBlockInfo.getLoopBody().contains(basicBlockForInstruction)
											|| loopBlockInfo.getLoopHeader().equals(basicBlockForInstruction)
											|| /*
												 * ((!loopBlockInfo.
												 * isExplicitlyInfiniteLoop())
												 * &&
												 */
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

							// if (!isIntoLoop && loop != null &&
							// loop.getLoopTarget() > invokeIR.iindex) {
							// isIntoLoop = true;
							// loopProfundidade = profundidade;
							// }

							CallSiteReference callSite = invokeIR.getCallSite();

							ArrayList<String> pacotesJava = iniciarPacotes();

							if (isJavaCollectionMethod(callSite.toString(), pacotesJava)) {

								int bcIndex = ((IBytecodeMethod) method).getBytecodeIndex(invokeIR.iindex);
								int invokeLineNumber = method.getLineNumber(bcIndex);

								// method.getLocalVariableName(bcIndex,0);

								CollectionMethod metodo = criarMetodo(callSite.getDeclaredTarget(), ir.getMethod(), concreteType, isIntoLoop, outerLoop,
										invokeLineNumber);

								if (metodo != null) {

									metodo.setProfundidade(profundidade);
									metodo.setOuterLoops(loops);

									Collection<FieldReference> fields = CodeScanner.getFieldsRead(method);

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
										addThreadRunnableClass(ir, invokeIR, invokedMethodRef);
									}
									
									IMethod resolveMethod = cha.resolveMethod(invokedMethodRef);									
									
									// TODO verificar com primordial !!!!
									if (resolveMethod != null && resolveMethod.getDeclaringClass().getClassLoader().toString().equals("Application")/*&&!sameSignature*/) {
										if(!sameSignature(method,resolveMethod)){
											searchMethodsLoopInside(resolveMethod, profundidade + 1, isIntoLoop, outerLoop, loops, loopProfundidade);
										}
									}
								}
							}

						}
					}
				} 
				
//				else if(instruction instanceof SSANewInstruction){
//					
//				}

				// delete the loops in the same depth or deeper
				deleteLoopsOutside(loops, profundidade);

				// Checagem para zerar se estiver dentro do loop
				if (profundidade == loopProfundidade) {
					isIntoLoop = false;
					outerLoop = null;
				}

			}

		} catch (NullPointerException e) {

			System.out.println("NULL - " + method.getName());
		}

	}

	private static void addThreadRunnableClass(IR ir, SSAInvokeInstruction invokeIR, MethodReference invokedMethodRef) {
		System.out.println("start");
		System.out.println(cha.lookupClass(invokedMethodRef.getDeclaringClass()));
		
		
		//ALL THREAD OF CHA
		//Collection<IClass> threads = cha.computeSubClasses(TypeReference.JavaLangThread);
		
		TypeAbstraction type = getInvokeConcreteType(ir, invokeIR.getUse(0));	
		IClass threadConcreteClass = cha.lookupClass(type.getTypeReference());
		System.out.println(type);
		
		int use = invokeIR.getUse(0);
		
		//se for execute
		if(invokeIR.toString().contains("ExecutorService, submit(")){
			use = invokeIR.getUse(1);
		}		
		
		IClass runnableClassOfInvokeInstruction = getRunnableClassOfInvokeInstructions(ir,use);
		
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
	private static IClass getRunnableClassOfInvokeInstructions(IR ir,int use){
		for (int i=0; i < ir.getInstructions().length; i++) {
			
			SSAInstruction instruction = ir.getInstructions()[i];

			if (instruction instanceof SSAInvokeInstruction) {
				if(instruction.getNumberOfUses() > 0 && instruction.getUse(0) == use){
					if(instruction.toString().contains("invokespecial < Application, Ljava/lang/Thread, <init>(Ljava/lang/Runnable;)V >")){
						
						TypeAbstraction invokeConcreteType = getInvokeConcreteType(ir,instruction.getUse(1));
						IClass lookupClass = cha.lookupClass(invokeConcreteType.getTypeReference());
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

		// for (int i = 0; i < invokeIR.getNumberOfUses(); i++) {
		// if(localNames==null){
		// String[] localNamesDef = ir.getLocalNames(invokeIR.iindex,
		// invokeIR.getDef());
		// localNames = ir.getLocalNames(invokeIR.iindex, invokeIR.getUse(i));
		// System.out.println(localNames);
		// }
		// }

		// if(localNames!=null){
		// fieldName = localNames[0];
		// }

		if (localNames == null) { // if isn't a local variable
			for (int j = 0; j < ir.getInstructions().length; j++) {
				SSAInstruction inst = ir.getInstructions()[j];

				// GetInstrutions to get declaradField from
				if (inst instanceof SSAGetInstruction && inst.iindex <= invokeIR.iindex) {
					SSAGetInstruction getInstruction = (SSAGetInstruction) inst;
					FieldReference declaredField = getInstruction.getDeclaredField();
					if (getInstruction.getDef() == invokeIR.getUse(0)) {
						fieldName = declaredField.getName().toString();
						System.out.println(getInstruction.getDef() + " " + declaredField.getName());
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

	private static String criarNomeArquivo(String CaminhoProjeto) {
		String caminhoProjeto[] = CaminhoProjeto.split("/");

		if (caminhoProjeto[caminhoProjeto.length - 1].endsWith(".jar")) {
			String nomeProjeto[] = caminhoProjeto[caminhoProjeto.length - 1].split("\\.");
			return CAMINHO_CSV + nomeProjeto[0] + ".csv";
		} else {
			// criei este else caso esteja analizando uma pasta com .class
			return CAMINHO_CSV + "analise.csv";
		}
	}

	private static CollectionMethod criarMetodo(MethodReference methodReference, IMethod metodoPai, String concreteType, boolean isIntoLoop,
			LoopBlockInfo loop, int invokeLineNumber) {

		String nome = methodReference.getName().toString();
		String pacote[] = methodReference.toString().split(",");

		if (nome.equals("<init>")) {
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

	private static void adicionarMetodo(CollectionMethod metodo) {
		boolean metodoExistente = false;

		if (listaMetodos.size() == 0) {
			listaMetodos.add(metodo);
			metodoExistente = true;
		} else {
			for (int i = 0; i < listaMetodos.size(); i++) {

				if (listaMetodos.get(i).equals(metodo)) {
					listaMetodos.get(i).incrementarOcorrencias();
					metodoExistente = true;
				}

			}
		}
		if (!metodoExistente) {
			listaMetodos.add(metodo);
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
		if (c.getSuperclass().getName().toString().substring(1).equals(JAVA_LANG_THREAD)) { return true; }
		return false;
	}

	/*
	 * Entrypoits -> MainEntrypoints or AllApplicationEntrypoints
	 */
	private static CallGraph construirCallGraph(AnalysisScope scope, IClassHierarchy cha) {

		Iterable<Entrypoint> e = Util.makeMainEntrypoints(scope, cha);
		// All entry points
		// Iterable<Entrypoint> e = new AllApplicationEntrypoints(scope, cha);

		// Iterable<Entrypoint> e = getAllSourceApplicationEntrypoints(cha);

		// encapsulates various analysis options
		AnalysisOptions o = new AnalysisOptions(scope, e);

		CallGraphBuilder builder = Util.makeZeroCFABuilder(o, new AnalysisCache(), cha, scope);
		CallGraph cg = null;
		try {
			cg = builder.makeCallGraph(o, null);
			// System.out.println("Teste");
		} catch (IllegalArgumentException e1) {
			// TODOAuto-generated catch block
			e1.printStackTrace();
		} catch (CallGraphBuilderCancelException e1) {
			e1.printStackTrace();
		}

		return cg;
	}

	/*
	 * Entrypoits -> public methods
	 */
	private static CallGraph construirCallGraphClassEntrypoints(AnalysisScope scope, IClassHierarchy cha) {

		ArrayList<String> classes = getClassesNames(cha);

		System.out.println("Comecar fazer entryPoints ");
		Iterable<Entrypoint> e = Util.makeMainEntrypoints(scope, cha, classes.toArray(new String[classes.size()]));

		//

		// get the entrypoints
		ClassLoaderReference clr = scope.getApplicationLoader();
		final HashSet<Entrypoint> result = HashSetFactory.make();
		for (IClass klass : cha) {

			if (isApplicationClass(klass)) {

				if (klass.getClassLoader().getReference().equals(clr)) {
					Collection<IMethod> allMethods = klass.getDeclaredMethods();
					for (IMethod m : allMethods) {
						if (m.isPublic()) {
							result.add(new DefaultEntrypoint(m, cha));
						}
					}
				}

			}
		}

		//
		System.out.println("Comecar a construir CG ");
		// encapsulates various analysis options
		AnalysisOptions o = new AnalysisOptions(scope, result);
		CallGraphBuilder builder = Util.makeZeroCFABuilder(o, new AnalysisCache(), cha, scope);
		CallGraph cg = null;
		try {
			cg = builder.makeCallGraph(o, null);
			// System.out.println("Teste");
		} catch (IllegalArgumentException e1) {
			// TODOAuto-generated catch block
			e1.printStackTrace();
		} catch (CallGraphBuilderCancelException e1) {
			e1.printStackTrace();
		}

		System.out.println("Terminou o CG ");
		return cg;
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
