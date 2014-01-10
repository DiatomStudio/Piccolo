
float forkSpread = PI*0.75f;
float bezDetail = 3;
boolean OnlyCentreLines = false; 


void generateForest() {
  background(255);
  smooth();
  noFill();
  float step = 100;
  for (float i = 0; i < width; i+=step) { 

    float lightLevel = i/width;
    branch(i, 290, -PI/2, lightLevel*150, 5, 0, lightLevel, g);
  }
}



void generatePlant( float lightLevel, PGraphics g){
      branch(150,290, -PI/2, 150*lightLevel, (int)(6*lightLevel), 0, lightLevel, g);
      
      g.point(1, 150);
}

void branch(float x1, float y1, float dir, float len, int forks, int itteration, float lightLevel, PGraphics g) {

  stroke(0);

  float bottomBendFactor = 1-lightLevel;//0.1f; //less light more bend
  float topBendFactor = 1-lightLevel;//0.1f; //less light more bend


  float offsetBottom = len /10.0f;
  float offsetTop = len / 20.0f;

  float leafThickness =  20;
  float stalkRatio = 0.25;



  if (lightLevel < 0.5)//no branches under 50% light
    forks=0;
  else
    forks--;



  boolean isFern = false;
  if (itteration == 0 && forks == 0) {
    isFern = true;
  }


  int leafOnBranch = 0;
  if (lightLevel < 0.5)//put leaves on the stalk of plants under 50% light
    leafOnBranch = 10;

  float x2 = cos(dir)*len + x1;
  float y2 = sin(dir)*len + y1;

  float a = dir+random(-PI*bottomBendFactor, PI*bottomBendFactor);
  float cx1 = cos(a)*len/2 + x1;
  float cy1 = sin(a)*len/2 + y1;

  a = dir+PI+random(-PI*topBendFactor, PI*topBendFactor);
  float cx2 = cos(a)*len/2 + x2;
  float cy2 = sin(a)*len/2 + y2;

  float tx2 = bezierTangent(x1, cx1, cx2, x2, 1);
  float ty2 = bezierTangent(y1, cy1, cy2, y2, 1);
  float endTangent = atan2(ty2, tx2);

  dir = endTangent;

  /*
  stroke(255,0,0);
   line(x1,y1,cx1,cy1);
   ellipse(cx1,cy1,5,5);
   line(x2,y2,cx2,cy2);
   ellipse(cx2,cy2,5,5);
   stroke(0);
   */


float step = (bezDetail/len);

  float ribCounter = 0;
  for (int i = 0; i <=3 ; i+=1) {
    g.beginShape();
    for (float t = 0 ; t <= 1+step; t+=step) {
      float x = bezierPoint(x1, cx1, cx2, x2, t);
      float y = bezierPoint(y1, cy1, cy2, y2, t);
      float tx = bezierTangent(x1, cx1, cx2, x2, t);
      float ty = bezierTangent(y1, cy1, cy2, y2, t);
      a = atan2(ty, tx);
      a -= HALF_PI;


      //Stem
      if (i == 0 || i ==1) {
        if(OnlyCentreLines){
          if(i == 0){
           g.vertex(  x, y);
          }
        }else{
        float flip = i==1? 1:-1;
        float w = ((1-t)*offsetBottom)+((t)*offsetTop) ;
        g.vertex( cos(a)*flip*w + x, sin(a)*flip*w + y);
        }
      }




      //fern
      if (i==3 && isFern) {
        ribCounter+=1;
        if (  ribCounter == 10) {
          ribCounter = 0;
          float w = ((1-t)*offsetBottom)+((t)*offsetTop) ;
          float w2 = sin(PI*t)*(len/2) ;

          float flip = 1;
          leaf(cos(a)*flip*w + x, sin(a)*flip*w + y, a, 15, forks, itteration+1, lightLevel,g);
          flip = -1;
          leaf(cos(a)*flip*w + x, sin(a)*flip*w + y, a+PI, w2, forks, itteration+1, lightLevel,g);
        }
      }
    }
    g.endShape();
  }



  float forkDeviation = 0.1;
  for (int i = 0; i < forks; i++) {
    float f = forks-1;

    if (f == 0)
      f = 1;
    a = (dir-(forkSpread/2.0f))+((forkSpread/(f))*i) + random(-PI*forkDeviation, PI*forkDeviation);

    branch(x2, y2, a, len*0.50, forks, itteration+1, lightLevel,g);
  }


  if (forks == 0  && !isFern) {
    for (int i = 0; i < 1; i++) {
      a = dir;//(dir-(forkSpread/2.0f))+((forkSpread/(2-1))*i) + random(-PI*forkDeviation, PI*forkDeviation);
      leaf(x2, y2, a, 30, forks, itteration+1, lightLevel,g);
    }
  }
}



