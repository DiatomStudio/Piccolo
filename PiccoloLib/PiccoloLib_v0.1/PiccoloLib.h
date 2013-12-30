/*
  PiccoloLib.h - Library for controlling Piccolo the tiny CNC-bot.
  Piccolo.cc
  Created by Diatom Studio, October 10, 2013.
  Released into the public domain.
*/



#ifndef PiccoloLib_h
#define PiccoloLib_h


#define PICCOLO_BED_WIDTH 300
#define PICCOLO_BED_HEIGHT 300
#define PICCOLO_BED_DEPTH 300

#define THUMBWHEEL_PIN A3
#define BUTTON_ONE_PIN 14
#define BUTTON_TWO_PIN 15

#include "Arduino.h"
#include <Servo.h>

  class PiccoloLib
  {
  public:
    PiccoloLib(int test);
    PiccoloLib();
    void setup();
    void loop();
    void  rect(float x, float y, float width,float height);
    void  rect(float x, float y, float z, float width,float height);
    void  line(float x1,float y1,float x2,float y2);
    void  line(float x1,float y1,float z1,float x2,float y2,float z2);
    void  vertex(float x, float y);
    void  vertex(float x, float y,float z);
    void  move(float x, float y);
    void  move(float x, float y, float z);
    void  bezier(float x1,float  y1,float  cx1,float  cy1,float  cx2,float  cy2,float  x2,float  y2);
    float  bezierPoint(float a, float b, float c, float d, float t);
    float  bezierTangent(float a, float b, float c, float d, float t);
    void  ellipse(float x, float y, float width, float height);
    void  arc(float x , float y , float width, float height, float startA, float stopA);
    void  beginShape();
    void  endShape();

    float getX();
    float getY();
    float getZ();

  	//Piccolo functions
    void home();
    void setPressure();

    //Set Z down height 
    void setPenDownPos(float pos);
    
    float getThumbwheelVal();
    boolean btnOneDown();
    boolean btnTwoDown();


    void serialSetup();
    void serialLoop(); 

  private:
  	boolean beginShapeFlag;
  	float dist(float x1, float y1,float z1,float x2,float y2,float z2);

  	//Servos
    Servo xServo; // blue
    Servo yServo; // green
    Servo zServo; // orange


	//serial functions

 float calcFloat(int data[], int ind);
 void establishContact();

 byte inByte;
	int inString[15];  // xxxxxyyyyyzzzzz
	int index;
	boolean gotPos;
	boolean ready;

	float xPosIn;
	float yPosIn;
	float zPosIn;



//this prints out the servo positions instead of printing the servo
	boolean sendToDebugConsole;

//This controls speed and resolution
	float stepSize; // smaller step means more detail and slower paths.
	float delayPerStep; // longer delay means more time for servo's to reach position. // will not take effect unless above 200 

  float xPos;
  float yPos;
  float zPos;

		//servo variables

	int xmin;
	int xmax;
	int xcenter;

	int ymin;
	int ymax;
	int ycenter;

	int zmin;
	int zmax;
	int zcenter;

//bed size
float bedwidth; 
float bedlength;
float bedheight; 

float xscale;
float yscale;
float zscale;


//invert a axis
boolean invertX ;
boolean invertY ;
boolean invertZ ;

//pen positions for pen up and down. should be set by piccolo model 
float penUpPos;
float penDownPos;


};
#endif