public class Runner {
    public static void main (String [] args){
        Attack a1 = new Rock();
        Attack a2 = new Scissors();
        Attack a3 = new Paper();

        a1.handle(a1);
        a1.handle(a2);
        a1.handle(a3);
        a2.handle(a1);
        a2.handle(a2);
        a2.handle(a3);
        a3.handle(a1);
        a3.handle(a2);
        a3.handle(a3);
    }
}
