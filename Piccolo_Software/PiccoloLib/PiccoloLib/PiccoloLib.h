/*
 PiccoloLib.h - Library for controlling Piccolo the tiny CNC-bot.
 Piccolo.cc
 Created by Diatom Studio, October 10, 2013.
 Released into the public domain.
 */


#ifndef PiccoloLib_h
#define PiccoloLib_h

/*Includes*/
#include "Arduino.h"
#include <Servo.h> //requires Servo.h to run.

/*Setup IO pins*/
#define THUMBWHEEL_PIN A3
#define BUTTON_ONE_PIN 14
#define BUTTON_TWO_PIN 15
#define SERVO_X_PIN 3
#define SERVO_Y_PIN 5
#define SERVO_Z_PIN 6

/* 
    SERVO VARIABLE HIERARCHY:

    uS = pulse width in microseconds of signal sent to the servo.

    1)      uScenter
        defines the center position of the servo, based on where we want
        piccolo think the center is.  Normally this would be mid-way between the
        minimum and maximum rotation of the servo.

            bedSize
        We define what we want the bed size to be.  The default is 50mm, which is a
        conservative size that should fit within the range of most DS929-MG servos.
    
            uSdeg
        We specify the change in uS per degree of rotation for the servo. This can be
        calibrated as in 5).    

            gearSize
        We specify the gear pitch diameter based on the actual pinion gear dimensions.
        

    2)  The gear pitch diameter and the change in uS per degree are used to calculate
        the change in uS per mm of motion:

            uSmm = uSdeg/((PI/360)*gearSize)
    
    3)  The uS range is found by multiplying the bed size by the uS/mm

            uSrange = bedSize * uSmm

    4)  The uS minimum and maximums are found by using the uS range and center

            uSmin = uScenter - (uSrange/2)
            uSmax = uScenter + (uSrange/2)

    5) uSdeg can be calibrated by drawing a target dimension and measuring the actual output

            uSdeg  = ((target*uSmm)/actual) * (PI/360)*gearSize;

*/

/*Servo Defaults*/
#define DEFAULT_USCENTER 1551  // center position of servo in uS
#define DEFAULT_BEDSIZE  50.0  // bed size in mm
#define DEFAULT_USDEG    9.7   // change in uS per degree of rotation
#define DEFAULT_GEARSIZE 35.23 // gear pitch diameter

/*Drawing Defaults*/
#define DEFAULT_STEP_SIZE 0.1  // in mm.
#define DEFAULT_SPEED 60       // in mm per second
#define DEFAULT_PEN_UP_POS 5   // in mm, relative to center
#define DEFAULT_PEN_DOWN_POS -5


class PiccoloAxis
{ 
public:
    PiccoloAxis();

    Servo servo;

    /* setup functions */
    void setup(int _pin);
    void setup(int _pin, int _uScenter, float _bedSize);
    void setup(int _pin, int _uScenter, float _bedSize, float _uSdeg);
    void setup(int _pin, int _uScenter, float _bedSize, float _uSdeg, float _gearSize);
    void setup(int _pin, int _uScenter, float _bedSize, float _uSdeg, float _gearSize, boolean _inverted);

    void calcuSdeg(float target, float actual);
    void calcuSmm();
    void calcuSrange();
    void calcMinMax();

    void invert(boolean _inverted);  
    void moveCenter(float offset);
    void setBedSize(float newSize);

    /* mechanical functions */
    void move(float _pos);

    /* get variables */
    int   getPin();
    int   getuScenter();
    float getuSdeg();
    float getGearSize();
    float getBedSize();

    float getuSmm();
    int   getuSrange();
    int   getuSmin();
    int   getuSmax();

    float getPos();
    boolean isInverted();

    // Advanced
    float setuSmm(float _uSmm);
    int   setuSrange(int _uSrange);
    int   setuSmin(int _uSmin);
    int   setuSmax(int _uSmax);


private:

    int   pin;
    int   uScenter;
    float bedSize;
    float uSdeg;
    float gearSize;

