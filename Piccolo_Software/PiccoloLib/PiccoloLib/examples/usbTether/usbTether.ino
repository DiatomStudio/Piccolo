#include <Servo.h> //Needed in Piccolo Lib
#include <PiccoloLib.h> //include the Piccolo Lib

PiccoloLib piccolo; //Make a instance of the Piccolo library for controlling Piccolo

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
  piccolo.setup();
  piccolo.serialSetup(); //Start serial communication
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
  float deltaVal = abs(prevThumbWheelVal-val);
  if( deltaVal > 30 || (millis() - lastThumbChange < 1000 && deltaVal > 5)){
    
    lastThumbChange  = millis();
    prevThumbWheelVal = val;
    float zVal = (piccolo.getBedDepth()/1024.0)*val;
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
  float maxR = (piccolo.getBedWidth()/2);
  float minR = 0;

  float spacing = (piccolo.getBedWidth()/2)/20; //Distance between loops.  
  float stepsPerMM = 1.0; 
  piccolo.beginShape();
  for(float r = maxR; r > minR; r -= spacing) {
    float stepSize = stepsPerMM / (2*r*PI);
    for(float a=TWO_PI; a>0; a-= stepSize) {    
      piccolo.vertex(sin(a)*r+(piccolo.getBedWidth()/2.0f),cos(a)*r+(piccolo.getBedHeight()/2.0f));
    }
  }

  piccolo.endShape();
  piccolo.vertex(0,piccolo.getBedWidth()/2);
}



