package semanticAnalysis;

public class AlreadyDefinedException extends Exception {

	
	
	public AlreadyDefinedException(String name, String streamType) {
		super(name + " has already been defined as " + streamType + "!");
	}

}
