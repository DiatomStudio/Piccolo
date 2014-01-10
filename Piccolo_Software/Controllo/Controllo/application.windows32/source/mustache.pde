
float centreX = 150;
float centreY = 150;

float rStartX = centreX;
float rStartY = centreY;

float rEndX = centreX;
float rEndY = centreY;

float rStartC1X = 150;
float rStartC1Y = 100;

float rEndC1X = 300;
float rEndC1Y = 250;


float lStartX = centreX;
float lStartY = centreY;

float lEndX = centreX;
float lEndY = centreY;

float lStartC1X = 150;
float lStartC1Y = 100;

float lEndC1X = 300;
float lEndC1Y = 250;



float STEPS = 50;
int WHISKERS = 10;
void drawMustache(PGraphics g){
  
  calculateControlPoint();
  
 // bezier(rStartX,rStartY,rStartC1X,rStartC1Y,rEndC1X,rEndC1Y,rEndX,rEndY);
    
  //bezier(lStartX,lStartY,lStartC1X,lStartC1Y,lEndC1X,lEndC1Y,lEndX,lEndY);
  
   // randomSeed(analogRead(0));
    float thickness = random(5,40);
    WHISKERS = (int)random(1,15);
    
  float STEP = 1.0f/STEPS;
  
  /* Circles
    for (float t = 0 ; t <= 1; t+=STEP) {
    float x = bezierPoint(rStartX,rStartC1X,rEndC1X,rEndX,t);
    float y = bezierPoint(rStartY,rStartC1Y,rEndC1Y,rEndY,t);
    float width = sin(PI*t)*thickness;
    ellipse(x,y,width,width);
    }
    
    for (float t = 0 ; t <= 1; t+=STEP) {
    float x = bezierPoint(lStartX,lStartC1X,lEndC1X,lEndX,t);
    float y = bezierPoint(lStartY,lStartC1Y,lEndC1Y,lEndY,t);
    float width = sin(PI*t)*thickness;
    ellipse(x,y,width,width);
    }
    */
    
  for(int i = 0; i < WHISKERS ; i++){
  g.beginShape();
  for (float t = 0 ; t <= 1; t+=STEP) {
  float x = bezierPoint(rStartX,rStartC1X,rEndC1X,rEndX,t);
  float y = bezierPoint(rStartY,rStartC1Y,rEndC1Y,rEndY,t);
  float tx = bezierTangent(rStartX,rStartC1X,rEndC1X,rEndX,t);
  float ty = bezierTangent(rStartY,rStartC1Y,rEndC1Y,rEndY,t);
  float a = atan2(ty, tx);
  a -= HALF_PI;
  float width = sin(PI*t)*(i-(WHISKERS/2.0f))*(thickness/WHISKERS) ;
  g.vertex( cos(a)*width + x, sin(a)*width + y);
}
g.endShape();
}


  for(int i = 0; i < WHISKERS ; i++){
  g.beginShape();
  for (float t = 0 ; t <= 1; t+=STEP) {
  float x = bezierPoint(lStartX,lStartC1X,lEndC1X,lEndX,t);
  float y = bezierPoint(lStartY,lStartC1Y,lEndC1Y,lEndY,t);
  float tx = bezierTangent(lStartX,lStartC1X,lEndC1X,lEndX,t);
  float ty = bezierTangent(lStartY,lStartC1Y,lEndC1Y,lEndY,t);
  float a = atan2(ty, tx);
  a -= HALF_PI;
  float width = sin(PI*t)*(i-(WHISKERS/2.0f))*(thickness/WHISKERS) ;
  g.vertex( cos(a)*-width + x, sin(a)*-width + y);
}
g.endShape();
}



  
}

void calculateControlPoint(){
//  randomSeed(analogRead(0));
  
  lEndX =  centreX - random(50,150.0f);
  lEndY =  random(70,230);//centreY - random(200.0f)+100.0f;
  
  lStartC1X = lStartX - (random(100.0f));
  lStartC1Y = lStartY - (random(200.0f)-100.0f);
  
  lEndC1X = lEndX - min((random(300.0f)-150.0f),5);
  lEndC1Y = lEndY - (random(300.0f)-150.0f);
  
  
  
  rEndX = (centreX -lEndX)+centreX;
  rEndY = lEndY;
  
  rStartC1X = (centreX -lStartC1X)+centreX;
  rStartC1Y = lStartC1Y;
  
  rEndC1X = (centreX -lEndC1X)+centreX;
  rEndC1Y = lEndC1Y;
  
  
}





