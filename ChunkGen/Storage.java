import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Storage {
    public static final Random rand = new Random();
    public static final long serialVersionUID = 1;
    // Game settings
    public static final int SCREEN_WIDTH = 900;
    public static final int SCREEN_HEIGHT = 900;
    public static final int VOXEL_SIZE = 10;
    public final static int CHUNK_SIZE = 16;
    public static int RENDER_DISTANCE = 1;
    public static final int GLOBAL_SEED = 7;
    public static int shadeCount = 10;
    public static double scale = 1.0;
    
    // Zoom settings
    public static double zoomLevel = 1;
    public static final double ZOOM_FACTOR = 0.5; // Factor by which zoom changes
    
    // fBm settings
    public static int OCTAVE_COUNT = 1;  // Number of octaves to layer
    public static double LACUNARITY = 2.0;  // How frequency increases with each octave
    public static double PERSISTENCE = 0.5;  // How amplitude decreases with each octave
    
    public static Map<String, ChunkData> CHUNK_CACHE = new HashMap<>();
    public static Map<CornerKey, Integer> CORNER_DICT = new HashMap<>();
    
    // Player position in chunk coordinates
    public static int playerX = 0; //rand.nextInt(-10000, 10000);
    public static int playerY = 0; //rand.nextInt(-10000, 10000);
}

class CornerKey{
    int x, y, octave;
    
    public CornerKey(int x, int y, int octave) {
        this.x = x;
        this.y = y;
        this.octave = octave;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CornerKey other = (CornerKey) obj;
        return x == other.x && y == other.y && octave == other.octave;
    }
    
    @Override
    public int hashCode() {
        return 31 * (31 * x + y) + octave;
    }
}

class ChunkData{
    String key;
    double[][] noiseVal;
    
    public ChunkData(String key, double[][] noiseVal) {
        this.key = key;
        this.noiseVal = noiseVal;
    }
}

class VectorPoint {
    int x, y, angle;
    
    public VectorPoint(int x, int y, int angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }
}
