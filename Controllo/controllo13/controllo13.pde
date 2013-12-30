//Libraries
import controlP5.*;
import processing.serial.*;
import java.awt.FileDialog;
import geomerative.*;
import java.util.*;

boolean debugDraw = true; // draw Piccolo current position

//Plotsi settings 
boolean flipX = true;
boolean flipY = true;
boolean flipZ = false;
boolean rotateBed = true;


float bedWidth = 300; 
float bedHeight = 300; 
float bedDepth = 300; 

float myX = bedWidth/2;
float myY = bedHeight/2;
float myZ = bedDepth/2;



//the height to lift the pen between 2d shapes
float penDownHeight = 90; // change using slider
float penLiftHeight = penDownHeight+50; //change using slider


float xPos = bedWidth/2;        
float yPos = bedHeight/2;      
float zPos = bedDepth/2;  

//sensor sets
public float lightLevel = 0.5;

PShape loadedSVG;
boolean sendPenHeight = false;

List path = new ArrayList();
List codeStack = new ArrayList();
PlotiWriter writer = new PlotiWriter();
ControlP5 controlP5;
CheckBox drawPlotsiOutput;
Knob pressureKnob;

PGraphics plotsiOutputCanvas; 


void setup() {
  size(500, 340);
  plotsiOutputCanvas = createGraphics((int)bedWidth, (int)bedHeight);
  plotsiOutputCanvas.beginDraw();
  plotsiOutputCanvas.smooth();
  //plotsiOutputCanvas.background(255);
  plotsiOutputCanvas.endDraw();

  //detail
  writer.setStepRes(1f);
  writer.bezierDetail(20); 

  controlP5 = new ControlP5(this);
  drawInterface();

  // List all the available serial ports
  println(Serial.list());
  myPort = new Serial(this, Serial.list()[Serial.list().length -1 ], 115200);
  myPort.bufferUntil('\n');

  // Initialise Geomerative for working with type.
  RG.init(this);
  delay(200);
  home();
}



void draw() {

  background(255, 255, 255);

  if (loadedSVG != null)
    shape(loadedSVG, 150, 10);

  /*  
   plotsiOutputCanvas.beginDraw();
   writer.simulate(plotsiOutputCanvas);
   plotsiOutputCanvas.endDraw();
   */

  //debug draw
  if (plotsiOutputCanvas!=null)
    image (plotsiOutputCanvas, 150, 10);
  stroke(100, 100, 100);
  noFill();
  rect(150, 10, bedWidth, bedHeight);


  //====== moved from serialEvent() =======//

  while (myPort.available () > 0) {

    String inString = myPort.readStringUntil('\n') ;//was causing occasional Null Pointer Exceptions.
   
    if (inString == null)
      break;
      
   
    inString = trim(inString);//Until('\n'));  

   

    println("|"+inString +"|");
    myPort.clear();


    if (inString.startsWith("G01")) {
      debugDraw(inString);
    }

    if(inString.startsWith("start")){
    start(0);
     }

if(inString.startsWith("setZ")){
  float zVal = Float.parseFloat(inString.substring(5,inString.length()));
  pressure((int)zVal); 
  pressureKnob.setValue(zVal);
}


    if (inString.equals("A")) { 
      myPort.clear();
      println("Plotsi Connected!");      
      println("Ready to Plot!");
      //myPort.write(stepDelay);
    }
    else if (inString.equals("B") && lineCount < codeStack.size()) {
      println("begin sending positions");
      myPort.clear();
      float x = ((PVector)(codeStack.get(lineCount))).x;
      float y = ((PVector)(codeStack.get(lineCount))).y;
      float z = ((PVector)(codeStack.get(lineCount))).z;

      String xString = Integer.toString(int(x*100));
      String yString = Integer.toString(int(y*100));
      String zString = Integer.toString(int(z*100));

     // println("ZString before " +zString);


      if (debugDraw) {
        if (flipX)
          x = map(x, 0, bedWidth, bedWidth, 0);

        if (flipY)
          y = map(y, 0, bedHeight, bedHeight, 0);

        plotsiOutputCanvas.beginDraw();
        plotsiOutputCanvas.stroke(255, 0, 0);
        plotsiOutputCanvas.noFill();
        plotsiOutputCanvas.ellipse( x, y, 2, 2);
        plotsiOutputCanvas.endDraw();
      }
      
      while(xString.length() < 5)
              xString = "0"+xString;
              
      while(yString.length() < 5)
              yString = "0"+yString;

      while(zString.length() < 5)
              zString = "0"+zString;


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
 if(rotateBed)
       sendString =  yString + xString + zString + ';';
 else
      sendString = xString + yString + zString + ';';
      // println(sendString);
      //println(sendString.length());
      myPort.write(sendString);
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


      if ((lineCount)==codeStack.size()) {
        myPort.write('E');
        delay(10);
        println("finished!");
        //stop();
      }
    } 
    else {
      //println(inByte);
      myPort.clear();
    }
  }


  //========================//
}

