



class Face {


  /*
  All Drawing commands assume they are rendered around a 0,0 origin 
   
   */
  
  
  float bedWidth    =   50; 
  float bedHeight   =   50;
  
  int BOY = 1;
  int GIRL = 0;
  int geneder = 0;


  static final int BALD = 0;
  static final int PIGTAILS = 1; 
  static final int SHORT = 2; 
  static final int LONG = 3;
  
  float age = 10; //
  float hairLength = 15; // in pixels. 
  public int hairStyle = PIGTAILS;

  boolean eyesOpen = true; 
  float eyeWidth = 2.0;
  float eyeHeight = 4.0;
  boolean hasGlasses = false;

  //nose
  float noseHeight = 5;
  float noseWidth = -3;
   
  //mouth
  float mouthWidth = 10; 
  float smileHeight = 0; 
  float mouthOpen = 0;


  public float headHeight = 40;
  public float headWidth = 25;
 
 
  public float fringeHeight = 5;
  
  
  float headControlTop = 10;  //control point top
  float headControlBottom = 15;  //controlPoint chin

  float chinControlTop =  15; //cheakbone ControlPoint
  float chinControlBottom = 3.0;//chin control point
  //Generated points 
  PVector shoulderPoint = null;


  Face() {
  } 

  void draw(PGraphics g) {
    g.pushMatrix();



    if(hairStyle == LONG)
    drawHairBack(g);
    //Head
    //drawHead(g);
    drawChin(g);

    //Nose
    g.pushMatrix();
    g.translate(0, 2);
    drawNose(g);
    g.popMatrix();


    //Mouth
    g.pushMatrix();
    g.translate(0, 12);
    drawMouth(g);
    g.popMatrix();


    //Eyes
    g.pushMatrix();
    g.translate(0, 0);
    drawEyes(g); // drawEyes. 

    if (hasGlasses)
      drawGlasses(g);

    drawEyebrows(g);

    g.popMatrix();
    
    
    drawShoulders(g);
    drawHair(g);
    
    g.popMatrix();
   
   
   
   
  }






/*================================ draw head ================================*/
/*
Chage to draw chin, heads look bald :P 
*/
  void drawHead(PGraphics g) {

    g.beginShape();
    g.vertex(0, headHeight/2); // start
    g.bezierVertex(headControlBottom, headHeight/2, headControlTop, -headHeight/2, 0, -headHeight/2);
    g.bezierVertex(-headControlTop, -headHeight/2, -headControlBottom, headHeight/2, 0, headHeight/2);
    g.endShape();
    
    float x = bezierPoint(0,  headControlBottom,headControlTop, 0, 0.25f);
    float y = bezierPoint(headHeight/2.0f, headHeight/2.0f, -headHeight/2.0f, -headHeight/2.0f, 0.25f);

    shoulderPoint = new PVector(x,y);
  }
  
  
  
  
  /*================================ draw chin ================================*/
    void drawChin(PGraphics g) {



    g.beginShape();
    g.vertex(headWidth/2.0f, -fringeHeight); // start
    g.bezierVertex(headWidth/2.0f, chinControlTop, chinControlBottom, headHeight/2, 0, headHeight/2);
    g.bezierVertex(-chinControlBottom, headHeight/2, -headWidth/2.0f, chinControlTop, -headWidth/2.0f, -fringeHeight);
    g.endShape();
    
    
    float x = bezierPoint(headWidth/2.0f,  headWidth/2.0f,chinControlBottom, 0, 0.5f);
    float y = bezierPoint(-fringeHeight, chinControlTop, headHeight/2.0f, headHeight/2.0f, 0.5f);

    shoulderPoint = new PVector(x,y);

  }
  
  
  
  
  
  
  
  
/*================================ draw eyes ================================*/

