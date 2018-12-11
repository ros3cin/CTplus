package br.ufpe.cin.datarecommendation;

public class CollectionsTypeResolver implements ICollectionsTypeResolver {

	public boolean isList(String className) {
		if(className != null) {
			return isSynchronizedLinkedList(className)||isVector(className)||isSynchronizedArrayList(className)||
					isCopyOnWriteArrayList(className)||isArrayList(className)||isLinkedList(className)||
					isListInterface(className)||isSynchronizedFastList(className)||isFastList(className)||
					isTreeList(className)||isNodeCachingLinkedList(className);
		} else {
			return false;
		}
	}
	
	public boolean isMap(String className) {
		if(className != null) {
			return isLinkedHashMap(className)||isConcurrentHashMapV8(className)||isHashtable(className)||isSynchronizedHashMap(className)||
					isSynchronizedTreeMap(className)||isSynchronizedWeakHashMap(className)||isHashMap(className)||
					isTreeMap(className)||isWeakHashMap(className)||isMapInterface(className)||isConcurrentHashMap(className)||
					isConcurrentHashMapEclipseCollections(className)||isSynchronizedUnifiedMap(className)||isStaticBucketMap(className)||
					isUnifiedMap(className)||isHashedMap(className)||isSynchronizedLinkedHashMap(className)||isConcurrentSkipListMap(className);
		} else {
			return false;
		}
	}
	
	public boolean isSet(String className) {
		if(className != null) {
			return isSynchronizedLinkedHashSet(className)||isSynchronizedHashSet(className)||isSetFromConcurrentHashMap(className)||
					isSetFromConcurrentHashMapV8(className)||isSynchronizedTreeSet(className)||isHashSet(className)||
					isLinkedHashSet(className)||isTreeSet(className)||isConcurrentSkipListSet(className)||isSynchronizedTreeSortedSet(className)
					||isSynchronizedUnifiedSet(className)||isTreeSortedSet(className)||isUnifiedSet(className)||isSetInterface(className);
		} else {
			return false;
		}
	}

	@Override
	public boolean isSynchronizedLinkedList(String source) {
		return source.equalsIgnoreCase("synchronizedLinkedList");
	}

	@Override
	public boolean isVector(String source) {
		return source.equalsIgnoreCase("vector")
				||source.equalsIgnoreCase("java.util.Vector")
				||source.equalsIgnoreCase("Ljava/util/Vector");
	}

	@Override
	public boolean isSynchronizedArrayList(String source) {
		return source.equalsIgnoreCase("synchronizedArrayList");
	}

	@Override
	public boolean isCopyOnWriteArrayList(String source) {
		return source.equalsIgnoreCase("copyOnWriteArrayList")
				||source.equalsIgnoreCase("java.util.concurrent.CopyOnWriteArrayList")
				||source.equalsIgnoreCase("Ljava/util/concurrent/CopyOnWriteArrayList");
	}

	@Override
	public boolean isArrayList(String source) {
		return source.equalsIgnoreCase("arrayList")
				||source.equalsIgnoreCase("java.util.ArrayList")
				||source.equalsIgnoreCase("Ljava/util/ArrayList");
	}

	@Override
	public boolean isLinkedList(String source) {
		return source.equalsIgnoreCase("linkedList")
				||source.equalsIgnoreCase("java.util.LinkedList")
				||source.equalsIgnoreCase("Ljava/util/LinkedList");
	}

	@Override
	public boolean isSynchronizedFastList(String source) {
		return source.equalsIgnoreCase("synchronizedFastList(EclipseCollections)");
	}

	@Override
	public boolean isFastList(String source) {
		return source.equalsIgnoreCase("fastList(EclipseCollections)");
	}

	@Override
	public boolean isTreeList(String source) {
		return source.equalsIgnoreCase("treeList(ApacheCommonsCollections)");
	}

	@Override
	public boolean isNodeCachingLinkedList(String source) {
		return source.equalsIgnoreCase("nodeCachingLinkedList(ApacheCommonsCollections)");
	}

	@Override
	public boolean isListInterface(String source) {
		return source.equalsIgnoreCase("java.util.List")
				||source.equalsIgnoreCase("Ljava/util/List");
	}

	@Override
	public boolean isLinkedHashMap(String source) {
		return source.equalsIgnoreCase("linkedHashMap")
				||source.equalsIgnoreCase("java.util.LinkedHashMap")
				||source.equalsIgnoreCase("Ljava/util/LinkedHashMap");
	}
	
