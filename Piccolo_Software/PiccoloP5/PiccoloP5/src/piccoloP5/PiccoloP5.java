/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package piccoloP5;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.sun.tools.javac.util.Convert;

import processing.core.*;
import processing.serial.*;

/**
 * This is a template class and can be used to start a new processing library or tool.
 * Make sure you rename this class as well as the name of the example package 'template' 
 * to your own library or tool naming convention.
 * 
 * @example Controllo 
 * 
 * (the tag @example followed by the name of an example included in folder 'examples' will
 * automatically include the example in the javadoc.)
 *
 */

public class PiccoloP5 extends PGraphics {
	static final char COMMAND_CONNECT = 		'C'; 	//connect byte						from computer		
	static final char COMMAND_SEND_NEXT = 		'B'; 	//send the next packet 				from piccolo
 	static final char COMMAND_READY =  			'A'; 	//piccolo ready to plot!			from piccolo
	static final char COMMAND_POS_START_BYTE = 	'P'; 	//start of position					from computer
	static final char COMMAND_POS_END_BYTE	= 	';';	//end of position					from computer
	static final char COMMAND_END_STACK = 		'E';	//finished sending current stack. 	from computer

	int CHAR_PER_POS = 8;
	  
	  
	  //Bed Size in mm
	  public float DEFAULT_BED_WIDTH = 50;
	  public float DEFAULT_BED_HEIGHT = 50;
	  public float DEFAULT_BED_DEPTH = 50;
	 
	  public float bedWidth = DEFAULT_BED_WIDTH;
	  public float bedHeight = DEFAULT_BED_WIDTH;
	  public float bedDepth= DEFAULT_BED_WIDTH;

	  
	  //Piccolo send coordinate settings, all false by default
	  //TODO: These should be reflected in GUI
	  public boolean flipX = false; 
	  public boolean flipY = false;
	  public boolean flipZ = false;
	  public boolean rotateBed = false;

	  
	  public boolean startButtonPressed = false;


	//debug output all serial communications to the console for debugging. 
	boolean debug = true; 
	public boolean serialConnected = false;

	//TODO: scale render not output. 
	//TODO: resize SVG to fit bed.
	// scale pixels to Piccolo coordinates
	float scaleOutput = 0.16666666666667f; 




	//the height to lift the pen between 2d shapes
	static final float PEN_LIFT_DELTA = 7.0f;
	private float penDownHeight = 0; // change using slider
	private float penLiftHeight = penDownHeight+PEN_LIFT_DELTA; //change using slider



	//Matrix variables  	
	  int transformCount = 0;
	  PMatrix3D transformStack[] = new PMatrix3D[12];
	  PMatrix3D matrix = new PMatrix3D();

	  
	  boolean sendPenHeight = true;
	  
	  boolean translateCenter = false; //if true all coordinates are translated by half the bed height and width to match Piccolo's native coordinate system. 
	  PVector beginShapePos = null;
	  boolean closeShape = false;
	  boolean penUp = true;

	  PGraphics screenCanvas; //Canvas used for buffering drawing steps to screen

	  //should we flattern curves?
	  boolean flattern = true;
	  float flatRes = 1.0f;// steps per mm around curves 

	  //Previous Vector
	  PVector pVertex = new PVector(0, 0, 0);

	  //Simulation stuff
	  int simStep = 0;
	  PVector simPrev = null;
	  List codeStack = new ArrayList(); 

	  //Drawing to screen 
	  float screenCanvasWidth =  300;
	  float screenCanvasHeight =  300;
	  float screenCanvasDepth =  300;


	  float bedDrawHeight = -(bedDepth/2.0f) ; // Piccolo draws with x, y and z starting in the center of the draw area. 
	  
	  float scale = 1.0f;
	  float translateX = 0.0f;
	  float translateY = 0.0f;
	  float translateZ = 0.0f;


	/*Serial Commands
	*/
	public Serial serial;

	boolean firstContact = false;
	boolean stepDelaySet = false;
	boolean pressureSet = false;

	int inByte;
	String sendString;  //xxxxxyyyyyzzzzz;
	int sendStringIndex = 0;

	String code[];

	/*HTTP
	 * Send Piccolo commands across a http connection
	 * */
	private String serverAddress;
	private boolean useHttpConnection = false;

	int zDraw = 0;
	int zLift = 10;

	int lineCount = 0;
	int numLines;



