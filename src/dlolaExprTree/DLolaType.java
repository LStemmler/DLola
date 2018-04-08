package dlolaExprTree;

public enum DLolaType {
	INT, BOOL;

	public int size() {
		switch (this) {
		case INT:
			return 4;
		case BOOL:
			return 1;
		}
		return 0;
	}
}
