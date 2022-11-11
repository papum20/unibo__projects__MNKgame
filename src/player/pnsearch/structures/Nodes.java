package player.pnsearch.structures;

import java.util.Collection;
import java.util.LinkedList;
import mnkgame.MNKCell;



public class Nodes {

	public static final short PROOF_N_ZERO = 0;
	public static final short PROOF_N_INFINITE = 32767;

	

	//#region CLASSES

	
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
				public short getChildren_minProof();
				public short getChildren_minDisproof();
				public S findChild(MNKCell move);
				public S findChildProof(short proof);
				public S findChildDisproof(short disproof);
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
				public void setProofDisproof(short proof, short disproof);
				public void setMove(M move);
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
				//public short getChildren_minProof();
				//public short getChildren_minDisproof();
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
				public void setProofDisproof(short proof, short disproof) {
					this.proof = proof;
					this.disproof = disproof;
				}
				public void setMove(M move) {
					this.move = move;
				}
				public void setParent(S parent) {
					this.parent = parent;
				}
			}

		//#endregion INTERFACES


		//#region COLLECTION
			
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
						if(compareMNKMoves(move, child.move.position)) {
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
				//public void setProofDisproof(short proof, short disproof);
				//public void setMove(M move);
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
				protected void init(Move move, Value value, short proof, short disproof, S parent) {
					super.init(move, value, proof, disproof, parent);
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
				//public void setMove(M move);
				//public void setParent(S parent);
				//public void setProofDisproof(short proof, short disproof);
				// SET
				public abstract void addChild(MNKCell move);
				public void expand() {
					expanded = true;
				}
				//public void setProofDisproof(short proof, short disproof);
				//public void setMove(M move);
				//public void setParent(S parent);
			}
			// INSTANCE FOR PnSearch
			public static class Node extends Node_e<Node> {
				public Node() {super();}
				public Node(Move move, Node parent) {super(move, parent);}
				public Node(Move move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}

				public void addChild(MNKCell move) {
					children.addLast(new Node(new Move(move), this));
				}
			}
			// INSTANCE FOR PnSearchDelete
			public static class NodeD extends Node_e<NodeD> {
				protected boolean expanded;
				
				public NodeD() {super();}
				public NodeD(Move move, NodeD parent) {super(move, parent);}
				public NodeD(Move move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}
				protected void init(Move move, Value value, short proof, short disproof, NodeD parent) {
					super.init(move, value, proof, disproof, parent);
					this.children = null;
				}
				
				// FUNCTIONS
				//functions about children should be redefined to check whether children==null;
				//however some are only called if node is expanded
				public NodeD findChild(MNKCell move) {
					if(children == null) return null;
					else return super.findChild(move);
				}
				// SET
				public void addChild(MNKCell move) {
					children.addLast(new NodeD(new Move(move), this));
				}
				public void expand() {
					expanded = true;
					children = new LinkedList<NodeD>();
				}
			}

		//#endregion COLLECTION


		//#region ARRAY
			
			/**
			 * A array
			 * @param <M> move type
			 * @param <S> self (same type)
			 */
			public abstract static class Node_a<M extends Move, S extends Node_a<M,S>> extends Node_t<M,S,S[]> {

				public Node_a() {super();}
				public Node_a(M move, S parent) {super(move, parent);}
				public Node_a(M move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}

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
				public S findChildProof(short proof) {
					S res = null;
					int i = 0;
					while(i < children.length && (res = children[i]).proof != proof) i++;
					if(i == children.length) return null;
					else return res;
				}
				public S findChildDisproof(short disproof) {
					S res = null;
					int i = 0;
					while(i < children.length && (res = children[i]).disproof != disproof) i++;
					if(i == children.length) return null;
					else return res;
				}
				// BOOL
				//public boolean isExpanded();
				// GET
				//public M getMove();
				//public MNKCell getPosition();
				public int getChildrenLength() {
					return children.length;
				}
				public S getFirstChild() {
					return children[0];
				}
				//public S getParent():
				// SET
				//public void addChild(MNKCell move);
				//public void expand();
				//public void setProofDisproof(short proof, short disproof);
				//public void setMove(M move);
				//public void setParent(S parent);
			}
			/**
			 * expand, linkedlist
			 * @param <S> self
			 */
			public abstract static class Node_ae<S extends Node_e<S>> extends Node_c<Move, S, LinkedList<S>> {
				protected boolean expanded;
				
				public Node_ae() {super();}
				public Node_ae(Move move, S parent) {super(move, parent);}
				public Node_ae(Move move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}
				protected void init(Move move, Value value, short proof, short disproof, S parent) {
					super.init(move, value, proof, disproof, parent);
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
				//public S getParent():
				//public M getMove();
				public MNKCell getPosition() {
					return move.position;
				}
				//public int getChildrenLength();
				// SET
				//public void setMove(M move);
				//public void setParent(S parent);
				//public void setProofDisproof(short proof, short disproof);
				// SET
				public abstract void addChild(MNKCell move);
				public void expand() {
					expanded = true;
				}
				//public void setProofDisproof(short proof, short disproof);
				//public void setMove(M move);
				//public void setParent(S parent);
			}
			// INSTANCE
			//public static class NodeAD extends Node_ae

		//#endregion ARRAY
	
	//#endregion CLASSES



	//#region FUNCTIONS
		
		public Nodes() {

		}

		protected static boolean compareMNKMoves(MNKCell a, MNKCell b) {
			return a.i == b.i && a.j == b.j;
		}

	//#endregion FUNCTIONS

}
