/*
 * CLASS WHICH DEFINES AND IMPLEMENTS METHODS AND VARIABLES FOR A "SMART"
 * ITERATIVE DEEPENING, WHICH SAVES THE GAME STATES AT THE CURRENT DEPTH OF THE
 * GAME TREE INSTEAD OF STARTING FROM THE ROOT FOR EACH ITERATION
 */


package player.it_deepening;

import java.util.Collection;

import mnkgame.MNKCell;
import player.ArrayBoard;
import structures.PHElement;
import structures.PHOrder;
import structures.PriorityHeap;



public abstract class ItDeepeningSmartInterface extends ItDeepeningInterface {
	

	ItDeepeningSmartInterface() {
		super();
	}

	//#region CLASSESS

	/**
	 * @param <K> the type used to compare
	 * @param <T> the type of the elements in the PriorityHeap
	 * @param <S> the same type as the class
	 */
	protected class States_priorityHeap<K, T extends PHElement<T, K>, S extends States_priorityHeap<K,T,S>> {
		protected final PriorityHeap<K, T> PH;		//
		protected final S parent;					//null if this is the root
		protected T parentMove;						//index in PQ's heap (array)	
		/**
		 * @param V = contains element to put in priorityHeap
		 * @param order = order for priorityHeap
		 * @param parent = parent index in parent heap's array
		 */
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
	}
	// INSTANCE OF States_priorityHeap CLASS
	protected class States_PH_double extends States_priorityHeap<Double, MoveDouble, States_PH_double> {
		public States_PH_double(Collection<MoveDouble> V, PHOrder order, States_PH_double parent, MoveDouble parentMove) {
			super(V, order, parent, parentMove);
		}
	}
	
	//#endregion CLASSES


	//#region PLAYER
			
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
