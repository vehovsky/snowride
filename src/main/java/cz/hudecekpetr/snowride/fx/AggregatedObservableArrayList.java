package cz.hudecekpetr.snowride.fx;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This class aggregates several other Observed Lists (sublists), observes changes on those sublists and applies those same changes to the
 * aggregated list.
 * Inspired by:
 * - http://stackoverflow.com/questions/25705847/listchangelistener-waspermutated-block
 * - http://stackoverflow.com/questions/37524662/how-to-concatenate-observable-lists-in-javafx
 * - https://github.com/lestard/advanced-bindings/blob/master/src/main/java/eu/lestard/advanced_bindings/api/CollectionBindings.java
 * Posted result on: http://stackoverflow.com/questions/37524662/how-to-concatenate-observable-lists-in-javafx
 */
public class AggregatedObservableArrayList<T> {

    private final List<ObservableList<T>> lists = new ArrayList<>();
    final private List<Integer> sizes = new ArrayList<>();
    final private List<InternalListModificationListener> listeners = new ArrayList<>();
    private final ObservableList<T> aggregatedList = FXCollections.observableArrayList();

    public AggregatedObservableArrayList() {

    }

    /**
     * The Aggregated Observable List. This list is unmodifiable, because sorting this list would mess up the entire bookkeeping we do here.
     *
     * @return an unmodifiable view of the aggregatedList
     */
    public ObservableList<T> getAggregatedList() {
        return FXCollections.unmodifiableObservableList(aggregatedList);
    }

    public void appendList(ObservableList<T> list) {
        assert !lists.contains(list) : "List is already contained: " + list;
        lists.add(list);
        final InternalListModificationListener listener = new InternalListModificationListener(list);
        list.addListener(listener);
        //System.out.println("list = " + list + " puttingInMap=" + list.hashCode());
        sizes.add(list.size());
        aggregatedList.addAll(list);
        listeners.add(listener);
        assert lists.size() == sizes.size() && lists.size() == listeners.size() :
                "lists.size=" + lists.size() + " not equal to sizes.size=" + sizes.size() + " or not equal to listeners.size=" + listeners.size();
    }

    public void prependList(ObservableList<T> list) {
        assert !lists.contains(list) : "List is already contained: " + list;
        lists.add(0, list);
        final InternalListModificationListener listener = new InternalListModificationListener(list);
        list.addListener(listener);
        //System.out.println("list = " + list + " puttingInMap=" + list.hashCode());
        sizes.add(0, list.size());
        aggregatedList.addAll(0, list);
        listeners.add(0, listener);
        assert lists.size() == sizes.size() && lists.size() == listeners.size() :
                "lists.size=" + lists.size() + " not equal to sizes.size=" + sizes.size() + " or not equal to listeners.size=" + listeners.size();
    }

    public void removeList(ObservableList<T> list) {
        assert lists.size() == sizes.size() && lists.size() == listeners.size() :
                "lists.size=" + lists.size() + " not equal to sizes.size=" + sizes.size() + " or not equal to listeners.size=" + listeners.size();
        final int index = lists.indexOf(list);
        if (index < 0) {
            throw new IllegalArgumentException("Cannot remove a list that is not contained: " + list + " lists=" + lists);
        }
        final int startIndex = getStartIndex(list);
        final int endIndex = getEndIndex(list, startIndex);
        // we want to find the start index of this list inside the aggregated List. End index will be start + size - 1.
        lists.remove(list);
        sizes.remove(index);
        final InternalListModificationListener listener = listeners.remove(index);
        list.removeListener(listener);
        aggregatedList.remove(startIndex, endIndex + 1); // end + 1 because end is exclusive
        assert lists.size() == sizes.size() && lists.size() == listeners.size() :
                "lists.size=" + lists.size() + " not equal to sizes.size=" + sizes.size() + " or not equal to listeners.size=" + listeners.size();
    }