	@Override
	public boolean isSynchronizedLinkedHashMap(String source) {
		return source.equalsIgnoreCase("synchronizedLinkedHashMap");
	}
	
	@Override
	public boolean isConcurrentHashMap(String source) {
		return source.equalsIgnoreCase("concurrentHashMap")
				||source.equalsIgnoreCase("java.util.concurrent.ConcurrentHashMap")
				||source.equalsIgnoreCase("Ljava/util/concurrent/ConcurrentHashMap");
	}

	@Override
	public boolean isConcurrentHashMapV8(String source) {
		return source.equalsIgnoreCase("concurrentHashMapV8");
	}

	@Override
	public boolean isConcurrentHashMapEclipseCollections(String source) {
		return source.equalsIgnoreCase("concurrentHashMap(EclipseCollections)");
	}
	
	@Override
	public boolean isConcurrentSkipListMap(String source) {
		return source.equalsIgnoreCase("concurrentSkipListMap")
				||source.equalsIgnoreCase("java.util.concurrent.ConcurrentSkipListMap")
				||source.equalsIgnoreCase("Ljava/util/concurrent/ConcurrentSkipListMap");
	}

	@Override
	public boolean isSynchronizedUnifiedMap(String source) {
		return source.equalsIgnoreCase("synchronizedUnifiedMap(EclipseCollections)");
	}

	@Override
	public boolean isStaticBucketMap(String source) {
		return source.equalsIgnoreCase("staticBucketMap(ApacheCommonsCollections)");
	}

	@Override
	public boolean isUnifiedMap(String source) {
		return source.equalsIgnoreCase("unifiedMap(EclipseCollections)");
	}

	@Override
	public boolean isHashedMap(String source) {
		return source.equalsIgnoreCase("hashedMap(ApacheCommonsCollections)");
	}

	@Override
	public boolean isHashtable(String source) {
		return source.equalsIgnoreCase("hashtable")
				||source.equalsIgnoreCase("java.util.Hashtable")
				||source.equalsIgnoreCase("Ljava/util/Hashtable");
	}

	@Override
	public boolean isSynchronizedHashMap(String source) {
		return source.equalsIgnoreCase("synchronizedHashMap");
	}

	@Override
	public boolean isSynchronizedTreeMap(String source) {
		return source.equalsIgnoreCase("synchronizedTreeMap");
	}

	@Override
	public boolean isSynchronizedWeakHashMap(String source) {
		return source.equalsIgnoreCase("synchronizedWeakHashMap");
	}

	@Override
	public boolean isHashMap(String source) {
		return source.equalsIgnoreCase("hashMap")
				||source.equalsIgnoreCase("java.util.HashMap")
				||source.equalsIgnoreCase("Ljava/util/HashMap");
	}

	@Override
	public boolean isTreeMap(String source) {
		return source.equalsIgnoreCase("treeMap")
				||source.equalsIgnoreCase("java.util.TreeMap")
				||source.equalsIgnoreCase("Ljava/util/TreeMap");
	}

	@Override
	public boolean isWeakHashMap(String source) {
		return source.equalsIgnoreCase("weakHashMap")
				||source.equalsIgnoreCase("java.util.WeakHashMap")
				||source.equalsIgnoreCase("Ljava/util/WeakHashMap");
	}

	@Override
	public boolean isMapInterface(String source) {
		return source.equalsIgnoreCase("java.util.Map");
	}

	@Override
	public boolean isSynchronizedLinkedHashSet(String source) {
		return source.equalsIgnoreCase("synchronizedLinkedHashSet");
	}

	@Override
	public boolean isConcurrentSkipListSet(String source) {
		return source.equalsIgnoreCase("concurrentSkipListSet")
				||source.equalsIgnoreCase("java.util.concurrent.ConcurrentSkipListSet")
				||source.equalsIgnoreCase("Ljava/util/concurrent/ConcurrentSkipListSet");
	}

	@Override
	public boolean isSynchronizedTreeSortedSet(String source) {
		return source.equalsIgnoreCase("synchronizedTreeSortedSet(Eclipse Collections)");
	}

	@Override
	public boolean isSynchronizedUnifiedSet(String source) {
		return source.equalsIgnoreCase("synchronizedUnifiedSet(Eclipse Collections)");
	}

	@Override
	public boolean isTreeSortedSet(String source) {
		return source.equalsIgnoreCase("treeSortedSet(Eclipse Collections)");
	}

	@Override
	public boolean isUnifiedSet(String source) {
		return source.equalsIgnoreCase("unifiedSet(Eclipse Collections)");
	}

