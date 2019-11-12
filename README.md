# Monads for Java

Gives you some nice monads for creating pipelines in java

## Where to start
```
Try<Integer> a = Try.applyThrowing(() -> hasDeclaredThrows());
Try<Integer> b = Try.apply(() -> throwsSomethingUndeclared());

Option<String> c = Option.apply(couldBeNull());

Either<String, Integer> string = Left.apply("hello");
Either<String, Integer> integer = Right.apply(5);
```

## Where to go

Obviously, you can go the java route and be like:
```
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
Option<String> possibleResult = Try.applyThrowing(() -> getUserInput())
    .map(String::toUpperCase)
    .filter(allowedInput::contains)
    .recoverWith(exception -> {
        Logger.log(exception.getMessage());
        printer.print("Invalid input, please try again");
        return Try.applyThrowing(() -> getUserInput());
    })
    .toOption();
```

```java
Option<Repository> repository;

public Either<Error, List<Integer>> getIds() {
    if (repository.isDefined()) {
        return Right.apply(repository.getAll());   
    }
    return Left.apply(new Error("Repository isn't up"));
}

public void printIds() {
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

## What to do
It's basically just like `Try`, `Option` and `Either` in Scala, so just look for some Documentation on those.