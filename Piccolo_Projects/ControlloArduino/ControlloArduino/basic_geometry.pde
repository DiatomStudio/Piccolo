//TODO: add paramaters to all commands 



void drawBezier(PGraphics g){

PVector p1 = new PVector(-25,-25,0);
PVector p2 = new PVector(25,25,0);
PVector c1 = new PVector(25,-25,0);
PVector c2 = new PVector(25,-25,0);


for(int i = 25; i > -25 ; i-=2){
  c1.x = i; c1.y = -i;
  c2.x = i; c2.y = -i;
  g.bezier(p1.x, p1.y,c1.x,c1.y,c2.x,c2.y, p2.x, p2.y);
}



}
void drawCircles(PGraphics g) {

  int o = 0;
  int r = 25; //radius
  int a = 1; 
  int b = 100;
  int startRad = 5;

  float x;
  float y; 

  g.beginShape();
  for (int i = startRad; i <= r; i += a) {
    for (float j=TWO_PI; j>0; j-= TWO_PI/(i*10)) {
      x = (i-(a*(j/TWO_PI)))*sin(j)+o;
      y = (i-(a*(j/TWO_PI)))*cos(j)+o;
      g.vertex(x, y);
    }
  }
  g.endShape();
}



void drawSpiral(PGraphics g){
  
  
}

void drawBoxes(PGraphics g) {

  int w = 25; //half box width
  int a = 1; //step size

  g.beginShape();
  for (int i = w; i > 1; i -= a) {
    g.vertex(+i, +i);
    g.vertex(+i, -i);
    g.vertex(-i, -i);
    g.vertex(-i, +i);
    g.vertex(+i, +i);
  }
  g.endShape();
}



void drawDiagonals(PGraphics g) 
{ 

  int o = 150;
  int r = 120;
  int a = 5; 
    
  g.beginShape();
  
    int i = -r;
    g.vertex(o+i, o-r);
    
    while(i<(r-a%r)) {
    i += a;
    g.vertex(o+i, o-r);
    g.vertex(o-r, o+i);  
    i += a;
    g.vertex(o-r, o+i);
    g.vertex(o+i, o-r); 
    }
    
    while(i>-(r-a%r)) {
    i -= a;
    g.vertex(o+r, o-i);
    g.vertex(o-i, o+r);  
    i -= a;
    g.vertex(o-i, o+r);
    g.vertex(o+r, o-i); 
    }
    
    g.vertex(o-i, o+r);
    
  g.endShape();
  g.point(150, 290); //home

}

void drawWord(PGraphics g) {
  /*
  int o = 150;
  int fontHeight = 100;
  String font = "swsimp.ttf";
  
  
  RShape grp1;
  RShape grp2;
  RShape grp3;
  RShape grp4;
  RPoint[] points1;
  RPoint[] points2;
  RPoint[] points3;
  RPoint[] points4;
  
  grp1 = RG.getText("P", font,fontHeight, CENTER);
  grp2 = RG.getText("L", font,fontHeight, CENTER);
  grp3 = RG.getText("A", font,fontHeight, CENTER);
  grp4 = RG.getText("Y", font,fontHeight, CENTER);

  RG.setPolygonizer(RG.ADAPTATIVE);
  //grp.draw();

  // Get the points on the curve's shape
  //RG.setPolygonizer(RG.UNIFORMSTEP);
  //RG.setPolygonizerStep(15);

  RG.setPolygonizer(RG.UNIFORMLENGTH);
  RG.setPolygonizerLength(0.3f);
  
  points1 = grp1.getPoints();
  points2 = grp2.getPoints();
  points3 = grp3.getPoints();  
  points4 = grp4.getPoints();
  
  g.pushMatrix();
    g.translate(-10,-10);
  g.scale(0.1);
  g.beginShape();
  for(int i=1; i<points1.length; i++) {
    g.vertex(points1[i].x+o-fontHeight,points1[i].y+o);
  }
  g.endShape();
  
  g.beginShape();
  for(int i=1; i<points2.length; i++) {
    g.vertex(points2[i].x+o-3*fontHeight/8,points2[i].y+o);
  }
  g.endShape();
  
  g.beginShape();
  for(int i=1; i<points3.length; i++) {
    g.vertex(points3[i].x+o+3*fontHeight/8,points3[i].y+o);
  }
  g.endShape();
  
  g.beginShape();
  for(int i=1; i<points4.length; i++) {
    g.vertex(points4[i].x+o+fontHeight,points4[i].y+o);
  }
  g.endShape();
  g.popMatrix();
  
  
  */
  
}


