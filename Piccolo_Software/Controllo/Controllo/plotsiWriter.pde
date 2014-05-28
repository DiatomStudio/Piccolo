/*
PlotsiWriter
____________
PlotsiWriter extends the Processing PGraphics, this means that you can use any of the processing drawing commands, ellipse, line, rect etc to send to Piccolo. 
PlotsiWriter will currently only send stroke commands and does not support fills. 
*/
int CHAR_PER_POS = 8;



//Piccolo send coordinate settings, all false by default
//TODO: These should be reflected in GUI
boolean flipX = false; 
boolean flipY = false;
boolean flipZ = false;
boolean rotateBed = false;



//debug output all serial communications to the console for debugging. 
boolean debug = true; 
boolean serialConnected = false;



//TODO: scale render not output. 
//TODO: resize SVG to fit bed.
// scale pixels to Piccolo coordinates
float scaleOutput = 0.16666666666667; 




//the height to lift the pen between 2d shapes
float penDownHeight = -bedDepth/2; // change using slider
float penLiftHeight = penDownHeight+2; //change using slider





class PlotsiWriter extends PGraphics {


  
  boolean sendPenHeight = true;
  
  boolean translateCenter = false; //if true all coordinates are translated by half the bed height and width to match Piccolo's native coordinate system. 
  PVector beginShapePos = null;
  boolean closeShape = false;
  boolean penUp = true;

  PGraphics screenCanvas; //Canvas used for buffering drawing steps to screen

  //should we flattern curves?
  boolean flattern = true;
  float flatRes = 1.0f;

  //Previous Vector
  PVector pVertex = new PVector(0, 0, 0);

  //Simulation stuff
  int simStep = 0;
  PVector simPrev = null;
  List codeStack = new ArrayList(); //TODO: move to plotsiWriter

  //Drawing to screen 
  float screenCanvasWidth =  300;
  float screenCanvasHeight =  300;
  float screenCanvasDepth =  300;

  //Bed Size
  float DEFAULT_BED_WIDTH = 50;
  float DEFAULT_BED_HEIGHT = 50;
  float DEFAULT_BED_DEPTH = 50;
 
  float bedWidth = DEFAULT_BED_WIDTH;
  float bedWHeight = DEFAULT_BED_WIDTH;
  float bedDepth= DEFAULT_BED_WIDTH;

  float bedDrawHeight = -(bedDepth/2.0f) ; // Piccolo draws with x, y and z starting in the center of the draw area. 
  
  float scale = 1.0;
  float translateX = 0.0;
  float translateY = 0.0;
  float translateZ = 0.0;


/*Serial Commands
*/
import processing.serial.*;
Serial serial;

boolean firstContact = false;
boolean stepDelaySet = false;
boolean pressureSet = false;

int inByte;
String sendString;  //xxxxxyyyyyzzzzz;
int sendStringIndex = 0;

String code[];


int zDraw = 0;
int zLift = 10;

int lineCount = 0;
int numLines;



boolean debugVertex = true; //write all vertex commands to console
boolean debugStep = true; // write all step commands to console
boolean debugDraw = false; //write all drawing commands to the console 


public PlotsiWriter(){
//return PlotsiWriter(DEFAULT_BED_WIDTH,DEFAULT_BED_HEIGHT,DEFAULT_BED_DEPTH);
}

public PlotsiWriter(float _w, float _h, float _d){
//screenCanvas = createGraphics(screenCanvasW, screenCanvasH);
}

@Override
public void vertex(float x, float y) {
      //put this on the vertecies stack before we translate
    super.vertex(x, y);


    x *= scale; 
    y *= scale;

    x += translateX;
    y += translateY;


    if (closeShape) {
      beginShapePos = new PVector(x, y, penDownHeight);
      closeShape = false;
    }

    if (penUp) {
      stepTo(pVertex.x, pVertex.y, penLiftHeight);
      stepTo(x, y, penLiftHeight);
      stepTo(x, y, penDownHeight);
      penUp = false;
    }
    else {
      stepTo(x, y, penDownHeight);
    }

    pVertex.x = x;
    pVertex.y = y;


      if(debugVertex)
    println("vertex: {x:"+x+ " y:"+y+ "}");


  }

@Override
  public void vertex(float x, float y, float z) {

    //put this on the vertecies stack before we translate
    super.vertex(x, y, z);



    x *= scale; 
    y *= scale;
    z *= scale; 

    x += translateX;
    y += translateY;
    z += translateZ;

    if (closeShape) {
      beginShapePos = new PVector(x, y, bedDrawHeight);
      closeShape = false;
    }

    if (pVertex.dist(new PVector(x, y, z)) >= flatRes) {
      stepTo(x, y, z);

      pVertex.x = x;
      pVertex.y = y;
      pVertex.z = y;
    }

  if(debugVertex)
    println("vertex: {x:"+x+ " y:"+y+ " z:"+z+"}");

  }



