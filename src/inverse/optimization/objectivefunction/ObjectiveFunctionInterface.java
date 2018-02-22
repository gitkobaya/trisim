package inverse.optimization.objectivefunction;

public interface ObjectiveFunctionInterface {
	public double lfObjectiveFunction( double[] plfArg );
	void vSetFunctionMode(int iMode);
}
