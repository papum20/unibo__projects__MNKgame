/*
 * MINIMAX ALGORITHM
 */

package player.minimax;

import mnkgame.MNKPlayer;
import java.sql.Struct;
import java.util.Calendar;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;
import mnkgame.MNKCell;





public class MiniMax implements MNKPlayer {
	
	
	protected int M;
	protected int N;
	protected int K;
	protected Boolean first;
	protected int timeout_in_secs;

	protected float time_start;					//turn start (milliseconds)
	protected Move<MiniMax_score> bestMove;		//best move for current turn
	protected int it_freeCells;					//iterator for FC
	
	
	//MOVE, WITH POSITION AND SCORE
	protected class Move<S> {
		public MNKCell position;	//move target
		public S score;				//score
	}
	//FINAL SCORE
	protected enum MiniMax_score {
		P2,		//-1
		DRAW,	//0
		P1		//1
	}
	

	
	//#region PLAYER	
	
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
			//start conting time for this turn
			time_start = System.currentTimeMillis();
			//recursive call for each possible move
			it_freeCells = 0;
			visit(FC, true);
			
			return getBestMove();
		}
		
		//Returns the player name
		public String playerName() {
			return null;
		}
	
	//#endregion PLAYER



	//#region AUXILIARY
	
		//returns the next free cell to visit and rearranges FC for the next iteration
		//PRECONDITION: FC.length > 0
		protected MNKCell iterateFreeCells(MNKCell[] FC) {
			it_freeCells++;
			return FC[it_freeCells - 1];
		}
		//recursive call for each possible move; returns final score obtained from current position, assuming both player make their best moves
		protected MiniMax_score visit(MNKCell[] FC, boolean own_turn) {
			//final score obtained from current position, assuming both player make their best moves
			MiniMax_score state_score;
			if(own_turn) state_score = MiniMax_score.P2;
			else state_score = MiniMax_score.P1;
			//try all moves and updarte state_score
			while(it_freeCells < FC.length && !isTimeEnded()) {
				MNKCell next = iterateFreeCells(FC);				//get next move
				MiniMax_score next_score = visit(FC, !own_turn);	//calculate score for next move
				setBestMove(next, next_score);
			}
		}
		

		//swaps two elements in an array
		protected <T> void swap(T[] V, int a, int b) {
			T tmp = V[a];
			V[a] = V[b];
			V[b] = tmp;
		}

		//returns true if it's time to end the turn
		protected Boolean isTimeEnded() {
			return (System.currentTimeMillis() - time_start) * 1000 >= timeout_in_secs;
		}
		//returns move to make on this turn
		protected Boolean getBestMove() {
			return bestMove;
		}
		protected void setBestMove(MNKCell move, MiniMax_score score) {
			if(score.compareTo(bestScore) > 0) {
				bestMove = move;
				bestScore = score;
			}
		}

	//#endregion AUXILIARY

}