	@Override
	public boolean isSynchronizedHashSet(String source) {
		return source.equalsIgnoreCase("synchronizedHashSet");
	}

	@Override
	public boolean isSetFromConcurrentHashMap(String source) {
		return source.equalsIgnoreCase("setFromConcurrentHashMap");
	}

	@Override
	public boolean isSetFromConcurrentHashMapV8(String source) {
		return source.equalsIgnoreCase("setFromConcurrentHashMapV8");
	}

	@Override
	public boolean isSynchronizedTreeSet(String source) {
		return source.equalsIgnoreCase("synchronizedTreeSet");
	}

	@Override
	public boolean isHashSet(String source) {
		return source.equalsIgnoreCase("hashSet")
				||source.equalsIgnoreCase("java.util.HashSet")
				||source.equalsIgnoreCase("Ljava/util/HashSet");
	}

	@Override
	public boolean isTreeSet(String source) {
		return source.equalsIgnoreCase("treeSet")
				||source.equalsIgnoreCase("java.util.TreeSet")
				||source.equalsIgnoreCase("Ljava/util/TreeSet");
	}

	@Override
	public boolean isLinkedHashSet(String source) {
		return source.equalsIgnoreCase("linkedHashSet")
				||source.equalsIgnoreCase("java.util.LinkedHashSet")
				||source.equalsIgnoreCase("Ljava/util/LinkedHashSet");
	}
	
	@Override
	public boolean isSetInterface(String source) {
		return source.equalsIgnoreCase("java.util.Set")
				||source.equalsIgnoreCase("Ljava/util/Set");
	}

	@Override
	public boolean isThreadSafeList(String name) {
		return isVector(name)||isSynchronizedArrayList(name)||isSynchronizedLinkedList(name)||isCopyOnWriteArrayList(name)||
				isSynchronizedFastList(name);
	}

	@Override
	public boolean isThreadSafeMap(String name) {
		return isConcurrentHashMapV8(name)||isConcurrentHashMap(name)||isHashtable(name)||isSynchronizedHashMap(name)||
				isSynchronizedLinkedHashMap(name)||isConcurrentSkipListMap(name)||isSynchronizedTreeMap(name)||
				isSynchronizedWeakHashMap(name)||isConcurrentHashMapEclipseCollections(name)||isSynchronizedUnifiedMap(name)||
				isStaticBucketMap(name)||isSynchronizedLinkedHashMap(name);
	}

	@Override
	public boolean isThreadSafeSet(String name) {
		return isSynchronizedLinkedHashSet(name)||isSynchronizedHashSet(name)||isConcurrentSkipListSet(name)||isSetFromConcurrentHashMap(name)
				||isSetFromConcurrentHashMapV8(name)||isSynchronizedTreeSet(name)||isSynchronizedTreeSortedSet(name)||isSynchronizedUnifiedSet(name);
	}

	@Override
	public boolean isListEqual(String a, String b) {
		return (isSynchronizedLinkedList(a)&&isSynchronizedLinkedList(b)) ||
				(isVector(a)&&isVector(b)) ||
				(isSynchronizedArrayList(a)&&isSynchronizedArrayList(b)) ||
				(isCopyOnWriteArrayList(a)&&isCopyOnWriteArrayList(b)) ||
				(isArrayList(a)&&isArrayList(b)) ||
				(isLinkedList(a)&&isLinkedList(b)) ||
				(isSynchronizedFastList(a)&&isSynchronizedFastList(b)) ||
				(isFastList(a)&&isFastList(b)) ||
				(isTreeList(a)&&isTreeList(b)) ||
				(isSynchronizedLinkedList(a)&&isSynchronizedLinkedList(b)) ||
				(isListInterface(a)&&isListInterface(b));
	}

	@Override
	public boolean isMapEqual(String a, String b) {
		return (isLinkedHashMap(a)&&isLinkedHashMap(b)) ||
				(isSynchronizedLinkedHashMap(a)&&isSynchronizedLinkedHashMap(b)) ||
				(isConcurrentSkipListMap(a)&&isConcurrentSkipListMap(b)) ||
				(isConcurrentHashMap(a)&&isConcurrentHashMap(b)) ||
				(isConcurrentHashMapV8(a)&&isConcurrentHashMapV8(b)) ||
				(isConcurrentHashMapEclipseCollections(a)&&isConcurrentHashMapEclipseCollections(b)) ||
				(isSynchronizedUnifiedMap(a)&&isSynchronizedUnifiedMap(b)) ||
				(isStaticBucketMap(a)&&isStaticBucketMap(b)) ||
				(isUnifiedMap(a)&&isUnifiedMap(b)) ||
				(isHashedMap(a)&&isHashedMap(b)) ||
				(isHashtable(a)&&isHashtable(b)) ||
				(isSynchronizedHashMap(a)&&isSynchronizedHashMap(b)) ||
				(isSynchronizedTreeMap(a)&&isSynchronizedTreeMap(b)) ||
				(isSynchronizedWeakHashMap(a)&&isSynchronizedWeakHashMap(b)) ||
				(isHashMap(a)&&isHashMap(b)) ||
				(isTreeMap(a)&&isTreeMap(b)) ||
				(isWeakHashMap(a)&&isWeakHashMap(b)) ||
				(isMapInterface(a)&&isMapInterface(b)) ;
	}

