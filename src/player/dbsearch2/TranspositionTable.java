package player.dbsearch2;

import java.util.Random;



public class TranspositionTable {

	private long[][][] moves;		//random hashes defined for each move
	private Element[] table;		//actual array where entries are stored
	private int size;

	private static final int PLAYERS_N = 2;



	public TranspositionTable(int M, int N) {
		Random random = new Random();
		moves = new long[M][N][PLAYERS_N];
		size = (int)(Math.pow(2, Element.tableSize()));
		table = new Element[size];

		for(int i = 0; i < M; i++) {
			for(int j = 0; j < N; j++) {
				for(int k = 0; k < PLAYERS_N; k++) {
					moves[i][j][k] = random.nextLong();
				}
			}
		}
	}

	public long getHash(long hash, int i, int j, int k) {
		long move_hash = moves[i][j][k];
		return (hash ^ move_hash);
	}

	public void insert(long key) {
		Element e = new Element(key);
		int index = Element.index(key);
		if(table[index] == null)
			table[index] = e;
		else
			table[index].addNext(e);
	}

	public Boolean exists(long key) {
		int index = Element.index(key);
		if(table[index] == null) return false;
		else {
			Element compare = new Element(key);
			return table[index].equalsNext(compare);
		}
	}



	private class Element {
		//KEY = key1 + key2 + index = (16+32+16) bit = 64bit

		private short key1;
		private int key2;
		protected Element next;

		private static final int TABLE_SIZE = 16;
		private static final int MASK2_BITS = TABLE_SIZE + Integer.SIZE;
		private static final int MASK1 = 65535;		//2**16-1 = 16 ones

		protected Element(long key) {
			key2 = (int)(key >> TABLE_SIZE);
			key1 = (short)(key >> MASK2_BITS);
		}
		protected void addNext(Element e) {
			if(next == null) next = e;
			else next.addNext(e);
		}
		//returns true if cmp==this or a next element in the list (assuming the index is the same)
		protected Boolean equalsNext(Element cmp) {
			if ((cmp.key1 == key1) && (cmp.key2 == key2)) return true;
			else if(next == null) return false;
			else return next.equalsNext(cmp);
		}
		protected static int tableSize() {
			return TABLE_SIZE;
		}
		protected static int index(long key) {
			return (int)(key & MASK1);
		}
	}

}
