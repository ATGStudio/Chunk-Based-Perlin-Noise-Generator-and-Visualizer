import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ChunkGeneration extends JPanel implements KeyListener {
    public ChunkGeneration() {
        setFocusable(true);
        addKeyListener(this);
    }
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        renderChunks(g, Storage.shadeCount);
    }
    
    private void renderChunks(Graphics g, int shadeCount) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Storage.SCREEN_WIDTH, Storage.SCREEN_HEIGHT);
        
        // Calculate zoomed chunk size
        int zoomedChunkSize = (int)(Storage.CHUNK_SIZE * Storage.zoomLevel);
        
        for (int dx = -Storage.RENDER_DISTANCE; dx <= Storage.RENDER_DISTANCE; dx++) {
            for (int dy = -Storage.RENDER_DISTANCE; dy <= Storage.RENDER_DISTANCE; dy++) {
                int chunkX = (Storage.playerX) + dx;
                int chunkY = (Storage.playerY) + dy;
                double[][] noiseMap = getNoiseMap(chunkX, chunkY);
                
                // Calculate screen position with zoom
                int screenCenterX = Storage.SCREEN_WIDTH / 2;
                int screenCenterY = Storage.SCREEN_HEIGHT / 2;
                int screenX = screenCenterX + (int)(dx * zoomedChunkSize);
                int screenY = screenCenterY + (int)(dy * zoomedChunkSize);
                
                for (int i = 0; i < Storage.CHUNK_SIZE; i++) {
                    for (int j = 0; j < Storage.CHUNK_SIZE; j++) {
                        // Enhance contrast first
                        double enhancedValue = enhanceContrast(noiseMap[i][j]);
                        
                        // Then quantize to the specified number of shades
                        double quantizedValue = quantizeShades(enhancedValue, shadeCount);
                        
                        int shade = (int)(quantizedValue * 255);
                        g.setColor(new Color(shade, shade, shade));
                        
                        // Calculate pixel position with zoom
                        int x = screenX + (int)(j * Storage.zoomLevel);
                        int y = screenY + (int)(i * Storage.zoomLevel);
                        
                        // Draw a pixel at the appropriate size based on zoom
                        int pixelSize = Math.max(1, (int)Storage.zoomLevel);
                        g.fillRect(x, y, pixelSize, pixelSize);
                    }
                }
            }
        }
    }
    
    private static double enhanceContrast(double value) {
        // Apply contrast curve - this makes middle values more distinct
        return 0.5 + Math.sin((value - 0.5) * Math.PI) * 0.5;
    }
    
    private static ChunkData generateChunk(int chunkX, int chunkY) {
        String key = "c.x" + chunkX + ".y" + chunkY;
        
        // Initialize noise map for the whole chunk
        double[][] fbmNoiseMap = new double[Storage.CHUNK_SIZE][Storage.CHUNK_SIZE];
        
        // Generate fBm noise for each point in the chunk
        for (int y = 0; y < Storage.CHUNK_SIZE; y++) {
            for (int x = 0; x < Storage.CHUNK_SIZE; x++) {
                // Calculate the global position of this point
                double globalX = chunkX * Storage.CHUNK_SIZE + x;
                double globalY = chunkY * Storage.CHUNK_SIZE + y;
                
                // Apply fBm (summing multiple octaves of noise)
                fbmNoiseMap[y][x] = calculateFBM(globalX, globalY);
            }
        }
        
        ChunkData chunkData = new ChunkData(key, fbmNoiseMap);
        Storage.CHUNK_CACHE.put(key, chunkData);
        
        return chunkData;
    }
    
    private static double calculateFBM(double x, double y) {
        double total = 0;
        double frequency = 1.0;
        double amplitude = 1.0;
        double maxValue = 0;  // Used for normalization
        
        // Scale input coordinates to make features more visible
        // This makes the base frequency lower, creating larger features
        x *= 0.05;
        y *= 0.05;
        
        // Sum octaves
        for (int i = 0; i < Storage.OCTAVE_COUNT; i++) {
            // Scale coordinates by frequency for this octave
            double scaledX = x * frequency * Storage.scale;
            double scaledY = y * frequency * Storage.scale;
            
            // Get noise value for this octave
            double noiseValue = GeneratePerlin.generatePerlinNoise(scaledX, scaledY, i);
            
            // Add weighted contribution
            total += noiseValue * amplitude;
            
            // Keep track of max possible value for normalization
            maxValue += amplitude;
            
            // Update frequency and amplitude for next octave
            frequency *= Storage.LACUNARITY;
            amplitude *= Storage.PERSISTENCE;
        }
        
        // Normalize to 0-1 range
        return (maxValue > 0) ? (total / maxValue) * 0.5 + 0.5 : 0;
    }
    
    private static double quantizeShades(double value, int shadeCount) {
        // Ensure at least 2 shades (black and white)
        shadeCount = Math.max(2, shadeCount);
        
        // Calculate the step size between shades
        double step = 1.0 / (shadeCount - 1);
        
        // Find the nearest shade level
        int level = (int)Math.round(value / step);
        
        // Convert back to a value between 0 and 1
        return level * step;
    }
    
    private static double[][] getNoiseMap(int x, int y) {
        String key = "c.x" + x + ".y" + y;
        
        if (Storage.CHUNK_CACHE.containsKey(key)) {
            return Storage.CHUNK_CACHE.get(key).noiseVal;
        }
        
        return generateChunk(x, y).noiseVal;
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        // Controls for movement
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
        	Storage.playerY -= 1;
        } 
        else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
        	Storage.playerY += 1;
        } 
        else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
        	Storage.playerX -= 1;
        } 
        else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
        	Storage.playerX += 1;
        } 
        // Controls for octave parameters
        else if (key == KeyEvent.VK_1) {
        	Storage.OCTAVE_COUNT = Math.max(1, Storage.OCTAVE_COUNT - 1);
        	Storage.CHUNK_CACHE.clear();  // Clear cache to regenerate all chunks
        } 
        else if (key == KeyEvent.VK_2) {
        	Storage.OCTAVE_COUNT = Math.min(4, Storage.OCTAVE_COUNT + 1);
        	Storage.CHUNK_CACHE.clear();
        // Controls for Lacunarity
        } 
        else if (key == KeyEvent.VK_3) {
        	Storage.LACUNARITY = Math.max(1.0, Storage.LACUNARITY - 0.1);
        	Storage.CHUNK_CACHE.clear();
        } 
        else if (key == KeyEvent.VK_4) {
        	Storage.LACUNARITY = Math.min(3.0, Storage.LACUNARITY + 0.1);
        	Storage.CHUNK_CACHE.clear();
        // Controls for Persistence
        } 
        else if (key == KeyEvent.VK_5) {
        	Storage.PERSISTENCE = Math.max(0.1, Storage.PERSISTENCE - 0.1);
        	Storage.CHUNK_CACHE.clear();
        } 
        else if (key == KeyEvent.VK_6) {
        	Storage.PERSISTENCE = Math.min(1.0, Storage.PERSISTENCE + 0.1);
        	Storage.CHUNK_CACHE.clear();
        }
        // Controls for Shades
        else if (key == KeyEvent.VK_7) {
        	Storage.shadeCount = Math.max(2, Storage.shadeCount - 1);
        } else if (key == KeyEvent.VK_8) {
        	Storage.shadeCount = Math.min(256, Storage.shadeCount + 1);
        }
        // Controls for Render Distance
        else if (key == KeyEvent.VK_9) {
        	Storage.RENDER_DISTANCE = Math.max(1, Storage.RENDER_DISTANCE - 1);
        }
        else if (key == KeyEvent.VK_0) {
        	Storage.RENDER_DISTANCE = Math.min(20, Storage.RENDER_DISTANCE  + 1);
        }
        // Controls for Zoom
        else if (key == KeyEvent.VK_PLUS || key == KeyEvent.VK_EQUALS) {
        	Storage.zoomLevel += Storage.ZOOM_FACTOR;
        	Storage.zoomLevel = Math.min(Storage.zoomLevel, 10.0); // Maximum zoom limit
        } 
        else if (key == KeyEvent.VK_MINUS) {
        	Storage.zoomLevel -= Storage.ZOOM_FACTOR;
        	Storage.zoomLevel = Math.max(Storage.zoomLevel, 0.5); // Minimum zoom limit
        }
        else if(key == KeyEvent.VK_Q) {
        	Storage.scale -= 0.05f;
        	Storage.scale = Math.max(Storage.scale, 0.05);
        }
        else if(key == KeyEvent.VK_E) {
        	Storage.scale += 0.05f;
        	Storage.scale = Math.min(Storage.scale, 2.0);
        }
        //Controls for Data Output
        else if (key == KeyEvent.VK_O) {
        	printData();
        }
        
        // Update title to include shade count and zoom level
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame != null) {
            frame.setTitle("_ChunkGen_ Seed: " + Storage.GLOBAL_SEED + ", " + 
            		Storage.playerX + "x. " + Storage.playerY + "y, O: " + Storage.OCTAVE_COUNT + 
                           ", L: " + String.format("%.1f", Storage.LACUNARITY) + 
                           ", P: " + String.format("%.1f", Storage.PERSISTENCE) +
                           ", Shades: " + Storage.shadeCount + 
                           ", Zoom: " + String.format("%.2fx", Storage.zoomLevel)+
                           ", Render: " + Storage.RENDER_DISTANCE +
                           ", Scale: " + Storage.scale);
        }
        
        repaint();
    }
    
    public void printData() {
    	System.out.println("Current Data: ");
    	System.out.println("Render Distance: " + Storage.RENDER_DISTANCE);
    	System.out.println("Global Seed: " + Storage.GLOBAL_SEED);
    	System.out.println("Octave Count: " + Storage.OCTAVE_COUNT);
    	System.out.println("Lacunarity: " + Storage.LACUNARITY);
    	System.out.println("Persistence: " + Storage.PERSISTENCE);
    	System.out.println("Shades: " + Storage.shadeCount);
    	System.out.println("Zoom: " + Storage.zoomLevel);
    	System.out.println("X: " + Storage.playerX + ", Y:" + Storage.playerY);
    	System.out.println("Chunk Cache: \n" + Storage.CHUNK_CACHE);
    	System.out.println("Corner Dict: \n" + Storage.CORNER_DICT);
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("_ChunkGen_");
        ChunkGeneration game = new ChunkGeneration();
        frame.add(game);
        frame.setSize(Storage.SCREEN_WIDTH, Storage.SCREEN_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
