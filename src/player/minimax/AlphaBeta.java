/*
 * ALPHABETA ALGORITHM
 */


package player.minimax;

import mnkgame.MNKCell;


public class AlphaBeta extends MiniMax {
	
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
			
		return null;
	}
	
	//Returns the player name
	public String playerName() {
		return null;
	}

}
