#include <Servo.h> //Needed in Piccolo Lib
#include <PiccoloLib.h> //include the Piccolo Lib

PiccoloLib piccolo; //Make a instance of the Piccolo library for controlling Piccolo

unsigned long tstBtnDwnTime;
boolean btnOneDown = false;
boolean btnTwoDown = false;

float sentZHeight = 0; 
float prevThumbWheelVal; 

void setup(){

  tstBtnDwnTime = 0;
  prevThumbWheelVal = 0;
  piccolo.setup(); //Setup Piccolo
  piccolo.serialSetup();        
}

void loop(){
  piccolo.serialLoop();


     //If button two is pressed fo mre than two seconds, draw a test spiral.
    //Did the button state change?
    if(!btnTwoDown && piccolo.btnTwoDown())
      tstBtnDwnTime = millis();
      
    //Is button two down and has been held down for more than 2 seconds?
    if(piccolo.btnTwoDown() && millis() - tstBtnDwnTime > 2000){
      drawSpiral();
      tstBtnDwnTime = 0;
    }
    //Store button two state
    btnTwoDown = piccolo.btnTwoDown();
    


    //If button one is pressed send the start drawing command
    //Did button one state change?
    if(!btnOneDown && piccolo.btnOneDown())
      sendStart();

    //Store button one state
    btnOneDown = piccolo.btnOneDown();


//Use the Piccolo thumbwheel to set the Z draw height for piccolo and relay this over the serial
  float val = piccolo.getThumbwheelVal();
  if(abs(prevThumbWheelVal-val) > 10){
    prevThumbWheelVal = val;
    float zVal = (PICCOLO_BED_DEPTH/1024.0)*val;
    piccolo.move(piccolo.getX(), piccolo.getY(),zVal);
    piccolo.setPenDownPos(zVal);


    Serial.print("setZ:");
    Serial.print(zVal);
    Serial.println();
  }

}


void sendStart(){
  Serial.println("start");
}

void drawSpiral(){
  float maxR = (PICCOLO_BED_WIDTH/2);
  float minR = 0;

  float spacing = (PICCOLO_BED_WIDTH/2)/20; //Distance between loops.   
  piccolo.beginShape();
  for(float r = maxR; r > minR; r -= spacing) {
    for(float a=TWO_PI; a>0; a-= 0.01) {    
      piccolo.vertex(sin(a)*r+(PICCOLO_BED_WIDTH/2.0f),cos(a)*r+(PICCOLO_BED_HEIGHT/2.0f));
    }
  }

  piccolo.endShape();
  piccolo.vertex(0,PICCOLO_BED_WIDTH/2);
}


