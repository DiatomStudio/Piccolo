// the ControlFrame class extends PApplet, so we 
// are creating a new processing applet inside a
// new frame with a controlP5 object loaded


import java.awt.Frame;
import java.awt.BorderLayout;
import controlP5.*;

Accordion accordion;

public class ControlFrame extends PApplet {

  int w, h;

  int abc = 100;
  private ControlP5 cp5;

  public void setup() {
    size(w, h);
    frameRate(25);
    cp5 = new ControlP5(this);
    float sliderWidth = 100.0;
    float sliderHeight = 20.0;

    float posX = 10;
    float posY = 10;



    //Main
    Group g1 = cp5.addGroup("main")
      .setBackgroundColor(color(0, 64))
        .setBackgroundHeight(60)
          ;

    cp5.addTextlabel("GENDER")
      .setText("GENDER")
        .setPosition(posX, posY)
          .moveTo(g1)
            ;

    posY+=15;


    cp5.addRadioButton("gender")
      .setPosition(posX, posY)
        .setItemWidth(20)
          .setItemHeight(20)
            .addItem("male", 0)
              .addItem("female", 1)
                .plugTo(face, "gender")
                  .activate(1)
                    .moveTo(g1)
                      ;


    //posY  += 30;


    posY = 10;
    posX += 60;

    cp5.addTextlabel("HEAD")
      .setText("HEAD")
        .setPosition(posX, posY)
          .moveTo(g1)
            ;

    posY+=15;




    cp5.addSlider("headHeight")
      .setPosition(posX, posY)
        .setRange(2, 50)
          .setSize((int)sliderWidth, (int)sliderHeight)
            .setValue(face.headHeight)
              .plugTo(face, "headHeight")
                .setLabelVisible(true)
                  .moveTo(g1)
                    ;
    posY+=30;


    cp5.addSlider("headWidth")
      .setPosition(posX, posY)
        .setSize((int)sliderWidth, (int)sliderHeight)
          .setRange(2, 50)
            .setValue(face.headWidth)
              .plugTo(face, "headWidth")
                .moveTo(g1)
                  ;
    posY+=20;




    //____________________hair
    posX = 10;
    posY =10;

    Group g2 = cp5.addGroup("HAIR")
      .setBackgroundColor(color(0, 64))
        .setBackgroundHeight(60)
          ;



    cp5.addTextlabel("HAIR")
      .setText("HAIR")
        .setPosition(posX, posY)
          .moveTo(g2)
            ;

    posY+=20;


    DropdownList d1 = cp5.addDropdownList("hairStyle")
      .setPosition(posX, posY)
          .moveTo(g2)
          ;

        d1.addItem("BALD", Face.BALD);
        d1.addItem("PIGTAILS", Face.PIGTAILS);
        d1.addItem("SHORT", Face.SHORT);
        d1.addItem("LONG", Face.LONG);

    //____________________mouth

    posX = 10;
    posY =10;

    Group g3 = cp5.addGroup("MOUTH")
      .setBackgroundColor(color(0, 64))
      .setBackgroundHeight(60)
      ;

    cp5.addTextlabel("MOUTH")
      .setText("MOUTH")
      .setPosition(posX, posY)
      .moveTo(g3)
      ;


    cp5.addSlider("smileHeight")
      .setPosition(posX, posY)
      .setRange(-5, 5)
      .setValue(face.smileHeight)
      .plugTo(face, "smileHeight")
      .setLabelVisible(true)
      .moveTo(g3)
      ;
    posY+=10;

    cp5.addSlider("mouthWidth")
      .setPosition(posX, posY)
      .setRange(-25, 25)
      .setValue(face.mouthWidth)
      .plugTo(face, "mouthWidth")
      .setLabelVisible(true)
      .moveTo(g3)
      ;
    posY+=10;


    cp5.addSlider("mouthOpen")
      .setPosition(posX, posY)
      .setRange(0, 5)
      .setValue(face.mouthOpen)
      .plugTo(face, "mouthOpen")
      .setLabelVisible(true)
      .moveTo(g3)
      ;
    posY+=10;


    cp5.addButton("captureRecognizeFace")
      .setPosition(width-75, height-19)
      .plugTo(captureFrame)
      .setSize(35, 19);




    //____________________eyes

    posX = 10;
    posY =10;

    Group g4 = cp5.addGroup("EYES")
      .setBackgroundColor(color(0, 64))
      .setBackgroundHeight(60)
      ;

    cp5.addSlider("eyeHeight")
      .setPosition(posX, posY)
      .setRange(1, 15)
      .setSize((int)sliderWidth, (int)sliderHeight)
      .setValue(face.headHeight)
      .plugTo(face, "eyeHeight")
      .setLabelVisible(true)
      .moveTo(g4)
      ;

    posY +=25;

    cp5.addSlider("eyeWidth")
      .setPosition(posX, posY)
      .setRange(1, 15)
      .setSize((int)sliderWidth, (int)sliderHeight)
      .setValue(face.headHeight)
      .plugTo(face, "eyeWidth")
      .setLabelVisible(true)
      .moveTo(g4)
      ;                

    posY +=25;        

    cp5.addToggle("eyesOpen")
      .setPosition(posX, posY)
      .plugTo(face, "eyesOpen")
      .setValue(face.eyesOpen)
      .moveTo(g4)
      ;

    posX +=55;  

    cp5.addToggle("hasGlasses")
      .setPosition(posX, posY)
      .plugTo(face, "hasGlasses")
      .setValue(face.hasGlasses)
      .moveTo(g4)
      ;    

    //____________________nose

    posX = 10;
    posY =10;

    Group g5 = cp5.addGroup("NOSE")
      .setBackgroundColor(color(0, 64))
      .setBackgroundHeight(60)
      ;

    cp5.addSlider("noseHeight")
      .setPosition(posX, posY)
      .setRange(1, 15)
      .setSize((int)sliderWidth, (int)sliderHeight)
      .setValue(face.noseHeight)
      .plugTo(face, "noseHeight")
      .setLabelVisible(true)
      .moveTo(g5)
      ;

    posY +=25;


    cp5.addSlider("noseWidth")
      .setPosition(posX, posY)
      .setRange(1, 15)
      .setSize((int)sliderWidth, (int)sliderHeight)
      .setValue(face.noseWidth)
      .plugTo(face, "noseWidth")
      .setLabelVisible(true)
      .moveTo(g5)
      ;



    // create a new accordion
    // add g1, g2, and g3 to the accordion.
    accordion = cp5.addAccordion("acc")
      .setPosition(10, 10)
      .setWidth(300)
      .addItem(g1)
      .addItem(g2)
      .addItem(g3)
      .addItem(g4)
      .addItem(g5)
      ;

    accordion.open(0, 1, 2, 3, 4);

    // use Accordion.MULTI to allow multiple group 
    // to be open at a time.
    accordion.setCollapseMode(Accordion.MULTI);
    
    
    
    
    
      
    cp5.addButton("startSend")
    .setPosition(width-110,height-60)
    .plugTo(parent,"startSend")
     .setSize(100,30);
     
         cp5.addButton("captureRecognizeFace")
    .setPosition(10,height-60)
        .plugTo(parent,"captureRecognizeFace")
     .setSize(100,30);
     
     
     
              cp5.addButton("randomFace")
    .setPosition(120,height-60)
        .plugTo(face,"randomFace")
     .setSize(100,30);
     
     
  }

  public void draw() {
    background(abc);
  }

  private ControlFrame() {
  }

  public ControlFrame(Object theParent, int theWidth, int theHeight) {
    parent = theParent;
    w = theWidth;
    h = theHeight;
  }


  public ControlP5 control() {
    return cp5;
  }



  Object parent;
  
  
  void controlEvent(ControlEvent theEvent) {
println(theEvent.getName());
 
  if (theEvent.getName() == "hairStyle") {
    
    println(theEvent);
  face.hairStyle = (int)theEvent.getValue();
  } 
  
}

}



  

