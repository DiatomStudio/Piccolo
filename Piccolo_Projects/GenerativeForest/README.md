# Generative Forest
A Piccolo Project 
----------------

Piccolo is a pocket-sized stand-alone CNC platform.  For more info please see [www.piccolo.cc](http://www.piccolo.cc)


####About: 
Piccolo draws a generative forest across a table top.

Piccolo draws generative plants and trees using a light sensor as it's input. When Piccolo is placed under a bight light, taller and more bushy tree is generated and drawn while lower light levels or shadows result in small shrubs and grasses. 


####You Will Need:

- 1 x Piccolo
- 1 x LDR (Light dependent resistor)
- 1 x Resistor matching maximun LDR value e.g.e 10K 

- PiccoloLib (tested with v0.8)
- GenerativeForest Arduino sketch

####Assembley: 

Wire your LDR with one leg connected to the A2 input on the side of your piccolo and the other connected to GND, now take your resistor and connect one end to VCC and the other also to A2.

This has created a voltage divider so if more light is hits the LDR it's resistance will lower allowing current to flow into GND instead of A2 a darker environment will cause the LDR's resistance to raise and more current to flow into A2 resulting in  a higher reading. 

The LDR can be placed at other points on Piccolo for example the Z-Axis by soldering wires to it's legs. 

Make sure PicccoloLib is installed correctly and upload the GenerativeForest Sketch to piccolo. 

####Usage: 

- Add a drawing implement to Piccolo and set the drawing height using the thumb wheel. 
- Place Piccolo under the darkest point of light on the surface and hold down button two for two seconds (the button next to the usb plug) this calibrates you light sensor.
- Move piccolo onto a surface and press button one to start drawing a generative tree. 
