package player;

import java.util.Random;
import mnkgame.MNKCellState;




public class Transposition_table {
	protected final int hash_size;
	protected final int ScoreNotFound;
	protected final int max_ite;
	protected boolean table_is_full;
	protected int M;
	protected int N;
	protected long[][][] storage;//deve essere una matrice tridimensionale
	protected transposition_hash_cell[] transposition_hash;    //l'hash table è 2^16, da inizializzare con tutti i campi val a -2 o comunque un valore per far capire che quella cella è vuota

	public Transposition_table(int M, int N){
		table_is_full=false;
		hash_size = (int)Math.pow(2,16);  //dimensione della tabella hash 
		max_ite = 20;  //n_max_iterazioni prima di ritornare ScoreNotFound nella ricerca della transposition_hash per trovare un Game_State uguale 
		ScoreNotFound = -10; //indica se quando Osama controlla se è presente nella transposition_hash lo stesso Game_state, non lo trova
		transposition_hash = new transposition_hash_cell[hash_size];
		for(int i=0; i<hash_size; i++){
			transposition_hash[i].score = -2;
		}
		this.M=M;
		this.N=N;
	}
	public void initTableRandom()
	{
		this.storage = new long[2][M][N];
		for(int i=0; i<2; i++){
			for(int j=0; j<M; j++){
				for(int k=0; k<N; k++){
						storage[i][j][k]= new Random().nextLong();//il numero random in questo caso può essere pure negativo
	    }   }	}
    }
	public long generate_key(long father_key_hash, int x, int y, MNKCellState p){ //y colonne e x le righe, genera la chiave relativa a una cella, la radice ha father_key_hash=(long)0
		if(p == MNKCellState.P1){
			father_key_hash ^= storage[0][y][x];
			}
		if(p == MNKCellState.P2){
			father_key_hash ^= storage[1][y][x];
			}	
		return 	father_key_hash; //con un hash a 64 bit, le collisioni possono avvenire 1 ogni sqrt(2^64) cioè dopo circa 2^32 o 4 miliardi di posizioni calcolate
    }

	public boolean table_is_full(){
		return(table_is_full);
	}
	/*public void open_addressing(int score, int key){
		if(table_is_full)
			return;
		int new_key = ispezione_quadrata(key);
		if(table_is_full)
			 return;
		else save_data(score, new_key);
		
	}*/

	private int ispezione_quadrata (long key){ //trova la prima cella libera 
		int transposition_table_index = (int)(key & (hash_size - 1));
		int i=0;
		double c1 = 0.5;  //c1 e c2 wikipedia dice di metterli uguali a 1/2 se hash size è 2^n
		double c2 = 0.5;
		while(transposition_hash[transposition_table_index].score!=-2){
			if(i==hash_size){
				table_is_full=true;
				break;
			}
			transposition_table_index=(int)(transposition_table_index + i*c1 + (i*i)*c2)%(hash_size - 1); //ispezione quadratica
			i++;      
		}
		return transposition_table_index;
	}

	public int gain_score (long key){   //funzione che deve fare osama per prendere lo score, ritorna la costante ScoreNotFound se non è stato trovato
		int transposition_table_index = (int)(key & (hash_size - 1));	//contando che c'è l'and binario non serve il valore assoluto perchè toglie i numeri negativi
		if(table_is_full)
			return ScoreNotFound;
		boolean Not_found_after_max_ite=false;
		int i=0;
		double c1= 0.5;  
		double c2= 0.5;
		while(transposition_hash[transposition_table_index].key!=key){ //da togliere il true
			transposition_table_index=(int)(transposition_table_index + i*c1 + (i*i)*c2)%(hash_size - 1); //ispezione quadratica
			i++;
			if(i>=max_ite){  //si cerca nella transposition_table fino a max_it    
				Not_found_after_max_ite=true;
				break;
			}
		}
		if(Not_found_after_max_ite)
			return ScoreNotFound;
		else return transposition_hash[transposition_table_index].score;
}

	//Osama genera la chiave, controlla se è presente nella tabella tramite gain_score, se non c'è fa una evaluation e poi salva lo score con save_data
	public void save_data(int score, long key){
		if(table_is_full)
			return;
		int transposition_table_index = ispezione_quadrata(key);
		if(table_is_full)
			return;
		transposition_hash[transposition_table_index].score=score;
		transposition_hash[transposition_table_index].key=key;
	}
	public boolean are_transpositions(MNKCellState[][] A, MNKCellState[][] B, int M, int N){
		if(N==M){
			boolean sim0rot90=true;
			boolean sim0rot180=true;
			boolean sim0rot270=true;
			boolean sim1rot0=true;
			boolean sim1rot90=true;
			boolean sim1rot180=true;
			boolean sim1rot270=true;
			for(int i=0; i<N;i++){
				for(int j=0; j<N; j++){
					if(sim0rot90){
						if(A[i][j]!=B[N-1-j][i])
							sim0rot90=false;
					}
					if(sim0rot180){
						if(A[i][j]!=B[N-1-i][N-1-j])
							sim0rot180=false;
					}
					if(sim0rot270){
						if(A[i][j]!=B[j][N-1-i])
							sim0rot270=false;
					}
					if(sim1rot0){
						int tmp = N-j-1;
						if(i>tmp)
							break;
						else if(A[i][j]!=B[i][tmp])  
							sim1rot0=false;
					}
					if(sim1rot90){
						if(A[i][j]!=B[j][i])
							sim1rot90=false;
					}
					if(sim1rot180){
						if(A[i][j]!=B[N-1-i][j])
							sim1rot180=false;
					}
					if(sim1rot270){
						if(A[i][j]!=B[j][N-1-i])
							sim1rot270=false;
					}

				}
			}
			return(sim0rot90 || sim0rot180 || sim0rot270 || sim1rot0 || sim1rot90 || sim1rot180 || sim1rot270);
		}
		else{
			boolean sim0rot180=true;
			boolean sim1rot0=true;
			boolean sim1rot180=true;
			for(int i =0; i <M; i++){
				for(int j=0; j<N; j++){
					if(sim0rot180){
						if(A[i][j]!=B[M-1-i][N-1-j])
							sim0rot180=false;
					}
					if(sim1rot0){
						int tmp = N-j-1;
						if(i>tmp)
							break;
						else if(A[i][j]!=B[i][tmp])  
							sim1rot0=false;
					}
					if(sim1rot180){
						if(A[i][j]!=B[M-1-i][j])
							sim0rot180=false;
					}
				}
			}
			return(sim0rot180 || sim1rot0 || sim1rot180);
		}
	}
	
	public class transposition_hash_cell {
		public int score;
		public long key;
		public transposition_hash_cell(){		
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