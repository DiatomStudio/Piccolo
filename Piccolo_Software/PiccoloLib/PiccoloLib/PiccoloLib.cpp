
#include "PiccoloLib.h"


Piccolo::Piccolo(){}

/* ================================= setup functions ================================= */

void Piccolo::setup() {
    setup(SERVO_X_PIN, SERVO_Y_PIN, SERVO_Z_PIN);
}

void Piccolo::setup(int xPin, int yPin, int zPin){

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
    thumbwheelZcontrol = false;
    serialStream = false;
    disableMotion = false;
    
    pinMode(BUTTON_ONE_PIN, INPUT_PULLUP);           // set pin to input
    pinMode(BUTTON_TWO_PIN, INPUT_PULLUP);           // set pin to input
    //pinMode(THUMBWHEEL_PIN, INPUT);                // not needed for analog pin
    
    index = 0;
    gotPos = false;
    
    
}

void Piccolo::invert(boolean invertX, boolean invertY, boolean invertZ){
    X.invert(invertX);
    Y.invert(invertY);
    Z.invert(invertZ);
}

void Piccolo::moveCenter(float xOffset, float yOffset) {
    X.moveCenter(xOffset);
    Y.moveCenter(yOffset);
}

void Piccolo::moveCenter(float xOffset, float yOffset, float zOffset) {
    X.moveCenter(xOffset);
    Y.moveCenter(yOffset);
    Z.moveCenter(zOffset);
}

void Piccolo::calibrate(float targetDimension, float actualX, float actualY) {
    X.calcuSdeg(targetDimension, actualX);
    Y.calcuSdeg(targetDimension, actualY);
}

void Piccolo::calibrate(float targetDimension, float actualX, float actualY, float actualZ) {
    X.calcuSdeg(targetDimension, actualX);
    Y.calcuSdeg(targetDimension, actualY);
    Z.calcuSdeg(targetDimension, actualZ);
}

void Piccolo::setBedSize(float size) {
    X.setBedSize(size);
    Y.setBedSize(size);
    Z.setBedSize(size);
}

void Piccolo::setBedSize(float xySize, float zSize) {
    X.setBedSize(xySize);
    Y.setBedSize(xySize);
    Z.setBedSize(zSize);
}


void Piccolo::setBedSize(float xSize, float ySize, float zSize) {
    X.setBedSize(xSize);
    Y.setBedSize(ySize);
    Z.setBedSize(zSize);
}

void Piccolo::setSpeed(float _mmPerSecond){
    mmPerSecond = _mmPerSecond; 
    calcDelayPerStep();
}

void Piccolo::setStepSize(float _stepSize){
    stepSize = _stepSize; 
    calcDelayPerStep();
}
    
void Piccolo::calcDelayPerStep(){
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

void Piccolo::moveX(float x){
    move(x, Y.getPos(), Z.getPos());
}
void Piccolo::moveY(float y){
    move(X.getPos(), y, Z.getPos());
}
void Piccolo::moveZ(float z){
    move(X.getPos(), Y.getPos(), z);
}
void Piccolo::move(float x, float y){
    move(x, y, Z.getPos());
}

void Piccolo::move(float x, float y, float z){

    // To remove and replace
    /*
    if (thumbwheelZcontrol && drawing) {
        z = map(readThumbwheel(), 0, 1024, -Z.getBedSize()/2, Z.getBedSize()/2);
        setPenDownPos(z);
    }
    */

    if (serialStream) {
        Serial.print("x:");
        Serial.print(x);
        Serial.print("\t"); //horizontal tab?
        Serial.print("y:");
        Serial.print(y);
        Serial.print("\t"); //horizontal tab?
        Serial.print("z");
        Serial.println(z);
    }

    if (!disableMotion) {
        X.move(x);
        Y.move(y);
        Z.move(z);
    }

}

void Piccolo::home() {
    vertex(-X.getBedSize()/2, 0, penUpPos);
}

void Piccolo::thumbwheelControlX(){
    float x = map(readThumbwheel(), 0, 1024, -X.getBedSize()/2, X.getBedSize()/2);
    moveX(x);
}
void Piccolo::thumbwheelControlY(){
    float y = map(readThumbwheel(), 0, 1024, -Y.getBedSize()/2, Y.getBedSize()/2);
    moveY(y);
}
void Piccolo::thumbwheelControlZ(){
    float z = map(readThumbwheel(), 0, 1024, -Z.getBedSize()/2, Z.getBedSize()/2);
    moveZ(z);
}

void Piccolo::setPenDownPos(float _penDownPos){
    penDownPos = _penDownPos;
    penUpPos = penDownPos + 5;
}

float Piccolo::getPenDownPos(){
    return penDownPos;
}


/* ============================== piccolo brain inputs =============================== */

float Piccolo::readThumbwheel(){
    return analogRead(THUMBWHEEL_PIN);
}

boolean Piccolo::buttonOneDown(){
    return !digitalRead(BUTTON_ONE_PIN);
}

boolean Piccolo::buttonTwoDown(){
    return !digitalRead(BUTTON_TWO_PIN);
}


/* ================================== draw functions ================================= */

void Piccolo::beginDraw(){
    beginDrawFlag = true;
}

void Piccolo::endDraw(){
    vertex(X.getPos(), Y.getPos(), penUpPos);
    drawing = false;
}

void Piccolo::vertex(float x, float y) {
    vertex(x, y, Z.getPos());
}

void Piccolo::vertex(float x, float y, float z) {
    
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
                    px + (float(dx/numSteps) * i),
                    py + (float(dy/numSteps) * i),
                    pz + (float(dz/numSteps) * i)
                );
            delay(delayPerStep);
        }
        move(x, y, z); // move to final position as last step.
        delay(delayPerStep);
    }

}

