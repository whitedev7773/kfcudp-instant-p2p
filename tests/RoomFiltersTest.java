import kfc.udp.client.gui.RoomFilters;
public class RoomFiltersTest {
    private static int checks;
    private static void check(boolean v) { if (!v) throw new AssertionError("Check " + checks); checks++; }
    public static void main(String[] args) {
        check(RoomFilters.ALL.matches(2, 0, "", "1.21.5"));
        RoomFilters party = new RoomFilters(8, 2, true);
        check(party.matches(6, 8, "1.21.5", "1.21.5"));
        check(!party.matches(7, 8, "1.21.5", "1.21.5"));
        check(!party.matches(2, 16, "1.21.5", "1.21.5"));
        check(!party.matches(2, 0, "1.21.5", "1.21.5"));
        check(!party.matches(2, 8, "1.21.1", "1.21.5"));
        RoomFilters other = new RoomFilters(0, 0, false);
        check(other.matches(8, 8, "1.21.1", "1.21.5"));
        check(other.matches(2, 8, "1.21.5", "1.21.5"));
        check(other.matches(2, 8, "", "1.21.5"));
        check(!new RoomFilters(4, 4, false).valid());
        check(new RoomFilters(4, 3, false).valid());
        check(party.activeCount() == 3 && RoomFilters.ALL.activeCount() == 0);
        System.out.println("RoomFilters: " + checks + " checks passed");
    }
}
