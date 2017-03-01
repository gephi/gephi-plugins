package cz.cvut.fit.gephi.multimode;

/**
 *
 * @author Jaroslav Kuchar
 */
public class ValueCombination {

    private String first, second;

    public ValueCombination(String first, String second) {
        this.first = first;
        this.second = second;
    }

    public String getFirst() {
        return first;
    }

    public String getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return first + " - " + second;
    }
}
