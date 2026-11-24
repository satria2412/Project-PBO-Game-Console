/**
 * Objek kotak suplai medis darurat di peta.
 * Menerapkan INHERITANCE (turunan dari Entity) dan POLYMORPHISM (override interact).
 */
public class SupplyCrate extends Entity {
    // [ENCAPSULATION] Atribut private sesuai UML
    private int healAmount;

    public SupplyCrate(int x, int y, int healAmount) {
        super('C', x, y);
        this.healAmount = Math.max(1, healAmount);
    }

    /**
     * [POLYMORPHISM] Memulihkan HP Player ketika kotak suplai dibuka.
     */
    @Override
    public void interact(Player p) {
        if (p == null) {
            return;
        }
        int beforeHp = p.getHp();
        p.heal(healAmount);
        int gained = p.getHp() - beforeHp;
        p.addLog("[SUPLAI] Membuka Supply Crate di (" + x + "," + y + ") -> +" + gained
                + " HP (Potensi +" + healAmount + " HP).");
    }

    public int getHealAmount() {
        return healAmount;
    }
}
