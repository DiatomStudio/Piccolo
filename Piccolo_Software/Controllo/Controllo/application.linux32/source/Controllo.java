import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import controlP5.*; 
import processing.serial.*; 
import java.awt.FileDialog; 
import geomerative.*; 
import java.util.*; 
import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Controllo extends PApplet {

//Libraries






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


float scaleOutput = 0.16666666666667f; // scale pixels to Piccolo coordinates

//the height to lift the pen between 2d shapes
float penDownHeight = 90; // change using slider
float penLiftHeight = penDownHeight+50; //change using slider


float xPos = bedWidth/2;        
float yPos = bedHeight/2;      
float zPos = bedDepth/2;  

//sensor sets
public float lightLevel = 0.5f;

PShape loadedSVG;
boolean sendPenHeight = false;

List path = new ArrayList();
List codeStack = new ArrayList();
PlotiWriter writer = new PlotiWriter();
ControlP5 controlP5;
CheckBox drawPlotsiOutput;
Knob pressureKnob;

PGraphics plotsiOutputCanvas; 


public void setup() {
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



public void draw() {

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
      float x = ((PVector)(codeStack.get(lineCount))).x*scaleOutput;
      float y = ((PVector)(codeStack.get(lineCount))).y*scaleOutput;
      float z = ((PVector)(codeStack.get(lineCount))).z*scaleOutput;

      String xString = Integer.toString(PApplet.parseInt(x*100));
      String yString = Integer.toString(PApplet.parseInt(y*100));
      String zString = Integer.toString(PApplet.parseInt(z*100));

     // println("ZString before " +zString);


      if (debugDraw) {
        /*
        if (flipX)
          x = map(x, 0, bedWidth, bedWidth, 0);

        if (flipY)
          y = map(y, 0, bedHeight, bedHeight, 0);
  */
        plotsiOutputCanvas.beginDraw();
        plotsiOutputCanvas.stroke(255, 0, 0);
        plotsiOutputCanvas.noFill();
        plotsiOutputCanvas.ellipse( x/scaleOutput, y/scaleOutput, 2, 2);
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

public void drawInterface() {

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



public void debugDraw(String inString) {
  plotsiOutputCanvas.beginDraw();
  float[] posIn = PApplet.parseFloat(split(inString, ","));

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

public void clearCanvas() {
  plotsiOutputCanvas = createGraphics((int)bedWidth, (int)bedHeight);
  plotsiOutputCanvas.beginDraw();
  plotsiOutputCanvas.smooth();
  //plotsiOutputCanvas.background(255);
  plotsiOutputCanvas.endDraw();
  loadedSVG = null;
}

public void drawCanvas() {
  plotsiOutputCanvas.beginDraw();
  plotsiOutputCanvas.background(255);
  plotsiOutputCanvas.noFill();
  writer.debugDraw(plotsiOutputCanvas);
  plotsiOutputCanvas.endDraw();
}

public void pressure(int val) {
  penDownHeight = val;
  penLiftHeight = penDownHeight+50;
  sendPenHeight = true;
 // clearCanvas();
  // establishContact();
}

public void up_() {
  penDownHeight+=5;
  penLiftHeight = penDownHeight+40;
  writer.clear();
  writer.stepTo(150, 300, penDownHeight);
  establishContact();
  clearCanvas();
}

public void down_() {
  penDownHeight-=5;
  println(penDownHeight);
  penLiftHeight = penDownHeight+40;
  writer.clear();
  writer.stepTo(150, 300, penDownHeight);
  establishContact();
  clearCanvas();
}

public void home() {
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


public void stop(){
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



Serial myPort;

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


/*
void serialEvent(Serial myPort) {
  String inString = myPort.readString();
  println(inString);
  println(">"+inString +"<");
}
*/

public void establishContact() {
  //S = get ready to send 
  println("establishContact");
  println(codeStack.size() + " lines to send");
  myPort.clear();
  myPort.write('S');
  lineCount = 0;
}




public void drawGrid(PGraphics g) {
  
  int gridWidth = 210;
  int o = 45;
  int w = gridWidth;
  
  g.beginShape();
  g.vertex(o,o);
  g.vertex(o,o+w);  
  g.vertex(o+w,o+w);  
  g.vertex(o+w,o);
  g.vertex(o,o);
  g.endShape();
  
  g.beginShape();
  g.vertex(o+w/3,o);
  g.vertex(o+w/3,o+w);  
  g.endShape();

  g.beginShape();
  g.vertex(o+2*w/3,o);
  g.vertex(o+2*w/3,o+w);  
  g.endShape();  
  
  g.beginShape();
  g.vertex(o,o+w/3);
  g.vertex(o+w,o+w/3);  
  g.endShape();

  g.beginShape();
  g.vertex(o,o+2*w/3);
  g.vertex(o+w,o+2*w/3);  
  g.endShape();    
  
  g.point(1, 150); //home
  
}

public void drawX(int x, int y) {
  
}

public void drawO(int x, int y) {
  
}


public void drawCircles(PGraphics g) {

  int o = 150;
  int r = 120;
  int a = 4; 
  int b = 48;

  float x;
  float y; 

  g.beginShape();
  for (int i = r; i > 10; i -= a) {
    for (float j=TWO_PI; j>0; j-= TWO_PI/b) {
      x = i*sin(j)+o;
      y = i*cos(j)+o;
      g.vertex(x, y);
    }
  }
  g.endShape();
  g.point(150, 290); //home
}


public void drawBoxes(PGraphics g) {

  int o = 150;
  int r = 120;
  int a = 4; 

  g.beginShape();
  for (int i = r; i > 10; i -= a) {
    g.vertex(o+i, o+i);
    g.vertex(o+i, o-i);
    g.vertex(o-i, o-i);
    g.vertex(o-i, o+i);
    g.vertex(o+i, o+i);
  }
  g.endShape();
  g.point(150, 290); //home
}



public void drawDiagonals(PGraphics g) 
{ 

  int o = 150;
  int r = 120;
  int a = 5; 
    
  g.beginShape();
  
    int i = -r;
    g.vertex(o+i, o-r);
    
    while(i<(r-a%r)) {
    i += a;
    g.vertex(o+i, o-r);
    g.vertex(o-r, o+i);  
    i += a;
    g.vertex(o-r, o+i);
    g.vertex(o+i, o-r); 
    }
    
    while(i>-(r-a%r)) {
    i -= a;
    g.vertex(o+r, o-i);
    g.vertex(o-i, o+r);  
    i -= a;
    g.vertex(o-i, o+r);
    g.vertex(o+r, o-i); 
    }
    
    g.vertex(o-i, o+r);
    
  g.endShape();
  g.point(150, 290); //home

}

public void drawWord(PGraphics g) {
  
  int o = 150;
  int fontHeight = 100;
  String font = "swsimp.ttf";
  
  
  RShape grp1;
  RShape grp2;
  RShape grp3;
  RShape grp4;
  RPoint[] points1;
  RPoint[] points2;
  RPoint[] points3;
  RPoint[] points4;
  
  grp1 = RG.getText("P", font,fontHeight, CENTER);
  grp2 = RG.getText("L", font,fontHeight, CENTER);
  grp3 = RG.getText("A", font,fontHeight, CENTER);
  grp4 = RG.getText("Y", font,fontHeight, CENTER);

  RG.setPolygonizer(RG.ADAPTATIVE);
  //grp.draw();

  // Get the points on the curve's shape
  //RG.setPolygonizer(RG.UNIFORMSTEP);
  //RG.setPolygonizerStep(15);

  RG.setPolygonizer(RG.UNIFORMLENGTH);
  RG.setPolygonizerLength(4);
  
  points1 = grp1.getPoints();
  points2 = grp2.getPoints();
  points3 = grp3.getPoints();  
  points4 = grp4.getPoints();
  
  g.beginShape();
  for(int i=1; i<points1.length; i++) {
    g.vertex(points1[i].x+o-fontHeight,points1[i].y+o);
  }
  g.endShape();
  
  g.beginShape();
  for(int i=1; i<points2.length; i++) {
    g.vertex(points2[i].x+o-3*fontHeight/8,points2[i].y+o);
  }
  g.endShape();
  
  g.beginShape();
  for(int i=1; i<points3.length; i++) {
    g.vertex(points3[i].x+o+3*fontHeight/8,points3[i].y+o);
  }
  g.endShape();
  
  g.beginShape();
  for(int i=1; i<points4.length; i++) {
    g.vertex(points4[i].x+o+fontHeight,points4[i].y+o);
  }
  g.endShape();
  
  
  
  
}




float startHeight = 150;

public void drawBrushMustache(PGraphics g){
  
  calculateControlPoint();
  
  
  //Dip Brush
  g.vertex(  250, 250,penLiftHeight);
  g.vertex(  250, 250,10);
  g.vertex(  250, 250,penLiftHeight);

  
 // bezier(rStartX,rStartY,rStartC1X,rStartC1Y,rEndC1X,rEndC1Y,rEndX,rEndY);
    
  //bezier(lStartX,lStartY,lStartC1X,lStartC1Y,lEndC1X,lEndC1Y,lEndX,lEndY);
  
   // randomSeed(analogRead(0));
    float thickness = random(5,40);
    WHISKERS = (int)random(1,15);
    
  float STEP = 1.0f/STEPS;
  
  /* Circles
    for (float t = 0 ; t <= 1; t+=STEP) {
    float x = bezierPoint(rStartX,rStartC1X,rEndC1X,rEndX,t);
    float y = bezierPoint(rStartY,rStartC1Y,rEndC1Y,rEndY,t);
    float width = sin(PI*t)*thickness;
    ellipse(x,y,width,width);
    }
    
    for (float t = 0 ; t <= 1; t+=STEP) {
    float x = bezierPoint(lStartX,lStartC1X,lEndC1X,lEndX,t);
    float y = bezierPoint(lStartY,lStartC1Y,lEndC1Y,lEndY,t);
    float width = sin(PI*t)*thickness;
    ellipse(x,y,width,width);
    }
    */
    
  g.beginShape();
  for (float t = 0 ; t <= 1; t+=STEP) {
  float x = bezierPoint(rStartX,rStartC1X,rEndC1X,rEndX,t);
  float y = bezierPoint(rStartY,rStartC1Y,rEndC1Y,rEndY,t);
  float tx = bezierTangent(rStartX,rStartC1X,rEndC1X,rEndX,t);
  float ty = bezierTangent(rStartY,rStartC1Y,rEndC1Y,rEndY,t);
  float a = atan2(ty, tx);
  a -= HALF_PI;
  float height = startHeight - (sin(PI*t)*(startHeight)) ;
  g.vertex(  x, y,height);
}
g.endShape();



  g.beginShape();
  for (float t = 0 ; t <= 1; t+=STEP) {
  float x = bezierPoint(lStartX,lStartC1X,lEndC1X,lEndX,t);
  float y = bezierPoint(lStartY,lStartC1Y,lEndC1Y,lEndY,t);
  float tx = bezierTangent(lStartX,lStartC1X,lEndC1X,lEndX,t);
  float ty = bezierTangent(lStartY,lStartC1Y,lEndC1Y,lEndY,t);
  float a = atan2(ty, tx);
  a -= HALF_PI;
  float height = startHeight - (sin(PI*t)*(startHeight)) ;
  g.vertex( x, y,height);
}
g.endShape();

g.point(1, 150);  // home




  
}


float forkSpread = PI*0.75f;
float bezDetail = 3;
boolean OnlyCentreLines = false; 


public void generateForest() {
  background(255);
  smooth();
  noFill();
  float step = 100;
  for (float i = 0; i < width; i+=step) { 

    float lightLevel = i/width;
    branch(i, 290, -PI/2, lightLevel*150, 5, 0, lightLevel, g);
  }
}



public void generatePlant( float lightLevel, PGraphics g){
      branch(150,290, -PI/2, 150*lightLevel, (int)(6*lightLevel), 0, lightLevel, g);
      
      g.point(1, 150);
}

public void branch(float x1, float y1, float dir, float len, int forks, int itteration, float lightLevel, PGraphics g) {

  stroke(0);

  float bottomBendFactor = 1-lightLevel;//0.1f; //less light more bend
  float topBendFactor = 1-lightLevel;//0.1f; //less light more bend


  float offsetBottom = len /10.0f;
  float offsetTop = len / 20.0f;

  float leafThickness =  20;
  float stalkRatio = 0.25f;



  if (lightLevel < 0.5f)//no branches under 50% light
    forks=0;
  else
    forks--;



  boolean isFern = false;
  if (itteration == 0 && forks == 0) {
    isFern = true;
  }


  int leafOnBranch = 0;
  if (lightLevel < 0.5f)//put leaves on the stalk of plants under 50% light
    leafOnBranch = 10;

  float x2 = cos(dir)*len + x1;
  float y2 = sin(dir)*len + y1;

  float a = dir+random(-PI*bottomBendFactor, PI*bottomBendFactor);
  float cx1 = cos(a)*len/2 + x1;
  float cy1 = sin(a)*len/2 + y1;

  a = dir+PI+random(-PI*topBendFactor, PI*topBendFactor);
  float cx2 = cos(a)*len/2 + x2;
  float cy2 = sin(a)*len/2 + y2;

  float tx2 = bezierTangent(x1, cx1, cx2, x2, 1);
  float ty2 = bezierTangent(y1, cy1, cy2, y2, 1);
  float endTangent = atan2(ty2, tx2);

  dir = endTangent;

  /*
  stroke(255,0,0);
   line(x1,y1,cx1,cy1);
   ellipse(cx1,cy1,5,5);
   line(x2,y2,cx2,cy2);
   ellipse(cx2,cy2,5,5);
   stroke(0);
   */


float step = (bezDetail/len);

  float ribCounter = 0;
  for (int i = 0; i <=3 ; i+=1) {
    g.beginShape();
    for (float t = 0 ; t <= 1+step; t+=step) {
      float x = bezierPoint(x1, cx1, cx2, x2, t);
      float y = bezierPoint(y1, cy1, cy2, y2, t);
      float tx = bezierTangent(x1, cx1, cx2, x2, t);
      float ty = bezierTangent(y1, cy1, cy2, y2, t);
      a = atan2(ty, tx);
      a -= HALF_PI;


      //Stem
      if (i == 0 || i ==1) {
        if(OnlyCentreLines){
          if(i == 0){
           g.vertex(  x, y);
          }
        }else{
        float flip = i==1? 1:-1;
        float w = ((1-t)*offsetBottom)+((t)*offsetTop) ;
        g.vertex( cos(a)*flip*w + x, sin(a)*flip*w + y);
        }
      }




      //fern
      if (i==3 && isFern) {
        ribCounter+=1;
        if (  ribCounter == 10) {
          ribCounter = 0;
          float w = ((1-t)*offsetBottom)+((t)*offsetTop) ;
          float w2 = sin(PI*t)*(len/2) ;

          float flip = 1;
          leaf(cos(a)*flip*w + x, sin(a)*flip*w + y, a, 15, forks, itteration+1, lightLevel,g);
          flip = -1;
          leaf(cos(a)*flip*w + x, sin(a)*flip*w + y, a+PI, w2, forks, itteration+1, lightLevel,g);
        }
      }
    }
    g.endShape();
  }



  float forkDeviation = 0.1f;
  for (int i = 0; i < forks; i++) {
    float f = forks-1;

    if (f == 0)
      f = 1;
    a = (dir-(forkSpread/2.0f))+((forkSpread/(f))*i) + random(-PI*forkDeviation, PI*forkDeviation);

    branch(x2, y2, a, len*0.50f, forks, itteration+1, lightLevel,g);
  }


  if (forks == 0  && !isFern) {
    for (int i = 0; i < 1; i++) {
      a = dir;//(dir-(forkSpread/2.0f))+((forkSpread/(2-1))*i) + random(-PI*forkDeviation, PI*forkDeviation);
      leaf(x2, y2, a, 30, forks, itteration+1, lightLevel,g);
    }
  }
}



public void leaf(float x1, float y1, float dir, float len, int forks, int itteration, float lightLevel, PGraphics g) {

  
  
  float bottomBendFactor = 1-lightLevel;//0.1f; //less light more bend
  float topBendFactor = 1-lightLevel;//0.1f; //less light more bend

  float x2 = cos(dir)*len + x1;
  float y2 = sin(dir)*len + y1;

  float a = dir+random(-PI*bottomBendFactor, PI*bottomBendFactor);
  float cx1 = cos(a)*len/2 + x1;
  float cy1 = sin(a)*len/2 + y1;

  a = dir+PI+random(-PI*topBendFactor, PI*topBendFactor);
  float cx2 = cos(a)*len/2 + x2;
  float cy2 = sin(a)*len/2 + y2;

  float tx2 = bezierTangent(x1, cx1, cx2, x2, 1);
  float ty2 = bezierTangent(y1, cy1, cy2, y2, 1);
  float endTangent = atan2(ty2, tx2);

  dir = endTangent;
  float leafThickness = len/4;
  float stalkRatio = 0.25f;
  float offsetBottom = leafThickness/4;
  float offsetTop = 0;
  
  float step = (bezDetail/len);
    float ribCounter = 0;
  for (int i = 0; i <=4 ; i++) {
    g.beginShape();
    for (float t = 0 ; t <= 1+step; t+=step) {
      float x = bezierPoint(x1, cx1, cx2, x2, t);
      float y = bezierPoint(y1, cy1, cy2, y2, t);
      float tx = bezierTangent(x1, cx1, cx2, x2, t);
      float ty = bezierTangent(y1, cy1, cy2, y2, t);
      a = atan2(ty, tx);
      a -= HALF_PI;

//stem
if(i == 0 || i == 1){
  if(OnlyCentreLines){
    if(i == 0){
     g.vertex( x, y);
    }
    
  }else{
    
        if(i == 0){
     g.vertex( x, y);
    }
    /*
 float flip = i==0? 1:-1;
 
 float w = ((1-t)*offsetBottom)+((t)*offsetTop) ;
 g.vertex( cos(a)*flip*w + x, sin(a)*flip*w + y);
 */
  }

}


//outside of leaf
if(i == 2 || i == 3){
            float flip = i==2? 1:-1;
            float w = sin(PI*t)*(leafThickness) ;
            
            if (t < stalkRatio)
            w = ((1-t)*offsetBottom)+((t)*offsetTop) ;
            
            g.vertex( cos(a)*flip*w + x, sin(a)*flip*w + y);
          
}
          if (i == 4 && lightLevel > 0.5f) {
            ribCounter+=1;
            if (  ribCounter == 20) {
              ribCounter = 0;
              float w = sin(PI*t)*(leafThickness) ;

              if (t < stalkRatio)
                w =0;

              g.endShape();
              g.line(x, y, cos(a)*w + x, sin(a)*w + y);
              g.line(x, y, cos(a)*-w + x, sin(a)*-w + y);
              g.beginShape();
            }
          }
        }
                  g.endShape();

      }
}

    


  
  
  


int mazeRowCount = 4;
int mazeColCount = 4;

int pathWidth = 65;
int wallThickness = 8;
int paintDensity = 4;

int zeroX = 10;
int zeroY = 10;

Cell[] visitedCells = null;
int visitedCount = 0;
int cellCount = 0;

Cell map[][] = null;

public void initialize()
{
  visitedCount = 0;
  cellCount = 0;

  cellCount = mazeColCount * mazeRowCount;
  visitedCells = new Cell[cellCount];

  Cell cell;
  map = new Cell[mazeColCount][mazeRowCount];

  for (int y = 0; y<mazeRowCount; y++)
    for ( int x=0; x<mazeColCount; x++)
    {
      cell = new Cell();
      cell.x = zeroX + x*pathWidth;
      cell.y = zeroY + y*pathWidth;
      cell.col = x;
      cell.row = y;
      map[x][y] = cell;
    }
}

public void createMaze()
{
  initialize();
  initMaze(map[(int)random(mazeColCount)][(int)random(mazeRowCount)]);

  map[0][mazeRowCount-1].walls[Cell.BOTTOM] = Cell.NOWALL;
  map[mazeColCount-1][0].walls[Cell.TOP] = Cell.NOWALL;
}

public void initMaze(Cell theCell)
{

  if ( visitedCount == cellCount)
    return;

  if (!isVisited(theCell))
  {
    theCell.visited = true;
    visitedCells[visitedCount++] = theCell;
  }

  int neibCount = 0;
  Cell[] neighbours = new Cell[4];

  Cell nextCell;
  if (theCell.col-1>=0 && !(nextCell = map[theCell.col-1][theCell.row]).visited)
  {
    neighbours[neibCount++] = nextCell;
  }
  if (theCell.row-1>=0 && !(nextCell=map[theCell.col][theCell.row-1]).visited)
  {
    neighbours[neibCount++] = nextCell;
  }
  if (theCell.col+1<this.mazeColCount && !(nextCell = map[theCell.col+1][theCell.row]).visited)
  {
    neighbours[neibCount++] = nextCell;
  }
  if (theCell.row+1<this.mazeRowCount && !(nextCell = map[theCell.col][theCell.row+1]).visited)
  {
    neighbours[neibCount++] = nextCell;
  }
  if (neibCount ==0)
  {
    initMaze(visitedCells[(int)random(visitedCount)]);
    return;
  }

  nextCell = neighbours[(int)random(neibCount)];

  if (nextCell.col < theCell.col)
  {
    nextCell.walls[Cell.RIGHT] = Cell.NOWALL;
    theCell.walls[Cell.LEFT] = Cell.NOWALL;
  }

  else if (nextCell.row < theCell.row)
  {
    nextCell.walls[Cell.BOTTOM] = Cell.NOWALL;
    theCell.walls[Cell.TOP] = Cell.NOWALL;
  }
  // neighbour right

  else if (nextCell.col > theCell.col)
  {
    nextCell.walls[Cell.LEFT] = Cell.NOWALL;
    theCell.walls[Cell.RIGHT] = Cell.NOWALL;
  }
  // neighbour bottom
  else if (nextCell.row > theCell.row)
  {
    nextCell.walls[Cell.TOP] = Cell.NOWALL;
    theCell.walls[Cell.BOTTOM] = Cell.NOWALL;
  }

  initMaze(nextCell);
}

public boolean isVisited(Cell cell)
{
  if (visitedCount ==0)
    return false;

  for (int i=0; i<visitedCount; i++)
  {
    if (visitedCells[i] == cell)
      return true;
  }
  return false;
}

class Cell
{
  static final int LEFT = 0;
  static final int TOP = 1;
  static final int RIGHT = 2;
  static final int BOTTOM = 3;
  static final int NOWALL = 0;
  static final int HAVEWALL = 1;

  int state = 0;

  int x = 0;
  int y = 0;

  int col = 0;
  int row = 0;

  boolean visited = false;

  int[] walls = new int[] {
    HAVEWALL, HAVEWALL, HAVEWALL, HAVEWALL
  };

  public void draw(PGraphics g)
  {
    int x1 = 0, y1=0, x2=0, y2=0;
    int x = this.x, y = this.y;
    for ( int i=0;i<4;i++)
    {
      if (walls[i] == NOWALL)
      {
        switch(i)
        {
          // left
        case LEFT:
          //          line(x, y+wallThickness, x+wallThickness, y+wallThickness);
          //          line(x, y+pathWidth-wallThickness, x+wallThickness, y+pathWidth-wallThickness);
          //            for(int k=0; k<pathWidth-2*wallThickness; k+=paintDensity)
          //            {
          //              g.line(x, y+wallThickness+k, x+wallThickness, y+wallThickness+k);
          //            }
          //            break;
          // top
        case TOP:
          //          line(x+wallThickness, y, x+wallThickness, y+wallThickness);
          //          line(x+pathWidth-wallThickness, y, x+pathWidth-wallThickness, y+wallThickness);
          //            for(int k=0;k<wallThickness; k+=paintDensity)
          //            {
          //               g.line(x+wallThickness, y+k, x+pathWidth-wallThickness, y+k);
          //            }
          break;
          // right
        case RIGHT:
          //          line(x+pathWidth-wallThickness, y+wallThickness, x+pathWidth, y+wallThickness);
          //          line(x+pathWidth-wallThickness, y+pathWidth-wallThickness, x+pathWidth, y+pathWidth-wallThickness);
          for (int k=0; k<2*wallThickness; k+=paintDensity/2)
          {
            g.line(x+pathWidth-wallThickness+k, y+wallThickness, x+pathWidth-wallThickness+k, y+pathWidth-wallThickness);
          }
          break;
          // bottom
        case BOTTOM:
          //          line(x+wallThickness, y+pathWidth-wallThickness, x+wallThickness, y+pathWidth);
          //          line(x+pathWidth-wallThickness, y+pathWidth-wallThickness, x+pathWidth-wallThickness, y+pathWidth);
          for (int k=0;k<wallThickness*2; k+=paintDensity/2)
          {
            g.line(x+wallThickness, y+pathWidth-wallThickness+k, x+pathWidth-wallThickness, y+pathWidth-wallThickness+k);
          }
          break;
        }
      }

      else if (walls[i] == HAVEWALL)
      {
        switch(i)
        {
          // left
        case LEFT:
          x1 = this.x;
          y1 = this.y;
          x2 = x1;
          y2 = y1 + pathWidth;
          // line(x+wallThickness, y+wallThickness, x+wallThickness, y+pathWidth-wallThickness);
          break;
          // top
        case TOP:
          x1 = this.x;
          y1 = this.y;
          x2 = x1 + pathWidth;
          y2 = y1;
          //line(x+wallThickness, y+wallThickness, x+pathWidth-wallThickness, y+wallThickness);
          break;
          // right
        case RIGHT:
          x1 = this.x+pathWidth;
          y1 = this.y;
          x2 = x1;
          y2 = y1 + pathWidth;
          //line(x+pathWidth-wallThickness, y+wallThickness, x+pathWidth-wallThickness, y+pathWidth-wallThickness);
          break;
          // bottom
        case BOTTOM:
          x1 = this.x;
          y1 = this.y +pathWidth;
          x2 = x1 +pathWidth;
          y2 = y1;
          //line(x+wallThickness, y+pathWidth-wallThickness, x+pathWidth-wallThickness, y+pathWidth-wallThickness);
          break;
        }

        //line(x1, y1, x2, y2);
        g.beginShape();
        for (int k=0; k<9; k+=1)
        {
          g.vertex(x+wallThickness+k*3, y+wallThickness+k*3);
          g.vertex(x+wallThickness+k*3, y+pathWidth-wallThickness-k*3);
          g.vertex(x+pathWidth-wallThickness-k*3, y+pathWidth-wallThickness-k*3);
          g.vertex(x+pathWidth-wallThickness-k*3, y+wallThickness+k*3);
          
        }
        g.endShape();
      }
    }
  }
}

public void paintMaze(PGraphics g)
{
  for (int y=0; y<mazeRowCount; y++)
    for (int x=0; x<mazeColCount; x++)
      map[x][y].draw(g);

  int x = map[mazeColCount-1][0].x;
  int y = map[mazeColCount-1][0].y;
  for (int k=0;k<wallThickness; k+=paintDensity/2)
  {
    g.line(x+wallThickness, y+k, x+pathWidth-wallThickness, y+k);
  }
}


float centreX = 150;
float centreY = 150;

float rStartX = centreX;
float rStartY = centreY;

float rEndX = centreX;
float rEndY = centreY;

float rStartC1X = 150;
float rStartC1Y = 100;

float rEndC1X = 300;
float rEndC1Y = 250;


float lStartX = centreX;
float lStartY = centreY;

float lEndX = centreX;
float lEndY = centreY;

float lStartC1X = 150;
float lStartC1Y = 100;

float lEndC1X = 300;
float lEndC1Y = 250;



float STEPS = 50;
int WHISKERS = 10;
public void drawMustache(PGraphics g){
  
  calculateControlPoint();
  
 // bezier(rStartX,rStartY,rStartC1X,rStartC1Y,rEndC1X,rEndC1Y,rEndX,rEndY);
    
  //bezier(lStartX,lStartY,lStartC1X,lStartC1Y,lEndC1X,lEndC1Y,lEndX,lEndY);
  
   // randomSeed(analogRead(0));
    float thickness = random(5,40);
    WHISKERS = (int)random(1,15);
    
  float STEP = 1.0f/STEPS;
  
  /* Circles
    for (float t = 0 ; t <= 1; t+=STEP) {
    float x = bezierPoint(rStartX,rStartC1X,rEndC1X,rEndX,t);
    float y = bezierPoint(rStartY,rStartC1Y,rEndC1Y,rEndY,t);
    float width = sin(PI*t)*thickness;
    ellipse(x,y,width,width);
    }
    
    for (float t = 0 ; t <= 1; t+=STEP) {
    float x = bezierPoint(lStartX,lStartC1X,lEndC1X,lEndX,t);
    float y = bezierPoint(lStartY,lStartC1Y,lEndC1Y,lEndY,t);
    float width = sin(PI*t)*thickness;
    ellipse(x,y,width,width);
    }
    */
    
  for(int i = 0; i < WHISKERS ; i++){
  g.beginShape();
  for (float t = 0 ; t <= 1; t+=STEP) {
  float x = bezierPoint(rStartX,rStartC1X,rEndC1X,rEndX,t);
  float y = bezierPoint(rStartY,rStartC1Y,rEndC1Y,rEndY,t);
  float tx = bezierTangent(rStartX,rStartC1X,rEndC1X,rEndX,t);
  float ty = bezierTangent(rStartY,rStartC1Y,rEndC1Y,rEndY,t);
  float a = atan2(ty, tx);
  a -= HALF_PI;
  float width = sin(PI*t)*(i-(WHISKERS/2.0f))*(thickness/WHISKERS) ;
  g.vertex( cos(a)*width + x, sin(a)*width + y);
}
g.endShape();
}


  for(int i = 0; i < WHISKERS ; i++){
  g.beginShape();
  for (float t = 0 ; t <= 1; t+=STEP) {
  float x = bezierPoint(lStartX,lStartC1X,lEndC1X,lEndX,t);
  float y = bezierPoint(lStartY,lStartC1Y,lEndC1Y,lEndY,t);
  float tx = bezierTangent(lStartX,lStartC1X,lEndC1X,lEndX,t);
  float ty = bezierTangent(lStartY,lStartC1Y,lEndC1Y,lEndY,t);
  float a = atan2(ty, tx);
  a -= HALF_PI;
  float width = sin(PI*t)*(i-(WHISKERS/2.0f))*(thickness/WHISKERS) ;
  g.vertex( cos(a)*-width + x, sin(a)*-width + y);
}
g.endShape();
}



  
}

public void calculateControlPoint(){
//  randomSeed(analogRead(0));
  
  lEndX =  centreX - random(50,150.0f);
  lEndY =  random(70,230);//centreY - random(200.0f)+100.0f;
  
  lStartC1X = lStartX - (random(100.0f));
  lStartC1Y = lStartY - (random(200.0f)-100.0f);
  
  lEndC1X = lEndX - min((random(300.0f)-150.0f),5);
  lEndC1Y = lEndY - (random(300.0f)-150.0f);
  
  
  
  rEndX = (centreX -lEndX)+centreX;
  rEndY = lEndY;
  
  rStartC1X = (centreX -lStartC1X)+centreX;
  rStartC1Y = lStartC1Y;
  
  rEndC1X = (centreX -lEndC1X)+centreX;
  rEndC1Y = lEndC1Y;
  
  
}





class PlotiWriter extends PGraphics {

  PVector beginShapePos = null;
  boolean closeShape = false;

  boolean penUp = true;

  List instructionBuffer = new ArrayList();

  //should we flattern curves?
  boolean flattern = true;
  float flatRes = 1.0f;

  //Previous Vector
  PVector pVertex = new PVector(0, 0, 0);

  //simulation stuff
  int simStep = 0;
  PVector simPrev = null;

  public void vertex(float x, float y) {

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
    super.vertex(x, y);
  }


  public void vertex(float x, float y, float z) {

    if (closeShape) {
      beginShapePos = new PVector(x, y, 0);
      closeShape = false;
    }

    if (pVertex.dist(new PVector(x, y, z)) >= flatRes) {
      stepTo(x, y, z);

      pVertex.x = x;
      pVertex.y = y;
      pVertex.z = y;
    }
    super.vertex(x, y);
  }

  /*
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
    println("END SHAPE");
    if (beginShapePos != null) {
      vertex(beginShapePos.x, beginShapePos.y);
      beginShapePos = null;
    }
    penUp = true;
    stepTo(pVertex.x, pVertex.y, penLiftHeight);
    super.endShape();
  }




  public void stepTo(float x, float y) {

    // float xMapped = map(x, 0, bedWidth, servoMinRotationX, servoMaxRotationX);
    //float yMapped = map(y, 0, bedHeight, servoMinRotationY, servoMaxRotationY);

    /*
    if (xMapped < servoMinRotationX || xMapped > servoMaxRotationX || yMapped < servoMinRotationY || yMapped > servoMaxRotationY)
     return;
     */

    if (flipX)
      x = map(x, 0, bedWidth, bedWidth, 0);

    if (flipY)
      y = map(y, 0, bedHeight, bedHeight, 0);

    codeStack.add(new PVector(x, y, 0));

    numLines++;
    /*
    codeStack.add("delay("+getStepDelay()+");");
     codeStack.add("x.write("+xMapped+");");
     codeStack.add("y.write("+yMapped+");");
     */
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
      x = map(x, 0, bedWidth, bedWidth, 0);

    if (flipY)
      y = map(y, 0, bedHeight, bedHeight, 0);

    if (flipZ)
      z = map(z, 0, bedDepth, bedDepth, 0);
      
      x = constrain(x,0,bedWidth);
      y = constrain(y,0,bedHeight);
      z = constrain(z,0,bedDepth);


    codeStack.add(new PVector(x, y, z));
    numLines++;
  }

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

  public void debugDraw(PGraphics g) {

    PVector prev = null;

    for (int i = 0; i < codeStack.size(); i ++) {
      PVector p = (PVector)codeStack.get(i);

      if (prev != null) {
        if (p.z > penLiftHeight )//!= 0 || prev.z != 0)
          g.stroke(50);
        else
          g.stroke(220);

        g.line(p.x, p.y, prev.x, prev.y);
      }

      prev = p;
    }
  }


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
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Controllo" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
