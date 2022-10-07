/*
 * MINIMAX ALGORITHM
 */

package player.minimax;
//package player.mnkgame;

import mnkgame.MNKPlayer;

import java.util.Scanner;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
import player.ArrayBoard;
 



public class MiniMax implements MNKPlayer {
	
	
	protected int M;
	protected int N;
	protected int K;
	protected Boolean first;
	protected long timeout_in_millisecs;

	
	//private MNKCellState player_own;
	//private MNKCellState player_opponent;
	protected MNKGameState my_win;
	//private MNKGameState your_win;
	
	protected long timer_start;					//turn start (milliseconds)
	protected long timer_end;					//time (millisecs) at which to stop timer
	protected Move<MiniMax_score> bestMove;		//best move for current turn
	protected ArrayBoard board;					//saves board for efficiency in checkGameEnded()

	
	//#region CLASSES

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
		//FOR VISIT...
		protected class FC_iterator {
			int i;			//current index
			ArrayBoard B;

			public FC_iterator(ArrayBoard B) {
				this.B = B;
			}
			//returns the next free cell to visit (index ) and rearranges FC for the next iteration
			//PRECONDITION: FC.length > 0
			protected void iterate() {
				i++;
			}
			protected void start() {
				i = 0;
			}
			protected boolean ended() {
				return i >= B.FreeCells_length();
			}
		}

	//#endregion CLASSES

	
	//#region PLAYER	
	
		/**
		 	* Initialize the (M,N,K) Player
		 	*
		 	* @param M Board rows
		 	* @param N Board columns
		 	* @param K Number of symbols to be aligned (horizontally, vertically, diagonally) for a win
		 	* @param first True if it is the first player, False otherwise
			* @param timeout_in_secs Maximum amount of time (in seconds) for selectCell 
		 */
		public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
			this.M = M;
			this.N = N;
			this.K = K;
			this.first = first;
			this.timeout_in_millisecs = timeout_in_secs * 1000;
			this.timer_end = timeout_in_millisecs - (4 * M * N);	// max time - 4ms times max tree depth (M * N = possible moves)
			this.board = new ArrayBoard(M, N, K);
			if(first) {
				//player_own = MNKCellState.P1;
				//player_opponent = MNKCellState.P2;
				my_win = MNKGameState.WINP1;
				//your_win = MNKGameState.WINP2;
			} else {
				//player_own = MNKCellState.P2;
				//player_opponent = MNKCellState.P1;
				my_win = MNKGameState.WINP2;
				//your_win = MNKGameState.WINP1;
			}
			bestMove = new Move<MiniMax_score>();
		}

		
		/**
			* Select a position among those listed in the <code>FC</code> array
			*
			* @param FC Free Cells: array of free cells
			* @param MC Marked Cells: array of already marked cells, ordered with respect
 			* to the game moves (first move is in the first position, etc)
 			*
 			* @return an element of <code>FC</code>
		*/
		public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
			System.out.println("------------------");
			//start conting time for this turn
			timer_start = System.currentTimeMillis();
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

			// DEBUG
			System.out.println(System.currentTimeMillis() - timer_start);

			return res;
		}
		
		/**
   			* Returns the player name
   			*
			* @return string 
   		*/
		public String playerName() {
			return "MiniMax";
		}
	
	//#endregion PLAYER



	//#region ALGORITHM

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
			FC_iterator it = new FC_iterator(board);
			while(!it.ended() && !isTimeEnded())
			{
				MNKCell next = board.getFreeCell(it.i);					//get next move				
				board.markCell(next.i, next.j);

				/*String str = "";
				for(int i = 0; i < board.FreeCells_length(); i++) str += "(" + board.getFreeCell(i).i + "," + board.getFreeCell(i).j + ") ";
				System.out.println(str);
				System.out.println(Integer.toString(next.i) + " " + Integer.toString(next.j));
				*/
				//Scanner s = new Scanner(System.in);
				//s.next();
				//s.close();


				MiniMax_score next_score = visit(!my_turn, depth + 1);		//calculate score for next move
				board.unmarkCell();

				setBestMove(next, next_score, depth);						//sets current best move
				if(my_turn) state_score = max(state_score, next_score);
				else state_score = min(state_score, next_score);

				it.iterate();
			}
		}
		return state_score;
	}
	//#endregion ALGORITHM



	//#region AUXILIARY

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
			return (System.currentTimeMillis() - timer_start) >= timer_end;
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
