/*
 * PLAYER IMPLEMENTED WITH ITERATIVE DEEPENING
 */

package player.it_deepening;

import mnkgame.MNKPlayer;
import player.minimax.MiniMax;
import mnkgame.MNKCell;






class ItDeepening implements MNKPlayer {

		int M;
		int N;
		int K;
		Boolean first;
		int timeout_in_secs;
		MiniMax minimax;
		
		

   		//Initialize the (M,N,K) Player
  		public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
			this.M = M;
			this.N = N;
			this.K = K;
			this.first = first;
			this.timeout_in_secs = timeout_in_secs;
			minimax = new MiniMax();
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
