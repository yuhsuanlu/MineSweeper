
/**
  VisibleField class
  This is the data that's being displayed at any one point in the game (i.e., visible field, because it's what the
  user can see about the minefield). Client can call getStatus(row, col) for any square.
  It actually has data about the whole current state of the game, including  
  the underlying mine field (getMineField()).  Other accessors related to game status: numMinesLeft(), isGameOver().
  It also has mutators related to actions the player could do (resetGameDisplay(), cycleGuess(), uncover()),
  and changes the game state accordingly.
  
  It, along with the MineField (accessible in mineField instance variable), forms
  the Model for the game application, whereas GameBoardPanel is the View and Controller, in the MVC design pattern.
  It contains the MineField that it's partially displaying.  That MineField can be accessed (or modified) from 
  outside this class via the getMineField accessor.  
 */
public class VisibleField {
   // ----------------------------------------------------------   
   // The following public constants (plus numbers mentioned in comments below) are the possible states of one
   // location (a "square") in the visible field (all are values that can be returned by public method 
   // getStatus(row, col)).
   
   // The following are the covered states (all negative values):
   public static final int COVERED = -1;   // initial value of all squares
   public static final int MINE_GUESS = -2;
   public static final int QUESTION = -3;

   // The following are the uncovered states (all non-negative values):
   
   // values in the range [0,8] corresponds to number of mines adjacent to this square
   
   public static final int MINE = 9;      // this loc is a mine that hasn't been guessed already (end of losing game)
   public static final int INCORRECT_GUESS = 10;  // is displayed a specific way at the end of losing game
   public static final int EXPLODED_MINE = 11;   // the one you uncovered by mistake (that caused you to lose)
   // ----------------------------------------------------------   
  
   // <put instance variables here>

   private MineField mineField;   // class with locations of mines for a game
   private int[][] gameMatrix;   // store the staus of every position in 2D array
   private int numMines;   // number of the mines
   private int numMinesGuessed;   // store the number of the mines that player already guessed



