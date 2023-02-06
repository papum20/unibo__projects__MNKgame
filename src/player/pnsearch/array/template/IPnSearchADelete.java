package player.pnsearch.array.template;

import mnkgame.MNKCell;
import player.pnsearch.structures.INodes.IMove;
import player.pnsearch.structures.INodesA.Node_ad;



public abstract class IPnSearchADelete<M extends IMove, V, N extends Node_ad<M,V,N>> extends IPnSearchA<M,V,N> {


	//#region PLAYER

		public IPnSearchADelete() {
				
		}

		public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {

			// DEBUG
			System.out.println("--------\t" + MC.length + "\t--------");
			debug.open();
			nodes_created = 0;
			nodes_alive = 0;

			//start conting time for this turn
			timer_start = System.currentTimeMillis();
			//update my istance of board
			if(MC.length > 0) {
				M opponent_move = newMove(MC[MC.length - 1]);
				//mark opponent cell
				board.markCell(opponent_move.i(), opponent_move.j());
				//update current_root (with last opponent move)
				//assumption: current_root != null
				N new_root = current_root.findChild(opponent_move);
				if(new_root != null) {
					System.out.println("found opponent move in tree.");
					nodes_alive_tot -= current_root.getChildrenLength();
					current_root = new_root;
					current_root.setParent(null);
				}
				else current_root.reset(opponent_move);
				
				// DEBUG
				System.out.println("last/opponent: " + MC[MC.length - 1]);
			}
			// DEBUG
			debug.markedCells(0);
			if(current_root.getMove() != null) System.out.println(current_root.getPosition());
			//recursive call for each possible move
			try{
				visit(current_root);
			} catch (NullPointerException e) {
				System.out.println("VISIT: NULL EXCEPTION");
				throw e;
			} catch(ArrayIndexOutOfBoundsException e) {
				System.out.println("VISIT: ARRAY BOUNDS EXCEPTION");
				throw e;
			}
			// DEBUG
			debug.markedCells(0);
			if(current_root.getMove() != null) System.out.println(current_root.getPosition());
			nodes_created_tot += nodes_created;
			nodes_alive_tot += nodes_alive;
			
			N best_node = getBestNode();
			MNKCell res = FC[0];
			if(best_node != null) {
				System.out.println("FOUND BEST NODE");
				res = new MNKCell(best_node.i(), best_node.j());
				//update current_root (with my last move)
				nodes_alive_tot -= current_root.getChildrenLength();
				current_root = best_node;
			};
			//update my istance of board
			board.markCell(res.i, res.j);								//mark my cell

			// DEBUG
			debug.info();
			debug.close();
			System.out.println("my move: " + res);

			return res;
		}
		
		public String playerName() {
			return "PnSearchADelete";
		}

	//#endregion PLAYER

	//#region ALGORITHM

		/**
		 * 
		 * @param node
		 */
		protected void developNode(N node) {
			node.expand(board.FreeCells_length());
			generateAllChildren(node);
			for(int i = 0; i < node.getChildrenLength(); i++) {
				N child = node.children[i];
				board.markCell(child.i(), child.j());
				evaluate(child);
				setProofAndDisproofNumbers(child, isMyTurn());
				board.unmarkCell();
			}
			nodes_created += node.getChildrenLength();
			nodes_alive += node.getChildrenLength();
		}

	//#endregion ALGORITHM

	//#region INIT
	
		@Override
		protected abstract N newNode();
		@Override
		protected abstract N newNode(int children_max);

	//#endregion INIT

}
