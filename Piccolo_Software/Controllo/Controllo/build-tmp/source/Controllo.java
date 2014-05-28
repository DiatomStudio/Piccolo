import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import controlP5.*; 
import processing.serial.*; 
import java.awt.FileDialog; 
import geomerative.*; 
import java.util.*; 
import javax.swing.JOptionPane; 
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

/*
Controllo 
by: Diatom Studio , 2013

This program controls and sends drawing commands to a Piccolo teathered by a usb cable. 
All coodinates are sent in pixels/mm and are scaled for drawing to the screen. 
Coordinates are centred around XYZ at 0,0,0.
*/

/*
Notes:
- Should we separate plotsiWriter to it's own library?
- Could Controllo be a example in this library?
- Can we make seperate sketches / apps for generative drawings etc so that we can keep controllo clean and simple?
- If so how would a exhibition setup work? webpage to launch diff apps.
*/

//Libraries







//debug output all serial communications to the console for debugging. 
boolean debug = true; 
boolean serialConnected = false;

boolean view3D = false; // display view in 3D

//Piccolo send coordinate settings, all false by default
//TODO: These should be reflected in GUI
boolean flipX = false; 
boolean flipY = false;
boolean flipZ = false;
boolean rotateBed = false;


//Piccolo bed size in mm
float bedWidth = 50.0f; 
float bedHeight = 50.0f; 
float bedDepth = 50.0f; 

float bedRenderWidth = 300;

//current position of drawing command to send
float xPos = 0;        
float yPos = 0;      
float zPos = 0;  


boolean fitSVGtoBed = true;


//the height to lift the pen between 2d shapes
float penDownHeight = -bedDepth/2; // change using slider
float penLiftHeight = penDownHeight+2; //change using slider

//TODO: scale render not output. 
//TODO: resize SVG to fit bed.
// scale pixels to Piccolo coordinates
float scaleOutput = 0.16666666666667f; 

//TODO: this should be removed or moved to make code more straight forward.
//sensor sets
public float lightLevel = 0.5f;

PShape loadedSVG; //svg loaded for sending to piccolo.
/*
Currently loaded SVG is shown on screen until user presses start. 
At this point the SVG is loaded into the output canvas and sent to Piccolo. 
It might be a better idea to always load drawing shapes directly into the output 
canvas so that they always reflect what Piccolo is drawing. 
*/

boolean sendPenHeight = false;

List path = new ArrayList();

PlotsiWriter writer = new PlotsiWriter(bedWidth,bedHeight,bedDepth);
ControlP5 controlP5;
CheckBox drawPlotsiOutput;
Knob pressureKnob;

PGraphics plotsiOutputCanvas;  //all lines drawn to this canvas will be sent to Piccolo


public void setup() {
  size(500, 340,P3D);
  plotsiOutputCanvas = createGraphics((int)bedWidth, (int)bedHeight);
  plotsiOutputCanvas.beginDraw();
  plotsiOutputCanvas.smooth();
  plotsiOutputCanvas.endDraw();

  //canvas defaults
  writer.setStepRes(1f);
  writer.bezierDetail(20); 

  //setup GUI
  controlP5 = new ControlP5(this);
  drawInterface();

  String s = (String) JOptionPane.showInputDialog(
      null,
      "Select Piccolo's COM Port",
      "Select Piccolo",
      JOptionPane.PLAIN_MESSAGE,
      null,
      Serial.list(),
      Serial.list()[Serial.list().length-1]
  );

  println(s);

  // List all the available serial ports
  //TODO: select serial port at this point. 
try{
  writer.serial = new Serial(this, s, 115200);
  writer.serial.bufferUntil('\n');
  serialConnected = true;
}catch(Exception e){
  serialConnected = false;

}
  

  // Initialise Geomerative for working with type.
  RG.init(this);
  delay(200);
  home();
}



public void draw() {

  background(255, 255, 255);
  ortho(0, width, 0, height); // same as ortho()
  pushMatrix();
  translate((bedRenderWidth/2) + 150, (bedRenderWidth/2)+20,0);
  

  if(view3D){
    rotateX(PI/4.0f);
    rotateZ(PI/4.0f);
    translate(0,0,(bedRenderWidth/2));
   }

  stroke(0);
  noFill();

  pushMatrix();
  translate(0,0,-(bedRenderWidth/2));
  rect(-(bedRenderWidth/2),-(bedRenderWidth/2),bedRenderWidth,bedRenderWidth);
  popMatrix();

  writer.draw(g,bedRenderWidth);
  popMatrix();
  writer.serialLoop();
}




