public class Chocolate extends Food {

    @Override
    public String eaten(Animal animal) {
        return "animal eats chocolate";
    }

    @Override
    public String eaten(String animal) {
        return animal + " eats chocolate";
    }
  
}
