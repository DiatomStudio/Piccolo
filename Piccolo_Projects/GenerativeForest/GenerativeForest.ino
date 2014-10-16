#include <Servo.h>
#include <PiccoloLib.h> //include the Piccolo Lib


PiccoloLib piccolo; //Make a instance of the Piccolo library for controlling Piccolo

int LDR_PIN = A2; 
float LDR_TOP_LIMIT = 612.0; //value when sensor is covered; 

unsigned long tstBtnDwnTime;
unsigned long lastThumbChange;

float THUMB_ACTIVATE_THRESHHOLD = 30.0f;
float THUMB_CHANGE_THRESHHOLD = 5.0f;
boolean btnOneDown;


float sentZHeight = 0; 
float prevThumbWheelVal; 
float lightReadingStart;

void setup() {
  piccolo.setup(); //Setup Piccolo
  piccolo.setDrawOrientation(ORIENTATION_BOTTOM); //draw with the picture facing piccolo 
  piccolo.home(); //Tell Piccolo to goto it's home position. 
  btnOneDown = false;
  
  tstBtnDwnTime = 0;
  lastThumbChange = 0;
  prevThumbWheelVal = 0;
  
}

void loop() {

  
  // Is button two down and has been held down for more than 2 seconds?
  if(piccolo.buttonTwoDown() && millis() - tstBtnDwnTime > 2000){
    LDR_TOP_LIMIT = analogRead(LDR_PIN);
    Serial.print("Set LDR_TOP_LIMIT to: ");
    Serial.println(LDR_TOP_LIMIT);
    piccolo.home();
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
  
  
  
  
    if(!btnOneDown && piccolo.buttonOneDown()){

     lightReadingStart =  1.0 - (analogRead(LDR_PIN) / LDR_TOP_LIMIT);
    Serial.print("light reading: ");
    Serial.println(lightReadingStart);
    
    //lightReading = constrain(lightReading,0.25,0.5);
    generatePlant(lightReadingStart);

   }
  
  btnOneDown = piccolo.buttonOneDown();
  
}



