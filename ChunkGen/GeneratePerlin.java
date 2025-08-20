
public class GeneratePerlin {
	public static double generatePerlinNoise(double x, double y, int octave) {
        // Get integer coordinates
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        
        // Get interpolation weights
        double sx = x - x0;
        double sy = y - y0;
        
        // Get corner gradients
        int gradTL = getCornerGradient(x0, y0, octave);
        int gradTR = getCornerGradient(x1, y0, octave);
        int gradBL = getCornerGradient(x0, y1, octave);
        int gradBR = getCornerGradient(x1, y1, octave);
        
        // Calculate dot products
        double dotTL = getDotProduct(x0, y0, x, y, gradTL);
        double dotTR = getDotProduct(x1, y0, x, y, gradTR);
        double dotBL = getDotProduct(x0, y1, x, y, gradBL);
        double dotBR = getDotProduct(x1, y1, x, y, gradBR);
        
        // Interpolate results
        double ix0 = lerp(sx, dotTL, dotTR);
        double ix1 = lerp(sx, dotBL, dotBR);
        double result = lerp(sy, ix0, ix1);
        
        return result;
    }
	
	private static int getCornerGradient(int x, int y, int octave) {
        CornerKey key = new CornerKey(x, y, octave);
        Integer angle = Storage.CORNER_DICT.get(key);
        
        if (angle == null) {
            angle = hashRandom(x, y, Storage.GLOBAL_SEED + octave, 0, 360);
            Storage.CORNER_DICT.put(key, angle);
        }
        
        return angle;
    }
    
    private static double lerp(double t, double a, double b) {
        t = smoothstep(0, 1, t);
        return a + t * (b - a);
    }
    
    private static double smoothstep(double edge0, double edge1, double x) {
        x = clamp(((x - edge0) / (edge1 - edge0)), 0, 1);
        return x * x * (3.0 - 2.0 * x);
    }
    
    private static double clamp(double x, double lowerlimit, double upperlimit) {
        if (x < lowerlimit) return lowerlimit;
        if (x > upperlimit) return upperlimit;
        return x;
    }
    
    private static double getDotProduct(double gridX, double gridY, double pointX, double pointY, int angle) {
        double dx = pointX - gridX;
        double dy = pointY - gridY;
        double angleRad = Math.toRadians(angle);
        return (dx * Math.cos(angleRad)) + (dy * Math.sin(angleRad));
    }
    
    private static int hashRandom(int x, int y, int globalSeed, int minVal, int maxVal) {
        long seed = (globalSeed * 73856093L) ^ (x * 19349663L) ^ (y * 83492791L);
        
        // Mix the bits more (simple xorshift + bitwise rotation)
        seed ^= (seed >> 13);
        seed ^= (seed << 17);
        seed ^= (seed >> 5);
        
        seed &= 0xFFFFFFFFL;  // Keep it in 32-bit unsigned range
        
        return minVal + (int)(seed % (maxVal - minVal + 1));
    }
}
