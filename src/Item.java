/**
 * Representasi barang suplai yang dijual oleh Merchant.
 * Menerapkan konsep ENCAPSULATION: seluruh atribut disimpan secara private.
 */
public class Item {
    // [ENCAPSULATION] Seluruh atribut Item bersifat private
    private String name;
    private int price;
    private int healAmount;
    private int staminaAmount;

    public Item(String name, int price, int healAmount, int staminaAmount) {
        this.name = name;
        this.price = price;
        this.healAmount = healAmount;
        this.staminaAmount = staminaAmount;
    }

    /**
     * Menerapkan efek pemulihan HP dan/atau Stamina item kepada Player.
     */
    public void applyTo(Player p) {
        if (p == null) {
            return;
        }
        if (healAmount > 0) {
            p.heal(healAmount);
        }
        if (staminaAmount > 0) {
            p.restoreStamina(staminaAmount);
        }
    }

    public int getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public int getHealAmount() {
        return healAmount;
    }

    public int getStaminaAmount() {
        return staminaAmount;
    }

    public String getEffectDescription() {
        StringBuilder sb = new StringBuilder();
        if (healAmount > 0) {
            sb.append("+").append(healAmount).append(" HP");
        }
        if (staminaAmount > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("+").append(staminaAmount).append(" Stamina");
        }
        return sb.toString();
    }
}
