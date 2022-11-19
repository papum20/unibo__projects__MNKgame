package player.pnsearch.structures;

import java.util.Collection;
import java.util.LinkedList;

import mnkgame.MNKCell;

public class INodesC extends Nodes {


	/**
	 * A extends Collection
	 * @param <M> move type
	 * @param <S> self (same type)
	 * @param <A> collection (children)
	 */
	public abstract static class Node_c<M extends Move, S extends Node_c<M,S,A>, A extends Collection<S>> extends Node_t<M,S,A> {

		public Node_c() {super();}
		public Node_c(M move, S parent) {super(move, parent);}
		public Node_c(M move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}
		//protected void init(M move, Value value, short proof, short disproof, S parent);

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
				if(equalMNKMoves(move, child.move.position)) {
					res = child;
					break;
				}
			}
			return res;
		}
		public S findChildProof(short proof) {
			S res = null;
			for(S child : children) {
				if(child.proof == proof) {
					res = child;
					break;
				}
			}
			return res;
		}
		public S findChildDisproof(short disproof) {
			S res = null;
			for(S child : children) {
				if(child.disproof == disproof) {
					res = child;
					break;
				}
			}
			return res;
		}
		// BOOL
		//public boolean isExpanded();
		// GET
		//public M getMove();
		//public MNKCell getPosition();
		public int getChildrenLength() {
			return children.size();
		}
		//public S getFirstChild();
		//public S getParent():
		// SET
		//public void addChild(MNKCell move);
		//public void expand();
		//public void prove(Value value);
		//public void reset(M move);
		//public void setProofDisproof(short proof, short disproof);
		//public void setParent(S parent);
	}

	/**
	 * expand, linkedlist
	 * @param <S> self
	 */
	public abstract static class Node_e<S extends Node_e<S>> extends Node_c<Move, S, LinkedList<S>> {
		protected boolean expanded;
		
		public Node_e() {super();}
		public Node_e(Move move, S parent) {super(move, parent);}
		public Node_e(Move move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}
		@Override
		protected void init(Move move, Value value, short proof, short disproof, S parent) {
			super.init(move, value, proof, disproof, parent);
			this.expanded = false;
			this.children = new LinkedList<S>();
		}

		// FUNCTIONS
		//public short getChildren_sumProof();
		//public short getChildren_sumDisproof();
		//public short getChildren_minProof();
		//public short getChildren_minDisproof();
		//public S findChild(MNKCell move);
		//public S findChildProof(short proof);
		//public S findChildDisproof(short disproof);
		// BOOL
		public boolean isExpanded() {
			return expanded;
		}
		// GET
		//public M getMove();
		public MNKCell getPosition() {
			return move.position;
		}
		//public int getChildrenLength();
		public S getFirstChild() {
			return children.getFirst();
		}
		//public S getParent():
		// SET
		public abstract void addChild(MNKCell move);
		public void expand() {
			expanded = true;
		}
		@Override
		public void prove(Value value) {
			super.prove(value);
			expanded = false;
		}
		@Override
		public void reset(Move move) {
			super.reset(move);
			expanded = false;
		}
		//public void setProofDisproof(short proof, short disproof);
		//public void setParent(S parent);
	}

	public abstract static class Node_d<S extends Node_d<S>> extends Node_e<S> {
		public Node_d() {super();}
		public Node_d(Move move, S parent) {super(move, parent);}
		public Node_d(Move move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}
		@Override
		protected void init(Move move, Value value, short proof, short disproof, S parent) {
			this.move = move;
			this.value = value;
			this.proof = proof;
			this.disproof = disproof;
			this.parent = parent;
			this.expanded = false;
			this.children = null;
		}
		
		// FUNCTIONS
		//functions about children should be redefined to check whether children==null;
		//however some are only called if node is expanded
		public S findChild(MNKCell move) {
			if(children == null) return null;
			else return super.findChild(move);
		}
		// SET
		public abstract void addChild(MNKCell move);
		public void expand() {
			expanded = true;
			children = new LinkedList<S>();
		}
		public void prove(Value value) {
			super.prove(value);
			expanded = false;
		}
	}

	// STORES THE MOST PROVING NODE
	public abstract static class Node_ds<S extends Node_ds<S>> extends Node_d<S> {
		public S most_proving;
		
		public Node_ds() {super();}
		public Node_ds(Move move, S parent) {super(move, parent);}
		public Node_ds(Move move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}
		@Override
		protected void init(Move move, Value value, short proof, short disproof, S parent) {
			super.init(move, value, proof, disproof, parent);
			most_proving = null;
		}
	}


}
