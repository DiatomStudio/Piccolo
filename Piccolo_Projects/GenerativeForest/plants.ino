
//====================================== User Settings ======================================================//
float PATH_RESOLUTION = 0.01f; // resolution of curve paths. smaller number more accurate curve slower to draw
int LEAF_LIMIT = 30;// limit the number of leaves
boolean ENABLE_RANDOM_PRUNE = true; // randomly prune a tree limb and add a leaf



float LIGHT_LEVEL = 0; // This should be provided by a sensor
int leafCount = 0;
float bezDetail = 3;
boolean OnlyCentreLines = false; 


//====================================== generatePlant ======================================================//
void generatePlant( float lightLevel) {

  
  Serial.println(" generatePlant ");
  Serial.print(" lightLevel:  ");
  Serial.println(lightLevel);

  randomSeed((int)(lightLevel*1000.0)); // make random


  if (lightLevel < 0.35) {
    generateGrassesV2(lightLevel);
  }
  else if (lightLevel < 0.65) {
    generateFlax(lightLevel);
  }
  else {
    ///   generateFern(lightLevel);


    if(random(0,100) < 20){ //20% bubble trees
      geneateBubbleTree(lightLevel);
    }else{
    geneateTree(lightLevel);
    }
  }
  
  
  piccolo.home();
}



//====================================== generateGrasses V2 ======================================================//

void generateGrassesV2(float lightLevel) {

    Serial.println("generateGrasses ");


float noiseScale = 0.02;

    float dir = 1.0;
    
    piccolo.beginShape();
    piccolo.vertex((-piccolo.X.getBedSize()/2.0),piccolo.Y.getBedSize() / 2.0);
    piccolo.endShape();
       
       
    for (float y = piccolo.Y.getBedSize() / 2.0; y > (-piccolo.Y.getBedSize()/2.0)+5; y-=5) {
    for (float x = (-piccolo.X.getBedSize()/2.0)*dir; (x*dir) < piccolo.X.getBedSize()/2.0; x+=(random(1,10)*dir)) {
    piccolo.vertex(x,y);
    
            float lightReading =  1.0 - (analogRead(LDR_PIN) / LDR_TOP_LIMIT);


      if (random(100) < (200.0*(lightReading+0.05f))){
        
       float len = (lightReading+0.25f)*15.0;
       float noise = pnoise(x*noiseScale,(y*noiseScale)+lightReadingStart*100.0,1);
       float dir = (-PI/2.0f)+((-PI/2.0f)*noise);
         float x2 = cos(dir)*len + x;
         float y2 = sin(dir)*len + y;
  
  
  Serial.println(noise);
  
       piccolo.beginShape();
       piccolo.vertex(x,y);
       piccolo.vertex(x2,y2);
       piccolo.vertex(x+1,y);
       piccolo.endShape();

      }
       
        
    }
    dir*=-1;
  }
  
}




//====================================== generateGrasses ======================================================//

void generateGrasses(float lightLevel) {

    Serial.println("generateGrasses ");

    for (float y = piccolo.Y.getBedSize() / 2.0; y > -piccolo.Y.getBedSize()/2.0; y-=5) {
  for (float x = -piccolo.X.getBedSize()/2.0; x < piccolo.X.getBedSize()/2.0; x+=5) {

      if (random(100) < 20) // only grow grass 20% of the time
        generateGrass(x, y, lightLevel);
        
    }
  }
}
//====================================== generateGrass  ======================================================//

void generateGrass(float x, float y, float lightLevel) {
      Serial.println("generateGrass ");

  float forkSpread = PI;

  float forks = (int)random(1, 5);
  for (int i = 0; i < forks; i++) {
    float a = ((-PI/2.0)-(forkSpread/2.0f))+((forkSpread/(forks))*(i+0.5)) ;//+ random(-PI*forkDeviation, PI*forkDeviation);
    float bend = ((-PI/2.0)-a)/(PI/2.0);

    drawOffsetBezier(x, y, a, 10, bend*0.25, 0.25*bend, 0, 1.0);
  }
}