void drawInterface() {

  drawPlotsiOutput = controlP5.addCheckBox("draw output", 100, 100);  
  controlP5.addButton("load_SVG", 0, 10, 10, 80, 19);
  controlP5.addSlider("lightLevel", 0, 1.0f, 0.5f, 10, 33, 80, 10);
  controlP5.addButton("generate_tree", 0, 10, 50, 80, 19);
  controlP5.addButton("generate_mustache", 0, 10, 70, 80, 19);
  //  controlP5.addButton("brush_Mustache", 0, 10, 120, 80, 19);
  controlP5.addButton("TicTacToe", 0, 10, 90, 80, 19);  
  controlP5.addButton("logo", 0, 10, 110, 80, 19);
  //  controlP5.addButton("maze", 0, 10, 180, 80, 19);
  controlP5.addButton("circles", 0, 10, 130, 80, 19);
  controlP5.addButton("boxes", 0, 10, 150, 80, 19);
  controlP5.addButton("diagonals", 0, 10, 170, 80, 19);
  controlP5.addButton("word", 0, 10, 190, 80, 19);

  //  controlP5.addButton("W", 0, 10, 170, 15, 19);
  //  controlP5.addButton("I", 0, 30, 170, 15, 19);
  //  controlP5.addButton("R", 0, 50, 170, 15, 19);
  //  controlP5.addButton("E", 0, 70, 170, 15, 19);
  //  controlP5.addButton("D", 0, 90, 170, 15, 19);

  pressureKnob = controlP5.addKnob("pressure")
    .setRange(0, bedDepth)
      .setValue(penDownHeight)
        .setPosition(10, 220)
          .setRadius(15)
            .setViewStyle(Knob.ARC)
              .setDragDirection(Knob.VERTICAL)
                ;
  controlP5.addButton("up_", 0, 50, 223, 30, 10);
  controlP5.addButton("down_", 0, 50, 237, 30, 10);

  //  controlP5.addButton("w",0, 30, 220, 19, 19);
  //  controlP5.addButton("s",0, 30, 240, 19, 19);
  //  controlP5.addButton("a",0, 10, 230, 19, 19);
  //  controlP5.addButton("d",0, 50, 230, 19, 19);
  //  controlP5.addButton("Up",0, 70, 220, 19, 19);
  //  controlP5.addButton("Down",0, 70, 240, 19, 19);

  controlP5.addButton("home", 0, 10, height-80, 80, 19);
  controlP5.addButton("start", 0, 10, height-60, 80, 19);
    controlP5.addButton("stop", 0, 10, height-40, 80, 19);

}



void debugDraw(String inString) {
  plotsiOutputCanvas.beginDraw();
  float[] posIn = float(split(inString, ","));

  if (posIn.length >=4) {

    plotsiOutputCanvas.noFill();

    if (posIn[3] == 0)
      plotsiOutputCanvas.stroke(255, 0, 0);
    else
      plotsiOutputCanvas.stroke(240, 240, 240, 254);

    if (flipX)
      posIn[1] = map(posIn[1], 0, bedWidth, bedWidth, 0);

    if (flipY)
      posIn[2] = map(posIn[2], 0, bedHeight, bedHeight, 0);

    //draw directly to the screen
    plotsiOutputCanvas.line(xPos, yPos, posIn[1], posIn[2]);
    xPos = posIn[1];
    yPos = posIn[2];
    zPos = posIn[3];
  } 
  plotsiOutputCanvas.endDraw();
}

public void start(int val) {
  if (loadedSVG != null) {
    writer.clear();
    writer.shape(loadedSVG);
  }
  establishContact();
}

public void penUp(int val) {
  writer.clear(); 
  writer.vertex(bedWidth/2, bedHeight/2, penLiftHeight);
  writer.vertex(bedWidth/2, bedHeight/2, penLiftHeight);

  establishContact();
}

public void penDown(int val) {
  writer.clear(); 
  writer.vertex(bedWidth/2, bedHeight/2, 0);
  writer.vertex(bedWidth/2, bedHeight/2, 0);

  establishContact();
}


