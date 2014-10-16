/*
 Spiral - PiccoloLib Example.
 Piccolo.cc
 Created by Diatom Studio, October 10, 2013.
 Released into the public domain.

 This examples causes Piccolo to draw spiral shapes.
*/

#include <Servo.h>            //Needed in Piccolo Lib
#include <PiccoloLib.h>       //include the Piccolo Lib
PiccoloLib piccolo;           //Make a instance of the Piccolo library for controlling Piccolo

float maxR = piccolo.X.getBedSize()/2.0; //Spiral radius, reach to end of Piccolo draw area. 
float minR = 1; //Min spiral radius. 

void setup(){
  piccolo.setup();    //Setup Piccolo
  piccolo.home();    //Tell Piccolo to goto it's home position. 
  drawSpiral();    //Draw a spiral. 
}

void drawSpiral(){
   float spacing = maxR/20; //Distance between loops.  	
    piccolo.beginShape();
    for(float r = maxR; r > minR; r -= spacing) {
    for(float a=TWO_PI; a>0; a-= 0.01) {    
      piccolo.vertex(sin(a)*r,cos(a)*r);
    }
  }
  piccolo.endShape();
}
