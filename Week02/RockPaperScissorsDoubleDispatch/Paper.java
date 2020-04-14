public class Paper extends Attack {

    public void handle(Attack a){
        a.handle(this);
    }

    public void handle(Rock a){
        System.out.println("Paper vs rock paper win");
    }
    public void handle(Paper a){
        System.out.println("Paper vs paper we draw");
    }
    public void handle(Scissors a){
        System.out.println("Paper vs scissors paper lose");
    }
}
