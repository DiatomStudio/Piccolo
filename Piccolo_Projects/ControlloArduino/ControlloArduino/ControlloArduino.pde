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
import piccoloP5.*;
import controlP5.*;
import java.util.*;


boolean view3D = false; // display view in 3D


//Piccolo bed size in mm
float bedWidth = 50.0; 
float bedHeight = 50.0; 
float bedDepth = 50.0; 
float bedRenderWidth = 600;
float bedRenderHeight = 600;
PVector bedRenderStart = new PVector(250,50,0);


//current position of drawing command to send
float xPos = 0;          
float yPos = 0;      
float zPos = 0;  

boolean fitSVGtoBed = true;

public boolean drawTool = false;

//TODO: this should be removed or moved to make code more straight forward.
//sensor sets
public float lightLevel = 0.5;

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

PVector prevDrawPoint = new PVector(0,0,0);





void setup() {

 // size(displayWidth, displayHeight,OPENGL);
  // orientation(LANDSCAPE);  // the hot dog way


  plotsiOutputCanvas = createGraphics((int)bedWidth, (int)bedHeight);
  plotsiOutputCanvas.beginDraw();
  plotsiOutputCanvas.smooth();
  plotsiOutputCanvas.endDraw();

  //canvas defaults
  piccolo.setStepRes(1f);
  piccolo.bezierDetail(20); 
  piccolo.rotate(PI/2.0f);
  piccolo.useHttpConnection("https://agent.electricimp.com/YfD2LsizYsy9"); ///use HTTP streaming.

  //setup GUI
  controlP5 = new ControlP5(this);
  drawInterface();

/*
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
  */

  // Initialise Geomerative for working with type.

  delay(200);
  home();
}

//---------------------------- draw functions --------------------------------//

void draw() {

  
  background(255, 255, 255);
 // ortho(0, width, 0, height); // same as ortho()
  pushMatrix();
  translate((bedRenderWidth/2) + bedRenderStart.x, (bedRenderWidth/2)+bedRenderStart.y);
  

  if(view3D){
    rotateX(PI/4.0);
    rotateZ(PI/4.0);
    translate(0,0,(bedRenderWidth/2));
   }

  stroke(0);
  noFill();

  pushMatrix();
  translate(0,0);
  rect(-(bedRenderWidth/2),-(bedRenderWidth/2),bedRenderWidth,bedRenderHeight);
  popMatrix();

  rotate(-PI/2.0f);
  piccolo.draw(g,bedRenderWidth);
  popMatrix();
  piccolo.update();
}



void mousePressed(){
  if(drawTool && mouseX > bedRenderStart.x && mouseX < bedRenderStart.x+bedRenderWidth && mouseY > bedRenderStart.y && mouseY < bedRenderStart.y+bedRenderHeight){
    piccolo.beginShape();
  }
}


void mouseDragged(){
  if(drawTool && mouseX > bedRenderStart.x && mouseX < bedRenderStart.x+bedRenderWidth && mouseY > bedRenderStart.y && mouseY < bedRenderStart.y+bedRenderHeight){
    
      float posX = (((mouseX - bedRenderStart.x)/bedRenderWidth) * piccolo.bedWidth) - piccolo.bedWidth/2.0f;
     float posY = (((mouseY - bedRenderStart.y)/bedRenderHeight)*piccolo.bedHeight) - piccolo.bedHeight/2.0f;


if(prevDrawPoint.dist(new PVector(posX,posY)) > 2){
    piccolo.vertex(posX,posY);
    prevDrawPoint = new PVector(posX,posY);
}

  }
  
 




}

void mouseReleased(){
    if(drawTool){
    piccolo.endShape();
    }
}



void keyPressed(){
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
/*
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
    scaleSVG = 0.16666666666667;
    
        piccolo.translate(-(piccolo.bedWidth/2.0),-(piccolo.bedHeight/2.0),0.0);

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
  */
}




void clearCanvas() {
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
void home() {
  piccolo.clear();
  piccolo.stepTo(0, 0, piccolo.getPenLiftHeight());
  piccolo.start();
 // clearCanvas();
}

  
public void start(int val) {
  piccolo.start();
}


void stop(){
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
    scaleSVG = 0.16666666666667;
    
        piccolo.translate(-(piccolo.bedWidth/2.0),-(piccolo.bedHeight/2.0),0.0);

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

  public String sketchRenderer() {
    return P3D; 
  }





