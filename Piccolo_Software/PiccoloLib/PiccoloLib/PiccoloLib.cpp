
#include "PiccoloLib.h"


PiccoloLib::PiccoloLib(){}

/* ================================= setup functions ================================= */

void PiccoloLib::setup() {
    setup(SERVO_X_PIN, SERVO_Y_PIN, SERVO_Z_PIN);
}

void PiccoloLib::setup(int xPin, int yPin, int zPin){

    X.setup(xPin);
    Y.setup(yPin);
    Z.setup(zPin);
 
    home();

    setSpeed(DEFAULT_SPEED);
    setStepSize(DEFAULT_STEP_SIZE);
    
    penUpPos = DEFAULT_PEN_UP_POS;
    penDownPos = DEFAULT_PEN_DOWN_POS;
    
    beginDrawFlag = false;
    drawing = false;
    //thumbwheelZcontrol = false;
    serialStream = false;
    disableMotion = false;
    
    pinMode(BUTTON_ONE_PIN, INPUT_PULLUP);           // set pin to input
    pinMode(BUTTON_TWO_PIN, INPUT_PULLUP);           // set pin to input
    //pinMode(THUMBWHEEL_PIN, INPUT);                // not needed for analog pin
    
    index = 0;
    gotPos = false;
    waitingForPos = false;
    
    drawOrientation = ORIENTATION_LEFT;
    
}

void PiccoloLib::invert(boolean invertX, boolean invertY, boolean invertZ){
    X.invert(invertX);
    Y.invert(invertY);
    Z.invert(invertZ);
}

void PiccoloLib::moveCenter(float xOffset, float yOffset) {
    X.moveCenter(xOffset);
    Y.moveCenter(yOffset);
}

void PiccoloLib::moveCenter(float xOffset, float yOffset, float zOffset) {
    X.moveCenter(xOffset);
    Y.moveCenter(yOffset);
    Z.moveCenter(zOffset);
}

void PiccoloLib::calibrate(float targetDimension, float actualX, float actualY) {
    X.calcuSdeg(targetDimension, actualX);
    Y.calcuSdeg(targetDimension, actualY);
}

void PiccoloLib::calibrate(float targetDimension, float actualX, float actualY, float actualZ) {
    X.calcuSdeg(targetDimension, actualX);
    Y.calcuSdeg(targetDimension, actualY);
    Z.calcuSdeg(targetDimension, actualZ);
}

void PiccoloLib::setBedSize(float size) {
    X.setBedSize(size);
    Y.setBedSize(size);
    Z.setBedSize(size);
}

void PiccoloLib::setBedSize(float xySize, float zSize) {
    X.setBedSize(xySize);
    Y.setBedSize(xySize);
    Z.setBedSize(zSize);
}


void PiccoloLib::setBedSize(float xSize, float ySize, float zSize) {
    X.setBedSize(xSize);
    Y.setBedSize(ySize);
    Z.setBedSize(zSize);
}

void PiccoloLib::setSpeed(float _mmPerSecond){
    mmPerSecond = _mmPerSecond; 
    calcDelayPerStep();
}

void PiccoloLib::setStepSize(float _stepSize){
    stepSize = _stepSize; 
    calcDelayPerStep();
}
    
void PiccoloLib::calcDelayPerStep(){
    delayPerStep = 1000 * (stepSize/mmPerSecond);
}


/* ============================= PiccoloAxis functions ============================== */

PiccoloAxis::PiccoloAxis(){

    uScenter = DEFAULT_USCENTER;
    uSdeg    = DEFAULT_USDEG;
    gearSize = DEFAULT_GEARSIZE;
    bedSize  = DEFAULT_BEDSIZE;
    inverted = false;

}

// setup functions

void PiccoloAxis::setup(int _pin){
    setup(_pin, DEFAULT_USCENTER, DEFAULT_BEDSIZE, DEFAULT_USDEG, DEFAULT_GEARSIZE, false);
}

