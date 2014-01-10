#include <Servo.h> //Needed in Piccolo Lib
#include <PiccoloLib.h> //include the Piccolo Lib

float maxR = PICCOLO_BED_WIDTH / 2; //Spiral radius, reach to end of Piccolo draw area. 
float minR = 1; //Min spiral radius. 

PiccoloLib piccolo; //Make a instance of the Piccolo library for controlling Piccolo

void setup(){
piccolo.setup(); //Setup Piccolo
piccolo.home(); //Tell Piccolo to goto it's home position. 

}

void loop(){
drawSpiral(); // Draw a spiral. 
delay(2000); //Wait 2 seconds before stating the loop again
}

void drawSpiral(){
  
  	float spacing = maxR/20; //Distance between loops.  	
    piccolo.beginShape();
    for(float r = maxR; r > minR; r -= spacing) {
    for(float a=TWO_PI; a>0; a-= 0.01) {    
      piccolo.vertex(sin(a)*r+(PICCOLO_BED_WIDTH/2.0f),cos(a)*r+(PICCOLO_BED_HEIGHT/2.0f));
    }
  }
  
  piccolo.endShape();
}