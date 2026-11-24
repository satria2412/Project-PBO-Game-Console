/**
 * Objek tambang bijih mineral di peta.
 * Menerapkan INHERITANCE (turunan dari Entity) dan POLYMORPHISM (override interact).
 */
public class OreVein extends Entity {
    // [ENCAPSULATION] Atribut private sesuai UML
    private int amount;

    public OreVein(int x, int y, int amount) {
        super('O', x, y);
        this.amount = Math.max(1, amount);
    }

    /**
     * [POLYMORPHISM] Menambahkan ore ke Player ketika ditambang.
     */
    @Override
    public void interact(Player p) {
        if (p == null) {
            return;
        }
        p.addOre(amount);
        p.addLog("[TAMBANG] Menambang Ore Vein di (" + x + "," + y + ") -> +" + amount + " Ore!");
    }

    public int getAmount() {
        return amount;
    }
}