void Piccolo::line(float x1, float y1, float x2, float y2){
    vertex(x1, y1, penDownPos);
    vertex(x2, y2, penDownPos);
}

void Piccolo::line(float x1, float y1, float z1, float x2, float y2, float z2){
    vertex(x1, y1, z1);
    vertex(x2, y2, z2);
}

void Piccolo::rect(float x, float y, float width,float height) {
    rect(x, y, penDownPos, width, height);
}

void Piccolo::rect(float x, float y, float z, float width,float height) {
    vertex(x, y, z);
    vertex(x+width, y, z);
    vertex(x+width, y+height, z);
    vertex(x, y+height, z);
    vertex(x, y, z);
}

void Piccolo::ellipse(float x, float y, float width, float height){
    if(width == 0 || height == 0)
        return;
    
    float arcStep = (stepSize/(width*PI));
    endDraw();
    arc(x, y, width, height, 0, 0);
    beginDraw();
    arc(x, y, width, height, 0, TWO_PI+arcStep);
    endDraw();
}

void Piccolo::arc(float x , float y , float width, float height, float startA, float stopA){
    float arcStep = (stepSize/((width/2)*PI));
    
    for(float a=stopA ; a >= startA; a-=arcStep) {
        vertex((sin(a)*width) + x, (cos(a)*height) + y);
    }
}

void Piccolo::bezier(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2){
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

float Piccolo::bezierPoint(float a, float b, float c, float d, float t) {
    float t1 = 1.0f - t;
    return a*t1*t1*t1 + 3*b*t*t1*t1 + 3*c*t*t*t1 + d*t*t*t;
}

float Piccolo::bezierTangent(float a, float b, float c, float d, float t) {
    return (3*t*t * (-a+3*b-3*c+d) +
            6*t * (a-2*b+c) +
            3 * (-a+b));
}


/* ================================== math functions ================================= */

float Piccolo::dist(float x1, float y1, float z1, float x2, float y2, float z2){
    return sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) + (z2-z1)*(z2-z1) );
}

float Piccolo::dist(float dx, float dy, float dz){
    return sqrt( dx*dx + dy*dy + dz*dz );
}

float Piccolo::calcFloat(int data[], int ind) {
    int sum;
    sum += (data[ind+0]-48)*10000;
    sum += (data[ind+1]-48)*1000;
    sum += (data[ind+2]-48)*100;
    sum += (data[ind+3]-48)*10;
    sum += (data[ind+4]-48);
    
    return float(sum/100.0);
}


/* ================================ serial functions ================================= */



void Piccolo::serialSetup() {

    Serial.begin(115200);
    serialStream = true;

}


void Piccolo::serialLoop() {

    float xPosIn;
    float yPosIn;
    float zPosIn;
    
    while (Serial.available() > 0) {

        inByte = Serial.read();
        if(inByte == 'S') {
            index = 0;
            for (int i=0; i<15; i++) {
                inString[i] = 0;
            }
            Serial.println('B');
            delay(300);
        }
        else if(inByte == ';') {
            xPosIn = calcFloat(inString,0);
            yPosIn = calcFloat(inString,5);
            zPosIn = calcFloat(inString,10);
            index = 0;
            gotPos = true;
        }
        else if(inByte == 'E') {
            home();
        }
        
        else {
            inString[index] = int(inByte);
            index++;
        }

    }
    
    if (gotPos) {
        vertex(xPosIn, yPosIn, zPosIn);
        gotPos = false;
        for (int i=0; i<15; i++) {
            inString[i] = 0;
        }
        Serial.println('B');
    }
    
}

