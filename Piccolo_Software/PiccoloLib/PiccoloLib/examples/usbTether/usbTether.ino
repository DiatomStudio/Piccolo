#include <Servo.h>      //Needed in PiccoloLib
#include <PiccoloLib.h> //include PiccoloLib  

PiccoloLib piccolo;     //Make an instance of the Piccolo class

unsigned long tstBtnDwnTime;
unsigned long lastThumbChange;

boolean btnOneDown = false;
boolean btnTwoDown = false;

float sentZHeight = 0; 
float prevThumbWheelVal; 

float THUMB_ACTIVATE_THRESHHOLD = 30.0f;
float THUMB_CHANGE_THRESHHOLD = 5.0f;
void setup(){

  tstBtnDwnTime = 0;
  lastThumbChange = 0;
  
  prevThumbWheelVal = 0;
  piccolo.setup();        // Setup Piccolo with default settings
  piccolo.serialSetup();  // Start serial communication with Piccolo
  //piccolo.serialStream = true;
  
  piccolo.Y.invert(true);
}

void loop(){

  piccolo.serialLoop(); //main piccolo serial loop, this takes any dawign commands from the serial line and draws them.

  // If button two is pressed for more than two seconds, draw test circles.
  // Did the button state change?
  if(!btnTwoDown && piccolo.buttonTwoDown())
    tstBtnDwnTime = millis();

  // Is button two down and has been held down for more than 2 seconds?
  if(piccolo.buttonTwoDown() && millis() - tstBtnDwnTime > 2000){
    drawSpiral();
    tstBtnDwnTime = 0;
  }




   //Use the Piccolo thumbwheel to set the Z draw height for piccolo and relay this over the serial
   float val = piccolo.readThumbwheel();
   float deltaVal = abs(prevThumbWheelVal-val);

   if( deltaVal > THUMB_ACTIVATE_THRESHHOLD || (millis() - lastThumbChange < 1000 && deltaVal > THUMB_CHANGE_THRESHHOLD)){
     lastThumbChange  = millis();
     prevThumbWheelVal = val;
     float zVal = ((piccolo.Z.getBedSize()/1024.0)*val)-(piccolo.Z.getBedSize()/2.0);
     piccolo.move(piccolo.X.getPos(), piccolo.Y.getPos(),zVal);
     piccolo.setPenDownPos(zVal);
 
     //Send new Z height of serial
     Serial.print("setZ:");
     Serial.print(zVal);
     Serial.println();
   }
  
  
  
  // Store button two state
  btnTwoDown = piccolo.buttonTwoDown();

  // If button one is pressed send the start drawing command.
  // Did button one state change?
  if(!btnOneDown && piccolo.buttonOneDown())
    Serial.println("start");

  //Store button one state
  btnOneDown = piccolo.buttonOneDown();

}


void drawSpiral(){

  float maxR = piccolo.X.getBedSize()/2;
  float minR = 0;

  float spacing = maxR/20; // Gap between circles.
  float stepsPerMM = 1.0; 
  
  piccolo.beginShape();
  for(float r = maxR; r > minR; r -= spacing) {
    float stepSize = stepsPerMM / (2*r*PI);
    for(float a=TWO_PI; a>0; a-= stepSize) {    
      piccolo.vertex(sin(a)*r,cos(a)*r);
    }
  }

  piccolo.endShape();
  
  piccolo.home();
}


