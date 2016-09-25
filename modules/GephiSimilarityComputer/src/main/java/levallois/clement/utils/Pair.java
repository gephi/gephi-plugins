/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package levallois.clement.utils;

/**
 *
 * @author C. Levallois
 * retrieved from http://stackoverflow.com/questions/521171/a-java-collection-of-value-pairs-tuples
 */
public class Pair<L,R> {

  private final L left;
  private final R right;

  public Pair(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public L getLeft() { return left; }
  public R getRight() { return right; }

  @Override
  public int hashCode() { return left.hashCode() ^ right.hashCode(); }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
          return false;
      }
    if (!(o instanceof Pair)) {
          return false;
      }
    Pair pairo = (Pair) o;
    return this.left.equals(pairo.getLeft()) &&
           this.right.equals(pairo.getRight());
  }

}