    /**
     * Get the start index of this list inside the aggregated List.
     * This is a private function. we can safely asume, that the list is in the map.
     *
     * @param list the list in question
     * @return the start index of this list in the aggregated List
     */
    private int getStartIndex( ObservableList<T> list) {
        int startIndex = 0;
        //System.out.println("=== searching startIndex of " + list);
        assert lists.size() == sizes.size() : "lists.size=" + lists.size() + " not equal to sizes.size=" + sizes.size();
        final int listIndex = lists.indexOf(list);
        for (int i = 0; i < listIndex; i++) {
            final Integer size = sizes.get(i);
            startIndex += size;
            //System.out.println(" startIndex = " + startIndex + " added=" + size);
        }
        //System.out.println("startIndex = " + startIndex);
        return startIndex;
    }

    /**
     * Get the end index of this list inside the aggregated List.
     * This is a private function. we can safely asume, that the list is in the map.
     *
     * @param list       the list in question
     * @param startIndex the start of the list (retrieve with {@link #getStartIndex(ObservableList)}
     * @return the end index of this list in the aggregated List
     */
    private int getEndIndex( ObservableList<T> list, int startIndex) {
        assert lists.size() == sizes.size() : "lists.size=" + lists.size() + " not equal to sizes.size=" + sizes.size();
        final int index = lists.indexOf(list);
        return startIndex + sizes.get(index) - 1;
    }

    private class InternalListModificationListener implements ListChangeListener<T> {


        private final ObservableList<T> list;

        public InternalListModificationListener( ObservableList<T> list) {
            this.list = list;
        }

        /**
         * Called after a change has been made to an ObservableList.
         *
         * @param change an object representing the change that was done
         * @see Change
         */
        @Override
        public void onChanged(Change<? extends T> change) {
            final ObservableList<? extends T> changedList = change.getList();
            final int startIndex = getStartIndex(list);
            final int index = lists.indexOf(list);
            final int newSize = changedList.size();
            //System.out.println("onChanged for list=" + list + " aggregate=" + aggregatedList);
            while (change.next()) {
                final int from = change.getFrom();
                final int to = change.getTo();
                //System.out.println(" startIndex=" + startIndex + " from=" + from + " to=" + to);
                if (change.wasPermutated()) {
                    final ArrayList<T> copy = new ArrayList<>(aggregatedList.subList(startIndex + from, startIndex + to));
                    //System.out.println("  permutating sublist=" + copy);
                    for (int oldIndex = from; oldIndex < to; oldIndex++) {
                        int newIndex = change.getPermutation(oldIndex);
                        copy.set(newIndex - from, aggregatedList.get(startIndex + oldIndex));
                    }
                    //System.out.println("  permutating done sublist=" + copy);
                    aggregatedList.subList(startIndex + from, startIndex + to).clear();
                    aggregatedList.addAll(startIndex + from, copy);
                } else if (change.wasUpdated()) {
                    // do nothing
                } else {
                    if (change.wasRemoved()) {
                        List<? extends T> removed = change.getRemoved();
                        //System.out.println("  removed= " + removed);
                        // IMPORTANT! FROM == TO when removing items.
                        aggregatedList.remove(startIndex + from, startIndex + from + removed.size());
                    }
                    if (change.wasAdded()) {
                        List<? extends T> added = change.getAddedSubList();
                        //System.out.println("  added= " + added);
                        //add those elements to your data
                        aggregatedList.addAll(startIndex + from, added);
                    }
                }
            }
            // update the size of the list in the map
            //System.out.println("list = " + list + " puttingInMap=" + list.hashCode());
            sizes.set(index, newSize);
            //System.out.println("listSizesMap = " + sizes);
        }

    }

    public String dump(Function<T, Object> function) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        aggregatedList.forEach(el -> sb.append(function.apply(el)).append(","));
        final int length = sb.length();
        sb.replace(length - 1, length, "");
        sb.append("]");
        return sb.toString();
    }
}