void PiccoloAxis::setup(int _pin, int _uScenter, float _bedSize){
    setup(_pin, _uScenter, _bedSize, DEFAULT_USDEG, DEFAULT_GEARSIZE, false);
}

void PiccoloAxis::setup(int _pin, int _uScenter, float _bedSize, float _uSdeg){
    setup(_pin, _uScenter, _bedSize, _uSdeg, DEFAULT_GEARSIZE, false);
}

void PiccoloAxis::setup(int _pin, int _uScenter, float _bedSize, float _uSdeg, float _gearSize){
    setup(_pin, _uScenter, _bedSize, _uSdeg, _gearSize, false);
}

void PiccoloAxis::setup(int _pin, int _uScenter, float _bedSize, float _uSdeg, float _gearSize, boolean _inverted){

    pin = _pin;
    uScenter = _uScenter;
    bedSize = _bedSize;
    uSdeg = _uSdeg;
    gearSize = _gearSize;
    inverted = _inverted;

    calcuSmm();     // find uSmm using gearSize and uSdeg
    calcuSrange();  // find uSrange using bedSize and uSmm
    calcMinMax();   // find min & max using uScenter, inverted and uSrange

    servo.attach(pin);//, 0, 3000); // attach servo and override default min/max uS values in the Servo library.
    
}

void PiccoloAxis::calcuSdeg(float target, float actual){
    uSdeg  = ((target*uSmm)/actual) * (PI/360)*gearSize;
    calcuSmm();     // update uSmm using gearSize and new uSdeg
    calcuSrange();  // update uSrange using bedSize and new uSmm
    calcMinMax();   // update min & max using uScenter, inverted and new uSrange
}

void PiccoloAxis::calcuSmm(){
    uSmm = uSdeg/((PI/360)*gearSize);
}

void PiccoloAxis::calcuSrange(){
    uSrange = bedSize * uSmm;
}

void PiccoloAxis::calcMinMax(){
    uSmin = uScenter - (uSrange/2);
    uSmax = uScenter + (uSrange/2); 
}

void PiccoloAxis::invert(boolean _inverted){
    inverted = _inverted;
}

void PiccoloAxis::moveCenter(float offset){
    uScenter += uSmm * offset;
    calcMinMax();   // update min & max using new uScenter and uSrange
}

void PiccoloAxis::setBedSize(float newSize){
    bedSize = newSize;
    calcuSrange();  // update uSrange using uSmm and new bedSize
    calcMinMax();   // update min & max using uScenter and new uSrange
}


// mechanical functions

void PiccoloAxis::move(float _pos){
    int uStarget;

    if(inverted) {
        uStarget = uScenter - _pos * uSmm;
    } else {
        uStarget = uScenter + _pos * uSmm;
    }
 
    uStarget = (uStarget < uSmin) ? uSmin : uStarget;
    uStarget = (uStarget > uSmax) ? uSmax : uStarget;

    servo.writeMicroseconds(uStarget);
    pos = _pos;
}


// get variables

int   PiccoloAxis::getPin(){
    return pin;
}
int   PiccoloAxis::getuScenter(){
    return uScenter;
}
float PiccoloAxis::getuSdeg(){
    return  uSdeg;
}
float PiccoloAxis::getGearSize(){
    return gearSize;
}
float PiccoloAxis::getBedSize(){
    return bedSize;
}
float PiccoloAxis::getuSmm(){
    return uSmm;
}
int   PiccoloAxis::getuSrange(){
    return uSrange;
}
int   PiccoloAxis::getuSmin(){
    return uSmin;
}
int   PiccoloAxis::getuSmax(){
    return uSmax;
}
float PiccoloAxis::getPos(){
    return pos;
}
boolean PiccoloAxis::isInverted(){
    return inverted;
}

/*  // To do.

// Advanced - set variables directly, recalculating dependencies.
float PiccoloAxis::setuSmm(float _uSmm){
    uSmm = _uSmm;
    // recalculate
}
int   PiccoloAxis::setuSrange(int _uSrange){
    uSrange = _uSrange;
    // recalculate
}
int   PiccoloAxis::setuSmin(int _uSmin){
    uSmin = _uSmin;
    // recalculate
}
int   PiccoloAxis::setuSmax(int _uSmax){
    uSmax = _uSmax;
    // recalculate
}

*/


