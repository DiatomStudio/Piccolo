##PiccoloLib v0.81  
*Arduino library for controlling Piccolo, the tiny CNC-bot.*  

Piccolo.cc  
Created by Diatom Studio, October 10, 2013.  
Released into the public domain.  

##Changes:

###0.82  
- Fixing bugs in examples.
- Removed incompatible examples. 

###0.81  
-  Minor restructuring of comments. 
-  Added bezier(y,z) function
-  Commented out unused Piccolo Axis functions.s

###0.8  
-  Major restructuring of code. 
-  Added Axis objects to store and calculate Axis positions. 

###0.21  
-  Reverted BeginShape and EndShape back to BeginDraw and EndDraw to bring them in line with processing.org
-  Changed received coordinates from human readable chars to signed 32 bit ints backed into 4 bytes. This make our ranges larger, supports negative numbers and we send less data. 

###0.2  
-  Start of change tracking.