	boolean debugVertex = true; //write all vertex commands to console
	boolean debugStep = true; // write all step commands to console
	boolean debugDraw = false; //write all drawing commands to the console 


	

public PiccoloP5(){
	bezierDetail(50);
}

public PiccoloP5(float _w, float _h, float _d){
	bezierDetail(50);
}



/* ============================== getters and setters =============================== */

public float getPenDownHeight(){return penDownHeight;}
public float getPenLiftHeight(){return penLiftHeight;}



/* ============================== draw functions =============================== */


@Override
public void ellipse(float x, float y, float width, float height){
    if(width == 0 || height == 0)
        return;
  
    float arcStep = (flatRes/(width*PI));
    beginShape();
    arc(x, y, width, height, 0, TWO_PI+arcStep);
    endShape();
}

@Override
public void arc(float x , float y , float width, float height, float startA, float stopA){
    float arcStep = (flatRes/(width*PI));

    startA -= PI/2.0f; // correct to match processing
    stopA -= PI/2.0f; // correct to match processing
    
    for(float a=startA ; a <= stopA; a+=arcStep) {
        vertex((PApplet.sin(a)*(width/2.0f)) + x, (PApplet.cos(a)*(height/2.0f)) + y);
    }
}

@Override
public void vertex(float x, float y) {
      //put this on the vertecies stack before we translate
    super.vertex(x, y);


    PVector vector = new PVector(x,y);
    
    matrix.mult(vector, vector);
    
 

    if (closeShape) {
      beginShapePos = new PVector(vector.x, vector.y, penDownHeight);
      closeShape = false;
    }

    if (penUp) {
      stepTo(pVertex.x, pVertex.y, penLiftHeight);
      stepTo(vector.x, vector.y, penLiftHeight);
      stepTo(vector.x, vector.y, penDownHeight);
      penUp = false;
    }
    else {
      stepTo(vector.x, vector.y, penDownHeight);
    }

    pVertex.x = vector.x;
    pVertex.y = vector.y;


      if(debugVertex)
    	  PApplet.println("vertex: {x:"+x+ " y:"+y+ "}");


  }

@Override
  public void vertex(float x, float y, float z) {

    //put this on the vertecies stack before we translate
    super.vertex(x, y, z);


    PVector vector = new PVector(x,y,z);
    matrix.mult(vector, vector);
    
    
    if (closeShape) {
      beginShapePos = new PVector(vector.x, vector.y, bedDrawHeight);
      closeShape = false;
    }

    if (pVertex.dist(new PVector(vector.x, vector.y, vector.z)) >= flatRes) {
      stepTo(vector.x, vector.y, vector.z);

      pVertex.x = vector.x;
      pVertex.y = vector.y;
      pVertex.z = vector.y;
    }

  if(debugVertex)
    PApplet.println("vertex: {x:"+x+ " y:"+y+ " z:"+z+"}");

  }



  //This is a filler function to scale drawing commands, This should eventually be replaced with the standard Matrix commands 
@Override  
public void scale(float _scale){
	matrix.scale(_scale);
    scale = _scale;
  }

//translate vertex points after scale has been applied
@Override
  public void pushMatrix(){
	
    if (transformCount == transformStack.length) {
        throw new RuntimeException("pushMatrix() cannot use push more than " +
                                   transformStack.length + " times");
      }
    
      transformStack[transformCount] = matrix;
      transformCount++;
      matrix = matrix.get();
      
      
}




@Override
public void translate(float _x, float _y){
 matrix.translate(_x, _y);

}

  //translate vertex points after scale has been applied
  @Override
    public void translate(float _x, float _y, float _z){
	 matrix.translate(_x, _y, _z);

  }
  
  
  @Override
  public void rotate(float radians) {
    matrix.rotate(radians);
  }
  
  
@Override
public
 void popMatrix(){
	   if (transformCount == 0) {
		      throw new RuntimeException("missing a pushMatrix() " +
		                                 "to go with that popMatrix()");
		    }
		    transformCount--;
		    matrix.set(transformStack[transformCount]);
 }

	@Override
	public
	void resetMatrix(){
		matrix = new PMatrix3D();
	}

  /*
  TODO: Add this function
 public void bezierVertex(float x1, float y1,
   float x2, float y2,
   float x3, float y3) {
   
   }
   */

  @Override
public void beginShape(int kind) {
    penUp = true;
    super.beginShape(kind);

    if (kind == 16 ) {
      closeShape = true;
    }
  }

  @Override
public void beginShape() {
    penUp = true;
    super.beginShape();
  }

