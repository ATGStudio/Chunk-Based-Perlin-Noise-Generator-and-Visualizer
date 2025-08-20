# Chunk-Based Perlin Noise Generator and Visualizer

https://github.com/user-attachments/assets/fb2f80a1-8606-4443-b386-5f51e103ee2e


# What it is
Perlin Noise is a gradient based noise, created by Ken Perlin in 1983.

The most noticeable application of Perlin Noise is by the game Minecraft created by Markus (Notch) Persson and published by Mojang.

Perlin Noise in the instance of Minecraft was used for the terrain and landscape generation. Of course the Minecraft formula has developed way further than just simple graident noise, but the core logic of it all remained.
Currently, Minecraft uses a complex system in addition with Perlin Noise in order to create the Overworld landscape, meanwhile using 3D Perlin Noise to generate the beautiful deep caves system. (Not considering Perlin Worm).

This of course doesn't limit us to just landscapes and terrains. Perlin Noise is capable of generating many more different assets. Such as: flowing water/lava, clouds, textures, and many more!

*So what does this Java code really do?*

As described by the title it generates Perlin Noise, but it takes it a step further by breaking it down into chunks.

In Minecraft, a chunk is a part of the landscape that is generated within a 16 x 16 area, this way Minecraft is able to generate only the necessary chunks in which the player is or has been saving tons of compution time & rescources. If not for this modular optomization, ***ONE*** Minecraft world would require **hundreds of petabytes** of storage on your device, thats still not considering the hours of raw compution your device has to go through in order to generate such thing. 
