package br.ufpe.cin.datarecommendation;

import java.util.HashMap;
import java.util.Map;

public class CollectionTypeCompatibility implements ICollectionTypeCompatibility {
	private String[] arrayListConstructors = new String[] {"()","(I)","(Ljava/util/Collection;)"};
	private String[] linkedListConstructors = new String[] {"()","(Ljava/util/Collection;)"};
	private String[] vectorConstructors = new String[] {"()","(I)","(II)","(Ljava/util/Collection;)"};
	private String[] copyOnWriteArrayListConstructors = new String[] {"()","(Ljava/util/Collection;)","([E)"};
	private String[] fastListConstructors = new String[] {"()","(I)","(Ljava/util/Collection;)"};
	private String[] treeListConstructors = new String[] {"()","(Ljava/util/Collection;)"};
	private String[] nodeCachingLinkedListConstructors = new String[] {"()","(I)","(Ljava/util/Collection;)"};
	
	private String[] hashMapConstructors = new String[] {"()","(I)","(IF)","(Ljava/util/Map;)"};
	private String[] hashedMapConstructors = new String[] {"()","(I)","(IF)","(Ljava/util/Map;)"};
	private String[] linkedHashMapConstructors = new String[] {"()","(I)","(IF)","(IFZ)","(Ljava/util/Map;)"};
	private String[] concurrentHashMapConstructors = new String[] {"()","(I)","(IF)","(IFI)","(Ljava/util/Map;)"};
	private String[] hashtableConstructors = new String[] {"()","(I)","(IF)","(Ljava/util/Map;)"};
	private String[] concurrentHashMapEcConstructors = new String[] {"()","(I)"};
	private String[] concurrentSkipListMapConstructors = new String[] {"()","(Ljava/util/Comparator;)","(Ljava/util/Map;)","(Ljava/util/SortedMap;)"};
	private String[] unifiedMapConstructors = new String[] {"()","(I)","(IF)","(Ljava/util/Map;)","(Lorg/eclipse/collections/api/tuple/Pair;)"};
	private String[] staticBucketMapConstructors = new String[] {"()","(I)"};
	private String[] treeMapConstructors = new String[] {"()","(Ljava/util/Comparator;)","(Ljava/util/Map;)","(Ljava/util/SortedMap;)"};
	private String[] weakHashMapConstructors = new String[] {"()","(I)","(IF)","(Ljava/util/Map;)"};
	
	private String[] hashSetConstructors = new String[] {"()","(I)","(IF)","(Ljava/util/Collection;)"};
	private String[] linkedHashSetConstructors = new String[] {"()","(I)","(IF)","(Ljava/util/Collection;)"};
	private String[] treeSetConstructors = new String[] {"()","(Ljava/util/Comparator;)","(Ljava/util/Collection;)","(Ljava/util/SortedSet;)"};
	private String[] concurrentSkipListSetConstructors = new String[] {"()","(Ljava/util/Comparator;)","(Ljava/util/Collection;)","(Ljava/util/SortedSet;)"};
	private String[] treeSortedSetConstructors = new String[] {
			"()",
			"(Ljava/lang/Iterable;)",
			"(Ljava/util/Comparator;)",
			"(Ljava/util/Comparator;Ljava/lang/Iterable;)",
			"(Ljava/util/SortedSet;)"
	};
	private String[] unifiedSetConstructors = new String[] {
			"()",
			"(I)",
			"(IF)",
			"(Ljava/util/Collection;)",
			"(Lorg/eclipse/collections/impl/set/mutable/UnifiedSet;)"
	};
	private Map<String,String[]> constructors;
	private Map<String,Boolean> validReplacements; 
	
