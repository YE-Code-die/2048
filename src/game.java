import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;
import java.awt.image.BufferedImage;
public class game extends JPanel{
	enum State{
		start,won,running,over;
	}
	final Color[] colorTable= {
			new Color(0x701710),new Color(0xFFE4C3),new Color(0xfff4d3),new Color(0xffdac3),new Color(0xe7b08e),new Color(0xe7bf8e),
			new Color(0xffc4c3),new Color(0xE7948e),new Color(0xbe7e56),new Color(0xbe5e56),new Color(0x9c3931),new Color(0x701710)
	};
	final static int target=2048;
	static int highest;
	static int score;
	private Color gridColor =new Color(0xBBADA0);
	private Color emptyColor =new Color(0xCDC1B4);
	private Color startColor =new Color(0xFFEBCD);
	private Random random=new Random();
	private Tile[][] tile;
	private int side =4;
	private State gamestate=State.start;
	private boolean checkingAvailableMoves;
	public game() {
		setPreferredSize(new Dimension(900,700));
		setBackground(new Color(0xFAF8EF));
		setFont(new Font("SansSerif",Font.BOLD,48));
		setFocusable(true);
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {//点击鼠标开始游戏
				startGame();
				repaint();
			}
		});
		addKeyListener(new KeyAdapter() {//键盘操作
			
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
					case KeyEvent.VK_UP:
						MoveUp();
						break;
					case KeyEvent.VK_DOWN:
						MoveDown();
						break;
					case KeyEvent.VK_LEFT:
						MoveLeft();
						break;
					case KeyEvent.VK_RIGHT:
						MoveRight();
						break;
				}
				repaint();
			}
			
		});
	}
	public void paintComponent(Graphics gg) {
		super.paintComponent(gg);
		Graphics2D g=(Graphics2D) gg;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		drawGrid(g);
	}
	private void drawGrid(Graphics2D g) {
		// TODO Auto-generated method stub
		g.setColor(gridColor);
		g.fillRoundRect(200, 100, 499, 499, 15, 15);//中间的游戏区域
		if(gamestate==State.running) {//游戏运行页面
			for(int r=0;r<side;r++) {
				for(int c=0;c<side;c++) {
					if(tile[r][c]==null) {//空框
						g.setColor(emptyColor);
						g.fillRoundRect(215+c*121, 115+r*121, 106, 106, 7, 7);
					}else {
						drawTile(g,r,c);
					}
				}
			}
		}else {//开始界面
			g.setColor(startColor);
			g.fillRoundRect(215, 115, 469, 469, 7, 7);//开始界面的背景
			g.setColor(gridColor.darker());
			g.setFont(new Font("SansSerif",Font.BOLD,128));
			g.drawString("2048", 310, 270);
			g.setFont(new Font("SansSerif",Font.BOLD,20));
			if(gamestate==State.won) {
				g.drawString("成了", 390, 350);
			}else if(gamestate==State.over) {
				g.drawString("莫了", 390, 350);
			}
			g.setColor(gridColor);
			g.drawString("开始新游戏", 330, 470);
			g.drawString("(方向键移动数字块)",310	, 530);

		}
	}
	public static void main(String[] args) {
		SwingUtilities.invokeLater(()->{
			JFrame f=new JFrame();
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//关闭时结束游戏
			f.setTitle("2048");
			f.setResizable(true);//可改变窗口大小
			f.add(new game(),BorderLayout.CENTER);//加载游戏
			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		});
	}
	public void drawTile(Graphics2D g,int r,int c) {
		int value=tile[r][c].getValue();
		g.setColor(colorTable[(int)(Math.log(value)/Math.log(2))+1]);
		g.fillRoundRect(215+c*121, 115+r*121, 106, 106, 7, 7);
		String s=String.valueOf(value);
		g.setColor(value<128?colorTable[0]:colorTable[1]);
		FontMetrics fm=g.getFontMetrics();
		int asc=fm.getAscent();
		int dec=fm.getDescent();
		int x=215+c*121+(106-fm.stringWidth(s))/2;
		int y=115+r*121+(asc+(106-(asc+dec))/2);
		g.drawString(s,x,y);
	}
	public void startGame() {
		if(gamestate!=State.running) {//游戏未开始
			score =0;//得分
			highest=0;//记录
			gamestate=State.running;//游戏启动
			tile=new Tile[side][side];
			addRandomTile();
			addRandomTile();
		}
	}
	private void addRandomTile() {
		int pos=random.nextInt(side*side);
		int row,col;
		do {
			pos= (pos+1)%(side*side);
			row=pos/side;
			col=pos%side;
		}while(tile[row][col]!=null);
		int val=random.nextInt(10)==0?4:2;//随机生成4和2，2的概率高于4
		tile[row][col]=new Tile(val);
		
	}
	private boolean move(int countDownFrom,int y,int x) {
		boolean moved=false;
		for(int i=0;i<side*side;i++) {
			int j=Math.abs(countDownFrom-i);
			int r=j/side;
			int c=j%side;
			if(tile[r][c]==null) {
				continue;
			}
			int nextR=r+y;
			int nextC=c+x;
			while(nextR>=0&&nextR<side&&nextC>=0&&nextC<side) {
				Tile next=tile[nextR][nextC];
				Tile curr=tile[r][c];
				if(next==null) {//下个格子是空的
					if(checkingAvailableMoves) {
						return true;
					}
					tile[nextR][nextC]=curr;
					tile[r][c]=null;
					r=nextR;
					c=nextC;
					nextR+=y;
					nextC+=x;
					moved=true;
				}else if(next.canMergeWith(curr)) {//下个格子可以合并
					if(checkingAvailableMoves) {
						return true;
					}
					int value=next.mergeWith(curr);
					if(value>highest) {//更换最大值
						highest=value;
					}
					score+=value;
					tile[r][c]=null;
					moved=true;
					break;
				}else {//不能移动
					break;
				}
			}
		
		}
		if(moved) {//如果可以移动
			if(highest<target) {
				clearMerged();
				addRandomTile();//出一个随机的新格子
				if(!movesAvailable()) {//不能移动，格子全满，游戏结束
					gamestate=State.over;
				}
			}else if(highest==target) {
				gamestate=State.won;//有2048，游戏胜利
			}
		}
		return moved;
		
	}
	private void clearMerged() {
		// TODO Auto-generated method stub
		for(Tile[] row:tile) {
			for(Tile col:row) {
				if(col!=null) {
					col.setMerged(false);
				}
			}
		}
	}
	private boolean movesAvailable() {
		// TODO Auto-generated method stub
		checkingAvailableMoves=true;
		boolean hasMoves=MoveUp()||MoveDown()||MoveLeft()||MoveRight();
		checkingAvailableMoves=false;
		return hasMoves;
	}
	private boolean MoveRight() {
		// TODO Auto-generated method stub
		return move(0,0,1);
	}
	private boolean MoveLeft() {
		// TODO Auto-generated method stub
		return move(0,0,-1);
	}
	private boolean MoveDown() {
		// TODO Auto-generated method stub
		return move(0,1,0);
	}
	private boolean MoveUp() {
		// TODO Auto-generated method stub
		return move(0,-1,0);
	}
	
}
