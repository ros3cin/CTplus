package edu.colorado.walautil.cg;

import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXContainerCFABuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.MutableMapping;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.OrdinalSet;

/** variant of ZeroX... with less of a memory footprint because it does not retain a reference to its callgraph builder */
public class MemoryFriendlyZeroXContainerCFABuilder extends ZeroXContainerCFABuilder {

  public MemoryFriendlyZeroXContainerCFABuilder(IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache,
                                                ContextSelector appContextSelector,
                                                SSAContextInterpreter appContextInterpreter, int instancePolicy) {

    super(cha, options, cache, appContextSelector, appContextInterpreter, instancePolicy);
  }

  static final class PropSystem extends PropagationSystem {
    private final PointerKeyFactory pointerKeyFactory;
    private final InstanceKeyFactory instanceKeyFactory;

    public PropSystem(CallGraph cg, PointerKeyFactory pointerKeyFactory, InstanceKeyFactory instanceKeyFactory) {
      super(cg, pointerKeyFactory, instanceKeyFactory);
      this.pointerKeyFactory = pointerKeyFactory;
      this.instanceKeyFactory = instanceKeyFactory;
    }

    @Override
    public PointerAnalysis<InstanceKey> makePointerAnalysis(PropagationCallGraphBuilder builder) {
      return new PointerAnalysisI(cg, pointsToMap, instanceKeys, pointerKeyFactory, instanceKeyFactory);
    }

  }

  // variant of PointerAnalysisImpl thtat does not retain a pointer to its CFA builder
  static final class PointerAnalysisI extends PointerAnalysisImpl {
    private final  PointsToMap pointsToMap;
    private final IClassHierarchy cha;

    public PointerAnalysisI(CallGraph cg, PointsToMap pointsToMap, MutableMapping<InstanceKey> instanceKeys,
                            PointerKeyFactory pointerKeys, InstanceKeyFactory iKeyFactory) {
      super(null, cg, pointsToMap, instanceKeys, pointerKeys, iKeyFactory);
      this.pointsToMap = pointsToMap;
      this.cha = cg.getClassHierarchy();
    }

    @Override
    // the original implementation of this method uses builder
    public IClassHierarchy getClassHierarchy() {
      return cha;
    }

    @Override
    public OrdinalSet<InstanceKey> getPointsToSet(PointerKey key) {
      if (pointsToMap.isImplicit(key)) {
        // TODO: could fix this case if we wanted to--this is the case that uses builder in the original implementation
        return computeImplicitPointsToSet(key);
      }
      return super.getPointsToSet(key);
    }

    protected static boolean isConstantRef(SymbolTable symbolTable, int valueNumber) {
      if (valueNumber == -1) {
        return false;
      }
      if (symbolTable.isConstant(valueNumber)) {
        Object v = symbolTable.getConstantValue(valueNumber);
        return (!(v instanceof Number));
      } else {
        return false;
      }
    }

    protected boolean contentsAreInvariant(SymbolTable symbolTable, DefUse du, int valueNumber) {
      if (isConstantRef(symbolTable, valueNumber) || valueNumber == -1) {
        return true;
      } else {
        SSAInstruction def = du.getDef(valueNumber);
        if (def instanceof SSANewInstruction) {
          return true;
        } else {
          return false;
        }
      }
    }

    static class PointsToSetVistor extends ImplicitPointsToSetVisitor {
      public PointsToSetVistor(PointerAnalysisImpl pa, LocalPointerKey lpk) {
        super(pa, lpk);
      }

      public OrdinalSet<InstanceKey> getPointsToSet() { return pointsToSet; }
    }

    private static int findOrCreateIndexForInstanceKey(InstanceKey key, MutableMapping<InstanceKey> instanceKeys) {
      int result = instanceKeys.getMappedIndex(key);
      if (result == -1) {
        result = instanceKeys.add(key);
      }
      return result;
    }

