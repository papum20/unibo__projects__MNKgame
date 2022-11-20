package player.pnsearch.structures;

import mnkgame.MNKCell;



public class INodes {

	public static final short PROOF_N_ZERO = 0;
	public static final short PROOF_N_INFINITE = 32767;

	
	//#region INTERFACES

		public static enum Value {
			TRUE,
			FALSE,
			UNKNOWN
		}

		public static class Move {
			protected MNKCell position;

			public Move() {

			}
			public Move(MNKCell move) {
				this.position = move;
			}
		}
		
		/**
		 * @param <M> move type
		 * @param <V> value
		 * @param <S> self (same type)
		 */
		public static interface INode<M extends Move, V, S extends INode<M,V,S>> {
			// protected M move;
			// public Value value;
			// public short proof;
			// public short disproof;
			// protected S parent;
			// protected LinkedList<S> children;

			//protected void init(M move, Value value, short proof, short disproof, S parent);

			// FUNCTIONS
			public short getChildren_sumProof();
			public short getChildren_sumDisproof();
			public S getChildren_minProof();
			public S getChildren_minDisproof();
			public S findChild(MNKCell move);
			public S findChildProof(short proof);
			public S findChildDisproof(short disproof);
			public void reduce();
			// BOOL
			public boolean isExpanded();
			// GET
			public M getMove();
			public MNKCell getPosition();
			public int getChildrenLength();
			public S getFirstChild();
			public S getParent();
			// SET
			public void addChild(MNKCell move);
			public void expand();
			public void prove(V value);			//prove or disprove node (making it not expanded)
			public void setProofDisproof(short proof, short disproof);
			public void reset(M move);
			public void setParent(S parent);

		}
		/**
		 * defines some functions and variables
		 * @param <M> move type
		 * @param <S> self (same type)
		 * @param <A> collection (children)
		 */
		public static abstract class Node_t<M extends Move, S extends Node_t<M,S,A>, A> implements INode<M,Value,S> {
			protected M move;
			public Value value;
			public short proof;
			public short disproof;
			protected S parent;
			public A children;

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
				//init this.children
			}

			// FUNCTIONS
			//public short getChildren_sumProof();
			//public short getChildren_sumDisproof();
			//public S getChildren_minProof();
			//public S getChildren_minDisproof();
			//public S findChild(MNKCell move);
			//public S findChildProof(short proof);
			//public S findChildDisproof(short disproof);
			// BOOL
			//public boolean isExpanded();
			// GET
			public M getMove() {
				return move;
			}
			//public S getFirstChild();
			public S getParent() {
				return parent;
			}
			//public MNKCell getPosition();
			//public int getChildrenLength();
			// SET
			//public void addChild(MNKCell move);
			//public void expand();
			public void prove(Value value) {
				this.value = value;
				children = null;
			}
			public void setProofDisproof(short proof, short disproof) {
				this.proof = proof;
				this.disproof = disproof;
			}
			public void reset(M move) {
				this.move = move;
				value = Value.UNKNOWN;
				proof = 1;
				disproof = 1;
				parent = null;
				children = null;
			}
			public void setParent(S parent) {
				this.parent = parent;
			}
		}

	//#endregion INTERFACES


	//#region FUNCTIONS
		
		public INodes() {

		}

		protected static boolean equalMNKMoves(MNKCell a, MNKCell b) {
			return a.i == b.i && a.j == b.j;
		}

	//#endregion FUNCTIONS

}
