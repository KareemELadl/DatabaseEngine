public class MyOctree{

    private OctPoint root;
    private OctPoint[] children;
    private OctPoint topLeftFront, bottomRightBack;


    public MyOctree() {
        root = new OctPoint();
        
    }
}