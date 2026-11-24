import java.util.List;
import java.util.Scanner;

/**
 * Pengendali utama alur permainan dari Hari ke-1 hingga pertarungan Boss Hari ke-10.
 * Menerapkan konsep COMPOSITION terhadap Player, GameMap, dan Merchant:
 * ketiga objek inti tersebut diinstansiasi dan dikelola penuh di dalam siklus hidup Game.
 */
public class Game {
    // [ENCAPSULATION & COMPOSITION] Atribut utama sesuai UML bersifat private
    private Player player;
    private GameMap map;
    private Merchant merchant;
    private int day;
    private static final int MAX_DAY = 10;

    // [ENCAPSULATION] Scanner input console dikelola secara private
    private final Scanner scanner;

    public Game() {
        this.scanner = new Scanner(System.in);
        this.day = 1;
    }

    /**
     * Memulai keseluruhan alur permainan sesuai flowchart:
     * Judul -> Input Nama -> Loop Hari 1..10 -> Boss Fight -> Hasil Akhir.
     */
    public void start() {
        clearScreen();
        printTitleBanner();

        String playerName = promptPlayerName();

        // [COMPOSITION] Game menciptakan objek Player, GameMap, dan Merchant miliknya sendiri
        this.player = new Player(playerName);
        this.map = new GameMap(8);
        this.map.setTrackedPlayer(this.player);
        this.merchant = new Merchant();
        this.day = 1;

        // Loop Hari (Day 1 s/d MAX_DAY = 10)
        while (day <= MAX_DAY) {
            startDay();

            // Loop Giliran Eksplorasi Peta
            while (!player.isDead() && !isDayOver()) {
                runTurn();
            }

            // Cek HP Player <= 0 -> langsung Kalah & Selesai
            if (player.isDead()) {
                showEndOfDayReport("GUGUR DI SEKTOR PERTAMBANGAN");
                showResult(false);
                return;
            }

            // Ringkasan penutup hari (berhasil mencapai Exit atau kehabisan Stamina)
            if (map.isExitReached()) {
                showEndOfDayReport("EVAKUASI AMAN MELALUI EXIT LIFT");
            } else {
                showEndOfDayReport("STAMINA HABIS - DITARIK KEMBALI KE MARKAS");
            }

            // Cek apakah sudah mencapai Hari ke-10
            if (day < MAX_DAY) {
                day++;
            } else {
                // Hari ke-10 selesai -> Summon Boss (CoreWarden)
                boolean win = fightBoss();
                showResult(win);
                return;
            }
        }
    }

    /**
     * Menangani pergantian hari: pemulihan stamina harian, fase belanja Merchant,
     * serta pembangkitan peta & pengacakan loot/musuh.
     */
    public void startDay() {
        if (day > 1) {
            player.recoverDailyStamina();
        }

        // Merchant melakukan restock barang setiap awal hari
        merchant.restock();
        runMerchantMenu();

        // Siapkan posisi awal Player dan acak peta sektor baru
        player.setPosition(0, 0);
        player.setDefending(false);
        player.clearLogs();
        map.generate(day);
        player.addLog("[SEKTOR] Hari ke-" + day + " dimulai! Kumpulkan Ore [O] dan capai Exit Lift [E].");
    }

