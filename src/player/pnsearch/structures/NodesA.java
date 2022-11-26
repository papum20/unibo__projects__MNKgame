package player.pnsearch.structures;



public class NodesA extends INodesA {
	
	//INSTANCE : Node with arrays
	public static class NodeA extends Node_a<MovePair,Value,NodeA> {
		protected short i, j;
		
		public NodeA(int children_max)								{super(children_max);}
		public NodeA(MovePair move, NodeA parent, int children_max)	{super(move, parent, children_max);}
		//public NodeAE(Move move, Value value, short proof, short disproof, int children_max) {super(move, value, proof, disproof, children_max);}
		
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
		public void addChild(MovePair move) {children[children_n++] = new NodeA(move, this, children.length);}
		// INIT
		@Override protected void setMove(MovePair move) {
			if(move != null) {
				this.i = move.i();
				this.j = move.j();
			}
		}
		@Override protected void generateChildren(int children_max) {children = new NodeA[children_max]; }
	}
	
	public static class NodeAD extends Node_ad<MovePair,Value,NodeAD> {
		protected short i, j;

		public NodeAD()								{super();}
		//public NodeAD(int children_max) {super(children_max);}
		public NodeAD(MovePair move, NodeAD parent)	{super(move, parent);}
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
		public void addChild(MovePair move) {children[children_n++] = new NodeAD(move, this);}
		// INIT
		@Override protected void setMove(MovePair move) {
			if(move != null) {
				this.i = move.i();
				this.j = move.j();
			}
		}
		@Override protected void generateChildren(int children_max) {children = new NodeAD[children_max];}
	}

	public static class NodeADS extends Node_ads<MovePair,Value,NodeADS> {
		protected short i, j;
		
		public NodeADS()								{super();}
		public NodeADS(MovePair move, NodeADS parent) {super(move, parent);}

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
		@Override public void addChild(MovePair move) {children[children_n++] = new NodeADS(move, this);}
		// INIT
		@Override protected void setMove(MovePair move) {
			if(move != null) {
				this.i = move.i();
				this.j = move.j();
			}
		}
		@Override protected void generateChildren(int children_max) {children = new NodeADS[children_max];}
	}

}
