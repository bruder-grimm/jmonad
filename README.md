# Monads for Java

Gives you some nice monads for creating pipelines in java

## Where to start
```java
// Create variables of which the creation process might fail
Try<Integer> a = Try.applyThrowing(() -> hasDeclaredThrows());
Try<Integer> b = Try.apply(() -> throwsSomethingUndeclared());

// Create values which might or might not be present
Option<String> c = Option.apply(couldBeNull());
Option<String> d = Some.apply("This does exist");
Option<String> e = None.apply();

// Create a disjunct set to model either/or situations
Either<String, Integer> string = Left.apply("hello");
Either<String, Integer> integer = Right.apply(5);
```

## Where to go

Obviously, you can go the java route and be like:
```java
Try<Integer> possiblyDoubled = a.map(someInt -> someInt * 2);
Try<Integer> possiblyFiltered = possiblyDoubled.filter(x -> x % 2 == 0);

Try<Integer> possiblyRecovered = possiblyFiltered.recover(exception -> {
                             Logger.log(exception.getMessage());
                             return 10;
                         });
```

Or, you can build a pipeline by chaining these sort of operations together:
```java
Try<Integer> possibleResult = a
    .map(someInt -> someInt * 2)
    .filter(x -> x % 2 == 0)
    .recover(exception -> {
        Logger.log(exception.getMessage());
        return 10;
    });
```

```java
String possibleResult = Option.apply(getUserInput())
    .map(String::toUpperCase)
    .filter(allowedInput::contains)
    .getOrElse("defaultInput");
```

```java
Option<Repository> repository;

/** By creating a function that returns a monad you force the callee to handle either situation
  * No more nullpointers, no more try catch blocks, no more java frustrations */
public Either<Error, List<Integer>> getIds() {
    if (repository.isDefined()) {
        return Right.apply(repository.getAll());   
    }
    return Left.apply(new Error("Repository isn't up"));
}

public void printIds() {
    // defining fallbacks, map over possible values, in the most concise way possible
    String output = getIds().fold(
        error -> {
            errorHandler.handleError(error);
            return error.toUpperCase;
        },
        ids -> {
            Logger.log("got all ids");
            return ids.mkString;
        }
    );

    printer.print(output);
}
```

You can now also create Futures to deferr evaluation of suppliers to other threads.
Scheduling is taken care of, use them just like the other monads.
```java
Future<Integer> eventual = Future.apply(() -> someExpensiveCalculation());
    .map(someInt -> someInt * 2)
    .flatMap(x -> someOtherExpensiveCalculation(x))
    .filter(y -> y > 20);

eventual.onSuccess(result -> System.out.println("Calculated result " + result));


Try<Integer> value = eventual.await(Duration.ofMillis(20));

return value.getOrElse(42);
```

You can await futures with a duration, register callbacks that are going to be executed once they're done, etc.

Everything works like you'd expect it to.


## What to do
It's basically just like `Try`, `Option`, `Future`, and `Either` in Scala, so just look for some Documentation on those.