public void load_SVG(int val) {

  FileDialog fd = new FileDialog(frame, "open", 
  FileDialog.LOAD);
  //fd.setFile("chair" + SETTINGS.chairSaveNum + ".svg");
  String currentDir = new File(".").getAbsolutePath();
  //fd.setDirectory(currentDir + "\\savedChairs\\");
  fd.setLocation(50, 50);
  fd.pack();

  fd.show();

  if (fd.getName() != null) {
    String filename = fd.getFile();
    clearCanvas();
    loadedSVG = loadShape(fd.getDirectory() + filename);
    //loadedSVG.scale(0.6); 
    //loadedSVG.disableStyle();
  } 
  else {
    // println("not an stl file");
  }
}

void clearCanvas() {
  plotsiOutputCanvas = createGraphics((int)bedWidth, (int)bedHeight);
  plotsiOutputCanvas.beginDraw();
  plotsiOutputCanvas.smooth();
  //plotsiOutputCanvas.background(255);
  plotsiOutputCanvas.endDraw();
  loadedSVG = null;
}

void drawCanvas() {
  plotsiOutputCanvas.beginDraw();
  plotsiOutputCanvas.background(255);
  plotsiOutputCanvas.noFill();
  writer.debugDraw(plotsiOutputCanvas);
  plotsiOutputCanvas.endDraw();
}

void pressure(int val) {
  penDownHeight = val;
  penLiftHeight = penDownHeight+50;
  sendPenHeight = true;
 // clearCanvas();
  // establishContact();
}

void up_() {
  penDownHeight+=5;
  penLiftHeight = penDownHeight+40;
  writer.clear();
  writer.stepTo(150, 300, penDownHeight);
  establishContact();
  clearCanvas();
}

void down_() {
  penDownHeight-=5;
  println(penDownHeight);
  penLiftHeight = penDownHeight+40;
  writer.clear();
  writer.stepTo(150, 300, penDownHeight);
  establishContact();
  clearCanvas();
}

void home() {
  writer.clear();
  writer.stepTo(150, 290, penLiftHeight);
  establishContact();
 // clearCanvas();
}

// =========================================================== //

public void TicTacToe() {
  writer.clear();
  clearCanvas();
  drawGrid(writer);
  drawCanvas();
}

public void generate_tree() {
  writer.clear();
  clearCanvas();
  generatePlant( lightLevel, writer);
  drawCanvas();
}

public void generate_mustache() {
  writer.clear();
  clearCanvas();
  drawMustache(writer);
  drawCanvas();
}

public void logo(int val) {
  writer.clear();
  clearCanvas();
  loadedSVG = loadShape("logo.svg");
}

public void circles() {
  writer.clear();
  clearCanvas();
  drawCircles(writer);
  drawCanvas();
}

public void boxes() {
  writer.clear();
  clearCanvas();
  drawBoxes(writer);
  drawCanvas();
}

public void diagonals() {
  writer.clear();
  clearCanvas();
  drawDiagonals(writer);
  drawCanvas();
}

public void word() {
  writer.clear();
  clearCanvas();
  drawWord(writer);
  drawCanvas();
}

/*
public void brush_Mustache() {
 writer.clear();
 clearCanvas();
 drawBrushMustache(writer);
 
 plotsiOutputCanvas.beginDraw();
 plotsiOutputCanvas.background(255);
 plotsiOutputCanvas.noFill();
 writer.debugDraw(plotsiOutputCanvas);
 plotsiOutputCanvas.endDraw();
 }
 
 public void maze() {
 writer.clear();
 clearCanvas();
 loadedSVG = loadShape("maze.svg");
 }  
 */


/*

void mouseReleased() {

  if (sendPenHeight) {    
    writer.clear();
    writer.stepTo(myX, myY, penDownHeight);
    establishContact();
    sendPenHeight = false;
  }
}
*/


void stop(){
  lineCount = 0;
  codeStack.clear();
}


public void Up(int val) {
  println("Up");
  writer.clear();
  establishContact();
  if (myZ<90)
  {
    myZ+=10;
    writer.stepTo(myX, myY, myZ);
  }
  print("myZ:");
  println(myX);
  println(myY);
  println(myZ);
}

public void Down(int val) {
  writer.clear();
  establishContact();
  if (myZ>10)
  {
    myZ-=10;
    writer.stepTo(myX, myY, myZ);
  }
  print("myZ:");
  println(myX);
  println(myY);
  println(myZ);
}

