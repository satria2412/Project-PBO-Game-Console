import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Pedagang keliling yang menawarkan suplai setiap awal hari.
 * Menerapkan konsep AGGREGATION (0..* ke class Item):
 * Merchant menampung koleksi objek Item di dalam list stock,
 * di mana objek Item tetap independen dan dapat diaplikasikan ke Player.
 */
public class Merchant {
    // [AGGREGATION] Merchant memiliki daftar referensi ke objek Item (0..*)
    // [ENCAPSULATION] Atribut stock bersifat private
    private List<Item> stock;
    private final Random random;

    public Merchant() {
        this.stock = new ArrayList<>();
        this.random = new Random();
    }

    /**
     * Mengisi ulang stok barang dagangan secara variatif setiap pergantian hari.
     */
    public void restock() {
        stock.clear();

        // Katalog item penambangan (selalu menjamin ketersediaan pemulih HP & Stamina)
        Item[] healPool = {
            new Item("Nano Bandage", 8, 20, 0),
            new Item("Medkit Standar", 14, 35, 0),
            new Item("Bio-Repair Kit", 20, 55, 0)
        };

        Item[] staminaPool = {
            new Item("Energy Drink", 7, 0, 6),
            new Item("Hyper Battery", 12, 0, 10),
            new Item("Overdrive Tonic", 15, 5, 12)
        };

        Item[] comboPool = {
            new Item("Miner Ration", 10, 15, 5),
            new Item("Combat Stimpack", 16, 25, 7),
            new Item("Core Elixir", 22, 40, 9)
        };

        // Pilih kombinasi 4 item agar pilihan bervariasi namun selalu seimbang
        stock.add(healPool[random.nextInt(healPool.length)]);
        stock.add(staminaPool[random.nextInt(staminaPool.length)]);
        stock.add(comboPool[random.nextInt(comboPool.length)]);

        // Slot ke-4 acak dari seluruh pool
        List<Item> extraCandidates = new ArrayList<>();
        Collections.addAll(extraCandidates, healPool);
        Collections.addAll(extraCandidates, staminaPool);
        Collections.addAll(extraCandidates, comboPool);
        stock.add(extraCandidates.get(random.nextInt(extraCandidates.size())));
    }

    /**
     * Menampilkan daftar stok barang dagangan yang tersedia ke layar console.
     */
    public void showStock() {
        System.out.println("  +----+----------------------+------------------------+------------+");
        System.out.println("  | No | Nama Barang          | Efek Suplai            | Harga      |");
        System.out.println("  +----+----------------------+------------------------+------------+");
        if (stock.isEmpty()) {
            System.out.println("  | -- | [STOK HABIS TERJUAL HARI INI]                 |     --     |");
        } else {
            for (int i = 0; i < stock.size(); i++) {
                Item item = stock.get(i);
                System.out.printf("  | %-2d | %-20s | %-22s | %4d Ore   |%n",
                        (i + 1),
                        item.getName(),
                        item.getEffectDescription(),
                        item.getPrice());
            }
        }
        System.out.println("  +----+----------------------+------------------------+------------+");
    }

    /**
     * Menjual item pada indeks ke-i (0-based) kepada Player.
     * Mengecek kecukupan ore, mengurangi ore, menerapkan efek item, dan menghapus dari stok.
     */
    public boolean sell(Player p, int i) {
        if (p == null || i < 0 || i >= stock.size()) {
            return false;
        }
        Item selected = stock.get(i);
        if (p.getOre() < selected.getPrice()) {
            return false;
        }
        p.spendOre(selected.getPrice());
        selected.applyTo(p);
        stock.remove(i);
        return true;
    }

    public int getStockSize() {
        return stock.size();
    }

    public Item getItem(int i) {
        if (i < 0 || i >= stock.size()) {
            return null;
        }
        return stock.get(i);
    }
}
