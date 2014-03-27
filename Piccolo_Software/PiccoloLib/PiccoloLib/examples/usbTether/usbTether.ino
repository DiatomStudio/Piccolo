#include <Servo.h>      //Needed in PiccoloLib
#include <PiccoloLib.h> //include PiccoloLib

PiccoloLib piccolo;     //Make an instance of the Piccolo class

unsigned long tstBtnDwnTime;
unsigned long lastThumbChange;

boolean btnOneDown = false;
boolean btnTwoDown = false;

float sentZHeight = 0; 
float prevThumbWheelVal; 

void setup(){

  tstBtnDwnTime = 0;
  lastThumbChange = 0;
  
  prevThumbWheelVal = 0;
  piccolo.setup();        // Setup Piccolo with default settings
  piccolo.serialSetup();  // Start serial communication with Piccolo

}

void loop(){

  piccolo.serialLoop();

  // If button two is pressed for more than two seconds, draw test circles.
  // Did the button state change?
  if(!btnTwoDown && piccolo.btnTwoDown())
    tstBtnDwnTime = millis();

  // Is button two down and has been held down for more than 2 seconds?
  if(piccolo.btnTwoDown() && millis() - tstBtnDwnTime > 2000){
    drawCircles();
    tstBtnDwnTime = 0;
  }

  // Store button two state
  btnTwoDown = piccolo.btnTwoDown();

  // If button one is pressed send the start drawing command.
  // Did button one state change?
  if(!btnOneDown && piccolo.btnOneDown())
    Serial.println("start");

  //Store button one state
  btnOneDown = piccolo.btnOneDown();

}


void drawCircles(){

  float maxR = piccolo.X.getBedSize()/2;
  float minR = 0;

  float spacing = maxR/20; // Gap between circles.
  float stepsPerMM = 1.0; 
  
  piccolo.beginShape();
  for(float r = maxR; r > minR; r -= spacing) {
    float stepSize = stepsPerMM / (2*r*PI);
    for(float a=TWO_PI; a>0; a-= stepSize) {    
      piccolo.vertex(sin(a)*r+(piccolo.getBedWidth()/2.0f),cos(a)*r+(piccolo.getBedHeight()/2.0f));
    }
  }

  piccolo.endShape();
  
  piccolo.home();
}



