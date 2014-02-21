import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.util.Arrays;

@SuppressWarnings("serial")
public class SimulatorMine2 extends JFrame
{	
  private JTextField xField;
  private JTextField yField;
  private JTextField zField;
  private JSlider xSlider;
  private JSlider ySlider;
  private JSlider zSlider;

  private TopPanel topPanel;
  private SidePanel sidePanel;
  
  private double x=0;	//	The figure below details h locations
  private double y=0;	//	    	|  Z  |  Z is to the rear
  private double z=0;	//		    Y  |  |
  private double hx=0;//		    | --- X
  private double hy=0;//		    |/   \|
  private double hz=0;//		    |\___/|                        
  
  //Physical dimensions
  private double diagonal=215;//From carriage pivot to effector pivot
  private double railOffset=137;//From center of printer to center of carriage rail
  //private double effectorOffset=19.9;//Distance from where diagonal attaches to center printer
  //private double carriageOffset=19.5;//Distance from diagonal attaches to carriage to center of carriage rail	
 
  //Printable area user measured
  private double buildX=170;
  private double buildY=170;
  private double buildZ=200;//Possibly calculated?
  
  //Calculated dimensions
  private double buildXMid=buildX/2;
  private double buildYMid=buildY/2;
	
  private double perimeter=2*(railOffset*Math.cos(Math.toRadians(30)));
  private double diagonalSq=Math.pow(diagonal,2);  

	private double[] railX = {perimeter/2,-Math.sqrt(Math.pow(railOffset,2)-Math.pow(perimeter,2)/4)};
	private double[] railY = {-perimeter/2,-Math.sqrt(Math.pow(railOffset,2)-Math.pow(perimeter,2)/4)};
	private double[] railZ = {0,perimeter*Math.sqrt(3)/2-Math.sqrt(Math.pow(railOffset,2)-Math.pow(perimeter,2)/4)};
  private int timeStep=5;

  public SimulatorMine2()
  {
    ActionHandler ah=new ActionHandler();
    ChangeHandler ch=new ChangeHandler();
		JMenuBar jmb=new JMenuBar();
    setJMenuBar(jmb);
    JMenu fileMenu=new JMenu("File");
    jmb.add(fileMenu);
    JMenuItem fileOpenItem=new JMenuItem("Open...");
    fileOpenItem.addActionListener(ah);
    fileMenu.add(fileOpenItem);

    JPanel mainPanel=new JPanel();
    mainPanel.setLayout(new GridLayout(1,2));
    add(mainPanel);

    JPanel inputPanel=new JPanel();
    inputPanel.setLayout(new GridLayout(4,1));
    add(inputPanel,BorderLayout.NORTH);

    JPanel xPanel=new JPanel();
    inputPanel.add(xPanel);

    xField=new JTextField(5);
    xField.setText(""+x);
    xPanel.add(new JLabel("X:"));
    xPanel.add(xField);

    xSlider=new JSlider((int)-diagonal,(int)diagonal,0);
    xSlider.addChangeListener(ch);
    xPanel.add(xSlider);
 
    JPanel yPanel=new JPanel();
    inputPanel.add(yPanel);

    yField=new JTextField(5);
    yField.setText(""+y);
    yPanel.add(new JLabel("Y:"));
    yPanel.add(yField);
 
    ySlider=new JSlider((int)-diagonal,(int)diagonal,0);
    ySlider.addChangeListener(ch);
    yPanel.add(ySlider);
 
    JPanel zPanel=new JPanel();
    inputPanel.add(zPanel);

    zField=new JTextField(5);
    zField.setText(""+z);
    zPanel.add(new JLabel("Z:"));
    zPanel.add(zField);

    zSlider=new JSlider(-100,100,0);
    zSlider.addChangeListener(ch);
    zPanel.add(zSlider);
 
    topPanel=new TopPanel();
    mainPanel.add(topPanel);

    sidePanel=new SidePanel();
    mainPanel.add(sidePanel);

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setSize(800,600);
    setVisible(true);
  
    new Timer().start();
  }

