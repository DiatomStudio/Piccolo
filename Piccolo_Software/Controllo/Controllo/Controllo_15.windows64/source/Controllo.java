import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import piccoloP5.*; 
import controlP5.*; 
import processing.serial.*; 
import java.awt.FileDialog; 
import geomerative.*; 
import java.util.*; 
import javax.swing.JOptionPane; 

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
- Should we separate plotsipiccolo. to it's own library?
- Could Controllo be a example in this library?
- Can we make seperate sketches / apps for generative drawings etc so that we can keep controllo clean and simple?
- If so how would a exhibition setup work? webpage to launch diff apps.
*/

//Libraries










boolean view3D = false; // display view in 3D


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

public boolean drawTool = false;

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

List path = new ArrayList();

PiccoloP5 piccolo = new PiccoloP5(bedWidth,bedHeight,bedDepth);
ControlP5 controlP5;
CheckBox drawPlotsiOutput;
Knob pressureKnob;

PGraphics plotsiOutputCanvas; 







public void setup() {
  size(500, 340,P3D);
  plotsiOutputCanvas = createGraphics((int)bedWidth, (int)bedHeight);
  plotsiOutputCanvas.beginDraw();
  plotsiOutputCanvas.smooth();
  plotsiOutputCanvas.endDraw();

  //canvas defaults
  piccolo.setStepRes(1f);
  piccolo.bezierDetail(20); 
  piccolo.rotate(PI/2.0f);

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
  piccolo.serial = new Serial(this, s, 115200);
  piccolo.serial.bufferUntil('\n');
  piccolo.serialConnected = true;
}catch(Exception e){
  piccolo.serialConnected = false;

}
  

  // Initialise Geomerative for working with type.
  RG.init(this);
  delay(200);
  home();
}

//---------------------------- draw functions --------------------------------//

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

  rotate(-PI/2.0f);
  piccolo.draw(g,bedRenderWidth);
  popMatrix();
  piccolo.update();
}



public void mousePressed(){
  if(drawTool && mouseX > 150 && mouseX < 450 && mouseY > 20 && mouseY < 320){
    piccolo.beginShape();
  }
}


public void mouseDragged(){
  if(drawTool && mouseX > 150 && mouseX < 450 && mouseY > 20 && mouseY < 320){
    
      float posX = (((mouseX - 150)/bedRenderWidth) * piccolo.bedWidth) - piccolo.bedWidth/2.0f;
     float posY = (((mouseY - 20)/bedRenderWidth)*piccolo.bedHeight) - piccolo.bedHeight/2.0f;


    piccolo.vertex(posX,posY);
  }
}

public void mouseReleased(){
    if(drawTool){
    piccolo.endShape();
    }
}



public void keyPressed(){
  if(key == '3')
    view3D = true;

    if(key == '2')
    view3D = false;
}



public void penUp(int val) {
  piccolo.clear(); 
  piccolo.vertex(0 , 0, piccolo.getPenLiftHeight());
  piccolo.vertex(0, 0, piccolo.getPenLiftHeight());

  piccolo.start();
}

