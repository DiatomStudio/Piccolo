PiccoloP5 is a Processing library for sending processing drawing commands to Piccolo the Tiny CNC-bot!

=== Install ===
To Install the PiccoloP5 library,

unzip /distribution/PiccoloP5-1/download/PiccoloP5.zip to your processing libraries directory.  
restart Processing.
Select  Sketch > Import Library > Piccolo P5 

=== PiccoloP5 Example ===

import piccoloP5.*;
import processing.serial.*;

//Piccolo bed size in mm
float bedWidth = 50.0; 
float bedHeight = 50.0; 
float bedDepth = 50.0; 
float bedRenderWidth = 300;

PiccoloP5 piccolo;

void setup() {
  size(400,400);  
  piccolo = new PiccoloP5(bedWidth,bedHeight,bedDepth);
  piccolo.serial = new Serial(this, Serial.list()[Serial.list().length-1]); //This selects the last COM port listed on your system, this is usually Piccolo but not always. 
  piccolo.serialConnected = true;
  
  piccolo.ellipse(0,0,dia,dia);   //tell piccolo to draw an ellipse
  piccolo.start();                //start drawing
}


void draw(){
   piccolo.update(); //update piccolo
}

