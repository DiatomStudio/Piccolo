/*
Calibrate Piccolo's Step Resolution
*/
#include <Servo.h> //Needed in Piccolo Lib
#include <PiccoloLib.h> //include the Piccolo Lib
PiccoloLib piccolo; //Make a instance of the Piccolo library for controlling Piccolo

float prevThumbWheelVal; 

float a = 0;
float r = r;
void setup(){
  
    piccolo.setup(SERVO_DEFAULT_MIN_X,SERVO_DEFAULT_MAX_X
    ,SERVO_DEFAULT_MIN_Y,SERVO_DEFAULT_MAX_Y
    ,SERVO_DEFAULT_MIN_Z,SERVO_DEFAULT_MAX_Z
    ,PICCOLO_DEFAULT_BED_WIDTH
    ,PICCOLO_DEFAULT_BED_HEIGHT
    ,PICCOLO_DEFAULT_BED_DEPTH);

  prevThumbWheelVal = 0;

  r = piccolo.getBedWidth()/2.0f;
  
}

void loop(){
  
    //Use the Piccolo thumbwheel to set the Z draw height for piccolo and relay this over the serial
  float val = piccolo.getThumbwheelVal();
  if(abs(prevThumbWheelVal-val) > 10){
    prevThumbWheelVal = val;
  }

a+=0.1;
 piccolo.vertex(sin(a)*r+(piccolo.getBedWidth()/2.0f),cos(a)*r+(piccolo.getBedHeight()/2.0f));

if(a>TWO_PI)
a =0;


}

