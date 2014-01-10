class PlotiWriter extends PGraphics {

  PVector beginShapePos = null;
  boolean closeShape = false;

  boolean penUp = true;

  List instructionBuffer = new ArrayList();

  //should we flattern curves?
  boolean flattern = true;
  float flatRes = 1.0f;

  //Previous Vector
  PVector pVertex = new PVector(0, 0, 0);

  //simulation stuff
  int simStep = 0;
  PVector simPrev = null;

  public void vertex(float x, float y) {

    if (closeShape) {
      beginShapePos = new PVector(x, y, penDownHeight);
      closeShape = false;
    }

    if (penUp) {
      stepTo(pVertex.x, pVertex.y, penLiftHeight);
      stepTo(x, y, penLiftHeight);
      stepTo(x, y, penDownHeight);
      penUp = false;
    }
    else {
      stepTo(x, y, penDownHeight);
    }

    pVertex.x = x;
    pVertex.y = y;
    super.vertex(x, y);
  }


  public void vertex(float x, float y, float z) {

    if (closeShape) {
      beginShapePos = new PVector(x, y, 0);
      closeShape = false;
    }

    if (pVertex.dist(new PVector(x, y, z)) >= flatRes) {
      stepTo(x, y, z);

      pVertex.x = x;
      pVertex.y = y;
      pVertex.z = y;
    }
    super.vertex(x, y);
  }

  /*
 public void bezierVertex(float x1, float y1,
   float x2, float y2,
   float x3, float y3) {
   
   }
   */

  public void beginShape(int kind) {

    penUp = true;
    super.beginShape(kind);

    if (kind == 16 ) {
      closeShape = true;
    }
  }

  public void beginShape() {
    penUp = true;
    super.beginShape();
  }

  public void endShape() {
    println("END SHAPE");
    if (beginShapePos != null) {
      vertex(beginShapePos.x, beginShapePos.y);
      beginShapePos = null;
    }
    penUp = true;
    stepTo(pVertex.x, pVertex.y, penLiftHeight);
    super.endShape();
  }




  void stepTo(float x, float y) {

    // float xMapped = map(x, 0, bedWidth, servoMinRotationX, servoMaxRotationX);
    //float yMapped = map(y, 0, bedHeight, servoMinRotationY, servoMaxRotationY);

    /*
    if (xMapped < servoMinRotationX || xMapped > servoMaxRotationX || yMapped < servoMinRotationY || yMapped > servoMaxRotationY)
     return;
     */

    if (flipX)
      x = map(x, 0, bedWidth, bedWidth, 0);

    if (flipY)
      y = map(y, 0, bedHeight, bedHeight, 0);

    codeStack.add(new PVector(x, y, 0));

    numLines++;
    /*
    codeStack.add("delay("+getStepDelay()+");");
     codeStack.add("x.write("+xMapped+");");
     codeStack.add("y.write("+yMapped+");");
     */
  }

  void stepTo(float x, float y, float z) {

    // float xMapped = map(x, 0, bedWidth, servoMinRotationX, servoMaxRotationX);
    //float yMapped = map(y, 0, bedHeight, servoMinRotationY, servoMaxRotationY);
    //float zMapped = map(z, 0, bedDepth, servoMinRotationZ, servoMaxRotationZ);

    /*
    if (xMapped < servoMinRotationX || xMapped > servoMaxRotationX 
     || yMapped < servoMinRotationY || yMapped > servoMaxRotationY
     || zMapped < servoMinRotationZ || zMapped > servoMaxRotationZ
     )
     return;
     */
    if (flipX)
      x = map(x, 0, bedWidth, bedWidth, 0);

    if (flipY)
      y = map(y, 0, bedHeight, bedHeight, 0);

    if (flipZ)
      z = map(z, 0, bedDepth, bedDepth, 0);
      
      x = constrain(x,0,bedWidth);
      y = constrain(y,0,bedHeight);
      z = constrain(z,0,bedDepth);


    codeStack.add(new PVector(x, y, z));
    numLines++;
  }

  void clear() {
    codeStack.clear();
    numLines=0;
    simStep = 0;
    simPrev = null;
  }

  void setStepRes(float res) {
    flatRes = res;
  }

  void setSize(float width, float height, float depth) {
    bedWidth = width;
    bedHeight = height;
    bedDepth = depth;
  }

  void debugDraw(PGraphics g) {

    PVector prev = null;

    for (int i = 0; i < codeStack.size(); i ++) {
      PVector p = (PVector)codeStack.get(i);

      if (prev != null) {
        if (p.z > penLiftHeight )//!= 0 || prev.z != 0)
          g.stroke(50);
        else
          g.stroke(220);

        g.line(p.x, p.y, prev.x, prev.y);
      }

      prev = p;
    }
  }


  //simulate the whole
  void simulate(PGraphics g) {

    PVector prev = null;

    if ( simStep < codeStack.size()) {
      PVector p = (PVector)codeStack.get(simStep);

      if (simPrev != null) {
        if (p.z != 0 || simPrev.z != 0)
          g.stroke(240, 240, 240, 250);
        else
          g.stroke(0);

        g.line(p.x, p.y, simPrev.x, simPrev.y);
      }
      simPrev = p;

      simStep++;
    }
  }
}

