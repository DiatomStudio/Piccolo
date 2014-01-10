# Controllo
----------------
Controllo is a standalone program that runs on your, pc , mac or linux machine. That lets you send your own SVG vector artworks or a series of pre-defined generative artworks to your Piccolo for drawing.


Your Piccolo will need to have the usbTether firmware installed and be attached to your computer in order to communicate with Controllo. The usbTether program can be found in [PiccoloLib](https://github.com/DiatomStudio/Piccolo/tree/master/PiccoloLib) under examples.
 
![](http://farm4.staticflickr.com/3682/11873676554_9e2fb8b5b3_o_d.png)
####installing
- Download Controllo_current.zip
- Unzip and run the appropriate application for your system.

####how to use
- Load your own svg or select a generative pattern using the buttons on the left. 
- Lower your piccolo's drawing impliment to the paper using either the thumbwhere on the side of the Piccolo or the UP_ DOWN_ buttons in Controllo.
- Press the STARRT button or button_1 on your Piccolo to start drawing. 


####hacking
Controllo is written using the Processing programming language. If you would like to make your own changes you will need.

- Processing: <http://processing.org/>

the following processing libraries

- controlP5: <http://www.sojamo.de/libraries/controlP5/>
- Geomerative: <http://www.ricardmarxer.com/geomerative/>

####notes
To correctly draw imported SVG drawings, SVG's should be exported with all lines expanded and no compound shapes. 

The svg size should be less than 300 x 300 px. 

Piccolo should be connected to your computer via usb before starting Controllo. 

Controllo and it's source code is a work in progress, most functions work however there will some bugs.  