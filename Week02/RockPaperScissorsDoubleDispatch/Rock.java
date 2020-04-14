public class Rock extends Attack {

    public void handle(Attack a){
        a.handle(this);
    }

    public void handle(Rock a){
        System.out.println("Rock vs rock we draw");
    }
    public void handle(Paper a){
        System.out.println("Rock vs paper rock lose");
    }
    public void handle(Scissors a){
        System.out.println("Rock vs scissors rock win");
    }
}