    protected static InstanceKey[] getInvariantContents(SymbolTable symbolTable, DefUse du, CGNode node,
                                                        int valueNumber, HeapModel hm, boolean ensureIndexes,
                                                        MutableMapping<InstanceKey> instanceKeys) {
      InstanceKey[] result;
      if (isConstantRef(symbolTable, valueNumber)) {
        Object x = symbolTable.getConstantValue(valueNumber);
        if (x instanceof String) {
          // this is always the case in Java. use strong typing in the call to getInstanceKeyForConstant.
          String S = (String) x;
          TypeReference type = node.getMethod().getDeclaringClass().getClassLoader().getLanguage().getConstantType(S);
          if (type == null) {
            return new InstanceKey[0];
          }
          InstanceKey ik = hm.getInstanceKeyForConstant(type, S);
          if (ik != null) {
            result = new InstanceKey[] { ik };
          } else {
            result = new InstanceKey[0];
          }
        } else {
          // some non-built in type (e.g. Integer). give up on strong typing.
          // language-specific subclasses (e.g. Javascript) should override this method to get strong typing
          // with generics if desired.
          TypeReference type = node.getMethod().getDeclaringClass().getClassLoader().getLanguage().getConstantType(x);
          if (type == null) {
            return new InstanceKey[0];
          }
          InstanceKey ik = hm.getInstanceKeyForConstant(type, x);
          if (ik != null) {
            result = new InstanceKey[] { ik };
          } else {
            result = new InstanceKey[0];
          }
        }
      } else {
        SSANewInstruction def = (SSANewInstruction) du.getDef(valueNumber);
        InstanceKey iKey = hm.getInstanceKeyForAllocation(node, def.getNewSite());
        result = (iKey == null) ? new InstanceKey[0] : new InstanceKey[] { iKey };
      }

      if (ensureIndexes) {
        for (int i = 0; i < result.length; i++) {
          findOrCreateIndexForInstanceKey(result[i], instanceKeys);
        }
      }

      return result;
    }


    private OrdinalSet<InstanceKey> toOrdinalSet(InstanceKey[] ik) {
      MutableSparseIntSet s = MutableSparseIntSet.makeEmpty();
      for (int i = 0; i < ik.length; i++) {
        int index = instanceKeys.getMappedIndex(ik[i]);
        if (index != -1) {
          s.add(index);
        } else {
          assert index != -1 : "instance " + ik[i] + " not mapped!";
        }
      }
      return new OrdinalSet<InstanceKey>(s, instanceKeys);
    }

    private OrdinalSet<InstanceKey> computeImplicitPointsToSet(PointerKey key) {
      if (key instanceof LocalPointerKey) {
        LocalPointerKey lpk = (LocalPointerKey) key;
        CGNode node = lpk.getNode();
        IR ir = node.getIR();
        DefUse du = node.getDU();
        if (contentsAreInvariant(ir.getSymbolTable(), du, lpk.getValueNumber())) {
          // cons up the points-to set for invariant contents
          InstanceKey[] ik = getInvariantContents(ir.getSymbolTable(), du, node, lpk.getValueNumber(), getHeapModel(),
                                                  true, instanceKeys);
          return toOrdinalSet(ik);
        } else {
          SSAInstruction def = du.getDef(lpk.getValueNumber());
          if (def != null) {
            PointsToSetVistor v = new PointsToSetVistor(this, lpk);
            def.visit(v);
            OrdinalSet<InstanceKey> pointsToSet = v.getPointsToSet();
            if (pointsToSet != null) {
              return pointsToSet;
            } else {
              Assertions.UNREACHABLE("saw " + key + ": time to implement for " + def.getClass());
              return null;
            }
          } else {
            Assertions.UNREACHABLE("unexpected null def for " + key);
            return null;
          }
        }
      } else {
        Assertions.UNREACHABLE("unexpected implicit key " + key + " that's not a local pointer key");
        return null;
      }
    }
  }

  @Override
  protected PropagationSystem makeSystem(AnalysisOptions options) {
    return new PropSystem(callGraph, pointerKeyFactory, instanceKeyFactory);
  }

}
