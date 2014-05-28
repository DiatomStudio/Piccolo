
void drawInterface() {

  drawPlotsiOutput = controlP5.addCheckBox("draw output", 100, 100);  
  controlP5.addButton("load_SVG", 0, 10, 10, 80, 19);
  controlP5.addSlider("lightLevel", 0, 1.0f, 0.5f, 10, 33, 80, 10);
  controlP5.addButton("generate_tree", 0, 10, 50, 80, 19);
  controlP5.addButton("generate_mustache", 0, 10, 70, 80, 19);
  //  controlP5.addButton("brush_Mustache", 0, 10, 120, 80, 19);
  controlP5.addButton("bezier", 0, 10, 90, 80, 19);  
  controlP5.addButton("logo", 0, 10, 110, 80, 19);
  //  controlP5.addButton("maze", 0, 10, 180, 80, 19);
  controlP5.addButton("circles", 0, 10, 130, 80, 19);
  controlP5.addButton("boxes", 0, 10, 150, 80, 19);
  controlP5.addButton("diagonals", 0, 10, 170, 80, 19);
  controlP5.addButton("word", 0, 10, 190, 80, 19);

  pressureKnob = controlP5.addKnob("pressure")
    .setRange(0, bedDepth)
      .setValue(piccolo.getPenDownHeight())
        .setPosition(10, 220)
          .setRadius(15)
            .setViewStyle(Knob.ARC)
              .setDragDirection(Knob.VERTICAL)
                ;
  controlP5.addButton("up_", 0, 50, 223, 30, 10);
  controlP5.addButton("down_", 0, 50, 237, 30, 10);

  //  controlP5.addButton("w",0, 30, 220, 19, 19);
  //  controlP5.addButton("s",0, 30, 240, 19, 19);
  //  controlP5.addButton("a",0, 10, 230, 19, 19);
  //  controlP5.addButton("d",0, 50, 230, 19, 19);
  //  controlP5.addButton("Up",0, 70, 220, 19, 19);
  //  controlP5.addButton("Down",0, 70, 240, 19, 19);

  controlP5.addButton("home", 0, 10, height-80, 80, 19);
  controlP5.addButton("start", 0, 10, height-60, 80, 19);
  controlP5.addButton("stop", 0, 10, height-40, 80, 19);

}

void debugDraw(String inString) {
  plotsiOutputCanvas.beginDraw();
  float[] posIn = float(split(inString, ","));

  if (posIn.length >=4) {

    plotsiOutputCanvas.noFill();

    if (posIn[3] == 0)
      plotsiOutputCanvas.stroke(255, 0, 0);
    else
      plotsiOutputCanvas.stroke(240, 240, 240, 254);

    if (piccolo.flipX)
      posIn[1] = map(posIn[1], 0, bedWidth, bedWidth, 0);

    if (piccolo.flipY)
      posIn[2] = map(posIn[2], 0, bedHeight, bedHeight, 0);

    //draw directly to the screen
    plotsiOutputCanvas.line(xPos, yPos, posIn[1], posIn[2]);
    xPos = posIn[1];
    yPos = posIn[2];
    zPos = posIn[3];
  } 
  plotsiOutputCanvas.endDraw();
}