//====================================== generateFlax ======================================================//
void generateFlax(float lightLevel) {
Serial.println("generateFlax");

  float forkSpread = PI;

  float forks = (int)random(5, 20);
  for (int i = 0; i < forks; i++) {
    float a = ((-PI/2.0)-(forkSpread/2.0f))+((forkSpread/(forks))*(i+0.5)) ;//+ random(-PI*forkDeviation, PI*forkDeviation);
    float bend = ((-PI/2.0)-a)/(PI/2.0);

    drawOffsetBezier(0, piccolo.Y.getBedSize()/2.0, a, piccolo.X.getBedSize()*lightLevel*1.2, bend*0.25, 0.25*bend, 0, 4.0*lightLevel);
  }
}

//====================================== generateFern ======================================================//
void generateFern(float lightLevel) {
  Serial.println("generateFern");

}




//====================================== geneateTree ======================================================//
void geneateBubbleTree(float lightLevel) {
   Serial.println("geneateTree");
  leafCount = 0;
  branch(0, piccolo.Y.getBedSize()/2.0, -PI/2, piccolo.Y.getBedSize()/2.0f , 0, 0, lightLevel);
  piccolo.ellipse(0,0,piccolo.Y.getBedSize()*lightLevel*0.5f,piccolo.Y.getBedSize()*lightLevel*0.5f);
}



//====================================== geneateTree ======================================================//
void geneateTree(float lightLevel) {
   Serial.println("geneateTree");
  leafCount = 0;
  branch(0, piccolo.Y.getBedSize()/2.0, -PI/2, piccolo.Y.getBedSize() * 0.6*lightLevel, (int)(5.0*lightLevel), 0, lightLevel);
}




//====================================== Branch ======================================================//
void branch(float x1, float y1, float dir, float len, int forks, int itteration, float lightLevel) {

  float forkSpread = PI*lightLevel;

  float bottomBendFactor = 1-lightLevel;//0.1f; //less light more bend
  float topBendFactor = 1-lightLevel;//0.1f; //less light more bend

  topBendFactor*=(int)random(-2, 2);
  bottomBendFactor*=(int)random(-2, 2);

  float offsetBottom = len /10.0f; // tapper branch
  float offsetTop = len / 20.0f; //tapper branch

  float leafThickness =  20; //
  float stalkRatio = 0.25; // length of stalk

  drawOffsetBezier( x1, y1, dir, len, topBendFactor, bottomBendFactor, offsetTop, offsetBottom);

  float x2 = cos(dir)*len + x1;
  float y2 = sin(dir)*len + y1;


  float forkDeviation = 0.1;
  float a;


  if ((ENABLE_RANDOM_PRUNE && (random(100)<(10*itteration) ))|| leafCount > LEAF_LIMIT)
    forks = 0; //prune


  for (int i = 0; i < forks; i++) {
    a = (dir-(forkSpread/2.0f))+((forkSpread/(forks))*(i+0.5)) + random(-0.2, 0.2);
    branch(x2, y2, a, len*0.5, forks-1, itteration+1, lightLevel);
  }


  //end in a leaf :) 
  if (forks == 0 && itteration > 0) {
    a = dir;//(dir-(forkSpread/2.0f))+((forkSpread/(2-1))*i) + random(-PI*forkDeviation, PI*forkDeviation);
    leaf(x2, y2, dir, 7.0, forks, itteration+1, lightLevel);
    leafCount ++;
  }
}





//====================================== drawOffsetBezier ======================================================//
void drawOffsetBezier(float x1, float y1, float dir, float len, float topBendFactor, float bottomBendFactor, float offsetTop, float offsetBottom) {
  float x2 = cos(dir)*len + x1;
  float y2 = sin(dir)*len + y1;

  float a = dir+(PI*bottomBendFactor);//random(-PI*bottomBendFactor, PI*bottomBendFactor);
  float cx1 = cos(a)*len/2 + x1;
  float cy1 = sin(a)*len/2 + y1;

  a = dir+PI+(-PI*topBendFactor);//random(-PI*topBendFactor, PI*topBendFactor);
  float cx2 = cos(a)*len/2 + x2;
  float cy2 = sin(a)*len/2 + y2;

  float tx2 = piccolo.bezierTangent(x1, cx1, cx2, x2, 1);
  float ty2 = piccolo.bezierTangent(y1, cy1, cy2, y2, 1);
  float endTangent = atan2(ty2, tx2);

  dir = endTangent;


  drawOffsetBezier( x1, y1, cx1, cy1, x2, y2, cx2, cy2, offsetTop, offsetBottom);
}