    /**
     * Menjalankan satu giliran eksplorasi:
     * tampilkan status & peta -> validasi input W/A/S/D -> gerakkan Player & interaksi Entity
     * -> gerakkan Hostile secara polymorphic.
     */
    public void runTurn() {
        clearScreen();
        printExplorationHeader();
        map.display(player);
        printActivityLogs(player.getActionLogs());

        // Validasi input arah W/A/S/D dan batas peta tanpa mengurangi stamina jika salah
        char chosenDir;
        int nextX;
        int nextY;
        while (true) {
            System.out.print("  Masukkan arah gerak [W/A/S/D]: ");
            if (!scanner.hasNextLine()) {
                // Jika stream input ditutup paksa, anggap stamina habis agar keluar dengan aman
                player.useStamina(player.getStamina());
                return;
            }
            String input = scanner.nextLine().trim();
            if (input.length() != 1 || !Player.isValidDirection(input.charAt(0))) {
                System.out.println("  [!] Input tidak valid! Gunakan huruf W (Atas), A (Kiri), S (Bawah), atau D (Kanan).");
                continue;
            }

            chosenDir = Character.toUpperCase(input.charAt(0));
            nextX = player.previewNextX(chosenDir);
            nextY = player.previewNextY(chosenDir);

            if (!map.isInside(nextX, nextY)) {
                System.out.println("  [!] Tidak dapat melangkah keluar batas peta! Pilih arah lain.");
                continue;
            }
            break;
        }

        // Player jalan & stamina berkurang
        player.clearLogs();
        int prevX = player.getX();
        int prevY = player.getY();
        Entity targetEntity = map.getEntity(nextX, nextY);

        player.move(chosenDir);
        player.useStamina(1);
        player.addLog("[GERAK] Berpindah ke petak (" + nextX + "," + nextY + ") [-1 Stamina].");

        // Interaksi polymorphic jika petak tujuan berisi Entity
        if (targetEntity != null) {
            targetEntity.interact(player);

            if (targetEntity instanceof OreVein || targetEntity instanceof SupplyCrate) {
                map.removeEntity(nextX, nextY);
            } else if (targetEntity instanceof Hostile) {
                Hostile mob = (Hostile) targetEntity;
                if (mob.isDead()) {
                    map.removeEntity(nextX, nextY);
                } else if (!player.isDead()) {
                    // Jika musuh masih hidup, musuh mempertahankan petaknya dan Player mundur kembali
                    player.setPosition(prevX, prevY);
                    player.addLog("[INFO] " + mob.getName() + " masih bertahan di ("
                            + nextX + "," + nextY + "), posisi Anda kembali ke (" + prevX + "," + prevY + ").");
                }
            }
        }

        // Giliran Hostile Mob beraksi secara polymorphic
        map.updateHostiles(player);
    }

    /**
     * Mengecek apakah eksplorasi hari ini selesai (Stamina habis atau Exit tercapai).
     */
    public boolean isDayOver() {
        return player.getStamina() <= 0 || map.isExitReached();
    }

    /**
     * Pertarungan puncak melawan Boss CoreWarden di akhir Hari ke-10.
     * Mengembalikan true jika Boss berhasil dikalahkan, false jika Player gugur.
     */
    public boolean fightBoss() {
        CoreWarden boss = new CoreWarden(150, 14);
        player.setDefending(false);
        if (player.getStamina() < 8) {
            player.restoreStamina(8);
        }

        clearScreen();
        System.out.println("  ==========================================================================");
        System.out.println("                 PERINGATAN DARURAT - SEKTOR INTI TERKUNCI!                 ");
        System.out.println("  ==========================================================================");
        System.out.println("   Saat lift evakuasi Hari ke-10 bergerak naik menuju permukaan,");
        System.out.println("   mesin penjaga raksasa CORE WARDEN bangkit dan mengunci jalur utama!");
        System.out.println("   Kalahkan Core Warden untuk membuka gerbang keluar dan selamat!");
        System.out.println("  ==========================================================================");
        waitForEnter("  Tekan [ENTER] untuk memulai pertarungan Boss...");

        player.clearLogs();
        player.addLog("[BOSS] Core Warden aktif! Waspadai perubahan ke PHASE 2 saat HP Boss <= 50%!");

        while (!boss.isDead() && !player.isDead()) {
            clearScreen();
            printBossArena(boss);
            printActivityLogs(player.getActionLogs());

            System.out.println("  PILIH AKSI TAKTIS:");
            System.out.println("    [1] Plasma Slash       (Serangan Standar: " + player.getAttackDamage() + " DMG | Gratis)");
            System.out.println("    [2] Overcharge Drill   (Serangan Berat  : " + (player.getAttackDamage() + 15) + " DMG | Biaya: 5 Ore)");
            System.out.println("    [3] Aegis Guard        (Bertahan: -50% DMG Boss giliran ini + Pulih 8 HP)");
            System.out.println("    [4] Emergency Repair   (Heal Taktis     : +30 HP | Biaya: 8 Ore)");
            System.out.println("  --------------------------------------------------------------------------");

            int choice = promptBossAction();
            if (choice == -1) {
                return false;
            }

            player.clearLogs();
            player.setDefending(false);

            switch (choice) {
                case 1: {
                    int dmg = player.getAttackDamage();
                    boss.takeDamage(dmg);
                    player.addLog("[SERANG] Anda menebas Core Warden dengan Plasma Slash -> " + dmg + " DMG!");
                    boss.checkPhaseTransition(player);
                    break;
                }
                case 2: {
                    player.spendOre(5);
                    int heavyDmg = player.getAttackDamage() + 15;
                    boss.takeDamage(heavyDmg);
                    player.addLog("[OVERCHARGE] Bor energi menghantam inti Boss (-5 Ore) -> " + heavyDmg + " DMG!");
                    boss.checkPhaseTransition(player);
                    break;
                }
                case 3: {
                    player.setDefending(true);
                    int before = player.getHp();
                    player.heal(8);
                    int healed = player.getHp() - before;
                    player.addLog("[BERTAHAN] Perisai Aegis diaktifkan (-50% DMG diterima) & memulihkan +" + healed + " HP.");
                    break;
                }
                case 4: {
                    player.spendOre(8);
                    int before = player.getHp();
                    player.heal(30);
                    int healed = player.getHp() - before;
                    player.addLog("[REPAIR] Menggunakan 8 Ore untuk perbaikan darurat -> +" + healed + " HP!");
                    break;
                }
                default:
                    break;
            }

            if (boss.isDead()) {
                player.incrementMonstersDefeated();
                player.addLog("[KEMENANGAN] INTI CORE WARDEN HANCUR LEBUR! Gerbang permukaan terbuka!");
                break;
            }

            // Giliran CoreWarden beraksi secara polymorphic
            boss.act(player);
            player.setDefending(false);
        }

        // Tampilkan kondisi akhir arena Boss sebelum menuju layar hasil akhir
        clearScreen();
        printBossArena(boss);
        printActivityLogs(player.getActionLogs());
        waitForEnter("  Tekan [ENTER] untuk melihat hasil akhir perjuangan Anda...");

        return boss.isDead() && !player.isDead();
    }

