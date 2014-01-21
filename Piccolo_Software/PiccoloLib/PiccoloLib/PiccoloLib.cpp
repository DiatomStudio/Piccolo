
#include "PiccoloLib.h"


PiccoloLib::PiccoloLib(){
    

}


void PiccoloLib::setup(){
    setup(SERVO_DEFAULT_MIN_X,SERVO_DEFAULT_MAX_X,SERVO_DEFAULT_MIN_Y,SERVO_DEFAULT_MAX_Y,SERVO_DEFAULT_MIN_Z,SERVO_DEFAULT_MAX_Z,PICCOLO_DEFAULT_BED_WIDTH,PICCOLO_DEFAULT_BED_HEIGHT,PICCOLO_DEFAULT_BED_DEPTH);

}

void PiccoloLib::setup(int servoMinX, int servoMaxX, int servoMinY, int servoMaxY, int servoMinZ, int servoMaxZ , int piccoloBedWidth, int piccoloBedHeight, int piccoloBedDepth){
    
    beginShapeFlag = false;
    
    index = 0;
    gotPos = false;
    ready = false;
    
    xPosIn = 0;
    yPosIn = 0;
    zPosIn = 0;
    sendToDebugConsole = false;
    
    setStepResolution(PICCOLO_DEFAULT_STEP_SIZE);
    setSpeed(PICCOLO_DEFAULT_MM_PER_SEC);
    
    //bed size
    bedWidth = piccoloBedWidth;
    bedHeight= piccoloBedHeight;
    bedDepth = piccoloBedDepth;
    
    //Servo min and max positions
    setServosMinMax(servoMinX,servoMaxX,servoMinY,servoMaxY,servoMinZ,servoMaxZ);
    
    invertAxis(false,false,false); /// set all axis to normal not flipped.
    
    penUpPos = PICCOLO_DEFAULT_PEN_UP_POS;
    penDownPos = PICCOLO_DEFAULT_PEN_DOWN_POS;
    
    
    //set the current position to 0,150,100
    xPos = 2;
    yPos = piccoloBedWidth/2;
    zPos = penUpPos;
    
    pinMode(BUTTON_ONE_PIN, INPUT_PULLUP);           // set pin to input
    pinMode(BUTTON_TWO_PIN, INPUT_PULLUP);           // set pin to input
    //pinMode(THUMBWHEEL_PIN, INPUT);
    

    setServoPins(SERVO_X_PIN,SERVO_Y_PIN,SERVO_Z_PIN);
    home();
    
}



void PiccoloLib::setServosMinMax(int minX,int maxX,int minY,int maxY,int minZ,int maxZ){
    
    xMin = minX;
    xMax = maxX;
    yMin = minY;
    yMax = maxY;
    zMin = minZ;
    zMax = maxZ;
    
    //pre calculate axis scales
    xScale = (xMax-xMin)/bedWidth;
    yScale = (yMax-yMin)/bedHeight;
    zScale = (zMax-zMin)/bedDepth;
    
}


void PiccoloLib::setServoPins(int xPin,int yPin, int zPin){
    xServo.attach(xPin);
    yServo.attach(yPin);
    zServo.attach(zPin);
}


void PiccoloLib::invertAxis(boolean xAxis, boolean yAxis, boolean zAxis ){
    invertX = xAxis;
    invertY = yAxis;
    invertZ = zAxis;
}

void PiccoloLib::loop(){
    
}


void PiccoloLib::setStepResolution(float resolution){
    stepSize = resolution; // smaller step means more detail and slower paths
}

void PiccoloLib::setSpeed(float mmPerSecond){
    delayPerStep =   (5000.0/mmPerSecond) - ((1.0/stepSize)*50.0);

}


void PiccoloLib::rect(float x, float y, float width,float height) {
    rect(x,y,penDownPos,width,height);
}

