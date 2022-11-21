package player.pnsearch.structures;

import mnkgame.MNKCell;



public class INodes {

	public static final short PROOF_N_ZERO = 0;
	public static final short PROOF_N_INFINITE = 32767;
	public static final short SHORT_ERROR = -1;

	
	//#region INTERFACES

		public static enum Value {
			TRUE,
			FALSE,
			UNKNOWN
		}

		public static interface IMove {
			public MovePair getPair();
			public short i();
			public short j();
			@Override public String toString();
			public <M extends IMove> boolean equals(M move);
		}
		public static class MovePair implements IMove {
			protected short i, j;
			public MovePair(short i, short j) {
				this.i = i;
				this.j = j;
			}
			public MovePair(MovePair move) {
				this.i = move.i;
				this.j = move.j;
			}
			public MovePair(MNKCell move) {
				this.i = (short)move.i;
				this.j = (short)move.j;
			}

			public MovePair getPair() {return this;}
			public short i() {return i;}
			public short j() {return j;}
			public String toString() {return "[" + i + "," + j + "]";}
			@Override public <M extends IMove> boolean equals(M move) {return i == move.i() && j == move.j();}
		}
		
		public static class MoveMNK implements IMove {
			protected MNKCell position;

			public MoveMNK() {

			}
			public MoveMNK(MNKCell move) {
				this.position = move;
			}
			public MovePair getPair() {return new MovePair((short)position.j, (short)position.i);}
			public short i() {return (short)position.j;}
			public short j() {return (short)position.i;}
			public String toString() {return position.toString();}
			@Override public <M extends IMove> boolean equals(M move) {return position.i == move.i() && position.j == move.j();}
		}
		
		/**
		 * @param <M> move type
		 * @param <V> value
		 * @param <S> self (same type)
		 */
		public static interface INode<M extends IMove, V, S extends INode<M,V,S>> {
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
			public S findChild(M move);
			public S findChildProof(short proof);
			public S findChildDisproof(short disproof);
			
			// BOOL
			public boolean isExpanded();
			// GET
			public M getMove();
			public MovePair getPosition();
			public short i();
			public short j();
			
			public int getChildrenLength();
			public S getFirstChild();
			public S getParent();
			
			public V getValue();
			// SET
			public void reset(M move);
			public void reduce();
			
			public void prove();				//prove or disprove node (making it not expanded)
			public void prove(Value value);
			public void setProofDisproof(short proof, short disproof);
			
			public void expand();
			public abstract void setValue(Value value);

			public void addChild(M move);
			public void setParent(S parent);

		}
		/**
		 * defines some functions and variables
		 * @param <M> move type
		 * @param <S> self (same type)
		 * @param <A> collection (children)
		 */
		public static abstract class Node_t<M extends IMove, V, S extends Node_t<M,V,S,A>, A> implements INode<M,V,S> {
			public short proof;
			public short disproof;
			protected S parent;
			public A children;

			public Node_t()								{init(null, PROOF_N_ZERO, PROOF_N_ZERO, null);}
			public Node_t(M move, S parent)				{init(move, PROOF_N_ZERO, PROOF_N_ZERO, parent);}
			//public Node_t(short i, short j, V value, short proof, short disproof) {init(i, j, proof, disproof, null, value);}
			protected void init(M move, short proof, short disproof, S parent) {
				setMove(move);
				this.proof = proof;
				this.disproof = disproof;
				this.parent = parent;
				initChildren();
			}
			protected void init(M move, short proof, short disproof, S parent, Value value) {
				setMove(move);
				setValue(value);
				this.proof = proof;
				this.disproof = disproof;
				this.parent = parent;
				initChildren();
			}

			// GET
			@Override public S getParent() {return parent;}
			// SET
			@Override public void prove() {
				evalValue();
				children = null;
			}
			@Override public void prove(Value value) {
				setValue(value);
				children = null;
			}
			@Override public void setProofDisproof(short proof, short disproof) {
				this.proof = proof;
				this.disproof = disproof;
			}
			@Override public void reset(M move) {
				setMove(move);
				resetValue();
				proof = 1;
				disproof = 1;
				parent = null;
				children = null;
			}
			@Override public void setParent(S parent) {this.parent = parent;}
			// INIT
			protected abstract void setMove(M move);
			protected void resetValue() {}
			protected void evalValue() {}	//value = (proof == 0) ? Value.TRUE : Value.FALSE;
			protected abstract void initChildren();
			protected abstract void generateChildren();
		}

	//#endregion INTERFACES


	//#region FUNCTIONS
		
		public INodes() {

		}

		/*
		protected static <M extends MovePair> boolean equalMovePairs(M a, M b) {
			return a.i == b.i && a.j == b.j;
		}
		*/
		protected static boolean equalMNKMoves(MNKCell a, MNKCell b) {
			return a.i == b.i && a.j == b.j;
		}

	//#endregion FUNCTIONS

}
