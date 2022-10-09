package player;
import java.math.BigInteger;
import java.util.Random;
import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import ArrayBoard;




public class transposition_table {
	private long key_hash;
	private int M;
	private int N;
	private long[] storage;
	private int transposition_table_index;
	//public long transposition_hash[(2^16)-1];
	public transposition_hash_cell[] transposition_hash;

	transposition_table(int M, int N){
		this.transposition_hash = new transposition_hash_cell[(2^16)-1];
		key_hash=0;
		this.M=M;
		this.N=N;
	}
	public void initTableRandom()
	{
		this.storage = new long[M*N*2];
		for(int i=0; i<M*N*2; i++){
					do{
						storage[i]= new Random().nextLong();//il numero deve essere positivo
					}while(storage[i]<0);
	    }
    }
	public void generate_key(final MNKCellState[][] B){
			for(int j=0; j<M*2; j++){
				for(int k=0; k<N*2; k++){
					if(B[j][k]==MNKCellState.FREE){
						key_hash ^= storage[j+k];
					}
		    }   }		
	}
	public void save_data(int score, MNKCell BestMove, boolean flag, int depth){
		//generate_key();
		transposition_hash[transposition_table_index].score=score;
		transposition_hash[transposition_table_index].BestMove=BestMove;
		transposition_hash[transposition_table_index].flag=flag;
		transposition_hash[transposition_table_index].depth=depth;

	}
	
	
	public class transposition_hash_cell {
		public int score;
		public MNKCell BestMove;
		public long zobrist_key;
		public boolean flag;
		public int depth;
		transposition_hash_cell(){		
		}
	}
}

/** 
 * This MediocreChess article explains transposition tables in details. The Zobrist algorithm is very simple to create transposition tables.
The zobrist system in two words :
Generate a random number (let's say 32 bits) for each couple of [possible piece, possible cell] (for tic-tac-toe it's 2*9) and store them in an array.
Start at hash=0, and XOR the hash with the stored number for each couple of [played piece, position of played piece]
You obtain your Zobrist key !
It's a very good system which allows removal of a piece ! you only have to XOR the same number again. It's really usefull for negamax/alpha-beta algorithms because we have to change/restore state a lot of times. 
It is easy to maintain a Zobrist key up-to-date.

The system of transposition table is :

For a certain game position, you generate a hash, which is the signature of the game position, with the Zobrist algorithm, and you obtain an integer (32 bits or 64 bits for example).
This "zobrist key" could be used directly to store best move and score for the given position, in a transposition table.
But you'll probably don't want to store 2^32 or 2^64 entries, so you take a "hash" of the Zobrist key to limit entries of the transposition table, let's say 16 bits for 2^16 game positions (in reality it's probably >=2^20). 
To obtain this hash, a simple method is to "modulo" the zobrist key, or do a "binary and" :

transposition table index = zobrist_key & 0xFFFF

You obtain an integer between 0 and 2^16-1, this is your index in the transposition table! Of course, we can encounter collisions, so we could store the full zobrist key in the transposition table.

Let's summarize :

For a given position, compute the zobrist key, and then a hash of the zobrist key, which will be your index in your transposition table. Let's store important data in this table entry : score, best_move, zobrist_key, flag, depth.
When you need to lookup in the transposition table, compute the zobrist key for the given game position, then the hash of it, and get the corresponding entry. Then check if entry's zobrist key is equal to yours, to avoid collision
 problems of "false positive".
So for a Connect 6, you have 2 stone colors, and let's say 59x59 positions, so you have to create an array of 59x59x2 = 6962 random numbers. To encode a game position in a Zobrist key, take each stone, and for its colour and its position, 
take the number you generated and XOR them together. Reduce your Zobrist Key to an index (hash, binary "and", ...), and store your data at this index in your transposition table.
 * 
 * 
*/