	@Override
	public boolean isSetEqual(String a, String b) {
		return (isSynchronizedLinkedHashSet(a)&&isSynchronizedLinkedHashSet(b)) ||
				(isConcurrentSkipListSet(a)&&isConcurrentSkipListSet(b)) ||
				(isSynchronizedTreeSortedSet(a)&&isSynchronizedTreeSortedSet(b)) ||
				(isSynchronizedUnifiedSet(a)&&isSynchronizedUnifiedSet(b)) ||
				(isTreeSortedSet(a)&&isTreeSortedSet(b)) ||
				(isUnifiedSet(a)&&isUnifiedSet(b)) ||
				(isSynchronizedHashSet(a)&&isSynchronizedHashSet(b)) ||
				(isSetFromConcurrentHashMap(a)&&isSetFromConcurrentHashMap(b)) ||
				(isSetFromConcurrentHashMapV8(a)&&isSetFromConcurrentHashMapV8(b)) ||
				(isSynchronizedTreeSet(a)&&isSynchronizedTreeSet(b)) ||
				(isHashSet(a)&&isHashSet(b)) ||
				(isTreeSet(a)&&isTreeSet(b)) ||
				(isLinkedHashSet(a)&&isLinkedHashSet(b)) ||
				(isSetInterface(a)&&isSetInterface(b));
	}
	
	@Override
	public boolean isSameCollection(String a, String b) {
		if(isList(a)) {
			return isListEqual(a, b);
		} else if (isMap(a)) {
			return isMapEqual(a, b);
		} else if (isSet(a)) {
			return isSetEqual(a, b);
		} else 
			return false;
	}

	@Override
	public boolean isFromStandardJCF(String name) {
		return isSynchronizedLinkedList(name)||
				isVector(name)||
				isSynchronizedArrayList(name)||
				isCopyOnWriteArrayList(name)||
				isArrayList(name)||
				isLinkedList(name)||
				isListInterface(name)||
				isLinkedHashMap(name)||
				isSynchronizedLinkedHashMap(name)||
				isConcurrentHashMap(name)||
				isConcurrentSkipListMap(name)||

				isHashtable(name)||
				isSynchronizedHashMap(name)||
				isSynchronizedTreeMap(name)||
				//isSynchronizedWeakHashMap(name)||
				isHashMap(name)||
				isTreeMap(name)||
				//isWeakHashMap(name)||
				isMapInterface(name)||

				isSynchronizedLinkedHashSet(name)||
				isConcurrentSkipListSet(name)||
				isSynchronizedHashSet(name)||
				isSetFromConcurrentHashMap(name)||
				isSynchronizedTreeSet(name)||
				isHashSet(name)||
				isTreeSet(name)||
				isLinkedHashSet(name)||
				isSetInterface(name);
	}

	@Override
	public boolean isFromEclipseCollections(String name) {
		return isSynchronizedFastList(name)||isFastList(name)||isConcurrentHashMapEclipseCollections(name)||
				isSynchronizedUnifiedMap(name)||isUnifiedMap(name)||isSynchronizedUnifiedSet(name)||isSynchronizedTreeSortedSet(name)
				||isUnifiedSet(name)||isTreeSortedSet(name);
	}

	@Override
	public boolean isFromApacheCommonsCollections(String name) {
		return isTreeList(name)||isNodeCachingLinkedList(name)||isCursorableLinkedList(name)||isStaticBucketMap(name)||isHashedMap(name);
	}

	@Override
	public boolean isFromJSR166e(String name) {
		return isConcurrentHashMapV8(name);
	}
	
	@Override
	public boolean isCursorableLinkedList(String source) {
		return "cursorableLinkedList(ApacheCommonsCollections)".equalsIgnoreCase(source);
	}
}
