/*
 * MINIMAX ALGORITHM
 */

package player.minimax;

import mnkgame.MNKPlayer;

import java.util.Calendar;

import mnkgame.MNKCell;


public class MiniMax implements MNKPlayer {
	
	
	int M;
	int N;
	int K;
	Boolean first;
	int timeout_in_secs;

	float time_start;	//turn start (milliseconds)
	MNKCell bestMove;	//best move found in this turn
	int bestScore;		//score of the best move found
	
	
	
	//Initialize the (M,N,K) Player
	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		this.M = M;
		this.N = N;
		this.K = K;
		this.first = first;
		this.timeout_in_secs = timeout_in_secs;
	}

	
	//Select a position among those listed in the <code>FC</code> array
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		time_start = System.currentTimeMillis();
		int i = 0;
		while(i < FC.length && !isTimeEnded()) {

		}

		return bestMove;
	}
	
	//Returns the player name
	public String playerName() {
		return null;
	}


	////AUXILIARY

	//swaps two elements in an array
	protected <T> void swap(T[] V, int a, int b) {
		T tmp = V[a];
		V[a] = V[b];
		V[b] = tmp;
	}

	//returns true if it's time to end the turn
	protected Boolean isTimeEnded() {
		return System.currentTimeMillis() - time_start >= timeout_in_secs;
	}

}
