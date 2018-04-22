package br.ufpe.cin.datarecommendation;

public interface ICollectionsTypeResolver {
	public boolean isSet(String name);
	public boolean isList(String name);
	public boolean isMap(String name);
	
	public boolean isFromStandardJCF(String name);
	
	public boolean isThreadSafeList(String name);
	public boolean isThreadSafeMap(String name);
	public boolean isThreadSafeSet(String name);
	
	public boolean isListEqual(String a, String b);
	public boolean isMapEqual(String a, String b);
	public boolean isSetEqual(String a, String b);
	
	public boolean isSameCollection(String a, String b);
	
	public boolean isSynchronizedLinkedList(String source);
	public boolean isVector(String source);
	public boolean isSynchronizedArrayList(String source);
	public boolean isCopyOnWriteArrayList(String source);
	public boolean isArrayList(String source);
	public boolean isLinkedList(String source);
	public boolean isSynchronizedFastList(String source);
	public boolean isFastList(String source);
	public boolean isTreeList(String source);
	public boolean isNodeCachingLinkedList(String source);
	public boolean isListInterface(String source);
	
	public boolean isLinkedHashMap(String source);
	public boolean isSynchronizedLinkedHashMap(String source);
	public boolean isConcurrentSkipListMap(String source);
	public boolean isConcurrentHashMap(String source);
	public boolean isConcurrentHashMapV8(String source);
	public boolean isConcurrentHashMapEclipseCollections(String source);
	public boolean isSynchronizedUnifiedMap(String source);
	public boolean isStaticBucketMap(String source);
	public boolean isUnifiedMap(String source);
	public boolean isHashedMap(String source);
	public boolean isHashtable(String source);
	public boolean isSynchronizedHashMap(String source);
	public boolean isSynchronizedTreeMap(String source);
	public boolean isSynchronizedWeakHashMap(String source);
	public boolean isHashMap(String source);
	public boolean isTreeMap(String source);
	public boolean isWeakHashMap(String source);
	public boolean isMapInterface(String source);
	
	public boolean isSynchronizedLinkedHashSet(String source);
	public boolean isConcurrentSkipListSet(String source);
	public boolean isSynchronizedTreeSortedSet(String source);
	public boolean isSynchronizedUnifiedSet(String source);
	public boolean isTreeSortedSet(String source);
	public boolean isUnifiedSet(String source);
	public boolean isSynchronizedHashSet(String source);
	public boolean isSetFromConcurrentHashMap(String source);
	public boolean isSetFromConcurrentHashMapV8(String source);
	public boolean isSynchronizedTreeSet(String source);
	public boolean isHashSet(String source);
	public boolean isTreeSet(String source);
	public boolean isLinkedHashSet(String source);
	public boolean isSetInterface(String source);
}
