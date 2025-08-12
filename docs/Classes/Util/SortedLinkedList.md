# In Short

Joins standard Java generic LinkedList with Comparable elements to achieve List of elements that is sorted according to the **natural ordering** of its elements.
Therefore, stored elements must implement the Comparable interface.

# Details

Methods of Java's LinkedList that explicitly specify the position of an element are not supported (since order is defined by its elements). 
These disabled methods will raise an Exception:

* `add(int i, T t) `
* `addFirst(T t)`
* `addLast(T t)`
* `offer(T t) `
* `offerFirst(T t)`
* `offerLast(T t)`
* `set(int i, T t)`
* `sort(Comparator<? super T> c)`

# See also

* [Comparable](https://docs.oracle.com/javase/8/docs/api/java/lang/Comparable.html)
* [LinkedList](https://docs.oracle.com/javase/8/docs/api/java/util/LinkedList.html)