//Notes: Swap x-y servo leads. 
#include <Servo.h> //Needed in Piccolo Lib
#include <PiccoloLib.h> //include the Piccolo Lib

PiccoloLib piccolo; //Make a instance of the Piccolo library for controlling Piccolo


unsigned long lastThumbChange;
float prevThumbWheelVal; 

float val;

void setup(){
piccolo.setup(); //Setup Piccolo
//piccolo.home(); //Tell Piccolo to goto it's home position.
piccolo.vertex(0,50,25);

//Serial.begin(9600);
}

void loop(){
  
    //Use the Piccolo thumbwheel to set the Z draw height for piccolo and relay this over the serial
  float val = piccolo.getThumbwheelVal();
  float deltaVal = abs(prevThumbWheelVal-val);
  if( deltaVal > 30 || (millis() - lastThumbChange < 1000 && deltaVal > 5)){
    
    lastThumbChange  = millis();
    prevThumbWheelVal = val;
    float zVal = (piccolo.getBedDepth()/1024.0)*val;
    piccolo.move(piccolo.getX(), piccolo.getY(),zVal);
    piccolo.setPenDownPos(zVal);
  
  }
  
  
  
  
  if(piccolo.btnOneDown()) {
    
    for(int i=50; i>0; i--) {
      val = analogRead(0);
     // Serial.println(val);
      val = map(val,0,80,50,0);
      piccolo.move(val,i,piccolo.getPenDownPos());
      delay(100);
    }
    piccolo.beginShape();
    piccolo.vertex(0,50,piccolo.getPenDownPos());
    piccolo.endShape();

 
  }
  
  
  
}