/* ============================== mechanical functions =============================== */

void PiccoloLib::moveX(float x){
    move(x, Y.getPos(), Z.getPos());
}
void PiccoloLib::moveY(float y){
    move(X.getPos(), y, Z.getPos());
}
void PiccoloLib::moveZ(float z){
    move(X.getPos(), Y.getPos(), z);
}
void PiccoloLib::move(float x, float y){
    move(x, y, Z.getPos());
}

void PiccoloLib::move(float x, float y, float z){

    if (serialStream) {
        Serial.print("x:");
        Serial.print(x);
        Serial.print(" y:");
        Serial.print(y);
        Serial.print(" z:");
        Serial.print(z);
        Serial.println();
        delay(10);
    }

    if (!disableMotion) {
        X.move(x);
        Y.move(y);
        Z.move(z);
    }

}

void PiccoloLib::setDrawOrientation(int _orientation){

     drawOrientation = _orientation;


    switch(_orientation){
        
        case ORIENTATION_LEFT: //default orientation 
        X.setup(SERVO_X_PIN);
        Y.setup(SERVO_Y_PIN);
        Z.setup(SERVO_Z_PIN);
        break;

        case ORIENTATION_TOP: // swap X & Y axes
        X.setup(SERVO_Y_PIN);
        Y.setup(SERVO_X_PIN);
        Z.setup(SERVO_Z_PIN);
        break;

        case ORIENTATION_RIGHT: // invert Y axis
        X.setup(SERVO_X_PIN);
        Y.setup(SERVO_Y_PIN);
        Z.setup(SERVO_Z_PIN);
        Y.invert(true);
        break;

        case ORIENTATION_BOTTOM: // swap X & Y axes & invert Y axis
        X.setup(SERVO_Y_PIN); 
        Y.setup(SERVO_X_PIN);
        Z.setup(SERVO_Z_PIN);
        Y.invert(true);
        break;
    }

}


void PiccoloLib::home() {

    switch(drawOrientation){

        case ORIENTATION_LEFT:
            vertex(-X.getBedSize()/2, 0, penUpPos);
        break;

        case ORIENTATION_TOP:
            vertex(0, -X.getBedSize()/2, penUpPos);
        break;

        case ORIENTATION_RIGHT:
            vertex(X.getBedSize()/2,0, penUpPos);
        break;

        case ORIENTATION_BOTTOM:
            vertex(0,X.getBedSize()/2, penUpPos);
        break;

    }
}

void PiccoloLib::thumbwheelControlX(){
    float x = map(readThumbwheel(), 0, 1024, -X.getBedSize()/2, X.getBedSize()/2);
    moveX(x);
}
void PiccoloLib::thumbwheelControlY(){
    float y = map(readThumbwheel(), 0, 1024, -Y.getBedSize()/2, Y.getBedSize()/2);
    moveY(y);
}
void PiccoloLib::thumbwheelControlZ(){
    float z = map(readThumbwheel(), 0, 1024, -Z.getBedSize()/2, Z.getBedSize()/2);
    moveZ(z);
}

void PiccoloLib::setPenDownPos(float _penDownPos){
    penDownPos = _penDownPos;
    penUpPos = penDownPos + 5;
}

float PiccoloLib::getPenDownPos(){
    return penDownPos;
}


/* ============================== piccolo brain inputs =============================== */

float PiccoloLib::readThumbwheel(){
    return analogRead(THUMBWHEEL_PIN);
}

boolean PiccoloLib::buttonOneDown(){
    return !digitalRead(BUTTON_ONE_PIN);
}

boolean PiccoloLib::buttonTwoDown(){
    return !digitalRead(BUTTON_TWO_PIN);
}


/* ================================== draw functions ================================= */

void PiccoloLib::beginShape(){
    beginDrawFlag = true;
}