    /**
     * Menampilkan layar hasil akhir permainan (Menang / Kalah) beserta statistik lengkap,
     * lalu menunggu tombol Enter sebelum menutup aplikasi.
     */
    public void showResult(boolean win) {
        clearScreen();
        System.out.println("  ==========================================================================");
        if (win) {
            System.out.println("                         *** VICTORY - MISI BERHASIL ***                    ");
            System.out.println("  ==========================================================================");
            System.out.println("   Selamat, " + player.getName() + "! Anda berhasil bertahan selama 10 hari");
            System.out.println("   di Sektor-9 dan menghancurkan Core Warden untuk kembali ke permukaan!");
        } else {
            System.out.println("                        *** GAME OVER - MISI GAGAL ***                      ");
            System.out.println("  ==========================================================================");
            System.out.println("   Sayang sekali, " + player.getName() + " gugur dalam tugas di Sektor-9.");
            System.out.println("   Persiapkan manajemen stamina, suplai, dan rute yang lebih baik lagi!");
        }
        System.out.println("  --------------------------------------------------------------------------");
        System.out.println("   RINGKASAN STATISTIK AKHIR:");
        System.out.printf("     - Nama Penambang        : %s%n", player.getName());
        System.out.printf("     - Status Akhir          : %s%n", win ? "MENANG (SURVIVED)" : "KALAH (DEFEATED)");
        System.out.printf("     - Hari Tercapai         : Hari ke-%d dari %d%n", day, MAX_DAY);
        System.out.printf("     - Sisa HP               : %d / %d%n", player.getHp(), player.getMaxHp());
        System.out.printf("     - Sisa Ore Tersimpan    : %d Ore%n", player.getOre());
        System.out.printf("     - Total Ore Ditambang   : %d Ore%n", player.getTotalOreCollected());
        System.out.printf("     - Total Musuh Hancur    : %d Unit%n", player.getMonstersDefeated());
        System.out.println("  ==========================================================================");
        waitForEnter("  Tekan [ENTER] untuk menutup jendela permainan...");
    }

    // --- UI & Helper Methods ---

