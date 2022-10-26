/*
 * PARAMTERS:
 * S=self (same class)
 * K=key type
 */


package structures;



/**
 * @param <S> the same type as the class
 * @param <K> the type used to compare
 */
public interface PHElement<S, K> extends Comparable<S> {
	
	/**
	 * alternative compare
	 * @param b
	 */
	public int compareTo2(S b);
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
	/**
	 * @return key
	 */
	public K getKey();
	/**
	 * sets new key
	 */
	public void setKey(K new_key);
}
