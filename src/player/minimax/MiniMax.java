/*
 * MINIMAX ALGORITHM
 */

package player.minimax;
//package player.mnkgame;

import mnkgame.MNKPlayer;
import mnkgame.MNKBoard;
import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
 



public class MiniMax implements MNKPlayer {
	
	
	protected int M;
	protected int N;
	protected int K;
	protected Boolean first;
	protected int timeout_in_secs;

	private MNKCellState player_own;
	private MNKCellState player_opponent;
	private MNKGameState my_win;
	private MNKGameState your_win;

	protected float time_start;					//turn start (milliseconds)
	protected Move<MiniMax_score> bestMove;		//best move for current turn
	private MNKBoard board;						//saves board for efficiency in checkGameEnded()
	
	
	//MOVE, WITH POSITION AND SCORE
	protected class Move<S extends Comparable<S>> {
		public MNKCell position;	//move target
		public S score;				//score
	}
	//INFORMATION ABOUT A GAME-TREE NODE
	//protected class Node<S extends Comparable<S>> {
	//	public S score;				//score
	//}
	//SCORE FOR FINAL STATE/INTERMEDIATE STATE (in a tree node)
	//i'm always ME, opponent always YOU
	protected enum MiniMax_score {
		YOU,	//-1
		DRAW,	//0
		ME		//1
	}
	

	
	//#region PLAYER	
	
		//Initialize the (M,N,K) Player
		public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
			this.M = M;
			this.N = N;
			this.K = K;
			this.first = first;
			this.timeout_in_secs = timeout_in_secs;
			this.board = new MNKBoard(M, N, K);
			if(first) {
				player_own = MNKCellState.P1;
				player_opponent = MNKCellState.P2;
				my_win = MNKGameState.WINP1;
				your_win = MNKGameState.WINP2;
			} else {
				player_own = MNKCellState.P2;
				player_opponent = MNKCellState.P1;
				my_win = MNKGameState.WINP2;
				your_win = MNKGameState.WINP1;
			}
			bestMove = new Move<MiniMax_score>();
		}

		
		//Select a position among those listed in the <code>FC</code> array
		public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
			//start conting time for this turn
			time_start = System.currentTimeMillis();
			//update my istance of board
			if(!first || MC.length > 0) {
				MNKCell opponent_move = MC[MC.length - 1];
				board.markCell(opponent_move.i, opponent_move.j);		//mark opponent cell
			}
			//recursive call for each possible move
			bestMove.position = FC[0];
			bestMove.score = MiniMax_score.YOU;
			visit(true, 0);
			
			MNKCell res = getBestMove();
			//update my istance of board
			board.markCell(res.i, res.j);								//mark my cell
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
	protected MNKCell iterateFreeCells() {
		return board.getFreeCells()[0];
	}
	protected boolean emptyFreeCells() {
		return board.getFreeCells().length == 0;
	}
	/**
	 * recursive call for each possible move; returns final score obtained from current position, assuming both player make their best moves
	 * @param my_turn
	 * @param depth = 0
	 */
	protected MiniMax_score visit(boolean my_turn, int depth) {
		//check if someone won or there was a draw
		MiniMax_score state_score = checkGameEnded();
		//else make a move
		if(state_score == null) {
			//final score obtained from current position, assuming both player make their best moves
			if(my_turn) state_score = MiniMax_score.YOU;		//my turn->worst score for me
			else state_score = MiniMax_score.ME;				//your turn->worst score for you
			//try all moves and update state_score
			MNKCell[] FC = board.getFreeCells();
			int i = 0;
			while(i < FC.length && !isTimeEnded())
			{
				//MNKCell next = iterateFreeCells();						//get next move
				MNKCell next = FC[i];
				board.markCell(next.i, next.j);
				MiniMax_score next_score = visit(!my_turn, depth + 1);		//calculate score for next move
				board.unmarkCell();

				setBestMove(next, next_score, depth);						//sets current best move
				if(my_turn) state_score = max(state_score, next_score);
				else state_score = min(state_score, next_score);

				i++;
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
		protected MiniMax_score checkGameEnded() {
			return GameState_to_Score(board.gameState());
		}

		//returns true if it's time to end the turn
		protected Boolean isTimeEnded() {
			return (System.currentTimeMillis() - time_start) * 1000 >= timeout_in_secs;
		}
		//returns move to make on this turn
		protected MNKCell getBestMove() {
			return bestMove.position;
		}
		protected void setBestMove(MNKCell move, MiniMax_score score, int depth) {
			if(depth == 0 && score.compareTo(bestMove.score) >= 0) {
				bestMove.position = move;
				bestMove.score = score;
			}
		}

		/**
		 * @return converts from MNKGameState to Score (class defined here)
		 */
		protected MiniMax_score GameState_to_Score(MNKGameState s) {
			if(s == MNKGameState.DRAW) return MiniMax_score.DRAW;
			else if(s == MNKGameState.OPEN) return null;
			else if(s == my_win) return MiniMax_score.ME;
			else return MiniMax_score.YOU;
		}

	//#endregion AUXILIARY

}
