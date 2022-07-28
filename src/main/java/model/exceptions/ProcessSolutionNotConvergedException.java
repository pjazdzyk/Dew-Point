package model.exceptions;

public class ProcessSolutionNotConvergedException extends RuntimeException {

    public ProcessSolutionNotConvergedException() {
        super();
    }

    public ProcessSolutionNotConvergedException(String msg) {

        super(msg);
    }

}
