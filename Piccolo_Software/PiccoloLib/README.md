#PiccoloLib#

PiccoloLib is a library for [Arduino](http://www.arduino.cc) that makes it simple to program a Piccolo.

* Control movement, speed and accuracy.
* Various shape drawing functions.
* Configure & calibrate Piccolo.
* Send Piccolo co-ordinates over serial.
* [UsbTether]() example lets you control piccolo from Processing using the PiccoloP5 library.
* Configure axes individually with PiccoloAxis class, or add additional axes.

---

##Installation##

1. Download PiccoloLib_current.zip
2. Open the zip in Arduino by: Sketch > Import Library > Add Library

Alternatively, you can extract the archive into into your Arduino libraries folder manually.
If you are not sure where your libraries folder is, have a look at the Arduino libraries guide: http://arduino.cc/en/Guide/Libraries

---

##Reference##

Piccolo's output is intended to be used in mm, however you can scale the output to any arbitrary units using *calibrate()*.


###PiccoloLib Class###

---
####Setup, Configuration & Motion####

```arduino
void setup()
```
Sets up piccolo using the default pin assignments for the servos: *setup(3,5,6)*

```arduino
void setup(int xPin, int yPin, int zPin)
```
Set up piccolo using custom pins assignments

`void invert(boolean invertX, boolean invertY, boolean invertZ)`  
Invert the output on any of the axes.

**void moveCenter(float xOffset, float yOffset)**  
**void moveCenter(float xOffset, float yOffset, float zOffset)**  
Adjust piccolo's zero position.

**void calibrate(float targetDimension, float actualX, float actualY)**  
**void calibrate(float targetDimension, float actualX, float actualY, float actualZ)**  
Use this to scale piccolo's output, which you can use to calibrate it's accuracy.  First draw a square(or cube) of *targetDimension*, then measure the output and put this into your *setup()* with these dimensions.

**void setBedSize(float size)**  
**void setBedSize(float xySize, float zSize)**  
**void setBedSize(float xSize, float ySize, float zSize)**  
Will set the minimum and maximum positions of each axis, therefore defining the overall bed size.

**void setSpeed(float speed)**  
Defines piccolo's movement speed when using vertex() or shape functions.  In mm per second.  Default speed is 60mm/s.

**void setStepSize(float _stepSize)**  
Steps are used to control piccolo's motion and speed.  The default step size is 0.1mm.  A smaller step size means a more controlled motion, but too small will limit the maximum speed.  This also affects curvature when using *ellipse()*, *arc()*, or *bezier()* - a smaller stepSize means a less faceted curve

**int drawOrientation**  
**void setDrawOrientation(int _orientation)**  
*Work in progress.* Rotates the output:  
0 - default, drawing with bottom of drawing to the left  
1 - top, as if piccolo is drawing upside down  
2 - right, drawing with the bottom of the drawing to the right  
3  - bottom, as if piccolo is drawing the correct way up  

---
####Piccolo Inputs####

**float readThumbwheel()**  
Returns a value from 0 to 1023 based on the position of the thumbwheel.

**boolean buttonOneDown()**  
Returns true if button one is pressed

**boolean buttonTwoDown()**  
Returns true if button two is pressed

---
####Mechanical Control####

**void moveX(float x)**  
**void moveY(float y)**  
**void moveZ(float z)**  
**void move(float x, float y)**  
**void move(float x, float y, float z)**  
Sends the servo(s) to a position.  They will respond with their maximum speed, and you may need to allow time for the servos to move in your code with *delay()8.

**void home()**  
Moves piccolo to (minimum X position, 0, pen-up position)

**void thumbwheelControlX()**  
**void thumbwheelControlY()**  
**void thumbwheelControlZ()**  
Will move piccolo in each axis based on the thumbwheel position, between the minimum and maximum.

**void setPenDownPos(float _penDownPos)**  
**float getPenDownPos()**  
Set or retrieve the pen-down position.

---
####Making Shapes####

In general all drawing commands follow the same format as Processing drawing commands, for more info on these please visit: http://processing.org/reference/

**void beginShape()**  
Moves piccolo to the first vertex and then lowers the pen to the pen-down position.  
**void endShape()**  
Raises the pen to the pen-up position.  

**void vertex(float x,  float y)**  
**void vertex(float x,  float y,  float z)**  
Moves piccolo to (x,y,z) using the step size and speed to control the motion.

**void line(float x1, float y1, float x2, float y2)**  
**void line(float x1, float y1, float z1, float x2, float y2, float z2)**  
**void rect(float x,  float y,  float width, float height)**  
**void rect(float x,  float y,  float z, float width, float height)**  
**void ellipse(float x,  float y,  float width, float height)**  
**void arc(float x , float y , float width, float height, float startA, float stopA)**  
**void bezier(float x1, float  y1, float  cx1, float  cy1, float  cx2, float  cy2, float  x2, float  y2)**  
**void bezierYZ(float x1, float  y1, float  cx1, float  cy1, float  cx2, float  cy2, float  x2, float  y2)**  
**float bezierPoint(float a, float b, float c, float d, float t)**  
**float bezierTangent(float a, float b, float c, float d, float t)**  

---
####Serial Communication####

**void serialSetup()**  
Opens a serial port at 115200 baud.

**void serialLoop()**  

**boolean serialStream**  
Streams coordinates over Serial as G-Code

**boolean disableMotion**  
Disables servo motion, for debugging purposes


---
###PiccoloAxis class###

Each servo is controlled as a PiccoloAxis object.  
*uS = pulse width in microseconds of signal sent to the servo.*

---
####Variable Heirarchy####

1. **uScenter** - Defines the center position of the servo, based on where we want piccolo think the center is.  Normally this would be mid-way between the minimum and maximum rotation of the servo.  Default is 1551uS.  
**bedSize** - We define what we want the bed size to be.  The default is 50mm, which is a conservative size that should fit within the range of most DS929-MG servos.  
**uSdeg** - We specify the change in uS per degree of rotation for the servo. This can be calibrated as in 5.  Default is 9.7 us/deg.  
**gearSize** - We specify the gear pitch diameter based on the actual pinion gear dimensions.  Default is 35.23mm.

2. The gear pitch diameter and the change in uS per degree are used to calculate the change in uS per mm of motion:  
`uSmm = uSdeg/((PI/360)*gearSize)`

3. The uS range is found by multiplying the bed size by the uS/mm:  
`uSrange = bedSize * uSmm`

4. The uS minimum and maximums are found by using the uS range and center:  
`uSmin = uScenter - (uSrange/2)`  
`uSmax = uScenter + (uSrange/2)`

5. uSdeg can be calibrated by drawing a target dimension and measuring the actual output:  
`uSdeg  = ((target*uSmm)/actual) * (PI/360)*gearSize`

---
####Setup, Configuration & Motion####

**void setup(int _pin)**  
**void setup(int _pin, int _uScenter, float _bedSize)**  
**void setup(int _pin, int _uScenter, float _bedSize, float _uSdeg)**  
**void setup(int _pin, int _uScenter, float _bedSize, float _uSdeg, float _gearSize)**  
**void setup(int _pin, int _uScenter, float _bedSize, float _uSdeg, float _gearSize, boolean _inverted)**  
**void calcuSmm()**  
**void calcuSrange()**  
**void calcMinMax()**  
**void calcuSdeg(float target, float actual)**  
**void invert(boolean _inverted)**  
**void moveCenter(float offset)**  
**void setBedSize(float newSize)**  
**void move(float _pos)**  

---
####Helper Functions####

**int getPin()**  
**int getuScenter()**  
**float getuSdeg()**  
**float getGearSize()**  
**float getBedSize()**  
**float getuSmm()**  
**int getuSrange()**  
**int getuSmin()**  
**int getuSmax()**  
**float getPos()**  
**boolean isInverted()**  