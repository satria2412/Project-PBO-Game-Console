/**
 * Abstract superclass untuk seluruh objek yang berada di dalam peta (GameMap).
 * Menerapkan konsep INHERITANCE & POLYMORPHISM melalui method abstract interact(Player p),
 * serta ENCAPSULATION menggunakan modifier protected (#) sesuai spesifikasi UML.
 */
public abstract class Entity {
    // [ENCAPSULATION & INHERITANCE] Atribut protected sesuai UML agar dapat diakses subclass
    protected char symbol;
    protected int x;
    protected int y;

    public Entity(char symbol, int x, int y) {
        this.symbol = symbol;
        this.x = x;
        this.y = y;
    }

    /**
     * [POLYMORPHISM] Setiap jenis Entity memiliki perilaku berbeda saat berinteraksi dengan Player.
     */
    public abstract void interact(Player p);

    public char getSymbol() {
        return symbol;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
