package org.minifigure;

import java.io.File;
import java.util.Arrays;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.*;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.TouchAdapter;
import lejos.robotics.objectdetection.Feature;
import lejos.robotics.objectdetection.FeatureDetector;
import lejos.robotics.objectdetection.FeatureListener;
import lejos.utility.Delay;
import lejos.utility.TextMenu;

public class TicTacToe implements FeatureListener{
	private static long sum_points;
	private static long[][] points = new long[3][3];
	private int[][] board;
	private static final int SIGN_ROBOT = 2;
	private static final int SIGN_HUMAN = 1;
	private static final int MOVE_ROBOT = 1;
	private static final int MOVE_HUMAN = 2;
	private static final int COLOR_HUMAN = 1; // BLUE BALLS
	//private static final int COLOR_ROBOT = 2; // RED BALLS
	private int g_rowX;
	private int g_columnY;
	HandSensor myHandSensor = new HandSensor();
	Arm myArm = new Arm();
	Camera myCamera = new Camera();
	int myBoard[][]= new int[3][3];
	boolean handDetected=false;
	
	public TicTacToe() {
		// initialize the board.
		board = new int[3][3];
		reset();

		// connect the hand detector to this class
		myHandSensor.detector.addListener(this);
		myHandSensor.detector.enableDetection(false);
		// calibrate the board or read in saved calibration file
		calibration();
		myCamera.start();
	}
	
	private void reset () {
		for (int[] row: board) {
		    Arrays.fill(row, 0);
		}
	}
	
	public void featureDetected(Feature feature, FeatureDetector detector) {
		handDetected=true;
		//System.out.println("HAND!");
		Sound.beep();
	}
	
	private void calibration () {
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

		LCD.clear();
		LCD.refresh();
		
		// shortcut so that I do not have to go through the menu
		//myCamera.calibrateBoard();
		//myCamera.readBoardCalibration();
	}

	private void resetPoints() {
    		sum_points = 0;
    		for (int x=0;x<3;x++) {
    			for (int y=0;y<3;y++) {
    				points[x][y] = Long.MIN_VALUE;
    			}
    		}
	}

