/**
 * Musuh statis penjaga sektor yang menembakkan laser jika Player masuk ke jarak tembak.
 * Menerapkan INHERITANCE (turunan dari Hostile) dan POLYMORPHISM (override act).
 */
public class SentryTurret extends Hostile {
    // [ENCAPSULATION] Atribut private sesuai UML
    private int range;

    public SentryTurret(int x, int y, int hp, int damage, int range) {
        super('T', x, y, hp, damage, "Sentry Turret");
        this.range = Math.max(1, range);
    }

    public SentryTurret(int x, int y) {
        this(x, y, 20, 8, 2);
    }

    /**
     * [POLYMORPHISM] Diam di tempat; menembak Player bila berada dalam jarak Manhattan <= range.
     */
    @Override
    public void act(Player p) {
        if (p == null || isDead() || p.isDead()) {
            return;
        }
        int dist = Math.abs(p.getX() - this.x) + Math.abs(p.getY() - this.y);
        if (dist <= this.range) {
            int beforeHp = p.getHp();
            p.takeDamage(this.damage);
            int actualDmg = beforeHp - p.getHp();
            p.addLog("[TURRET] Sentry Turret di (" + x + "," + y + ") menembak laser (jarak "
                    + dist + " petak) -> -" + actualDmg + " HP!");
        }
    }

    public int getRange() {
        return range;
    }
}
