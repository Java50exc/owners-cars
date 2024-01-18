package telran.cars.service;

@SuppressWarnings("serial")
public class ModelIllegalStateException extends IllegalStateException {
    public ModelIllegalStateException() {
		super("Model already exists");
	}
}
