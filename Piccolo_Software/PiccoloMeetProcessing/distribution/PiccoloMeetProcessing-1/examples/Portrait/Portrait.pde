import controlP5.*;
import piccoloMeetProcessing.*;

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




public Face face = new Face();
Piccolo piccolo = new Piccolo(bedWidth,bedHeight,bedDepth);



void setup(){
  size(300,300);
  cp5 = new ControlP5(this);
  
  
    cp5.addButton("startSend")
    .setPosition(width-35,height-19)
     .setSize(35,19);




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

piccolo.rotate(PI/2);


setupGUI();

}



void draw(){
  
  
  piccolo.update();
  
  
  background(200);
  
  
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
    stroke(255,0,0);
  piccolo.drawCodeStack(g);
  
  }
  popMatrix();
  
  
 // if(occlusionCanvas != null)
  //image(occlusionCanvas,0,0);
  
  if(keyPressed){
    
   if(key == 'm'){
    
  }}

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
    occlusionCanvas.rotate(PI/2);    
    occlusionCanvas.scale( occlusionCanvas.width/piccolo.bedWidth);
    face.draw(occlusionCanvas);
    occlusionCanvas.popMatrix();
    occlusionCanvas.endDraw();
    
    face.draw(piccolo);
   // piccolo.removeOcclusions(occlusionCanvas);
    
    piccolo.start();
}


void setupGUI(){
  

  float posX = 10;
  float posY = 10;
    // create a toggle
  cp5.addToggle("gender")
     .setPosition(posX,posY)
     .setSize(10,10)
     .plugTo(face,"gender")
     ;
     posY+=30;
     
  cp5.addSlider("headHeight")
     .setPosition(posX,posY)
     .setRange(2,50)
     .setValue(face.headHeight)
     .plugTo(face,"headHeight")
     .setLabelVisible(true)
     ;
      posY+=10;
      
     
    cp5.addSlider("headWidth")
     .setPosition(posX,posY)
     .setRange(2,50)
      .setValue(face.headWidth)
     .plugTo(face,"headWidth")
     ;
      posY+=10;
     
     
     
}

