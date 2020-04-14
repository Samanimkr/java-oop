public abstract class Attack {
    abstract void handle(Attack attack);
    abstract void handle(Rock attack);
    abstract void handle(Paper attack);
    abstract void handle(Scissors attack);
}