public void keyPressed(){
  if(key == '3')
    view3D = true;

    if(key == '2')
    view3D = false;
}

public void drawInterface() {

  drawPlotsiOutput = controlP5.addCheckBox("draw output", 100, 100);  
  controlP5.addButton("load_SVG", 0, 10, 10, 80, 19);
  controlP5.addSlider("lightLevel", 0, 1.0f, 0.5f, 10, 33, 80, 10);
  controlP5.addButton("generate_tree", 0, 10, 50, 80, 19);
  controlP5.addButton("generate_mustache", 0, 10, 70, 80, 19);
  //  controlP5.addButton("brush_Mustache", 0, 10, 120, 80, 19);
  controlP5.addButton("bezier", 0, 10, 90, 80, 19);  
  controlP5.addButton("logo", 0, 10, 110, 80, 19);
  //  controlP5.addButton("maze", 0, 10, 180, 80, 19);
  controlP5.addButton("circles", 0, 10, 130, 80, 19);
  controlP5.addButton("boxes", 0, 10, 150, 80, 19);
  controlP5.addButton("diagonals", 0, 10, 170, 80, 19);
  controlP5.addButton("word", 0, 10, 190, 80, 19);

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

  writer.establishContact();
}

public void penUp(int val) {
  writer.clear(); 
  writer.vertex(0 , 0, penLiftHeight);
  writer.vertex(0, 0, penLiftHeight);

  writer.establishContact();
}

public void penDown(int val) {
  writer.clear(); 
  writer.vertex(0, 0, 0);
  writer.vertex(0, 0, 0);

  writer.establishContact();
}