void PiccoloLib::endShape(){
    vertex(X.getPos(), Y.getPos(), penUpPos);
    drawing = false;
}

void PiccoloLib::vertex(float x, float y) {
    vertex(x, y, Z.getPos());
}

void PiccoloLib::vertex(float x, float y, float z) {
    
    if(beginDrawFlag){
        beginDrawFlag = false;
        vertex(x, y, penUpPos);   // move into position without drawing
        vertex(x, y, penDownPos); // then lower the pen;
        drawing = true;
    } else {
        float dx = x - X.getPos();
        float dy = y - Y.getPos();
        float dz = z - Z.getPos();
        float px = X.getPos();
        float py = Y.getPos();
        float pz = Z.getPos();

        float moveDelta = dist(dx, dy, dz);       // total move distance
        int numSteps = ceil(moveDelta/stepSize);  // number of steps to take



        for(int i = 0; i < numSteps; i++){
            move(                                 // move in increments of stepSize
                    px + (float(dx/numSteps) * float(i)),
                    py + (float(dy/numSteps) * float(i)),
                    pz + (float(dz/numSteps) * float(i))
                );
            delay(delayPerStep);
        }
        move(x, y, z); // move to final position as last step.

        delay(delayPerStep);
    }

}

void PiccoloLib::line(float x1, float y1, float x2, float y2){
    vertex(x1, y1, penDownPos);
    vertex(x2, y2, penDownPos);
}

void PiccoloLib::line(float x1, float y1, float z1, float x2, float y2, float z2){
    vertex(x1, y1, z1);
    vertex(x2, y2, z2);
}

void PiccoloLib::rect(float x, float y, float width,float height) {
    rect(x, y, penDownPos, width, height);
}

void PiccoloLib::rect(float x, float y, float z, float width,float height) {
    vertex(x, y, z);
    vertex(x+width, y, z);
    vertex(x+width, y+height, z);
    vertex(x, y+height, z);
    vertex(x, y, z);
}

void PiccoloLib::ellipse(float x, float y, float width, float height){
    if(width == 0 || height == 0)
        return;
    
    float arcStep = (stepSize/(width*PI));
    endShape();
    arc(x, y, width, height, 0, 0);
    beginShape();
    arc(x, y, width, height, 0, TWO_PI+arcStep);
    endShape();
}

void PiccoloLib::arc(float x , float y , float width, float height, float startA, float stopA){
    float arcStep = (stepSize/((width/2)*PI));

    for(float a=stopA ; a >= startA; a-=arcStep) {
        vertex((sin(a)*width) + x, (cos(a)*height) + y);
    }
}

void PiccoloLib::bezier(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2){
    float len = 0;
    float px = x1;
    float py = y1;
    
    //maybe this is to complex just make it a constant?
    //work out how long our bezier is
    for(float t=0; t<=1+0.1f; t+=0.1f){
        float x = bezierPoint(x1, cx1, cx2, x2, t);
        float y = bezierPoint(y1, cy1, cy2, y2, t);
        len += dist(x, y, 0, px, py, 0);
        px = x;
        py = y;   
    }
        
    float bezStep = 1.0f/(len/stepSize);
    
    for(float t2=0; t2 <=1+bezStep; t2+=bezStep){
        float x = bezierPoint(x1, cx1, cx2, x2, t2);
        float y = bezierPoint(y1, cy1, cy2, y2, t2);
        vertex(x, y);
    }
}

void PiccoloLib::bezierYZ(float y1, float z1, float cy1, float cz1, float cy2, float cz2, float y2, float z2){
    float len = 0;
    float py = y1;
    float pz = z1;
    
    for(float t=0; t<=1+0.1f; t+=0.1f){
        float y = bezierPoint(y1, cy1, cy2, y2, t);
        float z = bezierPoint(z1, cz1, cz2, z2, t);
        len += dist(y, z, 0, py, pz, 0);
        py = y;
        pz = z;   
    }
        
    float bezStep = 1.0f/(len/stepSize);
    
    for(float t2=0; t2 <=1+bezStep; t2+=bezStep){
        float y = bezierPoint(y1, cy1, cy2, y2, t2);
        float z = bezierPoint(z1, cz1, cz2, z2, t2);
        vertex(X.getPos(), y, z);
    }
}

