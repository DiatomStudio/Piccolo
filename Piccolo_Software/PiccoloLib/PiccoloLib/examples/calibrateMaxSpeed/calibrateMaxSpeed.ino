/*
Calibrate Piccolo draw speed. 

If Speed is to great servo's will not have time to catch up with drawing instructions.
Try running this sketch with piccoloSpeed set at different rates, the default is 3mm per second.

At a good speed Piccolo piccolo will draw a series of rectangles with close to square corners, as the speed becomes to great rectangles will begin to develop rounded edges.


Instructions: 
-Change piccoloSpeed 
-Upload sketch
-Set pen height with thumbwheel
-Press button one to start. 

 */
#include <Servo.h> //Needed in Piccolo Lib
#include <PiccoloLib.h> //include the Piccolo Lib
PiccoloLib piccolo; //Make a instance of the Piccolo library for controlling Piccolo


float piccoloSpeed = PICCOLO_DEFAULT_MM_PER_SEC; // change this to make piccolo draw slower or faster.

float prevThumbWheelVal; 
void setup(){

  piccolo.setup(SERVO_DEFAULT_MIN_X,SERVO_DEFAULT_MAX_X
    ,SERVO_DEFAULT_MIN_Y,SERVO_DEFAULT_MAX_Y
    ,SERVO_DEFAULT_MIN_Z,SERVO_DEFAULT_MAX_Z
    ,PICCOLO_DEFAULT_BED_WIDTH
    ,PICCOLO_DEFAULT_BED_HEIGHT
    ,PICCOLO_DEFAULT_BED_DEPTH);

  prevThumbWheelVal = 0;


}

void loop(){

  //Use the Piccolo thumbwheel to set the Z draw height for piccolo and relay this over the serial
  float val = piccolo.getThumbwheelVal();
  if(abs(prevThumbWheelVal-val) > 10){
    prevThumbWheelVal = val;
    float zVal = (piccolo.getBedDepth()/1024.0)*val;
    piccolo.move(piccolo.getX(), piccolo.getY(),zVal);
    piccolo.setPenDownPos(zVal);

  }


  piccolo.setSpeed(piccoloSpeed); // default is 3 mm per second.
  if(piccolo.btnOneDown()){
    int spacing = 2;
    for(int x = 0; x < (piccolo.getBedHeight()/2); x+= spacing){
      piccolo.rect(x,x,piccolo.getBedWidth()-(x*2), piccolo.getBedWidth()-(x*2));
    }
  }
}

