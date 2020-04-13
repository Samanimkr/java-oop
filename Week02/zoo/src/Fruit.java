public class Fruit extends Food {
    @Override
    public String eaten(Animal animal) {
      return "animal eats fruit";
    }

    @Override
    public String eaten(String animal) {
        return animal + " eats fruit";
    }
}
