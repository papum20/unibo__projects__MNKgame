package player.pnsearch.structures;

import mnkgame.MNKCell;



public class INodesA extends INodes {

	/**
	 * A array
	 * @param <M> move type
	 * @param <S> self (same type)
	 */
	public abstract static class Node_a<M extends Move, S extends Node_a<M,S>> extends Node_t<M,S,S[]> {
		protected int children_n;

		public Node_a() {super();}
		public Node_a(M move, S parent, int children_max) {init(move, Value.UNKNOWN, PROOF_N_ZERO, PROOF_N_ZERO, parent, children_max);}
		public Node_a(M move, Value value, short proof, short disproof, int children_max) {init(move, value, proof, disproof, null, children_max);}
		protected void init(M move, Value value, short proof, short disproof, S parent, int children_max) {
			super.init(move, value, proof, disproof, parent);
			children_n = 0;
			//init children
		}

		//#region FUNCTIONS
		public short getChildren_sumProof() {
			short sum = 0;
			for(int i = 0; i < children_n; i++) {
				S child = children[i];
				if(child.proof == PROOF_N_INFINITE) return PROOF_N_INFINITE;
				else sum += child.proof;
			}
			return sum;
		}
		public short getChildren_sumDisproof() {
			short sum = 0;
			for(int i = 0; i < children_n; i++) {
				S child = children[i];
				if(child.disproof == PROOF_N_INFINITE) return PROOF_N_INFINITE;
				else sum += child.disproof;
			}
			return sum;
		}
		public S getChildren_minProof() {
			S min = children[0];
			for(int i = 1; i < children_n; i++)
				if (children[i].proof < min.proof) min = children[i];
			return min;
		}
		public S getChildren_minDisproof() {
			S min = children[0];
			for(int i = 1; i < children_n; i++)
				if(children[i].disproof < min.disproof) min = children[i];
			return min;
		}
		public S findChild(MNKCell move) {
			S res = null;
			int i = 0;
			while(i < children_n && !equalMNKMoves(move, (res=children[i]).move.position) ) i++;
			if(i == children_n) return null;
			else return res;
		}
		public S findChildProof(short proof) {
			S res = null;
			int i = 0;
			while(i < children_n && (res = children[i]).proof != proof) i++;
			if(i == children_n) return null;
			else return res;
		}
		public S findChildDisproof(short disproof) {
			S res = null;
			int i = 0;
			while(i < children_n && (res = children[i]).disproof != disproof) i++;
			if(i == children_n) return null;
			else return res;
		}
		//#endregion FUNCTIONS
		// BOOL
		//public boolean isExpanded();
		// GET
		//public M getMove();
		//public MNKCell getPosition();
		public int getChildrenLength() {
			return children_n;
		}
		public S getFirstChild() {
			return children[0];
		}
		//public S getParent():
		// SET
		//public void addChild(MNKCell move);
		//public void expand();
		public void prove(Value value) {
			super.prove(value);
			children_n = 0;
		}
		//public void setProofDisproof(short proof, short disproof);
		public void reset(M move) {
			this.move = move;
			value = Value.UNKNOWN;
			proof = 1;
			disproof = 1;
			parent = null;
			children = null;
			children_n = 0;
		}
		//public void setParent(S parent);
	}
	
	/**
	 * expand, linkedlist
	 * @param <S> self
	 */
	public abstract static class Node_ae<S extends Node_ae<S>> extends Node_a<Move, S> {
		protected boolean expanded;
		
		public Node_ae(int children_max) {init(null, Value.UNKNOWN, PROOF_N_ZERO, PROOF_N_ZERO, null, children_max);}
		public Node_ae(Move move, S parent, int children_max) {init(move, Value.UNKNOWN, PROOF_N_ZERO, PROOF_N_ZERO, parent, children_max);}
		public Node_ae(Move move, Value value, short proof, short disproof, int children_max) {init(move, value, proof, disproof, null, children_max);}
		protected void init(Move move, Value value, short proof, short disproof, S parent, int children_max) {
			super.init(move, value, proof, disproof, parent, children_max);
			expanded = false;
			//init children
		}

		// BOOL
		public boolean isExpanded() {
			return expanded;
		}
		// GET
		//public S getParent():
		//public M getMove();
		public MNKCell getPosition() {
			return move.position;
		}
		//public int getChildrenLength();
		// SET
		public abstract void addChild(MNKCell move);
		public void expand() {
			expanded = true;
		}
		public void prove(Value value) {
			super.prove(value);
			expanded = false;
		}
		public void reset(Move move) {
			super.reset(move);
			expanded = false;
		}
		//public void setProofDisproof(short proof, short disproof);
		//public void setParent(S parent);
	}

	// for PnSearchADelete
	public abstract static class Node_ad<S extends Node_ad<S>> extends Node_ae<S> {
		public Node_ad() {super(0);}
		//public NodeAD(int children_max) {super(children_max);}
		public Node_ad(Move move, S parent) {super(move, parent, 0);}
		//public NodeAD(Move move, NodeAD parent, int children_max) {super(move, parent, children_max);}
		//public NodeAD(Move move, Value value, short proof, short disproof, int children_max) {super(move, value, proof, disproof, children_max);}
		@Override
		protected void init(Move move, Value value, short proof, short disproof, S parent, int children_max) {
			this.move = move;
			this.value = value;
			this.proof = proof;
			this.disproof = disproof;
			this.parent = parent;
			children = null;
			children_n = 0;
		}
		
		// FUNCTIONS
		//functions about children should be redefined to check whether children==null;
		//however some are only called if node is expanded

		// deletes all children but the one with the same values of proof-disproof
		// assumes that this node is proved
		public void reduce() {
			value = (proof == 0) ? Value.TRUE : Value.FALSE;
			int i = 0;
			//just checks child.proof==proof: child.disproof==disproof is obvious, if the nodes are proved
			while(i < children_n && children[i].proof != proof) i++;
			children[0] = children[i];
			children_n = 1;
		}
		// SET
		public abstract void addChild(MNKCell move);
		public abstract void expand(int children_max);
	}

	// for PnSearchStore : STORES THE MOST PROVING NODE
	public abstract static class Node_ads<S extends Node_ads<S>> extends Node_ad<S> {
		public S most_proving;
		
		public Node_ads() {super();}
		public Node_ads(Move move, S parent) {super(move, parent);}
		@Override
		protected void init(Move move, Value value, short proof, short disproof, S parent) {
			super.init(move, value, proof, disproof, parent);
			most_proving = null;
		}

		// SET
		@Override
		public void reset(Move move) {
			super.reset(move);
			most_proving = null;
		}
	}
	
	
}