  @Override
public void endShape() {
    if (beginShapePos != null) {
      vertex(beginShapePos.x, beginShapePos.y);
      beginShapePos = null;
    }
    penUp = true;
    stepTo(pVertex.x, pVertex.y, penLiftHeight);
    super.endShape();
  }




  public void stepTo(float x, float y) {
  stepTo(x,y,bedDrawHeight);
  }

  public void stepTo(float x, float y, float z) {

    // float xMapped = map(x, 0, bedWidth, servoMinRotationX, servoMaxRotationX);
    //float yMapped = map(y, 0, bedHeight, servoMinRotationY, servoMaxRotationY);
    //float zMapped = map(z, 0, bedDepth, servoMinRotationZ, servoMaxRotationZ);

    /*
    if (xMapped < servoMinRotationX || xMapped > servoMaxRotationX 
     || yMapped < servoMinRotationY || yMapped > servoMaxRotationY
     || zMapped < servoMinRotationZ || zMapped > servoMaxRotationZ
     )
     return;
     */
    if (flipX)
      x = PApplet.map(x, -(bedWidth/2.0f), (bedWidth/2.0f), (bedWidth/2.0f), -(bedWidth/2.0f));

    if (flipY)
      y = PApplet.map(y, -(bedHeight/2.0f), (bedHeight/2.0f), (bedHeight/2.0f), -(bedHeight/2.0f));

    if (flipZ)
      z = PApplet.map(z, -(bedDepth/2.0f), (bedDepth/2.0f), (bedDepth/2.0f), -(bedDepth/2.0f));
      

      /*
      Translate out coordinate system from top left corner to center. 
      */
      if(translateCenter){
        x -= bedWidth/2;
        y -= bedHeight/2;
        z -= bedDepth/2;
      }


      x = PApplet.constrain(x,-bedWidth/2.0f,bedWidth/2.0f);
      y = PApplet.constrain(y,-bedHeight/2.0f,bedHeight/2.0f);
      z = PApplet.constrain(z,-bedDepth/2.0f,bedDepth/2.0f);


  if(debugStep)
    PApplet.println("step: {x:"+x+ " y:"+y+ " z:"+z+"}");


    codeStack.add(new PVector(x, y, z));
    numLines++;
  }

  //Clear all drawing commands
  @Override
public void clear() {
    codeStack.clear();
    numLines=0;
    simStep = 0;
    simPrev = null;
  }

  public void setStepRes(float res) {
    flatRes = res;
  }

  public void setSize(float width, float height, float depth) {
    bedWidth = width;
    bedHeight = height;
    bedDepth = depth;
  }

  public void draw(PGraphics g, float _drawSize) {


    float scale = _drawSize/bedWidth;
    PVector prev = null;

    for (int i = 0; i < codeStack.size(); i ++) {
      PVector p = (PVector)codeStack.get(i);

      if (prev != null) {

        if (p.z > penDownHeight || prev.z > penDownHeight)//!= 0 || prev.z != 0)
          g.stroke(0,0,0,50);
        else
          g.stroke(0);

      g.strokeWeight(1);


if(debugDraw)
  PApplet.println("Draw line {x:" + p.x*scale + " y:"  + p.y*scale + " z:"+ p.z*scale + "} {x:" + prev.x*scale + " y:" +prev.y*scale + " z:" + prev.z*scale + " }" );


        g.line(p.x*scale, p.y*scale,p.z*scale, prev.x*scale, prev.y*scale,prev.z*scale);
      }

      prev = p;
    }
  }
  
  
  public void drawCodeStack(PGraphics g) {
	    
	    for (int i = 0; i < codeStack.size(); i ++) {
	      PVector p = (PVector)codeStack.get(i);
	      g.point(p.x,p.y);
	    }
	}
  
  
  


//TODO: broken
  //simulate the whole
  public void simulate(PGraphics g) {

    PVector prev = null;

    if ( simStep < codeStack.size()) {
      PVector p = (PVector)codeStack.get(simStep);

      if (simPrev != null) {
        if (p.z != 0 || simPrev.z != 0)
          g.stroke(240, 240, 240, 250);
        else
          g.stroke(0);

        g.line(p.x, p.y, simPrev.x, simPrev.y);
      }
      simPrev = p;

      simStep++;
    }
}


  public void start(){
	  
	  if(useHttpConnection)
		  sendHttp();
	  else
	  establishContact();
  }
  
  
  
