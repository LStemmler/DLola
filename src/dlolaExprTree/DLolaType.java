package dlolaExprTree;

import main.Global;

public enum DLolaType {
	INT, BOOL;

	public int size() {
		return Global.sizeofType(this);
	}
}