    float uSmm;
    int   uSrange;
    int   uSmin;
    int   uSmax;

    float pos;
    boolean inverted;

};


class PiccoloLib
{
public:

    PiccoloLib();
    
    PiccoloAxis X;
    PiccoloAxis Y;
    PiccoloAxis Z;

    /* setup functions */
    void setup(); // Sets up X,Y,Z axes with default settings.
    void setup(int xPin, int yPin, int zPin); // setup using custom pin assignments
    void invert(boolean invertX, boolean invertY, boolean invertZ);
    void moveCenter(float xOffset, float yOffset);
    void moveCenter(float xOffset, float yOffset, float zOffset);
    void calibrate(float targetDimension, float actualX, float actualY);
    void calibrate(float targetDimension, float actualX, float actualY, float actualZ);

    void setBedSize(float size);
    void setBedSize(float xySize, float zSize);
    void setBedSize(float xSize, float ySize, float zSize);
    void setSpeed(float speed);             //in mm per seconds
    void setStepSize(float _stepSize);      //default is 0.1mmm
 
    /* mechanical functions */
    void moveX(float x);
    void moveY(float y);
    void moveZ(float z);
    void move(float x, float y);
    void move(float x, float y, float z);
    void home();

    //tentative
    boolean thumbwheelZcontrol;
    void thumbwheelControlX();
    void thumbwheelControlY();
    void thumbwheelControlZ();

    void setPenDownPos(float _penDownPos);
    float getPenDownPos();

    /* piccolo brain inputs */
    float readThumbwheel();
    boolean buttonOneDown();
    boolean buttonTwoDown();
 
    /* serial functions */   
    void serialSetup();
    void serialLoop();   
    
    /* draw functions */
    void beginShape();
    void endShape();
    void vertex (float x,  float y);
    void vertex (float x,  float y,  float z);
    void line   (float x1, float y1, float x2, float y2);
    void line   (float x1, float y1, float z1, float x2, float y2, float z2);
    void rect   (float x,  float y,  float width, float height);
    void rect   (float x,  float y,  float z, float width, float height);
    void ellipse(float x,  float y,  float width, float height);
    void arc    (float x , float y , float width, float height, float startA, float stopA);
    void bezier (float x1, float  y1, float  cx1, float  cy1, float  cx2, float  cy2, float  x2, float  y2);
    float bezierPoint  (float a, float b, float c, float d, float t);
    float bezierTangent(float a, float b, float c, float d, float t);

    
private:

    // These control piccolo speed and level of detail
    float stepSize;       // Motion between two points is divided into segments to control speed.
                          // A smaller step size means a more controlled motion, but too small will limit the maximum speed.
                          // Also affects curvature when using ellipse(), arc(), or bezier(); smaller stepSize means a less faceted curve
    float mmPerSecond;    // speed in mm per second.
    int delayPerStep;     // delay in milliseconds, calculated based on speed and stepSize.
                          // longer delay means more time for the servo to reach its target position.
    void calcDelayPerStep();

    // The pen positions for pen-up (lifted) and down (drawing)
    float penUpPos;
    float penDownPos;
    
    boolean beginDrawFlag; // For moving the pen into position before starting to draw
  	boolean drawing;       // If the pen is touching the paper

  	float dist(float x1, float y1, float z1, float x2, float y2, float z2);
    float dist(float dx, float dy, float dz);

    //float calcFloat(char *str);
    //float calcFloat(int data[], int ind);
    float calcFloat(byte b_1, byte b_2, byte b_3, byte b_4);


	/* Serial functions */
    //void establishContact();
    #define CHAR_PER_POS 8
    byte inByte;
	byte inString[25];  // xxxxxxxxyyyyyyyzzzzzzzz;
    byte tmpPosStr[CHAR_PER_POS];  //copy inString into this for converting from HEX
	int index;
	boolean gotPos;
    
	boolean serialStream;    // Streams coordinates over Serial as G-Code
    boolean disableMotion;   // disables servo motion, for debugging purposes
  
};


#endif