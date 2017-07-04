package org.minifigure;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.*;
import lejos.robotics.Color;
import lejos.robotics.objectdetection.Feature;
import lejos.robotics.objectdetection.FeatureDetector;
import lejos.robotics.objectdetection.FeatureListener;
import lejos.utility.Delay;
import lejos.utility.TextMenu;

public class TicTacToe implements FeatureListener{
	private static final int SAMPLESIZE=20;
	private static long sum_points;
	private static long[][] points = new long[3][3];
	private int[][] board;
	private static final int SIGN_ROBOT = 2;
	private static final int SIGN_HUMAN = 1;
	private static final int MOVE_ROBOT = 1;
	private static final int MOVE_HUMAN = 2;
	private static final int COLOR_HUMAN = 0; // BLUE BALLS
	private static final int COLOR_ROBOT = 1; // RED BALLS
	private int g_row;
	private int g_column;
	HandSensor myHandSensor = new HandSensor();
	Arm myArm = new Arm();
	Camera myCamera = new Camera();
	int myBoard[][]= new int[3][3];
	boolean handDetected=false;
	
	public TicTacToe() {
		// initialize the board.
		board = new int[3][3];
		for (int ndx1=0;ndx1<3;ndx1++) {
			for (int ndx2=0;ndx2<3;ndx2++) {
				board[ndx1][ndx2] = 0;
			}
		}
		// connect the hand detector to this class
		myHandSensor.detector.addListener(this);
		// calibrate the board or read in safed calibration file
		calibration();
	}
	
	public void featureDetected(Feature feature, FeatureDetector detector) {
		handDetected=true;
		Sound.beep();
	}
	
	private void calibration () {
		/*
		String[] menuItems={"Load","New"};
		TextMenu menu=new TextMenu(menuItems,1,"Calibrate Field:");
		int menu_item;
		menu_item=menu.select();
	    	switch (menu_item) {
			case 0: myCamera.readBoardCalibration();
			break;
			case 1: myCamera.calibrateBoard();
			break;
	    }
	    */
		LCD.clear();
		LCD.refresh();
		// shortcut so that I do not have to go through the menu
		myCamera.readBoardCalibration();
	}

	private void resetPoints() {
    		sum_points = 0;
    		for (int i=0;i<3;i++) {
    			for (int j=0;j<3;j++) {
    				points[i][j] = Long.MIN_VALUE;
    			}
    		}
	}

	private boolean won(int[][] board, int player) {
		// are there rows of three marks?
		int count = 0;
		for (int ndx1=0;ndx1<3;ndx1++) {
			for (int ndx2=0;ndx2<3;ndx2++) {
				if (board[ndx1][ndx2] == player) {
					count++;
				}
			}
			if (count == 3) {
				return true;
			} else {
				count = 0;
			}
		}

		// are there columns of three marks?
		count = 0;
		for (int ndx2=0;ndx2<3;ndx2++) {
			for (int ndx1=0;ndx1<3;ndx1++) {
				if (board[ndx1][ndx2] == player) {
					count++;
				}
			}
			if (count == 3) {
				return true;
			} else {
				count = 0;
			}
		}

		// is the diagonal marked from left bottom to right height?
		count = 0;
		for (int ndx=0;ndx<3;ndx++) {
			if (board[ndx][ndx] == player) {
				count++;
			}
		}
		if (count == 3) {
			return true;
		}

		// is the diagonal marked from right bottom to left height?
		count = 0;
		for (int ndx=0;ndx<3;ndx++) {
			if (board[ndx][2 - ndx] == player) {
				count++;
			}
		}
		if (count == 3) {
			return true;
		}
		return false;
	}
    
	private boolean undecided(int[][] board) {
		boolean undec = true;
		// are there empty positions on the board?
		for (int ndx1=0;ndx1<3;ndx1++) {
			for (int ndx2=0;ndx2<3;ndx2++) {
				if (board[ndx1][ndx2] == 0) {
					undec = false;
					break;
				}
			}
		}
		return undec;
	}
    
	// miniMax algorithm to find the best move for the robot
	private void findRobotMove(int[][] board, int move, int level) {
		// rating function.
		if (won(board, move)) {
			if (move == SIGN_HUMAN) {
				sum_points -= 1;
			} else if (move == SIGN_ROBOT) {
				sum_points += 1;
			}
			return;
		} else if (undecided(board)) {
			return;
		}

		// analyze all constellations.
		for (int i=0;i<3;i++) {
			for (int j=0;j<3;j++) {
				if (board[i][j] == 0) {
					board[i][j] = move;
					if (move == 1) {
						findRobotMove(board, SIGN_ROBOT, level+1);
						if (level == 1) {
							points[i][j] = sum_points;
							sum_points = 0;
						}
					} else if (move == 2) {
						findRobotMove(board, SIGN_HUMAN, level+1);
						if (level == 1) {
							points[i][j] = sum_points;
							sum_points = 0;
						}
					}
					board[i][j] = 0;
				}
			}
		}
	}
    
