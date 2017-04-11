package edu.colorado.walautil.cg;

import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.IClassHierarchy;

/* ZeroXContainerCFABuilder with more precise (but potentially more expensive) context-sensitivity policy */
public class ImprovedZeroXContainerCFABuilder extends MemoryFriendlyZeroXContainerCFABuilder {

  public ImprovedZeroXContainerCFABuilder(IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache,
                                                ContextSelector appContextSelector,
                                                SSAContextInterpreter appContextInterpreter, int instancePolicy) {

    super(cha, options, cache, appContextSelector, appContextInterpreter, instancePolicy);
  }

  @Override
  protected ContextSelector makeContainerContextSelector(IClassHierarchy cha, ZeroXInstanceKeys keys) {
    return new ImprovedContainerContextSelector(cha, keys);
  }
}