void PiccoloLib::rect(float x, float y, float z, float width,float height) {
    beginShape();
    vertex(x,y,z);
    vertex(x+width,y,z);
    vertex(x+width,y+height,z);
    vertex(x,y+height,z);
    vertex(x,y,z);
    endShape();
}

void PiccoloLib::line(float x1,float y1,float x2,float y2){
    line(x1,y1,penDownPos,x2,y2,penDownPos);
}

void PiccoloLib::line(float x1,float y1,float z1,float x2,float y2,float z2){
    beginShape();
    vertex(x1,y1,z1);
    vertex(x2,y2,z2);
    endShape();
}

void PiccoloLib::vertex(float x, float y) {
    vertex(x,y,penDownPos);
}

void PiccoloLib::vertex(float x, float y,float z) {
    
    
    if(beginShapeFlag){
       // zPos =penDownPos;
        beginShapeFlag = false;
        vertex(xPos,yPos,penUpPos);
    }
    
    
    float xDelta = x-xPos ;
    float yDelta = y-yPos;
    float zDelta = z-zPos;
    
    float maxMove = max(abs(xDelta),abs(yDelta));
    maxMove = max(maxMove,abs(zDelta));
    int steps = (int)(maxMove/stepSize);
    
    //always move atleast 1 step
    if(steps == 0)
        steps = 1;
    
    for(int i = 0; i <= steps; i++){
        move(xPos+((xDelta/steps)*i),yPos+((yDelta/steps)*i),zPos+((zDelta/steps)*i));
        delayMicroseconds(delayPerStep);
        //delay(delayPerStep);
    }
    
    xPos = x;
    yPos = y;
    zPos = z;
    
    
}

void PiccoloLib::move(float x, float y){
    move(x,y,zPos);
}

void PiccoloLib::move(float x, float y, float z){
    
    //debugging send directly to console.
    if(sendToDebugConsole){
        Serial.print("G01,");
        Serial.print(x);
        Serial.print(",");
        Serial.print(y);
        Serial.print(",");
        Serial.println(z);
    }else{
        
        x=(x*xScale)+xMin;
        y=(y*xScale)+yMin;
        z=(z*zScale)+zMin;
        
        x = (x<xMin)? xMin : x;
        x = (x>xMax)? xMax : x;
        
        y = (y<yMin)? yMin : y;
        y = (y>yMax)? yMax : y;
        
        z = (z<zMin)? zMin : z;
        z = (z>zMax)? zMax : z;
        
        
        if (invertY) {
            y = (yMax-yMin)- y + yMin;
        }
        if (invertX) {
            x = (xMax-xMin)- x + xMin;
        }
        if (invertZ) {
            z = (zMax-zMin)- z + zMin;
        }
        
        
        xServo.write(x);
        yServo.write(y);
        zServo.write(z);
        
    }
}


