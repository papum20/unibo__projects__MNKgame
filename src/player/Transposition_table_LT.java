package player;
import java.util.Random;
import mnkgame.MNKCellState;


public class Transposition_table_LT {
	protected final int hash_size;
	protected final int ScoreNotFound;
	protected int M;
	protected int N;
	protected long[][][] storage;//deve essere una matrice tridimensionale
	protected transposition_hash_cell[] transposition_hash;    //l'hash table è 2^16, da inizializzare con tutti i campi val a -2 o comunque un valore per far capire che quella cella è vuota

	public Transposition_table_LT(int M, int N){
		hash_size = (int)Math.pow(2,16);  //dimensione della tabella hash 
		ScoreNotFound = -10; //indica se quando Osama controlla se è presente nella transposition_hash lo stesso Game_state, non lo trova
		transposition_hash = new transposition_hash_cell[hash_size];
		for(int i=0; i<hash_size; i++){
			transposition_hash[i].head.score = -2;
			transposition_hash[i].head = null;
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

	public int gain_score (long key){   //funzione che deve fare osama per prendere lo score, ritorna la costante ScoreNotFound se non è stato trovato
		int transposition_table_index = (int)(key & (hash_size - 1));	//contando che c'è l'and binario non serve il valore assoluto perchè toglie i numeri negativi
		int result = ScoreNotFound;
		node tmp = transposition_hash[transposition_table_index].head;
		while(tmp!=null){
			if(tmp.key==key){
				result=tmp.score;
				break;
			}
			tmp=tmp.next;
		}
		return result;	
	}

	//Osama genera la chiave, controlla se è presente nella tabella tramite gain_score, se non c'è fa una evaluation e poi salva lo score con save_data
	public void save_data(int score, long key){
		int transposition_table_index = (int)(key & (hash_size - 1));
		transposition_hash[transposition_table_index].head_insert(score, key);
	}
	
	public class node{
		public int score;
		public long key;
		public node next;
		public node(int score, long key, node next){
			this.score=score;
			this.next=next;
			this.key=key;
		}
	}
	
	public class transposition_hash_cell {
		private node head;
		public transposition_hash_cell(){		
		}
		public void head_insert(int score, long key){
			node tmp = new node(score,key,head);
			head=tmp;
		}
		public boolean is_empty(){
			if(head==null)
				 return true;
				 else return false;
		}
	}
	
}


	