public void load_SVG(int val) {

  FileDialog fd = new FileDialog(frame, "open", 
  FileDialog.LOAD);
  String currentDir = new File(".").getAbsolutePath();


  fd.setLocation(50, 50);
  fd.pack();
  fd.show();

  if (fd.getName() != null) {

    String filename = fd.getFile();
    clearCanvas();
    PShape svg = loadShape(fd.getDirectory() + filename);
    svg.disableStyle();
    writer.beginDraw();
    writer.clear();
    writer.pushMatrix();

    if(fitSVGtoBed)
    writer.scale(bedWidth / max(svg.width,svg.height) );
    else
    writer.scale(0.16666666666667f);

    writer.translate(-(bedWidth/2.0f),-(bedHeight/2.0f),0.0f);
    writer.shape(svg,0,0);
    writer.popMatrix();
    writer.endDraw();

    //loadedSVG.scale(0.6); 
    //
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
  writer.stepTo(0, 0, penDownHeight);
  writer.establishContact();
  clearCanvas();
}

public void down_() {
  penDownHeight-=5;
  println(penDownHeight);
  penLiftHeight = penDownHeight+40;
  writer.clear();
  writer.stepTo(0, 0, penDownHeight);
  writer.establishContact();
  clearCanvas();
}

public void home() {
  writer.clear();
  writer.stepTo(0, 0, penLiftHeight);
  writer.establishContact();
 // clearCanvas();
}


public void stop(){
 writer.clear();
}


public void Up(int val) {
  println("Up");
  writer.clear();
  writer.establishContact();
  if (zPos<90)
  {
    zPos+=10;
    writer.stepTo(xPos, yPos, zPos);
  }
  print("myZ:");
  println(xPos);
  println(yPos);
  println(zPos);
}

public void Down(int val) {
  writer.clear();
  writer.establishContact();
  if (zPos>10)
  {
    zPos-=10;
    writer.stepTo(xPos, yPos, zPos);
  }
  print("myZ:");
  println(xPos);
  println(yPos);
  println(zPos);
}



// =========================================================== //

public void TicTacToe() {
  writer.clear();
  clearCanvas();
  drawGrid(writer);
}

public void generate_tree() {
  writer.clear();
  clearCanvas();
  generatePlant( lightLevel, writer);
}

public void generate_mustache() {
  writer.clear();
  clearCanvas();
  drawMustache(writer);
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
}

public void boxes() {
  writer.clear();
  clearCanvas();
  drawBoxes(writer);
}

public void diagonals() {
  writer.clear();
  clearCanvas();
  drawDiagonals(writer);
}

public void word() {
  writer.clear();
  clearCanvas();
  drawWord(writer);
}

public void bezier(){
  writer.clear();
  clearCanvas();
  drawBezier(writer);

}







//TODO: maybe this can be moved to it's own seperate sketch

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
//TODO: add paramaters to all commands 



public void drawBezier(PGraphics g){

PVector p1 = new PVector(-25,-25,0);
PVector p2 = new PVector(25,25,0);
PVector c1 = new PVector(25,-25,0);
PVector c2 = new PVector(25,-25,0);


for(int i = 0; i < 25; i+=2){
  c1.x = i; c1.y = -i;
  c2.x = i; c2.y = -i;
  g.bezier(p1.x, p1.y,c1.x,c1.y,c2.x,c2.y, p2.x, p2.y);
}

}
public void drawCircles(PGraphics g) {

  int o = 0;
  int r = 25; //radius
  int a = 1; 
  int b = 100;
  int startRad = 5;

  float x;
  float y; 

  g.beginShape();
  for (int i = startRad; i <= r; i += a) {
    for (float j=TWO_PI; j>0; j-= TWO_PI/(i*10)) {
      x = (i-(a*(j/TWO_PI)))*sin(j)+o;
      y = (i-(a*(j/TWO_PI)))*cos(j)+o;
      g.vertex(x, y);
    }
  }
  g.endShape();
}



public void drawSpiral(PGraphics g){
  
  
}

public void drawBoxes(PGraphics g) {

  int w = 25; //half box width
  int a = 1; //step size

  g.beginShape();
  for (int i = w; i > 1; i -= a) {
    g.vertex(+i, +i);
    g.vertex(+i, -i);
    g.vertex(-i, -i);
    g.vertex(-i, +i);
    g.vertex(+i, +i);
  }
  g.endShape();
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





/*
PlotsiWriter
____________
PlotsiWriter extends the Processing PGraphics, this means that you can use any of the processing drawing commands, ellipse, line, rect etc to send to Piccolo. 
PlotsiWriter will currently only send stroke commands and does not support fills. 
*/
int CHAR_PER_POS = 8;

class PlotsiWriter extends PGraphics {


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
  
  float scale = 1.0f;
  float translateX = 0.0f;
  float translateY = 0.0f;
  float translateZ = 0.0f;


/*Serial Commands
*/

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
  
public @Override
 void popMatrix(){
  scale = 1.0f; 
  translateX = 0;
  translateY = 0;
  translateZ = 0;
 }
public @Override
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
      x = map(x, -(bedWidth/2.0f), (bedWidth/2.0f), (bedWidth/2.0f), -(bedWidth/2.0f));

    if (flipY)
      y = map(y, -(bedHeight/2.0f), (bedHeight/2.0f), (bedHeight/2.0f), -(bedHeight/2.0f));

    if (flipZ)
      z = map(z, -(bedDepth/2.0f), (bedDepth/2.0f), (bedDepth/2.0f), -(bedDepth/2.0f));
      

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
  println("Draw line {x:" + p.x*scale + " y:"  + p.y*scale + " z:"+ p.z*scale + "} {x:" + prev.x*scale + " y:" +prev.y*scale + " z:" + prev.z*scale + " }" );


        g.line(p.x*scale, p.y*scale,p.z*scale, prev.x*scale, prev.y*scale,prev.z*scale);
      }

      prev = p;
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


    public void establishContact() {
      //S = get ready to send 
      println("establishContact");
      println(writer.codeStack.size() + " lines to send");
      if(serialConnected){
      serial.clear();
      serial.write('S');
    }
      lineCount = 0;
    }



  

  public void serialLoop(){
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


      int xScaled = (int)(x*100.0f);
      int yScaled = (int)(y*100.0f);
      int zScaled = (int)(z*100.0f);


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

  public void sendInt(int i){
      serial.write((byte)i); // X
      serial.write((byte)(i>>8)); // X
      serial.write((byte)(i>>16)); // X
      serial.write((byte)(i>>24)); // X
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
