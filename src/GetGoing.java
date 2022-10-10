import static java.lang.System.out;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class GetGoing {
    // The Problem Input In String Format
    static List<String> problemInput;
    // The Problem Data Represented In A 2D Matrix
    static Object[][] matrix;
    // The Size Of The Matrix
    static int rows, columns;

    static void main(String[] args) {
        out.println("Welcome To The Simplex Algorithm: "
                + "Maximization With Problem Constraints Of The Form <= "
                + "For Linear Programming Solving.");
        inputProblem();
        defineProblemVariables();
        fillMatrix();
        applySimplex();
        outputSolution();
    }

    static void inputProblem() {
        Scanner keyboard = new Scanner(System.in);
        problemInput = new ArrayList<>();

        out.println("Enter All The Coefficients Explicitly.");
        out.println("Enter The Objective Function:");
        problemInput.add(keyboard.nextLine());

        out.println("Enter The Constraints:");
        out.println("Enter (Exit) To Stop Inputting Constraints.");

        String constraintInput;
        do {
            constraintInput = keyboard.nextLine();
            problemInput.add(constraintInput);
        }
        while(!constraintInput.equalsIgnoreCase("Exit"));
        // Delete The Last Element Of The List (Exit)
        problemInput.remove(problemInput.size() - 1);
    }

    /**
     * Filling In The Problem's Variables Into The Matrix
     * After Calling Sub-Procedures To Analyze The Ugly String-Formatted Input
     */
    static void defineProblemVariables() {
        // A List Of All Of The Problem Variables
        List<String> problemVariables = new ArrayList<>();

        problemVariables.addAll(analyzeObjectiveFunction());
        problemVariables.addAll(analyzeConstraints());

        int originalVariableCount = analyzeObjectiveFunction().size();
        int slackVariableCount = analyzeConstraints().size();

        // (The Added 3 Are: The Variables' Column + The Function's Column + The RHS Column)
        columns = originalVariableCount + slackVariableCount + 3;
        // (The Added 2 Are: The Variables' Row + The Function's Row)
        rows = slackVariableCount + 2;

        // After Determining The Size Of The Matrix, Construct It
        matrix = new Object[rows][columns];

        // Fill The First Column With The Slack Variables & The Function's Variable
        for(int i = 1; i < rows - 1; i++)
            matrix[i][0] =
                    problemVariables.get(problemVariables.size() - originalVariableCount - 2 + i);
        matrix[rows - 1][0] = problemInput.get(0).charAt(0);

        // Fill The First Row With All Of The Variables & The Function's Variable & The RHS
        for(int i = 1; i < columns - 2; i++)
            matrix[0][i] = problemVariables.get(i - 1);
        matrix[0][problemVariables.size() + 1] = problemInput.get(0).charAt(0);
        matrix[0][columns - 1] = "RHS";
    }

    /**
     * Give Me A List Of The Original Problem Variables
     */
    static List<String> analyzeObjectiveFunction() {
        List<String> problemOriginalVariables = new ArrayList<>();

        // The problemInput's First Element Is The Objective Function
        String objectiveFunction = problemInput.get(0);

        // Skip The Character With Index 0 Because That's The Objective Function's Variable
        int index = 1;

        while(index < objectiveFunction.length()) {
            // A Variable Is Only (One) Small Letter Of The English Alphabet
            if(objectiveFunction.charAt(index) >= 'a' && objectiveFunction.charAt(index) <= 'z') {
                problemOriginalVariables.add(Character.toString(objectiveFunction.charAt(index)));
            }
            index++;
        }
        return problemOriginalVariables;
    }

    /**
     * Give Me A List Of The Slack Variables That Need To Be Added To The Constraints
     */
    static List<String> analyzeConstraints() {
        List<String> slackVariables = new ArrayList<>();

        // A Counter For The Slack Variables, Helps In Naming Them (s1, s2, ...)
        int slackVariableNumber = 1;

        // Go Through The Constraints (problemInput)
        // Skip The First problemInput's Element Because That's The Objective Function
        // Hence, (i = 1)
        for(int i = 1; i < problemInput.size(); i++) {
            String constraint = problemInput.get(i);

            // Checking If The Current Constraint String Contains A < Sign
            if(constraint.contains("<")) {
                // If True, Here We Add A Slack Variable To The Constraint
                slackVariables.add("s" + slackVariableNumber);
                slackVariableNumber++;
            }
        }
        return slackVariables;
    }

    // You Don't Wanna Do This
    static void fillMatrix() {
        // Going Through The Constraints
        for(int i = 1; i < problemInput.size(); i++) {
            // A List That's Gonna Contain The Constraint's Variables' Coefficients
            List<Double> coefficients =
                    new ArrayList(columns - 1);

            String constraint = problemInput.get(i);
            int index = 0;
            // The Index From Where We're Going To Substring The Constraint
            int start = index;

            while(constraint.charAt(index) != '<') {
                while(!(constraint.charAt(index) >= 'a' && constraint.charAt(index) <= 'z')) {
                    index++;
                    if(constraint.charAt(index - 1) == '=') {
                        break;
                    }
                }

                coefficients.add(Double.parseDouble(constraint.substring(start, index)));
                index++;
                start = index;
            }
            // In Case The Constraint Is An Equation And Doesn't Need A Slack Variable
            // Then, All Of The Slack Variables'_That Are Added To The Other Constraints_
            // Coefficients In This Constraint Are Equal To 0
            // The Function Variable's Coefficient Is Equal To 0 In All Of The Constraints
            // Hence, (analyzeConstraints.size()) + 1)
            if(!(constraint.contains("<"))) {
                for(int j = 0; j < analyzeConstraints().size() + 1; j++)
                    coefficients.add(0.0D);
            }

            // In Case The Constraint Is An Inequality And Does Need A Slack Variable
            // Then, Initially Do The Same As Above (Adding Zeros)
            else {
                for(int j = 0; j < analyzeConstraints().size(); j++)
                    coefficients.add(0.0D);
                // Here, An Inequality Constraint Needs Only One Slack Variable
                // Aka, Only One Slack Variable's Coefficient Is Gonna Be 1
                // And The Rest Are Gonna Be 0
                // So, Add That 1 In The Right Place According To The Current Constraint
                coefficients.add(analyzeObjectiveFunction().size() - 1 + i,1.0D);
            }

            start += 2; // Skipping The Two Signs "<" & "="
            coefficients.add(Double.parseDouble(constraint.substring(start)));

            // Add The Coefficients To The Matrix
            for(int j = 1; j < columns; j++)
                matrix[i][j] = coefficients.get(j - 1);
        }

        // The Objective Function
        List<Double> coefficients = new ArrayList<>(columns - 1);
        String objectiveFunction = problemInput.get(0);
        int index = 0;
        int current = 2; // Already Skipping The Function's Variable & The "=" Sign

        while(index < objectiveFunction.length()) {
            while((!(objectiveFunction.charAt(index) >= 'a'
                    && objectiveFunction.charAt(index) <= 'z')) || index == 0) {
                index++;
            }

            coefficients.add(Double.parseDouble(objectiveFunction.substring(current, index)));
            index++;
            current = index;
        }
        // The Objective Function Slack Variables' Coefficients Are All 0.
        for(int i = 0; i < analyzeConstraints().size(); i++)
            coefficients.add(0.0D);
        // The Coefficient Of The Function's Variable Itself
        coefficients.add(-1.0D);
        // The RHS Of The Equation After Moving All Of The Variables To The LHS Is 0
        coefficients.add(0.0D);

        // Moving The Variables To The RHS Is Really Taking The Opposite Of Their Coefficients
        for(int i = 0; i < coefficients.size(); i++)
            coefficients.set(i, -1 * coefficients.get(i));

        // Add The Coefficients To The Matrix
        for(int i = 1; i < columns; i++)
            matrix[rows - 1][i] = coefficients.get(i - 1);
    }

    /**
     * A Bunch Of Sub-Procedures Calls Applying The Simplex Algorithm Step By Step
     */
    static void applySimplex() {
        int pivotColumnIndex;

        // This Process Continues On Repeating Unless
        // The Returned Pivot Column Index Is -1
        // Meaning The Last Row Now Contains only Positive Values
        while((pivotColumnIndex = findMostNegativeNumber()) != -1) {
            // Call computeRatios And Assign Its Returned Value To pivotRowIndex
            int pivotRowIndex = computeRatios(pivotColumnIndex);

            // Self-Explanatory ;)
            makePivotInto1(pivotRowIndex, pivotColumnIndex);
            makePivotColumnInto0(pivotRowIndex, pivotColumnIndex);
            matrix[pivotRowIndex][0] = matrix[0][pivotColumnIndex];
        }
    }

    /**
     * Iterate Through The Matrix Last Row (The Objective Function's Row)
     * Find Me The Most Negative Number
     * Return Me Its Column Index And That Will Be Out Pivot Column Index
     */
    static int findMostNegativeNumber() {
        // Initially, We Assume The First Element's Index In The Last Row
        // To Be Our Pivot Column Index
        int pivotColumnIndex = 1;

        //Then, We Go Through The Remaining Elements To Find The Most Negative Number
        // And Memorize Its Column Index (We'll Need It To Find Our Pivot Element)
        // We're Not Reaching The Last Column (columns-1)
        // Because We Don't Care About The Right-Hand Side Constants
        for(int i = 2; i < columns - 1; i++) {
            if((double)(matrix[rows - 1][i]) < (double)(matrix[rows - 1][pivotColumnIndex]))
                pivotColumnIndex = i;
        }

        // Checking If The Most Negative Number Doesn't Actually Exist
        // (Meaning The Last Row Doesn't Contain Any More Negative Elements)
        // If True, Return An Invalid Index (-1) To Indicate That
        // If False, Return The Found pivot Column Index
        if((double)(matrix[rows - 1][pivotColumnIndex]) >= 0.0D)
            return -1;
        return pivotColumnIndex;
    }

    /**
     * Compute The Ratios By Dividing The Right-Hand Side Column Constants By
     * The Pivot Column Elements
     * Find Me The Smallest Ratio
     * Return Me Its Row Index And That Will Be Out Pivot Row Index
     */
    static int computeRatios(int pivotColumnIndex) {
        double smallestRatio = 9999999999.9D;
        // Initially, We Assume The First Element's Index In The First Column
        // To Be Our Pivot Row Index
        int pivotRowIndex = 1;

        // Go Through Every Row And Compute The Ratio Between The Right-Hand Side Constant
        // And The Pivot Column Element
        for(int i = 1; i < rows - 1; i++) {
            double ratio;
            // Checking If The Divisor (Pivot Column Element) Is Equal To Zero
            if((double)(matrix[i][pivotColumnIndex]) == 0.0D)
                // If True, Can't Divide By Zero So Skip This Iteration
                // And The Current Row Can't Be Our Pivot Row
                continue;
            // If False, Compute The Ratio
            ratio = (double)(matrix[i][columns - 1]) / (double)(matrix[i][pivotColumnIndex]);
            // If The Computed Ratio Is Smaller Than The Memorized smallestRatio And Positive
            // Then, Memorize It And Its Row Index (We'll Need It To Find Our Pivot Element)
            if(ratio < smallestRatio && ratio > 0.0D) {
                smallestRatio = ratio;
                pivotRowIndex = i;
            }
        }
        return pivotRowIndex;
    }

    /**
     * Divide The Pivot Row By The Pivot Element To Make It Into 1
     */
    static void makePivotInto1(int pivotRowIndex, int pivotColumnIndex) {
        // Our Pivot
        double divisor = (double)(matrix[pivotRowIndex][pivotColumnIndex]);

        // Divide The Pivot Row By The Pivot
        for(int i = 1; i < columns; i++)
            (matrix[pivotRowIndex][i]) = (double)(matrix[pivotRowIndex][i]) / divisor;
    }

    /**
     * Go Through Every Non-Pivot Row
     * Make Every Pivot Column Element (Except For The Pivot) Into 0
     */
    static void makePivotColumnInto0(int pivotRowIndex, int pivotColumnIndex) {
        for(int i = 1; i < rows; i++) {
            // If The Current Row Is Our Pivot Row Or
            // The Pivot Column Element Is Equal To 0 (Already 0, Doesn't Need Modification)
            // Then, Skip This Iteration
            if(i == pivotRowIndex || (double)(matrix[i][pivotColumnIndex]) == 0.0D)
                continue;

            // Else, Compute The Pivot Row Coefficient
            // That's Gonna Be The Opposite Of The Pivot Column Element
            double pivotRowCoefficient = -1 * (double)(matrix[i][pivotColumnIndex]);

            // Add The Pivot Row Multiplied By The Pivot Row Coefficient
            // To The Current Row And That Will Be Our New Row
            for(int j = 1; j < columns; j++)
                matrix[i][j] =
                        pivotRowCoefficient * (double)(matrix[pivotRowIndex][j]) + (double)(matrix[i][j]);
        }
    }

    static void outputSolution() {
        out.println("The Solution To This Problem:");
        out.print("The Objective Function " + matrix[rows - 1][0]
                + " Reaches Its Optimal Value Which Is Equal To ");

        out.printf("%.2f\n", matrix[rows - 1][columns - 1]);
        out.println("When");
        for(int i = 1; i < rows - 1; i++) {
            out.print(matrix[i][0] + " = ");
            out.printf("%.2f\n", matrix[i][columns - 1]);
        }
    }
}