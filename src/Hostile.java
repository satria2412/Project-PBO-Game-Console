/**
 * Abstract class untuk seluruh entitas musuh berbahaya di dalam game.
 * Menerapkan MULTILEVEL INHERITANCE (Entity -> Hostile -> Subclass Musuh)
 * serta POLYMORPHISM melalui method abstract act(Player p).
 */
public abstract class Hostile extends Entity {
    // [ENCAPSULATION & INHERITANCE] Atribut protected sesuai UML
    protected int hp;
    protected int damage;

    // [ENCAPSULATION] Atribut pendukung bersifat private
    private int maxHp;
    private String name;

    public Hostile(char symbol, int x, int y, int hp, int damage, String name) {
        super(symbol, x, y);
        this.maxHp = Math.max(1, hp);
        this.hp = this.maxHp;
        this.damage = Math.max(1, damage);
        this.name = name;
    }

    /**
     * [POLYMORPHISM] Aksi unik masing-masing tipe musuh pada gilirannya.
     */
    public abstract void act(Player p);

    /**
     * Pertarungan jarak dekat: HP Player dan HP Hostile sama-sama berkurang.
     */
    public void fight(Player p) {
        if (p == null || isDead() || p.isDead()) {
            return;
        }
        int playerAtk = p.getAttackDamage();
        this.takeDamage(playerAtk);

        int beforePlayerHp = p.getHp();
        p.takeDamage(this.damage);
        int takenByPlayer = beforePlayerHp - p.getHp();

        if (this.isDead()) {
            p.incrementMonstersDefeated();
            p.addLog("[TEMPUR] Bentrok dengan " + name + "! Anda memberi " + playerAtk
                    + " DMG, menerima " + takenByPlayer + " DMG -> " + name + " HANCUR!");
        } else {
            p.addLog("[TEMPUR] Bentrok dengan " + name + "! Anda memberi " + playerAtk
                    + " DMG (Sisa HP " + name + ": " + hp + "/" + maxHp
                    + "), menerima " + takenByPlayer + " DMG.");
        }
    }

    public void takeDamage(int d) {
        if (d > 0) {
            this.hp = Math.max(0, this.hp - d);
        }
    }

    public boolean isDead() {
        return this.hp <= 0;
    }

    /**
     * [POLYMORPHISM] Saat Player menabrak petak musuh, otomatis memicu fight(p).
     */
    @Override
    public void interact(Player p) {
        fight(p);
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getDamage() {
        return damage;
    }

    public String getName() {
        return name;
    }
}
