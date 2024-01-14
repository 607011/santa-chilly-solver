package de.famiru.ctriddle.chilly.glue;

public class GlueModuleFactory {
    public TspSolverOutput createTspSolverOutput(GlueModuleType type) {
        return switch (type) {
            case CONCORDE -> new ConcordeTspSolverOutput();
            case OR_TOOLS -> new RawMatrixTspSolverOutput();
        };
    }

    public TspSolverInput createTspSolverInput(GlueModuleType type) {
        return switch (type) {
            case CONCORDE -> new ConcordeTspSolverInput();
            case OR_TOOLS -> new StdInTspSolverInput();
        };
    }
}