public void penDown(int val) {
  piccolo.clear(); 
  piccolo.vertex(0, 0, 0);
  piccolo.vertex(0, 0, 0);

  piccolo.start();
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
    piccolo.beginDraw();
    piccolo.clear();
    piccolo.pushMatrix();

    float scaleSVG;

    if(fitSVGtoBed)
     scaleSVG = bedWidth / max(svg.width,svg.height);
    else
    scaleSVG = 0.16666666666667f;
    
        piccolo.translate(-(piccolo.bedWidth/2.0f),-(piccolo.bedHeight/2.0f),0.0f);

    piccolo.scale(scaleSVG);

    piccolo.shape(svg,0,0);
    piccolo.popMatrix();
    piccolo.endDraw();

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

/*

void up_() {
  piccolo.penDownHeight+=5;
  piccolo.penLiftHeight = piccolo.penDownHeight+40;
  piccolo.clear();
  piccolo.stepTo(0, 0, piccolo.penDownHeight);
  piccolo.establishContact();
  clearCanvas();
}

void down_() {
  piccolo.penDownHeight-=5;
  println(piccolo.penDownHeight);
  piccolo.penLiftHeight = piccolo.penDownHeight+40;
  piccolo.clear();
  piccolo.stepTo(0, 0, piccolo.penDownHeight);
  piccolo.establishContact();
  clearCanvas();
}
*/
public void home() {
  piccolo.clear();
  piccolo.stepTo(0, 0, piccolo.getPenLiftHeight());
  piccolo.start();
 // clearCanvas();
}

  
public void start(int val) {
  piccolo.start();
}


public void stop(){
 piccolo.clear();
}


public void Up(int val) {
  println("Up");
  piccolo.clear();
  piccolo.start();
  if (zPos<90)
  {
    zPos+=10;
    piccolo.stepTo(xPos, yPos, zPos);
  }
  print("myZ:");
  println(xPos);
  println(yPos);
  println(zPos);
}

public void Down(int val) {
  piccolo.clear();
  piccolo.start();
  if (zPos>10)
  {
    zPos-=10;
    piccolo.stepTo(xPos, yPos, zPos);
  }
  print("myZ:");
  println(xPos);
  println(yPos);
  println(zPos);
}



// =========================================================== //


public void logo(int val) {

    PShape svg = loadShape("logo.svg");
    svg.disableStyle();
    piccolo.beginDraw();
    piccolo.clear();
    piccolo.pushMatrix();

    float scaleSVG;

    if(fitSVGtoBed)
     scaleSVG = bedWidth / max(svg.width,svg.height);
    else
    scaleSVG = 0.16666666666667f;
    
        piccolo.translate(-(piccolo.bedWidth/2.0f),-(piccolo.bedHeight/2.0f),0.0f);

    piccolo.scale(scaleSVG);

    piccolo.shape(svg,0,0);
    piccolo.popMatrix();
    piccolo.endDraw();
    
    
}

public void circles() {
  piccolo.clear();
  clearCanvas();
  drawCircles(piccolo);
}

public void boxes() {
  piccolo.clear();
  clearCanvas();
  drawBoxes(piccolo);
}

public void diagonals() {
  piccolo.clear();
  clearCanvas();
  drawDiagonals(piccolo);
}

public void word() {
  piccolo.clear();
  clearCanvas();
  drawWord(piccolo);
}

public void bezier(){
  piccolo.clear();
  clearCanvas();
  drawBezier(piccolo);

}







//TODO: add paramaters to all commands 



public void drawBezier(PGraphics g){

PVector p1 = new PVector(-25,-25,0);
PVector p2 = new PVector(25,25,0);
PVector c1 = new PVector(25,-25,0);
PVector c2 = new PVector(25,-25,0);


for(int i = 25; i > -25 ; i-=2){
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
  RG.setPolygonizerLength(0.3f);
  
  points1 = grp1.getPoints();
  points2 = grp2.getPoints();
  points3 = grp3.getPoints();  
  points4 = grp4.getPoints();
  
  g.pushMatrix();
    g.translate(-10,-10);
  g.scale(0.1f);
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
  g.popMatrix();
  
  
  
  
}



public void drawInterface() {

  drawPlotsiOutput = controlP5.addCheckBox("draw output", 100, 100);  
  
    controlP5.addToggle("draw")
     .setPosition(10,10)
     .setSize(80,19)
     .plugTo(this,"drawTool")
     ;
   
     controlP5.addTextlabel("DRAW")
     .setText("DRAW")
     .setPosition(10,14);



  controlP5.addButton("load_SVG", 0, 10, 30, 80, 19);
  


     
     
     
     //  controlP5.addButton("brush_Mustache", 0, 10, 120, 80, 19);
  controlP5.addButton("bezier", 0, 10, 90, 80, 19);  
  controlP5.addButton("logo", 0, 10, 110, 80, 19);
  //  controlP5.addButton("maze", 0, 10, 180, 80, 19);
  controlP5.addButton("circles", 0, 10, 130, 80, 19);
  controlP5.addButton("boxes", 0, 10, 150, 80, 19);
  controlP5.addButton("diagonals", 0, 10, 170, 80, 19);
  controlP5.addButton("word", 0, 10, 190, 80, 19);

/*
  pressureKnob = controlP5.addKnob("pressure")
    .setRange(0, bedDepth)
      .setValue(piccolo.getPenDownHeight())
        .setPosition(10, 220)
          .setRadius(15)
            .setViewStyle(Knob.ARC)
              .setDragDirection(Knob.VERTICAL)
                ;
  controlP5.addButton("up_", 0, 50, 223, 30, 10);
  controlP5.addButton("down_", 0, 50, 237, 30, 10);
*/
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

    if (piccolo.flipX)
      posIn[1] = map(posIn[1], 0, bedWidth, bedWidth, 0);

    if (piccolo.flipY)
      posIn[2] = map(posIn[2], 0, bedHeight, bedHeight, 0);

    //draw directly to the screen
    plotsiOutputCanvas.line(xPos, yPos, posIn[1], posIn[2]);
    xPos = posIn[1];
    yPos = posIn[2];
    zPos = posIn[3];
  } 
  plotsiOutputCanvas.endDraw();
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
