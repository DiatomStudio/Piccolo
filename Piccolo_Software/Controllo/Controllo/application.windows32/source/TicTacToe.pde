
void drawGrid(PGraphics g) {
  
  int gridWidth = 210;
  int o = 45;
  int w = gridWidth;
  
  g.beginShape();
  g.vertex(o,o);
  g.vertex(o,o+w);  
  g.vertex(o+w,o+w);  
  g.vertex(o+w,o);
  g.vertex(o,o);
  g.endShape();
  
  g.beginShape();
  g.vertex(o+w/3,o);
  g.vertex(o+w/3,o+w);  
  g.endShape();

  g.beginShape();
  g.vertex(o+2*w/3,o);
  g.vertex(o+2*w/3,o+w);  
  g.endShape();  
  
  g.beginShape();
  g.vertex(o,o+w/3);
  g.vertex(o+w,o+w/3);  
  g.endShape();

  g.beginShape();
  g.vertex(o,o+2*w/3);
  g.vertex(o+w,o+2*w/3);  
  g.endShape();    
  
  g.point(1, 150); //home
  
}

void drawX(int x, int y) {
  
}

void drawO(int x, int y) {
  
}
