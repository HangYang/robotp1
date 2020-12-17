package control;

import robot.Robot;

//Robot Assignment for Programming 1 s2 2020
//Adapted by Caspar and Ross from original Robot code written by Dr Charles Thevathayan
//Full Name: Hang Yang 
//Student Number: s3799719
public class RobotControl implements Control
{
	// we need to internally track where the arm is
	private int height = Control.INITIAL_HEIGHT;
	private int width = Control.INITIAL_WIDTH;
	private int depth = Control.INITIAL_DEPTH;
	
	// left and right side total height and a temporary variable to store stacks that
	// robot is picking
	private int stack1Height = 0;
	private int stack2Height = 0;
	private int stackHeight = 0;
	
	// left and right side stack current heights
	private int stack1Heights = 0;
	private int stack2Heights = 0;
	
	// decide which stack to drop
	private int stackTarget = Control.STACK1_COLUMN;
	
	// decide which bar arm2 should adjust
	private int barTarget = 1;
	
	// track direction
	private boolean leftToRight = true;
	
	private int[] barHeights;
	private int[] blockHeights;
	
	// separate blockHeights into two stack heights
	private int[] stackHeights1 = new int[Control.MAX_STACK_HEIGHT];
	private int[] stackHeights2 = new int[Control.MAX_STACK_HEIGHT];
	
	// new blockHeights depends on barHeights and blockHeights
	private int[] newBlockHeights;
	
	private Robot robot;

	// called by RobotImpl
	@Override
	public void control(Robot robot, int[] barHeightsDefault, int[] blockHeightsDefault)
	{
		this.robot = robot;

		// some hard coded init values you can change these for testing
		this.barHeights = new int[] { 0, 6, 1, 3, 0, 5, 1 };
		this.blockHeights = new int[] { 3, 3, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1, 2, 2 };
		
//		this.barHeights = new int[] { 0, 6, 1, 3, 0, 5, 1 };
//		this.blockHeights = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		
		// FOR SUBMISSION: uncomment following 2 lines
		//	this.barHeights = barHeightsDefault;
		//	this.blockHeights = blockHeightsDefault;

		// initialise the robot
		robot.init(this.barHeights, this.blockHeights, height, width, depth);

		// a simple private method to demonstrate how to control robot
		// note use of constant from Control interface
		// You should remove this method call once you start writing your code

		// ADD ASSIGNMENT PART A METHOD CALL(S) HERE 
		
        newBlockHeights = new int[blockHeights.length];
		
		// Puts the blockHeights array values into a new array
		for(int i = 0; i < blockHeights.length; i++) {
			newBlockHeights[i] = blockHeights[i];
		}
		
		// separate blockHeights into two stack heights
		// and determine the height of the destination stack
		splitBlocks();
		
		// loop
		while (stack1Height > 0 || stack2Height > 0 ) {
			
			// update which bar to pick
			barTarget = seekBarTarget(barTarget);
			
			// adjust arm1 and arm2 to direct bar
			adjustWidth(barTarget);
			
			// move arm3 to pick block
			findBlockTarget();
			
			adjustDepth(Control.INITIAL_DEPTH);
			
			// remove the block value from the stack heights
			cutStack();
			
			// determine which stack to drop the block
			if (stackTarget == Control.STACK1_COLUMN) {
				// drop to stack 1
				addToStack1();
				cutBlock();
				adjustDepth(Control.INITIAL_DEPTH);
				// change direction
				stackTarget = changeStackTarget(stackTarget);
			}
			else {
				// drop to stack 2
				addToStack2();
				cutBlock();
				adjustDepth(Control.INITIAL_DEPTH);
				// change direction
				stackTarget = changeStackTarget(stackTarget);
			}
		}
		// back to the initial height, initial width and initial depth
		reset();
		
	}

	// simple example method to help get you started
//	private void extendToWidth(int width)
//	{
//		while (this.width < width)
//		{
//			robot.extend();
//			this.width++;
//		}
//	}

	// WRITE THE REST OF YOUR METHODS HERE!
	
	private void adjustWidth(int width) {
		// extendTowidth
		while (this.width < width)
		{
			robot.extend();
			this.width++;
		}
		
		// contractToWidth
		while (this.width > width) {
			robot.contract();
			this.width--;
		}
	}
	
	private void adjustHeight(int height) {
		// raiseToHeight
		while (this.height < height) {
			robot.up();
			this.height++;
		}
		
		// downToHeight
		while (this.height > height) {
			robot.down();
			this.height--;
		}
	}
	
	private void adjustDepth(int depth) {
		// raiseToDepth
		while (this.depth > depth) {
			robot.raise();
			this.depth--;
		}
		
		// lowerToDepth
		while (this.depth < depth) {
			robot.lower();
			this.depth++;
		}
	}
	
	private void splitBlocks() {
		// separate blockHeights into two stack heights arrays
		for (int i = 0, j = 0; i < blockHeights.length; i += 2, j++) {
			stackHeights1[j] = blockHeights[i];
		}
		
		// determine the height of the destination stack 
		for (int i = 0; i < stackHeights1.length; i++) {
			stack1Height += stackHeights1[i];
		}
		
		for (int i = 1, j = 0; i < blockHeights.length; i += 2, j++) {
			stackHeights2[j] = blockHeights[i];
		}
		
		for (int i = 0; i < stackHeights2.length; i++) {
			stack2Height += stackHeights2[i];
		}
	}
	
