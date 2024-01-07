package de.famiru.ctriddle.chilly;

import de.famiru.ctriddle.chilly.distance.DijkstraSolver;
import de.famiru.ctriddle.chilly.game.BoardFactory;
import de.famiru.ctriddle.chilly.glue.TspFileWriter;
import de.famiru.ctriddle.chilly.tsp.AtspToTspTransformer;
import de.famiru.ctriddle.chilly.tsp.DistanceToAtspTransformer;
import de.famiru.ctriddle.chilly.tsp.SolutionParser;
import de.famiru.ctriddle.chilly.tsp.SolutionValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        BoardFactory.BoardAndPlayer boardAndPlayer = new BoardFactory().loadLevel("level.txt");

        DijkstraSolver dijkstraSolver =
                new DijkstraSolver(boardAndPlayer.board(), boardAndPlayer.playerX(), boardAndPlayer.playerY());
        Matrix matrix = dijkstraSolver.createDistanceMatrix();

        Matrix atspMatrix = new DistanceToAtspTransformer()
                .transformDistanceMatrixToAtsp(matrix, dijkstraSolver.getClusters());

        Matrix tspMatrix = new AtspToTspTransformer().transformAtspToTsp(atspMatrix);
        new TspFileWriter().writeTspFile("chilly.tsp", tspMatrix);

        LOGGER.info("Please pass chilly.tsp to a solver able to handle files in TSPLIB format.");
        LOGGER.info("Place the solution as file chilly.sol into the working dir and press enter.");
        System.in.read();
        if (!Files.isRegularFile(Path.of("chilly.sol"))) {
            LOGGER.error("File not found. Did you place chilly.sol in the working directory?");
            return;
        }

        List<Integer> path = new SolutionParser().parseSolution("chilly.sol");

        SolutionValidator validator = new SolutionValidator();
        if (!validator.isValidSolution(atspMatrix, path)) {
            // possibly the solution is simply the wrong way around
            Collections.reverse(path);
        }
        if (!validator.isValidSolution(atspMatrix, path)) {
            LOGGER.error("The solution is not valid.");
            return;
        }

        StringBuilder sb = createInstructions(path, atspMatrix);
        LOGGER.info("Solution (length {}): {}", sb.length(), sb.toString());
    }

    private static StringBuilder createInstructions(List<Integer> path, Matrix matrix) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < path.size(); j++) {
            int i = path.get(j);
            if (j > 0) {
                String fragment = matrix.getPath(path.get(j - 1) % matrix.getDimension(), i % matrix.getDimension());
                if (!fragment.startsWith("cluster") && !fragment.equals(Constants.EXIT_TO_START_PATH)) {
                    sb.append(fragment);
                }
                LOGGER.debug("{}", fragment);
            }
            if (i >= matrix.getDimension()) {
                LOGGER.debug("{}# {}", i % matrix.getDimension(), matrix.getDescription(i % matrix.getDimension()));
            } else {
                LOGGER.debug("{}: {}", i, matrix.getDescription(i));
            }
        }
        return sb;
    }
}