	private class ActionHandler implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      JFileChooser jfc=new JFileChooser();
      jfc.setCurrentDirectory(new File("."));
      int result=jfc.showOpenDialog(SimulatorMine2.this);
      if(result==JFileChooser.CANCEL_OPTION) return;
      File f=jfc.getSelectedFile();
      if(!f.exists()) return;
      try
      {
        BufferedReader in=new BufferedReader(new FileReader(f));
        String line;
        ArrayList<String> vertexList=new ArrayList<String>();
        ArrayList<String> faceList=new ArrayList<String>();
        while((line=in.readLine())!=null)
        {
					if(line.startsWith("G1")) vertexList.add(line);
				}
				in.close();				
				System.out.println(vertexList);
				String[] line2 = new String[10];
				double[] point = new double[10];
				for(String item: vertexList){		
					line2=item.split(" ");
					if(line2[1].substring(0,1).equals("X")){
						point[0]=Double.parseDouble(line2[1].substring(1));
						if(line2[2].substring(0,1).equals("Y")){
							point[1]=Double.parseDouble(line2[2].substring(1));
							if(line2[3].substring(0,1).equals("Z")){
								point[2]=Double.parseDouble(line2[3].substring(1));
							}
						}
					}
					boolean paint=false;	
					if((int)point[0]!=0){
						xSlider.setValue((int)point[0]);
						paint=true;
					}
					if((int)point[1]!=0){
						ySlider.setValue((int)point[1]);
						paint=true;
					}
					if((int)point[2]!=0){
						zSlider.setValue((int)point[2]);
						paint=true;
					}
					if(paint==true){
						try {
							Thread.sleep(80);
						} 
						catch(InterruptedException ex) {
							Thread.currentThread().interrupt();
						}
						topPanel.paint(topPanel.getGraphics());
						sidePanel.paint(sidePanel.getGraphics());									
						paint=false;
					}											
				}
			}
			catch(IOException ioe)
      {
        System.out.println(ioe);
      }
    }
  }
  
  public double getBaseX(){
		return Math.sqrt(Math.pow(railX[0]-xSlider.getValue(),2) + Math.pow(railX[1]-ySlider.getValue(),2));
  }
  
  public double getBaseY(){
		return Math.sqrt(Math.pow(railY[0]-xSlider.getValue(),2) + Math.pow(railY[1]-ySlider.getValue(),2));
  }
  
  public double getBaseZ(){
		return Math.sqrt(Math.pow(railZ[0]-xSlider.getValue(),2) + Math.pow(railZ[1]-ySlider.getValue(),2));
  }

  public double getHX(){
		return Math.sqrt(this.diagonalSq - Math.pow(getBaseX(),2));
  }
  
  public double getHY(){
		return Math.sqrt(this.diagonalSq - Math.pow(getBaseY(),2));
  }
  
  public double getHZ(){
		return Math.sqrt(this.diagonalSq - Math.pow(getBaseZ(),2));
  }

  private class TopPanel extends JPanel
  {	
    public void paintComponent(Graphics g)
    {
      super.paintComponent(g);
      g.setColor(Color.black);
      int width=getWidth();
      int height=getHeight();
      if(width==0) return;
      int x1=(int)(width/2+width*x/buildX);
      int y1=(int)(height/2-height*y/buildY);
      g.fillOval(x1-5,y1-5,10,10);
      g.drawLine(x1,y1,50,height-100);
      g.drawLine(x1,y1,width-50,height-100);
      g.drawLine(x1,y1,width/2,50);
      g.drawRect(0,0,width-1,height-1);
    }
  }

  private class SidePanel extends JPanel
  {
    public void paintComponent(Graphics g)
    {
/*
		System.out.println("hx"+getHX());
		System.out.println("hy"+getHY());
		System.out.println("hz"+getHZ());
		
		System.out.println("basex"+getBaseX());
		System.out.println("basey"+getBaseY());
		System.out.println("basez"+getBaseZ());
		
		System.out.println("perimeter"+perimeter);

		System.out.println("railX"+Arrays.toString(railX));
		System.out.println("railY"+Arrays.toString(railY));
		System.out.println("railZ"+Arrays.toString(railZ));
*/
      super.paintComponent(g);
      int width=getWidth();
      int height=getHeight();
      if(width==0) return;
      int x1=(int)(width/2+width*x/buildX);
      int y1=(int)(height/2-height*z/buildY);
      
      //pillars
      g.setColor(Color.red);
      g.drawLine(50,0,50,height);
      g.drawLine(width/2,0,width/2,height);
      g.drawLine(width-50,0,width-50,height);
      
      //effector
      g.setColor(Color.black);
      g.fillOval(x1-5,y1-5,10,10);
      
      //rods
      g.drawLine(x1,y1,width-50,y1-(int)getHX());
      g.drawLine(x1,y1,50,y1-(int)getHY());
      g.drawLine(x1,y1,width/2,y1-(int)getHZ());
      g.drawRect(0,0,width-1,height-1);
      g.setColor(Color.gray);
      g.fillRect(width-55,y1-(int)getHX(),10,10);
      g.fillRect(45,y1-(int)getHY(),10,10);       
      g.fillRect(width/2-5,y1-(int)getHZ(),10,10);           
    }
  }

  private class ChangeHandler implements ChangeListener
  {
    public void stateChanged(ChangeEvent e)
    {
      if(e.getSource()==xSlider)
      {
        x=xSlider.getValue();
        xField.setText(""+x);
        topPanel.repaint();
        sidePanel.repaint();
      }
      else if(e.getSource()==ySlider)
      {
        y=ySlider.getValue();
        yField.setText(""+y);
        topPanel.repaint();
        sidePanel.repaint();
      }
      else if(e.getSource()==zSlider)
      {
        z=zSlider.getValue();
        zField.setText(""+z);
        topPanel.repaint();
        sidePanel.repaint();
      }
    }
  }

  private class Timer extends Thread
  {
    public void run()
    {
      long currTime;
      long prevTime;
      try
      {
        prevTime=System.currentTimeMillis();
        for(int i=0;i<10000;i++)
        {
          z=20*Math.sin(i/100f);
          x=20*Math.sin(i/70f);
          y=20*Math.sin(i/85f);
          topPanel.repaint();
          sidePanel.repaint();
          currTime=System.currentTimeMillis();
          int t=(int)(currTime-prevTime);
          if(t<=timeStep) sleep(timeStep-t);
          else System.out.println("lagging "+(t-timeStep)+"ms at interval "+i);
          prevTime+=timeStep;
        }
      }
      catch(InterruptedException ie)
      {
        System.out.println(ie);
      }
    }
  }

  public static void main(String[] args)
  {
    new SimulatorMine2();
  }
}