   /**
      Create a visible field that has the given underlying mineField.
      The initial state will have all the mines covered up, no mines guessed, and the game
      not over.
      @param mineField  the minefield to use for this VisibleField
    */
   public VisibleField(MineField mineField) {
      this.mineField = mineField;
      numMines = mineField.numMines();
      gameMatrix = new int[mineField.numRows()][mineField.numCols()];
      resetGameDisplay();   // The inital state of the game
   }
   
   
   /**
      Reset the object to its initial state (see constructor comments), using the same underlying
      MineField. 
   */     
   public void resetGameDisplay() {

      for (int row = 0; row < mineField.numRows(); row++){
         for (int col = 0; col < mineField.numCols(); col++){
            gameMatrix[row][col] = COVERED;
         }
      }

   }
  
   
   /**
      Returns a reference to the mineField that this VisibleField "covers"
      @return the minefield
    */
   public MineField getMineField() {

      return mineField;

   }
   
   
   /**
      Returns the visible status of the square indicated.
      @param row  row of the square
      @param col  col of the square
      @return the status of the square at location (row, col).  See the public constants at the beginning of the class
      for the possible values that may be returned, and their meanings.
      PRE: getMineField().inRange(row, col)
    */
   public int getStatus(int row, int col) {

      return gameMatrix[row][col];

   }

   
   /**
      Returns the the number of mines left to guess.  This has nothing to do with whether the mines guessed are correct
      or not.  Just gives the user an indication of how many more mines the user might want to guess.  This value can
      be negative, if they have guessed more than the number of mines in the minefield.     
      @return the number of mines left to guess.
    */
   public int numMinesLeft() {

      return (numMines - numMinesGuessed);

   }
 
   
   /**
      Cycles through covered states for a square, updating number of guesses as necessary.  Call on a COVERED square
      changes its status to MINE_GUESS; call on a MINE_GUESS square changes it to QUESTION;  call on a QUESTION square
      changes it to COVERED again; call on an uncovered square has no effect.  
      @param row  row of the square
      @param col  col of the square
      PRE: getMineField().inRange(row, col)
    */
   public void cycleGuess(int row, int col) {

      if (getStatus(row, col) == COVERED){
         gameMatrix[row][col] = MINE_GUESS;
         numMinesGuessed++;
      }
      else if (getStatus(row, col) == MINE_GUESS){
         gameMatrix[row][col] = QUESTION;
         numMinesGuessed--;
      }
      else if (getStatus(row, col) == QUESTION){
         gameMatrix[row][col] = COVERED;
      }
      else{
         return;   // Call on an uncovered square has no effect
      }

   }

   
   /**
      Uncovers this square and returns false iff you uncover a mine here.
      If the square wasn't a mine or adjacent to a mine it also uncovers all the squares in 
      the neighboring area that are also not next to any mines, possibly uncovering a large region.
      Any mine-adjacent squares you reach will also be uncovered, and form 
      (possibly along with parts of the edge of the whole field) the boundary of this region.
      Does not uncover, or keep searching through, squares that have the status MINE_GUESS. 
      Note: this action may cause the game to end: either in a win (opened all the non-mine squares)
      or a loss (opened a mine).
      @param row  of the square
      @param col  of the square
      @return false   iff you uncover a mine at (row, col)
      PRE: getMineField().inRange(row, col)
    */
   public boolean uncover(int row, int col) {

      if (mineField.hasMine(row, col)) {
         gameMatrix[row][col] = EXPLODED_MINE;

         // When game ends, update gameMatrix.
         for (int i = 0; i < mineField.numRows(); i++) {
            for (int j = 0; j < mineField.numCols(); j++) {
               if ((getStatus(i, j) == MINE_GUESS) && !mineField.hasMine(i, j)) {
                  gameMatrix[i][j] = INCORRECT_GUESS;
               } else if (((getStatus(i, j) == QUESTION) || (getStatus(i, j) == COVERED)) && mineField.hasMine(i, j)) {
                  gameMatrix[i][j] = MINE;
               }
            }
         }
         return false;
      }
      else {
         openAdjSquares(row, col);
         return true;
      }


   }
 
   
   /**
      Returns whether the game is over.
      (Note: This is not a mutator.)
      @return whether game over
    */
   public boolean isGameOver() {

      // if find any exploded mine, the game ends (lose)
      for (int row = 0; row < mineField.numRows(); row++) {
         for (int col = 0; col < mineField.numCols(); col++) {
            if (gameMatrix[row][col] == EXPLODED_MINE) {
               return true;
            }
         }
      }

      // still have squares are not opened & are non-mine -> the game not yet ends
      for (int row = 0; row < mineField.numRows(); row++) {
         for (int col = 0; col < mineField.numCols(); col++) {
            if (!mineField.hasMine(row, col) && !isUncovered(row, col)) {
               return false;
            }
         }
      }

      // for other case (remaining square are not opened & are mine), the game ends (win).
      // Before ending the game, update remaining unguessed squares as yellow squares.
      for (int row = 0; row < mineField.numRows(); row++) {
         for (int col = 0; col < mineField.numCols(); col++) {
            if (mineField.hasMine(row, col) && !isUncovered(row, col)) {
               gameMatrix[row][col] = MINE_GUESS;
            }
         }
      }
      return true;
   }
 
   
   /**
      Returns whether this square has been uncovered.  (i.e., is in any one of the uncovered states, 
      vs. any one of the covered states).
      @param row of the square
      @param col of the square
      @return whether the square is uncovered
      PRE: getMineField().inRange(row, col)
    */
   public boolean isUncovered(int row, int col) {
      return (gameMatrix[row][col] > COVERED);
   }
   
 
   // <put private methods here>


   /**
    * Automatic open all the squares (adjacent with the square user click) in that region which has no adjacent mines.
    * Continuously process it until meet below 3 cases:
    * CASE 1: reaches to the boundary of the field
    * CASE 2: already in uncovered state
    * CASE 3: the square where user guess it as mine
    * CASE 4: reach the squares that are adjacent to the other mine(s)
    */
   private void openAdjSquares(int row, int col){

      // CASE 1, 2, 3
      if (!getMineField().inRange(row, col) || isUncovered(row, col) || gameMatrix[row][col] == MINE_GUESS) {
         return;
      }

      // CASE 4
      int numAdjMines = mineField.numAdjacentMines(row, col);
      gameMatrix[row][col] = numAdjMines;

      if(numAdjMines > 0){
         return;
      }

      for (int i = row - 1; i <= row + 1; i++) {
         for (int j = col - 1; j <= col + 1; j++) {
            openAdjSquares(i, j);
         }
      }

   }

}