void PiccoloLib::bezier(float x1,float  y1,float  cx1,float  cy1,float  cx2,float  cy2,float  x2,float  y2){
    float len = 0;
    float px = x1;
    float py = y1;
    
    //maybe this is to complex just make it a constant?
    //work out how long our bezier is
    for(float t = 0; t <=1+0.1f ; t+=0.1f){
        float x = bezierPoint(x1,cx1,cx2,x2,t);
        float y = bezierPoint(y1,cy1,cy2,y2,t);
        len+=dist(x,y,0,px,py,0);
        px=x;
        py=y;
        
    }
    
    
    beginShape();
    float bezStep = 1.0f/(len/stepSize);
    
    for(float t2 = 0; t2 <=1+bezStep ; t2+=bezStep){
        float x = bezierPoint(x1,cx1,cx2,x2,t2);
        float y = bezierPoint(y1,cy1,cy2,y2,t2);
        vertex(x,y);
    }
    endShape();
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

void PiccoloLib::ellipse(float x, float y, float width, float height){
    if(width == 0 || height == 0)
        return;
    
    float arcStep = (stepSize/(width*PI));
    endShape();
    arc(x,y,width,height,0,0);
    beginShape();
    arc(x,y,width,height,0,TWO_PI+arcStep);
    endShape();
}

void PiccoloLib::arc(float x , float y , float width, float height, float startA, float stopA){
    float arcStep = (stepSize/((width/2)*PI));
    
    for(float a=stopA ; a >= startA; a-=arcStep) {
        vertex((sin(a)*width)+x,(cos(a)*height)+y);
    }
}

void PiccoloLib::beginShape(){
    beginShapeFlag = true;
}


void PiccoloLib::endShape(){
    vertex(xPos,yPos,penUpPos);
}



//get pos
float PiccoloLib::getX(){
    return xPos;
}

float PiccoloLib::getY(){
    return yPos;
}

float PiccoloLib::getZ(){
    return zPos;
}


int PiccoloLib::getBedWidth(){
    return bedWidth;
}
int PiccoloLib::getBedHeight(){
    return bedHeight;
}
int PiccoloLib::getBedDepth(){
    return bedDepth;
}



//Math Functions
float PiccoloLib::dist(float x1, float y1,float z1,float x2,float y2,float z2){
    return sqrt(((x1-x2)*(x1-x2))+((y1-y2)*(y1-y2))+((z1-z2)*(z1-z2)));
}



//Piccolo Mechanical Functions
void PiccoloLib::home() {
    vertex(2, bedWidth/2, penUpPos);

}


void PiccoloLib::setPressure() {
    
    /*
     float zHeight = analogRead(0);
     zHeight = map(zHeight,0,1024,0,115);
     zServo.write(zHeight);
     int zmin = zHeight;
     zscale = bedheight/(zmax-zmin);
     delay(15);
     */
}


void PiccoloLib::setPenDownPos(float pos){
    penDownPos = pos;
    penUpPos = penDownPos + 5;
}

float PiccoloLib::getPenDownPos(){
    return penDownPos;
}


//Serial functions

// SERIAL VARS //
void PiccoloLib::serialSetup() {
    Serial.begin(115200);
    
    
    //home();
    
    //  establishContact();
    
    delay(1000);
    // Serial.println('B');  // Start asking for coordinates
    
}


void PiccoloLib::serialLoop() {
    
    //while (!gotPos) {
    while (Serial.available() > 0) {
        inByte = Serial.read();
        if(inByte == ';') {
            xPosIn = calcFloat(inString,0);
            yPosIn = calcFloat(inString,5);
            zPosIn = calcFloat(inString,10);
            
            index = 0;
            gotPos = true;
        }
        else if(inByte == 'E') {
            home();
        }
        else if(inByte == 'S') {
            //        /establishContact();'
            index = 0;
            for (int i=0; i<15; i++) {
                inString[i] = 0;
            }
            Serial.println('B');
            delay(300);
        }
        else {
            inString[index] = int(inByte);
            index++;
            // Serial.println('C');
            // delay(2);
        }
    }
    //}
    
    if (gotPos) {
        /*
         for (int i=0; i<15; i++) {
         Serial.print(inString[i]-48);
         }
         Serial.println();
         delay(200);
         
         Serial.print('x');
         Serial.print(xPosIn);
         
         Serial.print('y');
         Serial.print(yPosIn);
         
         Serial.print('z');
         Serial.print(zPosIn);
         
         Serial.println();
         delay(200);
         */
        vertex(xPosIn, yPosIn, zPosIn);
        gotPos = false;
        for (int i=0; i<15; i++) {
            inString[i] = 0;
        }
        
        Serial.println('B');
    }
    
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


void PiccoloLib::establishContact() {
    
    while (Serial.available() <= 0) {
        Serial.println('A');
        delay(300);
    }
    
}

float PiccoloLib::getThumbwheelVal(){
    return analogRead(THUMBWHEEL_PIN);
}

boolean PiccoloLib::btnOneDown(){
    return !digitalRead(BUTTON_ONE_PIN);
}

boolean PiccoloLib::btnTwoDown(){
    return !digitalRead(BUTTON_TWO_PIN);
}