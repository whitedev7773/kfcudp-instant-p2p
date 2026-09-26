package kfc.udp.client.gui;

/** Discovery filters: advertised room capacity, party space and Minecraft version. */
public record RoomFilters(int maxCapacity, int minFreeSlots, boolean sameVersionOnly) {
    public static final RoomFilters ALL = new RoomFilters(0, 0, false);

    public boolean valid() {
        return maxCapacity >= 0 && minFreeSlots >= 0
                && (maxCapacity == 0 || minFreeSlots < maxCapacity);
    }

    public int activeCount() {
        return (maxCapacity > 0 ? 1 : 0) + (minFreeSlots > 0 ? 1 : 0) + (sameVersionOnly ? 1 : 0);
    }

    public boolean matches(int players, int capacity, String roomVersion, String currentVersion) {
        if (!valid()) return false;
        if (maxCapacity > 0 && (capacity <= 0 || capacity > maxCapacity)) return false;
        if (minFreeSlots > 0 && (capacity <= 0 || (long) capacity - players < minFreeSlots)) return false;
        return !sameVersionOnly || currentVersion.equals(roomVersion);
    }
}