	private void reset() {
		adjustDepth(Control.INITIAL_DEPTH);
		adjustHeight(Control.INITIAL_HEIGHT);
		adjustWidth(Control.INITIAL_WIDTH);
	}
	
	// change direction
	private int changeStackTarget(int stackTarget) {
		if (stackTarget == Control.STACK1_COLUMN) {
			return Control.STACK2_COLUMN;
		}
		else {
			return Control.STACK1_COLUMN;
		}
	}
	
	private int seekBarTarget(int barTarget) {
		int bar = 0;
		if (leftToRight == true) {
			if (barTarget < Control.LAST_BAR_COLUMN) {
				bar = ++barTarget;
			}
			else if (barTarget == Control.LAST_BAR_COLUMN) {
			    bar = barTarget;
			    this.leftToRight = false;
			}
		}
		else if (leftToRight == false) {
			if (barTarget > Control.FIRST_BAR_COLUMN) {
				bar = --barTarget;
			}
			else if (barTarget == Control.FIRST_BAR_COLUMN) {
			    bar = barTarget;
			    this.leftToRight = true;
			}
		}
		return bar;
	}
	
	// determine newBarHeights array depends on barHeights and blockHeights array
	private int[] initNewBar() {
		int[] newBarHeights = new int[Control.MAX_BARS];
		for (int i = 0; i < Control.MAX_BARS; i++) {
			newBarHeights[i] = newBlockHeights[i];
		}
		for (int i = 0; i < barHeights.length; i++) {
			newBarHeights[i] += barHeights[i];
		}
		int count = 0; // count when to change direction
		boolean directionRight = true;
		for (int i = newBlockHeights.length - Control.MAX_BARS, j = Control.MAX_BARS; i > 0; i--, j++) {
			newBarHeights[Control.LAST_BAR_COLUMN - 2 - count] += newBlockHeights[j];
			if (directionRight == false) {
				count--;
				if (count < Control.FIRST_BAR_COLUMN - 2) {
					count = Control.FIRST_BAR_COLUMN - 2;
					directionRight = true;
				}
				
			}
			else {
				count++;
				if (count > Control.LAST_BAR_COLUMN - 2) {
					count = Control.LAST_BAR_COLUMN - 2;
					directionRight = false;
				}
			}
		}
		return newBarHeights;
		
	}
	
	private int getBlock() {
		return initNewBar()[barTarget - 2];
	}
	
	private int getTargetBlockPos() {
		int targetPos = 0;
		int p = Control.LAST_BAR_COLUMN - 2;
		boolean directionRight = false;
		for (int i = 0; i < newBlockHeights.length; i++) {
			if (Control.LAST_BAR_COLUMN - 2 - p == barTarget - 2) {
				if (newBlockHeights[i] != 0) {
					targetPos = i;
				}
			}
			if (directionRight == true) {
				p++;
				if (p > Control.LAST_BAR_COLUMN - 2) {
					p = Control.LAST_BAR_COLUMN - 2;
					directionRight = false;
				}
			}
			else {
				p--;
				if (p < Control.FIRST_BAR_COLUMN - 2) {
					p = Control.FIRST_BAR_COLUMN - 2;
					directionRight = true;
				}
			}
		}
		return targetPos;
	}
	
	// adjustDepth when arm above the target bar
	private void findBlockTarget() {
		adjustDepth(this.height - 1 - getBlock());
		robot.pick();
	}
	
	// remove picked block from newBlockHeights array
	private void cutBlock() {
		newBlockHeights[getTargetBlockPos()] = 0;
	}
	
	private void addToStack1() {
		adjustWidth(Control.STACK1_COLUMN);
		adjustDepth(this.height - 1 - newBlockHeights[getTargetBlockPos()] - stack1Heights);
		robot.drop();
		stack1Heights += newBlockHeights[getTargetBlockPos()];
	}
	
	private void addToStack2() {
		adjustWidth(Control.STACK2_COLUMN);
		adjustDepth(this.height - 1 - newBlockHeights[getTargetBlockPos()] - stack2Heights);
		robot.drop();
		stack2Heights += newBlockHeights[getTargetBlockPos()];	
	}
	
	// remove picked block from stack1Height and stack2Height
	private void cutStack() {
		int targetPos = 0;
		if (stackTarget == Control.STACK1_COLUMN) {
			for (int i = 0; i < stackHeights1.length; i++) {
				if (stackHeights1[i] > 0) {
					targetPos = i;
					stackHeight = stackHeights1[i];
					break;
				}
			}
			stack1Height -= stackHeight;
			stackHeights1[targetPos] = 0;
		}
		else {
			for (int i = 0; i < stackHeights2.length; i++) {
				if (stackHeights2[i] > 0) {
					targetPos = i;
					stackHeight = stackHeights2[i];
					break;
				}
			}
			stack2Height -= stackHeight;
			stackHeights2[targetPos] = 0;
		}
	}
	
}
