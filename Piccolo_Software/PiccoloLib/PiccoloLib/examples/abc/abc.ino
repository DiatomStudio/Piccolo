#include <Servo.h> //Needed in Piccolo Lib
#include <PiccoloLib.h> //include the Piccolo Lib

boolean btnOneDown;

PiccoloLib piccolo; //Make a instance of the Piccolo library for controlling Piccolo

unsigned long lastThumbChange;

float prevThumbWheelVal; 

float THUMB_ACTIVATE_THRESHHOLD = 30.0f;
float THUMB_CHANGE_THRESHHOLD = 5.0f;

int charNum ;

void setup(){
  piccolo.setup(); //Setup Piccolo
  piccolo.home(); //Tell Piccolo to goto it's home position. 
  btnOneDown = false;

  charNum = 0; 
}

void loop(){
  if(!btnOneDown && piccolo.buttonOneDown()){

    if(charNum == 0)
      drawA();

    if(charNum == 1)
      drawB();

    if(charNum == 2)
      drawC();

    charNum++;

    if(charNum>2)
      charNum = 0; 
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

   }
   

}


void drawA(){

  piccolo.beginShape();
  piccolo.vertex(0,0);
  piccolo.vertex((piccolo.X.getBedSize()/2.0f),piccolo.Y.getBedSize());
  piccolo.vertex(piccolo.X.getBedSize(),0);
  piccolo.endShape();

  piccolo.beginShape();
  piccolo.vertex(piccolo.X.getBedSize()*0.1,piccolo.Y.getBedSize()*0.5);
  piccolo.vertex(piccolo.X.getBedSize()*0.9,piccolo.Y.getBedSize()*0.5);
  piccolo.endShape();

}



void drawB(){
  float startX = 10;
  piccolo.beginShape();
  piccolo.vertex(startX,piccolo.Y.getBedSize());
  piccolo.vertex(startX,0);
  piccolo.endShape();

  piccolo.beginShape();
  piccolo.vertex(startX,piccolo.Y.getBedSize());
  piccolo.vertex(piccolo.X.getBedSize(),piccolo.Y.getBedSize());
  piccolo.vertex(piccolo.X.getBedSize(),piccolo.Y.getBedSize()*0.5);
  piccolo.vertex(startX,piccolo.Y.getBedSize()*0.5);
  piccolo.vertex(piccolo.X.getBedSize(),piccolo.Y.getBedSize()*0.5);
  piccolo.vertex(piccolo.X.getBedSize(),0);
  piccolo.vertex(0,0);
  piccolo.endShape();
}

void drawC(){

  piccolo.beginShape();
  piccolo.vertex(piccolo.X.getBedSize(),piccolo.Y.getBedSize());
  piccolo.vertex(0,piccolo.Y.getBedSize());
  piccolo.vertex(0,0);
  piccolo.vertex(piccolo.X.getBedSize(),0);

  piccolo.endShape();
}