//====================================== drawOffsetBezier ======================================================//
void drawOffsetBezier(float x1, float y1, float cx1, float cy1, float x2, float y2, float cx2, float cy2, float offsetTop, float offsetBottom) {

  float a;
  for (int i = 0; i <=2 ; i+=1) {
    piccolo.beginShape();
    for (float t = 0 ; t <= 1; t+=PATH_RESOLUTION) {
      float x = piccolo.bezierPoint(x1, cx1, cx2, x2, t);
      float y = piccolo.bezierPoint(y1, cy1, cy2, y2, t);
      float tx = piccolo.bezierTangent(x1, cx1, cx2, x2, t);
      float ty = piccolo.bezierTangent(y1, cy1, cy2, y2, t);
      a = atan2(ty, tx);
      a -= HALF_PI;


      //Stem
      if (i == 0 || i ==1) {
        float flip = i==1? 1:-1;
        float w = ((1-t)*offsetBottom)+((t)*offsetTop) ;
        piccolo.vertex( cos(a)*flip*w + x, sin(a)*flip*w + y);
      }




      /*
      //fern
       if (i==3 && isFern) {
       ribCounter+=1;
       if (  ribCounter == 10) {
       ribCounter = 0;
       float w = ((1-t)*offsetBottom)+((t)*offsetTop) ;
       float w2 = sin(PI*t)*(len/2) ;
       
       float flip = 1;
       leaf(cos(a)*flip*w + x, sin(a)*flip*w + y, a, 15, forks, itteration+1, lightLevel);
       flip = -1;
       leaf(cos(a)*flip*w + x, sin(a)*flip*w + y, a+PI, w2, forks, itteration+1, lightLevel);
       }
       }
       }
       */
    }
    piccolo.endShape();
  }
}



//====================================== Leaf ======================================================//
void leaf(float x1, float y1, float dir, float len, int forks, int itteration, float lightLevel) {



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

  float tx2 = piccolo.bezierTangent(x1, cx1, cx2, x2, 1);
  float ty2 = piccolo.bezierTangent(y1, cy1, cy2, y2, 1);
  float endTangent = atan2(ty2, tx2);

  dir = endTangent;
  float leafThickness = len/4;
  float stalkRatio = 0.25f;
  float offsetBottom = leafThickness/4;
  float offsetTop = 0;

  float step = (bezDetail/len);
  float ribCounter = 0;
  for (int i = 0; i <=4 ; i++) {
    piccolo.beginShape();
    for (float t = 0 ; t <= 1+step; t+=step) {
      float x = piccolo.bezierPoint(x1, cx1, cx2, x2, t);
      float y = piccolo.bezierPoint(y1, cy1, cy2, y2, t);
      float tx = piccolo.bezierTangent(x1, cx1, cx2, x2, t);
      float ty = piccolo.bezierTangent(y1, cy1, cy2, y2, t);
      a = atan2(ty, tx);
      a -= HALF_PI;

      //stem
      if (i == 0 || i == 1) {
        if (OnlyCentreLines) {
          if (i == 0) {
            piccolo.vertex( x, y);
          }
        }
        else {

          if (i == 0) {
            piccolo.vertex( x, y);
          }
          /*
 float flip = i==0? 1:-1;
           
           float w = ((1-t)*offsetBottom)+((t)*offsetTop) ;
           g.vertex( cos(a)*flip*w + x, sin(a)*flip*w + y);
           */
        }
      }


      //outside of leaf
      if (i == 2 || i == 3) {
        float flip = i==2? 1:-1;
        float w = sin(PI*t)*(leafThickness) ;

        if (t < stalkRatio)
          w = ((1-t)*offsetBottom)+((t)*offsetTop) ;

        piccolo.vertex( cos(a)*flip*w + x, sin(a)*flip*w + y);
      }
      if (i == 4 && lightLevel > 0.5) {
        ribCounter+=1;
        if (  ribCounter == 20) {
          ribCounter = 0;
          float w = sin(PI*t)*(leafThickness) ;

          if (t < stalkRatio)
            w =0;

          piccolo.endShape();
          piccolo.line(x, y, cos(a)*w + x, sin(a)*w + y);
          piccolo.line(x, y, cos(a)*-w + x, sin(a)*-w + y);
          piccolo.beginShape();
        }
      }
    }
    piccolo.endShape();
  }
}
