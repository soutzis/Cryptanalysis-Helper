import java.util.ArrayList;
import java.util.Collection;

/**
 * A simple class that extends ArrayList and overrides the 'get(int i)' method,
 * in order to give it Circular functionality.
 * E.g. If you perform list.get(-1), you will not get an exception, but the last
 * element of the ArrayList instead.
 *
 * @param <E> The type parameter of this ArrayList
 *
 * @author psoutzis
 */

public class CircularArrayList<E> extends ArrayList<E> {

    /**
     * Default constructor of CircularArrayList
     */
    public CircularArrayList(){

        super();
    }

    /**
     * This constructor will initialize the CircularArrayList with a specified size.
     * The arrayList can add more elements and grow its size normally.
     * @param startSize The starting size to allocate to this CircularArrayList
     */
    public CircularArrayList(int startSize){

        super(startSize);
    }

    /**
     * This constructor will make CircularArrayList accept
     * other type parameters that implement the Collection interface
     * @param c is the collection object to convert into a circular ArrayList
     */
    public CircularArrayList(Collection<? extends E> c){

        super(c);
    }

    /**
     * This overrides the get() method in arrayList, so that
     * when entering an index that is out of bounds, it will
     * go to the start of the arrayList and count from there.
     * @param index The index to use
     * @return The element in the List, corresponding to the circular position
     * of the index entered.
     */
    @Override
    public E get(int index){
        // While index is larger than list's size, keep subtracting
        // the size, until it's in List's size boundaries.
        while(index > this.size()-1) {
            index = index - this.size();
        }

        // While index is a negative number, then keep adding size() to that number,
        // until it's in List's size boundaries.
        while(index < 0)
            index = index + this.size();

        return super.get(index);
    }
}
