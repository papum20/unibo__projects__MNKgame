package player.dbsearch;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;

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
	public static boolean equalMNKCells(MNKCell a, MNKCell b) {
		return a.i == b.i && a.j == b.j;
	}
	//makes sense only assuming it's a win, and the cell is not empty
	public static MNKGameState cellState2winState(MNKCellState cell_state) {
		return (cell_state == MNKCellState.P1) ? MNKGameState.WINP1 : MNKGameState.WINP2;
	}
	
}