  private void establishContact() {
      //S = get ready to send 
      PApplet.println("establishContact");
      PApplet.println(codeStack.size() + " lines to send");
      if(serialConnected){
      serial.clear();
      serial.write(COMMAND_CONNECT);
    }
      lineCount = 0;
    }



  
  public void update(){
	  serialLoop();  
  }
  
  private void serialLoop(){
      /*
  TODO: Move this out of the draw loop
  */

  //====== moved from serialEvent() =======//

  while ( serialConnected && serial.available () > 0) {
    String inString = serial.readStringUntil('\n') ;//was causing occasional Null Pointer Exceptions.
    
    if (inString == null)
      break;
      
    inString = PApplet.trim(inString);//Until('\n'));  

    PApplet.println("|"+inString +"|");
    serial.clear();

    if (inString.startsWith("G01")) {
     //debugDraw(inString);
    }

    if(inString.startsWith("start")){
    	startButtonPressed = true;
    	//start();//start
     }

if(inString.startsWith("setZ")){
  float zVal = Float.parseFloat(inString.substring(5,inString.length()));
  pressure(zVal); 
  PApplet.println("Z height set to: "+zVal);
 // pressureKnob.setValue(zVal); // add soem type of callback?
}


    if (inString.equals(String.valueOf(COMMAND_READY))) { 
      serial.clear();
      PApplet.println("Plotsi Connected!");      
      PApplet.println("Ready to Plot!");
      //serial.write(stepDelay);
    }
    else if (inString.equals(String.valueOf(COMMAND_SEND_NEXT)) && lineCount < codeStack.size()) {
      /* 
      pack all positions down into 4 character ints e.g.e 5.5 = 0550 50 = 5000 
      TODO: send these as binary! currently each pos takes 4 bytes a unsigned int should also be 4 bytes. 
      */

      PApplet.println("begin sending positions");
      serial.clear();
      float x = ((PVector)(codeStack.get(lineCount))).x;
      float y = ((PVector)(codeStack.get(lineCount))).y;
      float z = ((PVector)(codeStack.get(lineCount))).z;


      int xScaled = (int)(x*100.0);
      int yScaled = (int)(y*100.0);
      int zScaled = (int)(z*100.0);

      PApplet.println("serial write {x:" +xScaled + " y:" + yScaled + " z:" +zScaled + "}");

      serial.write(COMMAND_POS_START_BYTE);
      sendInt(xScaled);
      sendInt(yScaled);
      sendInt(zScaled);
      serial.write(COMMAND_POS_END_BYTE);

      sendStringIndex = 1;
 
            lineCount++;
      if ((lineCount)==codeStack.size()) {
    	      	  
        serial.write(COMMAND_END_STACK);

      }
    } 
    else {
      //println(inByte);
      serial.clear();
    }
  }


  //========================//
  }

  public void sendInt(int i){
      serial.write((byte)i | 0x00); // X
      serial.write((byte)(i>>8)| 0x00); // X
      serial.write((byte)(i>>16)| 0x00); // X
      serial.write((byte)(i>>24)| 0x00); // X
  }
  

  public void pressure(float val) {
	  penDownHeight = val;
	  penLiftHeight = penDownHeight+PEN_LIFT_DELTA;
	  sendPenHeight = true;
	  //clearCanvas();
	  //establishContact();
  }

  
  public void useHttpConnection(String address){
	  serverAddress = address;
	  useHttpConnection = true;
  }
 
