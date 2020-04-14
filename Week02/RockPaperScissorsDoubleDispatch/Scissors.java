public class Scissors extends Attack {

    public void handle(Attack a){
        a.handle(this);
    }

    public void handle(Rock a){
        System.out.println("Scissors vs rock scissors lose");
    }
    public void handle(Paper a){
        System.out.println("Scissors vs paper scissors win");
    }
    public void handle(Scissors a){
        System.out.println("Scissors vs scissors we draw");
    }
}