    /**
     * Membersihkan layar console Windows CMD menggunakan perintah cls.
     */
    public static void clearScreen() {
        if (System.console() == null) {
            System.out.println();
            return;
        }
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 30; i++) {
                System.out.println();
            }
        }
    }

    private void printTitleBanner() {
        System.out.println("  ==========================================================================");
        System.out.println("      ____ ___  ____  _____   ____  ____  _____    _    _  _____ ____       ");
        System.out.println("     / ___/ _ \\|  _ \\| ____| | __ )|  _ \\| ____|  / \\  | |/ / ____|  _ \\    ");
        System.out.println("    | |  | | | | |_) |  _|   |  _ \\| |_) |  _|   / _ \\ | ' /|  _| | |_) |   ");
        System.out.println("    | |__| |_| |  _ <| |___  | |_) |  _ <| |___ / ___ \\| . \\| |___|  _ <    ");
        System.out.println("     \\____\\___/|_| \\_\\_____| |____/|_| \\_\\_____/_/   \\_\\_|\\_\\_____|_| \\_\\   ");
        System.out.println("                                                                            ");
        System.out.println("                SURVIVAL MINING CONSOLE RPG - 10 DAYS IN SECTOR-9           ");
        System.out.println("  ==========================================================================");
        System.out.println("   MISI ANDA:");
        System.out.println("   1. Bertahan hidup selama 10 Hari menambang Ore [O] di Sektor-9.");
        System.out.println("   2. Gunakan Ore di Merchant setiap pagi untuk membeli HP & Stamina.");
        System.out.println("   3. Hindari atau kalahkan Sentry Turret [T] dan Stalker [S].");
        System.out.println("   4. Capai Exit Lift [E] sebelum Stamina habis, lalu kalahkan CORE WARDEN!");
        System.out.println("  ==========================================================================");
    }

    private String promptPlayerName() {
        while (true) {
            System.out.print("  Masukkan Nama Penambang Anda: ");
            if (!scanner.hasNextLine()) {
                return "Miner-01";
            }
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("  [!] Nama tidak boleh kosong. Silakan isi nama Anda.");
        }
    }

    private void runMerchantMenu() {
        String feedback = "[MERCHANT] \"Selamat pagi, " + player.getName()
                + "! Silakan pilih suplai sebelum turun ke sektor tambang.\"";

        while (true) {
            clearScreen();
            System.out.println("  ==========================================================================");
            System.out.printf("   POS LOGISTIK MERCHANT - AWAL HARI KE-%d / %d%n", day, MAX_DAY);
            System.out.println("  ==========================================================================");
            System.out.printf("   Penambang : %-18s | Ore Tersedia : %d Ore%n", player.getName(), player.getOre());
            System.out.printf("   HP        : %s %d/%d%n",
                    buildBar(player.getHp(), player.getMaxHp(), 15), player.getHp(), player.getMaxHp());
            System.out.printf("   Stamina   : %s %d/%d%n",
                    buildBar(player.getStamina(), player.getMaxStamina(), 15),
                    player.getStamina(), player.getMaxStamina());
            System.out.println("  --------------------------------------------------------------------------");

            merchant.showStock();
            System.out.println("  Info: " + feedback);
            System.out.println("  --------------------------------------------------------------------------");
            System.out.print("  Pilih nomor barang untuk dibeli [1-" + merchant.getStockSize()
                    + "], atau ketik [0] lanjut ke Peta: ");

            if (!scanner.hasNextLine()) {
                return;
            }
            String raw = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                feedback = "[!] Masukkan angka pilihan yang valid!";
                continue;
            }

            if (choice == 0) {
                break;
            }

            if (choice < 1 || choice > merchant.getStockSize()) {
                feedback = "[!] Nomor barang tidak tersedia di daftar stok!";
                continue;
            }

            Item preview = merchant.getItem(choice - 1);
            int beforeHp = player.getHp();
            int beforeStamina = player.getStamina();

            if (merchant.sell(player, choice - 1)) {
                int gainedHp = player.getHp() - beforeHp;
                int gainedStm = player.getStamina() - beforeStamina;
                feedback = "[SUKSES] Membeli " + preview.getName() + " (-" + preview.getPrice()
                        + " Ore) -> +" + gainedHp + " HP, +" + gainedStm + " Stamina!";
            } else {
                feedback = "[GAGAL] Ore tidak cukup untuk membeli " + preview.getName()
                        + " (Harga: " + preview.getPrice() + " Ore).";
            }
        }
    }

    private void printExplorationHeader() {
        System.out.println("  ==========================================================================");
        System.out.printf("   HARI KE-%-2d / %-2d  |  Penambang: %-16s  |  Koordinat: (%d, %d)%n",
                day, MAX_DAY, player.getName(), player.getX(), player.getY());
        System.out.println("  --------------------------------------------------------------------------");
        System.out.printf("   HP      : %s %3d/%-3d   |  Ore : %-4d  |  ATK : %d%n",
                buildBar(player.getHp(), player.getMaxHp(), 14),
                player.getHp(), player.getMaxHp(), player.getOre(), player.getAttackDamage());
        System.out.printf("   Stamina : %s %3d/%-3d%n",
                buildBar(player.getStamina(), player.getMaxStamina(), 14),
                player.getStamina(), player.getMaxStamina());
        System.out.println("  ==========================================================================");
    }

    private void printActivityLogs(List<String> logs) {
        System.out.println("  --------------------------------------------------------------------------");
        System.out.println("  LOG KEJADIAN TERAKHIR:");
        if (logs == null || logs.isEmpty()) {
            System.out.println("    > Belum ada aktivitas.");
        } else {
            for (String log : logs) {
                System.out.println("    > " + log);
            }
        }
        System.out.println("  --------------------------------------------------------------------------");
    }

    private void showEndOfDayReport(String statusTitle) {
        clearScreen();
        printExplorationHeader();
        map.display(player);
        printActivityLogs(player.getActionLogs());
        System.out.println("  ==========================================================================");
        System.out.println("   AKHIR HARI KE-" + day + ": " + statusTitle);
        System.out.println("  ==========================================================================");
        waitForEnter("  Tekan [ENTER] untuk melanjutkan...");
    }

    private void printBossArena(CoreWarden boss) {
        String phaseLabel = (boss.getPhase() == 1) ? "PHASE 1 - GUARDIAN MODE" : "PHASE 2 - OVERDRIVE MODE (!)";
        System.out.println("  ==========================================================================");
        System.out.println("                   PERTARUNGAN PUNCAK HARI KE-10: CORE WARDEN               ");
        System.out.println("  ==========================================================================");
        System.out.printf("   BOSS   : %-28s | Status: %s%n", boss.getName() + " [W]", phaseLabel);
        System.out.printf("   HP BOSS: %s %3d/%-3d   | ATK Boss: %d%n",
                buildBar(boss.getHp(), boss.getMaxHp(), 20),
                boss.getHp(), boss.getMaxHp(), boss.getDamage());
        System.out.println("  --------------------------------------------------------------------------");
        if (boss.getPhase() == 1) {
            System.out.println("                     .-------.                                              ");
            System.out.println("                    /  [ W ]  \\     \"PENYUSUP TERDETEKSI DI SEKTOR INTI.\" ");
            System.out.println("                   |  ( === )  |                                            ");
            System.out.println("                    \\_________/                                             ");
        } else {
            System.out.println("                   \\  .-------.  /                                          ");
            System.out.println("                    * / [W!!] \\ *   \"PERINGATAN: INTI REAKTOR OVERDRIVE!\" ");
            System.out.println("                   * | (#####) | *                                          ");
            System.out.println("                    * \\_______/ *                                           ");
        }
        System.out.println("  --------------------------------------------------------------------------");
        System.out.printf("   PLAYER : %-18s | HP: %s %3d/%-3d | Ore: %d%n",
                player.getName(),
                buildBar(player.getHp(), player.getMaxHp(), 12),
                player.getHp(), player.getMaxHp(), player.getOre());
    }

    private int promptBossAction() {
        while (true) {
            System.out.print("  Pilih aksi [1-4]: ");
            if (!scanner.hasNextLine()) {
                return -1;
            }
            String raw = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Masukkan angka 1 sampai 4!");
                continue;
            }

            if (choice < 1 || choice > 4) {
                System.out.println("  [!] Pilihan tidak tersedia! Pilih angka 1 - 4.");
                continue;
            }
            if (choice == 2 && player.getOre() < 5) {
                System.out.println("  [!] Ore tidak cukup untuk Overcharge Drill (Butuh 5 Ore, Anda punya "
                        + player.getOre() + " Ore)!");
                continue;
            }
            if (choice == 4 && player.getOre() < 8) {
                System.out.println("  [!] Ore tidak cukup untuk Emergency Repair (Butuh 8 Ore, Anda punya "
                        + player.getOre() + " Ore)!");
                continue;
            }
            return choice;
        }
    }

    private String buildBar(int current, int max, int width) {
        int safeMax = Math.max(1, max);
        int clamped = Math.max(0, Math.min(current, safeMax));
        int filled = (int) Math.round(((double) clamped / safeMax) * width);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            sb.append(i < filled ? '#' : '.');
        }
        sb.append("]");
        return sb.toString();
    }

    private void waitForEnter(String message) {
        System.out.print(message);
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }
}