  public void sendHttp(){
	  
	  URL url;
	  HttpURLConnection connection;
	  DataOutputStream outputStream = null;
	

	  if(codeStack.size() ==0)
		  return;

      //4 bytes in a int
      //3 ints in a coordinate
      //4 byte START_SEND
      //4 byte END_SEND
      
      int numberOfBytes = (((3*4))*codeStack.size() )+24;
      byte[] codeStackBytes = new byte[numberOfBytes];


	  
      //checksum start 
      codeStackBytes[0] = COMMAND_CONNECT;
      //next 11 bytes are garbage can be used later if needed. 

      
      for(int i = 0; i < codeStack.size(); i++){
    		      	  
    	 
          int x = (int)((((PVector)(codeStack.get(i))).x)*100.0);
          int y = (int)((((PVector)(codeStack.get(i))).y)*100.0);
          int z = (int)((((PVector)(codeStack.get(i))).z)*100.0);  
          
          parent.println("ln:"+i + " "+ x + " " + y + " "+ z);

          //X
          codeStackBytes[((i+1)*12)+0]= (byte)(x);
          codeStackBytes[((i+1)*12)+1]= (byte)(x>>8);
          codeStackBytes[((i+1)*12)+2]= (byte)(x>>16);
          codeStackBytes[((i+1)*12)+3]= (byte)(x>>24);

          parent.println("ln:"+i + " "+codeStackBytes[((i+1)*12)+0] + " " +codeStackBytes[((i+1)*12)+1] + " " + codeStackBytes[((i+1)*12)+2] + " " + codeStackBytes[((i+1)*12)+3]);

          
          //Y
          codeStackBytes[((i+1)*12)+4]= (byte)(y);
          codeStackBytes[((i+1)*12)+5]= (byte)(y>>8);
          codeStackBytes[((i+1)*12)+6]= (byte)(y>>16);
          codeStackBytes[((i+1)*12)+7]= (byte)(y>>24);
          
          //Z
          codeStackBytes[((i+1)*12)+8]= (byte)(z);
          codeStackBytes[((i+1)*12)+9]= (byte)(z>>8);
          codeStackBytes[((i+1)*12)+10]= (byte)(z>>16);
          codeStackBytes[((i+1)*12)+11]= (byte)(z>>24);
          
      }
      
      //checksum start 
      codeStackBytes[codeStackBytes.length-1] = COMMAND_CONNECT;
      //next 11 bytes are garbage can be used later if needed. 
      
      
      //outputStream.write("codeStack=");

              //byte[] buffer contains the data
      try{
    	  PApplet.println("About to send drawing to: "+ serverAddress); 
    	  PApplet.println("codeStack size to send: "+ (numberOfBytes)+"b"); 
    	  
  		String urlParameters = "codeStack=";

  		
  		url = new URL(serverAddress);
	    connection = (HttpURLConnection) url.openConnection();
	    connection.setDoOutput(true);
	    connection.setDoInput(true);

	  //  String stringToStore = Base64.encode(codeStackBytes).toString();
	    
	    
		outputStream = new DataOutputStream( connection.getOutputStream());
	   // outputStream.writeBytes(urlParameters);
		outputStream.write(codeStackBytes);
		outputStream.flush();
	    outputStream.close();
	    
	      //Get Response	
	      InputStream is = connection.getInputStream();
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	      String line;
	      StringBuffer response = new StringBuffer(); 
	      parent.println("Getting Http response");

	      while((line = rd.readLine()) != null) {
	        parent.println(line);
	      }
	      rd.close();
	      
	      

	} catch (IOException e) {
		PApplet.println("unable to send drawing to server");
		// TODO Auto-generated catch block
		e.printStackTrace();
	}       
      
  }
  
  
  
  public void removeOcclusions(PGraphics occlusionCanvas){
	  
	  
	  PApplet.println("Size before remove occlusions: "+codeStack.size());
	  float scale = occlusionCanvas.width/ bedWidth;
	  occlusionCanvas.beginDraw();
	    for (int i = 0; i < codeStack.size(); i ++) {
	        PVector p = (PVector)codeStack.get(i);
	        
	        
	        
	        if(p.z == penDownHeight){
	        	PVector vectTranslated = new PVector((int)((p.x*scale)+(occlusionCanvas.width/2.0f)), (int)((p.y *scale)+(occlusionCanvas.height/2.0f)));
	        	int c = occlusionCanvas.get((int)vectTranslated.x,(int)vectTranslated.y);


	        	//occlusionCanvas.stroke(0);

	        	if(occlusionCanvas.brightness(c) == 255){
	        		
	        		p.z = penLiftHeight; //lift pen over occuded area's 
	        		//codeStack.remove((int)i); // remove occluded 
	        		//numLines--;
	        		//i--;

	        	}else{

	        	}
	        	

        		
	        }
	    }
	    occlusionCanvas.endDraw();
		  PApplet.println("Size after remove occlusions: "+codeStack.size());

  }


	
	
	// myParent is a reference to the parent sketch
	PApplet myParent;

	int myVariable = 0;
	
	public final static String VERSION = "##library.prettyVersion##";
	

	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example Hello
	 * @param theParent
	 */
	public PiccoloP5(PApplet theParent) {
		myParent = theParent;
		welcome();
	}
	
	
	private void welcome() {
		System.out.println("##library.name## ##library.prettyVersion## by ##author##");
	}
	
	
	public String sayHello() {
		return "hello library.";
	}
	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}


	
	
	
}

