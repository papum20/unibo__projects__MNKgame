package player.pnsearch;

import java.util.LinkedList;
import mnkgame.MNKCell;



public class Nodes {

	protected static final short PROOF_N_ZERO = 0;
	protected static final short PROOF_N_INFINITE = 32767;

	

	//#region CLASSES

	
		protected static enum Value {
			TRUE,
			FALSE,
			UNKNOWN
		}

		protected static class Move {
			protected MNKCell position;

			public Move() {

			}
			public Move(MNKCell move) {
				this.position = move;
			}
		}
		
		/**
		 * @param <M> move type
		 * @param <S> self (same type)
		 */
		protected static abstract class Node_t<M extends Move, S extends Node_t<M,S>> {
			protected M move;
			public Value value;
			public short proof;
			public short disproof;
			protected S parent;
			public LinkedList<S> children;

			public Node_t() {
				init(null, Value.UNKNOWN, PROOF_N_ZERO, PROOF_N_ZERO, null);
			}
			public Node_t(M move, S parent) {
				init(move, Value.UNKNOWN, PROOF_N_ZERO, PROOF_N_ZERO, parent);
			}
			public Node_t(M move, Value value, short proof, short disproof) {
				init(move, value, proof, disproof, null);
			}
			protected void init(M move, Value value, short proof, short disproof, S parent) {
				this.move = move;
				this.value = value;
				this.proof = proof;
				this.disproof = disproof;
				this.parent = parent;
				this.children = new LinkedList<S>();
			}

			// FUNCTIONS
			public short getChildren_sumProof() {
				short sum = 0;
				for(S child : children) {
					if(child.proof == PROOF_N_INFINITE) return PROOF_N_INFINITE;
					else sum += child.proof;
				}
				return sum;
			}
			public short getChildren_sumDisproof() {
				short sum = 0;
				for(S child : children) {
					if(child.disproof == PROOF_N_INFINITE) return PROOF_N_INFINITE;
					else sum += child.disproof;
				}
				return sum;
			}
			public short getChildren_minProof() {
				short min = PROOF_N_INFINITE;
				for(S child : children)
				if (child.proof < min) min = child.proof;
				return min;
			}
			public short getChildren_minDisproof() {
				short min = PROOF_N_INFINITE;
				for(S child : children) 
				if(child.disproof < min) min = child.disproof;
				return min;
			}
			public S findChild(MNKCell move) {
				S res = null;
				for(S child : children) {
					if(compareMNKMoves(move, child.move.position)) {
						res = child;
						break;
					}
				}
				return res;
			}
			// BOOL
			public abstract boolean isExpanded();
			// GET
			public S getParent() {
				return parent;
			}
			// SET
			public abstract void addChild(MNKCell move);
			public abstract void expand();
			public void setMove(M move) {
				this.move = move;
			}
			public void setParent(S parent) {
				this.parent = parent;
			}
			public void setProofDisproof(short proof, short disproof) {
				this.proof = proof;
				this.disproof = disproof;
			}

		}
		// INSTANCE FOR PnSearch
		public static class Node extends Node_t<Move, Node> {
			protected boolean expanded;
			
			public Node() {
				super();
			}
			public Node(Move move, Node parent) {
				super(move, parent);
			}
			public Node(Move move, Value value, short proof, short disproof) {
				super(move, value, proof, disproof);
			}
			protected void init(Move move, Value value, short proof, short disproof, Node parent) {
				this.move = move;
				this.value = value;
				this.proof = proof;
				this.disproof = disproof;
				this.expanded = false;
				this.parent = parent;
				this.children = new LinkedList<Node>();
			}
			
			// BOOL
			@Override
			public boolean isExpanded() {
				return expanded;
			}
			// SET
			@Override
			public void addChild(MNKCell move) {
				children.addLast(new Node(new Move(move), this));
			}
			@Override
			public void expand() {
				expanded = true;
			}
		}
		// INSTANCE FOR PnSearchDelete
		protected class NodeD extends Node {
			public NodeD() {
				super();
			}
			public NodeD(Move move, Node parent) {
				super(move, parent);
			}
			public NodeD(Move move, Value value, short proof, short disproof) {
				super(move, value, proof, disproof);
			}
			protected void init(Move move, Value value, short proof, short disproof, Node parent) {
				this.move = move;
				this.value = value;
				this.proof = proof;
				this.disproof = disproof;
				this.expanded = false;
				this.parent = parent;
				this.children = null;
			}

		}
	
	//#endregion CLASSES



	//#region FUNCTIONS
		
		public Nodes() {

		}

		protected static boolean compareMNKMoves(MNKCell a, MNKCell b) {
			return a.i == b.i && a.j == b.j;
		}

	//#endregion FUNCTIONS

}
