
// ECS629/759 Assignment 2 - ID3 Skeleton Code
// Author: Simon Dixon

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class ID3 {

	/**
	 * Each node of the tree contains either the attribute number (for non-leaf
	 * nodes) or class number (for leaf nodes) in <b>value</b>, and an array of
	 * tree nodes in <b>children</b> containing each of the children of the node
	 * (for non-leaf nodes). The attribute number corresponds to the column
	 * number in the training and test files. The children are ordered in the
	 * same order as the Strings in strings[][]. E.g., if value == 3, then the
	 * array of children correspond to the branches for attribute 3 (named
	 * data[0][3]): children[0] is the branch for attribute 3 == strings[3][0]
	 * children[1] is the branch for attribute 3 == strings[3][1] children[2] is
	 * the branch for attribute 3 == strings[3][2] etc. The class number (leaf
	 * nodes) also corresponds to the order of classes in strings[][]. For
	 * example, a leaf with value == 3 corresponds to the class label
	 * strings[attributes-1][3].
	 **/
	class Tree {

		Tree[] children;
		int value;

		public Tree(Tree[] ch, int val) {
			value = val;
			children = ch;
		} // constructor

		public String toString() {
			return toString("");
		} // toString()

		String toString(String indent) {
			if (children != null) {
				String s = "";
				for (int i = 0; i < children.length; i++)
					s += indent + data[0][value] + "=" + strings[value][i] + "\n" + children[i].toString(indent + '\t');
				return s;
			} else
				return indent + "Class: " + strings[attributes - 1][value] + "\n";
		} // toString(String)

	} // inner class Tree

	private int attributes; // Number of attributes (including the class)
	private int examples; // Number of training examples
	private Tree decisionTree; // Tree learnt in training, used for classifying
	private String[][] data; // Training data indexed by example, attribute
	private String[][] strings; // Unique strings for each attribute
	private int[] stringCount; // Number of unique strings for each attribute

	public ID3() {
		attributes = 0;
		examples = 0;
		decisionTree = null;
		data = null;
		strings = null;
		stringCount = null;
	} // constructor

	public void printTree() {
		if (decisionTree == null)
			error("Attempted to print null Tree");
		else
			System.out.println(decisionTree);
	} // printTree()

	/** Print error message and exit. **/
	static void error(String msg) {
		System.err.println("Error: " + msg);
		System.exit(1);
	} // error()

	static final double LOG2 = Math.log(2.0);

	static double xlogx(double x) {
		return x == 0 ? 0 : x * Math.log(x) / LOG2;
	} // xlogx()

	/**
	 * Execute the decision tree on the given examples in testData, and print
	 * the resulting class names, one to a line, for each example in testData.
	 **/
	public void classify(String[][] testData) {
		if (decisionTree == null) {
			error("Please run training phase before classification");
		} else {
			for (int line = 1; line < testData.length; line++) {
				int clazz = this.classifyExample(decisionTree, testData[line]).value;
				System.out.println(strings[attributes - 1][clazz]);
			}
		}
	} // classify()

	/**
	 * Create decision tree based on the training data
	 * 
	 * @param trainingData
	 *            Data used to train decision tree
	 */
	public void train(String[][] trainingData) {
		indexStrings(trainingData);
		ArrayList<Integer> visitedQuestions = new ArrayList<Integer>();
		decisionTree = this.trainDecisionTree(trainingData, visitedQuestions);
	} // train()

	/**
	 * Find leaf class correspondent to data example by exploring the decision
	 * tree
	 * 
	 * @param tree
	 *            Trained decision tree
	 * @param line
	 *            Data example to be classified
	 * @return Class to which the data example belongs
	 */
	private Tree classifyExample(Tree tree, String[] line) {
		if (tree.children == null) {
			return tree;
		}
		for (int i = 0; i < stringCount[tree.value]; i++) {
			if (line[tree.value].equals(strings[tree.value][i])) {
				return this.classifyExample(tree.children[i], line);
			}
		}
		return null;
	}

	/**
	 * Train decision tree node by node based on which split will result in the
	 * best entropy
	 * 
	 * @param initialData
	 *            Previous node data split
	 * @param visitedQuestions
	 *            Questions (tree nodes) already visited by that branch
	 * @return Trained decision tree
	 */
	@SuppressWarnings("unchecked")
	private Tree trainDecisionTree(String[][] initialData, ArrayList<Integer> visitedQuestions) {
		int leaf = checkForLeaf(initialData);
		if (leaf > -1) {
			return new Tree(null, leaf);
		}
		double initialEntropy = this.computeEntropy(initialData);
		int bestQuestion = this.findBestQuestion(initialEntropy, initialData, visitedQuestions);
		ArrayList<String[][]> splits = this.splitData(bestQuestion, initialData);
		ArrayList<Integer> branchVisitedQuestions = (ArrayList<Integer>) visitedQuestions.clone();
		branchVisitedQuestions.add(bestQuestion);
		Tree[] children = new Tree[splits.size()];
		for (int s = 0; s < splits.size(); s++) {
			children[s] = this.trainDecisionTree(splits.get(s), branchVisitedQuestions);
		}
		return new Tree(children, bestQuestion);
	}

	/**
	 * Check if a data set contains only examples of the same class
	 * 
	 * @param dataSplit
	 *            Data set
	 * @return True if all examples belong to the same class, false otherwise
	 */
	private int checkForLeaf(String[][] dataSplit) {
		boolean sameClass;
		for (int clazz = 0; clazz < stringCount[attributes - 1]; clazz++) {
			sameClass = true;
			for (int ex = 1; ex < dataSplit.length; ex++) {
				if (!dataSplit[ex][attributes - 1].equals(strings[attributes - 1][clazz])) {
					sameClass = false;
					break;
				}
			}
			if (sameClass) {
				return clazz;
			}
		}
		return -1;
	}

	/**
	 * Splits data set based on the best question, groups data by the attribute
	 * values of the best question
	 * 
	 * @param bestQuestion
	 *            Question with the best gain, based on which the data will be
	 *            split
	 * @param initialData
	 *            Data to be split
	 * @return List of data splits
	 */
	private ArrayList<String[][]> splitData(int bestQuestion, String[][] initialData) {
		int[] attrDistribution = new int[stringCount[bestQuestion]];
		for (int ex = 1; ex < initialData.length; ex++) {
			for (int value = 0; value < stringCount[bestQuestion]; value++) {
				if (initialData[ex][bestQuestion].equals(strings[bestQuestion][value])) {
					attrDistribution[value]++;
					break;
				}
			}
		}
		ArrayList<String[][]> splits = new ArrayList<String[][]>();
		for (int attrValue = 0; attrValue < stringCount[bestQuestion]; attrValue++) {
			String[][] split = new String[attrDistribution[attrValue] + 1][attributes];
			split[0] = initialData[0];
			int count = 1;
			for (int line = 1; line < initialData.length; line++) {
				if (initialData[line][bestQuestion].equals(strings[bestQuestion][attrValue])) {
					split[count] = initialData[line];
					count++;
				}
			}
			if (split.length > 1) {
				splits.add(split);
			}
		}
		return splits;
	}

	/**
	 * Calculates entropy of a determined data set
	 * 
	 * @param dataSplit
	 *            Data set
	 * @return Entropy of data split
	 */
	private double computeEntropy(String[][] dataSplit) {
		double[] classDistribution = new double[stringCount[attributes - 1]];
		for (int ex = 1; ex < dataSplit.length; ex++) {
			for (int clazz = 0; clazz < stringCount[attributes - 1]; clazz++) {
				if (dataSplit[ex][attributes - 1].equals(strings[attributes - 1][clazz])) {
					classDistribution[clazz]++;
					break;
				}
			}
		}
		double entropy = 0.0;
		for (int c = 0; c < classDistribution.length; c++) {
			entropy -= (xlogx(classDistribution[c] / (dataSplit.length - 1)));
		}
		return entropy;
	}

	/**
	 * Finds best question based on which data split has the best gain on
	 * entropy
	 * 
	 * @param initialEntropy
	 *            Initial entropy of data set
	 * @param dataSplit
	 *            Data set
	 * @param visitedQuestions
	 *            Questions already visited by that branch
	 * @return Index of the best question
	 */
	private int findBestQuestion(double initialEntropy, String[][] dataSplit, ArrayList<Integer> visitedQuestions) {
		double[] gain = new double[attributes - 1];
		for (int a = 0; a < attributes - 1; a++) {
			if (!visitedQuestions.contains(a)) {
				double[][] distribution = new double[stringCount[a]][stringCount[attributes - 1]];
				for (int ex = 1; ex < dataSplit.length; ex++) {
					int clazz = 0;
					for (int result = 0; result < stringCount[attributes - 1]; result++) {
						if (dataSplit[ex][attributes - 1].equals(strings[attributes - 1][result])) {
							clazz = result; // find class of data line
							break;
						}
					}
					for (int value = 0; value < stringCount[a]; value++) {
						if (dataSplit[ex][a].equals(strings[a][value])) { // find value of attribute a
							distribution[value][clazz]++; // compute value
							break;
						}
					}
				}
				gain[a] = this.computeGain(initialEntropy, distribution, a, dataSplit.length - 1);
			} else {
				gain[a] = -Double.MAX_VALUE;
			}
		}
		int bestQuestion = 0;
		double helper = -Double.MAX_VALUE;
		for (int question = 0; question < gain.length; question++) {
			if (gain[question] > helper) {
				helper = gain[question];
				bestQuestion = question;
			}
		}
		return bestQuestion;
	}

	/**
	 * Calculates gain of a certain question split
	 * 
	 * @param initialEntropy
	 *            Initial entropy of data set before split
	 * @param distribution
	 *            Distribution of classes of data examples
	 * @param attribute
	 *            Attribute with which the split was made
	 * @param dataSize
	 *            Size of data set
	 * @return Gain of the split made with the attribute
	 */
	private double computeGain(double initialEntropy, double[][] distribution, int attribute, int dataSize) {
		double[] attrClassDistribution = new double[stringCount[attribute]];
		for (int a = 0; a < distribution.length; a++) {
			for (int c = 0; c < distribution[a].length; c++) {
				attrClassDistribution[a] += (double) distribution[a][c];
			}
		}
		double gain = initialEntropy;
		for (int i = 0; i < distribution.length; i++) {
			double entropy = 0.0;
			for (int j = 0; j < distribution[i].length; j++) {
				entropy -= (xlogx(distribution[i][j] / attrClassDistribution[i]));
			}
			gain -= (attrClassDistribution[i] / dataSize) * entropy;
		}
		return gain;
	}

	/**
	 * Given a 2-dimensional array containing the training data, numbers each
	 * unique value that each attribute has, and stores these Strings in
	 * instance variables; for example, for attribute 2, its first value would
	 * be stored in strings[2][0], its second value in strings[2][1], and so on;
	 * and the number of different values in stringCount[2].
	 **/
	void indexStrings(String[][] inputData) {
		data = inputData;
		examples = data.length;
		attributes = data[0].length;
		stringCount = new int[attributes];
		strings = new String[attributes][examples];// might not need all columns
		int index = 0;
		for (int attr = 0; attr < attributes; attr++) {
			stringCount[attr] = 0;
			for (int ex = 1; ex < examples; ex++) {
				for (index = 0; index < stringCount[attr]; index++)
					if (data[ex][attr].equals(strings[attr][index]))
						break; // we've seen this String before
				if (index == stringCount[attr]) // if new String found
					strings[attr][stringCount[attr]++] = data[ex][attr];
			} // for each example
		} // for each attribute
	} // indexStrings()

	/**
	 * For debugging: prints the list of attribute values for each attribute and
	 * their index values.
	 **/
	void printStrings() {
		for (int attr = 0; attr < attributes; attr++)
			for (int index = 0; index < stringCount[attr]; index++)
				System.out.println(data[0][attr] + " value " + index + " = " + strings[attr][index]);
	} // printStrings()

	/**
	 * Reads a text file containing a fixed number of comma-separated values on
	 * each line, and returns a two dimensional array of these values, indexed
	 * by line number and position in line.
	 **/
	static String[][] parseCSV(String fileName) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String s = br.readLine();
		int fields = 1;
		int index = 0;
		while ((index = s.indexOf(',', index) + 1) > 0)
			fields++;
		int lines = 1;
		while (br.readLine() != null)
			lines++;
		br.close();
		String[][] data = new String[lines][fields];
		Scanner sc = new Scanner(new File(fileName));
		sc.useDelimiter("[,\n]");
		for (int l = 0; l < lines; l++)
			for (int f = 0; f < fields; f++)
				if (sc.hasNext())
					data[l][f] = sc.next();
				else
					error("Scan error in " + fileName + " at " + l + ":" + f);
		sc.close();
		return data;
	} // parseCSV()

	public static void main(String[] args) throws FileNotFoundException, IOException {
		if (args.length != 2)
			error("Expected 2 arguments: file names of training and test data");
		String[][] trainingData = parseCSV(args[0]);
		String[][] testData = parseCSV(args[1]);
		ID3 classifier = new ID3();
		classifier.train(trainingData);
		classifier.printTree();
		classifier.classify(testData);
	} // main()

} // class ID3
