public class ObjectFields {
    static class Point {
        int x;
        int y;
        Point(int x, int y) { this.x = x; this.y = y; }
    }
    public static void main(String[] args) {
        Point p = new Point(3, 7);
        System.out.println(p.x);
    }
}
