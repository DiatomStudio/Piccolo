

float startHeight = 150;

void drawBrushMustache(PGraphics g){
  
  calculateControlPoint();
  
  
  //Dip Brush
  g.vertex(  250, 250,penLiftHeight);
  g.vertex(  250, 250,10);
  g.vertex(  250, 250,penLiftHeight);

  
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
    
  g.beginShape();
  for (float t = 0 ; t <= 1; t+=STEP) {
  float x = bezierPoint(rStartX,rStartC1X,rEndC1X,rEndX,t);
  float y = bezierPoint(rStartY,rStartC1Y,rEndC1Y,rEndY,t);
  float tx = bezierTangent(rStartX,rStartC1X,rEndC1X,rEndX,t);
  float ty = bezierTangent(rStartY,rStartC1Y,rEndC1Y,rEndY,t);
  float a = atan2(ty, tx);
  a -= HALF_PI;
  float height = startHeight - (sin(PI*t)*(startHeight)) ;
  g.vertex(  x, y,height);
}
g.endShape();



  g.beginShape();
  for (float t = 0 ; t <= 1; t+=STEP) {
  float x = bezierPoint(lStartX,lStartC1X,lEndC1X,lEndX,t);
  float y = bezierPoint(lStartY,lStartC1Y,lEndC1Y,lEndY,t);
  float tx = bezierTangent(lStartX,lStartC1X,lEndC1X,lEndX,t);
  float ty = bezierTangent(lStartY,lStartC1Y,lEndC1Y,lEndY,t);
  float a = atan2(ty, tx);
  a -= HALF_PI;
  float height = startHeight - (sin(PI*t)*(startHeight)) ;
  g.vertex( x, y,height);
}
g.endShape();

g.point(1, 150);  // home




  
}

