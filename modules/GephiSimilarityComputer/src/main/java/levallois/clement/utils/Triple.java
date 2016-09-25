/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package levallois.clement.utils;

/**
 *
 * @author Clement
 */
public class Triple<L, M, R> {
    
    private final L left;
    private final M middle;    
    private final R right;

    public Triple(L left, M middle, R right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public M getMiddle() {
        return middle;
    }

    public R getRight() {
        return right;
    }    
    
}
