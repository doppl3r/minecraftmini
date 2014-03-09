import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class MainClass extends Applet implements Runnable, MouseMotionListener, MouseListener, KeyListener
{
	private static final long serialVersionUID = 1L;
	
	//Double Buffering
	Image dbImage;
	Graphics dbg;
	//map info
	int DELAY = 15;
	int rows = 64;
	int cols = 64;
	int blockSize = 16;
	int map[][] = new int[rows][cols];
	boolean buildMap;
	boolean pause = true;
	//player info
	int playerX = (cols*blockSize)/2;
	int playerY = 0;
	int maxMoveSpeed = 5;
	int moveSpeed = maxMoveSpeed;
	boolean up, right, down, left, buildLeft, buildRight;
	final int range = 8;
	
	
	//automatically initiated
	public void init() {
		this.addKeyListener(this);
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.setSize(265,265);
	}
	public void start() {		
		Thread th = new Thread(this);
		th.start();
	}
	public void run() {
		//build a random map
		while (true){
			if (!buildMap){
				int height = rows/2;
				//clean map
				for (int row = 0; row < rows; row++){
					for (int col = 0; col < cols; col++){
						map[row][col]=0;
					}
				}
				for (int col = 0; col < cols; col++){
					//add grass
					map[height][col] = 2;
					//add dirt
					for (int tempHeight = height+1; tempHeight < height+4; tempHeight++){
						map[tempHeight][col] = 3;
					}
					//add stone
					for (int tempHeight = height+4; tempHeight < rows; tempHeight++){
						map[tempHeight][col] = 1;
						if (tempHeight == rows-1) map[tempHeight][col] = 4;
					}
					height += (int)(Math.random()*3)-1; //random height
				}
				//add diamond
				for (int row = 0; row < rows; row++){
					for (int col = 0; col < cols; col++){
						if (map[row][col]==1){
							if ((int)(Math.random()*100)==0){
								map[row][col]=6;
							}
						}
					}
				}
				//repair grass
				for (int row = 1; row < rows - 1; row++){
					for (int col = 1; col < cols - 1; col++){
						if (map[row][col]==2 && map[row][col-1]==0 && map[row][col+1]==0){
							map[row][col]=0;
							map[row+1][col]=2;
							
						}
						else if (map[row][col]==0 && map[row][col-1]==2 && map[row][col+1]==2){
							map[row][col]=2;
							map[row+1][col]=3;
						}
					}
				}
				//spawn player
				playerY=0;
				while (map[(playerY/blockSize)+1][playerX/blockSize]==0) playerY+=blockSize;
				buildMap = true;
			}
			else{
				if (!pause){
					//check player
					if (moveSpeed > 0) moveSpeed--;
					else{
						if (map[(playerY/blockSize)+1][playerX/blockSize] == 0){
							//check gravity
							playerY+=blockSize;
							moveSpeed = maxMoveSpeed/2;
						}
						else{
							if (left){
								if (playerX > 0){
									moveSpeed = maxMoveSpeed;
									playerX-=blockSize;
									if (map[(playerY/blockSize)][(playerX/blockSize)]>0 &&
											map[(playerY/blockSize)-1][(playerX/blockSize)]==0 &&
											map[(playerY/blockSize)-1][(playerX/blockSize)+1]==0){
										playerY-=blockSize;
									}
									else map[(playerY/blockSize)][playerX/blockSize] = 0;
								}
							}
							else if (right){
								if (playerX < (cols-1)*blockSize){
									moveSpeed = maxMoveSpeed;
									playerX+=blockSize;
									if (map[(playerY/blockSize)][(playerX/blockSize)]>0 &&
											map[(playerY/blockSize)-1][(playerX/blockSize)]==0 &&
											map[(playerY/blockSize)-1][(playerX/blockSize)-1]==0){
										playerY-=blockSize;
									}
									else map[(playerY/blockSize)][playerX/blockSize] = 0;
								}
							}
							else if (up){
								if (playerY > 0){
									moveSpeed = maxMoveSpeed;
									playerY-=blockSize;
									if (map[(playerY/blockSize)][playerX/blockSize] == 0)
									map[(playerY/blockSize)+1][playerX/blockSize] = 5;
									else{
										map[(playerY/blockSize)][playerX/blockSize] = 0;
										map[(playerY/blockSize)+1][playerX/blockSize] = 5;
									}
								}
							}
							else if (down){
								if (playerY < (rows-2)*blockSize){
									moveSpeed = maxMoveSpeed;
									playerY+=blockSize;
									map[(playerY/blockSize)][playerX/blockSize] = 0;
								}
							}
						}
						//build sideways
						if (playerX > 0 && playerX < (cols-1)*blockSize &&
								playerY > 0 && playerY < (rows-2)*blockSize){
							if (map[(playerY/blockSize)+1][(playerX/blockSize)]!=0){
								if (buildLeft){
									if (map[(playerY/blockSize)][(playerX/blockSize)-1]==0){
										if (map[(playerY/blockSize)+1][(playerX/blockSize)-1]==0)
										map[(playerY/blockSize)+1][(playerX/blockSize)-1] = 5;
										else map[(playerY/blockSize)+1][(playerX/blockSize)-1] = 0;
									}
									buildLeft = false;
								}
								if (buildRight){
									if (map[(playerY/blockSize)][(playerX/blockSize)+1]==0){
										if (map[(playerY/blockSize)+1][(playerX/blockSize)+1]==0)
										map[(playerY/blockSize)+1][(playerX/blockSize)+1] = 5;
										else map[(playerY/blockSize)+1][(playerX/blockSize)+1] = 0;
										buildRight = false;
									}
								}
							}
						}
						buildRight = buildLeft = false;
					}
				}
			}
			//update the graphics
			repaint();
			try { Thread.sleep(DELAY); } 
			catch (InterruptedException ex){}
		}
	}
	//key input
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		//move the player
		if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) left = true;
		else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) right = true;
		if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) up = true;
		else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) down = true;
	}
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		//make the player stop moving
		if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) left = false;
		else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) right = false;
		if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) up = false;
		else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) down = false;
		//build blocks horizontally
		if (key == KeyEvent.VK_Q) buildLeft = true;
		if (key == KeyEvent.VK_E) buildRight = true;
		//pause the game
		if (key == KeyEvent.VK_ESCAPE) buildMap = false;
	}
	public void keyTyped(KeyEvent e) {}
	public void paint(Graphics g) {
		//background
		g.setColor(Color.white);
		g.fillRect(0, 0, 264, 264);
		//draw level
		int type;
		g.setColor(Color.green);
		for (int row = 0; row < 17; row++){
			for (int col = 0; col < 17; col++){
				//only draw blocks that have a value greater than zero
				if (col+(playerX/blockSize)-range >= 0 && col+(playerX/blockSize)-range < cols &&
						row+(playerY/blockSize)-range >= 0 && row+(playerY/blockSize)-range < rows){
					type = map[row+(playerY/blockSize)-range][col+(playerX/blockSize)-range];
					switch (type) {
						case(1): g.setColor(Color.lightGray); break;
						case(2): g.setColor(Color.green); break;
						case(3): g.setColor(Color.orange); break;
						case(4): g.setColor(Color.darkGray); break;
						case(5): g.setColor(Color.gray); break;
						case(6): g.setColor(Color.cyan); break;
					}
					if (type > 0){
						g.fillRect(col*blockSize-blockSize/2, row*blockSize-blockSize/2, blockSize, blockSize);
						g.setColor(Color.gray); //grid color
						g.drawRect(col*blockSize-blockSize/2, row*blockSize-blockSize/2, blockSize, blockSize);
					}
				}
			}
		}
		//draw player
		g.setColor(Color.darkGray);
		g.fillRect((8*blockSize)-(blockSize/2)+3, (8*blockSize)-(blockSize/2)+5, blockSize-5, blockSize-5);
		//instructions
		g.setColor(Color.darkGray);
		if (pause){
			g.drawString("Controls: arrows to move around", 4, 12);
			g.drawString("'Q' or 'R' to build horizontal blocks", 4, 32);
			g.drawString("[Click to continue!]", 4, 52);
		}
		else{
			//debug
			g.drawString("("+playerX+","+playerY+")", 4, 12);
		}
	}
	public void update(Graphics g) //double buffering
	{
		if (dbImage == null){
			dbImage = createImage (this.getSize().width, this.getSize().height);
			dbg = dbImage.getGraphics ();
		}		
		dbg.setColor (getBackground ());
		dbg.fillRect (0, 0, this.getSize().width, this.getSize().height);
		dbg.setColor (getForeground());
		paint (dbg);		
		g.drawImage (dbImage, 0, 0, this);
		//hide cursor
		if (!pause){
			BufferedImage cursorImg = new BufferedImage(16,16, BufferedImage.TYPE_INT_ARGB);
			Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
					cursorImg, new Point(0,0), "blank cursor");
			setCursor(blankCursor);
		}
		//else setCursor(null);
		Toolkit.getDefaultToolkit().sync();
		g.dispose();
	}
	//mouse input
	public void mouseDragged(MouseEvent arg0) {}
	public void mouseMoved(MouseEvent arg0) {}
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) { pause = false; }
	public void mouseReleased(MouseEvent arg0) {}
}
