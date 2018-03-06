package br.ufpe.cin.datarecommendation;

public class CollectionsNameResolver implements ICollectionsNameResolver {

	public boolean isList(String className) {
		if(className != null) {
			boolean isSynchronizedLinkedList = className.equalsIgnoreCase("synchronizedLinkedList");
			boolean isVector = className.equalsIgnoreCase("vector")||className.equalsIgnoreCase("Ljava/util/Vector");
			boolean isSynchronizedArrayList = className.equalsIgnoreCase("synchronizedArrayList");
			boolean isCopyOnWriteArrayList = className.equalsIgnoreCase("copyOnWriteArrayList")||className.equalsIgnoreCase("Ljava/util/concurrent/CopyOnWriteArrayList");
			boolean isArrayList = className.equalsIgnoreCase("arrayList")||className.equalsIgnoreCase("Ljava/util/ArrayList");
			boolean isLinkedList = className.equalsIgnoreCase("linkedList")||className.equalsIgnoreCase("Ljava/util/LinkedList");
			boolean isSynchronizedFastList = className.equalsIgnoreCase("synchronizedFastList(EclipseCollections)");
			boolean isFastList = className.equalsIgnoreCase("fastList(EclipseCollections)");
			boolean isTreeList = className.equalsIgnoreCase("treeList(ApacheCommonsCollections)");
			boolean isNodeCachingLinkedList = className.equalsIgnoreCase("nodeCachingLinkedList(ApacheCommonsCollections)");
			boolean isListInterface = className.equalsIgnoreCase("Ljava/util/List");
			
			return isSynchronizedLinkedList||isVector||isSynchronizedArrayList||isCopyOnWriteArrayList||isArrayList
					||isLinkedList||isListInterface||isSynchronizedFastList||isFastList||isTreeList||isNodeCachingLinkedList;
		} else {
			return false;
		}
	}
	
	public boolean isMap(String className) {
		if(className != null) {
			boolean isLinkedHashMap = className.equalsIgnoreCase("linkedHashMap")||className.equalsIgnoreCase("Ljava/util/LinkedHashMap");
			boolean isConcurrentHashMap = className.equalsIgnoreCase("concurrentHashMap")||className.equalsIgnoreCase("Ljava/util/concurrent/ConcurrentHashMap");
			boolean isConcurrentHashMapV8 = className.equalsIgnoreCase("concurrentHashMapV8");
			boolean isConcurrentHashMapEclipseCollections = className.equalsIgnoreCase("concurrentHashMap(EclipseCollections)");
			boolean isSynchronizedUnifiedMap = className.equalsIgnoreCase("synchronizedUnifiedMap(EclipseCollections)");
			boolean isStaticBucketMap = className.equalsIgnoreCase("staticBucketMap(ApacheCommonsCollections)");
			boolean isunifiedMap = className.equalsIgnoreCase("unifiedMap(EclipseCollections)");
			boolean isHashedMap = className.equalsIgnoreCase("hashedMap(ApacheCommonsCollections)");
			boolean isHashtable = className.equalsIgnoreCase("hashtable")||className.equalsIgnoreCase("Ljava/util/Hashtable");
			boolean isSynchronizedHashMap = className.equalsIgnoreCase("synchronizedHashMap");
			boolean isSynchronizedTreeMap = className.equalsIgnoreCase("synchronizedTreeMap");
			boolean isSynchronizedWeakHashMap = className.equalsIgnoreCase("synchronizedWeakHashMap");
			boolean isHashMap = className.equalsIgnoreCase("hashMap")||className.equalsIgnoreCase("Ljava/util/HashMap");
			boolean isTreeMap = className.equalsIgnoreCase("treeMap")||className.equalsIgnoreCase("Ljava/util/TreeMap");
			boolean isWeakHashMap = className.equalsIgnoreCase("weakHashMap")||className.equalsIgnoreCase("Ljava/util/WeakHashMap");
			boolean isMapInterface = className.equalsIgnoreCase("Ljava/util/Map");
			
			return isLinkedHashMap||isConcurrentHashMapV8||isHashtable||isSynchronizedHashMap||
					isSynchronizedTreeMap||isSynchronizedWeakHashMap||isHashMap||isTreeMap||isWeakHashMap||
					isMapInterface||isConcurrentHashMap||isConcurrentHashMapEclipseCollections||isSynchronizedUnifiedMap
					||isStaticBucketMap||isunifiedMap||isHashedMap;
		} else {
			return false;
		}
	}
	
	public boolean isSet(String className) {
		if(className != null) {
			boolean isSynchronizedLinkedHashSet = className.equalsIgnoreCase("synchronizedLinkedHashSet");
			boolean isConcurrentSkipListSet = className.equalsIgnoreCase("concurrentSkipListSet")||className.equalsIgnoreCase("Ljava/util/concurrent/ConcurrentSkipListSet");
			boolean isSynchronizedTreeSortedSet = className.equalsIgnoreCase("synchronizedTreeSortedSet(Eclipse Collections)");
			boolean isSynchronizedUnifiedSet = className.equalsIgnoreCase("synchronizedUnifiedSet(Eclipse Collections)");
			boolean isTreeSortedSet = className.equalsIgnoreCase("treeSortedSet(Eclipse Collections)");
			boolean isUnifiedSet = className.equalsIgnoreCase("unifiedSet(Eclipse Collections)");
			boolean isSynchronizedHashSet = className.equalsIgnoreCase("synchronizedHashSet");
			boolean isSetFromConcurrentHashMap = className.equalsIgnoreCase("setFromConcurrentHashMap");
			boolean isSetFromConcurrentHashMapV8 = className.equalsIgnoreCase("setFromConcurrentHashMapV8");
			boolean isSynchronizedTreeSet = className.equalsIgnoreCase("synchronizedTreeSet");
			boolean isHashSet = className.equalsIgnoreCase("hashSet")||className.equalsIgnoreCase("Ljava/util/HashSet");
			boolean isTreeSet = className.equalsIgnoreCase("treeSet")||className.equalsIgnoreCase("Ljava/util/TreeSet");
			boolean isLinkedHashSet = className.equalsIgnoreCase("linkedHashSet")||className.equalsIgnoreCase("Ljava/util/LinkedHashSet");
			
			return isSynchronizedLinkedHashSet||isSynchronizedHashSet||isSetFromConcurrentHashMap||isSetFromConcurrentHashMapV8||
					isSynchronizedTreeSet||isHashSet||isLinkedHashSet||isTreeSet||isConcurrentSkipListSet||isSynchronizedTreeSortedSet
					||isSynchronizedUnifiedSet||isTreeSortedSet||isUnifiedSet;
		} else {
			return false;
		}
	}

}
