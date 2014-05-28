// the ControlFrame class extends PApplet, so we 
// are creating a new processing applet inside a
// new frame with a controlP5 object loaded

import java.awt.Frame;
import java.awt.BorderLayout;
import controlP5.*;
import com.facepp.*;
import processing.video.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import java.io.BufferedOutputStream;
import javax.imageio.ImageIO;
Capture cam;



String FACEPP_API_KEY = "8b87b939e3f24fa481c35b2d4893da66";
String FACEPP_API_SECRET = "ga4KPlMIvPhsOCcAcZNrAOm8rr1UrDU1";



public class CaptureFrame extends PApplet {
  int w, h;

 
 
  public void setup(){
        size(w, h);
    frameRate(25);
    
          startCamera();

  }
  
  public void draw() {
      background(255);
      
      
            if (cam.available()) { 
    // Reads the new frame
    cam.read(); 
  } 
  image(cam,0,0);
  

  }
  
  private CaptureFrame() {
  }

  public CaptureFrame(Object theParent, int theWidth, int theHeight) {
    parent = theParent;
    w = theWidth;
    h = theHeight;
    

  }


  public ControlP5 control() {
    return cp5;
  }
  
  
void startCamera(){
  
    String[] cameras = Capture.list();
  
  if (cameras.length == 0) {
    println("There are no cameras available for capture.");
    exit();
  } else {
    println("Available cameras:");
    for (int i = 0; i < cameras.length; i++) {
      println(cameras[i]);
    }
    
    // The camera can be initialized directly using an 
    // element from the array returned by list():
    cam = new Capture(this, cameras[7]);
    cam.start();     
  }      
  
}

  void captureRecognizeFace(){
  
  
  PImage img = cam;
  
  HttpRequests httpRequests = new HttpRequests(FACEPP_API_KEY, FACEPP_API_SECRET,false,true);


/*PostParameters postParameters =
      new PostParameters()
          .setUrl("http://www.uni-regensburg.de/Fakultaeten/phil_Fak_II/Psychologie/Psy_II/beautycheck/english/durchschnittsgesichter/m(01-32)_gr.jpg")
          .setAttribute("all");
      */
      
      
      PostParameters postParameters =
      new PostParameters()
          .setImg(pImgToByteArray(img))
          .setAttribute("glass,pose,gender,age,race,smiling");
      
          
          try {
            
            JSONObject result = httpRequests.detectionDetect(postParameters);
       println(result);
     
    } catch(FaceppParseException e) {
            e.printStackTrace();
        }
}



byte[]  pImgToByteArray(PImage img) {
      BufferedImage bimg = new BufferedImage( img.width,img.height, 
       BufferedImage.TYPE_INT_RGB );    
      img.loadPixels();
      bimg.setRGB( 0, 0, img.width, img.height, img.pixels, 0, img.width);
      ByteArrayOutputStream baStream    = new ByteArrayOutputStream();
      BufferedOutputStream bos      = new BufferedOutputStream(baStream);
      try {
           ImageIO.write(bimg, "jpg", bos);
      } 
      catch (IOException e) {
            e.printStackTrace();
      }
      byte[] packet = baStream.toByteArray();
      return packet;
}





  Object parent;

  
}

