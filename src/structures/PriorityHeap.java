package structures;

import java.util.ArrayList;
import java.util.Collection;



public class PriorityHeap<K, T extends PHElement<T, K>> {
	
	ArrayList<T> heap;
	int size;
	protected PHOrder order;



	public PriorityHeap(PHOrder order) {
		heap = new ArrayList<T>();
		size = 0;
		this.order = order;
	}
	public PriorityHeap(Collection<T> V, PHOrder order) {
		heap = new ArrayList<T>(V);
		size = heap.size();
		this.order = order;
		heapify(0);
	}


	//#region OPERATIONS

	public void insert(T elem) {
		heap.add(elem);
		fix(size);
		size++;
	}
	public void insertAll(Collection<T> V) {
		heap.addAll(V);
		heapify(0);
		size += V.size();
	}
	public void remove(T elem) {
		int index = find(elem);
		if(index != -1) {
			swap(index, size - 1);
			size--;
			fix(index);
			// doesn't remove, but decreases size
		}
	}
	/**
	 * PriorityHeap must not be empty
	 * @return best element, in the root of the max/min/other-heap
	 */
	public T findBest() {
		return heap.get(0);
	}
	/**
	 * @param elem
	 * @return index in heap / -1 if not found
	 */
	public int find(T elem) {
		return recursiveSearch(elem, 0);
	}
	/**
	 * calls elem.increaseKey(key)
	 */
	public void increaseKey(T elem, K key) {
		elem.increaseKey(key);
		fix(find(elem));
	} 
	/**
	 * calls heap[ind].increaseKey(key)
	 */
	public void increaseKey(int ind, K key) {
		heap.get(ind).increaseKey(key);
		fix(ind);
	} 
	/**
	 * calls elem.decreaseKey(key)
	 */
	public void decreaseKey(T elem, K key) {
		elem.decreaseKey(key);
		fix(find(elem));
	}
	/**
	 * calls elem.decreaseKey(key)
	 */
	public void decreaseKey(int ind, K key) {
		heap.get(ind).decreaseKey(key);
		fix(ind);
	}
	public void setKey(T elem, K key) {
		elem.setKey(key);
		fix(find(elem));
	}
	public void setKey(int ind, K key) {
		heap.get(ind).setKey(key);
		fix(ind);
	}

	//#endregion OPERATIONS



	//#region AUXILIARY

	/**
	 * @param pos = 0
	 */
	protected void heapify(int pos) {
		if(pos < size) {
			heapify(left(pos));
			heapify(right(pos));
			fixDown(pos);
		}
	}
	protected void fix(int pos) {
		fixUp(pos);
		fixDown(pos);
	}
	protected void fixUp(int pos) {
		if(pos > 0) {
			int par = parent(pos);
			if(compare(heap.get(par), heap.get(pos)) > 0) {
				swap(pos, par);
				fixUp(par);
			}
		}
	}
	protected void fixDown(int pos) {
		int l = left(pos), r = right(pos);
		if(l < size) {
			boolean r_valid = r < size && compare(heap.get(r), heap.get(pos)) < 0;
			if(compare(heap.get(l), heap.get(pos)) < 0 && (!r_valid || compare(heap.get(l), heap.get(r)) <= 0)) {
				swap(pos, l);
				fixDown(l);
			}
			else if(r_valid) {
				swap(pos, r);
				fixDown(r);
			}
		}
	}
	/**
	 * @param elem
	 * @param pos = 0
	 * @return index / -1 if not found
	 */
	protected int recursiveSearch(T elem, int pos) {
		if(pos < size && heap.get(pos) == elem) return pos;
		else {
			int res = -1;
			if(left(pos) < size) {
				if(compare(elem, heap.get(left(pos))) <= 0) res = recursiveSearch(elem, left(pos));
				if(res != -1 && right(pos) < size && compare(elem, heap.get(right(pos))) <= 0) res = recursiveSearch(elem, right(pos));
			}
			return res;
		}
	}
	protected void swap(int a, int b) {
		T tmp = heap.get(a);
		heap.set(a, heap.get(b));
		heap.set(b, tmp);
	}
	/**
	 * @return a.compareTo(b), relative to chosen order
	 */
	protected int compare(T a, T b) {
		return a.compareTo(b) * order.get();
	}
	protected int parent(int pos) {
		return (pos - 1) / 2;
	}
	protected int left(int pos) {
		return pos * 2 + 1;
	}
	protected int right(int pos) {
		return pos * 2 + 2;
	}

	//#endregion AUXILIARY


}

