/*
 * MINIMAX ALGORITHM
 */

package player.minimax;

import mnkgame.MNKPlayer;
import mnkgame.MNKCell;
import mnkgame.MNKCellState;
 



public class MiniMax implements MNKPlayer {
	
	
	protected int M;
	protected int N;
	protected int K;
	protected Boolean first;
	protected int timeout_in_secs;

	private MNKCellState player_own;
	private MNKCellState player_opponent;

	protected float time_start;					//turn start (milliseconds)
	protected Move<MiniMax_score> bestMove;		//best move for current turn
	protected int it_freeCells;					//iterator for FC
	private MNKCellState[][] board;				//saves board for efficiency in checkGameEnded()
	
	
	//MOVE, WITH POSITION AND SCORE
	protected class Move<S extends Comparable<S>> {
		public MNKCell position;	//move target
		public S score;				//score
	}
	//INFORMATION ABOUT A GAME-TREE NODE
	protected class Node<S extends Comparable<S>> {
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
			this.board = new MNKCellState[M][N];
			for(int y = 0; y < M; y++)
				for(int x = 0; x < N; x++) board[y][x] = MNKCellState.FREE;
			if(first) {
				player_own = MNKCellState.P1;
				player_opponent = MNKCellState.P2;
			} else {
				player_own = MNKCellState.P2;
				player_opponent = MNKCellState.P1;
			}
		}

		
		//Select a position among those listed in the <code>FC</code> array
		public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
			//start conting time for this turn
			time_start = System.currentTimeMillis();
			//recursive call for each possible move
			it_freeCells = 0;
			visit(FC, MC, true);
			
			MNKCell res = getBestMove();
			MNKCell opponent_move = MC[MC.length - 1];
			board[res.i][res.j] = player_own;
			board[opponent_move.i][opponent_move.j] = player_opponent;
			return res;

		}
		
		//Returns the player name
		public String playerName() {
			return ".";
		}
	
	//#endregion PLAYER



	//#region ALGORITHM

	//returns the next free cell to visit and rearranges FC for the next iteration
	//PRECONDITION: FC.length > 0
	protected MNKCell iterateFreeCells(MNKCell[] FC) {
		it_freeCells++;
		return FC[it_freeCells - 1];
	}
	//recursive call for each possible move; returns final score obtained from current position, assuming both player make their best moves
	protected MiniMax_score visit(MNKCell[] FC, MNKCell[] MC, boolean own_turn) {
		//check if someone won or there was a draw
		MiniMax_score state_score = checkGameEnded(MC);
		//else make a move
		if(state_score == null) {
			//final score obtained from current position, assuming both player make their best moves
			if(own_turn) state_score = MiniMax_score.P2;
			else state_score = MiniMax_score.P1;
			//try all moves and updarte state_score
			while(it_freeCells < FC.length && !isTimeEnded()) {
				MNKCell next = iterateFreeCells(FC);					//get next move
				MiniMax_score next_score = visit(FC, MC, !own_turn);	//calculate score for next move
				setBestMove(next, next_score);
				if(!own_turn) state_score = max(state_score, next_score);
				else state_score = min(state_score, next_score);
			}
		}
		return state_score;
	}
	//#endregion ALGORITHM



	//#region AUXILIARY

		//swaps two elements in an array
		protected <T> void swap(T[] V, int a, int b) {
			T tmp = V[a];
			V[a] = V[b];
			V[b] = tmp;
		}
		protected <T extends Comparable<T>> T max(T a, T b) {
			return a.compareTo(b) >= 0 ? a : b;
		}
		protected <T extends Comparable<T>> T min(T a, T b) {
			return a.compareTo(b) <= 0 ? a : b;
		}
		//checks if either a player won or it's a draw and returns the winner, else returns null
		protected MiniMax_score checkGameEnded(MNKCell[] MC) {
			
			return null;
		}

		//returns true if it's time to end the turn
		protected Boolean isTimeEnded() {
			return (System.currentTimeMillis() - time_start) * 1000 >= timeout_in_secs;
		}
		//returns move to make on this turn
		protected MNKCell getBestMove() {
			return bestMove.position;
		}
		protected void setBestMove(MNKCell move, MiniMax_score score) {
			if(score.compareTo(bestMove.score) > 0) {
				bestMove.position = move;
				bestMove.score = score;
			}
		}

	//#endregion AUXILIARY

}
