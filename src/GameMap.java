import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Peta grid sektor pertambangan tempat eksplorasi berlangsung.
 * Menerapkan konsep COMPOSITION (0..* ke class Entity):
 * GameMap menciptakan, memiliki, dan mengelola siklus hidup seluruh objek Entity di dalam grid.
 */
public class GameMap {
    // [ENCAPSULATION & COMPOSITION] Atribut utama sesuai UML bersifat private
    private int size;
    private Entity[][] grid;

    // [ENCAPSULATION] Atribut pendukung untuk pelacakan Exit, Player, dan generator acak
    private Exit currentExit;
    private Player trackedPlayer;
    private final Random random;

    public GameMap(int size) {
        this.size = Math.max(5, size);
        this.grid = new Entity[this.size][this.size];
        this.random = new Random();
    }

    /**
     * [COMPOSITION] Membangun peta baru untuk hari ke-day serta mengacak posisi
     * OreVein, SupplyCrate, Exit, SentryTurret, dan Stalker.
     */
    public void generate(int day) {
        this.grid = new Entity[size][size];
        this.currentExit = null;

        // 1. Tempatkan Exit di lokasi yang cukup menantang dari titik awal (0,0)
        int[] exitPos = findEmptyCell(size - 2, -1, -1, 0);
        this.currentExit = new Exit(exitPos[0], exitPos[1]);
        setEntity(exitPos[0], exitPos[1], this.currentExit);

        // 2. Tempatkan OreVein (6 titik tambang dengan jumlah ore meningkat sesuai hari)
        int oreCount = 6;
        for (int i = 0; i < oreCount; i++) {
            int[] pos = findEmptyCell(1, -1, -1, 0);
            int oreAmount = 8 + day + random.nextInt(6);
            setEntity(pos[0], pos[1], new OreVein(pos[0], pos[1], oreAmount));
        }

        // 3. Tempatkan SupplyCrate (3 kotak suplai pemulih HP)
        int crateCount = 3;
        for (int i = 0; i < crateCount; i++) {
            int[] pos = findEmptyCell(1, -1, -1, 0);
            int healAmount = 15 + random.nextInt(11);
            setEntity(pos[0], pos[1], new SupplyCrate(pos[0], pos[1], healAmount));
        }

        // 4. Tempatkan SentryTurret (jumlah dan kekuatan meningkat bertahap sesuai hari)
        int turretCount = 1 + (day >= 4 ? 1 : 0) + (day >= 8 ? 1 : 0);
        int turretHp = (day <= 3) ? 20 : (day <= 7 ? 25 : 30);
        int turretDmg = (day <= 3) ? 8 : (day <= 7 ? 10 : 12);
        for (int i = 0; i < turretCount; i++) {
            // Jaga jarak minimal 3 petak dari spawn (0,0) dan minimal 2 petak dari Exit
            int[] pos = findEmptyCell(3, exitPos[0], exitPos[1], 2);
            setEntity(pos[0], pos[1], new SentryTurret(pos[0], pos[1], turretHp, turretDmg, 2));
        }

        // 5. Tempatkan Stalker (jumlah dan kekuatan meningkat bertahap sesuai hari)
        int stalkerCount = 1 + (day >= 3 ? 1 : 0) + (day >= 7 ? 1 : 0);
        int stalkerHp = (day <= 3) ? 20 : (day <= 7 ? 25 : 30);
        int stalkerDmg = (day <= 3) ? 9 : (day <= 7 ? 11 : 13);
        for (int i = 0; i < stalkerCount; i++) {
            int[] pos = findEmptyCell(3, -1, -1, 0);
            setEntity(pos[0], pos[1], new Stalker(pos[0], pos[1], stalkerHp, stalkerDmg, 1, this));
        }
    }

    /**
     * Menampilkan grid peta beserta posisi Player dan legenda simbol secara berdampingan.
     */
    public void display() {
        StringBuilder colHeader = new StringBuilder("     ");
        StringBuilder border = new StringBuilder("    +");
        for (int x = 0; x < size; x++) {
            colHeader.append(String.format(" %d ", x));
            border.append("---");
        }
        border.append("+");

        System.out.println(colHeader + "     LEGENDA SIMBOL PETA");
        System.out.println(border + "    +------------------------------------------+");

        for (int y = 0; y < size; y++) {
            StringBuilder row = new StringBuilder(String.format(" %2d |", y));
            for (int x = 0; x < size; x++) {
                char sym;
                if (trackedPlayer != null && trackedPlayer.getX() == x && trackedPlayer.getY() == y) {
                    sym = 'P';
                } else if (grid[y][x] != null) {
                    sym = grid[y][x].getSymbol();
                } else {
                    sym = '.';
                }
                row.append(" ").append(sym).append(" ");
            }
            row.append("|");
            row.append("    ").append(getSideLegendLine(y));
            System.out.println(row);
        }

        System.out.println(border);
    }

