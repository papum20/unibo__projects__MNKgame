/*
 * CLASS WHICH DEFINES AND IMPLEMENTS METHODS AND VARIABLES FOR A "SMART"
 * ITERATIVE DEEPENING, WHICH SAVES THE GAME STATES AT THE CURRENT DEPTH OF THE
 * GAME TREE INSTEAD OF STARTING FROM THE ROOT FOR EACH ITERATION
 */


package player.it_deepening;

import java.util.Collection;
import java.util.LinkedList;

import mnkgame.MNKCell;
import player.ArrayBoard;
import player.ArrayBoardHeuristic;
import structures.PHElement;
import structures.PHOrder;
import structures.PriorityHeap;



public abstract class ItDeepeningSmartInterface extends ItDeepeningInterface {
	

	
	//#region CLASSESS
	
	/**
	 * @param <K> the type used to compare
	 * @param <T> the type of the elements in the PriorityHeap
	 * @param <S> the same type as the class
	 */
	protected class States_priorityHeap<K, T extends PHElement<T, K>, S extends States_priorityHeap<K,T,S>> {
		protected final PriorityHeap<K, T> PH;		//
		protected S parent;							//null if this is the root
		protected T parentMove;						//index in PQ's heap (array)	
		/**
		 * @param V = contains element to put in priorityHeap
		 * @param order = order for priorityHeap
		 * @param parent = parent index in parent heap's array
		 */
		public States_priorityHeap(PHOrder order, S parent, T parentMove) {
			PH = new PriorityHeap<K, T>(order);
			this.parent = parent;
			this.parentMove = parentMove;
		}
		public States_priorityHeap(Collection<T> V, PHOrder order, S parent, T parentMove) {
			PH = new PriorityHeap<K, T>(V, order);
			this.parent = parent;
			this.parentMove = parentMove;
		}
		/**
		 * updates own priorityHeap and, if necessary, parent's States_priorityHeap recursively
		 * @param index = index in priorityHeap's array
		 * @param key = new key to set for element
		 */
		public void update(T elem, K key) {
			if(parent != null) {
				T old_best = PH.findBest();
				PH.setKey(elem, key);
				T new_best = PH.findBest();
				if(new_best != old_best) parent.update(parentMove, new_best.getKey());
			}
			else PH.setKey(elem, key);
		}
		/**
		 * updates parent States_priorityHeap with own value
		 */
		public void updateParent() {
			if(parent != null) parent.update(parentMove, PH.findBest().getKey());
		}
		public void addAll(Collection<T> V) {
			PH.insertAll(V);
		}
		public final S getParent() {
			return parent;
		}
		public final T getParentMove() {
			return parentMove;
		}
		public T getBest() {
			return PH.findBest();
		}
	}
	// INSTANCE OF States_priorityHeap CLASS
	protected class States_PH_double extends States_priorityHeap<Double, MoveDouble, States_PH_double> {
		public States_PH_double(PHOrder order, States_PH_double parent, MoveDouble parentMove) {
			super(order, parent, parentMove);
		}
		public States_PH_double(Collection<MoveDouble> V, PHOrder order, States_PH_double parent, MoveDouble parentMove) {
			super(V, order, parent, parentMove);
		}
	}	
	/**
	 * @param <B> board type
	 * @param <K> key type for move
	 * @param <S> move type
	 */
	protected class ScoreBoard<B extends ArrayBoard, K, M extends Move<M, K>> {
		public final B board;
		public final M lastMove;
		public ScoreBoard(B board, M lastMove) {
			this.board = board;
			this.lastMove = lastMove;
		}
	}
	protected class ScoreBoard_double extends ScoreBoard<ArrayBoardHeuristic, Double, MoveDouble> {
		public ScoreBoard_double(ArrayBoardHeuristic board, MoveDouble lastMove) {
			super(board, lastMove);
		}
	}
	/**
	 * CLASS REPRESENTING A LIST OF GAME STATES, AS PAIRS OF BOARD-MOVE WITH SCORE, ALL DERIVING FROM THE SAME GAME STATE
	 * (i.e. THEY DIFFER FROM THE PARENT STATE BY ONE MOVE);
	 * THE CLASS SAVES THIS LIST AND ITS ASSOCIATE PriorityHeap (RELATIVE TO ITS ELEMENTS)
	 * THE LIST IS USED LIKE A QUEUE
	 * @param <B> type of score-board pair
	 * @param <K> type of key for move
	 * @param <M> type of move for Score-board pair
	 * @param <SB> type of Score-board pair
	 * @param <H> type of (States_)priorityHeap
	 */
	protected class ChildStates<B extends ArrayBoard, K, M extends Move<M, K>, SB extends ScoreBoard<B, K, M>, H extends States_priorityHeap<K, M, H>> {
		protected final LinkedList<SB> boards;	//boards with lastMove/score
		protected final H PH;					//own relative priorityHeap
		
		public ChildStates(H PH) {
			boards = new LinkedList<SB>();
			this.PH = PH;
		}
		public ChildStates(H PH, SB scoreBoard) {
			boards = new LinkedList<SB>();
			boards.add(scoreBoard);
			this.PH = PH;
		}
		public ChildStates(H PH, LinkedList<SB> scoreBoards) {
			this.boards = scoreBoards;
			this.PH = PH;
		}
		/**
		 * @param board : element to add as last
		 */
		public void push(SB scoreBoard) {
			boards.addLast(scoreBoard);
		}
		/**
		 * @return first board added
		 */
		public SB pop() {
			return boards.removeFirst();
		}
		public boolean isEmpty() {
			return boards.size() == 0;
		}
		public final LinkedList<SB> getBoards()	{return boards;}
		public final H getPH()					{return PH;}
	}
	// INSTANCE OF ChildStates CLASS
	protected class ChildStates_double extends ChildStates<ArrayBoardHeuristic, Double, MoveDouble, ScoreBoard_double, States_PH_double> {
		public ChildStates_double(States_PH_double PH) {
			super(PH);
		}
		public ChildStates_double(States_PH_double PH, ScoreBoard_double scoreBoard) {
			super(PH, scoreBoard);
		}
		public ChildStates_double(States_PH_double PH, LinkedList<ScoreBoard_double> scoreBoards) {
			super(PH, scoreBoards);
		}
	}
	
	//#endregion CLASSES

	
	//#region PLAYER
		
		ItDeepeningSmartInterface() {
			super();
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
			visitInLine();
			
			MNKCell res = getBestMove();
			//update my istance of board
			board.markCell(res.i, res.j);								//mark my cell

			// DEBUG
			System.out.println(System.currentTimeMillis() - timer_start);

			return res;
		}

	//#endregion PLAYER



	//#region AUXILIARY

	protected abstract void visitInLine();
	/**
	 * @param <B> type of board
	 * @param board to check
	 * @return true if it's my turn
	 */
	protected <B extends ArrayBoard> boolean checkTurn(B board) {
		return first == (board.currentPlayer() == 0);	//return true if (first && currentPlayer==0) or (!first && currentPlayer==1) 
	}

	//#endregion AUXILIARY


}
