package main

import "fmt"

// See test4-not.goo for why we are using expression like (1 == 1) and not
// boolean variables.


// Used to show the short circuit evaluation.
func evalTrue() bool {
    fmt.Printf(" evalTrue ");
    return (1 == 1)
}


func main() {

    fmt.Printf("%d\n", (1 == 1) && evalTrue() );

    fmt.Printf("%d\n", (1 == 0) && evalTrue() );

    fmt.Printf("%d\n", (1 == 1) && (1 == 0) );

    fmt.Printf("%d\n", (1 == 0) && (1 == 0) );

// Expected output:
//       evalTrue 1
//      0
//      0
//      0

}
