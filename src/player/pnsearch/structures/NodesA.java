package player.pnsearch.structures;



public class NodesA extends INodesA {
	
	//INSTANCE : Node with arrays
	public static class NodeAE extends Node_ae<MovePair,Value,NodeAE> {
		short i, j;
		
		public NodeAE(int children_max)									{super(children_max);}
		public NodeAE(MovePair move, NodeAE parent, int children_max)	{super(move, parent, children_max);}
		//public NodeAE(Move move, Value value, short proof, short disproof, int children_max) {super(move, value, proof, disproof, children_max);}
		
		// BOOL
		@Override protected boolean equalMoves(MovePair a, MovePair b) {return a.i == b.i && a.j == b.j;}
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
		public void addChild(MovePair move) {children[children_n++] = new NodeAE(move, this, children.length);}
		// INIT
		@Override protected void setMove(MovePair move) {
			this.i = move.i();
			this.j = move.j();
		}
		@Override @Deprecated protected void resetValue() {}
		@Override @Deprecated protected void evalValue() {}
		@Override protected void initChildren(int children_max) {
			children = new NodeAE[children_max];
			children_n = 0;
		}
	}
	
	public static class NodeAED extends Node_aed<MovePair,Value,NodeAED> {
		short i, j;

		public NodeAED()								{super();}
		//public NodeAD(int children_max) {super(children_max);}
		public NodeAED(MovePair move, NodeAED parent)	{super(move, parent);}
		//public NodeAD(Move move, NodeAD parent, int children_max) {super(move, parent, children_max);}
		//public NodeAD(Move move, Value value, short proof, short disproof, int children_max) {super(move, value, proof, disproof, children_max);}
		
		// BOOL
		@Override protected boolean equalMoves(MovePair a, MovePair b) {return a.i == b.i && a.j == b.j;}
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
			this.i = move.i();
			this.j = move.j();
		}
		@Override @Deprecated protected void resetValue() {}
		@Override @Deprecated protected void evalValue() {}
		@Override protected void initChildren(int children_max) {
			children = new NodeAED[children_max];
			children_n = 0;
		}
		@Override protected void generateChildren(int children_max) {children = new NodeAED[children_max];}
	}

	public static class NodeAEDS extends Node_aeds<MovePair,Value,NodeAEDS> {
		short i, j;
		
		public NodeAEDS()								{super();}
		public NodeAEDS(MovePair move, NodeAEDS parent) {super(move, parent);}

		// BOOL
		@Override protected boolean equalMoves(MovePair a, MovePair b) {return a.i == b.i && a.j == b.j;}
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
			this.i = move.i();
			this.j = move.j();
		}
		@Override @Deprecated protected void resetValue() {}
		@Override @Deprecated protected void evalValue() {}
		@Override protected void initChildren(int children_max) {
			children = new NodeAEDS[children_max];
			children_n = 0;
		}
		@Override protected void generateChildren(int children_max) {children = new NodeAEDS[children_max];}
	}

}
