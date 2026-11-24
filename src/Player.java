import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representasi karakter pemain dalam permainan.
 * Menerapkan konsep ENCAPSULATION: seluruh atribut bersifat private
 * dan diakses/dimodifikasi secara terkontrol melalui method publik.
 */
public class Player {
    // [ENCAPSULATION] Atribut utama sesuai UML bersifat private
    private String name;
    private int hp;
    private int stamina;
    private int ore;
    private int x;
    private int y;

    // [ENCAPSULATION] Atribut pendukung untuk batas status, pertarungan, dan statistik
    private int maxHp;
    private int maxStamina;
    private int attackDamage;
    private boolean defending;
    private int totalOreCollected;
    private int monstersDefeated;
    private final List<String> actionLogs;

    public Player(String name) {
        this.name = (name == null || name.trim().isEmpty()) ? "Miner-01" : name.trim();
        this.maxHp = 100;
        this.hp = this.maxHp;
        this.maxStamina = 25;
        this.stamina = 16;
        this.ore = 10; // Bekal awal penambang
        this.x = 0;
        this.y = 0;
        this.attackDamage = 25;
        this.defending = false;
        this.totalOreCollected = 0;
        this.monstersDefeated = 0;
        this.actionLogs = new ArrayList<>();
    }

    /**
     * Menggerakkan koordinat pemain berdasarkan input arah W/A/S/D (case-insensitive).
     */
    public boolean move(char dir) {
        char upper = Character.toUpperCase(dir);
        switch (upper) {
            case 'W':
                this.y--;
                return true;
            case 'S':
                this.y++;
                return true;
            case 'A':
                this.x--;
                return true;
            case 'D':
                this.x++;
                return true;
            default:
                return false;
        }
    }

    /**
     * Mengurangi HP pemain saat terkena serangan.
     * Jika pemain sedang dalam mode bertahan (defending), damage berkurang 50%.
     */
    public void takeDamage(int dmg) {
        if (dmg <= 0) {
            return;
        }
        int actualDamage = defending ? Math.max(1, dmg / 2) : dmg;
        this.hp = Math.max(0, this.hp - actualDamage);
    }

    /**
     * Memulihkan HP pemain tanpa melebihi batas maxHp.
     */
    public void heal(int n) {
        if (n > 0) {
            this.hp = Math.min(this.maxHp, this.hp + n);
        }
    }

    /**
     * Mengurangi stamina pemain saat beraktivitas/berpindah petak.
     */
    public void useStamina(int n) {
        if (n > 0) {
            this.stamina = Math.max(0, this.stamina - n);
        }
    }

    /**
     * Menambahkan jumlah ore hasil tambang ke inventori pemain.
     */
    public void addOre(int n) {
        if (n > 0) {
            this.ore += n;
            this.totalOreCollected += n;
        }
    }

    /**
     * Mengecek apakah pemain sudah gugur (HP <= 0).
     */
    public boolean isDead() {
        return this.hp <= 0;
    }

    // --- Method pendukung & Getter/Setter (Encapsulation) ---

    public void restoreStamina(int n) {
        if (n > 0) {
            this.stamina = Math.min(this.maxStamina, this.stamina + n);
        }
    }

    /**
     * Pemulihan stamina harian saat pergantian hari (istirahat di markas).
     */
    public void recoverDailyStamina() {
        int recovered = Math.max(14, this.stamina + 14);
        this.stamina = Math.min(this.maxStamina, recovered);
    }

    public boolean spendOre(int amount) {
        if (amount < 0 || this.ore < amount) {
            return false;
        }
        this.ore -= amount;
        return true;
    }

    public static boolean isValidDirection(char dir) {
        char upper = Character.toUpperCase(dir);
        return upper == 'W' || upper == 'A' || upper == 'S' || upper == 'D';
    }

    public int previewNextX(char dir) {
        char upper = Character.toUpperCase(dir);
        if (upper == 'A') return this.x - 1;
        if (upper == 'D') return this.x + 1;
        return this.x;
    }

    public int previewNextY(char dir) {
        char upper = Character.toUpperCase(dir);
        if (upper == 'W') return this.y - 1;
        if (upper == 'S') return this.y + 1;
        return this.y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getStamina() {
        return stamina;
    }

    public int getMaxStamina() {
        return maxStamina;
    }

    public int getOre() {
        return ore;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public boolean isDefending() {
        return defending;
    }

    public void setDefending(boolean defending) {
        this.defending = defending;
    }

    public int getTotalOreCollected() {
        return totalOreCollected;
    }

    public int getMonstersDefeated() {
        return monstersDefeated;
    }

    public void incrementMonstersDefeated() {
        this.monstersDefeated++;
    }

    public void addLog(String message) {
        if (message != null && !message.isEmpty()) {
            this.actionLogs.add(message);
        }
    }

    public void clearLogs() {
        this.actionLogs.clear();
    }

    public List<String> getActionLogs() {
        return Collections.unmodifiableList(this.actionLogs);
    }
}