  void drawEyes(PGraphics g) {
    float eyeSpacing  = 10;//




    if (eyesOpen) {
      //left eye
      g.ellipse(-eyeSpacing/2, 0, eyeWidth, eyeHeight);
      g.ellipse(-eyeSpacing/2, 0, eyeWidth/2, eyeHeight/2);

      //right eye
      g.ellipse(eyeSpacing/2, 0, eyeWidth, eyeHeight);
      g.ellipse(eyeSpacing/2, 0, eyeWidth/2.0, eyeHeight/2);
    } else {

      float cHeight = -2; 

      //left eye 
      g.beginShape();
      g.vertex((-eyeSpacing/2)-(eyeWidth/2), 0);
      g.bezierVertex((-eyeSpacing/2)-(eyeWidth/2), cHeight, (-eyeSpacing/2)+(eyeWidth/2), cHeight, (-eyeSpacing/2)+(eyeWidth/2), 0);
      g.endShape();

      //right eye
      g.beginShape();
      g.vertex((eyeSpacing/2)-(eyeWidth/2), 0);
      g.bezierVertex((eyeSpacing/2)-(eyeWidth/2), cHeight, (eyeSpacing/2)+(eyeWidth/2), cHeight, (eyeSpacing/2)+(eyeWidth/2), 0);
      g.endShape();
    }
  }










/*================================ draw glasses ================================*/
  void drawGlasses(PGraphics g) {

    float glassesHeight = 15; 
    float lenseWidth = 18; 
    float lenseSpacing = 2; 

    float cTopHeightOutside = 0; 
    float cTopHeightInside = 0; 

    float cBottomHeightOutside = 7; 
    float cBottomHeightInside = 10; 

    g.noFill();
    g.pushMatrix();
    g.translate(0, -3);
    //Left lense
    g.beginShape();
    g.vertex((-lenseSpacing/2) - (lenseWidth/2), 0);
    g.bezierVertex((-lenseSpacing/2) - (lenseWidth/2), cTopHeightOutside, (-lenseSpacing/2), cTopHeightInside, (-lenseSpacing/2), 0);
    g.bezierVertex((-lenseSpacing/2), cBottomHeightInside, (-lenseSpacing/2)- (lenseWidth/2), cBottomHeightOutside, (-lenseSpacing/2)- (lenseWidth/2), 0);
    g.endShape();


    //Right lense
    g.beginShape();
    g.vertex((lenseSpacing/2) + (lenseWidth/2), 0);
    g.bezierVertex((lenseSpacing/2) + (lenseWidth/2), cTopHeightOutside, (-lenseSpacing/2), cTopHeightInside, (lenseSpacing/2), 0);
    g.bezierVertex((lenseSpacing/2), cBottomHeightInside, (lenseSpacing/2) + (lenseWidth/2), cBottomHeightOutside, (lenseSpacing/2)+ (lenseWidth/2), 0);
    g.endShape();


    //bridge
    g.line(-lenseSpacing/2, 0, lenseSpacing/2, 0);


    //stem left
    g.line((-lenseSpacing/2) - (lenseWidth/2), 0, -headWidth/2, 5);
    // arc(-headWidth/2,2.5,5,5,PI/2,PI*1.5,OPEN);


    //stem right
    g.line((+lenseSpacing/2) + (lenseWidth/2), 0, headWidth/2, 5);
    //arc(headWidth/2,2.5,5,5,PI/2,PI*1.5,OPEN);

    g.popMatrix();
  }


  void drawEyebrows(PGraphics g){
    
  }



/*================================ draw hair================================*/

  void drawHair(PGraphics g) {
    
    
    headControlTop = (headWidth/2.0f);
    
    
    g.fill(255);
    if(hairStyle == PIGTAILS){
    // Base 
    g.beginShape();
    
    //crown left to right
    g.vertex(-headWidth/2.0f, -fringeHeight); // start
    g.bezierVertex(-headWidth/2.0f, -fringeHeight, -headControlTop, -headHeight/2, 0, -headHeight/2);
    g.bezierVertex(headControlTop, -headHeight/2, headWidth/2.0f, -fringeHeight, headWidth/2.0f, -fringeHeight);    
    
    //Fringe
    g.bezierVertex(headWidth/2.0f, -fringeHeight, 0, -fringeHeight+3, 0, -fringeHeight);
    g.bezierVertex(0, -fringeHeight+3, -headWidth/2.0f, -fringeHeight-3, -headWidth/2.0f, -fringeHeight);

    g.endShape();

    
    float x = bezierPoint(-headWidth/2.0f,  -headWidth/2.0f,-headControlTop, 0, 0.5f);
    float y = bezierPoint(-fringeHeight, -fringeHeight, -headHeight/2.0f, -headHeight/2.0f, 0.5f);



    //pigtail left 
    g.beginShape();
    g.vertex(x, y); // start
    g.bezierVertex(x-15, y-30, -25, -10,  -headWidth/2.0f -10, -fringeHeight);
    g.bezierVertex(-headWidth/2.0f -10, -fringeHeight, -25, -20 ,x,y);
    g.endShape();
    
    //pigtail right
    g.beginShape();
    g.vertex(-x, y); // start
    g.bezierVertex(-x+15, y-30, 25, -10,  headWidth/2.0f +10, -fringeHeight);
    g.bezierVertex(headWidth/2.0f +10, -fringeHeight, 25, -20 ,-x,y);
    g.endShape();
    
    
 

    float bobsDia = 7;
    //left
    g.ellipse(x- (bobsDia/3.0f), y - (bobsDia/3.0f),bobsDia,bobsDia);   
   g.ellipse(-x+ (bobsDia/3.0f), y - (bobsDia/3.0f),bobsDia,bobsDia);   

    }
    
   if( hairStyle == BALD){

         g.beginShape();
    
    //crown left to right
    g.vertex(-headWidth/2.0f, -fringeHeight); // start
    g.bezierVertex(-headWidth/2.0f, -fringeHeight, -headControlTop, -headHeight/2, 0, -headHeight/2);
    g.bezierVertex(headControlTop, -headHeight/2, headWidth/2.0f, -fringeHeight, headWidth/2.0f, -fringeHeight);    
    g.endShape();
    //Back 
    } 
    
    
    if (hairStyle == SHORT || hairStyle == LONG){
         g.beginShape();
    
    //crown left to right
    g.vertex(-headWidth/2.0f, -fringeHeight); // start
    g.bezierVertex(-headWidth/2.0f, -fringeHeight, -headControlTop, -headHeight/2, 0, -headHeight/2);
    g.bezierVertex(headControlTop, -headHeight/2, headWidth/2.0f, -fringeHeight, headWidth/2.0f, -fringeHeight);    
    
    //Fringe
    g.bezierVertex(headWidth/2.0f, -fringeHeight, 0, -fringeHeight+3, 0, -fringeHeight);
    g.bezierVertex(0, -fringeHeight+3, -headWidth/2.0f, -fringeHeight-3, -headWidth/2.0f, -fringeHeight);

    g.endShape(); 
      
    }
    
  }
  
