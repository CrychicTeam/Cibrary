package org.pickaid.pibrary.api.screen;

public record PiScreenPoint(int x, int y, PiScreenPointSpace space) {
    public static PiScreenPoint screen(int x, int y) {
        return new PiScreenPoint(x, y, PiScreenPointSpace.SCREEN);
    }

    public static PiScreenPoint local(int x, int y) {
        return new PiScreenPoint(x, y, PiScreenPointSpace.LOCAL);
    }
}
