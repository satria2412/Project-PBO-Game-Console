/**
 * Boss penjaga inti tambang yang muncul pada akhir Hari ke-10.
 * Menerapkan INHERITANCE (turunan dari Hostile) dan POLYMORPHISM (override act)
 * dengan mekanik perubahan fase (phase) saat HP turun di bawah atau sama dengan 50%.
 */
public class CoreWarden extends Hostile {
    // [ENCAPSULATION] Atribut private sesuai UML
    private int phase;
    private int turnCount;

    public CoreWarden(int hp, int damage) {
        super('W', 0, 0, hp, damage, "Core Warden");
        this.phase = 1;
        this.turnCount = 0;
    }

    public CoreWarden() {
        this(150, 14);
    }

    /**
     * [POLYMORPHISM] Mengecek perubahan fase berdasarkan sisa HP lalu melancarkan serangan Boss.
     */
    @Override
    public void act(Player p) {
        if (p == null || isDead() || p.isDead()) {
            return;
        }

        // Cek transisi ke Phase 2 saat HP <= 50%
        checkPhaseTransition(p);

        turnCount++;
        int rawDamage;
        String attackName;

        if (phase == 1) {
            if (turnCount % 3 == 0) {
                attackName = "Twin Plasma Beam";
                rawDamage = this.damage + 4;
            } else {
                attackName = "Seismic Core Slam";
                rawDamage = this.damage;
            }
        } else {
            if (turnCount % 3 == 0) {
                attackName = "Hypernova Discharge";
                rawDamage = this.damage + 6;
            } else {
                attackName = "Overdrive Meltdown Wave";
                rawDamage = this.damage;
            }
        }

        boolean wasDefending = p.isDefending();
        int beforeHp = p.getHp();
        p.takeDamage(rawDamage);
        int actualLost = beforeHp - p.getHp();

        if (wasDefending) {
            p.addLog("[BOSS - FASE " + phase + "] Core Warden melancarkan " + attackName
                    + "! Perisai Aegis menahan serangan -> -" + actualLost + " HP.");
        } else {
            p.addLog("[BOSS - FASE " + phase + "] Core Warden menghantam dengan " + attackName
                    + " -> -" + actualLost + " HP!");
        }
    }

    /**
     * Memeriksa dan mengaktifkan Phase 2 apabila HP turun ke 50% atau kurang.
     */
    public void checkPhaseTransition(Player p) {
        if (this.phase == 1 && this.hp <= (getMaxHp() / 2) && !isDead()) {
            this.phase = 2;
            this.damage += 6;
            if (p != null) {
                p.addLog("[PERINGATAN] HP Core Warden di bawah 50%! Memasuki PHASE 2 (OVERDRIVE): ATK meningkat!");
            }
        }
    }

    public int getPhase() {
        return phase;
    }
}