void leaf(float x1, float y1, float dir, float len, int forks, int itteration, float lightLevel, PGraphics g) {

  
  
  float bottomBendFactor = 1-lightLevel;//0.1f; //less light more bend
  float topBendFactor = 1-lightLevel;//0.1f; //less light more bend

  float x2 = cos(dir)*len + x1;
  float y2 = sin(dir)*len + y1;

  float a = dir+random(-PI*bottomBendFactor, PI*bottomBendFactor);
  float cx1 = cos(a)*len/2 + x1;
  float cy1 = sin(a)*len/2 + y1;

  a = dir+PI+random(-PI*topBendFactor, PI*topBendFactor);
  float cx2 = cos(a)*len/2 + x2;
  float cy2 = sin(a)*len/2 + y2;

  float tx2 = bezierTangent(x1, cx1, cx2, x2, 1);
  float ty2 = bezierTangent(y1, cy1, cy2, y2, 1);
  float endTangent = atan2(ty2, tx2);

  dir = endTangent;
  float leafThickness = len/4;
  float stalkRatio = 0.25f;
  float offsetBottom = leafThickness/4;
  float offsetTop = 0;
  
  float step = (bezDetail/len);
    float ribCounter = 0;
  for (int i = 0; i <=4 ; i++) {
    g.beginShape();
    for (float t = 0 ; t <= 1+step; t+=step) {
      float x = bezierPoint(x1, cx1, cx2, x2, t);
      float y = bezierPoint(y1, cy1, cy2, y2, t);
      float tx = bezierTangent(x1, cx1, cx2, x2, t);
      float ty = bezierTangent(y1, cy1, cy2, y2, t);
      a = atan2(ty, tx);
      a -= HALF_PI;

//stem
if(i == 0 || i == 1){
  if(OnlyCentreLines){
    if(i == 0){
     g.vertex( x, y);
    }
    
  }else{
    
        if(i == 0){
     g.vertex( x, y);
    }
    /*
 float flip = i==0? 1:-1;
 
 float w = ((1-t)*offsetBottom)+((t)*offsetTop) ;
 g.vertex( cos(a)*flip*w + x, sin(a)*flip*w + y);
 */
  }

}


//outside of leaf
if(i == 2 || i == 3){
            float flip = i==2? 1:-1;
            float w = sin(PI*t)*(leafThickness) ;
            
            if (t < stalkRatio)
            w = ((1-t)*offsetBottom)+((t)*offsetTop) ;
            
            g.vertex( cos(a)*flip*w + x, sin(a)*flip*w + y);
          
}
          if (i == 4 && lightLevel > 0.5) {
            ribCounter+=1;
            if (  ribCounter == 20) {
              ribCounter = 0;
              float w = sin(PI*t)*(leafThickness) ;

              if (t < stalkRatio)
                w =0;

              g.endShape();
              g.line(x, y, cos(a)*w + x, sin(a)*w + y);
              g.line(x, y, cos(a)*-w + x, sin(a)*-w + y);
              g.beginShape();
            }
          }
        }
                  g.endShape();

      }
}

    


  
  
  