    /**
     * Overload display(Player p) untuk memperbarui referensi Player lalu menggambar peta.
     */
    public void display(Player p) {
        this.trackedPlayer = p;
        display();
    }

    /**
     * Mengecek apakah koordinat (x, y) masih berada di dalam batas peta.
     */
    public boolean isInside(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    /**
     * Mengambil objek Entity pada koordinat (x, y), atau null jika kosong/di luar batas.
     */
    public Entity getEntity(int x, int y) {
        if (!isInside(x, y)) {
            return null;
        }
        return grid[y][x];
    }

    /**
     * [POLYMORPHISM] Menjalankan act(p) untuk seluruh Hostile yang masih hidup di peta,
     * serta menghapus Hostile dari grid apabila HP-nya habis (isDead).
     */
    public void updateHostiles(Player p) {
        if (p == null || p.isDead() || isExitReached()) {
            return;
        }

        // Kumpulkan semua Hostile aktif terlebih dahulu agar mob yang berpindah petak
        // tidak diproses dua kali pada giliran yang sama.
        List<Hostile> activeHostiles = new ArrayList<>();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Entity entity = grid[y][x];
                if (entity instanceof Hostile) {
                    Hostile hostile = (Hostile) entity;
                    if (hostile.isDead()) {
                        grid[y][x] = null;
                    } else {
                        activeHostiles.add(hostile);
                    }
                }
            }
        }

        for (Hostile hostile : activeHostiles) {
            if (p.isDead()) {
                break;
            }
            if (!hostile.isDead()) {
                // Pemanggilan polymorphic: SentryTurret menembak jarak jauh, Stalker mengejar Player
                hostile.act(p);
            }
            if (hostile.isDead()) {
                removeEntityInstance(hostile);
            }
        }
    }

    // --- Helper Methods ---

    public void setTrackedPlayer(Player trackedPlayer) {
        this.trackedPlayer = trackedPlayer;
    }

    public void setEntity(int x, int y, Entity entity) {
        if (isInside(x, y)) {
            grid[y][x] = entity;
        }
    }

    public void removeEntity(int x, int y) {
        if (isInside(x, y)) {
            grid[y][x] = null;
        }
    }

    public void moveEntity(Entity entity, int newX, int newY) {
        if (entity == null || !isInside(newX, newY)) {
            return;
        }
        int oldX = entity.getX();
        int oldY = entity.getY();
        if (isInside(oldX, oldY) && grid[oldY][oldX] == entity) {
            grid[oldY][oldX] = null;
        }
        entity.setPosition(newX, newY);
        grid[newY][newX] = entity;
    }

    public boolean isExitReached() {
        return currentExit != null && currentExit.isReached();
    }

    public int getSize() {
        return size;
    }

    private void removeEntityInstance(Entity target) {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (grid[y][x] == target) {
                    grid[y][x] = null;
                }
            }
        }
    }

    private int[] findEmptyCell(int minDistFromSpawn, int avoidX, int avoidY, int minDistFromAvoid) {
        for (int attempt = 0; attempt < 300; attempt++) {
            int rx = random.nextInt(size);
            int ry = random.nextInt(size);
            if (rx == 0 && ry == 0) {
                continue;
            }
            if (grid[ry][rx] != null) {
                continue;
            }
            int spawnDist = rx + ry;
            if (spawnDist < minDistFromSpawn) {
                continue;
            }
            if (avoidX >= 0 && avoidY >= 0) {
                int avoidDist = Math.abs(rx - avoidX) + Math.abs(ry - avoidY);
                if (avoidDist < minDistFromAvoid) {
                    continue;
                }
            }
            return new int[]{rx, ry};
        }

        // Fallback deterministik jika percobaan acak penuh
        for (int y = size - 1; y >= 0; y--) {
            for (int x = size - 1; x >= 0; x--) {
                if ((x != 0 || y != 0) && grid[y][x] == null) {
                    return new int[]{x, y};
                }
            }
        }
        return new int[]{size - 1, size - 1};
    }

    private String getSideLegendLine(int rowIndex) {
        switch (rowIndex) {
            case 0:
                return "| [P] Player (Anda)    [.] Petak Kosong    |";
            case 1:
                return "| [O] Ore Vein (+Ore)  [C] Supply (+HP)    |";
            case 2:
                return "| [T] Sentry Turret    [S] Stalker (Aktif) |";
            case 3:
                return "| [E] Exit Lift (Jalur Evakuasi Aman)      |";
            case 4:
                return "+------------------------------------------+";
            case 5:
                return "KONTROL GERAK (-1 Stamina / langkah):";
            case 6:
                return "  [W] Atas    [S] Bawah";
            case 7:
                return "  [A] Kiri    [D] Kanan";
            default:
                return "";
        }
    }
}
