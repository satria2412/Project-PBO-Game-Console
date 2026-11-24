/**
 * Musuh pemburu aktif yang mengejar koordinat Player di atas peta.
 * Menerapkan INHERITANCE (turunan dari Hostile) dan POLYMORPHISM (override act).
 */
public class Stalker extends Hostile {
    // [ENCAPSULATION] Atribut private sesuai UML
    private int speed;
    private GameMap map;

    public Stalker(int x, int y, int hp, int damage, int speed, GameMap map) {
        super('S', x, y, hp, damage, "Stalker");
        this.speed = Math.max(1, speed);
        this.map = map;
    }

    public Stalker(int x, int y, int speed) {
        this(x, y, 20, 9, speed, null);
    }

    /**
     * [POLYMORPHISM] Mengejar posisi Player sesuai speed; jika mengenai Player memicu fight(p).
     */
    @Override
    public void act(Player p) {
        if (p == null || isDead() || p.isDead()) {
            return;
        }

        for (int step = 0; step < speed; step++) {
            int dx = p.getX() - this.x;
            int dy = p.getY() - this.y;
            int manhattan = Math.abs(dx) + Math.abs(dy);

            if (manhattan == 0) {
                fight(p);
                break;
            }

            // Jika sudah bersebelahan (jarak 1 petak), langkah ini mengenai Player -> picu fight(p)
            if (manhattan == 1) {
                p.addLog("[STALKER] Stalker di (" + x + "," + y + ") menerjang ke arah Anda!");
                fight(p);
                break;
            }

            int stepX = Integer.compare(dx, 0);
            int stepY = Integer.compare(dy, 0);

            boolean moved = false;
            if (Math.abs(dx) >= Math.abs(dy)) {
                moved = tryMoveTo(this.x + stepX, this.y, p);
                if (!moved && stepY != 0) {
                    moved = tryMoveTo(this.x, this.y + stepY, p);
                }
            } else {
                moved = tryMoveTo(this.x, this.y + stepY, p);
                if (!moved && stepX != 0) {
                    moved = tryMoveTo(this.x + stepX, this.y, p);
                }
            }

            if (!moved) {
                break;
            }
        }
    }

    private boolean tryMoveTo(int nextX, int nextY, Player p) {
        if (nextX == p.getX() && nextY == p.getY()) {
            fight(p);
            return false;
        }
        if (map == null) {
            setPosition(nextX, nextY);
            return true;
        }
        if (map.isInside(nextX, nextY) && map.getEntity(nextX, nextY) == null) {
            int oldX = this.x;
            int oldY = this.y;
            map.moveEntity(this, nextX, nextY);
            int newDist = Math.abs(p.getX() - nextX) + Math.abs(p.getY() - nextY);
            if (newDist <= 2) {
                p.addLog("[RADAR] Stalker bergerak mendekat (" + oldX + "," + oldY
                        + ") -> (" + nextX + "," + nextY + ").");
            }
            return true;
        }
        return false;
    }

    public int getSpeed() {
        return speed;
    }
}
