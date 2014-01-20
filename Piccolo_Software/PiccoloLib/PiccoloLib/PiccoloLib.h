/*
 PiccoloLib.h - Library for controlling Piccolo the tiny CNC-bot.
 Piccolo.cc
 Created by Diatom Studio, October 10, 2013.
 Released into the public domain.
 */



#ifndef PiccoloLib_h
#define PiccoloLib_h

/*Default Piccolo bed size*/
//These should be set to the actual output size
#define PICCOLO_DEFAULT_BED_WIDTH 50
#define PICCOLO_DEFAULT_BED_HEIGHT 50
#define PICCOLO_DEFAULT_BED_DEPTH 50

#define PICCOLO_DEFAULT_PEN_UP_POS 20
#define PICCOLO_DEFAULT_PEN_DOWN_POS 20


#define PICCOLO_DEFAULT_STEP_SIZE 0.1
#define PICCOLO_DEFAULT_MM_PER_SEC 3



/*
 Default start and end positions for servo's.
 This is minimum and maximum degree that the servo is able to move to reliably. This may change depending on your servo and your Piccolo version.
 
 */
#define SERVO_DEFAULT_MIN_X 13
#define SERVO_DEFAULT_MAX_X 167
#define SERVO_DEFAULT_MIN_Y 13
#define SERVO_DEFAULT_MAX_Y 167
#define SERVO_DEFAULT_MIN_Z 13
#define SERVO_DEFAULT_MAX_Z 167

/*Setup IO pins*/
#define THUMBWHEEL_PIN A3
#define BUTTON_ONE_PIN 14
#define BUTTON_TWO_PIN 15

#define SERVO_X_PIN 3
#define SERVO_Y_PIN 5
#define SERVO_Z_PIN 6

/*Includes*/
#include "Arduino.h"
#include <Servo.h> //requires Servo.h to run.

class PiccoloLib
{
public:
    PiccoloLib();

    /* setup functions */
    void setup(); // must be called in setup;
    void setup(int servoMinX, int servoMaxX, int servoMinY, int servoMaxY, int servoMinZ, int servoMaxZ , int piccoloBedWidth, int piccoloBedHeight, int piccoloBedDepth);
    void loop(); // not used currently
    void setServosMinMax(int minX,int maxX,int minY,int maxY,int minZ,int maxZ); //set min and max servo positions
    void setServoPins(int xPin,int yPin, int zPin); //set servo pins
    void invertAxis(boolean xAxis, boolean yAxis, boolean zAxis );//Invert a drawing axis.
    void setSpeed(float speed);//range beween 0-1 1 is max speed. Uses step resolution to calculate max move speed.
    void setStepResolution(float resolution);//default is 0.1mmm
    
    /* draw functions */
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
    
    int getBedWidth();
    int getBedHeight();
    int getBedDepth();
    
  	/* Piccolo functions */
    void home();
    void setPressure();
    
    //Set Z down height
    void setPenDownPos(float pos);
    float getPenDownPos();

    
    /* Piccolo brain inputs */
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
	int xMin;
	int xMax;
    
	int yMin;
	int yMax;
    
	int zMin;
	int zMax;
    
    //bed size
    float bedWidth;
    float bedHeight;
    float bedDepth;
    
    float xScale;
    float yScale;
    float zScale;
    
    
    //invert a axis
    boolean invertX ;
    boolean invertY ;
    boolean invertZ ;
    
    //pen positions for pen up and down. should be set by piccolo model
    float penUpPos;
    float penDownPos;
    
    
};
#endif