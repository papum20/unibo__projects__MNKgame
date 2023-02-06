package player.pnsearch;



public class PnNode {

	
	public static enum Value {
		TRUE,
		FALSE,
		UNKNOWN
	}

	public static final short PROOF_N_ZERO = 0;
	public static final short PROOF_N_INFINITE = 32767;
	public static final short SHORT_ERROR = -1;

	public short proof;
	public short disproof;
	protected PnNode parent;
	public PnNode[] children;
	protected short i, j;
	protected int children_n;
	public PnNode most_proving;
	


	//#region CONTSTRUCTORS

		public PnNode()								{
			init(null, PROOF_N_ZERO, PROOF_N_ZERO, null);
		}
		public PnNode(MovePair move, PnNode parent) {
			init(move, PROOF_N_ZERO, PROOF_N_ZERO, parent);
		}
		/*
		public PnNode(int children_max) 					{
			init(null, PROOF_N_ZERO, PROOF_N_ZERO, null, children_max);
		}
		public PnNode(MovePair move, PnNode parent, int children_max)	{
			init(move, PROOF_N_ZERO, PROOF_N_ZERO, parent, children_max);
		}
		*/	
		
		protected void init(MovePair move, short proof, short disproof, PnNode parent) {
			setMove(move);
			this.proof = proof;
			this.disproof = disproof;
			this.parent = parent;
			initChildren();
			most_proving = null;
		}
		/*
		protected void init(MovePair move, short proof, short disproof, PnNode parent, int children_max) {
			setMove(move);
			this.proof = proof;
			this.disproof = disproof;
			this.parent = parent;
			initChildren(children_max);
		}
		protected void init(MovePair move, short proof, short disproof, PnNode parent, int children_max, Value value) {
			setMove(move);
			setValue(value);
			this.proof = proof;
			this.disproof = disproof;
			this.parent = parent;
			initChildren(children_max);
		}
		*/
	//#endregion CONSTRUCTORS

	//#region FUNCTIONS
		//functions about children should be redefined to check whether children==null;
		//however some are only called if node is expanded
		public short getChildren_sumProof() {
			short sum = 0;
			for(int i = 0; i < children_n; i++) {
				PnNode child = children[i];
				if(child.proof == PROOF_N_INFINITE) return PROOF_N_INFINITE;
				else sum += child.proof;
			}
			return sum;
		}
		public short getChildren_sumDisproof() {
			short sum = 0;
			for(int i = 0; i < children_n; i++) {
				PnNode child = children[i];
				if(child.disproof == PROOF_N_INFINITE) return PROOF_N_INFINITE;
				else sum += child.disproof;
			}
			return sum;
		}
		public PnNode getChildren_minProof() {
			PnNode min = children[0];
			for(int i = 1; i < children_n; i++)
				if (children[i].proof < min.proof) min = children[i];
			return min;
		}
		public PnNode getChildren_minDisproof() {
			PnNode min = children[0];
			for(int i = 1; i < children_n; i++)
				if(children[i].disproof < min.disproof) min = children[i];
			return min;
		}
		public PnNode findChild(MovePair move) {
			PnNode res = null;
			int k = 0;
			while(k < children_n && !move.equals((res=children[k]).getMove()) ) k++;
			if(k == children_n) return null;
			else return res;
		}
		public PnNode findChildProof(short proof) {
			PnNode res = null;
			int i = 0;
			while(i < children_n && (res = children[i]).proof != proof) i++;
			if(i == children_n) return null;
			else return res;
		}
		public PnNode findChildDisproof(short disproof) {
			PnNode res = null;
			int i = 0;
			while(i < children_n && (res = children[i]).disproof != disproof) i++;
			if(i == children_n) return null;
			else return res;
		}
		// deletes all children but the one with the same values of proof-disproof
		// assumes that this node is proved
	//#endregion FUNCTIONS

	//#region BOOL
		//return true if node value is "unknown" and has children
		public boolean isExpanded() {
			return children != null && children_n > 0;
		}
	//#endregion BOOL

	//#region GET
		public PnNode getParent() {return parent;}
		public int getChildrenLength() {return children_n;}
		public PnNode getFirstChild() {return children[0];}
		public MovePair getMove() {return new MovePair(i, j);}
		public MovePair getPosition() {return new MovePair(i, j);}
		public short i() {return i;}
		public short j() {return j;}
		public Value getValue() {
			if(proof == PROOF_N_ZERO) return Value.TRUE;
			else if(disproof == PROOF_N_ZERO) return Value.FALSE;
			else return Value.UNKNOWN;
		}
	//#endregion GET

	//#region SET
		/*
		public void prove() {
			evalValue();
			children = null;
			children_n = 0;
		}
		*/
		public void prove(Value value) {
			setValue(value);
			children = null;
			children_n = 0;
		}
		public void setProofDisproof(short proof, short disproof) {
			this.proof = proof;
			this.disproof = disproof;
		}
		//public void expand() {}
		public void expand(int children_max) {
			children = new PnNode[children_max];
		}
		public void reset(MovePair move) {
			setMove(move);
			//resetValue();
			proof = 1;
			disproof = 1;
			parent = null;
			initChildren();
			most_proving = null;
		}
		/*
		public void reset(MovePair move, int children_max) {
			setMove(move);
			resetValue();
			proof = 1;
			disproof = 1;
			parent = null;
			initChildren(children_max);
		}
		*/
		public void setParent(PnNode parent) {
			this.parent = parent;
		}
		/*
		public void reduce() {
			evalValue();
			children[0] = most_proving;
			children_n = 1;
		}
		*/
		public void setValue(Value value) {
			if(value == Value.TRUE) setProofDisproof(PROOF_N_ZERO, PROOF_N_INFINITE);
			else if(value == Value.FALSE) setProofDisproof(PROOF_N_INFINITE, PROOF_N_ZERO);
			else setProofDisproof((short)1, (short)1);
		}
		public void addChild(MovePair move) {
			children[children_n++] = new PnNode(move, this);
		}
	//#endregion SET

	//#region INIT
		//protected void resetValue() {}
		//protected void evalValue() {}	//value = (proof == 0) ? Value.TRUE : Value.FALSE;
		protected void initChildren() {
			children = null;
			children_n = 0;
		}
		protected void setMove(MovePair move) {
			if(move != null) {
				this.i = move.i();
				this.j = move.j();
			}
		}
	//#endregion INIT

		
}