	// safe the robot's move in the board array
	private void moveRobot(int[][] board) {
		long max = Long.MIN_VALUE;
		for (int i=0;i<3;i++) {
			for (int j=0;j<3;j++) {
				if (points[i][j] > max) {
					max = points[i][j];
				}
			}
		}
		for (g_row=0;g_row<3;g_row++) {
			for (g_column=0;g_column<3;g_column++) {
				if (points[g_row][g_column] == max) {
					board[g_row][g_column] = SIGN_ROBOT;
					return;
				}
			}
		}
	}
	
	// show move on the LCD screen
	public void drawMove(int who) {
		if (who == MOVE_ROBOT) {
			LCD.drawString("computer move", 0, 3);
		} else if (who == MOVE_HUMAN) {
			LCD.drawString("human move   ", 0, 3);
		}
		LCD.drawString("row:       ", 0, 4);
		LCD.drawInt(g_row, 5, 4);
		LCD.drawString("column:       ", 0, 5);
		LCD.drawInt(g_row, 8, 5);
	}

	// find and execute the next move for the robot
	private void makeRobotMove() {
		Button.LEDPattern(4);
		resetPoints();		
		findRobotMove(board, SIGN_ROBOT, 1);	
		moveRobot(board);
		drawMove(MOVE_ROBOT);
		// column and row might be switched. not sure which one is x and y
		// put ball in the desired field
		myArm.putBall(g_row, g_column);
	}

	private boolean gameOver(int[][] board) {
		boolean end = undecided(board);
		if (!end) {
			end = won(board, SIGN_HUMAN); // human won?
		}
		if (!end) {
			end = won(board, SIGN_ROBOT); // computer won?
		}
		return end;
	}
	
	// wait for the human to make a move
	// move detected by the IR sensor measuring the hand 
	private void waitForHumanMove() {
		myHandSensor.detector.enableDetection(true);
		while (!handDetected && Button.ESCAPE.isUp()) {
			// wait
			Delay.msDelay(100);
		}
		handDetected=false;
		myHandSensor.detector.enableDetection(false);
	}
	
	// read the board with the camera and find the difference
	private boolean findHumanMove() {
		//System.out.println("objects: "+myCamera.cameraNXT.getNumberOfObjects());
		int[][] myBoard=myCamera.getBoardFields(SAMPLESIZE);
		for (int i=0;i<3;i++) {
			for (int j=0;j<3;j++) {
				if (board[i][j] == 0) {
					// I used the index for myBoard in the reversed i,j=>j,i
					if (myBoard[j][i]==COLOR_HUMAN) {
						board[i][j] = SIGN_HUMAN;
						g_row = i;
						g_column = j;
						return true;
					}
				}
			}
		}
		return false;
	}
	   
	   
	private void go () {
		LCD.drawString("go...             ", 0, 2);
		Delay.msDelay(100);
		try {
			makeRobotMove();
			while(!gameOver(board) && Button.ESCAPE.isUp()) {
				// blink yellow
				Button.LEDPattern(6);
				waitForHumanMove();
				//************* Here it does not work ******************
				test();
				//******************************************************
				if (!findHumanMove()) {
					Button.LEDPattern(5);
					Sound.buzz();
					LCD.drawString("human not moved.  ", 0, 4);
					LCD.drawString("                  ", 0, 5);
					Delay.msDelay(4000);
					break;
				}
				// human move.
				drawMove(MOVE_HUMAN);
				board[g_row][g_column] = SIGN_HUMAN; // human.
				if (gameOver(board)) {
					break;
				}
				makeRobotMove();
			};
			Button.LEDPattern(8);
			Sound.beepSequenceUp();
			LCD.drawString("the game is over!", 0, 3);

			if (won(board, 1)) {
				Sound.beepSequence();
				LCD.drawString("congratulation,   ", 0, 4);
				LCD.drawString("you have won.     ", 0, 5);
			} else if (won(board, 2)) {
				Sound.beepSequenceUp();
				LCD.drawString("I have won.       ", 0, 4);
				LCD.drawString("                  ", 0, 5);
			} else {
				Sound.twoBeeps();
				LCD.drawString("none has won.     ", 0, 4);
				LCD.drawString("                  ", 0, 5);
			}
			Delay.msDelay(4000);
			//closeEnvironmet

		} catch (Exception ex) {
			System.out.println("Error: "+ex);
		}
	}


	private void testArm() { 
		for (int i=0;i<3;i++) {
			for (int j=0;j<3;j++) {
				myArm.putBall(i, j);
				Sound.beep();
				LCD.drawInt(i, 0, 2);
			}
		}
	}
	
	private void test() {
		for (int i=1;i<5;i++) {
			Sound.beep();
			System.out.println(i+" ball");
			Delay.msDelay(3000);
			int[][] cameraTest = myCamera.getBoardFields(SAMPLESIZE);
		}
	}
	
	public static void main(String[] args) {
		TicTacToe ttt = new TicTacToe();
		//************* Here it does work **********************
		ttt.test();
		//******************************************************
		ttt.go();
	}
}
