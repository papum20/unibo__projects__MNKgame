package structures;


public enum PHOrder {
	GREATER(1),
	LESS(-1);
	protected int value;
	private PHOrder(int value) {
		this.value = value;
	}
	public int get() {
		return value;
	}
}