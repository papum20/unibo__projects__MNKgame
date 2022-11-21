package player.pnsearch.structures;



public class NodesAE extends INodesA {
	
	//INSTANCE : Node with arrays
	public static class NodeAE extends Node_a<MovePair,Value,NodeAE> {
		protected short i, j;
		protected boolean expanded;
		
		public NodeAE()													{init(null, PROOF_N_ZERO, PROOF_N_ZERO, null);}
		public NodeAE(int children_max) 								{init(null, PROOF_N_ZERO, PROOF_N_ZERO, null, children_max);}
		public NodeAE(MovePair move, NodeAE parent)						{init(move, PROOF_N_ZERO, PROOF_N_ZERO, parent);}
		public NodeAE(MovePair move, NodeAE parent, int children_max)	{init(move, PROOF_N_ZERO, PROOF_N_ZERO, parent, children_max);}
		
		// GET
		@Override public MovePair getMove() {return new MovePair(i, j);}
		@Override public MovePair getPosition() {return new MovePair(i, j);}
		@Override public short i() {return i;}
		@Override public short j() {return j;}
		@Override public Value getValue(
			
		) {
			if(proof == PROOF_N_ZERO) return Value.TRUE;
			else if(disproof == PROOF_N_ZERO) return Value.FALSE;
			else return Value.UNKNOWN;
		}
		// SET
		@Override public void setValue(Value value) {
			if(value == Value.TRUE) setProofDisproof(PROOF_N_ZERO, PROOF_N_INFINITE);
			else if(value == Value.FALSE) setProofDisproof(PROOF_N_INFINITE, PROOF_N_ZERO);
			else setProofDisproof((short)1, (short)1);
		}
		public void addChild(MovePair move) {children[children_n++] = new NodeAE(move, this, children.length);}
		// INIT
		@Override protected void setMove(MovePair move) {
			if(move != null) {
				this.i = move.i();
				this.j = move.j();
			}
		}
		@Override protected void initChildren(int children_max) {
			children = new NodeAE[children_max];
			children_n = 0;
		}

		// EXPANDED:
		@Override protected void init(MovePair move, short proof, short disproof, NodeAE parent) {
			super.init(move, proof, disproof, parent);
			expanded = false;
		}
		@Override protected void init(MovePair move, short proof, short disproof, NodeAE parent, Value value) {
			super.init(move, proof, disproof, parent, value);
			expanded = false;
		}
		@Override protected void init(MovePair move, short proof, short disproof, NodeAE parent, int children_max) {
			super.init(move, proof, disproof, parent, children_max);
			expanded = false;
		}
		@Override protected void init(MovePair move, short proof, short disproof, NodeAE parent, int children_max, Value value) {
			super.init(move, proof, disproof, parent, children_max, value);
			expanded = false;
		}
		// BOOL
		@Override public boolean isExpanded() {return expanded;}
		// SET
		@Override public void expand() {expanded = true;}
		@Override public void prove() {
			super.prove();
			expanded = false;
		}
		@Override public void prove(Value value) {
			super.prove(value);
			expanded = false;
		}
		@Override public void reset(MovePair move) {
			super.reset(move);
			expanded = false;
		}
	}
	
	public static class NodeAED extends Node_ad<MovePair,Value,NodeAED> {
		protected short i, j;
		protected boolean expanded;

		public NodeAED()								{super();}
		//public NodeAD(int children_max) {super(children_max);}
		public NodeAED(MovePair move, NodeAED parent)	{super(move, parent);}
		//public NodeAD(Move move, NodeAD parent, int children_max) {super(move, parent, children_max);}
		//public NodeAD(Move move, Value value, short proof, short disproof, int children_max) {super(move, value, proof, disproof, children_max);}
		
		// GET
		@Override public MovePair getMove() {return new MovePair(i, j);}
		@Override public MovePair getPosition() {return new MovePair(i, j);}
		@Override public short i() {return i;}
		@Override public short j() {return j;}
		@Override public Value getValue() {
			if(proof == PROOF_N_ZERO) return Value.TRUE;
			else if(disproof == PROOF_N_ZERO) return Value.FALSE;
			else return Value.UNKNOWN;
		}
		// SET
		@Override public void setValue(Value value) {
			if(value == Value.TRUE) setProofDisproof(PROOF_N_ZERO, PROOF_N_INFINITE);
			else if(value == Value.FALSE) setProofDisproof(PROOF_N_INFINITE, PROOF_N_ZERO);
			else setProofDisproof((short)1, (short)1);
		}
		public void addChild(MovePair move) {children[children_n++] = new NodeAED(move, this);}
		// INIT
		@Override protected void setMove(MovePair move) {
			if(move != null) {
				this.i = move.i();
				this.j = move.j();
			}
		}
		@Override protected void initChildren(int children_max) {
			children = new NodeAED[children_max];
			children_n = 0;
		}
		@Override protected void generateChildren(int children_max) {children = new NodeAED[children_max];}
		
