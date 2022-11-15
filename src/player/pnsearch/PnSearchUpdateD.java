/*
 * PnSearch Update WITH PnSearch Delete2 ENHANCEMENT
 */

package player.pnsearch;

import mnkgame.MNKCell;
import player.ArrayBoard;
import player.pnsearch.structures.Nodes.NodeD;
import player.pnsearch.structures.Nodes.Value;



public class PnSearchUpdateD extends PnSearchUpdate {

	protected int nodes_current;
	
	
	
	//#region PLAYER

	public PnSearchUpdateD() {
		super();
	}

	@Override
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		nodes_current = 0;
		return super.selectCell(FC, MC);
	}
	
	@Override
	public String playerName() {
		return "PnSearchUpdate2";
	}
	
	//#endregion PLAYER

	//#region ALGORITHM

		@Override
		protected void developNode(NodeD node) {
			super.developNode(node);
			nodes_current += node.getChildrenLength();
		}
		/**
		 * 
		 * @param <M>
		 * @param <N>
		 * @param node
		 * @param my_turn
		 */
		@Override
		protected NodeD updateAncestorsUpto(NodeD node) {
			NodeD previousNode = node;
			boolean changed = true;
			while(node != null && changed) {
				int oldProof = node.proof, oldDisproof = node.disproof;
				setProofAndDisproofNumbers(node, isMyTurn());
				changed = (oldProof != node.proof || oldDisproof != node.disproof);

				debug.nestedNode(node, 0);

				// delete children
				if((node.proof == 0 || node.disproof == 0) && node != current_root) {
					node.value = (node.proof == 0) ? Value.TRUE : Value.FALSE;
					node.children = null;
				}
				// update ancestors
				previousNode = node;
				node = node.getParent();
				if(node != null && changed) board.unmarkCell();
			}
			return previousNode;
		}
	
	//#endregion ALGORITHM

	//#region INIT
	
		protected void initAttributes() {
			board = new ArrayBoard(M, N, K);
			timer_end = timeout_in_millisecs - 1000;
			runtime = Runtime.getRuntime();
			current_root = newNode();		
			
			debug = new DebugUpdateD("debug/debug-" + playerName(), false);
		}

	//#endregion INIT



	//#region DEBUG

		protected class DebugUpdateD extends Debug {

			public DebugUpdateD(String filename, boolean active) {
				super(filename, active);
			}
			@Override
			protected void info() {
				System.out.println("time" + Long.toString(System.currentTimeMillis() - timer_start));
				System.out.println("nodes created: " + Integer.toString(nodes_created));
				System.out.println("nodes alive: " + Integer.toString(nodes_current));
			}


		}

	//#endregion DEBUG


}
