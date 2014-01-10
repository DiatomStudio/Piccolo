
import processing.serial.*;
Serial myPort;

boolean firstContact = false;
boolean stepDelaySet = false;
boolean pressureSet = false;

int inByte;
String sendString;  //xxxxxyyyyyzzzzz;
int sendStringIndex = 0;

String code[];


int zDraw = 0;
int zLift = 10;

int lineCount = 0;
int numLines;


/*
void serialEvent(Serial myPort) {
  String inString = myPort.readString();
  println(inString);
  println(">"+inString +"<");
}
*/

void establishContact() {
  //S = get ready to send 
  println("establishContact");
  println(codeStack.size() + " lines to send");
  myPort.clear();
  myPort.write('S');
  lineCount = 0;
}