		// EXPANDED:
		@Override protected void init(MovePair move, short proof, short disproof, NodeAED parent) {
			super.init(move, proof, disproof, parent);
			expanded = false;
		}
		@Override protected void init(MovePair move, short proof, short disproof, NodeAED parent, Value value) {
			super.init(move, proof, disproof, parent, value);
			expanded = false;
		}
		@Override protected void init(MovePair move, short proof, short disproof, NodeAED parent, int children_max) {
			super.init(move, proof, disproof, parent, children_max);
			expanded = false;
		}
		@Override protected void init(MovePair move, short proof, short disproof, NodeAED parent, int children_max, Value value) {
			super.init(move, proof, disproof, parent, children_max, value);
			expanded = false;
		}
		// BOOL
		@Override public boolean isExpanded() {return expanded;}
		// SET
		@Override public void expand() {expanded = true;}
		@Override public void prove() {
			super.prove();
			expanded = false;
		}
		@Override public void prove(Value value) {
			super.prove(value);
			expanded = false;
		}
		@Override public void reset(MovePair move) {
			super.reset(move);
			expanded = false;
		}
	}

	public static class NodeAEDS extends Node_ads<MovePair,Value,NodeAEDS> {
		protected short i, j;
		protected boolean expanded;
		
		public NodeAEDS()								{super();}
		public NodeAEDS(MovePair move, NodeAEDS parent) {super(move, parent);}

		// GET
		@Override public MovePair getMove() {return new MovePair(i, j);}
		@Override public MovePair getPosition() {return new MovePair(i, j);}
		@Override public short i() {return i;}
		@Override public short j() {return j;}
		@Override public Value getValue() {
			if(proof == PROOF_N_ZERO) return Value.TRUE;
			else if(disproof == PROOF_N_ZERO) return Value.FALSE;
			else return Value.UNKNOWN;
		}
		// SET
		@Override public void setValue(Value value) {
			if(value == Value.TRUE) setProofDisproof(PROOF_N_ZERO, PROOF_N_INFINITE);
			else if(value == Value.FALSE) setProofDisproof(PROOF_N_INFINITE, PROOF_N_ZERO);
			else setProofDisproof((short)1, (short)1);
		}
		@Override public void addChild(MovePair move) {children[children_n++] = new NodeAEDS(move, this);}
		// INIT
		@Override protected void setMove(MovePair move) {
			if(move != null) {
				this.i = move.i();
				this.j = move.j();
			}
		}
		@Override protected void initChildren(int children_max) {
			children = new NodeAEDS[children_max];
			children_n = 0;
		}
		@Override protected void generateChildren(int children_max) {children = new NodeAEDS[children_max];}
		
		// EXPANDED:
		@Override protected void init(MovePair move, short proof, short disproof, NodeAEDS parent) {
			super.init(move, proof, disproof, parent);
			expanded = false;
		}
		@Override protected void init(MovePair move, short proof, short disproof, NodeAEDS parent, Value value) {
			super.init(move, proof, disproof, parent, value);
			expanded = false;
		}
		@Override protected void init(MovePair move, short proof, short disproof, NodeAEDS parent, int children_max) {
			super.init(move, proof, disproof, parent, children_max);
			expanded = false;
		}
		@Override protected void init(MovePair move, short proof, short disproof, NodeAEDS parent, int children_max, Value value) {
			super.init(move, proof, disproof, parent, children_max, value);
			expanded = false;
		}
		// BOOL
		@Override public boolean isExpanded() {return expanded;}
		// SET
		@Override public void expand() {expanded = true;}
		@Override public void prove() {
			super.prove();
			expanded = false;
		}
		@Override public void prove(Value value) {
			super.prove(value);
			expanded = false;
		}
		@Override public void reset(MovePair move) {
			super.reset(move);
			expanded = false;
		}
	}

}