	public CollectionTypeCompatibility() {
		this.constructors = new HashMap<String,String[]>();
		this.validReplacements = new HashMap<String,Boolean>();
		
		this.validReplacements.put("java.util.ArrayList->linkedList",true);
		this.validReplacements.put("java.util.ArrayList->fastList(EclipseCollections)",true);
		this.validReplacements.put("java.util.ArrayList->treeList(ApacheCommonsCollections)",true);
		this.validReplacements.put("java.util.ArrayList->nodeCachingLinkedList(ApacheCommonsCollections)",true);
		this.validReplacements.put("java.util.ArrayList->cursorableLinkedList",true);
		this.validReplacements.put("java.util.LinkedList->arrayList",true);
		this.validReplacements.put("java.util.LinkedList->fastList(EclipseCollections)",true);
		this.validReplacements.put("java.util.LinkedList->treeList(ApacheCommonsCollections)",true);
		this.validReplacements.put("java.util.LinkedList->nodeCachingLinkedList(ApacheCommonsCollections)",true);
		this.validReplacements.put("java.util.LinkedList->cursorableLinkedList",true);
		this.validReplacements.put("java.util.Vector->copyOnWriteArrayList",true);
		this.validReplacements.put("java.util.Vector->synchronizedLinkedList",true);
		this.validReplacements.put("java.util.Vector->synchronizedArrayList",true);
		this.validReplacements.put("java.util.Vector->synchronizedFastList(EclipseCollections)",true);
		this.validReplacements.put("java.util.concurrent.CopyOnWriteArrayList->vector",true);
		this.validReplacements.put("java.util.concurrent.CopyOnWriteArrayList->synchronizedLinkedList",true);
		this.validReplacements.put("java.util.concurrent.CopyOnWriteArrayList->synchronizedArrayList",true);
		this.validReplacements.put("java.util.concurrent.CopyOnWriteArrayList->synchronizedFastList(EclipseCollections)",true);
		
		this.validReplacements.put("java.util.HashMap->linkedHashMap",true);
		this.validReplacements.put("java.util.HashMap->unifiedMap(EclipseCollections)",true);
		this.validReplacements.put("java.util.HashMap->hashedMap(ApacheCommonsCollections)",true);
		this.validReplacements.put("java.util.Hashtable->concurrentHashMap",true);
		this.validReplacements.put("java.util.Hashtable->concurrentSkipListMap)",true);
		this.validReplacements.put("java.util.Hashtable->synchronizedHashMap",true);
		this.validReplacements.put("java.util.Hashtable->synchronizedLinkedHashMap",true);
		this.validReplacements.put("java.util.Hashtable->concurrentHashMap(EclipseCollections)",true);
		this.validReplacements.put("java.util.Hashtable->synchronizedUnifiedMap(EclipseCollections)",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentHashMap->hashtable",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentHashMap->concurrentSkipListMap)",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentHashMap->synchronizedHashMap",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentHashMap->synchronizedLinkedHashMap",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentHashMap->concurrentHashMap(EclipseCollections)",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentHashMap->synchronizedUnifiedMap(EclipseCollections)",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentSkipListMap->hashtable",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentSkipListMap->concurrentSkipListMap)",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentSkipListMap->synchronizedHashMap",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentSkipListMap->synchronizedLinkedHashMap",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentSkipListMap->concurrentHashMap(EclipseCollections)",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentSkipListMap->synchronizedUnifiedMap(EclipseCollections)",true);
		
		this.validReplacements.put("java.util.HashSet->linkedHashSet",true);
		this.validReplacements.put("java.util.HashSet->unifiedSet(Eclipse Collections)",true);
		this.validReplacements.put("java.util.TreeSet->treeSortedSet(Eclipse Collections)",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentSkipListSet->synchronizedHashSet",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentSkipListSet->synchronizedLinkedHashSet",true);
		this.validReplacements.put("java.util.concurrent.ConcurrentSkipListSet->synchronizedUnifiedSet(Eclipse Collections)",true);
		
		this.constructors.put("arrayList",this.arrayListConstructors);
		this.constructors.put("java.util.ArrayList",this.arrayListConstructors);
		this.constructors.put("Ljava/util/ArrayList",this.arrayListConstructors);
		this.constructors.put("synchronizedArrayList",this.arrayListConstructors);
		this.constructors.put("linkedList",this.linkedListConstructors);
		this.constructors.put("java.util.LinkedList",this.linkedListConstructors);
		this.constructors.put("Ljava/util/LinkedList",this.linkedListConstructors);
		this.constructors.put("synchronizedLinkedList",this.arrayListConstructors);
		this.constructors.put("copyOnWriteArrayList",this.copyOnWriteArrayListConstructors);
		this.constructors.put("java.util.concurrent.CopyOnWriteArrayList",this.copyOnWriteArrayListConstructors);
		this.constructors.put("Ljava/util/concurrent/CopyOnWriteArrayList",this.copyOnWriteArrayListConstructors);
		this.constructors.put("vector",this.vectorConstructors);
		this.constructors.put("java.util.Vector",this.vectorConstructors);
		this.constructors.put("Ljava/util/Vector",this.vectorConstructors);
		this.constructors.put("fastList(EclipseCollections)",this.fastListConstructors);
		this.constructors.put("synchronizedFastList(EclipseCollections)",this.fastListConstructors);
		this.constructors.put("treeList(ApacheCommonsCollections)",this.treeListConstructors);
		this.constructors.put("nodeCachingLinkedList(ApacheCommonsCollections)",this.nodeCachingLinkedListConstructors);
		
		this.constructors.put("hashMap",this.hashMapConstructors);
		this.constructors.put("java.util.HashMap",this.hashMapConstructors);
		this.constructors.put("Ljava/util/HashMap",this.hashMapConstructors);
		this.constructors.put("synchronizedHashMap",this.hashMapConstructors);
		this.constructors.put("linkedHashMap",this.linkedHashMapConstructors);
		this.constructors.put("java.util.LinkedHashMap",this.linkedHashMapConstructors);
		this.constructors.put("Ljava/util/LinkedHashMap",this.linkedHashMapConstructors);
		this.constructors.put("treeMap",this.treeMapConstructors);
		this.constructors.put("java.util.TreeMap",this.treeMapConstructors);
		this.constructors.put("Ljava/util/TreeMap",this.treeMapConstructors);
		this.constructors.put("synchronizedTreeMap",this.treeMapConstructors);
		this.constructors.put("weakHashMap",this.weakHashMapConstructors);
		this.constructors.put("java.util.WeakHashMap",this.weakHashMapConstructors);
		this.constructors.put("Ljava/util/WeakHashMap",this.weakHashMapConstructors);
		this.constructors.put("synchronizedWeakHashMap",this.weakHashMapConstructors);
		this.constructors.put("hashtable",this.hashtableConstructors);
		this.constructors.put("java.util.Hashtable",this.hashtableConstructors);
		this.constructors.put("Ljava/util/Hashtable",this.hashtableConstructors);
		this.constructors.put("concurrentHashMap",this.concurrentHashMapConstructors);
		this.constructors.put("java.util.concurrent.ConcurrentHashMap",this.concurrentHashMapConstructors);
		this.constructors.put("Ljava/util/concurrent/ConcurrentHashMap",this.concurrentHashMapConstructors);
		this.constructors.put("concurrentSkipListMap",this.concurrentSkipListMapConstructors);
		this.constructors.put("java.util.concurrent.ConcurrentSkipListMap",this.concurrentSkipListMapConstructors);
		this.constructors.put("Ljava/util/concurrent/ConcurrentSkipListMap",this.concurrentSkipListMapConstructors);
		this.constructors.put("concurrentHashMap(EclipseCollections)",this.concurrentHashMapEcConstructors);
		this.constructors.put("unifiedMap(EclipseCollections)",this.unifiedMapConstructors);
		this.constructors.put("synchronizedUnifiedMap(EclipseCollections)",this.unifiedMapConstructors);
		this.constructors.put("hashedMap(ApacheCommonsCollections)",this.hashedMapConstructors);
		this.constructors.put("staticBucketMap(ApacheCommonsCollections)",this.staticBucketMapConstructors);
		
		this.constructors.put("concurrentSkipListSet",this.concurrentSkipListSetConstructors);
		this.constructors.put("java.util.concurrent.ConcurrentSkipListSet",this.concurrentSkipListSetConstructors);
		this.constructors.put("Ljava/util/concurrent/ConcurrentSkipListSet",this.concurrentSkipListSetConstructors);
		this.constructors.put("hashSet",this.hashSetConstructors);
		this.constructors.put("java.util.HashSet",this.hashSetConstructors);
		this.constructors.put("Ljava/util/HashSet",this.hashSetConstructors);
		this.constructors.put("synchronizedHashSet",this.hashSetConstructors);
		this.constructors.put("treeSet",this.treeSetConstructors);
		this.constructors.put("java.util.TreeSet",this.treeSetConstructors);
		this.constructors.put("Ljava/util/TreeSet",this.treeSetConstructors);
		this.constructors.put("synchronizedTreeSet",this.treeSetConstructors);
		this.constructors.put("linkedHashSet",this.linkedHashSetConstructors);
		this.constructors.put("java.util.LinkedHashSet",this.linkedHashSetConstructors);
		this.constructors.put("Ljava/util/LinkedHashSet",this.linkedHashSetConstructors);
		this.constructors.put("synchronizedLinkedHashSet",this.linkedHashSetConstructors);
		this.constructors.put("treeSortedSet(Eclipse Collections)",this.treeSortedSetConstructors);
		this.constructors.put("synchronizedTreeSortedSet(Eclipse Collections)",this.treeSortedSetConstructors);
		this.constructors.put("unifiedSet(Eclipse Collections)",this.unifiedSetConstructors);
		this.constructors.put("synchronizedUnifiedSet(Eclipse Collections)",this.unifiedSetConstructors);
	}
	
