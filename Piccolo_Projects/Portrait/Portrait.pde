import piccoloP5.*;
import controlP5.*;


ControlP5 cp5;

import processing.serial.*;
import java.awt.FileDialog;
import java.util.*;
import javax.swing.JOptionPane;


float drawHeight = 50; 


//Piccolo bed size in mm
float bedWidth = 50.0; 
float bedHeight = 50.0; 
float bedDepth = 50.0; 


PGraphics occlusionCanvas = null;;
ControlFrame cf;
CaptureFrame captureFrame = null;



public Face face = new Face();
PiccoloP5 piccolo = new PiccoloP5(bedWidth,bedHeight,bedDepth);



boolean startPlot = false;


void setup(){
  size(300,300);
  cp5 = new ControlP5(this);
  

     




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

piccolo.rotate(PI + (PI/2));






  Frame f = new Frame("capture");
  CaptureFrame p = new CaptureFrame(this, 320, 240);
  f.add(p);
  p.init();
  f.setTitle("capture");
  f.setSize(p.w, p.h);
  f.setLocation(500, 100);
  f.setResizable(true);
  f.setVisible(true);
  captureFrame = p;
  
  
  
  
cf = addControlFrame("extra", 340,640);




  

}



void draw(){

  piccolo.update();
  
  
  
  if(piccolo.startButtonPressed){
  face.randomFace();
  piccolo.startButtonPressed = false; 
  
  startPlot = true;
  }
  
  
  background(255);
  
  
  //grid
  stroke(100);
  strokeWeight(0.1);
  line(width/2,0, width/2,height);
  line(0, height/2, width,height/2);

  noFill();
  

  stroke(0);
  strokeWeight(1);
  pushMatrix();
  translate(width/2, height/2);
  scale(height/drawHeight);  
  
  g.strokeWeight(0.5f);
  g.fill(255);
    
  face.draw(g);
  
  
 if(keyPressed && key =='d'){
   pushMatrix();
   rotate(-PI/2);
    g.stroke(255,0,0);
  piccolo.drawCodeStack(g);
  popMatrix();
  
  }
  popMatrix();
  
  

  
 // if(occlusionCanvas != null)
  //image(occlusionCanvas,0,0);
  
  if(keyPressed){
    
   if(key == 'm'){
    
  }}
  
 
 
 if( startPlot){
   startPlot = false;
 startSend();
 }

}

void keyPressed(){
  
  if(key == 'e')
  face.eyesOpen = !face.eyesOpen;
  
  if(key == 'g')
  face.hasGlasses = !face.hasGlasses;
  
    if(key == 'u'){
     if(cp5.isVisible())
     cp5.hide();
     else
     cp5.show();
    }
    
  
}



void startSend(){
    piccolo.clear();
    
    occlusionCanvas = createGraphics(300,300);
    occlusionCanvas.beginDraw();
    occlusionCanvas.strokeWeight(0.5f);
    occlusionCanvas.fill(255);
    occlusionCanvas.background(255);
    occlusionCanvas.pushMatrix();
    occlusionCanvas.translate(occlusionCanvas.width/2.0f, occlusionCanvas.height/2.0f);
    occlusionCanvas.rotate(PI + (PI/2));    
    occlusionCanvas.scale( occlusionCanvas.width/piccolo.bedWidth);
    face.draw(occlusionCanvas);
    occlusionCanvas.popMatrix();
    occlusionCanvas.endDraw();
    
    face.draw(piccolo);
    piccolo.removeOcclusions(occlusionCanvas);
    
    piccolo.start();
}


ControlFrame addControlFrame(String theName, int theWidth, int theHeight) {
  Frame f = new Frame(theName);
  ControlFrame p = new ControlFrame(this, theWidth, theHeight);
  f.add(p);
  p.init();
  f.setTitle(theName);
  f.setSize(p.w, p.h);
  f.setLocation(100, 100);
  f.setResizable(true);
  f.setVisible(true);
  return p;
}