  //This is a filler function to scale drawing commands, This should eventually be replaced with the standard Matrix commands 
  public void scale(float _scale){
    scale = _scale;
  }

  //translate vertex points after scale has been applied
    public void translate(float _x, float _y, float _z){
    translateX = _x; 
    translateY = _y; 
    translateZ = _z; 

  }
  
@Override
 void popMatrix(){
  scale = 1.0; 
  translateX = 0;
  translateY = 0;
  translateZ = 0;
 }
@Override
void resetMatrix(){
  popMatrix();
}

  /*
  TODO: Add this function
 public void bezierVertex(float x1, float y1,
   float x2, float y2,
   float x3, float y3) {
   
   }
   */

  public void beginShape(int kind) {

    penUp = true;
    super.beginShape(kind);

    if (kind == 16 ) {
      closeShape = true;
    }
  }

  public void beginShape() {
    penUp = true;
    super.beginShape();
  }

  public void endShape() {
    if (beginShapePos != null) {
      vertex(beginShapePos.x, beginShapePos.y);
      beginShapePos = null;
    }
    penUp = true;
    stepTo(pVertex.x, pVertex.y, penLiftHeight);
    super.endShape();
  }




  void stepTo(float x, float y) {
  stepTo(x,y,bedDrawHeight);
  }

  void stepTo(float x, float y, float z) {

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
      x = map(x, -(bedWidth/2.0), (bedWidth/2.0), (bedWidth/2.0), -(bedWidth/2.0));

    if (flipY)
      y = map(y, -(bedHeight/2.0), (bedHeight/2.0), (bedHeight/2.0), -(bedHeight/2.0));

    if (flipZ)
      z = map(z, -(bedDepth/2.0), (bedDepth/2.0), (bedDepth/2.0), -(bedDepth/2.0));
      

      /*
      Translate out coordinate system from top left corner to center. 
      */
      if(translateCenter){
        x -= bedWidth/2;
        y -= bedHeight/2;
        z -= bedDepth/2;
      }


      x = constrain(x,-(bedWidth/2),bedWidth/2);
      y = constrain(y,-bedHeight/2,bedHeight/2);
      z = constrain(z,-bedDepth/2,bedDepth/2);


  if(debugStep)
    println("step: {x:"+x+ " y:"+y+ " z:"+z+"}");


    codeStack.add(new PVector(x, y, z));
    numLines++;
  }

  //Clear all drawing commands
  void clear() {
    codeStack.clear();
    numLines=0;
    simStep = 0;
    simPrev = null;
  }

  void setStepRes(float res) {
    flatRes = res;
  }

  void setSize(float width, float height, float depth) {
    bedWidth = width;
    bedHeight = height;
    bedDepth = depth;
  }

  void draw(PGraphics g, float _drawSize) {


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
  println("Draw line {x:" + p.x*scale + " y:"  + p.y*scale + " z:"+ p.z*scale + "} {x:" + prev.x*scale + " y:" +prev.y*scale + " z:" + prev.z*scale + " }" );


        g.line(p.x*scale, p.y*scale,p.z*scale, prev.x*scale, prev.y*scale,prev.z*scale);
      }

      prev = p;
    }
  }