	public boolean hasConstructor(String sourceConstructor, String targetCollection) {
		ICollectionsTypeResolver typeResolver = new CollectionsTypeResolver();
		if (typeResolver.isList(targetCollection) || typeResolver.isMap(targetCollection) || typeResolver.isSet(targetCollection)) {
				String[] constructorList = this.constructors.get(targetCollection);
				if(constructorList != null) {
					for (String constructor : constructorList) {
						if (constructor.equals(sourceConstructor)) {
							return true;
						}
					}
					return false;
				} else {
					return false;
				}
		} else {
			return false;
		}
	}
	
	
	private boolean isListThreadSafenessEqual(String source, String dest) {
		ICollectionsTypeResolver typeResolver = new CollectionsTypeResolver();
		boolean isListInterface = typeResolver.isListInterface(source);
		boolean isSameThreadSafeness = typeResolver.isThreadSafeList(source)==typeResolver.isThreadSafeList(dest);
		return isListInterface||isSameThreadSafeness;
	}
	private boolean isMapThreadSafenessEqual(String source, String dest) {
		ICollectionsTypeResolver typeResolver = new CollectionsTypeResolver();
		boolean isMapInterface = typeResolver.isMapInterface(source);
		boolean isSameThreadSafeness = typeResolver.isThreadSafeMap(source)==typeResolver.isThreadSafeMap(dest);
		return isMapInterface||isSameThreadSafeness;
	}
	private boolean isSetThreadSafenessEqual(String source, String dest) {
		ICollectionsTypeResolver typeResolver = new CollectionsTypeResolver();
		boolean isSetInterface = typeResolver.isSetInterface(source);
		boolean isSameThreadSafeness = typeResolver.isThreadSafeSet(source)==typeResolver.isThreadSafeSet(dest);
		return isSetInterface||isSameThreadSafeness;
	}

	@Override
	public boolean isThreadSafenessEqual(String source, String dest) {
		ICollectionsTypeResolver typeResolver = new CollectionsTypeResolver();
		if(typeResolver.isList(source))
			return isListThreadSafenessEqual(source, dest);
		else if (typeResolver.isMap(source))
			return isMapThreadSafenessEqual(source, dest);
		else if (typeResolver.isSet(source))
			return isSetThreadSafenessEqual(source, dest);
		else
			return false;
	}

	@Override
	public boolean canReplace(String source, String dest) {
		String key = String.format("%s->%s",source,dest);
		if(this.validReplacements.get(key) != null) {
			return this.validReplacements.get(key);
		} else 
			return false;
	}

}
