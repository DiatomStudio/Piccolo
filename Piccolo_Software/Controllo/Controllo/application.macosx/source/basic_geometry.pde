

void drawCircles(PGraphics g) {

  int o = 150;
  int r = 120;
  int a = 4; 
  int b = 48;

  float x;
  float y; 

  g.beginShape();
  for (int i = r; i > 10; i -= a) {
    for (float j=TWO_PI; j>0; j-= TWO_PI/b) {
      x = i*sin(j)+o;
      y = i*cos(j)+o;
      g.vertex(x, y);
    }
  }
  g.endShape();
  g.point(150, 290); //home
}


void drawBoxes(PGraphics g) {

  int o = 150;
  int r = 120;
  int a = 4; 

  g.beginShape();
  for (int i = r; i > 10; i -= a) {
    g.vertex(o+i, o+i);
    g.vertex(o+i, o-i);
    g.vertex(o-i, o-i);
    g.vertex(o-i, o+i);
    g.vertex(o+i, o+i);
  }
  g.endShape();
  g.point(150, 290); //home
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
  RG.setPolygonizerLength(4);
  
  points1 = grp1.getPoints();
  points2 = grp2.getPoints();
  points3 = grp3.getPoints();  
  points4 = grp4.getPoints();
  
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
  
  
  
  
}