  /*================================ draw hair================================*/

  void drawHairBack(PGraphics g) {
     if (hairStyle == LONG){
         g.beginShape();
             g.vertex(-headWidth/2.0f, -fringeHeight); // start
             g.vertex(headWidth/2.0f, -fringeHeight); // start
             g.vertex(headWidth/2.0f, hairLength); // start
             g.vertex(-headWidth/2.0f, hairLength); // start
             g.vertex(-headWidth/2.0f, -fringeHeight); // start
         g.endShape();


     }
  }
  

/*================================ draw mouth ================================*/

  void drawMouth(PGraphics g) {



    float lipBulge = 1; 


    g.beginShape();

    g.vertex(-mouthWidth/2, -smileHeight);
    g.bezierVertex(-mouthWidth/2, smileHeight-mouthOpen, mouthWidth/2, smileHeight-mouthOpen, mouthWidth/2, -smileHeight);
    g.bezierVertex(mouthWidth/2, smileHeight+mouthOpen, -mouthWidth/2, smileHeight+mouthOpen, -mouthWidth/2, -smileHeight);
    g.endShape();
  } 





  void drawBeard(PGraphics g) {
  } 



  void drawFreckeles(PGraphics g) {
  }



/*================================ draw hair================================*/
  void drawNose(PGraphics g) {




    //Nose style 1
    g.beginShape();
    g.vertex(0, 0);
    g.bezierVertex(0, 0, noseWidth, noseHeight, noseWidth, noseHeight);
    g.bezierVertex(noseWidth, noseHeight, 0, noseHeight, 0, noseHeight);
    g.endShape();
  }
  
  
  
  /*================================ draw shoulder================================*/

  void drawShoulders(PGraphics g) {
    
    
    
    g.beginShape();
    g.vertex(shoulderPoint.x, shoulderPoint.y);
    g.bezierVertex(shoulderPoint.x, shoulderPoint.y, bedWidth/3.0f, bedHeight/2.0f-10, bedWidth/3.0f, bedHeight/2.0f);
    g.endShape();
    
    g.beginShape();
    g.vertex(-shoulderPoint.x, shoulderPoint.y);
    g.bezierVertex(-shoulderPoint.x, shoulderPoint.y, -bedWidth/3.0f, bedHeight/2.0f-10, -bedWidth/3.0f, bedHeight/2.0f);
    g.endShape();
    
    
    
  }
  
  
  
  void randomFace(){
    
  hairLength = random(0,25); // in pixels. 
  hairStyle = (int)random(0,4);

   eyesOpen = ((int)random(0,2) == 1) ? true : false;
   eyeWidth = random(1,10);
   eyeHeight = random(1,10);
  
   hasGlasses = ((int)random(0,2) == 1) ? true : false;

  //nose
   noseHeight = random(2,7);
   noseWidth = random(2,7);
   
  //mouth
   mouthWidth = random(2.0,20.0);; 
   smileHeight = random(-2.0,7.0); 
   mouthOpen = random(0.0,7.0);


  headHeight = random(17,50);
  headWidth = random(10,50);
 
 

  
  
  }
}

