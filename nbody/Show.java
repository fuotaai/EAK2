import javax.swing.*;
import java.awt.*;
public class Show extends JPanel implements Runnable {

	private volatile Body[] bodies;

	Show( Body[] bodies ){ this.bodies = bodies; }

	private double scale = -1.0; // one pixel corresponds to this distance
	                             // -1 means uninitialized
	void calibrate(){
		double maxX = 0.0, maxY = 0.0;
		for( Body body: bodies ){
			maxX = Math.max(maxX, Math.abs(body.position.x));
			maxY = Math.max(maxY, Math.abs(body.position.y));
		}
		scale = Math.max(maxX/getWidth(), maxY/getHeight()) / 0.45;
	}

	void drawDisc( Graphics g, int xCenter, int yCenter, int radius ){
		g.fillOval(xCenter-radius,yCenter-radius,2*radius,2*radius);
	}

	void drawBody( Graphics g, Body body ){
		drawDisc( g, 
			  (int)(body.position.x/scale) + getWidth()/2,
			  (int)(body.position.y/scale) + getHeight()/2,
			  3 );
	}

	@Override public void paintComponent(Graphics g){
		if( scale < 0 ) calibrate();
		g.clearRect(0,0,getWidth(),getHeight());
		for( Body body: bodies ){
			drawBody(g,body);
		}
	}

	@Override public void run(){
		while( true ){
			bodies = NBody.simulate(bodies,0.000001,100);
			repaint();  // this will call paintComponent
		}
	}

	public static void main( String[] args ){
		JFrame frame = new JFrame("n-body simulation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Show show = new Show(NBody.solarSystem);
		frame.setSize(1000,700);
		frame.getContentPane().add(show);
		frame.setVisible(true);
		new Thread(show).start();
	}
}
