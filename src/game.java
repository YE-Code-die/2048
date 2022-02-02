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
			public void mousePressed(MouseEvent e) {//�����꿪ʼ��Ϸ
				startGame();
				repaint();
			}
		});
		addKeyListener(new KeyAdapter() {//���̲���
			
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
		g.fillRoundRect(200, 100, 499, 499, 15, 15);//�м����Ϸ����
		if(gamestate==State.running) {//��Ϸ����ҳ��
			for(int r=0;r<side;r++) {
				for(int c=0;c<side;c++) {
					if(tile[r][c]==null) {//�տ�
						g.setColor(emptyColor);
						g.fillRoundRect(215+c*121, 115+r*121, 106, 106, 7, 7);
					}else {
						drawTile(g,r,c);
					}
				}
			}
		}else {//��ʼ����
			g.setColor(startColor);
			g.fillRoundRect(215, 115, 469, 469, 7, 7);//��ʼ����ı���
			g.setColor(gridColor.darker());
			g.setFont(new Font("SansSerif",Font.BOLD,128));
			g.drawString("2048", 310, 270);
			g.setFont(new Font("SansSerif",Font.BOLD,20));
			if(gamestate==State.won) {
				g.drawString("����", 390, 350);
			}else if(gamestate==State.over) {
				g.drawString("Ī��", 390, 350);
			}
			g.setColor(gridColor);
			g.drawString("��ʼ����Ϸ", 330, 470);
			g.drawString("(������ƶ����ֿ�)",310	, 530);

		}
	}
	public static void main(String[] args) {
		SwingUtilities.invokeLater(()->{
			JFrame f=new JFrame();
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//�ر�ʱ������Ϸ
			f.setTitle("2048");
			f.setResizable(true);//�ɸı䴰�ڴ�С
			f.add(new game(),BorderLayout.CENTER);//������Ϸ
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
		if(gamestate!=State.running) {//��Ϸδ��ʼ
			score =0;//�÷�
			highest=0;//��¼
			gamestate=State.running;//��Ϸ����
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
		int val=random.nextInt(10)==0?4:2;//�������4��2��2�ĸ��ʸ���4
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
				if(next==null) {//�¸������ǿյ�
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
				}else if(next.canMergeWith(curr)) {//�¸����ӿ��Ժϲ�
					if(checkingAvailableMoves) {
						return true;
					}
					int value=next.mergeWith(curr);
					if(value>highest) {//�������ֵ
						highest=value;
					}
					score+=value;
					tile[r][c]=null;
					moved=true;
					break;
				}else {//�����ƶ�
					break;
				}
			}
		
		}
		if(moved) {//��������ƶ�
			if(highest<target) {
				clearMerged();
				addRandomTile();//��һ��������¸���
				if(!movesAvailable()) {//�����ƶ�������ȫ������Ϸ����
					gamestate=State.over;
				}
			}else if(highest==target) {
				gamestate=State.won;//��2048����Ϸʤ��
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
