package player.dbsearch2;

import mnkgame.MNKCellState;

public class Auxiliary {

	public static short abs_short(short a) {
		return (a > 0) ? a : (short)(-a);
	}
	public static short max_short(short a, short b) {
		return (a > b) ? a : b;
	}
	public static int clamp(int a, int min, int max) {
		if(a < min) return min;
		else if(a >= max) return max - 1;
		else return a;
	}

	//swaps two elements in an array
	public static <T> void swap(T[] V, int a, int b) {
		T tmp = V[a];
		V[a] = V[b];
		V[b] = tmp;
	}

	public static MNKCellState opponent(MNKCellState player) {
		return (player == MNKCellState.P1) ? MNKCellState.P2 : MNKCellState.P1;
	}
	
}
