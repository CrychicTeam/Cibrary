package org.pickaid.pibrary.api.screen;

public record PiScreenRect(int x, int y, int width, int height) {
    public PiScreenRect {
        if (width < 0) {
            throw new IllegalArgumentException("width must not be negative");
        }
        if (height < 0) {
            throw new IllegalArgumentException("height must not be negative");
        }
    }

    public boolean contains(PiScreenPoint point) {
        return point.x() >= x
                && point.y() >= y
                && point.x() < x + width
                && point.y() < y + height;
    }

    public PiScreenPoint toLocal(PiScreenPoint point) {
        return PiScreenPoint.local(point.x() - x, point.y() - y);
    }
}
