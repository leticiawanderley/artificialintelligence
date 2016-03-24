import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * This gomoku player is an implementation of the Minimax algorithm with
 * alpha-beta pruning
 * 
 * @author Leticia Wanderley
 */
public class ThreatModification extends GomokuPlayer {

	private final int DEPTH = 5;
	private Map<Double, Move> successors;

	@Override
	public Move chooseMove(Color[][] board, Color me) {
		this.successors = new HashMap<Double, Move>();
		Double max = this.maxValue(board, me, DEPTH, -Double.MAX_VALUE, Double.MAX_VALUE);
		return this.successors.get(max);
	}

	/**
	 * Evaluates the state of the board (game) and returns the evaluation value
	 * 
	 * @param board
	 *            Representation of the state of the game
	 * @param me
	 *            Color which the player is playing
	 * @return evaluation value
	 */
	private Double evaluationFuction(Color[][] board, Color me) {
		Double value = 0.0;
		for (int i = 0; i < GomokuBoard.ROWS; i++) {
			for (int j = 0; j < GomokuBoard.COLS; j++) {
				if (board[i][j] != null) {
					// horizontal + vertical + diagonal descending right +
					// diagonal descending left
					value += threatSearch(i, j, 0, 1, board, me) + threatSearch(i, j, 1, 0, board, me)
							+ threatSearch(i, j, 1, 1, board, me) + threatSearch(i, j, 1, -1, board, me);
				}
			}
		}
		return value;
	}

	/**
	 * Part of the Minimax algorithm Calculates maximum value to be achieved from
	 * the state of the board
	 * 
	 * @param board
	 *            Representation of the state of the game
	 * @param me
	 *            Color which the player is playing
	 * @param depth
	 *            Depth of the search tree
	 * @param alpha
	 *            Lowest value range, used for pruning
	 * @param beta
	 *            Highest value range, used for pruning
	 * @return maximum value achieved from the initial state
	 */
	public Double maxValue(Color[][] board, Color me, int depth, Double alpha, Double beta) {
		Double val;
		Double v = -Double.MAX_VALUE;
		int bestRow, bestCol;
		depth--;
		if (depth == 0) {
			return evaluationFuction(board, me);
		}
		for (int row = 0; row < GomokuBoard.ROWS; row++) {
			for (int col = 0; col < GomokuBoard.COLS; col++) {
				if (board[row][col] == null && this.hasAdjacentStones(board, row, col)) {
					board[row][col] = me;
					val = this.minValue(board, me, depth, alpha, beta);
					this.successors.put(val, new Move(row, col));
					if (val > v) {
						v = val;
						bestRow = row;
						bestCol = col;
					}
					if (val >= beta) {
						board[row][col] = null;
						return val;
					}
					if (alpha < val) {
						alpha = val;
					}
					board[row][col] = null;
				}
			}
		}
		if (v == -Double.MAX_VALUE) {
			bestRow = this.randInt(2, 5);
			bestCol = this.randInt(2, 5);
			this.successors.put(v, new Move(bestRow, bestCol));
		}
		return v;
	}

	/**
	 * Part of the Minimax algorithm Calculates minimum value to be achieved from
	 * the state of the board
	 * 
	 * @param board
	 *            Representation of the state of the game
	 * @param me
	 *            Color which the player is playing
	 * @param depth
	 *            Depth of the search tree
	 * @param alpha
	 *            Lowest value range, used for pruning
	 * @param beta
	 *            Highest value range, used for pruning
	 * @return minimum value achieved from the initial state
	 */
	private Double minValue(Color[][] board, Color me, int depth, Double alpha, Double beta) {
		Double val;
		Double v = Double.MAX_VALUE;
		depth--;
		if (depth == 0) {
			return evaluationFuction(board, me);
		}
		for (int row = 0; row < GomokuBoard.ROWS; row++) {
			for (int col = 0; col < GomokuBoard.COLS; col++) {
				if (board[row][col] == null && this.hasAdjacentStones(board, row, col)) {
					board[row][col] = oppositeColor(me);
					val = this.maxValue(board, me, depth, alpha, beta);
					if (val < v) {
						v = val;
					}
					if (val <= alpha) {
						board[row][col] = null;
						return val;
					}
					if (beta > val) {
						beta = val;
					}
					board[row][col] = null;
				}
			}
		}
		return v;
	}

	/**
	 * Check if the position has any stones placed around it
	 * 
	 * @param board
	 *            Representation of the game board
	 * @param row
	 *            Position row
	 * @param col
	 *            Position column
	 * @return true if the position has any  adjacent stones, false otherwise
	 */
	private boolean hasAdjacentStones(Color[][] board, int row, int col) {
		boolean result = false;
		int upperRow = row - 1;
		int belowRow = row + 1;
		int leftCol = col - 1;
		int rightCol = col + 1;
		if (upperRow >= 0) {
			if (leftCol >= 0) {
				result = result || (board[upperRow][leftCol] != null);
			}
			if (rightCol < GomokuBoard.COLS) {
				result = result || (board[upperRow][rightCol] != null);
			}
			result = result || (board[upperRow][col] != null);
		}
		if (belowRow < GomokuBoard.ROWS) {
			if (leftCol >= 0) {
				result = result || (board[belowRow][leftCol] != null);
			}
			if (rightCol < GomokuBoard.COLS) {
				result = result || (board[belowRow][rightCol] != null);
			}
			result = result || (board[belowRow][col] != null);
		}
		if (leftCol >= 0) {
			result = result || (board[row][leftCol] != null);
		}
		if (rightCol < GomokuBoard.COLS) {
			result = result || (board[row][rightCol] != null);
		}
		return result;
	}

