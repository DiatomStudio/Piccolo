# PiccoloLib
----------------
PiccoloLib is a Arduino Library that lets you control your Piccolo with drawing commands similar to what is used in processing.

PiccoloLib also includes usbTether in it's example directory. This is used to draw drawings sent from your computer using the Controllo application. 

####installing
- Unzip and put the extracted PiccoloLib folder into your Arduino libraries folder.
- Add to your Arduino sketch using Sketch > Import Library > PiccoloLib


####example

Taken from spiral example in examples folder.

``` c
#include <Servo.h> //Needed in Piccolo Lib
#include <PiccoloLib.h> //include the Piccolo Lib


PiccoloLib piccolo; //Make a instance of the Piccolo library for controlling Piccolo

void setup(){
piccolo.setup(); //Setup Piccolo
piccolo.home(); //Tell Piccolo to goto it's home position. 

}

void loop(){
drawSpiral(); // Draw a spiral. 
delay(2000); //Wait 2 seconds before stating the loop again
}

void drawSpiral(){
  
  	float spacing = piccolo.getBedWidth()/20; //Distance between loops.  	float maxR = piccolo.getBedWidth();
  	float minR = 10.0f;
  	
    piccolo.beginShape();
    for(float r = maxR; r > minR; r -= spacing) {
    for(float a=TWO_PI; a>0; a-= 0.01) {    
      piccolo.vertex(sin(a)*r+(PICCOLO_BED_WIDTH/2.0f),cos(a)*r+(PICCOLO_BED_HEIGHT/2.0f));
    }
  }
  
  piccolo.endShape();
}


```


####reference
You will need Arduino <http://arduino.cc/> installed on your computer in order to develop with the PiccoloLib.

In general all drawing commands follow the same format as Processing drawing commands, for more info on these please visit: <http://processing.org/reference/>

``` c
    /* setup functions */
    void setup(); // must be called in setup;
    void setup(int servoMinX, int servoMaxX, int servoMinY, int servoMaxY, int servoMinZ, int servoMaxZ , int piccoloBedWidth, int piccoloBedHeight, int piccoloBedDepth);
    void loop(); // not used currently
    void setServosMinMax(int minX,int maxX,int minY,int maxY,int minZ,int maxZ); //set min and max servo positions
    void setServoPins(int xPin,int yPin, int zPin); //set servo pins
    void invertAxis(boolean xAxis, boolean yAxis, boolean zAxis );//Invert a drawing axis.
    void setSpeed(float speed);//range between 0-1 1 is max speed. Uses step resolution to calculate max move speed.
    void setStepResolution(float resolution); //default is 0.1mmm
    
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
    
    /* Piccolo brain inputs */
    float getThumbwheelVal();
    boolean btnOneDown();
    boolean btnTwoDown();
    
    
    void serialSetup();
    void serialLoop();
    ```