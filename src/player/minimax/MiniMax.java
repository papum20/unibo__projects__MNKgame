/*
 * MINIMAX ALGORITHM
 */

package player.minimax;
//package player.mnkgame;

import mnkgame.MNKPlayer;
import mnkgame.MNKCell;
import mnkgame.MNKGameState;
import player.ArrayBoard;
import structures.PHElement;
 



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
	
	protected ArrayBoard board;	
	protected long timer_start;					//turn start (milliseconds)
	protected long timer_end;					//time (millisecs) at which to stop timer

	protected MoveInt bestMove;					//best move for current turn
	protected final int STATE_SCORE_OPEN;		//(for visit)

	
	//#region CLASSES

		/**
		 * MOVE, WITH POSITION AND SCORE
		 * @param <S> the same type as the class
		 * @param <K> the type used to compare
		 */
		protected interface Move<S, K> extends PHElement<S, K> {
			public void copy(S b);
		} 
		protected class MoveInt implements Move<MoveInt, Integer> {
			public MNKCell position;	//move target
			public int score;			//score
			public MoveInt(){ };
			public MoveInt(MNKCell position) {
				this.position = position;
				
			}
			public int compareTo(MoveInt b) {
				return score - b.score;
			}
			public void copy(MoveInt b) {
				position = b.position;
				score = b.score;
			}
			@Override
			public void increaseKey(Integer delta) {
				score += delta;
			}
			@Override
			public void decreaseKey(Integer delta) {
				score -= delta;				
			}
			@Override
			public Integer getKey() {
				return score;
			}
			@Override
			public void setKey(Integer new_key) {
				score = new_key;
			}
		}
		//INFORMATION ABOUT A GAME-TREE NODE
		//protected class Node<S extends Comparable<S>> {
		//	public S score;				//score
		//}
		//SCORE FOR FINAL STATE/INTERMEDIATE STATE (in a tree node)
		protected interface Score<S> {
			//private VAL value;
			public int getInt();
			public double getDouble();
			public int compareToScore(S b);
		}
		//i'm always ME, opponent always YOU
		protected enum MiniMax_score implements Score<MiniMax_score> {
			YOU(-1),		//-1
			DRAW(0),	//0
			ME(1);		//1
			protected int value;
			private MiniMax_score(int val) {
				this.value = val;
			}
			@Override public int getInt() {
				return value;
			}
			@Override public double getDouble() {
				return value;
			}
			@Override public int compareToScore(MiniMax_score b) {
				return compareTo(b);
			}
		}
		//FOR VISIT...
		protected class FC_iterator {
			public int i;			//current index
			private ArrayBoard B;

			public FC_iterator(ArrayBoard B) {
				this.B = B;
			}
			//returns the next free cell to visit (index ) and rearranges FC for the next iteration
			//PRECONDITION: FC.length > 0
			public void iterate() {
				i++;
			}
			public void start() {
				i = 0;
			}
			public boolean ended() {
				return i >= B.FreeCells_length();
			}
		}

	//#endregion CLASSES

	
	//#region PLAYER	

		public MiniMax() {
			STATE_SCORE_OPEN = -2;
		}
		
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
			initParameters(M, N, K, first, timeout_in_secs);
			initCellStates(first);
			initAttributes();
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

			// DEBUG
			//System.out.println("------------------");

			//start conting time for this turn
			timer_start = System.currentTimeMillis();
			//update my istance of board
			if(!first || MC.length > 0) {
				MNKCell opponent_move = MC[MC.length - 1];
				board.markCell(opponent_move.i, opponent_move.j);		//mark opponent cell
			}
			//recursive call for each possible move
			bestMove.position = FC[0];
			bestMove.score = getMinScore();
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
		 * recursive call for each possible move; returns final score obtained from current position, assuming both players make their best moves
		 * @param my_turn = true
		 * @param depth = 0
		 */
		protected int visit(boolean my_turn, int depth) {
			//check if someone won or there was a draw
			int state_score = checkGameEnded();
			//else make a move
			if(state_score == STATE_SCORE_OPEN) {
				//final score obtained from current position, assuming both player make their best moves
				if(my_turn) state_score = getMinScore();		//my turn->worst score for me
				else state_score = getMaxScore();				//your turn->worst score for you
				//try all moves and update state_score
				FC_iterator it = new FC_iterator(board);
				while(!it.ended() && !isTimeEnded())
				{
					MoveInt next = new MoveInt(board.getFreeCell(it.i));		//get next move				
					board.markCell(next.position.i, next.position.j);

					/*String str = "";
					for(int i = 0; i < board.FreeCells_length(); i++) str += "(" + board.getFreeCell(i).i + "," + board.getFreeCell(i).j + ") ";
					System.out.println(str);
					System.out.println(Integer.toString(next.i) + " " + Integer.toString(next.j));
					*/
					//Scanner s = new Scanner(System.in);
					//s.next();
					//s.close();


					next.score = visit(!my_turn, depth + 1);					//calculate score for next move
					board.unmarkCell();

					setBestMove(next, bestMove, depth);							//sets current best move
					if(my_turn) state_score = max(state_score, next.score);
					else state_score = min(state_score, next.score);

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
		protected int checkGameEnded() {
			MiniMax_score score = GameState_to_Score(board.gameState());
			if(score == null) return STATE_SCORE_OPEN;
			else return score.getInt();
		}

		//returns true if it's time to end the turn
		protected Boolean isTimeEnded() {
			return (System.currentTimeMillis() - timer_start) >= timer_end;
		}
		//returns move to make on this turn
		protected MNKCell getBestMove() {
			return bestMove.position;
		}
		protected <K, M extends Move<M, K>> void setBestMove(M move, M bestMove, int depth) {
			if(depth == 0 && move.compareTo(bestMove) > 0) bestMove.copy(move);
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

		protected int getMinScore() {
			return -10;
		}
		protected int getMaxScore() {
			return 10;
		}

	//#endregion AUXILIARY


	//#region INIT

		//inits InitPlayer parameters
		protected void initParameters(int M, int N, int K, boolean first, int timeout_in_secs) {
			// initialize Player interface variable (passed as parameters)
			this.M = M;
			this.N = N;
			this.K = K;
			this.first = first;
			this.timeout_in_millisecs = timeout_in_secs * 1000;		//converts seconds in milliseconds
		}
		//inits constants related to cell states, turn order in game...
		protected void initCellStates(boolean first) {
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
		}
		//inits own attributes (for this class)
		protected void initAttributes() {
			timer_end = timeout_in_millisecs - (4 * M * N);			// max time - 4ms times max tree depth (M * N = possible moves)
			board = new ArrayBoard(M, N, K);
			bestMove = new MoveInt();
		}

	//#endregion INIT


}
