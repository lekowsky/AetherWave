package pl.skyrise.mCPhone.models;

import org.bukkit.Location;

import java.util.List;

/**
 * Model punktu sprzedaży kart SIM
 */
public class SimShop {

    private final String id;
    private final String name;
    private final Location location;
    private final int radius;
    private final List<String> availableSims;

    public SimShop(String id, String name, Location location, int radius, List<String> availableSims) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.radius = radius;
        this.availableSims = availableSims;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public int getRadius() {
        return radius;
    }

    public List<String> getAvailableSims() {
        return availableSims;
    }

    public boolean isInRange(Location playerLocation) {
        if (!playerLocation.getWorld().equals(location.getWorld())) {
            return false;
        }
        return playerLocation.distance(location) <= radius;
    }

    @Override
    public String toString() {
        return "SimShop{id='" + id + "', name='" + name + "', location=" + location + "}";
    }
}
