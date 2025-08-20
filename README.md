# Chunk-Based Perlin Noise Generator and Visualizer

https://github.com/user-attachments/assets/fb2f80a1-8606-4443-b386-5f51e103ee2e

# *Project Files*
*ChunkGeneration.java* → Contains the main and performs as the main file.  
*GeneratePerlin.java* → Performs the basic Perlin Noise functions.  
*Storage.java* → Performs as storage for constants.  

# *Controls*
*`↑` or `W` → move North*

*`↓` or `S` → move South*

*`→` or `D` → move East*

*`←` or `A` → move West*

*`1` → - Octave*

*`2` → + Octave*

*`3` → - Lacunarity*

*`4` → + Lacunarity*

*`5` → - Persistence*

*`6` → + Persistence*

*`7` → - Shades*

*`8` → + Shades*

*`9` → - Render Distance*

*`0` → + Render Distance*

*`-` → - Zoom*

*`=` → + Zoom*

*`Q` → - Scale*

*`E` → + Scale*

*`O` → Print current data in console*
- - -
# What it is
Perlin Noise is a gradient-based noise created by Ken Perlin in 1983.

The most noticeable application of Perlin Noise is by the game Minecraft, created by Markus (Notch) Persson and published by Mojang.

Perlin Noise, in the instance of Minecraft, was used for the terrain and landscape generation. Of course, the Minecraft formula has evolved far beyond simple gradient noise, but the core logic remains.
Currently, Minecraft uses a complex system in addition to Perlin Noise in order to create the Overworld landscape, meanwhile using 3D Perlin Noise to generate the beautiful, deep caves system. (Not considering Perlin Worm).

This, of course, doesn't limit us to just landscapes and terrains. Perlin Noise is capable of generating many more different assets. Such as: flowing water/lava, clouds, textures, and many more!

*So what does this Java code really do?*

As described by the title, it generates Perlin Noise, but it takes it a step further by breaking it down into chunks.

In Minecraft, a chunk is a part of the landscape that is generated within a 16 x 16 area. This way, Minecraft can generate only the necessary chunks in which the player is or has been, saving tons of computation time & resources. If not for this modular optimization, ***ONE*** Minecraft world would require **hundreds of petabytes** of storage on your device, that's still not considering the hours of raw computation your device has to go through in order to generate such a thing.

# How it works

## *Chunks*
*Let's start with chunks...* The chunks are ultimately simple cells that contain a grid of decimal point numbers. (The decimal point number is later converted to a shade of gray or height). 
Each chunk - *or cell* - has an address, also known as coordinates on the **xy** plane. For example, a chunk located on **X: 100** and **Y: 15** would have an address of ***"c.x100.y15"*** (this is a custom thing within this code, it is different from official Minecraft).

The address - *coordinates* - is later used to cache the chunk as an existing chunk. When the player moves and the chunk is outside of *`Render Distance`*, it is stored within *`CHUNK_CACHE`* HashMap and then despawned from the world. This allows the world to run smoothly without getting overwhelmed with existing chunk data.

*So what is really stored in the cache?*
In ***`Storage.java`***, you can see that there is a local class named *`"ChunkData"`*. The local class has a *String* variable ***key***, which is the address (ex. ***"c.x100.y15"***), and a *double* 2D-array ***noiseVal*** variable, which saves all those decimal point numbers mentioned above.

*Now let's explore how the chunks are generated...*  
In ***`ChunkGeneration.java`***, there is a method named *`generateChunk`*. Seems straightforward, and honestly, it is! It initializes a key value based on chunk coordinates passed into the method, then initializes an fbmNoiseMap *double* 2D-array with preset chunk sizes. (FBM is explained here later). As evident from the code, we have a nested for loop that goes through every cell of the chunk's array. During the for loop, we calculate the global positioning of the cell, and use the global coordinates to calculate the FMB value. After the for loops, all the data is saved into *`ChunkData`* and then stored into the Cache as an existing chunk. 

*Okay... but how is the chunk data retrieved?*
Glad you asked! In the same ***`ChunkGeneration.java`***, there is another method named *`getNoiseMap`* and if you haven't guessed already, it accesses the *`CHUNK_CACHE`*, checks if the address key exists, if it does, it simply returns the fmbNoiseMap values we created in the previous method. But if it doesn't, it calls on the previous method with the request to generate the chunk before returning the values.

Now that we've got those out of the way, let's finish up the chunks portion with Render Distance. Once again, another important thing in Minecraft. Render Distance is how far chunks get generated around the player. Let's suppose the player has Render Distance set to 4 chunks far. Now there are going to be 4 chunks generated in each direction from the player. In total, there are 81 chunks generated around the player. The formula to count the number of chunks is: *(2x + 1)<sup>2</sup>*.  
*How is it done in the code?*  
Well, probably the most noticeable method that pops up when you look at ***`ChunkGeneration.java`***, *`renderChunks`*. It can look overwhelming, but most of it is mainly for the Zoom in/out feature. In truth, the method is quite simple. It goes into a nested for loop, noting the player's chunk coordinates as the center. While it looks through the neighboring chunks to the player based on *`Render Distance`*, it calls *`getNoiseMap`* to see whether those chunks already exist or not in the cache. Then it simply draws the chunks for the player. 

Phew, that was a lot, but we got through the chunk-based aspect of the code. Next up, we'll be exploring the generation of Perlin Noise itself, FMBs, and everything else that goes on.