	/**
	 * Searches for possible threats in sets of 6 adjacent positions
	 * 
	 * @param currentRow
	 *            Current row being searched
	 * @param currentCol
	 *            Current column being searched
	 * @param rowStep
	 *            Row search step
	 * @param colStep
	 *            Column search step
	 * @param board
	 *            Representation of the game board
	 * @param me
	 *            Color which the player is playing
	 * @return value of threat found within 6 positions set
	 */
	private Double threatSearch(int currentRow, int currentCol, int rowStep, int colStep, Color[][] board, Color me) {
		String stringRow = "";
		int myCount = 0;
		int oppositeCount = 0;
		int nullCount = 0;
		Double value = 0.0;
		for (int k = 0; k < 6; k++) {
			int row = currentRow + k * rowStep;
			int col = currentCol + k * colStep;
			if (!(row < 0 || col < 0 || row >= board.length || col >= board[row].length)) {
				if (me.equals(board[row][col])) {
					myCount++;
					stringRow += "M";
				} else if (oppositeColor(me).equals(board[row][col])) {
					oppositeCount++;
					stringRow += "O";
				} else if (board[row][col] == null) {
					nullCount++;
					stringRow += "_";
				}
			} else {
				break;
			}
		}
		if (stringRow.length() >= 5) {
			if (myCount == 5 || stringRow.contains("MMMMM")) {
				value += Threat.FIVE.getWeight();
			} else if (oppositeCount == 5 || stringRow.contains("OOOOO")) {
				value += -(2 * Threat.FIVE.getWeight());
			} else if (stringRow.equals("_MMMM_")) {
				value += Threat.STRAIGHT_FOUR.getWeight();
			} else if (stringRow.contains("_MMMM") || stringRow.contains("MMMM_") || stringRow.contains("M_MMM")
				|| stringRow.contains("MM_MM") || stringRow.contains("MMM_M")) {
				value += Threat.FOUR.getWeight();
			} else if (stringRow.equals("__MMM_") || stringRow.equals("___MMM")
				|| stringRow.equals("MMM___") || stringRow.equals("_MMM__")) {
				value += Threat.OPEN_THREE.getWeight();
			} else if (stringRow.contains("MMM__") || stringRow.contains("_MMM_")
				|| stringRow.contains("M_MM_") || stringRow.contains("_M_MM")
				|| stringRow.contains("M_M_M") || stringRow.contains("MM_M_")
				|| stringRow.contains("_MM_M") || stringRow.contains("__MMM")) {
				value += Threat.THREE.getWeight();	
			} else if (stringRow.contains("___MM") || stringRow.contains("___MM")
				|| stringRow.contains("_MM__") || stringRow.contains("__MM_")
				|| stringRow.contains("M_M__") || stringRow.contains("M__M_")
				|| stringRow.contains("M___M") || stringRow.contains("_M__M")
				|| stringRow.contains("__M_M")) {
				value += Threat.TWO.getWeight();
			} else if (stringRow.equals("_OOOO_")) {
				value += -(10 * Threat.STRAIGHT_FOUR.getWeight());
			} else if (stringRow.contains("_OOOO") || stringRow.contains("OOOO_") || stringRow.contains("O_OOO")
				|| stringRow.contains("OO_OO") || stringRow.contains("OOO_O")) {
				value += -(10 * Threat.FOUR.getWeight());
			} else if (stringRow.equals("__OOO_") || stringRow.equals("___OOO")
				|| stringRow.equals("OOO___") || stringRow.equals("_OOO__")) {
				value += -(3 * Threat.OPEN_THREE.getWeight());
			} else if (stringRow.contains("OOO__") || stringRow.contains("_OOO_")
				|| stringRow.contains("O_OO_") || stringRow.contains("_O_OO")
				|| stringRow.contains("O_O_O") || stringRow.contains("OO_O_")
				|| stringRow.contains("_OO_O") || stringRow.contains("__OOO")) {
				value += -(3 * Threat.THREE.getWeight());	
			} else if (stringRow.contains("___OO") || stringRow.contains("___OO")
				|| stringRow.contains("_OO__") || stringRow.contains("__OO_")
				|| stringRow.contains("O_O__") || stringRow.contains("O__O_")
				|| stringRow.contains("O___O") || stringRow.contains("_O__O")
				|| stringRow.contains("__O_O")) {
				value += -(3 * Threat.TWO.getWeight());
			}
		}
		return value;
	}

	/**
	 * Returns the opposite color from the one sent as parameter
	 * 
	 * @param opposite
	 *            Player's color
	 * @return black if the player's color is white, white if the player's color
	 *         is black
	 */
	private Color oppositeColor(Color opposite) {
		if (opposite.equals(Color.BLACK)) {
			return Color.WHITE;
		}
		return Color.BLACK;
	}

	/**
	 * Randomizes an integer inside the range in the parameters
	 * 
	 * @param min
	 *            minimum range value
	 * @param max
	 *            maximum range value
	 * @return random number between minimum and max
	 */
	public int randInt(int min, int max) {
		Random random = new Random();
		int randomNum = random.nextInt((max - min) + 1) + min;
		return randomNum;
	}

	/**
	 * Enum representing the weights values of threats found on the board
	 * 
	 * @author Leticia Wanderley
	 */
	enum Threat {
		TWO(1.0), THREE(10.0), FOUR(1000.0), OPEN_THREE(100.0), STRAIGHT_FOUR(10000.0), FIVE(1000000.0);

		private Double weight;

		Threat(Double weight) {
			this.weight = weight;
		}

		public Double getWeight() {
			return this.weight;
		}
	}
}
