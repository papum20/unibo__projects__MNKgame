package structures;



public interface PHElement<K> extends Comparable<PHElement<K>> {
	
	/**
	 * changes own K key such that PHElement(new_key).compareTo(PHElement(old_key)) >= 0
	 * @param delta = variation to "add"
	 */
	public void increaseKey(K delta);
	/**
	 * changes own K key such that PHElement(new_key).compareTo(PHElement(old_key)) <= 0
	 * @param delta = variation to "subtract"
	 */
	public void decreaseKey(K delta);
}