//TODO: broken
  //simulate the whole
  void simulate(PGraphics g) {

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


    void establishContact() {
      //S = get ready to send 
      println("establishContact");
      println(writer.codeStack.size() + " lines to send");
      if(serialConnected){
      serial.clear();
      serial.write('S');
    }
      lineCount = 0;
    }



  

  void serialLoop(){
      /*
  TODO: Move this out of the draw loop
  */

  //====== moved from serialEvent() =======//

  while ( serialConnected && serial.available () > 0) {
    String inString = serial.readStringUntil('\n') ;//was causing occasional Null Pointer Exceptions.
    if (inString == null)
      break;
      
    inString = trim(inString);//Until('\n'));  

    println("|"+inString +"|");
    serial.clear();

    if (inString.startsWith("G01")) {
      debugDraw(inString);
    }

    if(inString.startsWith("start")){
    start(0);
     }

if(inString.startsWith("setZ")){
  float zVal = Float.parseFloat(inString.substring(5,inString.length()))/scaleOutput;
  pressure((int)zVal); 
  pressureKnob.setValue(zVal);
}


    if (inString.equals("A")) { 
      serial.clear();
      println("Plotsi Connected!");      
      println("Ready to Plot!");
      //serial.write(stepDelay);
    }
    else if (inString.equals("B") && lineCount < writer.codeStack.size()) {
      /* 
      pack all positions down into 4 character ints e.g.e 5.5 = 0550 50 = 5000 
      TODO: send these as binary! currently each pos takes 4 bytes a unsigned int should also be 4 bytes. 
      */

      println("begin sending positions");
      serial.clear();
      float x = ((PVector)(writer.codeStack.get(lineCount))).x;
      float y = ((PVector)(writer.codeStack.get(lineCount))).y;
      float z = ((PVector)(writer.codeStack.get(lineCount))).z;


      int xScaled = (int)(x*100.0);
      int yScaled = (int)(y*100.0);
      int zScaled = (int)(z*100.0);


/*
      String xString = Integer.toString(int(x*100));
      String yString = Integer.toString(int(y*100));
      String zString = Integer.toString(int(z*100));

     // println("ZString before " +zString);



      if (debug) {
        /*
        if (flipX)
          x = map(x, 0, bedWidth, bedWidth, 0);
        if (flipY)
          y = map(y, 0, bedHeight, bedHeight, 0);
        */
        
        //TODO move this to draw loop somehow 
        /*
        plotsiOutputCanvas.beginDraw();
        plotsiOutputCanvas.stroke(255, 0, 0);
        plotsiOutputCanvas.noFill();
        plotsiOutputCanvas.ellipse( x/scaleOutput, y/scaleOutput, 2, 2);
        plotsiOutputCanvas.endDraw();
        */
     // }
      

      /*
      //pad send string with 0's if under 4 characters 
      while(xString.length() < 5)
              xString = "0"+xString;
              
      while(yString.length() < 5)
              yString = "0"+yString;

      while(zString.length() < 5)
              zString = "0"+zString;
*/

/*
      if (xString.length() < 4) {
        xString = "00"+xString;
      }
      if (xString.length() < 5) {
        xString = "0"+xString;
      }
     
      if (yString.length() < 4) {
        yString = "00"+yString;
      }        
      if (yString.length() < 5) {
        yString = "0"+yString;
      }
      if (zString.length() < 4) {
        zString = "00"+zString;
      }        
      if (zString.length() < 5) {
        zString = "0"+zString;
      }
 */
 
 //rotate the bed by swapping x-y
      /*
 if(rotateBed)
       sendString =  yString + xString + zString + ';';
 else
      sendString = xString + yString + zString + ';';
      */
      // println(sendString);
      //println(sendString.length());

      println("serial write {x:" +xScaled + " y:" + yScaled + " z:" +zScaled + "}");


      sendInt(xScaled);
      sendInt(yScaled);
      sendInt(zScaled);

      serial.write(';');

      sendStringIndex = 1;
      /*
       println("line#"+lineCount+" of "+codeStack.size()); 
       println(sendString);
       println(x + " " + y + " " + z);
      println("XString " +xString);
      println("YString " +yString);
      println("ZString " +zString);
      */
            lineCount++;


      if ((lineCount)==writer.codeStack.size()) {
        serial.write('E');
        delay(10);
        println("finished!");
        //stop();
      }
    } 
    else {
      //println(inByte);
      serial.clear();
    }
  }


  //========================//
  }

  void sendInt(int i){
      serial.write((byte)i); // X
      serial.write((byte)(i>>8)); // X
      serial.write((byte)(i>>16)); // X
      serial.write((byte)(i>>24)); // X
  }
  
  
  public void start(int val) {

  writer.establishContact();
}


void pressure(int val) {
  penDownHeight = val;
  penLiftHeight = penDownHeight+50;
  sendPenHeight = true;
 // clearCanvas();
  // establishContact();
}


}

