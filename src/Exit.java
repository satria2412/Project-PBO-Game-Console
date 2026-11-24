/**
 * Objek jalur evakuasi (lift keluar sektor tambang) untuk mengakhiri hari dengan aman.
 * Menerapkan INHERITANCE (turunan dari Entity) dan POLYMORPHISM (override interact).
 */
public class Exit extends Entity {
    // [ENCAPSULATION] Atribut private sesuai UML
    private boolean reached;

    public Exit(int x, int y) {
        super('E', x, y);
        this.reached = false;
    }

    /**
     * [POLYMORPHISM] Menandai bahwa Player telah mencapai titik evakuasi hari ini.
     */
    @Override
    public void interact(Player p) {
        this.reached = true;
        if (p != null) {
            p.addLog("[EVAKUASI] Anda mencapai Exit Lift di (" + x + "," + y + ")! Kembali ke markas...");
        }
    }

    public boolean isReached() {
        return reached;
    }
}