	private boolean won(int[][] board, int player) {
		// are there rows of three marks?
		int count = 0;
		for (int x=0;x<3;x++) {
			for (int y=0;y<3;y++) {
				if (board[x][y] == player) {
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
		for (int x=0;x<3;x++) {
			for (int y=0;y<3;y++) {
				if (board[x][y] == player) {
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
		for (int x=0;x<3;x++) {
			for (int y=0;y<3;y++) {
				if (board[x][y] == 0) {
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
		for (int x=0;x<3;x++) {
			for (int y=0;y<3;y++) {
				if (board[x][y] == 0) {
					board[x][y] = move;
					if (move == 1) {
						findRobotMove(board, SIGN_ROBOT, level+1);
						if (level == 1) {
							points[x][y] = sum_points;
							sum_points = 0;
						}
					} else if (move == 2) {
						findRobotMove(board, SIGN_HUMAN, level+1);
						if (level == 1) {
							points[x][y] = sum_points;
							sum_points = 0;
						}
					}
					board[x][y] = 0;
				}
			}
		}
	}
    
	// safe the robot's move in the board array
	private void moveRobot(int[][] board) {
		long max = Long.MIN_VALUE;
		for (int x=0;x<3;x++) {
			for (int y=0;y<3;y++) {
				if (points[x][y] > max) {
					max = points[x][y];
				}
			}
		}
		for (g_rowX=0;g_rowX<3;g_rowX++) {
			for (g_columnY=0;g_columnY<3;g_columnY++) {
				if (points[g_rowX][g_columnY] == max) {
					board[g_rowX][g_columnY] = SIGN_ROBOT;
					return;
				}
			}
		}
	}
	
	// show move on the LCD screen
	private void drawMove(int who) {
		if (who == MOVE_ROBOT) {
			LCD.drawString("computer move", 0, 3);
			//System.out.println("computer move row:"+g_rowX+" column:"+g_columnY);
		} else if (who == MOVE_HUMAN) {
			LCD.drawString("human move   ", 0, 3);
			//System.out.println("human move row:"+g_rowX+" column:"+g_columnY);
		}
		LCD.drawString("row:       ", 0, 4);
		LCD.drawInt(g_rowX, 5, 4);
		LCD.drawString("column:       ", 0, 5);
		LCD.drawInt(g_columnY, 8, 5);
	}

	// find and execute the next move for the robot
	private boolean makeRobotMove() {
		Button.LEDPattern(4);
		resetPoints();		
		findRobotMove(board, SIGN_ROBOT, 1);	
		moveRobot(board);
		drawMove(MOVE_ROBOT);
		// put ball in the desired field
		myArm.putBall(g_rowX, g_columnY);
		//System.out.println("Computer Move(2): x="+g_rowX+" y="+g_columnY);
		//printMatrix();
		int[][] fieldCheck=myCamera.getBoardFields();
		if (fieldCheck[g_rowX][g_columnY]!=2) {
			return false;
		}
		return true;
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
			Delay.msDelay(100);
		}
		handDetected=false;
		myHandSensor.detector.enableDetection(false);
		Delay.msDelay(3000);
	}
	
	// read the board with the camera and find the difference
	private boolean findHumanMove() {
		//System.out.println("objects: "+myCamera.cameraNXT.getNumberOfObjects());

		int[][] myBoard=myCamera.getBoardFields();
		
		/*
		for (int x=0;x<3;x++) {
			for (int y=0;y<3;y++) {
				System.out.println("b: "+board[x][y]+" c: "+myBoard[x][y]);
			}
		}
		*/

		for (int x=0;x<3;x++) {
			for (int y=0;y<3;y++) {
				//System.out.println("b: "+board[x][y]+" c: "+myBoard[x][y]);
				if (board[x][y] == 0) {
					if (myBoard[x][y]==COLOR_HUMAN) {
						board[x][y] = SIGN_HUMAN;
						//System.out.println("Human Move(1): x="+x+" y="+y);
						g_rowX = x;
						g_columnY = y;
						return true;
					}
				}
			}
		}
		return false;
	}
	   
	   
	private void go () {
		EV3TouchSensor touchSensor = new EV3TouchSensor(SensorPort.S2);
		TouchAdapter touch = new TouchAdapter(touchSensor);
		boolean moveWorked=true;
		LCD.clear();
		LCD.refresh();
		LCD.drawString("press start...             ", 0, 2);
		try {
			File iStart = new File ("iStart.wav");
			File myTurn = new File ("myTurn.wav");
			File yourTurn = new File ("yourTurn.wav");
			//File youCheated = new File ("youCheated.wav");

			while (Button.ESCAPE.isUp()) {
				LCD.drawString("press start...             ", 0, 2);
				// wait for the start button press
				while (!touch.isPressed() && Button.ESCAPE.isUp()) {
					Delay.msDelay(100);
				}
				LCD.clear();
				LCD.refresh();
				if (Button.ESCAPE.isUp()) {
					Sound.playSample(iStart, 100);
					moveWorked = makeRobotMove();
				};
				
				while(!gameOver(board) && Button.ESCAPE.isUp()) {
					// counter++;
					// blink yellow
					Button.LEDPattern(6);
					Sound.playSample(yourTurn, 100);
					waitForHumanMove();
					if (!findHumanMove()) {
						Button.LEDPattern(5);
						Sound.buzz();
						LCD.drawString("human not moved.  ", 0, 4);
						LCD.drawString("                  ", 0, 5);
						Delay.msDelay(2000);
						break;
					}
					/*
					 * TODO: respond to incorrect ball drop
					else if (!moveWorked) {
						Button.LEDPattern(5);
						Sound.buzz();
						LCD.clear();
						LCD.refresh();
						printMatrix("board",board,8,0);
						printMatrix("camera", myCamera.getBoardFields(),8,5);
						while (Button.ESCAPE.isUp()) {Delay.msDelay(100);}
						LCD.drawString("I failed move.  ", 0, 4);
						LCD.drawString("                  ", 0, 5);
						Delay.msDelay(2000);
						break;
					}
					*/
					// human move.
					drawMove(MOVE_HUMAN);
					board[g_rowX][g_columnY] = SIGN_HUMAN; // human.
					if (gameOver(board)) {
						break;
					}
					Sound.playSample(myTurn, 100);
					moveWorked=makeRobotMove();
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
				
				//LCD.drawString("ending game   ", 0, 4);
				Delay.msDelay(5000);
				// reset the board
				reset();
				myCamera.reset();
				resetPoints();
				LCD.clear();
				LCD.refresh();

			}
		} catch (Exception ex) {
			System.out.println("Error: "+ex);
		}
		//closeEnvironmet
		myCamera.stopped=true;
	}

	/*
	private void testArm() { 
		for (int x=0;x<3;x++) {
			for (int y=0;y<3;y++) {
				System.out.println(x+" "+y);
				myArm.putBall(x, y);
				Sound.beep();
				LCD.drawInt(x, 0, 2);
			}
		}
	}
	*/
	
	/*
	private void testCamera() {
		int[][] cameraTest = new int [3][3];
		String line="";
		for (int i=1;i<4;i++) {
			line="";
			//Sound.beep();
			System.out.println(i+" ball");
			Delay.msDelay(3000);
			cameraTest = myCamera.getBoardFields();
			for (int x=0;x<3;x++) {
				for (int y=0;y<3;y++) {
					line=line+","+cameraTest[x][y];
				}
			}
			System.out.println(line);
		}
	}
	*/
	
	private void printMatrix(String name, int[][] receivedBoard,int horizontal, int vertical) {
		String line="";
		LCD.drawString(name+": ",5,vertical);
		for (int x=2;x>-1;x--) {
			for (int y=0;y<3;y++) {
				line=line+receivedBoard[x][y];
			}
			LCD.drawString(line, horizontal, vertical-x);
			//System.out.println(line);z
			line="";
		}
	}
	
	/*
	private void printMatrix() {
		String line="";
		int[][] matrix = myCamera.getBoardFields();
		System.out.println("Camera: ");
		for (int x=2;x>-1;x--) {
			for (int y=0;y<3;y++) {
				line=line+matrix[x][y];
			}
			System.out.println(line);
			line="";
		}
		matrix = board;
		System.out.println("Board: ");
		for (int x=2;x>-1;x--) {
			for (int y=0;y<3;y++) {
				line=line+matrix[x][y];
			}
			System.out.println(line);
			line="";
		}
	}
	*/
	
	/*
	private void testMatrix(){
		System.out.println(Arrays.deepToString(myCamera.getBoardFields()));
		myArm.putBall(0, 0);
		System.out.println(Arrays.deepToString(myCamera.getBoardFields()));
		myArm.putBall(1, 0);
		System.out.println(Arrays.deepToString(myCamera.getBoardFields()));
	}
	*/
	
	public static void main(String[] args) {
		TicTacToe ttt = new TicTacToe();
		// methods to test the functionality of the robot
		// Uncomment as needed
		//ttt.testArm();
		//ttt.testCamera();
		//ttt.testMatrix();
		ttt.go();
	}
}
