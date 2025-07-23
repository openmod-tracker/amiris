# In Short
Represents a polynomial function of any degree that can be evaluated at any position.

```
    a * x^0 + b * x^1 + c * x^2 + ...
```

# Details
Initialise with a List or Array of prefactors in a strictly ascending order, e.g. [a,b,c] in the above example. 
Not given prefactors are assumed to be Zero.
You cannot leave out any values, e.g. [a,c] is not possible - since a pair of values is always interpreted as prefacors [a,b].

Use `evaluateAt(x)` function to evaluate the polynomial at given position *x*.