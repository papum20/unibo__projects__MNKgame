/* USES A LIST TO STORE COMBINATIONS OF LAST LEVEL,
 * USED FOR DEPENDENCY STAGE IN THIS LEVEL
 */

package player.dbsearch_old;

import java.util.LinkedList;
import java.util.ListIterator;

import player.boards.IBoardDB;
import player.dbsearch_old.structures.INodeDB;
import player.pnsearch.structures.INodes.MovePair;



public abstract class IDbSearchQ<B extends IBoardDB, N extends INodeDB<N>> extends IDbSearch<B,N> {

	protected LinkedList<N> lastCombination;
	protected LinkedList<N> lastDependency;
	
	

	//#region PLAYER

		IDbSearchQ() {
			
		}
	
	//#endregion PLAYER
	
	//#region ALGORITHM

		/**
		 * @param root : root for this db-search
		 * @param my_attacker : true if i'm attacker
		 * @param goal_squares : if one occupied by attacker, terminates search
		 * @param attacking : potential winning threat sequences only investigated for attacker
		 * @param max_tier : only threats <= this category can be applied
		 */
		@Override protected boolean visit(N root, boolean my_attacker, MovePair[] goal_squares, boolean attacking, short max_tier) {
			//short level = 1;
			boolean won = false;
			while(!isTimeEnded() && isTreeChanged() && !won) {
				lastDependency.clear();
				won = addDependencyStage(my_attacker);			//uses lastCombination, fills lastDependency
				lastCombination.clear();
				if(!won) won = addCombinationStage(root, root);	//uses lasdtDependency, fills lastCombination
				//level++;
			}
			return won;
		}

		/** (for now) assumptions:
		 * - the game ends only after a dependency stage is added (almost certain about proof)
		 * 	actually not true for mnk game (if you put 3 lined in a board, other 2 in another one, then merge the boards...)
		 */
		protected boolean addDependencyStage(boolean my_attacker) {
			boolean won = false;
			ListIterator<N> it = lastCombination.listIterator();
			while(it.hasNext() && !won)
				won = addDependentChildren(it.next(), my_attacker);
			return won;
		}
		protected boolean addCombinationStage(N root) {
			boolean won = false;
			ListIterator<N> it = lastDependency.listIterator();
			while(it.hasNext() && !won)
				won = findAllCombinationNodes(it.next(), root);
			return won;
		}

	//#endregion ALGORITHM

	//#region AUXILIARY

		//#region BOOL
			/* tree is changed if either lastdCombination o lastDependency are not empty;
			 * however, dependency node are created from other dependency nodes only in the same level,
			 * so such iteration would be useless
			 */
			@Override protected boolean isTreeChanged() {
				return lastCombination.size() > 0;
			}
		//#endregion BOOL
		//#region CREATE
			@Override protected N createRoot() {
				N root = super.createRoot();
				lastCombination.clear();
				lastCombination.add(root);
				return root;
			}
			@Override protected N addDependentChild(N node, AppliedOperator f) {
				N newChild = addChild(node, f, false);
				lastDependency.add(newChild);
				return newChild;
			}
			// SHOULD ADD PRODUCED COMBINATIONS TO lastCombination
			@Override protected abstract boolean addCombination(N A, N B);
		//#endregion CREATE


	
	//#endregion AUXILIARY

	//#region INIT

		@Override protected void initAttributes() {
			super.initAttributes();
			lastCombination = new LinkedList<N>();
		}

	//#endregion INIT

}
