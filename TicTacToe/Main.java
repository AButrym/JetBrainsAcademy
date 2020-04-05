// https://hyperskill.org/projects/48?goal=7
// Model a Tic-Tac-Toe game with simple CLI

package tictactoe;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private char[][] board = new char[3][3];
    private int row, col;
    boolean readMoveOK;

    public Main() {
        for (int i = 0, k = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '_';
            }
        }
    }

    public boolean isReadMoveOK() {
        return readMoveOK;
    }

    private void print() {
        System.out.println("---------");
        for (char[] row : board) {
            System.out.format("| %c %c %c |%n", row[0], row[1], row[2]);
        }
        System.out.println("---------");
    }

    private void readBoard() {
        char[] cells = sc.nextLine().toCharArray();
        for (int i = 0, k = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = cells[k++];
            }
        }
    }

    private enum State {
        GAME_NOT_FINISHED("Game not finished"),
        DRAW("Draw"),
        X_WINS("X wins"),
        O_WINS("O wins"),
        IMPOSSIBLE("Impossible");

        private String str;

        State(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    private enum ErrorMessages {
        OCCUPIED("This cell is occupied! Choose another one!"),
        NOT_NUMBERS("You should enter numbers!"),
        OUT_OF_RANGE("Coordinates should be from 1 to 3!");

        private String str;
        ErrorMessages(String str) {
            this.str = str;
        }
        @Override
        public String toString() {
            return str;
        }
    }

    private int count(char ch) {
        int res = 0;
        for (char[] row : board) {
            for (char cell : row) {
                if (cell == ch) res++;
            }
        }
        return res;
    }

    private void readCoords() {
        System.out.print("Enter the coordinates: ");
        int i, j;
        try {
            i = sc.nextInt();
            j = sc.nextInt();
            sc.nextLine();
        } catch (InputMismatchException e) {
            sc.nextLine(); // clean the input
            System.out.println(ErrorMessages.NOT_NUMBERS);
            readMoveOK = false;
            return;
        }
        if (i < 1 || i > 3 || j < 1 || j > 3) {
            System.out.println(ErrorMessages.OUT_OF_RANGE);
            readMoveOK = false;
            return;
        }
        col = i - 1;
        row = 3 - j;
        if (board[row][col] != '_') {
            System.out.println(ErrorMessages.OCCUPIED);
            readMoveOK = false;
            return;
        }
        readMoveOK = true;
    }

    private void makeMove(char ch) {
        board[row][col] = ch;
        readMoveOK = false;
    }

    private boolean has3(char c) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == c && board[i][1] == c && board[i][2] == c) return true;
            if (board[0][i] == c && board[1][i] == c && board[2][i] == c) return true;
        }
        if (board[0][0] == c && board[1][1] == c && board[2][2] == c) return true;
        if (board[2][0] == c && board[1][1] == c && board[0][2] == c) return true;
        return false;
    }

    public State evaluate() {
        int x = count('X');
        int o = count('O');
        int u = count('_');
        boolean xWin = has3('X');
        boolean oWin = has3('O');
        if (Math.abs(x - o) > 1 || xWin && oWin) return State.IMPOSSIBLE;
        if (xWin) return State.X_WINS;
        if (oWin) return State.O_WINS;
        if (u > 0) return State.GAME_NOT_FINISHED;
        return State.DRAW;
    }

    public static void main(String[] args) {
        Main game = new Main();
        game.print();
        char turn = 'X';
        while (game.evaluate() == State.GAME_NOT_FINISHED) {
            while (!game.isReadMoveOK()) { game.readCoords(); }
            game.makeMove(turn);
            game.print();
            turn = turn == 'X' ? 'O' : 'X';
        }
        System.out.println(game.evaluate());
    }
}
/* Log of a typical usage:
---------
|       |
|       |
|       |
---------
Enter the coordinates: 2 2
---------
|       |
|   X   |
|       |
---------
Enter the coordinates: 2 2
This cell is occupied! Choose another one!
Enter the coordinates: two two
You should enter numbers!
Enter the coordinates: 1 4
Coordinates should be from 1 to 3!
Enter the coordinates: 1 3
---------
| O     |
|   X   |
|       |
---------
Enter the coordinates: 3 1
---------
| O     |
|   X   |
|     X |
---------
Enter the coordinates: 1 2
---------
| O     |
| O X   |
|     X |
---------
Enter the coordinates: 1 1
---------
| O     |
| O X   |
| X   X |
---------
Enter the coordinates: 3 2
---------
| O     |
| O X O |
| X   X |
---------
Enter the coordinates: 2 1
---------
| O     |
| O X O |
| X X X |
---------
X wins
*/