float PiccoloLib::bezierPoint(float a, float b, float c, float d, float t) {
    float t1 = 1.0f - t;
    return a*t1*t1*t1 + 3*b*t*t1*t1 + 3*c*t*t*t1 + d*t*t*t;
}

float PiccoloLib::bezierTangent(float a, float b, float c, float d, float t) {
    return (3*t*t * (-a+3*b-3*c+d) +
            6*t * (a-2*b+c) +
            3 * (-a+b));
}


/* ================================== math functions ================================= */

float PiccoloLib::dist(float x1, float y1, float z1, float x2, float y2, float z2){
    return sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) + (z2-z1)*(z2-z1) );
}

float PiccoloLib::dist(float dx, float dy, float dz){
    return sqrt( dx*dx + dy*dy + dz*dz );
}


/*
float PiccoloLib::calcFloat(char *str){
return (float)(((int)strtoul(str,NULL,CHAR_PER_POS)))/100.0;
}


float PiccoloLib::calcFloat(int data[], int ind) {
    int sum;
    sum += (data[ind+0]-48)*10000;
    sum += (data[ind+1]-48)*1000;
    sum += (data[ind+2]-48)*100;
    sum += (data[ind+3]-48)*10;
    sum += (data[ind+4]-48);
    
    return float(sum/100.0);
}
*/

/* ================================ serial functions ================================= */



void PiccoloLib::serialSetup() {

    Serial.begin(115200);
    serialStream = false; // only set this if we want to stream back our movements to the console 

}


float PiccoloLib::calcFloat(byte b_1, byte b_2, byte b_3, byte b_4){
 long packed = 0;


//delay(1000);
//cast to long first so bits have room
 packed |= (long)b_1 << 24 ;
 packed |= (long)b_2 << 16 ;
 packed |= (long)b_3 << 8 ;
 packed |= (long)b_4 ;

//return (float)packed; 
return((float)packed)/100.0f;
}

void PiccoloLib::serialLoop() {


/*
Command Bytes 

S   connect 
B   received command (from piccolo)
G   sending pos, the next 12 bytes are x,y,z packed down into 4 byte ints
;   finished sending pos
E   go home

*/

    
    while (Serial.available() > 0) {

        inByte = Serial.read();

        //Received end of position byte. 
       if(waitingForPos && inByte == COMMAND_POS_END_BYTE  && index == 12 ) {
            float xPosIn;
            float yPosIn;
            float zPosIn;

            xPosIn = calcFloat((byte)inString[3],(byte)inString[2],(byte)inString[1],(byte)inString[0]);
            yPosIn = calcFloat((byte)inString[7],(byte)inString[6],(byte)inString[5],(byte)inString[4]);
            zPosIn = calcFloat((byte)inString[11],(byte)inString[10],(byte)inString[9],(byte)inString[8]);

            index = 0;
            waitingForPos =false;

            vertex(xPosIn, yPosIn, zPosIn);
            Serial.println(COMMAND_SEND_NEXT);


        
        }
        //Received start of pos byte, wait for position bytes 
        else if(inByte == COMMAND_POS_START_BYTE && !waitingForPos){
        waitingForPos = true;
        index = 0;
        }

        //received connect byte, reset bytes and notify host that we're ready to plot
        else if(!waitingForPos && inByte == COMMAND_CONNECT) {
            index = 0;
            for (int i=0; i<25; i++) {
                inString[i] = 0;
            }
            Serial.println(COMMAND_READY);
            delay(300);
            Serial.println(COMMAND_SEND_NEXT);
            waitingForPos = false;
            
        }
        //Go home
        else if(inByte == COMMAND_END_STACK && !waitingForPos) {
            home();
        }
        //everything else
        else {
            inString[index] = inByte;
            index++;
        }

    }

 
    
}

