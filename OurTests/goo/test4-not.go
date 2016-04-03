package main

import "fmt"


// The provided code does not support assigning to booleans[1] so
// expression like (1 == 1) were used instead.
//
// Also the provided code never default assigned booleans.
//
// [1] Writing "var b bool = false" crashes
//     So does
//       "var b bool
//        b = false"
func main() {

    fmt.Printf("(1 == 1): %d\n", (1 == 1))

    fmt.Printf("!(1 == 1): %d\n", !(1 == 1));

    fmt.Printf("!!(1 == 1): %d\n", !!(1 == 1));

}
