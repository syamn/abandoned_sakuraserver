package syam.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Cuboid {
    public Cuboid(Location point1, Location point2) {
        this.xMin = Math.min(point1.getBlockX(), point2.getBlockX());
        this.xMax = Math.max(point1.getBlockX(), point2.getBlockX());
        this.yMin = Math.min(point1.getBlockY(), point2.getBlockY());
        this.yMax = Math.max(point1.getBlockY(), point2.getBlockY());
        this.zMin = Math.min(point1.getBlockZ(), point2.getBlockZ());
        this.zMax = Math.max(point1.getBlockZ(), point2.getBlockZ());
        this.world = point1.getWorld();
    }
    
    public int xMin, xMax, yMin, yMax, zMin, zMax;
    public World world;
    
    public boolean isIn(Location loc) {
        if (loc.getWorld() != this.world) return false;
        if (loc.getBlockX() < xMin) return false;
        if (loc.getBlockX() > xMax) return false;
        if (loc.getBlockY() < yMin) return false;
        if (loc.getBlockY() > yMax) return false;
        if (loc.getBlockZ() < zMin) return false;
        if (loc.getBlockZ() > zMax) return false;
        return true;
    }
    
    public int getXWidth() {
        return xMax - xMin;
    }
    
    public int getZWidth() {
        return zMax - zMin;
    }
    
    public int getHeight() {
        return yMax - yMin;
    }
    
    public int getArea() {
        return getHeight() * getXWidth() * getZWidth();
    }
    
    public List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<Block>();
        
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; y <= zMax; z++) {
                    // リストにブロックを追加
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }
        
        return blocks;
    }
}