package com.example.colorcalculator;

public class Color {
    int red, green, blue, color_red, color_green, color_blue;
    String Name;

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public int getColor_red() {
        return color_red;
    }

    public void setColor_red(int color_red) {
        this.color_red = color_red;
    }

    public int getColor_green() {
        return color_green;
    }

    public void setColor_green(int color_green) {
        this.color_green = color_green;
    }

    public int getColor_blue() {
        return color_blue;
    }

    public void setColor_blue(int color_blue) {
        this.color_blue = color_blue;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    @Override
    public String toString() {
        return "Color{" +
                "red=" + red +
                ", green=" + green +
                ", blue=" + blue +
                ", color_red=" + color_red +
                ", color_green=" + color_green +
                ", color_blue=" + color_blue +
                ", Name='" + Name + '\'' +
                '}';
    }
}

