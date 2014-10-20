##PiccoloP5  
*PiccoloP5 is a Processing library for controlling Piccolo, the Tiny CNC-bot, from your processing sketch.*

Piccolo.cc  
Created by Diatom Studio, 16 October, 2014.  
Released into the public domain.  

##Installation  

To Install the PiccoloP5 library:  
1. Unzip /distribution/PiccoloP5-1/download/PiccoloP5.zip to your processing libraries directory.  
2. Restart Processing.  
3. Select  Sketch > Import Library > Piccolo P5  

##Hello Piccolo Example

```Processing
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
  
  
  for(int dia = 5; dia < bedWidth; dia += 5){
   piccolo.ellipse(0,0,dia,dia); 
  }
  
  piccolo.start();//start drawing
}


void draw(){
   piccolo.update(); //update piccolo
}
```
