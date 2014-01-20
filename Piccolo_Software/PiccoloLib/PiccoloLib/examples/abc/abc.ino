#include <Servo.h> //Needed in Piccolo Lib
#include <PiccoloLib.h> //include the Piccolo Lib

//Note: Swap X and Y servo leads.

boolean btnOneDown;
PiccoloLib piccolo; //Make a instance of the Piccolo library for controlling Piccolo
unsigned long lastThumbChange;
float prevThumbWheelVal; 


int charNum ;

void setup(){
  piccolo.setup(); //Setup Piccolo
  piccolo.home(); //Tell Piccolo to goto it's home position. 
  btnOneDown = false;

  charNum = 0; 
}

void loop(){
  if(!btnOneDown && piccolo.btnOneDown()){

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
  float val = piccolo.getThumbwheelVal();
  float deltaVal = abs(prevThumbWheelVal-val);
  if( deltaVal > 30 || (millis() - lastThumbChange < 1000 && deltaVal > 5)){

    lastThumbChange  = millis();
    prevThumbWheelVal = val;
    float zVal = (piccolo.getBedDepth()/1024.0)*val;
    piccolo.move(piccolo.getX(), piccolo.getY(),zVal);
    piccolo.setPenDownPos(zVal);
    piccolo.invertAxis(true,false,false);
  }





}


void drawA(){

  piccolo.beginShape();
  piccolo.vertex(0,0);
  piccolo.vertex((piccolo.getBedWidth()/2.0f),piccolo.getBedHeight());
  piccolo.vertex(piccolo.getBedWidth(),0);
  piccolo.endShape();


  piccolo.beginShape();
  piccolo.vertex(piccolo.getBedWidth()*0.1,piccolo.getBedHeight()*0.5);
  piccolo.vertex(piccolo.getBedWidth()*0.9,piccolo.getBedHeight()*0.5);
  piccolo.endShape();

}



void drawB(){
  float startX = 10;
  piccolo.beginShape();
  piccolo.vertex(startX,piccolo.getBedHeight());
  piccolo.vertex(startX,0);
  piccolo.endShape();


  piccolo.beginShape();
  piccolo.vertex(startX,piccolo.getBedHeight());
  piccolo.vertex(piccolo.getBedWidth(),piccolo.getBedHeight());
  piccolo.vertex(piccolo.getBedWidth(),piccolo.getBedHeight()*0.5);
  piccolo.vertex(startX,piccolo.getBedHeight()*0.5);
  piccolo.vertex(piccolo.getBedWidth(),piccolo.getBedHeight()*0.5);
  piccolo.vertex(piccolo.getBedWidth(),0);
  piccolo.vertex(0,0);
  piccolo.endShape();


  //Circles
  /*
  piccolo.beginShape();
   piccolo.arc(startX,piccolo.getBedHeight()*0.75,piccolo.getBedHeight()*0.50,piccolo.getBedHeight()*0.25,0,PI);
   piccolo.endShape();
   
   piccolo.beginShape();
   piccolo.arc(startX,piccolo.getBedHeight()*0.25,piccolo.getBedHeight()*0.50,piccolo.getBedHeight()*0.25,0,PI);
   piccolo.endShape();
   
   */

}



void drawC(){

  piccolo.beginShape();
  piccolo.vertex(piccolo.getBedWidth(),piccolo.getBedHeight());
  piccolo.vertex(0,piccolo.getBedHeight());
  piccolo.vertex(0,0);
  piccolo.vertex(piccolo.getBedWidth(),0);

  piccolo.endShape();
  /*
  piccolo.beginShape();
   piccolo.arc(piccolo.getBedWidth()*0.5,piccolo.getBedHeight()*0.5,piccolo.getBedHeight()*0.5,piccolo.getBedHeight()*0.5,0,TWO_PI*0.75);
   piccolo.endShape();
   
   */

}

