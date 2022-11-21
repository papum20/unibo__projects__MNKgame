package player.pnsearch.structures;



public class INodesA extends INodes {

	/**
	 * A array
	 * @param <M> move type
	 * @param <S> self (same type)
	 */
	public abstract static class Node_a<M extends IMove, V, S extends Node_a<M,V,S>> extends Node_t<M,V,S,S[]> {
		protected int children_n;

		public Node_a()										{super();}
		public Node_a(int children_max) 					{init(null, PROOF_N_ZERO, PROOF_N_ZERO, null, children_max);}
		public Node_a(M move, S parent)						{init(move, PROOF_N_ZERO, PROOF_N_ZERO, parent);}
		public Node_a(M move, S parent, int children_max)	{init(move, PROOF_N_ZERO, PROOF_N_ZERO, parent, children_max);}
		//public Node_a(short i, short j, V value, short proof, short disproof, int children_max) {init(i, j, proof, disproof, null, children_max, value);}
		protected void init(M move, short proof, short disproof, S parent, int children_max) {
			setMove(move);
			this.proof = proof;
			this.disproof = disproof;
			this.parent = parent;
			initChildren(children_max);
		}
		protected void init(M move, short proof, short disproof, S parent, int children_max, Value value) {
			setMove(move);
			setValue(value);
			this.proof = proof;
			this.disproof = disproof;
			this.parent = parent;
			initChildren(children_max);
		}

		//#region FUNCTIONS
		@Override public short getChildren_sumProof() {
			short sum = 0;
			for(int i = 0; i < children_n; i++) {
				S child = children[i];
				if(child.proof == PROOF_N_INFINITE) return PROOF_N_INFINITE;
				else sum += child.proof;
			}
			return sum;
		}
		@Override public short getChildren_sumDisproof() {
			short sum = 0;
			for(int i = 0; i < children_n; i++) {
				S child = children[i];
				if(child.disproof == PROOF_N_INFINITE) return PROOF_N_INFINITE;
				else sum += child.disproof;
			}
			return sum;
		}
		@Override public S getChildren_minProof() {
			S min = children[0];
			for(int i = 1; i < children_n; i++)
				if (children[i].proof < min.proof) min = children[i];
			return min;
		}
		@Override public S getChildren_minDisproof() {
			S min = children[0];
			for(int i = 1; i < children_n; i++)
				if(children[i].disproof < min.disproof) min = children[i];
			return min;
		}
		@Override public S findChild(M move) {
			S res = null;
			int k = 0;
			while(k < children_n && !move.equals((res=children[k]).getMove()) ) k++;
			if(k == children_n) return null;
			else return res;
		}
		@Override public S findChildProof(short proof) {
			S res = null;
			int i = 0;
			while(i < children_n && (res = children[i]).proof != proof) i++;
			if(i == children_n) return null;
			else return res;
		}
		@Override public S findChildDisproof(short disproof) {
			S res = null;
			int i = 0;
			while(i < children_n && (res = children[i]).disproof != disproof) i++;
			if(i == children_n) return null;
			else return res;
		}
		// deletes all children but the one with the same values of proof-disproof
		// assumes that this node is proved
		//#endregion FUNCTIONS
		// BOOL
		//return true if node value is "unknown" and has children
		@Override public boolean isExpanded() {return proof != 0 && disproof != 0 && children_n > 0;}
		// GET
		@Override public int getChildrenLength() {return children_n;}
		@Override public S getFirstChild() {return children[0];}
		// SET
		@Override public void reset(M move) {
			super.reset(move);
			children_n = 0;
		}
		@Override public void reduce() {
			evalValue();
			int i = 0;
			while(i < children_n && (children[i].proof != proof || children[i].disproof != disproof)) i++;
			children[0] = children[i];
			children_n = 1;
		}
		@Override public void prove() {
			super.prove();
			children_n = 0;
		}
		@Override public void prove(Value value) {
			super.prove(value);
			children_n = 0;
		}
		@Override public void expand() {}
		// INIT
		@Override protected void initChildren() {
			children = null;
			children_n = 0;
		}
		protected abstract void initChildren(int children_max);
		@Override @Deprecated protected void generateChildren() {}
	}

	// for PnSearchADelete
	public abstract static class Node_ad<M extends IMove, V, S extends Node_ad<M,V,S>> extends Node_a<M,V,S> {

		public Node_ad()					{init(null, PROOF_N_ZERO, PROOF_N_ZERO, null);}
		public Node_ad(M move, S parent)	{init(move, PROOF_N_ZERO, PROOF_N_ZERO, parent);}
		//public NodeAD(int children_max) {super(children_max);}
		//public NodeAD(Move move, NodeAD parent, int children_max) {super(move, parent, children_max);}
		//public NodeAD(Move move, Value value, short proof, short disproof, int children_max) {super(move, value, proof, disproof, children_max);
		
		// FUNCTIONS
		//functions about children should be redefined to check whether children==null;
		//however some are only called if node is expanded
		// SET
		@Override @Deprecated public void expand() {}
		public void expand(int children_max) {generateChildren(children_max);}
		// INIT
		@Override @Deprecated protected void initChildren(int children_max) {initChildren();}
		@Override @Deprecated protected void generateChildren() {}
		protected abstract void generateChildren(int children_max);
	}

	// for PnSearchStore : STORES THE MOST PROVING NODE
	public abstract static class Node_ads<M extends IMove, V, S extends Node_ads<M,V,S>> extends Node_ad<M,V,S> {
		public S most_proving;
		
		public Node_ads()					{super();}
		public Node_ads(M move, S parent)	{super(move, parent);}
		@Override protected void init(M move, short proof, short disproof, S parent) {
			super.init(move, proof, disproof, parent);
			most_proving = null;
		}

		// SET
		@Override public void reset(M move) {
			super.reset(move);
			most_proving = null;
		}
		@Override public void reduce() {
			evalValue();
			children[0] = most_proving;
			children_n = 1;
		}
	}
	
	
}
